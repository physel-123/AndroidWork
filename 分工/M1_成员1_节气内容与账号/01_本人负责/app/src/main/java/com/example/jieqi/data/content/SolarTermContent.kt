package com.example.jieqi.data.content

import androidx.compose.ui.graphics.Color
import com.example.jieqi.ui.theme.termColor

/**
 * 节气所属季节。
 */
enum class Season(val label: String) {
    SPRING("春"),
    SUMMER("夏"),
    AUTUMN("秋"),
    WINTER("冬")
}

/**
 * 一个节气条目的完整内容模型。
 *
 * @param index 序号 1..24，对应 [com.example.jieqi.ui.theme.TermColors] 中的主题色
 * @param name 节气名，如“立春”
 * @param pinyin 拼音，用于搜索
 * @param longitude 太阳黄经度数，节气天文计算的依据
 * @param dateRange 公历大致日期区间（文案展示用）
 * @param summary 一句话释义
 * @param meaning 节气由来与含义，2~3 句
 * @param phenology 三候（古人总结的物候现象）
 * @param customs 传统习俗
 * @param foods 时令食俗
 * @param health 养生提示
 * @param poem 相关诗句
 * @param poemFrom 诗句出处
 * @param keywords 关键词，供搜索与推荐使用
 */
data class SolarTerm(
    val index: Int,
    val name: String,
    val pinyin: String,
    val season: Season,
    val longitude: Int,
    val dateRange: String,
    val summary: String,
    val meaning: String,
    val phenology: List<String>,
    val customs: List<String>,
    val foods: List<String>,
    val health: String,
    val poem: String,
    val poemFrom: String,
    val keywords: List<String>
) {
    /** 该节气的主题色，用于卡片与详情页渐变。 */
    val color: Color get() = termColor(index - 1)

    /** “立春·春”这类短标签。 */
    val shortLabel: String get() = "$name · ${season.label}"
}

/**
 * 二十四节气内容库（内置离线数据）。
 *
 * 三候、习俗、食俗取自公开的传统民俗资料整理而成，用于课程项目的文化科普展示。
 */
object SolarTermContent {

