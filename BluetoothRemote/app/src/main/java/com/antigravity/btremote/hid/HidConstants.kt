package com.antigravity.btremote.hid

object HidConstants {
    const val REPORT_ID_MOUSE = 1
    const val REPORT_ID_KEYBOARD = 2
    const val REPORT_ID_CONSUMER = 3

    // Modifier masks
    const val MOD_NONE: Byte = 0x00
    const val MOD_LCTRL: Byte = 0x01
    const val MOD_LSHIFT: Byte = 0x02
    const val MOD_LALT: Byte = 0x04
    const val MOD_LGUI: Byte = 0x08 // Windows key / Command key
    const val MOD_RCTRL: Byte = 0x10
    const val MOD_RSHIFT: Byte = 0x20
    const val MOD_RALT: Byte = 0x40
    const val MOD_RGUI: Byte = 0x80.toByte()

    // Mouse button bitmasks
    const val MOUSE_BTN_NONE: Byte = 0x00
    const val MOUSE_BTN_LEFT: Byte = 0x01
    const val MOUSE_BTN_RIGHT: Byte = 0x02
    const val MOUSE_BTN_MIDDLE: Byte = 0x04

    // Special keyboard keys
    const val KEY_NONE: Byte = 0x00
    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESC: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C
    const val KEY_CAPS_LOCK: Byte = 0x39
    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C
    const val KEY_F4: Byte = 0x3D
    const val KEY_F5: Byte = 0x3E
    const val KEY_F6: Byte = 0x3F
    const val KEY_F7: Byte = 0x40
    const val KEY_F8: Byte = 0x41
    const val KEY_F9: Byte = 0x42
    const val KEY_F10: Byte = 0x43
    const val KEY_F11: Byte = 0x44
    const val KEY_F12: Byte = 0x45
    const val KEY_PRINT_SCREEN: Byte = 0x46
    const val KEY_SCROLL_LOCK: Byte = 0x47
    const val KEY_PAUSE: Byte = 0x48
    const val KEY_INSERT: Byte = 0x49
    const val KEY_HOME: Byte = 0x4A
    const val KEY_PAGE_UP: Byte = 0x4B
    const val KEY_DELETE: Byte = 0x4C
    const val KEY_END: Byte = 0x4D
    const val KEY_PAGE_DOWN: Byte = 0x4E
    const val KEY_RIGHT: Byte = 0x4F
    const val KEY_LEFT: Byte = 0x50
    const val KEY_DOWN: Byte = 0x51
    const val KEY_UP: Byte = 0x52

    // Numpad keys
    const val KEY_NUM_LOCK: Byte = 0x53
    const val KEY_KP_DIVIDE: Byte = 0x54
    const val KEY_KP_MULTIPLY: Byte = 0x55
    const val KEY_KP_MINUS: Byte = 0x56
    const val KEY_KP_PLUS: Byte = 0x57
    const val KEY_KP_ENTER: Byte = 0x58
    const val KEY_KP_1: Byte = 0x59
    const val KEY_KP_2: Byte = 0x5A
    const val KEY_KP_3: Byte = 0x5B
    const val KEY_KP_4: Byte = 0x5C
    const val KEY_KP_5: Byte = 0x5D
    const val KEY_KP_6: Byte = 0x5E
    const val KEY_KP_7: Byte = 0x5F
    const val KEY_KP_8: Byte = 0x60
    const val KEY_KP_9: Byte = 0x61
    const val KEY_KP_0: Byte = 0x62
    const val KEY_KP_DOT: Byte = 0x63
    const val KEY_MUTE: Byte = 0x7F
    const val KEY_VOLUME_UP: Byte = 0x80.toByte()
    const val KEY_VOLUME_DOWN: Byte = 0x81.toByte()
    const val KEY_LEFT_GUI: Byte = 0xE3.toByte()

    // Consumer usage codes (16-bit)
    const val CONSUMER_PLAY_PAUSE: Short = 0x00CD
    const val CONSUMER_SCAN_NEXT: Short = 0x00B5
    const val CONSUMER_SCAN_PREV: Short = 0x00B6
    const val CONSUMER_STOP: Short = 0x00B7
    const val CONSUMER_VOLUME_UP: Short = 0x00E9
    const val CONSUMER_VOLUME_DOWN: Short = 0x00EA
    const val CONSUMER_MUTE: Short = 0x00E2
    const val CONSUMER_CALCULATOR: Short = 0x0192
    const val CONSUMER_BROWSER_HOME: Short = 0x0223
    const val CONSUMER_AC_HOME: Short = 0x0223
    const val CONSUMER_AC_BACK: Short = 0x0224
    const val CONSUMER_AC_SEARCH: Short = 0x0221
    const val CONSUMER_BRIGHTNESS_UP: Short = 0x006F
    const val CONSUMER_BRIGHTNESS_DOWN: Short = 0x0070
    const val CONSUMER_VOICE_COMMAND: Short = 0x00CF
    const val CONSUMER_POWER: Short = 0x0030
    const val CONSUMER_MENU: Short = 0x0040

