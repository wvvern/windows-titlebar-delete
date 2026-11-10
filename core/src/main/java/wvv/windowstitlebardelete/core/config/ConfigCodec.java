package wvv.windowstitlebardelete.core.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Converts {@link Config} to and from the text of the config file, where a key that is not in the text keeps its default. */
public final class ConfigCodec {
    private static final String HIDDEN = "hidden";
    private static final String KEEP_RESIZE_BORDER = "keepResizeBorder";
    private static final Pattern HIDDEN_VALUE = value(HIDDEN);
    private static final Pattern KEEP_RESIZE_BORDER_VALUE = value(KEEP_RESIZE_BORDER);

    private ConfigCodec() {}

    public static String encode(Config config) {
        return "{\n  \"" + HIDDEN + "\": " + config.hidden()
                + ",\n  \"" + KEEP_RESIZE_BORDER + "\": " + config.keepResizeBorder() + "\n}\n";
    }

    /** @throws IllegalArgumentException when a key is in the text with a value that is not true or false */
    public static Config decode(String text) {
        Config d = Config.DEFAULTS;
        return new Config(bool(HIDDEN_VALUE, text, d.hidden()), bool(KEEP_RESIZE_BORDER_VALUE, text, d.keepResizeBorder()));
    }

    private static Pattern value(String key) {
        return Pattern.compile('"' + key + "\"(?:\\s*:\\s*(true|false)\\b)?");
    }

    private static boolean bool(Pattern value, String text, boolean fallback) {
        Matcher m = value.matcher(text);
        if (!m.find()) return fallback;
        if (m.group(1) == null) throw new IllegalArgumentException(m.group() + " must be true or false");
        return Boolean.parseBoolean(m.group(1));
    }
}
