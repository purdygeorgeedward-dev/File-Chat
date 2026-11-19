package com.purdydesign.filechat.plan

/**
 * Checks a plan by running it against a copy of the folder, op by op, before anything real happens.
 * Every problem is reported with the number of the operation that caused it.
 * A plan with any problem is not runnable.
 */
object PlanChecker {

    const val MAX_OPS = 10_000

    data class Issue(val opIndex: Int, val message: String) {
        override fun toString() = if (opIndex < 0) message else "Step ${opIndex + 1}: $message"
    }

    data class Result(val issues: List<Issue>, val after: FileTree) {
        val ok get() = issues.isEmpty()
    }

    fun check(plan: Plan, tree: FileTree): Result {
        val issues = mutableListOf<Issue>()
        val sim = tree.copy()

        if (plan.ops.isEmpty()) issues += Issue(-1, "The plan has no steps.")
        if (plan.ops.size > MAX_OPS) {
            issues += Issue(-1, "The plan has ${plan.ops.size} steps; the limit is $MAX_OPS.")
            return Result(issues, sim)
        }

        plan.ops.forEachIndexed { i, op ->
            val problem = apply(op, sim)
            if (problem != null) issues += Issue(i, problem)
        }
        return Result(issues, sim)
    }

    /** Applies one op to the simulated tree. Returns a problem, or null when the op is fine. */
    private fun apply(op: Operation, sim: FileTree): String? = when (op) {
        is Operation.Move -> moveOrCopy(op.from, op.to, sim, keepOld = false)
        is Operation.Copy -> moveOrCopy(op.from, op.to, sim, keepOld = true)

        is Operation.Rename -> {
            val path = Paths.clean(op.path)
            when {
                path == null -> "\"${op.path}\" is not an allowed path."
                path == FileTree.ROOT -> "The root folder cannot be renamed."
                !Paths.isName(op.newName) -> "\"${op.newName}\" is not an allowed name."
                !sim.exists(path) -> "\"$path\" does not exist."
                else -> {
                    val target = Paths.join(Paths.parent(path), op.newName)
                    if (target != path && sim.exists(target)) "\"$target\" already exists."
                    else { sim.relocate(path, target, keepOld = false); null }
                }
            }
        }

        is Operation.MakeDir -> {
            val path = Paths.clean(op.path)
            when {
                path == null || path == FileTree.ROOT -> "\"${op.path}\" is not an allowed path."
                sim.exists(path) -> "\"$path\" already exists."
                !sim.isDir(Paths.parent(path)) -> "The folder for \"$path\" does not exist."
                else -> { sim.put(path, true); null }
            }
        }

        is Operation.Delete -> {
            val path = Paths.clean(op.path)
            when {
                path == null -> "\"${op.path}\" is not an allowed path."
                path == FileTree.ROOT -> "The root folder cannot be deleted."
                !sim.exists(path) -> "\"$path\" does not exist."
                else -> { sim.remove(path); null }
            }
        }

        is Operation.DeleteEmptyDir -> {
            val path = Paths.clean(op.path)
            when {
                path == null -> "\"${op.path}\" is not an allowed path."
                path == FileTree.ROOT -> "The root folder cannot be deleted."
                !sim.exists(path) -> "\"$path\" does not exist."
                !sim.isDir(path) -> "\"$path\" is a file, not a folder."
                sim.children(path).isNotEmpty() -> "\"$path\" is not empty (${sim.children(path).size} items left)."
                else -> { sim.remove(path); null }
            }
        }
    }

    private fun moveOrCopy(rawFrom: String, rawTo: String, sim: FileTree, keepOld: Boolean): String? {
        val from = Paths.clean(rawFrom)
        val to = Paths.clean(rawTo)
        return when {
            from == null -> "\"$rawFrom\" is not an allowed path."
            to == null -> "\"$rawTo\" is not an allowed path."
            from == FileTree.ROOT || to == FileTree.ROOT -> "The root folder cannot be moved or copied."
            !sim.exists(from) -> "\"$from\" does not exist."
            sim.exists(to) -> "\"$to\" already exists."
            !sim.isDir(Paths.parent(to)) -> "The folder for \"$to\" does not exist."
            to.startsWith("$from/") -> "\"$from\" cannot go inside itself."
            else -> { sim.relocate(from, to, keepOld); null }
        }
    }
}

/** Path rules: relative, "/" separated, no "." or "..", nothing that could leave the root. */
object Paths {

    /** Returns the cleaned path, or null if it is not allowed. "" means the root. */
    fun clean(raw: String): String? {
        if (raw.startsWith("/") || raw.contains('\\') || raw.contains('\u0000')) return null
        val trimmed = raw.trim().trimEnd('/')
        if (trimmed.isEmpty() || trimmed == ".") return FileTree.ROOT
        val parts = trimmed.split('/')
        if (parts.any { !isName(it) }) return null
        return parts.joinToString("/")
    }

    fun isName(name: String) =
        name.isNotBlank() && name != "." && name != ".." && !name.contains('/') &&
            !name.contains('\\') && !name.contains('\u0000') && name == name.trim()

    fun parent(path: String) = path.substringBeforeLast('/', FileTree.ROOT)

    fun join(dir: String, name: String) = if (dir == FileTree.ROOT) name else "$dir/$name"
}
