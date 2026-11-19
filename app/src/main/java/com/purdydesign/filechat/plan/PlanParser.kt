package com.purdydesign.filechat.plan

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Turns plan JSON into a [Plan]. Anything that is not a valid plan is rejected, never guessed at. */
object PlanParser {

    private val json = Json {
        classDiscriminator = "op"
        ignoreUnknownKeys = false
        encodeDefaults = true
        prettyPrint = true
    }

    sealed class Result {
        data class Ok(val plan: Plan) : Result()
        data class Error(val message: String) : Result()
    }

    fun parse(text: String): Result = try {
        Result.Ok(json.decodeFromString(Plan.serializer(), text))
    } catch (e: SerializationException) {
        Result.Error("Not a valid plan: ${e.message}")
    } catch (e: IllegalArgumentException) {
        Result.Error("Not a valid plan: ${e.message}")
    }

    fun write(plan: Plan): String = json.encodeToString(Plan.serializer(), plan)
}
