package dev.spaceseries.spacechat.sync.redis.stream;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.sync.packet.StreamDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisPublishDataPacket;
import dev.spaceseries.spacechat.sync.redis.stream.packet.RedisStringReceiveDataPacket;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.logging.Level;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class RedisMessenger implements RedisPubSubListener<String, String> {

    /**
     * Sync service
     */
    private final RedisServerStreamSyncService syncService;

    /**
     * Client
     */
    private final RedisClient client;

    /**
     * Subscription Connection
     */
    private final StatefulRedisPubSubConnection<String, String> subConnection;

    /**
     * Publish Connection
     */
    private StatefulRedisConnection<String, String> pubConnection;

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
        this.client = syncService.getServiceManager().getRedisProvider().provide();

        // create subscription connection
        subConnection = client.connectPubSub();

        // add us as listener
        subConnection.addListener(this);

        // subscribe to redis pub/sub
        subConnection.async().subscribe(
                REDIS_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()),
                REDIS_PRIVATE_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()),
                REDIS_BROADCAST_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter())
        );

        // create publish connection
        pubConnection = client.connect();
    }

    /**
     * Shuts down the client
     */
    public void shutdown() {
        if (pubConnection.isOpen()) {
            // unsubscribe from chat channel
            subConnection.sync().unsubscribe(
                    REDIS_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()),
                    REDIS_PRIVATE_CHAT_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter()),
                    REDIS_BROADCAST_CHANNEL.get(this.plugin.getSpaceChatConfig().getAdapter())
            );
            // remove listener
            subConnection.removeListener(this);
            // close connection
            subConnection.close();
        }
        if (pubConnection.isOpen()) {
            // close connection
            pubConnection.close();
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {
        message(channel, message);
    }

    @Override
    public void message(String channel, String message) {
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
    public void psubscribed(String pattern, long count) {
        subscribed(pattern, count);
    }

    @Override
    public void subscribed(String channel, long subscribedChannels) {
        // we have subscribed to [channel]. We are currently subscribed to [subscribedChannels] channels.
        plugin.getLogger().log(Level.INFO, "SpaceChat subscribed to the redis channel '" + channel + "'");
    }

    @Override
    public void punsubscribed(String unsubscribed, long count) {
        unsubscribed(unsubscribed, count);
    }

    @Override
    public void unsubscribed(String channel, long subscribedChannels) {
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
        if (pubConnection == null || !pubConnection.isOpen()) {
            pubConnection = client.connect();
        }
        pubConnection.async().publish(channel, message);
    }
}
