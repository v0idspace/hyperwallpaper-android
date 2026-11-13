package com.v0idspace.hyperwallpaper.data.model

import com.google.gson.annotations.SerializedName

data class XiaomiApiResponse(
    @SerializedName("code") val code: Int? = 0,
    @SerializedName("apiData") val apiData: XiaomiApiData? = null
)

data class XiaomiApiData(
    @SerializedName("cards") val cards: List<XiaomiCard>? = null
)

data class XiaomiCard(
    @SerializedName("cardType") val cardType: String? = null,
    @SerializedName("products") val products: List<XiaomiProduct>? = null
)

data class XiaomiProduct(
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("productUuid") val productUuid: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("authorName") val authorName: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("designer") val designer: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("originalImageUrl") val originalImageUrl: String? = null,
    @SerializedName("smallCover") val smallCover: String? = null,
    @SerializedName("downloadCount") val downloadCount: Long? = 0,
    @SerializedName("likeCount") val likeCount: Long? = 0,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("metaTags") val metaTags: List<String>? = null,
    @SerializedName("innerTags") val innerTags: List<String>? = null,
    @SerializedName("fileSizeInKB") val fileSizeInKB: Long? = 0
)

data class XiaomiProductDetailResponse(
    @SerializedName("apiCode") val apiCode: Int? = 0,
    @SerializedName("apiData") val apiData: XiaomiProductDetailData? = null
)

data class XiaomiProductDetailData(
    @SerializedName("name") val name: String? = null,
    @SerializedName("designerName") val designerName: String? = null,
    @SerializedName("designerIcon") val designerIcon: String? = null,
    @SerializedName("designerMiId") val designerMiId: String? = null,
    @SerializedName("productCount") val productCount: Int? = 0,
    @SerializedName("tags") val tags: String? = null,
    @SerializedName("fileSize") val fileSize: Long? = 0,
    @SerializedName("status") val status: String? = null
)

data class ProductDetail(
    val id: String,
    val name: String,
    val designerName: String,
    val designerIcon: String,
    val designerMiId: String,
    val productCount: Int,
    val tags: List<String>,
    val fileSizeBytes: Long,
    val formattedFileSize: String,
    val editorialStory: String
)
