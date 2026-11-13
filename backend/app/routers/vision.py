"""手相 / 面相识别路由。"""
import base64

from fastapi import APIRouter, File, UploadFile, HTTPException

from app.services import vision_service

router = APIRouter(prefix="/api", tags=["vision"])

MAX_SIZE = 5 * 1024 * 1024  # 5MB 上限


async def _read_image_base64(file: UploadFile) -> str:
    data = await file.read()
    if len(data) > MAX_SIZE:
        raise HTTPException(status_code=413, detail="图片过大，请压缩后重试（≤5MB）")
    if not data:
        raise HTTPException(status_code=400, detail="图片为空")
    return base64.b64encode(data).decode("utf-8")


@router.post("/palm/analyze")
async def analyze_palm(file: UploadFile = File(...)):
    """手相分析：上传手掌照片，返回特征解读。"""
    img = await _read_image_base64(file)
    return vision_service.analyze_palm(img)


@router.post("/face/analyze")
async def analyze_face(file: UploadFile = File(...)):
    """面相分析：上传人脸照片，返回特征解读。"""
    img = await _read_image_base64(file)
    return vision_service.analyze_face(img)
