package com.purdydesign.filechat

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.purdydesign.filechat.plan.FileTree
import com.purdydesign.filechat.plan.PlanChecker
import com.purdydesign.filechat.plan.PlanParser

/**
 * Phase 1 test screen. No AI and no real files yet:
 * paste a folder listing and a plan, tap Check, see whether the plan is safe to run.
 * Large type and high contrast by default.
 */
class MainActivity : Activity() {

    private val ink = Color.WHITE
    private val paper = Color.BLACK
    private val accent = Color.rgb(0x4F, 0xC3, 0xF7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(32))
            setBackgroundColor(paper)
        }

        column.addView(label("File-Chat", 30f, bold = true))
        column.addView(label("Phase 1: plan checker. Nothing on your phone is changed.", 18f))

        column.addView(label("Folder (one path per line, folders end with /)", 18f, bold = true, top = 24))
        val treeBox = box(SAMPLE_TREE)
        column.addView(treeBox)

        column.addView(label("Plan (JSON)", 18f, bold = true, top = 24))
        val planBox = box(SAMPLE_PLAN)
        column.addView(planBox)

        val result = label("", 20f, top = 16)

        val check = Button(this).apply {
            text = "CHECK PLAN"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(paper)
            setBackgroundColor(accent)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setOnClickListener { result.text = runCheck(treeBox.text.toString(), planBox.text.toString()) }
        }
        column.addView(check, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(24) })
        column.addView(result)

        setContentView(ScrollView(this).apply {
            setBackgroundColor(paper)
            addView(column)
        })
    }

    private fun runCheck(treeText: String, planText: String): String {
        val tree = FileTree.fromLines(treeText.lines())
        return when (val parsed = PlanParser.parse(planText)) {
            is PlanParser.Result.Error -> "REJECTED\n\n${parsed.message}"
            is PlanParser.Result.Ok -> {
                val result = PlanChecker.check(parsed.plan, tree)
                if (result.ok) {
                    "OK: ${parsed.plan.ops.size} steps, all safe.\n\nFolder afterwards:\n" +
                        result.after.paths().joinToString("\n") { if (result.after.isDir(it)) "$it/" else it }
                } else {
                    "REJECTED: ${result.issues.size} problem(s)\n\n" + result.issues.joinToString("\n")
                }
            }
        }
    }

    private fun label(text: String, sp: Float, bold: Boolean = false, top: Int = 0) = TextView(this).apply {
        this.text = text
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sp)
        setTextColor(ink)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(top), 0, dp(4))
    }

    private fun box(initial: String) = EditText(this).apply {
        setText(initial)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setTextColor(ink)
        setBackgroundColor(Color.rgb(0x22, 0x22, 0x22))
        typeface = Typeface.MONOSPACE
        gravity = Gravity.TOP or Gravity.START
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        minLines = 6
        setPadding(dp(12), dp(12), dp(12), dp(12))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object {
        private val SAMPLE_TREE = """
            Show A/ep1.mkv
            Show A/ep2.mkv
            Show B/clip.mp4
            Show B/notes.txt
        """.trimIndent()

        private val SAMPLE_PLAN = """
            {
              "request": "move the mp4 and mkv files out of the subfolders into the root, then delete the subfolders",
              "root": "Movies",
              "ops": [
                {"op": "move", "from": "Show A/ep1.mkv", "to": "ep1.mkv"},
                {"op": "move", "from": "Show A/ep2.mkv", "to": "ep2.mkv"},
                {"op": "move", "from": "Show B/clip.mp4", "to": "clip.mp4"},
                {"op": "deleteEmptyDir", "path": "Show A"},
                {"op": "deleteEmptyDir", "path": "Show B"}
              ]
            }
        """.trimIndent()
    }
}