    val HID_REPORT_DESCRIPTOR = byteArrayOf(
        // Mouse Report (Report ID 1)
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x02.toByte(), // USAGE (Mouse)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x01.toByte(), //   REPORT_ID (1)
        0x09.toByte(), 0x01.toByte(), //   USAGE (Pointer)
        0xa1.toByte(), 0x00.toByte(), //   COLLECTION (Physical)
        0x05.toByte(), 0x09.toByte(), //     USAGE_PAGE (Button)
        0x19.toByte(), 0x01.toByte(), //     USAGE_MINIMUM (Button 1)
        0x29.toByte(), 0x05.toByte(), //     USAGE_MAXIMUM (Button 5)
        0x15.toByte(), 0x00.toByte(), //     LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //     LOGICAL_MAXIMUM (1)
        0x95.toByte(), 0x05.toByte(), //     REPORT_COUNT (5)
        0x75.toByte(), 0x01.toByte(), //     REPORT_SIZE (1)
        0x81.toByte(), 0x02.toByte(), //     INPUT (Data,Var,Abs)
        0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
        0x75.toByte(), 0x03.toByte(), //     REPORT_SIZE (3)
        0x81.toByte(), 0x03.toByte(), //     INPUT (Cnst,Var,Abs) -- 3-bit padding
        0x05.toByte(), 0x01.toByte(), //     USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x30.toByte(), //     USAGE (X)
        0x09.toByte(), 0x31.toByte(), //     USAGE (Y)
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7f.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x02.toByte(), //     REPORT_COUNT (2)
        0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)
        0x09.toByte(), 0x38.toByte(), //     USAGE (Wheel)
        0x15.toByte(), 0x81.toByte(), //     LOGICAL_MINIMUM (-127)
        0x25.toByte(), 0x7f.toByte(), //     LOGICAL_MAXIMUM (127)
        0x75.toByte(), 0x08.toByte(), //     REPORT_SIZE (8)
        0x95.toByte(), 0x01.toByte(), //     REPORT_COUNT (1)
        0x81.toByte(), 0x06.toByte(), //     INPUT (Data,Var,Rel)
        0xc0.toByte(),                 //   END_COLLECTION
        0xc0.toByte(),                 // END_COLLECTION

        // Keyboard Report (Report ID 2)
        0x05.toByte(), 0x01.toByte(), // USAGE_PAGE (Generic Desktop)
        0x09.toByte(), 0x06.toByte(), // USAGE (Keyboard)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x02.toByte(), //   REPORT_ID (2)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0xe0.toByte(), //   USAGE_MINIMUM (Keyboard LeftControl)
        0x29.toByte(), 0xe7.toByte(), //   USAGE_MAXIMUM (Keyboard Right GUI)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x25.toByte(), 0x01.toByte(), //   LOGICAL_MAXIMUM (1)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x95.toByte(), 0x08.toByte(), //   REPORT_COUNT (8)
        0x81.toByte(), 0x02.toByte(), //   INPUT (Data,Var,Abs) -- Modifiers byte
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x81.toByte(), 0x03.toByte(), //   INPUT (Cnst,Var,Abs) -- Reserved byte
        0x95.toByte(), 0x05.toByte(), //   REPORT_COUNT (5)
        0x75.toByte(), 0x01.toByte(), //   REPORT_SIZE (1)
        0x05.toByte(), 0x08.toByte(), //   USAGE_PAGE (LEDs)
        0x19.toByte(), 0x01.toByte(), //   USAGE_MINIMUM (Num Lock)
        0x29.toByte(), 0x05.toByte(), //   USAGE_MAXIMUM (Kana)
        0x91.toByte(), 0x02.toByte(), //   OUTPUT (Data,Var,Abs)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x75.toByte(), 0x03.toByte(), //   REPORT_SIZE (3)
        0x91.toByte(), 0x03.toByte(), //   OUTPUT (Cnst,Var,Abs) -- LED padding
        0x95.toByte(), 0x06.toByte(), //   REPORT_COUNT (6)
        0x75.toByte(), 0x08.toByte(), //   REPORT_SIZE (8)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xff.toByte(), 0x00.toByte(), // LOGICAL_MAXIMUM (255)
        0x05.toByte(), 0x07.toByte(), //   USAGE_PAGE (Keyboard/Keypad)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (Reserved)
        0x2a.toByte(), 0xff.toByte(), 0x00.toByte(), // USAGE_MAXIMUM (255)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs) -- 6 key codes
        0xc0.toByte(),                 // END_COLLECTION

