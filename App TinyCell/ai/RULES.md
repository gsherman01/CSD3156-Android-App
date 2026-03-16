# Gemini AI Rules – TinyCell Project

These rules MUST be followed in every Gemini session.

## Core Technology Rules
- Kotlin only
- Jetpack Compose only
- Material 3 only
- Gradle Kotlin DSL
- Min SDK 26
- Target device: Pixel 9

## Architecture Rules
- MVVM architecture
- One ViewModel per screen
- ViewModels expose StateFlow only
- No LiveData
- UI collects state using collectAsState()
- Composables must be stateless
- Navigation handled via Navigation Compose

## Code Safety Rules
- Additive changes only unless explicitly told otherwise
- Do NOT refactor existing working code unless asked
- Code must compile after each step
- Avoid magic strings (use constants)
- Avoid global mutable state

## Dependency Rules
- Use official Android libraries where possible
- Prefer stable APIs
- Experimental APIs must be explicitly annotated

## Output Rules
- Explain reasoning before generating large code blocks
- Keep answers concise and technical
- Ask before introducing new libraries
