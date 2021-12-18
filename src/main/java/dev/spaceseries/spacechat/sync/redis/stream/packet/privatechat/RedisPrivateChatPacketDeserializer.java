package dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class RedisPrivateChatPacketDeserializer implements JsonDeserializer<RedisPrivateChatPacket> {

    @Override
    public RedisPrivateChatPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject object = json.getAsJsonObject();

        // get sender uuid
        String senderUUIDString = object.get("senderUUID").getAsString();
        // deserialize
        UUID sender = UUID.fromString(senderUUIDString);

        // get sender name
        String senderName = object.get("senderName").getAsString();

        // get sender display name
        String senderDisplayName = object.get("senderDisplayName").getAsString();

        // get target name
        String targetName = object.get("targetName") == null ? null : object.get("targetName").getAsString();

        // get message
        String message = object.get("message") == null ? null : object.get("message").getAsString();

        // get server identifier
        String serverIdentifier = object.get("serverIdentifier").getAsString();
        // get server display name
        String serverDisplayName = object.get("serverDisplayName").getAsString();

        // get component string
        String componentString = object.get("component").getAsString();
        // deserialize
        Component component = GsonComponentSerializer.gson().deserialize(componentString);

        // get ignore bypass
        boolean canBypassIgnore = object.get("canBypassIgnore").getAsBoolean();

        // get chat disable bypass
        boolean canBypassDisabled = object.get("canBypassDisabled").getAsBoolean();

        // return a new message
        return new RedisPrivateChatPacket(sender, senderName, senderDisplayName, targetName, message, serverIdentifier, serverDisplayName, component, canBypassIgnore, canBypassDisabled);
    }
}
