package de.example.affiliateworker.media

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.nio.file.Files

@Component
class MediaClientImpl(
    builder: WebClient.Builder
) : MediaClient {

    private val client = builder.build()

    override fun download(
        media: List<MediaReference>
    ): Mono<List<DownloadedMedia>> {

        return Mono.defer {

            val downloaded = mutableListOf<DownloadedMedia>()

            downloadSequentially(
                media = media,
                index = 0,
                downloaded = downloaded
            )
                .doOnError {
                    cleanup(downloaded)
                }
        }
    }

    private fun downloadSequentially(
        media: List<MediaReference>,
        index: Int,
        downloaded: MutableList<DownloadedMedia>
    ): Mono<List<DownloadedMedia>> {

        if (index >= media.size) {
            return Mono.just(downloaded.toList())
        }

        return downloadSingle(media[index])
            .flatMap { result ->

                downloaded += result

                downloadSequentially(
                    media = media,
                    index = index + 1,
                    downloaded = downloaded
                )
            }
    }

    private fun downloadSingle(
        reference: MediaReference
    ): Mono<DownloadedMedia> {

        return Mono.defer {

            val file = Files.createTempFile(
                "affiliate-media-${reference.id}-",
                extensionFor(reference.mimeType)
            )

            client
                .get()
                .uri(reference.url)
                .retrieve()
                .bodyToFlux(DataBuffer::class.java)
                .transform { dataBuffers ->
                    DataBufferUtils.write(
                        dataBuffers,
                        file
                    )
                }
                .then(
                    Mono.just(
                        DownloadedMedia(
                            id = reference.id,
                            mimeType = reference.mimeType,
                            path = file
                        )
                    )
                )
                .doOnError {
                    Files.deleteIfExists(file)
                }
        }
    }

    private fun cleanup(
        media: List<DownloadedMedia>
    ) {
        media.forEach { downloaded ->
            try {
                Files.deleteIfExists(downloaded.path)
            } catch (_: Exception) {
                // Cleanup darf den ursprünglichen Fehler nicht überdecken.
            }
        }
    }

    private fun extensionFor(
        mimeType: String
    ): String =
        when (mimeType.lowercase()) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            "image/avif" -> ".avif"
            "video/mp4" -> ".mp4"
            "video/webm" -> ".webm"
            else -> ""
        }
}