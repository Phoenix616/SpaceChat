package dev.spaceseries.spacechat.model;

import java.util.Locale;

public enum ChatType {
    PUBLIC,
    PRIVATE;

    /**
     * Try to parse incomplete input strings
     *
     * @param chatType the input string
     * @return the type or null
     */
    public static ChatType parse(String chatType) {
        chatType = chatType.toUpperCase(Locale.ROOT).replace(' ', '_');
        try {
            return valueOf(chatType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
