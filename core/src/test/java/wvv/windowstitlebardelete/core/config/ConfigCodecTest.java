package wvv.windowstitlebardelete.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigCodecTest {
    @Test
    void roundTripsAllValues() {
        for (int combo = 0; combo < 4; combo++) {
            Config c = new Config((combo & 1) != 0, (combo & 2) != 0);
            assertEquals(c, ConfigCodec.decode(ConfigCodec.encode(c)));
        }
    }

    @Test
    void writesTheDocumentedKeys() {
        assertEquals("{\n  \"hideOnStartup\": true,\n  \"keepResizeBorder\": false\n}\n",
                ConfigCodec.encode(Config.DEFAULTS.withKeepResizeBorder(false)));
    }

    @Test
    void missingKeysKeepDefaultsAndUnknownKeysAreIgnored() {
        assertEquals(Config.DEFAULTS, ConfigCodec.decode("{}"));
        Config c = ConfigCodec.decode("{ // a comment\n \"keepResizeBorder\": false, \"other\": [1, {\"a\": null}], }");
        assertEquals(new Config(true, false), c);
    }

    @Test
    void rejectsBrokenText() {
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hideOnStartup\": tru"));
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("[true]"));
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hideOnStartup\": \"yes\"}"));
    }
}
