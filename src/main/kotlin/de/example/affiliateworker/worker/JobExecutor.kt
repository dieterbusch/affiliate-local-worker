package de.example.affiliateworker.worker

import de.example.affiliateworker.model.Job
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JobExecutor(
    private val handlers: List<JobHandler>
) {

    fun execute(job: Job): Mono<Any> {

        val handler = handlers.firstOrNull {
            it.supports(job.action)
        }
            ?: return Mono.error(
                IllegalArgumentException(
                    "Unknown job action: ${job.action}"
                )
            )

        return Mono.defer {
            handler.execute(job)
        }
    }
}