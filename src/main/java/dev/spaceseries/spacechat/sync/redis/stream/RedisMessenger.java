package dev.spaceseries.spacechat.sync.redis.stream;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.sync.packet.StreamDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisPublishDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisStringReceiveDataPacket;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class RedisMessenger extends JedisPubSub {

    /**
     * Sync service
     */
    private final RedisServerStreamSyncService syncService;

    /**
     * Pool
     */
    private final JedisPool pool;

    /**
     * Plugin
     */
    private final SpaceChatPlugin plugin;

    /**
     * Construct redis connector
     */
    public RedisMessenger(SpaceChatPlugin plugin, RedisServerStreamSyncService syncService) {
        this.plugin = plugin;
        this.syncService = syncService;

        // initialize pool
        this.pool = syncService.getServiceManager().getRedisProvider().provide();

        // subscribing to redis pub/sub is a blocking operation.
        // we need to make a new thread in order to not block the main thread....
        new Thread(() -> {
            // subscribe this class to chat channel
            pool.getResource().subscribe(this, REDIS_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()));
        }).start();

        // create a separate thread for private chat packets
        new Thread(() -> {
            // subscribe this class to chat channel
            pool.getResource().subscribe(this, REDIS_PRIVATE_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()));
        }).start();

        // create a separate thread for broadcast packets
        new Thread(() -> {
            // subscribe this class to chat channel
            pool.getResource().subscribe(this, REDIS_BROADCAST_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()));
        }).start();
    }

    /**
     * Shuts down the client
     */
    public void shutdown() {
        if (this.pool != null && this.pool.getResource().getClient() != null) {
            // unsubscribe from chat channel
            unsubscribe(REDIS_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()));
            unsubscribe(REDIS_PRIVATE_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()));
            unsubscribe(REDIS_BROADCAST_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter()));

            pool.close();
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        // receiving
        // [channel] sent [message]

        // if it's the correct channel
        if (channel.equalsIgnoreCase(REDIS_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter())))
            this.syncService.receiveChat(new RedisStringReceiveDataPacket(message));
        else if (channel.equalsIgnoreCase(REDIS_PRIVATE_CHAT_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter())))
            this.syncService.receivePrivateChat(new RedisStringReceiveDataPacket(message));
        else if (channel.equalsIgnoreCase(REDIS_BROADCAST_CHANNEL.get(plugin.getSpaceChatConfig().getAdapter())))
            this.syncService.receiveBroadcast(new RedisStringReceiveDataPacket(message));
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        // we have subscribed to [channel]. We are currently subscribed to [subscribedChannels] channels.
        plugin.getLogger().log(Level.INFO, "SpaceChat subscribed to the redis channel '" + channel + "'");
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        // we have unsubscribed from [channel]. We are currently subscribed to another [subscribedChannels] channels.
        plugin.getLogger().log(Level.INFO, "SpaceChat unsubscribed from the redis channel '" + channel + "'");
    }

    /**
     * Publish a message
     *
     * @param dataPacket packet
     */
    public void publish(StreamDataPacket dataPacket) {
        RedisPublishDataPacket redisPublishDataPacket = (RedisPublishDataPacket) dataPacket;

        String channel = redisPublishDataPacket.getChannel();
        String message = redisPublishDataPacket.getMessage();
        // run async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.publish(channel, message);
            }
        });
    }
}
