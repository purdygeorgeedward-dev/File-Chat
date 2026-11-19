package com.purdydesign.filechat.plan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanCheckerTest {

    private val movies = FileTree.fromLines(
        listOf("Show A/ep1.mkv", "Show A/ep2.mkv", "Show B/clip.mp4", "Show B/notes.txt")
    )

    private fun plan(vararg ops: Operation) = Plan(root = "Movies", ops = ops.toList())

    @Test
    fun flattenVideosAndDeleteEmptyFolder() {
        val result = PlanChecker.check(
            plan(
                Operation.Move("Show A/ep1.mkv", "ep1.mkv"),
                Operation.Move("Show A/ep2.mkv", "ep2.mkv"),
                Operation.Move("Show B/clip.mp4", "clip.mp4"),
                Operation.DeleteEmptyDir("Show A"),
            ),
            movies,
        )
        assertTrue(result.issues.toString(), result.ok)
        assertEquals(
            listOf("Show B", "Show B/notes.txt", "clip.mp4", "ep1.mkv", "ep2.mkv"),
            result.after.paths(),
        )
    }

    @Test
    fun folderWithFilesLeftIsNotEmpty() {
        val result = PlanChecker.check(
            plan(Operation.Move("Show B/clip.mp4", "clip.mp4"), Operation.DeleteEmptyDir("Show B")),
            movies,
        )
        assertFalse(result.ok)
        assertEquals(1, result.issues.single().opIndex)
    }

    @Test
    fun pathsCannotLeaveTheRoot() {
        listOf("../secret.txt", "/sdcard/x", "Show A/../../x", "a\\b").forEach { bad ->
            val result = PlanChecker.check(plan(Operation.Delete(bad)), movies)
            assertFalse(bad, result.ok)
        }
    }

    @Test
    fun twoStepsCannotWriteTheSameTarget() {
        val result = PlanChecker.check(
            plan(Operation.Move("Show A/ep1.mkv", "x.mkv"), Operation.Move("Show A/ep2.mkv", "x.mkv")),
            movies,
        )
        assertEquals(listOf(1), result.issues.map { it.opIndex })
    }

    @Test
    fun missingSourceIsCaught() {
        val result = PlanChecker.check(plan(Operation.Move("nope.mp4", "x.mp4")), movies)
        assertFalse(result.ok)
    }

    @Test
    fun renameStaysInItsFolder() {
        val ok = PlanChecker.check(plan(Operation.Rename("Show A/ep1.mkv", "Pilot.mkv")), movies)
        assertTrue(ok.after.exists("Show A/Pilot.mkv"))
        val bad = PlanChecker.check(plan(Operation.Rename("Show A/ep1.mkv", "../Pilot.mkv")), movies)
        assertFalse(bad.ok)
    }

    @Test
    fun movingAFolderMovesItsContents() {
        val result = PlanChecker.check(
            plan(Operation.MakeDir("TV"), Operation.Move("Show A", "TV/Show A")),
            movies,
        )
        assertTrue(result.issues.toString(), result.ok)
        assertTrue(result.after.exists("TV/Show A/ep2.mkv"))
        assertFalse(result.after.exists("Show A"))
    }

    @Test
    fun folderCannotGoInsideItself() {
        val result = PlanChecker.check(plan(Operation.Move("Show A", "Show A/inner")), movies)
        assertFalse(result.ok)
    }

    @Test
    fun copyKeepsTheOriginal() {
        val result = PlanChecker.check(plan(Operation.Copy("Show B", "Backup")), movies)
        assertTrue(result.after.exists("Show B/clip.mp4"))
        assertTrue(result.after.exists("Backup/clip.mp4"))
    }

    @Test
    fun rootCannotBeDeleted() {
        assertFalse(PlanChecker.check(plan(Operation.Delete("")), movies).ok)
        assertFalse(PlanChecker.check(plan(Operation.Delete("/")), movies).ok)
    }

    @Test
    fun emptyPlanIsRejected() {
        assertFalse(PlanChecker.check(plan(), movies).ok)
    }
}
