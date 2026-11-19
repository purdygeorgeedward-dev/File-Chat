package com.purdydesign.filechat.plan

/**
 * An in-memory picture of a folder: relative path -> is it a folder.
 * The root itself is the empty path "". Used to check a plan without touching real files.
 */
class FileTree private constructor(private val entries: MutableMap<String, Boolean>) {

    fun exists(path: String) = path == ROOT || entries.containsKey(path)
    fun isDir(path: String) = path == ROOT || entries[path] == true

    fun children(dir: String): List<String> {
        val prefix = if (dir == ROOT) "" else "$dir/"
        return entries.keys.filter { it.startsWith(prefix) && !it.removePrefix(prefix).contains('/') }.sorted()
    }

    /** The path and everything under it. */
    fun subtree(path: String): List<String> =
        entries.keys.filter { it == path || it.startsWith("$path/") }.sorted()

    fun copy() = FileTree(entries.toMutableMap())

    fun paths(): List<String> = entries.keys.sorted()

    internal fun put(path: String, isDir: Boolean) {
        entries[path] = isDir
    }

    internal fun remove(path: String) {
        subtree(path).forEach { entries.remove(it) }
    }

    /** Re-homes `from` and everything under it to `to`. Removes the old copy only when `keepOld` is false. */
    internal fun relocate(from: String, to: String, keepOld: Boolean) {
        val moved = subtree(from).associateWith { entries.getValue(it) }
        if (!keepOld) remove(from)
        moved.forEach { (p, dir) -> entries[to + p.removePrefix(from)] = dir }
    }

    companion object {
        const val ROOT = ""

        /**
         * Builds a tree from lines, one path per line. A trailing "/" marks a folder.
         * Parent folders are added automatically.
         */
        fun fromLines(lines: List<String>): FileTree {
            val tree = FileTree(mutableMapOf())
            lines.map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
                val isDir = line.endsWith("/")
                val path = line.trim('/')
                if (path.isEmpty()) return@forEach
                val parts = path.split('/')
                for (i in 1 until parts.size) tree.put(parts.take(i).joinToString("/"), true)
                tree.put(path, isDir)
            }
            return tree
        }
    }
}
