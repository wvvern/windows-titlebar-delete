# Tested

These are the results of `tools/harness.ps1` for each target on 2026-11-09. A target is the dev client of one loader and one node of the build.

A target passes when all of these are true:

- Each `style` command of the commands file is a checkpoint, and each checkpoint has two proofs. Proof 1: the game reads the style of its window through `JnaWindow` and writes it to the harness log. Proof 2: the script reads the style of the same window from outside the game, with `EnumDesktopWindows` and `GetWindowLong`.
- At each checkpoint, the two styles are equal and the style bits agree with the `# expect` line of the commands file.
- The harness log has no line that starts with `!`, and the game gets to `quit`.

`titlebar.txt` has 6 checkpoints, with a fullscreen step. `titlebar-legacy.txt` has 5 checkpoints and no fullscreen step. See [ARCHITECTURE.md](ARCHITECTURE.md) for the harness and for the gaps that these tests have.

| Target | Minecraft | Loader | Commands file | Checkpoints passed | Result |
|--------|-----------|--------|---------------|--------------------|--------|
| `forge-1.8.9` | 1.8.9 | Forge | `titlebar-legacy.txt` | 5 of 5 | PASS |
| `forge-1.12.2` | 1.12.2 | Forge | `titlebar-legacy.txt` | 5 of 5 | PASS |
| `fabric-1.16.5` | 1.16.5 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.17.1` | 1.17.1 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.18.2` | 1.18.2 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.19.2` | 1.19.2 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.19.4` | 1.19.4 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.20.1` | 1.20.1 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.20.4` | 1.20.4 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.20.6` | 1.20.6 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.1` | 1.21.1 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.3` | 1.21.3 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.4` | 1.21.4 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.5` | 1.21.5 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.8` | 1.21.8 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.10` | 1.21.10 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-1.21.11` | 1.21.11 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-26.1` | 26.1.2 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-26.2` | 26.2 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `fabric-26.3` | 26.3 | Fabric | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.16.5` | 1.16.5 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.17.1` | 1.17.1 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.18.2` | 1.18.2 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.19.2` | 1.19.2 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.19.4` | 1.19.4 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.20.1` | 1.20.1 | Forge | `titlebar.txt` | 6 of 6 | PASS |
| `forge-1.20.4` | 1.20.4 | Forge | `titlebar.txt` | not run | not run |
| `neoforge-1.20.4` | 1.20.4 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.20.6` | 1.20.6 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.1` | 1.21.1 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.3` | 1.21.3 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.4` | 1.21.4 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.5` | 1.21.5 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.8` | 1.21.8 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.10` | 1.21.10 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-1.21.11` | 1.21.11 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-26.1` | 26.1.2 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-26.2` | 26.2 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |
| `neoforge-26.3` | 26.3 | NeoForge | `titlebar.txt` | 6 of 6 | PASS |

38 of 39 targets pass. `forge-1.20.4` has no result, because its Gradle cache was not usable.

The screenshots of the settings screen and the logs of a run are in the `-OutDir` folder of that run. The repository does not keep them.
