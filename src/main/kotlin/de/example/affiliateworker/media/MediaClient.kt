package de.example.affiliateworker.media

import reactor.core.publisher.Mono
import java.nio.file.Path

interface MediaClient {

    fun download(
        media: List<MediaReference>
    ): Mono<List<DownloadedMedia>>
}

data class MediaReference(
    val id: String,
    val url: String,
    val mimeType: String
)

data class DownloadedMedia(
    val id: String,
    val mimeType: String,
    val path: Path
)