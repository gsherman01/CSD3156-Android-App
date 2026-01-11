# Gemini Saved Prompts

## Feature Implementation Prompt
You are an Android engineer implementing a SINGLE feature.

Constraints:
- Follow MVVM
- Use StateFlow
- No refactoring
- Additive changes only

Task:
[DESCRIBE FEATURE]

---

## Library Scaffolding Prompt
You are scaffolding a new Android library integration.

Steps:
1. List required dependencies
2. Update Gradle (Kotlin DSL)
3. Provide base classes
4. Show minimal usage example

Library:
[LIBRARY NAME]

---

## Bug Fix Prompt
You are debugging a compile or runtime issue.

Rules:
- Do not introduce new features
- Identify root cause
- Propose minimal fix
- Explain why the fix works

Error:
[PASTE ERROR]

Relevant Files:
[PASTE FILES]
