package dev.spaceseries.spacechat.logging.wrap;

import java.util.Date;
import java.util.UUID;

public class LogPrivateChatWrapper extends LogChatWrapper {

    /**
     * The target's name
     */
    private String targetName;

    /**
     * Creates a new log chat wrapper
     *
     * @param logType    The log type
     * @param senderName The sender name
     * @param senderUUID The sender uuid
     * @param message    message
     * @param at         The time
     */
    public LogPrivateChatWrapper(LogType logType, String senderName, UUID senderUUID, String targetName, String message, Date at) {
        super(logType, senderName, senderUUID, message, at);

        this.targetName = targetName;
    }

    /**
     * Returns target name
     *
     * @return target name
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Sets target name
     *
     * @param targetName target name
     */
    public void setTargetNameName(String targetName) {
        this.targetName = targetName;
    }
}
