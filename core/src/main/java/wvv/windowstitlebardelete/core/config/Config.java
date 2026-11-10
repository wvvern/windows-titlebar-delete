package wvv.windowstitlebardelete.core.config;

/** The values that the config file stores, as an immutable value. */
public final class Config {
    public static final Config DEFAULTS = new Config(true, true);

    private final boolean hidden;
    private final boolean keepResizeBorder;

    public Config(boolean hidden, boolean keepResizeBorder) {
        this.hidden = hidden;
        this.keepResizeBorder = keepResizeBorder;
    }

    /** True while the titlebar is hidden, which the toggle key changes. */
    public boolean hidden() { return hidden; }

    /** True to keep the resize border while the titlebar is hidden. */
    public boolean keepResizeBorder() { return keepResizeBorder; }

    public Config withHidden(boolean value) {
        return new Config(value, keepResizeBorder);
    }

    public Config withKeepResizeBorder(boolean value) {
        return new Config(hidden, value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Config)) return false;
        Config c = (Config) o;
        return hidden == c.hidden && keepResizeBorder == c.keepResizeBorder;
    }

    @Override
    public int hashCode() {
        return (hidden ? 2 : 0) + (keepResizeBorder ? 1 : 0);
    }

    @Override
    public String toString() {
        return "hidden=" + hidden + ", keepResizeBorder=" + keepResizeBorder;
    }
}
