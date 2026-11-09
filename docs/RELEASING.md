# Releasing

A release is a tag such as `v2.0.0`. CI builds all jars, makes a GitHub release with the notes from `CHANGELOG.md`, and uploads the jars to Modrinth (slug `windows-titlebar-delete`) when the repository variable `MODRINTH_PROJECT_ID` and the secret `MODRINTH_TOKEN` exist.

## Steps

1. Make sure that CI is green on `master`.
2. Select the version (semantic versioning): a breaking config change increases the major number, a `feat` increases the minor number, only `fix` and `perf` increase the patch number.
3. Set the version in two places: `mod_version` in `modern/gradle.properties` and `modVersion` in `legacy/gradle.properties`. CI builds the jars with the version of the tag.
4. Write the `CHANGELOG.md` section for the version (rules below). Put it at the top, under the title.
5. Commit as `chore: Release X.Y.Z`, push, and push the tag: `git tag vX.Y.Z && git push origin vX.Y.Z`.
6. Monitor the CI run. Check that the release has 39 jars and the notes, and that Modrinth has 39 new versions.

CI makes a release only when a new tag is pushed. The old tags `v1.0.0.0189` and `v1.0.0.1214` start no run.

## Jars

| Build | Jars | Path |
|-------|------|------|
| `modern/` | 18 Fabric, 7 Forge, 12 NeoForge | `modern/<loader>/versions/<node>/build/libs/windowstitlebardelete-<loader>-X.Y.Z+<minecraft_version>.jar` |
| `legacy/` | Forge 1.8.9, Forge 1.12.2 | `legacy/versions/<mc>/build/libs/windowstitlebardelete-forge-X.Y.Z+<mc>.jar` |

The node `26.1` builds for Minecraft 26.1.2, so the name of its jar ends in `+26.1.2.jar`.

## Modrinth

- The repository variable `MODRINTH_PROJECT_ID` is `QRT35UEn`. Without it, CI skips the Modrinth job.
- The owner makes the token on Modrinth and sets the secret `MODRINTH_TOKEN` in the GitHub settings. No other person or tool reads or writes the token.
- The token needs the two scopes "Create versions" and "Write versions". Without "Write versions", `mc-publish` makes a version, cannot complete it, and leaves incomplete duplicate versions on Modrinth.
- Each jar becomes one Modrinth version with the number `vX.Y.Z+<loader>-<mc>`, for example `v2.0.0+neoforge-1.21.8`.
- The matrix in `.github/workflows/ci.yml` gives the game versions of each jar. The Fabric jar of 1.18.2 and later has the loaders `fabric` and `quilt` (`fabric_loaders`).
- If the upload of one jar fails, delete its incomplete version on Modrinth, then upload the jar from the GitHub release again. Take the values from the matrix:

```bash
gh workflow run modrinth.yml -f tag=v2.0.0 -f loader=fabric -f mc=1.21.8 -f loaders="fabric quilt" -f game_versions="1.21.6 1.21.7 1.21.8"
```

## Changelog rules

The section is a summary of the net change since the previous tag. It is not a list of commits.

1. Find the previous tag: `git describe --tags --abbrev=0 --exclude 'v1.0.0.*'`. Read all commits since it: `git log --format='%h %s%n%b' vPREV..HEAD`. Version 1.0.0 has two tags with a different format, `v1.0.0.0189` (Forge 1.8.9) and `v1.0.0.1214` (Fabric 1.21.4). For 2.0.0, compare with the two of them.
2. Keep only changes that users see: features, fixes of bugs that a released version had, performance, compatibility, config changes. Remove refactors, tests, CI, docs and tool changes.
3. Remove a bug that was added and fixed between the two tags. Its net change is zero.
4. Merge commits that change the same thing into one line. Describe the result, not the steps.
5. Write each line as a Conventional Commit title in ASD-STE100 Simplified Technical English: `type: Verb ...`, one sentence, no period, ASCII only. The colon after the type is the only colon. Do not use semicolons. Write for a player, not a developer. Types, in this order: `feat`, `fix`, `perf`, `compat`, `config` (a change that needs users to edit their config). Mark a breaking change with `!` after the type, for example `config!: Rename the key "keepResizeBorder" to "resizeBorder" in the config file`.
6. Start the section with `## X.Y.Z`. Optional: one short sentence under it.

Example:

```
## 2.1.0

- feat: Add the option Hide the titlebar only when the window is maximized
- fix: Keep the titlebar hidden after you leave fullscreen on Forge 1.12.2
- compat: Add jars for Minecraft 26.4
```

CI signs each jar with a GitHub build attestation and adds a `gh attestation verify` command to the end of the GitHub notes. Do not write it in the changelog.

`bash tools/release-notes.sh X.Y.Z` prints the section as CI uses it. When the section is missing, the `Check` job fails and CI makes no release.
