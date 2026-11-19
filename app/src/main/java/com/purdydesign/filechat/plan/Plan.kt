package com.purdydesign.filechat.plan

import kotlinx.serialization.Serializable

/**
 * A plan: what the user asked, the folder it applies to, and the operations in order.
 * `notes` carries anything the planner wants the user to read before confirming.
 */
@Serializable
data class Plan(
    val request: String = "",
    val root: String,
    val ops: List<Operation>,
    val notes: List<String> = emptyList(),
)
