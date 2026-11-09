# Windows Titlebar Delete

A client-side Minecraft mod that hides the Windows titlebar of the game window. The mod id is `windowstitlebardelete`.

Read these files before you change the project:

- [docs/CODING_STANDARDS.md](docs/CODING_STANDARDS.md): the rules for code, text and commits.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): the core, the two builds, the hooks and the harness.
- [docs/RELEASING.md](docs/RELEASING.md): the release steps and the changelog rules.

## Layout

- `core/`: plain Java 8 with all of the logic. It has no Minecraft types.
- `modern/`: Fabric, Forge and NeoForge for 1.16.5 to 26.3 (Stonecutter, Gradle 9.8 on JDK 25).
- `legacy/`: Forge 1.8.9 and 1.12.2 (Gradle 8.8 on JDK 21, game on Java 8).
- `tools/`: `lint.sh`, `release-notes.sh`, `harness.ps1`, `run-hidden.ps1`.

## Checks

```bash
cd modern && ./gradlew :core:check   # compile, Checkstyle and tests of the core
bash tools/lint.sh                   # text and comment rules, from the repository root
```

`tools/lint.sh` examines only the files that git tracks. Run `git add` for a new file before the check.

## Standing rules

1. Commit only with the git identity that the repository has (`wvvern`). Never commit or push with a different account.
2. Never use `--no-verify`.
3. Write all text that users read in ASD-STE100 Simplified Technical English, with ASCII characters only.
4. A comment has one sentence. It never tells the history of the code.
5. Do not add an implicit state machine. `Titlebar.tick` calculates the wanted window style from the config each tick.
6. Do not over-engineer. Add a class only when it removes more code than it adds.
7. Never start a game window, or a program that makes sound, on the visible desktop. Use `tools/harness.ps1`, which uses a hidden desktop.
8. Never download OptiFine.
9. Never read, write or print the Modrinth token. The owner sets the GitHub secrets and variables.
10. Do not touch MultiMC instances.

## Work flow

Do each task in this sequence: plan, review of the plan, implement, `/code-review`, `/simplify`, fix the findings, ship.

## Harness tips

- Pass `-Targets` through `pwsh -Command`, so that PowerShell reads the list as an array: `pwsh -Command "./tools/harness.ps1 -Targets forge-1.8.9,fabric-1.21.11 -OutDir build\harness"`.
- Keep `-OutDir` relative to the repository root.
- Give each agent its own `TEMP` folder. The harness files are in `%TEMP%\wtd-harness\<target>`, and two runs of one target must not share them.
- NeoForge and modern Forge runs need `earlyWindowControl = false` in `config/fml.toml`. `tools/harness.ps1` sets it.
- Make all native calls on the client thread.
- Legacy targets have no fullscreen step, because LWJGL 2 fullscreen can change the display mode of the real monitor.
