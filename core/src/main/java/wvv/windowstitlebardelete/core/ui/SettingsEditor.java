package wvv.windowstitlebardelete.core.ui;

import wvv.windowstitlebardelete.core.Titlebar;
import wvv.windowstitlebardelete.core.config.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * The settings screen of all versions, which the screen class of each adapter draws each frame and gives the clicks
 * to; a switch click saves the config at once through {@link Titlebar#apply}.
 */
public final class SettingsEditor {
    static final String HIDE_ON_STARTUP = "Hide the titlebar at startup";
    static final String KEEP_RESIZE_BORDER = "Keep the resize border";
    static final String DONE = "Done";

    private static final int SCRIM = 0x99000000;
    private static final int BORDER = 0xFF323944;
    private static final int PANEL = 0xF2181C22;
    private static final int HOVER = 0xFF2A303A;
    private static final int TEXT = 0xFFE9ECF1;
    private static final int MUTED = 0xFF9AA3B3;
    private static final int ACCENT = 0xFF4D8EFF;
    private static final int OFF = 0xFF59606C;
    private static final int PAD = 10;
    private static final int GAP = 8;
    private static final int ROW_H = 14;
    private static final int LINE_H = 10;
    private static final int SWITCH_W = 20;
    private static final int SWITCH_H = 10;
    private static final int BUTTON_W = 80;
    private static final int BUTTON_H = 18;
    /** Space between a label and its switch. */
    private static final int LABEL_GAP = 16;

    /** The editor that is open now, which the harness clicks. */
    private static volatile SettingsEditor current;

    private final String[] hint;
    /** The controls that the last frame drew, which clicks hit. */
    private final List<Control> controls = new ArrayList<Control>();
    private boolean done;

    /** @param keyLabel the name of the key that the toggle key binding has now, for example "F12" */
    public SettingsEditor(String keyLabel) {
        hint = new String[]{"Push " + keyLabel + " to show or hide the titlebar.", "Change the key in Controls."};
        current = this;
    }

    /** The editor of the open settings screen, or null. */
    public static SettingsEditor current() { return current; }

    /** True after a click on Done, which tells the adapter to close the screen. */
    public boolean done() { return done; }

    /** Call when the screen closes for any reason. */
    public void close() {
        if (current == this) current = null;
    }

    /** Draws the screen centered on the canvas, with the pointer at mouseX, mouseY for the hover colors. */
    public void render(Canvas c, int mouseX, int mouseY) {
        Config config = Titlebar.config();
        String oneLine = hint[0] + " " + hint[1];
        int switchW = Math.max(c.textWidth(HIDE_ON_STARTUP), c.textWidth(KEEP_RESIZE_BORDER)) + LABEL_GAP + SWITCH_W;
        int innerW = Math.max(Math.max(switchW, c.textWidth(Titlebar.MOD_NAME)), Math.max(c.textWidth(hint[0]), c.textWidth(hint[1])));
        boolean wide = c.textWidth(oneLine) <= c.width() - 2 * PAD - 8;
        if (wide) innerW = Math.max(innerW, c.textWidth(oneLine));
        int hintLines = wide ? 1 : 2;
        int w = innerW + 2 * PAD;
        int h = PAD + LINE_H + GAP + 2 * ROW_H + GAP + hintLines * LINE_H + GAP + BUTTON_H + PAD;
        int x = (c.width() - w) / 2;
        int y = (c.height() - h) / 2;

        controls.clear();
        c.fill(0, 0, c.width(), c.height(), SCRIM);
        c.fill(x, y, w, h, BORDER);
        c.fill(x + 1, y + 1, w - 2, h - 2, PANEL);
        int ty = y + PAD;
        centered(c, Titlebar.MOD_NAME, x, w, ty, TEXT);
        ty += LINE_H + GAP;
        switchRow(c, x + PAD, ty, innerW, HIDE_ON_STARTUP, config.hideOnStartup(), mouseX, mouseY);
        ty += ROW_H;
        switchRow(c, x + PAD, ty, innerW, KEEP_RESIZE_BORDER, config.keepResizeBorder(), mouseX, mouseY);
        ty += ROW_H + GAP;
        if (wide) centered(c, oneLine, x, w, ty, MUTED);
        else {
            centered(c, hint[0], x, w, ty, MUTED);
            centered(c, hint[1], x, w, ty + LINE_H, MUTED);
        }
        ty += hintLines * LINE_H + GAP;
        Control button = add(DONE, x + (w - BUTTON_W) / 2, ty, BUTTON_W, BUTTON_H);
        c.fill(button.x, button.y, button.w, button.h, BORDER);
        c.fill(button.x + 1, button.y + 1, button.w - 2, button.h - 2, button.contains(mouseX, mouseY) ? HOVER : PANEL);
        centered(c, DONE, button.x, button.w, button.y + (BUTTON_H - 8) / 2, TEXT);
    }

    /** A label at the left and a switch at the right, where a click on any part of the row flips the switch. */
    private void switchRow(Canvas c, int x, int y, int w, String label, boolean on, int mouseX, int mouseY) {
        Control row = add(label, x, y, w, ROW_H);
        if (row.contains(mouseX, mouseY)) c.fill(x - 4, y, w + 8, ROW_H, HOVER);
        c.text(label, x, y + (ROW_H - 8) / 2, TEXT);
        int sx = x + w - SWITCH_W, sy = y + (ROW_H - SWITCH_H) / 2;
        c.fill(sx, sy, SWITCH_W, SWITCH_H, on ? ACCENT : OFF);
        c.fill(on ? sx + SWITCH_W - SWITCH_H + 1 : sx + 1, sy + 1, SWITCH_H - 2, SWITCH_H - 2, TEXT);
    }

    private static void centered(Canvas c, String text, int x, int w, int y, int argb) {
        c.text(text, x + (w - c.textWidth(text)) / 2, y, argb);
    }

    private Control add(String label, int x, int y, int w, int h) {
        Control control = new Control(label, x, y, w, h);
        controls.add(control);
        return control;
    }

    /** Gives a click of the left mouse button at a GUI position to the control that the last frame drew there. */
    public void mouseClicked(int x, int y) {
        for (Control control : controls) {
            if (control.contains(x, y)) {
                activate(control.label);
                return;
            }
        }
    }

    /** Clicks the middle of the control with this label, as a mouse does, and returns false when the last frame has no such control. */
    public boolean click(String label) {
        for (Control control : controls) {
            if (control.label.equalsIgnoreCase(label.trim())) {
                mouseClicked(control.x + control.w / 2, control.y + control.h / 2);
                return true;
            }
        }
        return false;
    }

    private void activate(String label) {
        Config config = Titlebar.config();
        if (label.equals(HIDE_ON_STARTUP)) Titlebar.apply(config.withHideOnStartup(!config.hideOnStartup()));
        else if (label.equals(KEEP_RESIZE_BORDER)) Titlebar.apply(config.withKeepResizeBorder(!config.keepResizeBorder()));
        else if (label.equals(DONE)) done = true;
    }

    /** A place on the screen that a click can hit. */
    private static final class Control {
        final String label;
        final int x, y, w, h;

        Control(String label, int x, int y, int w, int h) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean contains(int px, int py) {
            return px >= x && py >= y && px < x + w && py < y + h;
        }
    }
}
