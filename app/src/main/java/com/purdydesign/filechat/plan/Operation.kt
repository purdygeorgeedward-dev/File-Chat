package com.purdydesign.filechat.plan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The only file operations a plan may contain.
 * The AI (later phases) writes plans made of these; nothing else can touch storage.
 * All paths are relative to the plan's root and use "/" as the separator.
 */
@Serializable
sealed class Operation {

    /** Move a file or folder. `to` is the full new path, not a folder to drop it into. */
    @Serializable
    @SerialName("move")
    data class Move(val from: String, val to: String) : Operation()

    /** Copy a file or folder. `to` is the full path of the copy. */
    @Serializable
    @SerialName("copy")
    data class Copy(val from: String, val to: String) : Operation()

    /** Rename in place. `newName` is a name only, no folders. */
    @Serializable
    @SerialName("rename")
    data class Rename(val path: String, val newName: String) : Operation()

    /** Create a folder. Its parent must already exist. */
    @Serializable
    @SerialName("mkdir")
    data class MakeDir(val path: String) : Operation()

    /** Delete a file or folder. The engine (phase 3) moves it to the app trash, never erases it. */
    @Serializable
    @SerialName("delete")
    data class Delete(val path: String) : Operation()

    /** Delete a folder only if it is empty at that point in the plan. */
    @Serializable
    @SerialName("deleteEmptyDir")
    data class DeleteEmptyDir(val path: String) : Operation()
}
