# Tested

This table has the results of `tools/harness.ps1` for each target. The results are from 2026-11-09. All 39 targets pass the default run. 8 targets pass the `remember.txt` run. The other 31 targets did not do the `remember.txt` run. A target is the dev client of one loader and one node of the build.

Each target has two runs. The first run uses the default commands file. The second run uses `-Commands remember.txt`.

A run passes when all of these are true:

- Each `style` command of the commands file is a checkpoint, and each checkpoint has two proofs. Proof 1: the game reads the style of its window through `JnaWindow` and writes it to the harness log. Proof 2: the script reads the style of the same window from outside the game, with `EnumDesktopWindows` and `GetWindowLong`.
- At each checkpoint, the two styles are equal and the style agrees with the `# expect` line of the commands file.
- The harness log has no line that starts with `!`, and the game gets to `quit`.
- After the run, the config file has the text of the `# config-end` line of the commands file.

`titlebar.txt` has 4 checkpoints, with a fullscreen step. `titlebar-legacy.txt` has 3 checkpoints and no fullscreen step. `remember.txt` has 2 checkpoints. See [ARCHITECTURE.md](ARCHITECTURE.md) for the harness and for the gaps that these tests have.

| Target | Minecraft | Loader | Default commands file | Default run | `remember.txt` run |
|--------|-----------|--------|-----------------------|-------------|--------------------|
| `forge-1.8.9` | 1.8.9 | Forge | `titlebar-legacy.txt` | PASS (3 checkpoints) | PASS (2 checkpoints) |
| `forge-1.12.2` | 1.12.2 | Forge | `titlebar-legacy.txt` | PASS (3 checkpoints) | PASS (2 checkpoints) |
| `fabric-1.16.5` | 1.16.5 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |
| `fabric-1.17.1` | 1.17.1 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.18.2` | 1.18.2 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.19.2` | 1.19.2 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.19.4` | 1.19.4 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.20.1` | 1.20.1 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.20.4` | 1.20.4 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.20.6` | 1.20.6 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.1` | 1.21.1 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.3` | 1.21.3 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.4` | 1.21.4 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.5` | 1.21.5 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.8` | 1.21.8 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.10` | 1.21.10 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-1.21.11` | 1.21.11 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |
| `fabric-26.1` | 26.1.2 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-26.2` | 26.2 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `fabric-26.3` | 26.3 | Fabric | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |
| `forge-1.16.5` | 1.16.5 | Forge | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |
| `forge-1.17.1` | 1.17.1 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `forge-1.18.2` | 1.18.2 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `forge-1.19.2` | 1.19.2 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `forge-1.19.4` | 1.19.4 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `forge-1.20.1` | 1.20.1 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `forge-1.20.4` | 1.20.4 | Forge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.20.4` | 1.20.4 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.20.6` | 1.20.6 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.1` | 1.21.1 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.3` | 1.21.3 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.4` | 1.21.4 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.5` | 1.21.5 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.8` | 1.21.8 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.10` | 1.21.10 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-1.21.11` | 1.21.11 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |
| `neoforge-26.1` | 26.1.2 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-26.2` | 26.2 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | not run |
| `neoforge-26.3` | 26.3 | NeoForge | `titlebar.txt` | PASS (4 checkpoints) | PASS (2 checkpoints) |

The logs of a run are in the `-OutDir` folder of that run. The repository does not keep them.
