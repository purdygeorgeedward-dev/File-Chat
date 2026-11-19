# File-Chat

Chatbot that handles file management.

An Android dual-pane file manager built around an AI planner. You say what you want done; the
planner writes a plan; you see it and confirm; plain code carries it out; one tap undoes it.
The AI never touches files: it only writes plans, and only the operation engine changes storage.

Full plan: AI File Manager — Plan (Claude doc): https://claude.ai/artifact/X4CnCUsM2Nj3CNiD9HPYtp

## Status

Phase 1 of 7: operation set, plan format, plan checker, test screen. No AI, no real file access yet.

## Operations

| Operation | Fields | Undo |
| --- | --- | --- |
| `move` | from, to | move back |
| `copy` | from, to | delete the copy |
| `rename` | path, newName | rename back |
| `mkdir` | path | remove if still empty |
| `delete` | path | restore from app trash |
| `deleteEmptyDir` | path | recreate folder |

Code: `app/src/main/java/com/purdydesign/filechat/plan/`

## Build

GitHub Actions builds on every push to `main` (workflow "File-Chat Android build"):
unit tests, then a debug APK, attached to the run as an artifact.

## Notes

Dated log of every push: `DEV_LOG.md`.
