package com.example.jieqi.data.content

/**
 * 一幅节气配画的元数据。
 *
 * 素材来自大都会艺术博物馆 Open Access（CC0 公共领域），可自由使用与分发，
 * 详情页与「图片来源」页会展示作者、年代与馆藏信息。
 *
 * @param file assets 下的相对路径
 * @param title 作品名
 * @param artist 作者（佚名时为空）
 * @param date 年代
 * @param culture 文化/地区
 * @param medium 材质
 * @param objectId 大都会博物馆藏品编号
 */
data class Artwork(
    val file: String,
    val title: String,
    val artist: String,
    val date: String,
    val culture: String,
    val medium: String,
    val objectId: Int
) {
    /** 官方藏品页面链接。 */
    val sourceUrl: String get() = "https://www.metmuseum.org/art/collection/search/$objectId"

    /** 图片下方的署名小字。 */
    val creditLine: String
        get() = listOf(title, artist, date, culture)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
}

/**
 * 节气配画库（全部为 CC0 公共领域藏品）。
 *
 * 每个节气配一到两幅意象相近的中国 / 日本绘画，
 * 既让内容有真实画面，也保证来源清晰、可自由使用。
 */
object ArtworkLibrary {

    private fun art(id: Int, title: String, artist: String, date: String, culture: String, medium: String) =
        Artwork("images/met_$id.jpg", title, artist, date, culture, medium, id)

    // ---------- 春 ----------
    private val plumJinNong = art(36432, "梅花图", "金农", "1757 年", "中国", "纸本水墨")
    private val swallowWisteria = art(36726, "紫藤双燕", "歌川广重", "1840 年代", "日本", "版画")
    private val mistyRainRiver = art(36188, "春江烟雨", "吴石仙", "清代", "中国", "金笺水墨（扇面）")
    private val nightRain = art(37145, "座敷八景 · 双架夜雨", "铃木春信", "约 1766 年", "日本", "版画")
    private val swallowScroll = art(57232, "燕子", "狩野常信", "江户时代", "日本", "纸本设色")
    private val peonyZhao = art(36161, "牡丹图", "赵之谦", "1862 年", "中国", "金笺设色（扇面）")
    private val herbaceousPeony = art(49456, "芍药图", "胡远", "清代", "中国", "金笺设色（扇面）")
    private val landscapeWithPoems = art(36131, "山水题诗册", "龚贤", "1688 年", "中国", "纸本水墨")

    // ---------- 夏 ----------
    private val fishermanLodge = art(40057, "溪山渔隐图", "李结", "约 1170 年", "中国", "绢本设色")
    private val wangchuanVilla = art(49187, "辋川别墅图", "王原祁", "1711 年", "中国", "纸本设色")
    private val lotusWaterbirds = art(36437, "荷花水鸟图", "佚名", "约 1300 年", "中国", "绢本设色（双幅）")
    private val clearingAfterRain = art(49150, "溪山雨后", "王翚", "1662 年", "中国", "纸本水墨")
    private val shadedDwellings = art(48949, "溪山清远", "董其昌", "约 1622—25 年", "中国", "纸本水墨")

    // ---------- 秋 ----------
    private val autumnWood = art(45682, "秋林静钓", "沈周", "1475 年", "中国", "纸本设色")
    private val autumnTreesGrasses = art(53633, "秋草屏风", "佚名", "17 世纪末—18 世纪初", "日本", "设色屏风")
    private val geeseUnderMoon = art(36742, "满月飞雁", "歌川广重", "约 1833 年", "日本", "版画")
    private val tossingFans = art(53439, "投扇图屏风", "佚名", "17 世纪初", "日本", "金地设色屏风")
    private val travelersStreams = art(41485, "溪山行旅图", "吴历", "约 1670 年代", "中国", "纸本水墨")

    // ---------- 冬 ----------
    private val bambooInSnow = art(49068, "雪竹图", "大鹏正鲲", "1774 年", "日本", "绢本水墨（双幅）")
    private val whitePlumMoon = art(53408, "月下白梅", "伊藤若冲", "1755 年", "日本", "绢本设色")
    private val plumBambooPoems = art(40285, "梅花竹石图咏", "赵孟坚", "1260 年", "中国", "纸本水墨")
    private val horseNightShining = art(39901, "照夜白图", "韩干", "约 750 年", "中国", "纸本水墨")
    private val womanPlumNight = art(41057, "夜赏梅花", "铃木春信", "约 1766 年", "日本", "版画")
    private val plumJinLan = art(49475, "梅花", "金兰", "1886 年", "中国", "纸本水墨（扇面）")
    private val fragrantSnowBridge = art(40080, "断桥暗香", "王冕", "14 世纪", "中国", "绢本水墨")
    private val craneBamboo = art(39993, "竹鹤图", "佚名", "14—15 世纪初", "中国", "绢本设色")
    private val streamsWithoutEnd = art(36049, "溪山无尽图", "佚名", "17 世纪", "中国", "绢本设色")
    private val oldTreesLevelDistance = art(39668, "老树平林", "郭熙（传）", "约 1080 年", "中国", "纸本水墨")
    private val dwellingMountains = art(49133, "云山隐居", "龚贤", "17 世纪", "中国", "纸本水墨")
    private val rockyLandscape = art(39931, "奇峰图册", "戴本孝", "17 世纪末", "中国", "纸本水墨")

