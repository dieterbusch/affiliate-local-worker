package de.example.affiliateworker.social

import de.example.affiliateworker.media.DownloadedMedia
import reactor.core.publisher.Mono

interface SocialPublishClient {

    val platform: String

    fun publish(
        content: SocialPublishContent
    ): Mono<SocialPublishResult>
}

data class SocialPublishContent(
    val text: String?,
    val hashtags: List<String>,
    val media: List<DownloadedMedia>,
    val affiliateUrl: String?
)

data class SocialPublishResult(
    val platform: String,
    val published: Boolean,
    val postId: String?,
    val error: String?
)