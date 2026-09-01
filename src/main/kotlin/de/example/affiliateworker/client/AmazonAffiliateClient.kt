package de.example.affiliateworker.client

import reactor.core.publisher.Mono

interface AmazonAffiliateClient {

    fun createAffiliateLink(url: String): Mono<String>
}