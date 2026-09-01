package de.example.affiliateworker.social

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
@Profile("mock")
class InstagramPublishClientMock : SocialPublishClient {

    override val platform: String = "instagram"

    override fun publish(
        content: SocialPublishContent
    ): Mono<SocialPublishResult> {

        return Mono.just(
            SocialPublishResult(
                platform = platform,
                published = true,
                postId = "mock-instagram-${UUID.randomUUID()}",
                error = null
            )
        )
    }
}