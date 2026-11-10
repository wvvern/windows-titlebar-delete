package wvv.windowstitlebardelete.core;

import wvv.windowstitlebardelete.core.dev.Harness;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** A platform for tests that records the log lines and the calls of the core. */
public final class FakePlatform implements Platform, Harness.Host {
    public final File dir;
    public final List<String> infos = new ArrayList<String>();
    public final List<String> warnings = new ArrayList<String>();
    public final List<String> calls = new ArrayList<String>();
    public boolean fullscreen;

    public FakePlatform(File dir) {
        this.dir = dir;
    }

    @Override
    public String describe() { return "Test"; }

    @Override
    public File configDir() { return dir; }

    @Override
    public void info(String message) { infos.add(message); }

    @Override
    public void warn(String message, Throwable error) { warnings.add(message); }

    @Override
    public boolean isFullscreen() { return fullscreen; }

    @Override
    public Harness.Host harnessHost() { return this; }

    @Override
    public void setFullscreen(boolean on) { calls.add("setFullscreen " + on); }

    @Override
    public void quit() { calls.add("quit"); }
}
