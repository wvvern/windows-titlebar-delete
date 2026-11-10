---
name: changelog
description: Write the CHANGELOG.md section for a Windows Titlebar Delete release from the commits since the previous tag. Use when cutting a release, when asked to write or fix release notes or the changelog, or before pushing a vX.Y.Z tag.
---

# Windows Titlebar Delete changelog

The section is a short summary of what changed for players since the previous tag. It is not a commit list. CI publishes it as the GitHub release notes and the Modrinth changelog, so players read every word.

## Steps

1. Find the previous tag with `git describe --tags --abbrev=0 --exclude 'v1.0.0.*'`. Read every commit since it with `git log --format='%h %s%n%b' vPREV..HEAD`. For 2.0.0, the previous version is 1.0.0, which has the two tags `v1.0.0.0189` (Forge 1.8.9) and `v1.0.0.1214` (Fabric 1.21.4). Compare with the two of them.
2. Read the diff of each commit that could matter to players. A title can lie or be vague, and the diff cannot.
3. Keep only changes that players see. These are features, fixes of bugs that a released version had, speed, compatibility and config changes. Drop refactors, tests, CI, docs, tools and code style.
4. Compute the net change. A bug that a commit in the range added and a later commit in the range fixed has a net change of zero, so drop both. A feature that was added and then changed in the range is one line that describes the final result.
5. Merge lines that change the same thing. Describe the result for the player, not the steps.
6. Write the lines (rules below), then put the section at the top of `CHANGELOG.md`, under the title.
7. Run `bash tools/release-notes.sh X.Y.Z` and read the output as a player would.

## Line rules

Each line is a Conventional Commit title.

```
- type: Sentence
```

- **Types, in this order:** `feat`, `fix`, `perf`, `compat`, `config`. Use `config` for a change that makes players edit their config or their options. Put `!` after the type for a breaking change, for example `config!`.
- **One colon only.** The colon after the type is the only colon in the line. Do not use semicolons, dashes as pauses, or parentheses for side notes. If a line needs two clauses, write two lines or a simpler sentence.
- **ASD-STE100.** Start with an approved verb in the imperative (Add, Change, Show, Hide, Keep, Remove, Stop). Use one word for one meaning, for example always "titlebar". Use the active voice. Keep each line to 20 words or fewer. Use no period at the end.
- **Human.** Write for a player, not a developer. Name what the player sees or does: the key, the Controls screen, the config file, the window. Say "you" when it reads better. Do not use class names, method names, ticket numbers or internal terms such as "window style", "mixin" or "adapter".
- **Specific.** Say what changed and where. "Keep the titlebar hidden after fullscreen on 1.8.9" is good. "Fix several bugs" and "Improve compatibility" are not.
- **Plain.** ASCII only. Do not use marketing words (powerful, seamless, blazing, robust), emoji or exclamation marks.
- **Optional intro.** One short sentence under the heading is permitted, for example "First release." Do not add more prose.
- **No verify command.** CI adds the `gh attestation verify` block to the end of the GitHub notes. Do not write it in the changelog.

## Example

```
## 2.1.0

- feat: Add "onlyWhenMaximized" to the config file, which hides the titlebar only in a maximized window
- fix: Keep the titlebar hidden after you leave fullscreen on Forge 1.12.2
- compat: Add jars for Minecraft 26.4
- config!: Rename the key "keepResizeBorder" to "resizeBorder" in the config file
```

Bad lines and their fixes.

| Bad | Good |
|---|---|
| `feat: Add a toggle key; default F12` | `feat: Add the key Toggle the titlebar to the Controls screen, with F12 as the default` |
| `fix: Fixed a bug where JnaWindow matched by title` | `fix: Change only the game window, and not a window of a different program` |
| `compat: Seamless support for all loaders!` | `compat: Add jars for Quilt on Minecraft 1.18.2 and later` |
| `feat: Config: remember the titlebar` | `feat: Keep the titlebar hidden or shown as you left it when you start the game again` |

## Before you finish

- Each line starts with an approved type and has exactly one colon.
- No line has a semicolon, a period at the end, or a non-ASCII character.
- No line names a change that players cannot see.
- No line names a bug that no released version had.
- The version heading matches `mod_version` in `modern/gradle.properties`.

The release steps (version number, tag, CI check) are in `docs/RELEASING.md`.
