# Coding standards

Obey these rules when you change the code. The tag after each rule shows how the project examines it:

- `[cs]`: Checkstyle, with the rules in `core/config/checkstyle/checkstyle.xml`.
- `[lint]`: `tools/lint.sh`. CI runs it in the `Check` job.
- `[javac]`: the compiler settings in `core/build.gradle`.
- `[review]`: a reviewer examines it. No tool can do this check.

A `[cs]`, `[lint]` or `[javac]` violation stops the build. Before you push, run `./gradlew :core:check` in `modern/` (compile, Checkstyle and tests) and `bash tools/lint.sh` in the repository root.

## Scope

Checkstyle examines `core/src/main` and `core/src/test` only. It does not examine `modern/` and `legacy/`:

- Stonecutter changes the `modern/` source text for each version. The inactive branches are code in comments.
- Mixin handler names contain `$`. The adapters use Mojang and MCP names and long target descriptors.

`tools/lint.sh` examines all files that git tracks, the adapters included.

## Architecture

1. The core does not use Minecraft, loader, Mixin, LWJGL, AWT, Swing or ImageIO types. `[javac]` `[cs] IllegalImport, noGameOrDesktopTypes`
2. The core compiles for Java 8. `[javac] options.release = 8`
3. JNA is the only library of the core, and only the `core.win32` package uses it. Other code uses the `NativeWindow` port. `[cs] jnaOnlyInWin32`
4. Use only the JNA API that is the same in JNA 3.4.0 (Minecraft 1.8.9) and in JNA 5: direct mapping with `Native.register`, `Pointer`, and callbacks. Do not use jna-platform. `[cs] onlyJnaFromComSun` `[review]`
5. Make all native calls on the client thread, from `Titlebar.tick` and the code that it calls. `[review]`
6. The core has no Stonecutter comments (`//?`). `[cs] noStonecutter`
7. The core does not write to `System.out` or `System.err`. Use `Platform.info` and `Platform.warn`. `[cs] noConsole`
8. The name of each mixin handler starts with `wtd$`. `@Shadow` and `@Accessor` members keep the target name. `[lint]`
9. A mixin handler or a loader event has no logic. It calls `Titlebar`, and the core makes the decision. `[review]`
10. Config types are immutable. A `with*` method gives a new object. `[review]`
11. Do not use a state machine or edge detection for the window. Each tick, `Titlebar` calculates the wanted style from the config and writes it when it is different. `[review]`

## Text and files

12. Tracked text files contain only ASCII characters. `[lint]` `[cs] asciiOnly`
13. Use spaces, not tabs. Do not put spaces at the end of a line. Each file ends with a newline. `[lint]` `[cs]`
14. Write all text that users read in ASD-STE100 Simplified Technical English: messages, screen labels, README and docs. `[review]`

## Java (core)

15. A line has a maximum of 150 characters. Package lines, import lines and lines with a URL are not examined. `[cs] LineLength`
16. Do not use wildcard imports. You can use a static wildcard import. Remove unused imports. `[cs] AvoidStarImport, UnusedImports`
17. Put imports in three groups, with one empty line between them: `com`, `org`, `wvv` and other libraries; `java` and `javax`; static. Sort each group in ASCII order. `[cs] CustomImportOrder`
18. Names: `UpperCamel` for types, `lowerCamel` for methods, fields, parameters and variables, `UPPER_SNAKE` for constants, lowercase letters and digits for packages. Native methods keep the Win32 name. `[cs]` naming checks
19. Put one space around binary operators and after keywords, commas and casts. Do not put spaces inside parentheses. `[cs]` whitespace checks
20. Put `{` at the end of the line. You can write a short body on one line, for example `{ return name; }`. `[cs] braceOnOwnLine`
21. Each `switch` has a `default`. Mark a fall-through with the comment `// fall through`. `[cs] MissingSwitchDefault, FallThrough`
22. Write modifiers in JLS order. Do not write redundant modifiers. `[cs] ModifierOrder, RedundantModifier`
23. A class with only static members is `final` and has a private constructor. `[cs] HideUtilityClassConstructor, FinalClass`
24. If you override `equals`, also override `hashCode`. Compare strings with `equals`, not `==`. `[cs] EqualsHashCode, StringLiteralEquality`
25. An empty `catch` block has a comment that gives the reason, or its variable has the name `ignored`. `[cs] EmptyCatchBlock`
26. Do not commit `TODO` or `FIXME` comments. Open an issue. `[cs] TodoComment`
27. Layout: indent with 4 spaces. You can write an `if`, `for` or `else` with no braces if the statement fits on one line. `[review]`
28. Javadoc is short. Write it for public types, and for members when the name does not tell enough. For each change to core behavior, add or change a JUnit 5 test. `[review]`
29. A comment has one sentence. Each Javadoc tag (`@param`, `@return`) has one sentence. If you need more, give the code a better name or split it. `[lint]`
30. A comment tells what the code does and why. It does not tell what the code did before a change. Put the history in the commit message. `[lint]` `[review]`
31. Keep the mod small. Add a class only when it removes more code than it adds. `[review]`

## Commits

32. A commit title is a Conventional Commit in ASD-STE100: `type: Verb ...`, at most 72 characters, no period. Types: `feat`, `fix`, `perf`, `refactor`, `test`, `docs`, `ci`, `chore`. The body says why, not how. Release notes come from these titles (see [RELEASING.md](RELEASING.md)). `[review]`

## When a check is incorrect

If a check is incorrect for one element, add `@SuppressWarnings("checkstyle:<check>")`, for example `checkstyle:linelength`, with a comment that gives the reason. Do not change `checkstyle.xml` for one file. The regex checks also match text in string literals.
