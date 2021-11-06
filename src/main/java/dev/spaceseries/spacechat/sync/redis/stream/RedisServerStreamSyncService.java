package dev.spaceseries.spacechat.sync.redis.stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.chat.ChatManager;
import dev.spaceseries.spacechat.config.SpaceChatConfigKeys;
import dev.spaceseries.spacechat.sync.ServerStreamSyncService;
import dev.spaceseries.spacechat.sync.ServerSyncServiceManager;
import dev.spaceseries.spacechat.sync.packet.ReceiveStreamDataPacket;
import dev.spaceseries.spacechat.sync.packet.SendStreamDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisPublishDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisStringReceiveDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.broadcast.RedisBroadcastPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.broadcast.RedisBroadcastPacketDeserializer;
import dev.spaceseries.spacechat.sync.redis.stream.packet.broadcast.RedisBroadcastPacketSerializer;
import dev.spaceseries.spacechat.sync.redis.stream.packet.chat.RedisChatPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.chat.RedisChatPacketDeserializer;
import dev.spaceseries.spacechat.sync.redis.stream.packet.chat.RedisChatPacketSerializer;
import dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat.RedisPrivateChatPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat.RedisPrivateChatPacketDeserializer;
import dev.spaceseries.spacechat.sync.redis.stream.packet.privatechat.RedisPrivateChatPacketSerializer;
import net.kyori.adventure.identity.Identity;
import org.bukkit.entity.Player;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class RedisServerStreamSyncService extends ServerStreamSyncService {

    /**
     * Redis Messenger
     * <p>
     * This class does all of the actual work and connection management
     */
    private RedisMessenger redisMessenger;

    /**
     * Gson
     * <p>
     * Responsible for serializing and deserializing messages
     */
    private Gson gson;

    /**
     * Chat manager
     */
    private final ChatManager chatManager;

    /**
     * Construct server sync service
     *
     * @param plugin         plugin
     * @param serviceManager service manager
     */
    public RedisServerStreamSyncService(SpaceChatPlugin plugin, ServerSyncServiceManager serviceManager) {
        super(plugin, serviceManager);
        this.chatManager = plugin.getChatManager();
    }

    /**
     * Publishes a chat message across the server
     *
     * @param packet packet
     */
    @Override
    public void publishChat(SendStreamDataPacket<?> packet) {
        RedisChatPacket redisChatPacket = (RedisChatPacket) packet;

        // gson-ify the redis message
        String json = gson.toJson(redisChatPacket, RedisChatPacket.class);

        // publish to redis
        redisMessenger.publish(new RedisPublishDataPacket(REDIS_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()), json));
    }

    /**
     * Publishes a chat message across the server
     *
     * @param packet packet
     */
    @Override
    public void publishPrivateChat(SendStreamDataPacket<?> packet) {
        RedisPrivateChatPacket redisChatPacket = (RedisPrivateChatPacket) packet;

        // gson-ify the redis message
        String json = gson.toJson(redisChatPacket, RedisPrivateChatPacket.class);

        // publish to redis
        redisMessenger.publish(new RedisPublishDataPacket(REDIS_PRIVATE_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()), json));
    }

    /**
     * Publishes a broadcast message across the server
     *
     * @param packet packet
     */
    @Override
    public void publishBroadcast(SendStreamDataPacket<?> packet) {
        RedisBroadcastPacket redisBroadcastPacket = (RedisBroadcastPacket) packet;

        // gson-ify the redis message
        String json = gson.toJson(redisBroadcastPacket, RedisBroadcastPacket.class);

        // publish to redis
        redisMessenger.publish(new RedisPublishDataPacket(REDIS_BROADCAST_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()), json));
    }

    /**
     * Receives an incoming chat message
     *
     * @param packet packet
     */
    @Override
    public void receiveChat(ReceiveStreamDataPacket<?> packet) {
        RedisStringReceiveDataPacket redisStringReceiveDataPacket = (RedisStringReceiveDataPacket) packet;
        // deserialize
        RedisChatPacket chatPacket = gson.fromJson(redisStringReceiveDataPacket.getData(), RedisChatPacket.class);

        // if the message is from ourselves, then return
        if (chatPacket.getServerIdentifier().equalsIgnoreCase(REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()))) {
            return;
        }

        // if channel exists, send through that instead
        if (chatPacket.getChannel() != null && plugin.getChannelManager().get(chatPacket.getChannel().getHandle()) != null) {
            chatManager.sendComponentChannelMessage(chatPacket.getSender(), chatPacket.getComponent(), chatPacket.getChannel());
            return;
        }

        // send to all players
        chatManager.sendComponentChatMessage(chatPacket.getSender(), chatPacket.getComponent());
    }

    /**
     * Receives an incoming private chat message
     *
     * @param packet packet
     */
    @Override
    public void receivePrivateChat(ReceiveStreamDataPacket<?> packet) {
        RedisStringReceiveDataPacket redisStringReceiveDataPacket = (RedisStringReceiveDataPacket) packet;
        // deserialize
        RedisPrivateChatPacket chatPacket = gson.fromJson(redisStringReceiveDataPacket.getData(), RedisPrivateChatPacket.class);

        // if the message is from ourselves, then return
        if (chatPacket.getServerIdentifier().equalsIgnoreCase(REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()))) {
            return;
        }

        // if player is online send it to them
        Player to = plugin.getServer().getPlayer(chatPacket.getTargetName());
        if (to != null) {
            if (chatPacket.getSender().equals(Identity.nil().uuid())) {
                // message is error sent by plugin
                chatManager.sendComponentMessage(Identity.nil(), chatPacket.getComponent(), to);
            } else {
                plugin.getUserManager().use(to.getUniqueId(), user -> {
                    // Check for ignored
                    if (user.isIgnored(chatPacket.getSender())) {
                        publishPrivateChat(new RedisPrivateChatPacket(Identity.nil().uuid(), "", chatPacket.getSenderName(), SpaceChatConfigKeys.REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()), SpaceChatConfigKeys.REDIS_SERVER_DISPLAYNAME.get(plugin.getSpaceChatConfig().getAdapter()), Messages.getInstance(plugin).pmIgnoredByTarget.compile("%user%", to.getName())));
                    } else {
                        chatManager.sendComponentMessage(Identity.identity(chatPacket.getSender()), chatPacket.getComponent(), to);
                        user.setLastMessaged(chatPacket.getSenderName());
                    }
                });
            }
        }
    }

    /**
     * Receives an incoming chat message
     *
     * @param packet packet
     */
    @Override
    public void receiveBroadcast(ReceiveStreamDataPacket<?> packet) {
        RedisStringReceiveDataPacket stringReceiveDataPacket = (RedisStringReceiveDataPacket) packet;

        // deserialize
        RedisBroadcastPacket broadcastPacket = gson.fromJson(stringReceiveDataPacket.getData(), RedisBroadcastPacket.class);

        // if the message is from ourselves, then return
        if (broadcastPacket.getServerIdentifier().equalsIgnoreCase(REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()))) {
            return;
        }

        // send to all players
        chatManager.sendComponentMessage(broadcastPacket.getComponent());
    }

    /**
     * Starts the service in question
     */
    @Override
    public void start() {
        // initialize messenger
        this.redisMessenger = new RedisMessenger(plugin, this);
        // initialize my super-duper gson adapter
        gson = new GsonBuilder()
                .registerTypeAdapter(RedisChatPacket.class, new RedisChatPacketSerializer())
                .registerTypeAdapter(RedisChatPacket.class, new RedisChatPacketDeserializer(plugin))
                .registerTypeAdapter(RedisPrivateChatPacket.class, new RedisPrivateChatPacketSerializer())
                .registerTypeAdapter(RedisPrivateChatPacket.class, new RedisPrivateChatPacketDeserializer())
                .registerTypeAdapter(RedisBroadcastPacket.class, new RedisBroadcastPacketSerializer())
                .registerTypeAdapter(RedisBroadcastPacket.class, new RedisBroadcastPacketDeserializer())
                .create();
    }

    /**
     * Ends the service in question
     */
    @Override
    public void end() {
        this.redisMessenger.shutdown();
    }
}
