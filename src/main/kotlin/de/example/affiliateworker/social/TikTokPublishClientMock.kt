package de.example.affiliateworker.social

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
@Profile("mock")
class TikTokPublishClientMock : SocialPublishClient {

    override val platform: String = "tiktok"

    override fun publish(
        content: SocialPublishContent
    ): Mono<SocialPublishResult> {

        return Mono.just(
            SocialPublishResult(
                platform = platform,
                published = true,
                postId = "mock-tiktok-${UUID.randomUUID()}",
                error = null
            )
        )
    }
}