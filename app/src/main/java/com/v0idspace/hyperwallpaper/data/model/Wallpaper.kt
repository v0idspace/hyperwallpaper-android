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
    val fileSizeInKB: Long = 0,
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
            subtags = listOf("Nature", "Scenery", "Sky", "Landscape", "Mountain"),
            cnTag = "自然",
            cnSubtags = listOf("自然", "风景", "天空", "风景自然", "雪山")
        ),
        Category(
            id = "AMOLED",
            name = "AMOLED & Dark",
            icon = "🌑",
            desc = "True black 0% pixel backgrounds, OLED neon and dark aesthetic",
            primaryTag = "Dark",
            subtags = listOf("Dark", "Black", "Night", "Neon", "OLED"),
            cnTag = "暗黑",
            cnSubtags = listOf("暗黑", "黑色", "暗色", "夜景", "发光", "黑金")
        ),
        Category(
            id = "Nature",
            name = "Nature & Scenery",
            icon = "🌲",
            desc = "Lush forests, towering peaks, valleys and natural scenery",
            primaryTag = "Nature",
            subtags = listOf("Nature", "Scenery", "Forest", "Mountain", "Sky"),
            cnTag = "自然",
            cnSubtags = listOf("自然", "森林", "雪山", "湖泊", "风景自然")
        ),
        Category(
            id = "Landscape",
            name = "Landscape",
            icon = "🌄",
            desc = "Sunrises, horizons, lakes, deserts and mountain ranges",
            primaryTag = "Landscape",
            subtags = listOf("Landscape", "Scenery", "Horizon", "Desert", "Sunset"),
            cnTag = "风景",
            cnSubtags = listOf("风景", "日落", "晚霞", "湖泊", "风光")
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
            name = "Anime & Illustration",
            icon = "🎨",
            desc = "Anime artistry, Japanese aesthetics, character concepts and paintings",
            primaryTag = "Anime",
            subtags = listOf("Anime", "Art", "Illustration", "Drawing"),
            cnTag = "动漫",
            cnSubtags = listOf("动漫", "插画", "萌系", "二次元", "治愈")
        ),
        Category(
            id = "Abstract",
            name = "Abstract & 3D",
            icon = "🔮",
            desc = "3D geometric renders, fluid dynamics, gradients and glassmorphism",
            primaryTag = "Abstract",
            subtags = listOf("Abstract", "3D", "Gradient", "Render"),
            cnTag = "抽象",
            cnSubtags = listOf("抽象", "光影", "炫彩", "流体", "立体")
        ),
        Category(
            id = "Cyberpunk",
            name = "Cyberpunk & Tech",
            icon = "⚡",
            desc = "Futuristic cityscapes, glowing wires, glitch art and mechanical tech",
            primaryTag = "Cyberpunk",
            subtags = listOf("Cyberpunk", "Tech", "Future", "Neon"),
            cnTag = "机械",
            cnSubtags = listOf("机械", "赛博朋克", "未来", "科技", "霓虹")
        ),
        Category(
            id = "Cars",
            name = "Cars & Vehicles",
            icon = "🏎️",
            desc = "Hypercars, classic vintage motors, motorcycles and aviation",
            primaryTag = "Car",
            subtags = listOf("Car", "Supercar", "Auto", "Vehicle"),
            cnTag = "汽车",
            cnSubtags = listOf("汽车", "跑车", "机车", "超跑")
        ),
        Category(
            id = "Ocean",
            name = "Ocean & Sea",
            icon = "🌊",
            desc = "Azure waters, crashing waves, coral reefs and oceanic horizons",
            primaryTag = "Ocean",
            subtags = listOf("Ocean", "Sea", "Water", "Beach", "Wave"),
            cnTag = "海洋",
            cnSubtags = listOf("海洋", "大海", "沙滩", "浪花")
        )
    )
}
