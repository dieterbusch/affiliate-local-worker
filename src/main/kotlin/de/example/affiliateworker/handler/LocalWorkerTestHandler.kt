package de.example.affiliateworker.handler

import de.example.affiliateworker.model.Job
import de.example.affiliateworker.worker.JobHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class LocalWorkerTestHandler : JobHandler {

    override fun supports(action: String): Boolean =
        action == "local-worker-test"

    override fun execute(job: Job): Mono<Any> =
        Mono.just(
            mapOf(
                "message" to "Local worker executed job successfully",
                "jobId" to job.id
            )
        )
}