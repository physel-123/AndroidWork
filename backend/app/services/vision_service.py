"""多模态识别服务：封装通义千问 Qwen-VL 调用。

- 有 DASHSCOPE_API_KEY 时，真实调用 Qwen-VL 识别手掌/面部特征；
- 无 key 时，返回 mock 特征，保证链路可跑通。
- 模型只负责「客观描述特征」，解读文案由语料库生成（见 palm_face_corpus）。
"""
import base64
import json
import logging
import re

from app.core.config import settings
from app.data import palm_face_corpus as c

logger = logging.getLogger(__name__)

_MODEL = "qwen3.8-flash"

_PALM_PROMPT = """请观察这张手掌照片，客观描述其手相特征，不要做任何吉凶预测。
请严格按以下 JSON 格式输出（只输出 JSON，不要多余文字）：
{
  "手型": "方手/圆手/长手/短手 中选一个",
  "手指": "修长/粗短/匀称 中选一个",
  "感情线": "清晰深长/平直/呈波浪/多条支线 中选一个",
  "生命线": "清晰深长/起点高/弧度大 中选一个",
  "智慧线": "清晰深长/平直/下垂 中选一个",
  "掌纹": "川字纹/断掌纹/人字纹/普通纹路 中选一个"
}"""

_FACE_PROMPT = """请观察这张人脸照片，客观描述其面相特征，不要做任何吉凶预测。
请严格按以下 JSON 格式输出（只输出 JSON，不要多余文字）：
{
  "脸型": "圆脸/方脸/瓜子脸/长脸 中选一个",
  "眉毛": "浓眉/淡眉/长眉/弯眉 中选一个",
  "眼睛": "大眼/细长/双眼皮/单眼皮 中选一个",
  "鼻子": "高挺/圆润/小巧 中选一个",
  "嘴唇": "厚唇/薄唇/唇形端正 中选一个"
}"""


def _call_qwen_vl(image_base64: str, prompt: str) -> dict | None:
    """调用 DashScope Qwen-VL，返回解析后的特征 dict；失败返回 None。"""
    try:
        import dashscope
        from dashscope import MultiModalConversation

        dashscope.api_key = settings.dashscope_api_key
        messages = [
            {
                "role": "user",
                "content": [
                    {"image": f"data:image/jpeg;base64,{image_base64}"},
                    {"text": prompt},
                ],
            }
        ]
        resp = MultiModalConversation.call(model=_MODEL, messages=messages)
        if resp.status_code != 200:
            logger.warning("Qwen-VL 调用失败: %s", resp.message)
            return None
        text = resp.output.choices[0].message.content[0].get("text", "")
        return _parse_json(text)
    except Exception as e:
        logger.exception("Qwen-VL 异常: %s", e)
        return None


def _parse_json(text: str) -> dict | None:
    """从模型输出中提取 JSON 对象。"""
    try:
        return json.loads(text)
    except Exception:
        pass
    # 尝试用正则提取 {...} 块
    m = re.search(r"\{.*\}", text, re.DOTALL)
    if m:
        try:
            return json.loads(m.group(0))
        except Exception:
            pass
    return None


def _mock_features(kind: str) -> dict:
    """无 key 时的 mock 特征，保证链路可跑通。"""
    if kind == "palm":
        return {
            "手型": "方手", "手指": "修长", "感情线": "清晰深长",
            "生命线": "清晰深长", "智慧线": "清晰深长", "掌纹": "普通纹路",
        }
    return {
        "脸型": "圆脸", "眉毛": "浓眉", "眼睛": "大眼",
        "鼻子": "高挺", "嘴唇": "唇形端正",
    }


def _build_report(kind: str, features: dict) -> dict:
    """把特征 dict 转成语料库解读报告。"""
    corpus = c.PALM_FEATURES if kind == "palm" else c.FACE_FEATURES
    closing = c.PALM_CLOSING if kind == "palm" else c.FACE_CLOSING

    items = []
    for dim, options in corpus.items():
        raw = features.get(dim, "")
        # 模糊匹配：模型输出可能与语料 key 不完全一致
        matched = raw
        if raw not in options:
            for k in options:
                if k in raw or raw in k:
                    matched = k
                    break
        desc = options.get(matched)
        if desc is None and matched:
            # 兜底：用第一个选项
            desc = next(iter(options.values()))
        if desc:
            items.append({"dimension": dim, "feature": matched or raw, "interpret": desc})

    return {
        "features": features,
        "items": items,
        "closing": closing,
        "disclaimer": "内容为传统民俗文化趣味解读，仅供娱乐参考。",
    }


def _is_unrecognized(features: dict) -> bool:
    """判断模型是否未能识别目标（如上传了非手掌/非人脸图片）。"""
    if not features:
        return True
    for v in features.values():
        s = str(v)
        for kw in ("无法识别", "无法观察", "无法判断", "不含", "没有手掌", "没有手",
                   "无手掌", "无人脸", "无面部", "无手", "不是手掌", "不是手", "截图",
                   "无法确定", "未检测到", "未能"):
            if kw in s:
                return True
    return False


def _unrecognized_response(kind: str) -> dict:
    target = "手掌" if kind == "palm" else "人脸"
    return {
        "features": {},
        "items": [],
        "recognized": False,
        "message": f"未能识别到清晰的{target}，请上传一张清晰的{target}照片后重试。",
        "disclaimer": "内容为传统民俗文化趣味解读，仅供娱乐参考。",
    }


def analyze_palm(image_base64: str) -> dict:
    """手相分析。"""
    features = None
    if settings.dashscope_api_key:
        features = _call_qwen_vl(image_base64, _PALM_PROMPT)
    if features is None:
        features = _mock_features("palm")
    if _is_unrecognized(features):
        return _unrecognized_response("palm")
    report = _build_report("palm", features)
    report["recognized"] = True
    return report


def analyze_face(image_base64: str) -> dict:
    """面相分析。"""
    features = None
    if settings.dashscope_api_key:
        features = _call_qwen_vl(image_base64, _FACE_PROMPT)
    if features is None:
        features = _mock_features("face")
    if _is_unrecognized(features):
        return _unrecognized_response("face")
    report = _build_report("face", features)
    report["recognized"] = True
    return report
