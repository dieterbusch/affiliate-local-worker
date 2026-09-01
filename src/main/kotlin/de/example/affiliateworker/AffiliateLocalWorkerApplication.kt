package de.example.affiliateworker

import de.example.affiliateworker.config.WorkerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(WorkerProperties::class)
class AffiliateLocalWorkerApplication

fun main(args: Array<String>) {
	runApplication<AffiliateLocalWorkerApplication>(*args)
}