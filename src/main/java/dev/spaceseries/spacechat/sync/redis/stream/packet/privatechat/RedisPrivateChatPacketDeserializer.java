package dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;
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

        // get target name
        String targetName = object.get("targetName") == null ? null : object.get("targetName").getAsString();

        // get server identifier
        String serverIdentifier = object.get("serverIdentifier").getAsString();
        // get server display name
        String serverDisplayName = object.get("serverDisplayName").getAsString();

        // get component string
        String componentString = object.get("component").getAsString();
        // deserialize
        Component component = GsonComponentSerializer.gson().deserialize(componentString);

        // return a new message
        return new RedisPrivateChatPacket(sender, senderName, targetName, serverIdentifier, serverDisplayName, component);
    }
}
