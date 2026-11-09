package wvv.windowstitlebardelete.core.config;

/** The settings that the config file stores, as an immutable value. */
public final class Config {
    public static final Config DEFAULTS = new Config(true, true);

    private final boolean hideOnStartup;
    private final boolean keepResizeBorder;

    public Config(boolean hideOnStartup, boolean keepResizeBorder) {
        this.hideOnStartup = hideOnStartup;
        this.keepResizeBorder = keepResizeBorder;
    }

    /** True to hide the titlebar when the game starts. */
    public boolean hideOnStartup() { return hideOnStartup; }

    /** True to keep the resize border while the titlebar is hidden. */
    public boolean keepResizeBorder() { return keepResizeBorder; }

    public Config withHideOnStartup(boolean value) {
        return new Config(value, keepResizeBorder);
    }

    public Config withKeepResizeBorder(boolean value) {
        return new Config(hideOnStartup, value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Config)) return false;
        Config c = (Config) o;
        return hideOnStartup == c.hideOnStartup && keepResizeBorder == c.keepResizeBorder;
    }

    @Override
    public int hashCode() {
        return (hideOnStartup ? 2 : 0) + (keepResizeBorder ? 1 : 0);
    }

    @Override
    public String toString() {
        return "hideOnStartup=" + hideOnStartup + ", keepResizeBorder=" + keepResizeBorder;
    }
}
