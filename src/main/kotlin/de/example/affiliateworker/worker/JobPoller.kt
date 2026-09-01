package de.example.affiliateworker.worker

import de.example.affiliateworker.client.JobApiClient
import de.example.affiliateworker.config.WorkerProperties
import de.example.affiliateworker.model.Job
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class JobPoller(
    private val jobApiClient: JobApiClient,
    private val jobExecutor: JobExecutor,
    private val properties: WorkerProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun start() {
        log.info("Job poller starting")

        Flux.defer {
            log.info("Polling for next job...")
            processNextJob()
        }
            .repeatWhen { signals ->
                signals.delayElements(properties.api.pollInterval)
            }
            .subscribe(
                {},
                { error ->
                    // Dieser Fehler sollte eigentlich nicht mehr auftreten.
                    // Der Poller darf trotzdem nicht still sterben.
                    log.error(
                        "Job poller stopped unexpectedly",
                        error
                    )
                }
            )
    }

    private fun processNextJob(): Mono<Void> {

        return jobApiClient
            .claimNextJob()
            .flatMap { job ->

                log.info(
                    "Claimed job: id={}, action={}, leaseUntil={}",
                    job.id,
                    job.action,
                    job.leaseUntil
                )

                executeJobSafely(job)
            }
            .switchIfEmpty(
                Mono.fromRunnable {
                    log.info("No pending job available")
                }
            )
            .onErrorResume { error ->

                /*
                 * Fehler beim Claim bzw. bei der Kommunikation mit der
                 * Job API dürfen den Poller ebenfalls nicht beenden.
                 */
                log.error(
                    "Error while polling or claiming next job",
                    error
                )

                Mono.empty()
            }
            .then()
    }

    private fun executeJobSafely(
        job: Job
    ): Mono<Void> {

        /*
         * defer ist wichtig:
         *
         * Falls jobExecutor.execute(job) synchron eine Exception wirft,
         * wird sie dadurch in die Reactor-Fehlerkette überführt.
         */
        return Mono.defer {
            jobExecutor.execute(job)
        }
            .flatMap { result ->
                jobApiClient.completeJob(
                    job.id,
                    result
                )
            }
            .doOnNext { completed ->
                log.info(
                    "Completed job: id={}, status={}",
                    completed.id,
                    completed.status
                )
            }
            .then()
            .onErrorResume { error ->

                log.error(
                    "Job execution failed: id={}, action={}",
                    job.id,
                    job.action,
                    error
                )

                failJobSafely(
                    job,
                    error
                )
            }
    }

    private fun failJobSafely(
        job: Job,
        error: Throwable
    ): Mono<Void> {

        return Mono.defer {
            jobApiClient.failJob(
                job.id,
                error.message
                    ?: error.javaClass.simpleName
            )
        }
            .doOnNext { failed ->
                log.info(
                    "Job marked as failed: id={}, status={}",
                    failed.id,
                    failed.status
                )
            }
            .then()
            .onErrorResume { failError ->

                /*
                 * Auch ein Fehler beim Markieren als failed darf den Poller
                 * nicht beenden.
                 *
                 * Der ursprüngliche Fehler wird bereits geloggt.
                 */
                log.error(
                    "Failed to mark job as failed: id={}",
                    job.id,
                    failError
                )

                Mono.empty()
            }
    }
}