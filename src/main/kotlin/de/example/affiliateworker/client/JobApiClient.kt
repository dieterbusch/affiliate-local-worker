package de.example.affiliateworker.client

import de.example.affiliateworker.config.WorkerProperties
import de.example.affiliateworker.model.Job
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class JobApiClient(
    private val properties: WorkerProperties,
    builder: WebClient.Builder
) {

    private val client = builder
        .baseUrl(properties.api.baseUrl)
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Bearer ${properties.api.apiKey}"
        )
        .defaultHeader(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
        )
        .build()

    fun claimNextJob(): Mono<Job> =
        client
            .post()
            .uri("/jobs/claim")
            .retrieve()
            .bodyToMono(Job::class.java)

    fun completeJob(
        jobId: String,
        result: Any
    ): Mono<Job> =
        client
            .put()
            .uri("/jobs/{id}", jobId)
            .bodyValue(
                mapOf(
                    "status" to "completed",
                    "result" to result
                )
            )
            .retrieve()
            .bodyToMono(Job::class.java)

    fun failJob(
        jobId: String,
        error: String
    ): Mono<Job> =
        client
            .put()
            .uri("/jobs/{id}", jobId)
            .bodyValue(
                mapOf(
                    "status" to "failed",
                    "error" to error
                )
            )
            .retrieve()
            .bodyToMono(Job::class.java)
}