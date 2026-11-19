# File-Chat dev log

Dated note of every push. Newest at the bottom. Times are Mountain Time (El Paso).

## 2026-11-19 (Thu), 15:07 MT: phase 1 (plan format and checker) and build workflow

- Android project from scratch: Kotlin, AGP 9.3.1, Gradle 9.6.1 wrapper (same pairing as the Fossify forks), minSdk 26, targetSdk 36. App ID `com.purdydesign.filechat` (working ID, can change before release).
- `plan/Operation.kt`: the six operations (move, copy, rename, mkdir, delete, deleteEmptyDir).
- `plan/Plan.kt`, `plan/PlanParser.kt`: the JSON plan format. Unknown operations and extra fields are rejected.
- `plan/FileTree.kt`, `plan/PlanChecker.kt`: runs a plan against a copy of the folder, op by op. Catches: paths leaving the root ("..", absolute paths), missing sources, two steps writing the same target, a folder moved inside itself, deleting the root, "empty" folders that still hold files.
- `MainActivity.kt`: test screen. Paste a folder listing and a plan, tap CHECK PLAN. Large type, black background. Nothing on the phone is changed.
- Unit tests: `PlanCheckerTest` (11), `PlanParserTest` (5).
- `.github/workflows/android-build.yml`: tests + debug APK on every push to main, APK uploaded as a run artifact.
- Status: not compiled locally (this workspace cannot reach Maven or the Gradle download site); first compile is the GitHub Actions run for this push.