    val all: List<SolarTerm> = listOf(
        SolarTerm(
            index = 1,
            name = "立春",
            pinyin = "lichun",
            season = Season.SPRING,
            longitude = 315,
            dateRange = "2月3日—5日",
            summary = "东风解冻，万物复苏，一年的农事自此开始。",
            meaning = "立春是二十四节气之首，“立”为开始之意。此时太阳到达黄经 315°，白昼渐长、气温回升，虽仍有寒意，但地气已动，古人视为新一年的起点。",
            phenology = listOf("一候 东风解冻", "二候 蛰虫始振", "三候 鱼陟负冰"),
            customs = listOf(
                "打春牛：用泥土塑造春牛并击碎，寓意催耕迎春、五谷丰登",
                "咬春：咬一口春饼或萝卜，把“春天”咬住",
                "迎春与贴宜春帖：门上贴“宜春”二字，互赠春幡",
                "戴春鸡、佩春娃：给孩童缝制布鸡布娃，祈愿健康成长"
            ),
            foods = listOf("春饼", "春卷", "萝卜", "五辛盘", "春茶"),
            health = "春属木，对应肝脏。宜早睡早起、舒展筋骨；饮食少酸增甘，可食大枣、山药、春笋，忌过早减衣。",
            poem = "律回岁晚冰霜少，春到人间草木知。",
            poemFrom = "宋·张栻《立春偶成》",
            keywords = listOf("迎春", "开年", "春耕", "咬春")
        ),
        SolarTerm(
            index = 2,
            name = "雨水",
            pinyin = "yushui",
            season = Season.SPRING,
            longitude = 330,
            dateRange = "2月18日—20日",
            summary = "冰雪消融，降水渐增，草木开始萌动。",
            meaning = "雨水节气太阳到达黄经 330°，气温回升、降水增多，雪渐少而雨渐多，是越冬作物返青的关键时期。",
            phenology = listOf("一候 獭祭鱼", "二候 候雁北", "三候 草木萌动"),
            customs = listOf(
                "回娘家送罐罐肉：出嫁女儿带礼探望父母，感恩养育",
                "拉保保（认干亲）：为孩子认一位干爹，祈求平安顺遂",
                "占稻色：爆炒糯谷米花，以成色占卜当年收成",
                "接寿：女婿为岳父母送节，祝愿长寿"
            ),
            foods = listOf("罐罐肉", "春笋", "韭菜", "蜂蜜", "山药粥"),
            health = "雨水时节湿气渐重，宜健脾祛湿。可食薏米、山药、莲子；注意“春捂”，尤其护住腰腹与足踝。",
            poem = "天街小雨润如酥，草色遥看近却无。",
            poemFrom = "唐·韩愈《早春呈水部张十八员外》",
            keywords = listOf("降雨", "春捂", "回娘家", "农耕")
        ),
        SolarTerm(
            index = 3,
            name = "惊蛰",
            pinyin = "jingzhe",
            season = Season.SPRING,
            longitude = 345,
            dateRange = "3月5日—7日",
            summary = "春雷乍动，蛰虫惊醒，田间农事忙起来。",
            meaning = "惊蛰时太阳到达黄经 345°，春雷始鸣，蛰伏于土中的昆虫被惊醒，故称“惊蛰”。此时我国大部分地区进入春耕季节。",
            phenology = listOf("一候 桃始华", "二候 仓庚鸣", "三候 鹰化为鸠"),
            customs = listOf(
                "祭白虎、打小人：岭南一带以纸绘白虎、拍打纸人化解是非",
                "吃梨：寓意与害虫“分离”，也润燥清火",
                "蒙鼓皮：借雷声鼓声相应，祈愿风调雨顺",
                "炒虫：把豆子玉米炒得噼啪响，象征炒死害虫"
            ),
            foods = listOf("梨", "炒豆", "芋头", "鸡蛋", "春韭"),
            health = "惊蛰肝气旺盛，易口干舌燥。宜食梨、百合、银耳润肺；早睡以养肝血，避免情绪急躁。",
            poem = "微雨众卉新，一雷惊蛰始。",
            poemFrom = "唐·韦应物《观田家》",
            keywords = listOf("春雷", "吃梨", "春耕", "驱虫")
        ),
        SolarTerm(
            index = 4,
            name = "春分",
            pinyin = "chunfen",
            season = Season.SPRING,
            longitude = 0,
            dateRange = "3月20日—22日",
            summary = "昼夜均而寒暑平，春色正中分。",
            meaning = "春分这天太阳直射赤道，全球昼夜几乎等长，故称“分”。春分之后北半球昼渐长、夜渐短，是踏青赏春的好时节。",
            phenology = listOf("一候 玄鸟至", "二候 雷乃发声", "三候 始电"),
            customs = listOf(
                "竖蛋：选一枚光滑鸡蛋轻轻立起，寓意平衡与好运",
                "吃春菜：采野苋菜与鱼片同煮“春汤”，祈求安康",
                "送春牛图：挨家送印有节令与农事的春牛图，说吉利话",
                "粘雀子嘴：做汤圆插在竹签上置于田边，盼鸟雀不啄庄稼"
            ),
            foods = listOf("春菜", "春汤", "驴打滚", "太阳糕", "香椿"),
            health = "春分阴阳平衡，饮食讲究寒热搭配。宜食时令野菜，作息规律；过敏体质者注意防花粉。",
            poem = "仲春初四日，春色正中分。",
            poemFrom = "唐·徐铉《春分日》",
            keywords = listOf("昼夜平分", "竖蛋", "踏青", "春菜")
        ),
        SolarTerm(
            index = 5,
            name = "清明",
            pinyin = "qingming",
            season = Season.SPRING,
            longitude = 15,
            dateRange = "4月4日—6日",
            summary = "气清景明，慎终追远，也是踏青插柳的日子。",
            meaning = "清明太阳到达黄经 15°，天气清朗、草木繁茂。它既是节气，也是唯一兼具节日身份的节气，承载着祭祖与迎春双重意涵。",
            phenology = listOf("一候 桐始华", "二候 田鼠化为鴽", "三候 虹始见"),
            customs = listOf(
                "扫墓祭祖：修整坟茔、献花默哀，表达对先人的追思",
                "踏青郊游：趁春光正好出游赏花，古称“探春”",
                "插柳戴柳：门前插柳、头上戴柳，寓意驱邪留春",
                "放风筝与荡秋千：放飞纸鸢并剪断丝线，寓意放走晦气"
            ),
            foods = listOf("青团", "馓子", "艾饺", "清明螺", "寒食粥"),
            health = "清明宜疏肝理气，可饮菊花茶、食荠菜；乍暖还寒，注意保暖，避免过度悲伤伤肝。",
            poem = "清明时节雨纷纷，路上行人欲断魂。",
            poemFrom = "唐·杜牧《清明》",
            keywords = listOf("祭祖", "踏青", "青团", "插柳")
        ),
        SolarTerm(
            index = 6,
            name = "谷雨",
            pinyin = "guyu",
            season = Season.SPRING,
            longitude = 30,
            dateRange = "4月19日—21日",
            summary = "雨生百谷，春季最后一个节气。",
            meaning = "谷雨取“雨生百谷”之意，太阳到达黄经 30°。此时降水明显增加，正是播种移苗、埯瓜点豆的最佳时节。",
            phenology = listOf("一候 萍始生", "二候 鸣鸠拂其羽", "三候 戴胜降于桑"),
            customs = listOf(
                "赏牡丹：谷雨前后牡丹盛开，故牡丹又称“谷雨花”",
                "祭海：北方沿海渔民出海前祭海祈福，祈求平安丰收",
                "走谷雨：青年妇女走村串亲或野外走一圈，寓意强身",
                "禁蝎贴符：陕西一带贴“谷雨贴”驱除毒虫"
            ),
            foods = listOf("谷雨茶", "香椿", "槐花", "荠菜", "乌米饭"),
            health = "谷雨湿气渐盛，宜健脾利湿。可食薏米、赤小豆、冬瓜；适度运动出汗，有助祛湿。",
            poem = "谷雨如丝复似尘，煮瓶浮蜡正尝新。",
            poemFrom = "宋·范成大《晚春田园杂兴》",
            keywords = listOf("雨生百谷", "牡丹", "谷雨茶", "祭海")
        ),
        SolarTerm(
            index = 7,
            name = "立夏",
            pinyin = "lixia",
            season = Season.SUMMER,
            longitude = 45,
            dateRange = "5月5日—7日",
            summary = "万物至此皆长大，夏日自此启幕。",
            meaning = "立夏是夏季的第一个节气，太阳到达黄经 45°。此时气温显著升高，作物进入旺盛生长期，古人以“立夏”标志夏天的开始。",
            phenology = listOf("一候 蝼蝈鸣", "二候 蚯蚓出", "三候 王瓜生"),
            customs = listOf(
                "斗蛋：孩童用煮蛋互相碰击，蛋壳不破者为“蛋王”",
                "称人：立夏日悬秤称体重，寓意不减福气、平安度夏",
                "尝新：品尝樱桃、青梅、麦子等初夏新物，称“立夏尝三新”",
                "挂蛋与编蛋套：用彩线编织网袋装蛋挂在孩子胸前"
            ),
            foods = listOf("立夏蛋", "五色饭", "乌米饭", "蚕豆", "樱桃"),
            health = "夏属火，对应心脏。宜养心安神，午间小憩；饮食清淡，可食绿豆、莲子、苦瓜，忌贪凉伤脾。",
            poem = "四月维夏，六月徂暑。",
            poemFrom = "《诗经·小雅·四月》",
            keywords = listOf("斗蛋", "称人", "尝新", "养心")
        ),
        SolarTerm(
            index = 8,
            name = "小满",
            pinyin = "xiaoman",
            season = Season.SUMMER,
            longitude = 60,
            dateRange = "5月20日—22日",
            summary = "麦粒渐满而未熟，江河渐盈而未溢。",
            meaning = "小满时太阳到达黄经 60°，夏熟作物籽粒开始灌浆饱满但尚未成熟，故称“小满”。南方则进入雨季，江河湖水渐盈。",
            phenology = listOf("一候 苦菜秀", "二候 靡草死", "三候 麦秋至"),
            customs = listOf(
                "祭车神：在水车基上放置鱼肉香烛，祈求灌溉顺利",
                "动三车：水车、油车、丝车同时开动，标志农忙开始",
                "食苦菜：采苦菜佐食，清热解暑、忆苦思甜",
                "抢水：全村协力踏水车引水入田，场面热烈"
            ),
            foods = listOf("苦菜", "麦仁", "蚕豆", "枇杷", "绿豆汤"),
            health = "小满湿热渐盛，宜清热祛湿。可食苦瓜、冬瓜、薏米；注意皮肤护理，预防湿疹与脚气。",
            poem = "夜莺啼绿柳，皓月醒长空。",
            poemFrom = "宋·欧阳修《小满》",
            keywords = listOf("麦浪", "苦菜", "抢水", "湿热")
        ),
        SolarTerm(
            index = 9,
            name = "芒种",
            pinyin = "mangzhong",
            season = Season.SUMMER,
            longitude = 75,
            dateRange = "6月5日—7日",
            summary = "有芒之麦快收，有芒之稻可种，一年中最忙。",
            meaning = "芒种太阳到达黄经 75°，“芒”指有芒的作物，“种”指播种。此时麦类抢收、稻类抢种，农事最为繁忙，故又称“忙种”。",
            phenology = listOf("一候 螳螂生", "二候 鵙始鸣", "三候 反舌无声"),
            customs = listOf(
                "送花神：芒种时百花凋残，民间举行祭祀为花神饯行",
                "安苗：皖南一带以新麦面捏制五谷六畜形状蒸制，祈求丰收",
                "打泥巴仗：贵州侗族边插秧边互掷泥巴，嬉闹中完成农活",
                "煮青梅：青梅酸涩需煮制，故有“芒种煮梅”之俗"
            ),
            foods = listOf("青梅", "君踏菜", "绿豆糕", "西瓜", "薏米水"),
            health = "芒种闷热潮湿，宜午睡补眠、避免暴晒。饮食清淡并多补水，可饮酸梅汤生津敛汗。",
            poem = "时雨及芒种，四野皆插秧。",
            poemFrom = "宋·陆游《时雨》",
            keywords = listOf("抢收抢种", "青梅", "送花神", "安苗")
        ),
        SolarTerm(
            index = 10,
            name = "夏至",
            pinyin = "xiazhi",
            season = Season.SUMMER,
            longitude = 90,
            dateRange = "6月21日—22日",
            summary = "北半球白昼最长，阳气至极而阴气始生。",
            meaning = "夏至这天太阳直射北回归线，北半球白昼最长、正午影子最短。古人认为“夏至一阴生”，是阴阳转换的重要节点。",
            phenology = listOf("一候 鹿角解", "二候 蜩始鸣", "三候 半夏生"),
            customs = listOf(
                "祭地祈丰：古时夏至日举行祭地仪式，祈求年谷顺成",
                "吃面：北方“冬至饺子夏至面”，新麦做面尝新",
                "消夏避伏：互赠折扇、脂粉，用以扇风祛湿",
                "称人与秤水：观测体重与水位变化，记录时令"
            ),
            foods = listOf("夏至面", "凉面", "豌豆糕", "麦粽", "酸梅汤"),
            health = "夏至阳气最盛，宜晚睡早起并午休。饮食宜清淡，可食苦瓜、莲子心；忌大量冷饮伤及脾胃。",
            poem = "绿筠尚含粉，圆荷始散芳。",
            poemFrom = "唐·韦应物《夏至避暑北池》",
            keywords = listOf("最长白昼", "夏至面", "祭地", "消夏")
        ),
        SolarTerm(
            index = 11,
            name = "小暑",
            pinyin = "xiaoshu",
            season = Season.SUMMER,
            longitude = 105,
            dateRange = "7月6日—8日",
            summary = "暑气渐盛但未至最热，出梅入伏。",
            meaning = "小暑太阳到达黄经 105°，“暑”为炎热，小暑即天气开始炎热但尚未到极致。此时江淮流域梅雨结束，进入伏天。",
            phenology = listOf("一候 温风至", "二候 蟋蟀居宇", "三候 鹰始鸷"),
            customs = listOf(
                "食新：以新割稻谷碾米做饭供祖，然后尝新",
                "晒书画衣裳：小暑前后阳光最烈，民间翻晒衣物书籍防霉",
                "吃藕与食黄鳝：民间有“小暑黄鳝赛人参”之说",
                "封斋与吃饺子：伏日食欲不振，饺子开胃解馋"
            ),
            foods = listOf("莲藕", "黄鳝", "饺子", "绿豆芽", "莲子羹"),
            health = "小暑宜少动多静、心平气和。可食藕、绿豆、百合清心；空调温度不宜过低，谨防“阴暑”。",
            poem = "倏忽温风至，因循小暑来。",
            poemFrom = "唐·元稹《小暑六月节》",
            keywords = listOf("出梅", "入伏", "食新", "晒伏")
        ),
        SolarTerm(
            index = 12,
            name = "大暑",
            pinyin = "dashu",
            season = Season.SUMMER,
            longitude = 120,
            dateRange = "7月22日—24日",
            summary = "一年中最热的时节，湿热交蒸。",
            meaning = "大暑太阳到达黄经 120°，正值“中伏”前后，是一年中气温最高、雷雨最多的时期，农作物生长最快。",
            phenology = listOf("一候 腐草为萤", "二候 土润溽暑", "三候 大雨时行"),
            customs = listOf(
                "饮伏茶：在村口凉亭免费供应金银花茶，供路人解暑",
                "晒伏姜：将生姜与红糖晒制，用于祛寒暖胃",
                "送大暑船：浙江沿海送“大暑船”入海，祈求平安",
                "斗蟋蟀：田间捕捉蟋蟀相斗，是旧时消夏乐事"
            ),
            foods = listOf("仙草冻", "伏姜", "绿豆汤", "荔枝", "冬瓜汤"),
            health = "大暑谨防中暑，宜多饮温水、少在正午外出。可食冬瓜、薏米、荷叶粥；出汗多时注意补充电解质。",
            poem = "桂轮开子夜，萤火照空时。",
            poemFrom = "唐·元稹《大暑六月中》",
            keywords = listOf("伏天", "伏茶", "萤火", "防暑")
        ),
        SolarTerm(
            index = 13,
            name = "立秋",
            pinyin = "liqiu",
            season = Season.AUTUMN,
            longitude = 135,
            dateRange = "8月7日—9日",
            summary = "暑气未消而秋意已萌，禾谷开始成熟。",
            meaning = "立秋是秋季第一个节气，太阳到达黄经 135°。虽然仍处暑热之中，但早晚凉意渐生，草木开始结果孕籽。",
            phenology = listOf("一候 凉风至", "二候 白露降", "三候 寒蝉鸣"),
            customs = listOf(
                "贴秋膘：立秋称体重，若夏日消瘦则以炖肉补回",
                "啃秋咬秋：抱着西瓜或香瓜啃食，寓意咬住秋天",
                "晒秋：山区村落把辣椒、玉米晾晒在屋顶，五彩斑斓",
                "秋忙会与放秋令：集市交易农具，为秋收做准备"
            ),
            foods = listOf("炖肉", "西瓜", "秋桃", "赤小豆", "南瓜"),
            health = "立秋燥气渐起，宜润肺养阴。可食银耳、百合、梨；“秋不食姜”之说提醒少吃辛辣。",
            poem = "一叶梧桐一报秋，稻花田里话丰收。",
            poemFrom = "宋·刘翰《立秋》",
            keywords = listOf("贴秋膘", "啃秋", "晒秋", "秋燥")
        ),
        SolarTerm(
            index = 14,
            name = "处暑",
            pinyin = "chushu",
            season = Season.AUTUMN,
            longitude = 150,
            dateRange = "8月22日—24日",
            summary = "暑气至此而止，秋凉正式开始。",
            meaning = "处暑的“处”是终止之意，太阳到达黄经 150°。此时副热带高压南退，炎热天气结束，民间称“暑气至此而止矣”。",
            phenology = listOf("一候 鹰乃祭鸟", "二候 天地始肃", "三候 禾乃登"),
            customs = listOf(
                "放河灯：在中元节前后放灯于水面，寄托对逝者的思念",
                "开渔节：东海休渔结束，渔民举行开渔仪式出海",
                "煎药茶与吃鸭子：民间以老鸭炖汤润燥，称“处暑送鸭”",
                "祭祖迎秋：用新收作物祭祖，报告秋成"
            ),
            foods = listOf("百合鸭", "老鸭汤", "龙眼", "银耳", "莲藕"),
            health = "处暑昼夜温差加大，宜早睡早起。饮食润燥，可食蜂蜜、芝麻、梨；注意腹部保暖防秋泻。",
            poem = "疾风驱急雨，残暑扫除空。",
            poemFrom = "宋·苏泂《长江二首》",
            keywords = listOf("暑气止", "开渔", "放河灯", "秋燥")
        ),
        SolarTerm(
            index = 15,
            name = "白露",
            pinyin = "bailu",
            season = Season.AUTUMN,
            longitude = 165,
            dateRange = "9月7日—9日",
            summary = "夜寒生露，秋意渐浓。",
            meaning = "白露太阳到达黄经 165°，夜间水汽在草木上凝结成白色露珠，故名。此时天气转凉，是全年昼夜温差较大的节气。",
            phenology = listOf("一候 鸿雁来", "二候 玄鸟归", "三候 群鸟养羞"),
            customs = listOf(
                "收清露：清晨采集草叶露水，古人认为可入药润燥",
                "饮白露茶：此时茶树经夏成长，茶味甘醇清香",
                "吃龙眼与酿米酒：福建有“白露必吃龙眼”之说",
                "祭禹王：太湖渔民于白露祭祀大禹，祈求湖上平安"
            ),
            foods = listOf("白露茶", "龙眼", "米酒", "红薯", "秋梨"),
            health = "“白露不露身”，宜及时添衣，护好肩颈与肚脐。饮食滋阴润肺，可食山药、百合、杏仁。",
            poem = "露从今夜白，月是故乡明。",
            poemFrom = "唐·杜甫《月夜忆舍弟》",
            keywords = listOf("露水", "白露茶", "添衣", "鸿雁")
        ),
        SolarTerm(
            index = 16,
            name = "秋分",
            pinyin = "qiufen",
            season = Season.AUTUMN,
            longitude = 180,
            dateRange = "9月22日—24日",
            summary = "昼夜再度均分，秋色平分，也是丰收节。",
            meaning = "秋分太阳直射赤道，昼夜等长，此后北半球昼短夜长。自 2018 年起，秋分被设立为“中国农民丰收节”。",
            phenology = listOf("一候 雷始收声", "二候 蛰虫坯户", "三候 水始涸"),
            customs = listOf(
                "祭月与拜月：古时秋分祭月，后演化为中秋节俗",
                "竖蛋与吃秋菜：岭南采野苋菜与鱼片煮“秋汤”",
                "送秋牛图：挨家送印有节气农事的秋牛图，说吉祥话",
                "粘雀子嘴：做汤圆插在竹签上置于田边护庄稼"
            ),
            foods = listOf("秋菜", "桂花糕", "螃蟹", "芋头", "柿子"),
            health = "秋分宜平补，饮食注重阴阳平衡。可食芝麻、核桃、糯米；早晚添衣，适度“秋冻”但忌过度。",
            poem = "金气秋分，风清露冷秋期半。",
            poemFrom = "宋·谢逸《点绛唇》",
            keywords = listOf("丰收节", "祭月", "秋菜", "昼夜平分")
        ),
        SolarTerm(
            index = 17,
            name = "寒露",
            pinyin = "hanlu",
            season = Season.AUTUMN,
            longitude = 195,
            dateRange = "10月8日—9日",
            summary = "露气寒冷将凝，菊黄蟹肥登高时。",
            meaning = "寒露太阳到达黄经 195°，露水已寒、快要凝结成霜，是气候由凉转寒的过渡节气，也是登高赏菊的好时节。",
            phenology = listOf("一候 鸿雁来宾", "二候 雀入大水为蛤", "三候 菊有黄华"),
            customs = listOf(
                "登高望远：重阳前后登高赏秋，寓意步步高升",
                "赏菊与饮菊花酒：菊为“延寿客”，寒露時节最盛",
                "吃芝麻与秋钓：民间“寒露吃芝麻”，并有秋钓边之说",
                "斗蛐蛐与观红叶：秋虫鸣唱、枫叶渐红，皆为雅事"
            ),
            foods = listOf("菊花酒", "芝麻", "螃蟹", "柿子", "雪梨"),
            health = "“寒露脚不露”，注意足部保暖。饮食滋阴润燥，可食芝麻、银耳、蜂蜜，少食辛辣。",
            poem = "袅袅凉风动，凄凄寒露零。",
            poemFrom = "唐·白居易《池上》",
            keywords = listOf("登高", "菊花", "秋钓", "转寒")
        ),
        SolarTerm(
            index = 18,
            name = "霜降",
            pinyin = "shuangjiang",
            season = Season.AUTUMN,
            longitude = 210,
            dateRange = "10月23日—24日",
            summary = "秋末最后一个节气，初霜见而柿子红。",
            meaning = "霜降太阳到达黄经 210°，是秋季最后一个节气。黄河流域开始出现初霜，昼夜温差最大，农谚有“霜降见霜，米谷满仓”。",
            phenology = listOf("一候 豺乃祭兽", "二候 草木黄落", "三候 蛰虫咸俯"),
            customs = listOf(
                "赏菊与菊花会：霜降时节菊花开得最盛，开菊会、饮菊酒",
                "吃柿子：民间认为霜降吃柿子可御寒保暖、不流鼻涕",
                "登高远眺与祭旗纛：古时军队于此日祭旗，祈求武运",
                "进补“煲羊肉”：闽台一带霜降“补冬不如补霜降”"
            ),
            foods = listOf("柿子", "羊肉煲", "萝卜", "栗子", "鸭肉"),
            health = "霜降宜平补，重点保暖膝踝与胃部。可食栗子、山药、白果；慢性呼吸道疾病患者注意防寒。",
            poem = "霜叶红于二月花。",
            poemFrom = "唐·杜牧《山行》",
            keywords = listOf("初霜", "柿子", "赏菊", "进补")
        ),
        SolarTerm(
            index = 19,
            name = "立冬",
            pinyin = "lidong",
            season = Season.WINTER,
            longitude = 225,
            dateRange = "11月7日—8日",
            summary = "水始冰、地始冻，万物收藏。",
            meaning = "立冬是冬季第一个节气，太阳到达黄经 225°。“冬”有终了与收藏之意，农事告一段落，人们开始“补冬”御寒。",
            phenology = listOf("一候 水始冰", "二候 地始冻", "三候 雉入大水为蜃"),
            customs = listOf(
                "贺冬与拜冬：更换新衣、往来庆贺，如同拜年",
                "补冬：炖羊肉、鸡汤等滋补，为寒冬储备能量",
                "北方吃饺子：取“交子之时”的谐音，寓意季节交替",
                "祭祖饮宴：以时令食物祭祖，犒劳一年辛劳"
            ),
            foods = listOf("饺子", "羊肉汤", "姜母鸭", "山药", "板栗"),
            health = "冬季养藏，宜早睡晚起、待日光而作。饮食温补，可食羊肉、黑豆、核桃；注意背部与足部保暖。",
            poem = "冻笔新诗懒写，寒炉美酒时温。",
            poemFrom = "唐·李白《立冬》",
            keywords = listOf("补冬", "饺子", "收藏", "祭祖")
        ),
        SolarTerm(
            index = 20,
            name = "小雪",
            pinyin = "xiaoxue",
            season = Season.WINTER,
            longitude = 240,
            dateRange = "11月22日—23日",
            summary = "初雪将至，家家腌腊味。",
            meaning = "小雪太阳到达黄经 240°，气温下降、开始降雪但雪量不大，故称“小雪”。此时是腌制腊味、储备冬菜的最佳时机。",
            phenology = listOf("一候 虹藏不见", "二候 天气上升地气下降", "三候 闭塞而成冬"),
            customs = listOf(
                "腌腊肉与灌香肠：民间“小雪腌菜，大雪腌肉”",
                "吃糍粑：南方部分地区的节气食俗，寓意团圆丰收",
                "晒鱼干：沿海渔民趁晴好天气晾晒鱼干储备过冬",
                "杀年猪与吃刨汤：土家族小雪前后杀年猪、宴请乡邻"
            ),
            foods = listOf("腊肉", "糍粑", "香肠", "鱼干", "萝卜干"),
            health = "小雪宜温润，可食黑木耳、山药、白菜。注意室内通风与湿度，预防呼吸道疾病与情绪低落。",
            poem = "花雪随风不厌看，更多还肯失林峦。",
            poemFrom = "唐·徐铉《和萧郎中小雪日作》",
            keywords = listOf("腌腊", "糍粑", "初雪", "御寒")
        ),
        SolarTerm(
            index = 21,
            name = "大雪",
            pinyin = "daxue",
            season = Season.WINTER,
            longitude = 255,
            dateRange = "12月6日—8日",
            summary = "雪盛天寒，观雪腌肉进补时。",
            meaning = "大雪太阳到达黄经 255°，降雪可能性与降雪量都比小雪更大。此时天气更为寒冷，民间开始为迎接新年做准备。",
            phenology = listOf("一候 鹖鴠不鸣", "二候 虎始交", "三候 荔挺出"),
            customs = listOf(
                "观赏封河与堆雪人：北方“小雪封地，大雪封河”",
                "腌肉与制腊味：家家门口挂起腌好的鱼肉，年味渐浓",
                "进补与吃羊肉：北方食羊、南方食鸭，温补御寒",
                "藏冰与窖藏：北方凿冰入窖，为来年夏日所用"
            ),
            foods = listOf("羊肉火锅", "红薯粥", "腊味", "雪菜", "枸杞茶"),
            health = "大雪重“藏”，宜早睡晚起并减少户外长时间停留。饮食温热，可食羊肉、桂圆、红枣；注意心脑血管防护。",
            poem = "千山鸟飞绝，万径人踪灭。",
            poemFrom = "唐·柳宗元《江雪》",
            keywords = listOf("封河", "腌肉", "进补", "年味")
        ),
        SolarTerm(
            index = 22,
            name = "冬至",
            pinyin = "dongzhi",
            season = Season.WINTER,
            longitude = 270,
            dateRange = "12月21日—23日",
            summary = "白昼最短，阴极阳生，冬至大如年。",
            meaning = "冬至太阳直射南回归线，北半球白昼最短、黑夜最长。古人认为“冬至一阳生”，将其视为吉日，有“冬至大如年”之说。",
            phenology = listOf("一候 蚯蚓结", "二候 麋角解", "三候 水泉动"),
            customs = listOf(
                "北吃饺子南吃汤圆：饺子形似耳朵，汤圆寓意团圆",
                "数九与画九九消寒图：从冬至起数九九八十一天盼春",
                "祭天祭祖：古代帝王于冬至日举行祭天大典",
                "赠鞋袜与拜冬：晚辈向长辈献鞋袜，寓意添寿保暖"
            ),
            foods = listOf("饺子", "汤圆", "羊肉汤", "赤豆粥", "年糕"),
            health = "冬至是进补最佳时机，可食羊肉、当归生姜汤。宜早卧晚起，避免大汗淋漓，保护阳气。",
            poem = "邯郸驿里逢冬至，抱膝灯前影伴身。",
            poemFrom = "唐·白居易《邯郸冬至夜思家》",
            keywords = listOf("数九", "汤圆", "饺子", "一阳生")
        ),
        SolarTerm(
            index = 23,
            name = "小寒",
            pinyin = "xiaohan",
            season = Season.WINTER,
            longitude = 285,
            dateRange = "1月5日—7日",
            summary = "进入三九严寒，腊八粥香年味起。",
            meaning = "小寒太阳到达黄经 285°，标志着开始进入一年中最寒冷的日子，常与“三九”重合，民间有“小寒胜大寒”的说法。",
            phenology = listOf("一候 雁北乡", "二候 鹊始巢", "三候 雉始雊"),
            customs = listOf(
                "喝腊八粥：以八种以上谷物豆果熬粥，祭祖并馈赠亲友",
                "吃菜饭与糯米饭：南京、广东等地的节气食俗",
                "探梅与赏梅：小寒初绽的梅花为二十四番花信之首",
                "准备年货与写春联：小寒之后年味渐浓，开始忙年"
            ),
            foods = listOf("腊八粥", "腊八蒜", "菜饭", "糯米饭", "羊肉"),
            health = "小寒重在保暖，尤其护头颈与腰背。可食温热的羊肉、桂圆、核桃；可用艾叶泡脚促进循环。",
            poem = "霜鹰近北首，雊雉隐丛茅。",
            poemFrom = "唐·元稹《小寒》",
            keywords = listOf("三九", "腊八粥", "探梅", "忙年")
        ),
        SolarTerm(
            index = 24,
            name = "大寒",
            pinyin = "dahan",
            season = Season.WINTER,
            longitude = 300,
            dateRange = "1月20日—21日",
            summary = "寒气之逆极，也是二十四节气最后一站。",
            meaning = "大寒太阳到达黄经 300°，是二十四节气的最后一个。寒气达到极点，之后便立春，完成一个循环，故有“大寒迎年”之说。",
            phenology = listOf("一候 鸡始乳", "二候 征鸟厉疾", "三候 水泽腹坚"),
            customs = listOf(
                "除尘与糊窗：扫房、裱糊窗户，辞旧迎新",
                "蒸供与备年货：蒸年馍、赶年集，为春节做准备",
                "喝腊酒与吃糯米饭：岭南有“大寒吃糯米饭”之俗",
                "尾牙祭与祭灶：商家设宴谢伙计，民间祭灶神"
            ),
            foods = listOf("糯米饭", "腊酒", "年糕", "八宝饭", "鸡汤"),
            health = "大寒宜温补脾肾，可食山药、黑芝麻、鸡肉。注意防寒防风，适度运动以助阳气升发。",
            poem = "旧雪未及消，新雪又拥户。",
            poemFrom = "宋·邵雍《大寒吟》",
            keywords = listOf("迎年", "除尘", "糯米饭", "尾牙")
        )
    )

    /** 按序号（1..24）索引。 */
    private val byIndex: Map<Int, SolarTerm> = all.associateBy { it.index }

    /** 按名称索引，如 “立春”。 */
    private val byName: Map<String, SolarTerm> = all.associateBy { it.name }

    fun byIndex(index: Int): SolarTerm? = byIndex[index]

    fun byName(name: String): SolarTerm? = byName[name]

    /** 关键词 / 名称 / 拼音模糊搜索。 */
    fun search(keyword: String): List<SolarTerm> {
        val q = keyword.trim()
        if (q.isEmpty()) return all
        return all.filter { term ->
            term.name.contains(q, ignoreCase = true) ||
                term.pinyin.contains(q, ignoreCase = true) ||
                term.summary.contains(q) ||
                term.keywords.any { it.contains(q) }
        }
    }

    /** 各季节节气数量统计（用于“我的”页面的文化小知识）。 */
    fun countBySeason(): Map<Season, Int> = all.groupingBy { it.season }.eachCount()
}
