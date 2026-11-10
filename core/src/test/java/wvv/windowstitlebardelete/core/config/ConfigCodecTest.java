package wvv.windowstitlebardelete.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigCodecTest {
    private static void assertDecodes(boolean hidden, boolean keepResizeBorder, String text) {
        Config c = ConfigCodec.decode(text);
        assertEquals(hidden, c.hidden());
        assertEquals(keepResizeBorder, c.keepResizeBorder());
    }

    @Test
    void roundTripsAllValues() {
        for (int combo = 0; combo < 4; combo++) {
            boolean hidden = (combo & 1) != 0;
            boolean keepResizeBorder = (combo & 2) != 0;
            assertDecodes(hidden, keepResizeBorder, ConfigCodec.encode(new Config(hidden, keepResizeBorder)));
        }
    }

    @Test
    void writesTheDocumentedKeys() {
        assertEquals("{\n  \"hidden\": true,\n  \"keepResizeBorder\": false\n}\n", ConfigCodec.encode(new Config(true, false)));
    }

    @Test
    void missingKeysKeepDefaultsAndOtherTextIsIgnored() {
        assertDecodes(true, true, "{}");
        assertDecodes(true, true, "not JSON");
        assertDecodes(true, false, "{ // a comment\n \"keepResizeBorder\" : false, \"other\": [1, {\"a\": null}], }");
        assertDecodes(false, true, "{\"hidden\":false}");
    }

    @Test
    void rejectsAKeyWithAValueThatIsNotTrueOrFalse() {
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hidden\": tru"));
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hidden\": \"yes\"}"));
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hidden\": true, \"keepResizeBorder\": 1}"));
        assertThrows(IllegalArgumentException.class, () -> ConfigCodec.decode("{\"hidden\" true}"));
    }
}