        // Consumer Control Report (Report ID 3)
        0x05.toByte(), 0x0c.toByte(), // USAGE_PAGE (Consumer Devices)
        0x09.toByte(), 0x01.toByte(), // USAGE (Consumer Control)
        0xa1.toByte(), 0x01.toByte(), // COLLECTION (Application)
        0x85.toByte(), 0x03.toByte(), //   REPORT_ID (3)
        0x15.toByte(), 0x00.toByte(), //   LOGICAL_MINIMUM (0)
        0x26.toByte(), 0xff.toByte(), 0x03.toByte(), // LOGICAL_MAXIMUM (0x03FF)
        0x19.toByte(), 0x00.toByte(), //   USAGE_MINIMUM (0)
        0x2a.toByte(), 0xff.toByte(), 0x03.toByte(), // USAGE_MAXIMUM (0x03FF)
        0x75.toByte(), 0x10.toByte(), //   REPORT_SIZE (16)
        0x95.toByte(), 0x01.toByte(), //   REPORT_COUNT (1)
        0x81.toByte(), 0x00.toByte(), //   INPUT (Data,Ary,Abs)
        0xc0.toByte()                  // END_COLLECTION
    )

    fun charToHid(c: Char): Pair<Byte, Byte>? {
        return when (c) {
            in 'a'..'z' -> Pair(MOD_NONE, (0x04 + (c - 'a')).toByte())
            in 'A'..'Z' -> Pair(MOD_LSHIFT, (0x04 + (c - 'A')).toByte())
            '1' -> Pair(MOD_NONE, 0x1E.toByte())
            '!' -> Pair(MOD_LSHIFT, 0x1E.toByte())
            '2' -> Pair(MOD_NONE, 0x1F.toByte())
            '@' -> Pair(MOD_LSHIFT, 0x1F.toByte())
            '3' -> Pair(MOD_NONE, 0x20.toByte())
            '#' -> Pair(MOD_LSHIFT, 0x20.toByte())
            '4' -> Pair(MOD_NONE, 0x21.toByte())
            '$' -> Pair(MOD_LSHIFT, 0x21.toByte())
            '5' -> Pair(MOD_NONE, 0x22.toByte())
            '%' -> Pair(MOD_LSHIFT, 0x22.toByte())
            '6' -> Pair(MOD_NONE, 0x23.toByte())
            '^' -> Pair(MOD_LSHIFT, 0x23.toByte())
            '7' -> Pair(MOD_NONE, 0x24.toByte())
            '&' -> Pair(MOD_LSHIFT, 0x24.toByte())
            '8' -> Pair(MOD_NONE, 0x25.toByte())
            '*' -> Pair(MOD_LSHIFT, 0x25.toByte())
            '9' -> Pair(MOD_NONE, 0x26.toByte())
            '(' -> Pair(MOD_LSHIFT, 0x26.toByte())
            '0' -> Pair(MOD_NONE, 0x27.toByte())
            ')' -> Pair(MOD_LSHIFT, 0x27.toByte())
            '\n' -> Pair(MOD_NONE, KEY_ENTER)
            '\b' -> Pair(MOD_NONE, KEY_BACKSPACE)
            '\t' -> Pair(MOD_NONE, KEY_TAB)
            ' ' -> Pair(MOD_NONE, KEY_SPACE)
            '-' -> Pair(MOD_NONE, 0x2D.toByte())
            '_' -> Pair(MOD_LSHIFT, 0x2D.toByte())
            '=' -> Pair(MOD_NONE, 0x2E.toByte())
            '+' -> Pair(MOD_LSHIFT, 0x2E.toByte())
            '[' -> Pair(MOD_NONE, 0x2F.toByte())
            '{' -> Pair(MOD_LSHIFT, 0x2F.toByte())
            ']' -> Pair(MOD_NONE, 0x30.toByte())
            '}' -> Pair(MOD_LSHIFT, 0x30.toByte())
            '\\' -> Pair(MOD_NONE, 0x31.toByte())
            '|' -> Pair(MOD_LSHIFT, 0x31.toByte())
            ';' -> Pair(MOD_NONE, 0x33.toByte())
            ':' -> Pair(MOD_LSHIFT, 0x33.toByte())
            '\'' -> Pair(MOD_NONE, 0x34.toByte())
            '"' -> Pair(MOD_LSHIFT, 0x34.toByte())
            '`' -> Pair(MOD_NONE, 0x35.toByte())
            '~' -> Pair(MOD_LSHIFT, 0x35.toByte())
            ',' -> Pair(MOD_NONE, 0x36.toByte())
            '<' -> Pair(MOD_LSHIFT, 0x36.toByte())
            '.' -> Pair(MOD_NONE, 0x37.toByte())
            '>' -> Pair(MOD_LSHIFT, 0x37.toByte())
            '/' -> Pair(MOD_NONE, 0x38.toByte())
            '?' -> Pair(MOD_LSHIFT, 0x38.toByte())
            else -> null
        }
    }
}
