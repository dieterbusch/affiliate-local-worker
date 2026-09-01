package de.example.affiliateworker.client

import de.example.affiliateworker.config.WorkerProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Profile("mock")
class AmazonAffiliateClientMock(
    private val properties: WorkerProperties
) : AmazonAffiliateClient {

    override fun createAffiliateLink(url: String): Mono<String> {

        val separator =
            if (url.contains("?")) "&" else "?"

        return Mono.just(
            "$url${separator}tag=${properties.amazon.partnerId}"
        )
    }
}
