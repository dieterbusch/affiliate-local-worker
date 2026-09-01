package de.example.affiliateworker.worker

import de.example.affiliateworker.model.Job
import reactor.core.publisher.Mono

interface JobHandler {

    fun supports(action: String): Boolean

    fun execute(job: Job): Mono<Any>
}