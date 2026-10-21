package com.example.jieqi.data.content

import com.example.jieqi.data.db.FolkCustomEntity

/**
 * 各地民俗内置数据。
 *
 * 说明：经纬度为城市中心近似坐标，用于“地域民俗地图”打点展示，
 * 数据来源为公开的传统民俗资料整理，供课程项目文化科普使用。
 */
object FolkCustomContent {

    val all: List<FolkCustomEntity> = listOf(
        FolkCustomEntity(
            title = "打春牛",
            termName = "立春",
            province = "浙江",
            city = "衢州",
            latitude = 28.9359,
            longitude = 118.8744,
            description = "立春之日以泥土塑春牛，由官员执彩鞭击碎，谓之“打春”，寓意催耕迎春、五谷丰登。九华立春祭已被列入国家级非物质文化遗产名录。"
        ),
        FolkCustomEntity(
            title = "立春祭（九华立春祭）",
            termName = "立春",
            province = "浙江",
            city = "衢州柯城",
            latitude = 29.0167,
            longitude = 118.8667,
            description = "柯城区九华乡一带保存完整的立春祭祀仪式，含祭春神、鞭春牛、抬春牛巡游等环节，2016 年作为“二十四节气”相关项目入选联合国教科文组织人类非遗名录。"
        ),
        FolkCustomEntity(
            title = "咬春吃春饼",
            termName = "立春",
            province = "北京",
            city = "东城",
            latitude = 39.9289,
            longitude = 116.4163,
            description = "立春这天北京人家家烙春饼，卷上豆芽、韭黄、酱肉，谓之“咬春”，取咬住春天、迎新纳福之意。"
        ),
        FolkCustomEntity(
            title = "送春牛图",
            termName = "春分",
            province = "湖南",
            city = "长沙",
            latitude = 28.2282,
            longitude = 112.9388,
            description = "春分前后，说唱艺人挨家挨户送印有节气与农事的“春牛图”，见啥说啥、即景生情，称为“说春”，是流传已久的民间曲艺形式。"
        ),
        FolkCustomEntity(
            title = "竖蛋",
            termName = "春分",
            province = "全国",
            city = "多地流行",
            latitude = 34.3416,
            longitude = 108.9398,
            description = "春分竖蛋是流传甚广的节令游戏：选一枚生下四五天的光滑鸡蛋，小心立在桌面上，成功者一年好运。"
        ),
        FolkCustomEntity(
            title = "清明做青团",
            termName = "清明",
            province = "江苏",
            city = "苏州",
            latitude = 31.2989,
            longitude = 120.5853,
            description = "以浆麦草或艾草汁和入糯米粉，包豆沙馅蒸制而成，色泽碧绿、清香扑鼻，是江南清明祭祖与踏青必备的时令点心。"
        ),
        FolkCustomEntity(
            title = "清明插柳戴柳",
            termName = "清明",
            province = "安徽",
            city = "黄山",
            latitude = 29.7147,
            longitude = 118.3376,
            description = "徽州一带清明日门前插柳、孩童头戴柳圈，民谚“清明不戴柳，红颜成皓首”，寓意驱邪避疫、留住春光。"
        ),
        FolkCustomEntity(
            title = "谷雨祭海",
            termName = "谷雨",
            province = "山东",
            city = "荣成",
            latitude = 37.1652,
            longitude = 122.4863,
            description = "谷雨时节百鱼上岸，渔民备祭品到海神娘娘庙祭海祈福，举行隆重仪式后开始一年的海上生产，是全国知名的渔家民俗。"
        ),
        FolkCustomEntity(
            title = "谷雨赏牡丹",
            termName = "谷雨",
            province = "河南",
            city = "洛阳",
            latitude = 34.6197,
            longitude = 112.4540,
            description = "“谷雨三朝看牡丹”，洛阳牡丹在谷雨前后盛放，牡丹因此被称为“谷雨花”，花会期间满城赏花。"
        ),
        FolkCustomEntity(
            title = "立夏斗蛋",
            termName = "立夏",
            province = "江苏",
            city = "南京",
            latitude = 32.0603,
            longitude = 118.7969,
            description = "立夏这天煮鸡蛋装入彩线编织的蛋套挂在孩子胸前，孩童以蛋相碰，蛋壳不破者为“蛋王”，是江南广为流行的童趣民俗。"
        ),
        FolkCustomEntity(
            title = "立夏称人",
            termName = "立夏",
            province = "浙江",
            city = "杭州",
            latitude = 30.2741,
            longitude = 120.1551,
            description = "立夏日在大树下悬起大秤，男女老少轮流称重并记数，寓意“不减福气”，祈愿平安度过炎夏。"
        ),
        FolkCustomEntity(
            title = "小满祭车神",
            termName = "小满",
            province = "江苏",
            city = "无锡",
            latitude = 31.4912,
            longitude = 120.3119,
            description = "旧时水车是灌溉命脉，小满日在水车基上置鱼肉香烛祭“车神”，并用白水一杯泼入田中，寓意水源涌旺。"
        ),
        FolkCustomEntity(
            title = "芒种安苗",
            termName = "芒种",
            province = "安徽",
            city = "宣城",
            latitude = 30.9407,
            longitude = 118.7588,
            description = "皖南各地芒种后以新麦面捏制五谷六畜、瓜果蔬菜形状的面食蒸制，并以蔬菜汁染色，作为祭祀供品祈求丰收，称“安苗”。"
        ),
        FolkCustomEntity(
            title = "夏至吃面",
            termName = "夏至",
            province = "山东",
            city = "济南",
            latitude = 36.6512,
            longitude = 117.1201,
            description = "“冬至饺子夏至面”，夏至新麦登场，山东各地家家吃凉面，既尝新又消暑，是最具代表性的夏至食俗。"
        ),
        FolkCustomEntity(
            title = "大暑送大暑船",
            termName = "大暑",
            province = "浙江",
            city = "台州椒江",
            latitude = 28.6765,
            longitude = 121.4420,
            description = "椒江葭沚一带大暑日造木船载祭品，鼓乐鞭炮相送出海后焚烧，祈求驱瘟消灾、渔业平安，是国家级非遗项目。"
        ),
        FolkCustomEntity(
            title = "大暑饮伏茶",
            termName = "大暑",
            province = "浙江",
            city = "温州",
            latitude = 27.9938,
            longitude = 120.6994,
            description = "伏天在村口凉亭设免费茶桶，以金银花、夏枯草、甘草等煎煮，供行人解暑消渴，是延续数百年的公益民俗。"
        ),
        FolkCustomEntity(
            title = "立秋啃秋",
            termName = "立秋",
            province = "江苏",
            city = "南京",
            latitude = 32.0603,
            longitude = 118.7969,
            description = "立秋日全家围坐啃西瓜，称“啃秋”，寓意咬住秋天、防治秋痱；也有以秋桃、香瓜代替的。"
        ),
        FolkCustomEntity(
            title = "晒秋",
            termName = "立秋",
            province = "江西",
            city = "上饶婺源",
            latitude = 29.2476,
            longitude = 117.8615,
            description = "篁岭古村地无三尺平，村民利用房前屋后与屋顶晾晒辣椒、玉米、南瓜等作物，五彩斑斓，成为闻名全国的“晒秋”景观。"
        ),
        FolkCustomEntity(
            title = "处暑开渔节",
            termName = "处暑",
            province = "浙江",
            city = "宁波象山",
            latitude = 29.4767,
            longitude = 121.8693,
            description = "处暑前后东海休渔期满，象山石浦举行祭海、开船仪式，千帆竞发驶向渔场，是声势浩大的海洋民俗。"
        ),
        FolkCustomEntity(
            title = "白露祭禹王",
            termName = "白露",
            province = "江苏",
            city = "苏州太湖",
            latitude = 31.2000,
            longitude = 120.2000,
            description = "太湖渔民于白露时节在禹王庙举行祭祀，供“素菜”与“荤菜”，祈求太湖平安、鱼虾丰收，香火延续千年。"
        ),
        FolkCustomEntity(
            title = "秋分吃秋菜",
            termName = "秋分",
            province = "广东",
            city = "开平",
            latitude = 22.3764,
            longitude = 112.6983,
            description = "岭南秋分日全家出动采野苋菜，与鱼片同煮“秋汤”，俗谚“秋汤灌脏，洗涤肝肠；阖家老少，平安健康”。"
        ),
        FolkCustomEntity(
            title = "秋分祭月",
            termName = "秋分",
            province = "北京",
            city = "西城",
            latitude = 39.9151,
            longitude = 116.3660,
            description = "古时帝王于秋分日祭月，北京月坛即为明清皇家祭月之所；后因秋分未必逢圆月，祭月之俗逐渐移至中秋节。"
        ),
        FolkCustomEntity(
            title = "寒露登高赏菊",
            termName = "寒露",
            province = "广东",
            city = "广州",
            latitude = 23.1291,
            longitude = 113.2644,
            description = "寒露与重阳相近，岭南人家登白云山、饮菊花酒、佩茱萸，菊花会与菊展是这一时节最盛的民俗活动。"
        ),
        FolkCustomEntity(
            title = "霜降吃柿子",
            termName = "霜降",
            province = "福建",
            city = "福州",
            latitude = 26.0745,
            longitude = 119.2965,
            description = "闽地俗谚“霜降吃丁柿，不会流鼻涕”，认为霜降吃柿子可御寒保暖、补筋骨，是家家户户的节令食俗。"
        ),
        FolkCustomEntity(
            title = "立冬补冬",
            termName = "立冬",
            province = "福建",
            city = "厦门",
            latitude = 24.4798,
            longitude = 118.0894,
            description = "闽南立冬日以“四物”“八珍”炖鸡鸭或羊肉，全家进补，称为“补冬”，俗谚“立冬补冬，补嘴空”。"
        ),
        FolkCustomEntity(
            title = "立冬吃饺子",
            termName = "立冬",
            province = "北京",
            city = "朝阳",
            latitude = 39.9215,
            longitude = 116.4433,
            description = "北方立冬日吃饺子，取“交子之时”谐音，寓意秋冬交替、顺遂安康。"
        ),
        FolkCustomEntity(
            title = "小雪腌腊肉",
            termName = "小雪",
            province = "四川",
            city = "成都",
            latitude = 30.5728,
            longitude = 104.0668,
            description = "“小雪腌菜，大雪腌肉”，川渝人家小雪后灌香肠、腌腊肉，用柏枝烟熏，屋檐下挂满腊味便是年味将至的信号。"
        ),
        FolkCustomEntity(
            title = "大雪藏冰",
            termName = "大雪",
            province = "黑龙江",
            city = "哈尔滨",
            latitude = 45.8038,
            longitude = 126.5350,
            description = "北方大雪封河后取冰块入窖贮藏，供来年夏日消暑与保鲜之用；如今的哈尔滨冰雪大世界正是这一冰雪文化的当代表达。"
        ),
        FolkCustomEntity(
            title = "冬至吃汤圆",
            termName = "冬至",
            province = "福建",
            city = "福州",
            latitude = 26.0745,
            longitude = 119.2965,
            description = "南方冬至家家搓汤圆，取团圆美满之意；福州人家还会把汤圆粘在门框、农具上，称“餉耗”，寓意犒劳一年辛劳。"
        ),
        FolkCustomEntity(
            title = "冬至数九画消寒图",
            termName = "冬至",
            province = "北京",
            city = "东城",
            latitude = 39.9289,
            longitude = 116.4163,
            description = "冬至起“数九”，画一枝素梅共八十一瓣，每日染一瓣，瓣尽而九九出，则春深矣，是古人雅致的盼春方式。"
        ),
        FolkCustomEntity(
            title = "小寒喝腊八粥",
            termName = "小寒",
            province = "全国",
            city = "多地流行",
            latitude = 34.3416,
            longitude = 108.9398,
            description = "腊八以八种以上谷豆果仁熬粥，先祭祖敬神、再馈赠亲友，最后全家食用，是腊月里最温暖的民俗记忆。"
        ),
        FolkCustomEntity(
            title = "大寒尾牙祭",
            termName = "大寒",
            province = "福建",
            city = "泉州",
            latitude = 24.8741,
            longitude = 118.6757,
            description = "旧时商家于大寒前后设“尾牙宴”犒劳伙计，决定来年去留；如今闽台企业仍保留年末尾牙聚餐的传统。"
        ),
        FolkCustomEntity(
            title = "大寒除尘迎年",
            termName = "大寒",
            province = "山西",
            city = "平遥",
            latitude = 37.1892,
            longitude = 112.1743,
            description = "大寒至立春之间扫房除尘、糊窗贴花、蒸年馍、赶年集，晋中人家把“忙年”的每一道工序都做得郑重其事。"
        )
    )

    /** 内置数据覆盖的省份列表（用于地图筛选）。 */
    val provinces: List<String> = all.map { it.province }.distinct().sorted()
}
