package wvv.windowstitlebardelete.core.config;

import java.util.Map;

/**
 * Converts {@link Config} to and from JSON such as {@code {"hidden": true, "keepResizeBorder": true}}, where a
 * missing key keeps its default and an unknown key is ignored.
 */
public final class ConfigCodec {
    private static final String HIDDEN = "hidden";
    private static final String KEEP_RESIZE_BORDER = "keepResizeBorder";

    private ConfigCodec() {}

    public static String encode(Config config) {
        return "{\n  \"" + HIDDEN + "\": " + config.hidden()
                + ",\n  \"" + KEEP_RESIZE_BORDER + "\": " + config.keepResizeBorder() + "\n}\n";
    }

    /** @throws IllegalArgumentException when the text is not a JSON object or a value is not true or false */
    public static Config decode(String json) {
        Object parsed = Json.parse(json);
        if (!(parsed instanceof Map)) throw new IllegalArgumentException("the config is not a JSON object");
        Map<?, ?> root = (Map<?, ?>) parsed;
        Config d = Config.DEFAULTS;
        return new Config(bool(root, HIDDEN, d.hidden()), bool(root, KEEP_RESIZE_BORDER, d.keepResizeBorder()));
    }

    private static boolean bool(Map<?, ?> root, String key, boolean fallback) {
        if (!root.containsKey(key)) return fallback;
        Object value = root.get(key);
        if (!(value instanceof Boolean)) throw new IllegalArgumentException(key + " must be true or false");
        return (Boolean) value;
    }
}
