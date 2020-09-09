package com.goldenmelon.youtv.utils.json

import com.google.android.exoplayer2.video.MediaCodecVideoRenderer

data class loginData(val items: Array<item>)
data class item(val guideSigninPromoRenderer: guideSigninPromoRenderer?)
data class guideSigninPromoRenderer(val signInButton: signInButton)
data class signInButton(val buttonRenderer: buttonRenderer)
data class buttonRenderer(val navigationEndpoint: navigationEndpoint)
data class navigationEndpoint(val commandMetadata: commandMetadata)
data class commandMetadata(val webCommandMetadata: webCommandMetadata)
data class webCommandMetadata(val url:String)

data class contentData(val contents: contents)
data class contents(val twoColumnBrowseResultsRenderer: twoColumnBrowseResultsRenderer?)
data class twoColumnBrowseResultsRenderer(val tabs: Array<tab>)

data class tab(val tabRenderer: tabRenderer)
data class tabRenderer(val content: tabRenderer_content)

data class tabRenderer_content(val richGridRenderer: richGridRenderer, val sectionListRenderer: sectionListRenderer)

data class richGridRenderer(val contents: Array<richGridRenderer_content>)
data class richGridRenderer_content(val richItemRenderer: richItemRenderer?)

data class richItemRenderer(val content: richItemRenderer_content)
data class richItemRenderer_content(val videoRenderer: videoRenderer?)

//Search
data class searchContentData(val contents: searchContents)
data class searchContents(val twoColumnSearchResultsRenderer: twoColumnSearchResultsRenderer?)
data class twoColumnSearchResultsRenderer(val primaryContents: primaryContents)

data class primaryContents(val sectionListRenderer:sectionListRenderer)
data class sectionListRenderer(val contents: Array<sectionListRenderer_content>)

data class sectionListRenderer_content(val itemSectionRenderer: itemSectionRenderer)

data class itemSectionRenderer(val contents:Array<itemSectionRenderer_content>)
data class itemSectionRenderer_content(val videoRenderer: videoRenderer?, val gridRenderer: gridRenderer?)
data class gridRenderer(val items: Array<gridRenderer_item>)
data class gridRenderer_item(val gridVideoRenderer: gridVideoRenderer?)

data class videoRenderer(
    val videoId: String,
    val thumbnail: videoRenderer_thumbnail,
    val title: title,
    val ownerText: ownerText,
    val publishedTimeText: publishedTimeText?,
    val viewCountText: viewCountText?,
    val lengthText: lengthText?,
    val channelThumbnailSupportedRenderers: channelThumbnailSupportedRenderers
)

data class gridVideoRenderer(
    val videoId: String,
    val thumbnail: videoRenderer_thumbnail,
    val title: title,
    val publishedTimeText: publishedTimeText?,
    val viewCountText: viewCountText?,
    val lengthText: lengthText?,
    val thumbnailOverlays: Array<thumbnailOverlay>?
)

data class videoRenderer_thumbnail(val thumbnails: Array<thumbnail>)
data class thumbnail(val url: String, val width: Int, val height: Int)

data class title(val simpleText: String?, val runs: Array<run>?)
data class ownerText(val runs: Array<run>)

data class run(val text: String)

data class lengthText(val simpleText: String)
data class publishedTimeText(val simpleText: String)
data class viewCountText(val simpleText: String, val runs: Array<run>)

data class channelThumbnailSupportedRenderers(val channelThumbnailWithLinkRenderer: channelThumbnailWithLinkRenderer)
data class channelThumbnailWithLinkRenderer(val thumbnail: channelThumbnailWithLinkRenderer_thumbnail, val navigationEndpoint: navigationEndpoint)
data class channelThumbnailWithLinkRenderer_thumbnail(val thumbnails: Array<thumbnail>)

data class thumbnailOverlay(val thumbnailOverlayTimeStatusRenderer:thumbnailOverlayTimeStatusRenderer?)
data class thumbnailOverlayTimeStatusRenderer(val text:text)
data class text(val simpleText: String)