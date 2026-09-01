package de.example.affiliateworker.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class Job(
    val id: String,
    val status: String,
    val action: String,
    val payload: Map<String, Any?>?,
    val result: Any?,
    val error: String?,

    @JsonProperty("lease_until")
    val leaseUntil: Instant?,

    @JsonProperty("created_at")
    val createdAt: Instant,

    @JsonProperty("updated_at")
    val updatedAt: Instant
)