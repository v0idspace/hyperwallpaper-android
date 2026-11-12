package com.v0idspace.hyperwallpaper.data.model

data class Wallpaper(
    val id: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val previewUrl: String,
    val downloadUrl: String,
    val hash: String,
    val tags: List<String> = emptyList(),
    val category: String = "Featured",
    val downloadCount: Long = 0,
    val width: Int = 1080,
    val height: Int = 2400,
    val verifiedStudio: Boolean = true
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val desc: String,
    val primaryTag: String,
    val subtags: List<String>,
    val cnTag: String,
    val cnSubtags: List<String>
)

object CategoryData {
    val categories = listOf(
        Category(
            id = "Featured",
            name = "Curated & Featured",
            icon = "🔥",
            desc = "Hand-picked flagship wallpapers from MIUI EU & HyperOS editions",
            primaryTag = "Nature",
            subtags = listOf("Nature", "Scenery", "Sky", "Landscape"),
            cnTag = "自然",
            cnSubtags = listOf("自然", "风景", "天空", "风景自然")
        ),
        Category(
            id = "AMOLED",
            name = "AMOLED & Dark",
            icon = "🌑",
            desc = "True black 0% pixel backgrounds, OLED neon and dark aesthetic",
            primaryTag = "Dark",
            subtags = listOf("Dark", "Black", "Night", "Neon", "OLED"),
            cnTag = "暗黑",
            cnSubtags = listOf("暗黑", "黑色", "纯黑", "夜景", "发光")
        ),
        Category(
            id = "Nature",
            name = "Nature & Scenery",
            icon = "🌲",
            desc = "Lush forests, towering peaks, valleys and natural scenery",
            primaryTag = "Nature",
            subtags = listOf("Nature", "Scenery", "Forest", "Mountain", "Sky"),
            cnTag = "自然",
            cnSubtags = listOf("自然", "森林", "山脉", "湖泊", "雪山")
        ),
        Category(
            id = "Landscape",
            name = "Landscape",
            icon = "🌄",
            desc = "Sunrises, horizons, lakes, deserts and mountain ranges",
            primaryTag = "Landscape",
            subtags = listOf("Landscape", "Scenery", "Horizon", "Desert"),
            cnTag = "风景",
            cnSubtags = listOf("风景", "日出", "湖泊", "沙漠", "风光")
        ),
        Category(
            id = "Minimal",
            name = "Minimal & Clean",
            icon = "✨",
            desc = "Clean geometry, subtle textures and minimalist aesthetic",
            primaryTag = "Simple",
            subtags = listOf("Simple", "Minimal", "Clean", "Texture"),
            cnTag = "极简",
            cnSubtags = listOf("极简", "简约", "质感", "几何", "纯色")
        ),
        Category(
            id = "Space",
            name = "Space & Cosmic",
            icon = "🪐",
            desc = "Galaxies, nebulae, planets and deep space astronomy",
            primaryTag = "Space",
            subtags = listOf("Space", "Galaxy", "Sky", "Starry"),
            cnTag = "星空",
            cnSubtags = listOf("星空", "宇宙", "银河", "星球", "宇航员")
        ),
        Category(
            id = "City",
            name = "City & Street",
            icon = "🏙️",
            desc = "Urban skylines, night lights and modern architecture",
            primaryTag = "City",
            subtags = listOf("City", "Street", "Night", "Architecture"),
            cnTag = "城市",
            cnSubtags = listOf("城市", "建筑", "夜景", "街道", "摩天大楼")
        ),
        Category(
            id = "Anime",
            name = "Anime & Art",
            icon = "🎨",
            desc = "Japanese anime, digital illustrations and concept art",
            primaryTag = "Anime",
            subtags = listOf("Anime", "Art", "Drawing"),
            cnTag = "动漫",
            cnSubtags = listOf("动漫", "二次元", "手绘", "插画", "人物")
        ),
        Category(
            id = "Car",
            name = "Supercars",
            icon = "🏎️",
            desc = "Sports cars, hypercars and automotive design",
            primaryTag = "Car",
            subtags = listOf("Car", "Vehicle", "Speed"),
            cnTag = "汽车",
            cnSubtags = listOf("汽车", "跑车", "机车", "赛车", "超跑")
        ),
        Category(
            id = "Animals",
            name = "Wildlife & Pets",
            icon = "🐾",
            desc = "Wildlife photography, cats, dogs and majestic animals",
            primaryTag = "Animal",
            subtags = listOf("Animal", "Cat", "Dog", "Wildlife"),
            cnTag = "动物",
            cnSubtags = listOf("动物", "猫", "狗", "萌宠", "野生动物")
        ),
        Category(
            id = "Flowers",
            name = "Flowers & Flora",
            icon = "🌸",
            desc = "Floral aesthetics, botanical blooms and macro flora",
            primaryTag = "Flower",
            subtags = listOf("Flower", "Floral", "Bloom"),
            cnTag = "植物",
            cnSubtags = listOf("植物", "花卉", "花朵", "绿植", "樱花")
        ),
        Category(
            id = "Ocean",
            name = "Ocean & Waves",
            icon = "🌊",
            desc = "Azure waters, coral reefs, coastlines and waves",
            primaryTag = "Ocean",
            subtags = listOf("Ocean", "Sea", "Water", "Beach"),
            cnTag = "海洋",
            cnSubtags = listOf("海洋", "海浪", "沙滩", "水下", "岛屿")
        ),
        Category(
            id = "Abstract",
            name = "Abstract & 3D",
            icon = "🔮",
            desc = "3D fluid renders, chromatic shapes and modern art",
            primaryTag = "Art",
            subtags = listOf("Art", "Abstract", "Color"),
            cnTag = "插画",
            cnSubtags = listOf("插画", "立体", "质感", "流动", "炫彩")
        )
    )
}