    /** 全部素材（供「图片来源」页展示）。 */
    val all: List<Artwork> = listOf(
        plumJinNong, swallowWisteria, mistyRainRiver, nightRain, swallowScroll,
        peonyZhao, herbaceousPeony, landscapeWithPoems,
        fishermanLodge, wangchuanVilla, lotusWaterbirds, clearingAfterRain, shadedDwellings,
        autumnWood, autumnTreesGrasses, geeseUnderMoon, tossingFans, travelersStreams,
        bambooInSnow, whitePlumMoon, plumBambooPoems, horseNightShining, womanPlumNight,
        plumJinLan, fragrantSnowBridge, craneBamboo, streamsWithoutEnd, oldTreesLevelDistance,
        dwellingMountains, rockyLandscape
    )

    private val byTerm: Map<Int, List<Artwork>> = mapOf(
        1 to listOf(plumJinNong, swallowWisteria),           // 立春 · 梅开迎春、紫藤双燕
        2 to listOf(mistyRainRiver, nightRain),              // 雨水 · 春江烟雨、夜雨
        3 to listOf(swallowWisteria, swallowScroll),         // 惊蛰 · 燕子归来
        4 to listOf(peonyZhao, herbaceousPeony),             // 春分 · 牡丹芍药
        5 to listOf(mistyRainRiver, landscapeWithPoems),     // 清明 · 烟雨山水
        6 to listOf(herbaceousPeony, peonyZhao),             // 谷雨 · 雨生百谷、牡丹
        7 to listOf(fishermanLodge, wangchuanVilla),         // 立夏 · 溪山渔隐
        8 to listOf(shadedDwellings, wangchuanVilla),        // 小满 · 溪山清远
        9 to listOf(clearingAfterRain, fishermanLodge),      // 芒种 · 雨后溪山
        10 to listOf(lotusWaterbirds, swallowScroll),        // 夏至 · 荷花水鸟
        11 to listOf(swallowScroll, lotusWaterbirds),        // 小暑 · 荷塘燕子
        12 to listOf(clearingAfterRain, rockyLandscape),     // 大暑 · 雨后云山
        13 to listOf(autumnWood, travelersStreams),          // 立秋 · 秋林
        14 to listOf(geeseUnderMoon, autumnTreesGrasses),    // 处暑 · 雁阵、秋草
        15 to listOf(geeseUnderMoon, autumnTreesGrasses),    // 白露 · 月下飞雁
        16 to listOf(tossingFans, geeseUnderMoon),           // 秋分 · 金地屏风、满月
        17 to listOf(autumnTreesGrasses, autumnWood),        // 寒露 · 秋草秋林
        18 to listOf(autumnWood, oldTreesLevelDistance),     // 霜降 · 秋林老树
        19 to listOf(streamsWithoutEnd, dwellingMountains),  // 立冬 · 溪山无尽
        20 to listOf(bambooInSnow, fragrantSnowBridge),      // 小雪 · 雪竹
        21 to listOf(dwellingMountains, streamsWithoutEnd),  // 大雪 · 云山
        22 to listOf(plumBambooPoems, whitePlumMoon),        // 冬至 · 梅竹、月下白梅
        23 to listOf(whitePlumMoon, plumJinLan),             // 小寒 · 月下白梅、梅花
        24 to listOf(plumJinNong, horseNightShining)         // 大寒 · 梅花图、照夜白
    )

    /** 取某个节气的配画。 */
    fun of(termIndex: Int): List<Artwork> = byTerm[termIndex].orEmpty()

    /** 取某个节气的主图（列表卡片用）。 */
    fun hero(termIndex: Int): Artwork? = of(termIndex).firstOrNull()

    /** 该节气是否有真实配图（没有则界面回退到季节动画）。 */
    fun hasArt(termIndex: Int): Boolean = of(termIndex).isNotEmpty()

    /** 素材总数，用于「图片来源」页文案。 */
    val count: Int get() = all.size

    /** 额外素材：与节气不直接对应、但可作为氛围图的藏品（本库暂未使用）。 */
    val unusedForTerms: List<Artwork> = listOf(
        craneBamboo, womanPlumNight, whitePlumMoon, rockyLandscape
    )
}
