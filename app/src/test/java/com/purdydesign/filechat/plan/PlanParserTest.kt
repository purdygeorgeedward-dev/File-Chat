package com.purdydesign.filechat.plan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanParserTest {

    @Test
    fun readsThePlanFormat() {
        val text = """
            {"request":"tidy","root":"Movies","ops":[
              {"op":"move","from":"a.mp4","to":"b.mp4"},
              {"op":"deleteEmptyDir","path":"Old"}
            ],"notes":["kept notes.txt"]}
        """.trimIndent()
        val result = PlanParser.parse(text)
        assertTrue(result is PlanParser.Result.Ok)
        val plan = (result as PlanParser.Result.Ok).plan
        assertEquals(Operation.Move("a.mp4", "b.mp4"), plan.ops[0])
        assertEquals(Operation.DeleteEmptyDir("Old"), plan.ops[1])
    }

    @Test
    fun unknownOperationIsRejected() {
        val result = PlanParser.parse("""{"root":"x","ops":[{"op":"format","path":"/"}]}""")
        assertTrue(result is PlanParser.Result.Error)
    }

    @Test
    fun extraFieldsAreRejected() {
        val result = PlanParser.parse("""{"root":"x","ops":[{"op":"delete","path":"a","force":true}]}""")
        assertTrue(result is PlanParser.Result.Error)
    }

    @Test
    fun notJsonIsRejected() {
        assertTrue(PlanParser.parse("move everything please") is PlanParser.Result.Error)
    }

    @Test
    fun writeThenReadGivesTheSamePlan() {
        val plan = Plan("r", "Movies", listOf(Operation.Rename("a.mp4", "b.mp4"), Operation.MakeDir("New")))
        val back = PlanParser.parse(PlanParser.write(plan))
        assertEquals(PlanParser.Result.Ok(plan), back)
    }
}
