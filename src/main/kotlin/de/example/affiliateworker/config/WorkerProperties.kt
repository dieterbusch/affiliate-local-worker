package de.example.affiliateworker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "worker")
data class WorkerProperties(
    val api: ApiProperties,
    val amazon: AmazonProperties
){
    data class ApiProperties(
        val baseUrl: String,
        val apiKey: String,
        val pollInterval: Duration
    )

    data class AmazonProperties(
        val partnerId: String
    )
}

