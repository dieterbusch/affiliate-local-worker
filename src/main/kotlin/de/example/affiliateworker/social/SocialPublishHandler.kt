package de.example.affiliateworker.social

import de.example.affiliateworker.client.AmazonAffiliateClient
import de.example.affiliateworker.media.MediaClient
import de.example.affiliateworker.media.MediaReference
import de.example.affiliateworker.media.DownloadedMedia
import de.example.affiliateworker.model.Job
import de.example.affiliateworker.worker.JobHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Files

@Component
class SocialPublishHandler(
    private val amazonAffiliateClient: AmazonAffiliateClient,
    private val mediaClient: MediaClient,
    private val socialPublishClients: List<SocialPublishClient>
) : JobHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun supports(action: String): Boolean =
        action == "social-publish"

    override fun execute(job: Job): Mono<Any> {

        val payload = job.payload
            ?: return Mono.error(
                IllegalArgumentException("Missing job payload")
            )

        val content = payload["content"] as? Map<*, *>
            ?: return Mono.error(
                IllegalArgumentException("Missing payload.content")
            )

        val text = content["text"] as? String

        val hashtags =
            (content["hashtags"] as? List<*>)
                ?.filterIsInstance<String>()
                ?: emptyList()

        val amazonUrl =
            content["amazonUrl"] as? String

        val mediaReferences =
            extractMediaReferences(content)

        if (
            amazonUrl == null &&
            text == null &&
            hashtags.isEmpty() &&
            mediaReferences.isEmpty()
        ) {
            return Mono.error(
                IllegalArgumentException("Social content is empty")
            )
        }

        if (socialPublishClients.isEmpty()) {
            return Mono.error(
                IllegalStateException(
                    "No social publish clients configured"
                )
            )
        }

        return mediaClient
            .download(mediaReferences)
            .flatMap { downloadedMedia ->

                if (amazonUrl != null) {
                    amazonAffiliateClient
                        .createAffiliateLink(amazonUrl)
                        .flatMap { affiliateUrl ->
                            val finalText = text?.replace(amazonUrl, affiliateUrl)
                            val containsAmazonUrl = text?.contains(amazonUrl) == true
                            val replaced = containsAmazonUrl && finalText != text

                            log.info(
                                "Affiliate link prepared: amazonUrlPresent={}, textContainedAmazonUrl={}, amazonUrlReplaced={}",
                                amazonUrl.isNotBlank(),
                                containsAmazonUrl,
                                replaced
                            )


                            publish(
                                text = finalText,
                                hashtags = hashtags,
                                media = downloadedMedia,
                                affiliateUrl = affiliateUrl
                            )
                        }
                } else {
                    publish(
                        text = text,
                        hashtags = hashtags,
                        media = downloadedMedia,
                        affiliateUrl = null
                    )
                }
            }
            .map { results ->
                mapOf(
                    "publications" to results
                )
            }
    }

    private fun publish(
        text: String?,
        hashtags: List<String>,
        media: List<DownloadedMedia>,
        affiliateUrl: String?
    ): Mono<List<SocialPublishResult>> {

        val content = SocialPublishContent(
            text = text,
            hashtags = hashtags,
            media = media,
            affiliateUrl = affiliateUrl
        )

        return Flux.fromIterable(socialPublishClients)
            .flatMap { client ->
                client.publish(content)
                    .onErrorResume { error ->
                        Mono.just(
                            SocialPublishResult(
                                platform = client.platform,
                                published = false,
                                postId = null,
                                error = error.message
                                    ?: error.javaClass.simpleName
                            )
                        )
                    }
            }
            .collectList()
            .doFinally {
                cleanupMedia(media)
            }
    }

    private fun extractMediaReferences(
        content: Map<*, *>
    ): List<MediaReference> {

        val media =
            content["media"] as? List<*>
                ?: return emptyList()

        return media.mapIndexed { index, item ->

            val map = item as? Map<*, *>
                ?: throw IllegalArgumentException(
                    "Invalid media entry at position $index"
                )

            val id =
                map["id"] as? String
                    ?: throw IllegalArgumentException(
                        "Missing media.id at position $index"
                    )

            val url =
                map["url"] as? String
                    ?: throw IllegalArgumentException(
                        "Missing media.url at position $index"
                    )

            val mimeType =
                map["mimeType"] as? String
                    ?: throw IllegalArgumentException(
                        "Missing media.mimeType at position $index"
                    )

            MediaReference(
                id = id,
                url = url,
                mimeType = mimeType
            )
        }
    }

    private fun cleanupMedia(
        media: List<DownloadedMedia>
    ) {
        media.forEach { downloaded ->
            try {
                Files.deleteIfExists(downloaded.path)
            } catch (_: Exception) {
                // Ein Cleanup-Fehler darf den bereits verarbeiteten
                // Publishing-Job nicht nachträglich fehlschlagen lassen.
            }
        }
    }
}