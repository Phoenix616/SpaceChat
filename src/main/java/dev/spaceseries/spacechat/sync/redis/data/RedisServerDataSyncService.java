package dev.spaceseries.spacechat.sync.redis.data;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.sync.ServerDataSyncService;
import dev.spaceseries.spacechat.sync.ServerSyncServiceManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class RedisServerDataSyncService extends ServerDataSyncService {

    /**
     * Client
     */
    private final RedisClient client;

    /**
     * Connection
     */
    private StatefulRedisConnection<String, String> connection;

    /**
     * Construct server sync service
     *
     * @param serviceManager service manager
     */
    public RedisServerDataSyncService(SpaceChatPlugin plugin, ServerSyncServiceManager serviceManager) {
        super(plugin, serviceManager);

        // initialize pool
        this.client = this.getServiceManager().getRedisProvider().provide();
        // connect
        this.connection = getConnection();
    }

    private StatefulRedisConnection<String, String> getConnection() {
        if (connection == null || !connection.isOpen()) {
            connection = this.client.connect();
        }
        return connection;
    }

    /**
     * Subscribes a player to a channel
     *
     * @param uuid    uuid
     * @param channel channel
     */
    @Override
    public void subscribeToChannel(UUID uuid, Channel channel) {
        // lpush new channel
        getConnection().async().lpush(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%uuid%", uuid.toString()), channel.getHandle());

        // also lpush to master channels list that contains a list of uuids for every player subscribed to that given channel
        getConnection().async().lpush(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%channel%", channel.getHandle()), uuid.toString());
    }

    /**
     * Unsubscribes a player from a channel
     *
     * @param uuid              uuid
     * @param subscribedChannel subscribed channel
     */
    @Override
    public void unsubscribeFromChannel(UUID uuid, Channel subscribedChannel) {
        // lrem channel
        getConnection().async().lrem(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%uuid%", uuid.toString()), 0, subscribedChannel.getHandle());

        // also lrem from master channels list that contains a list of uuids for every player subscribed to that given channel
        getConnection().async().lrem(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%channel%", subscribedChannel.getHandle()), 0, uuid.toString());
    }

    /**
     * Updates the current channel that a player is talking in
     *
     * @param uuid    uuid
     * @param channel channel
     */
    @Override
    public void updateCurrentChannel(UUID uuid, Channel channel) {
        // update key
        if (channel != null)
            getConnection().async().set(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%uuid%", uuid.toString()), channel.getHandle());
        else {
            // get current
            getCurrentChannel(uuid).thenAccept(current -> {
                if (current != null)
                    getConnection().async().del(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                            .replace("%uuid%", uuid.toString()), current.getHandle());
            });
        }
    }

    /**
     * Gets a list of subscribed channels
     *
     * @param uuid uuid
     * @return channels
     */
    @Override
    public CompletableFuture<List<Channel>> getSubscribedChannels(UUID uuid) {
        return getConnection().async().lrange(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%uuid%", uuid.toString()), 0, -1).thenApply(
                        list -> list.stream()
                                .map(s -> plugin.getChannelManager().get(s, null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                .toCompletableFuture();
    }

    /**
     * Gets the current channel that a player is talking in
     *
     * @param uuid uuid
     * @return channel
     */
    @Override
    public CompletableFuture<Channel> getCurrentChannel(UUID uuid) {
        return getConnection().async().get(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%uuid%", uuid.toString()))
                .thenApply(channel -> plugin.getChannelManager().get(channel, null))
                .toCompletableFuture();
    }

    /**
     * Gets a list of all of the uuids of players who are currently subscribed to a given channel
     * <p>
     * This only includes the uuids of players who are currently online
     *
     * @param channel channel
     * @return uuids
     */
    @Override
    public CompletableFuture<List<UUID>> getSubscribedUUIDs(Channel channel) {
        // get list of uuids of players who've subscribed to a given channel
        return getConnection().async().lrange(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%channel%", channel.getHandle()), 0, -1)
                .thenApply(uuids -> uuids.stream()
                        .map(UUID::fromString)
                        .filter(u -> {
                            Player p = Bukkit.getPlayer(u);
                            return p != null && p.isOnline();
                        })
                        .collect(Collectors.toList()))
                .toCompletableFuture();
    }

    @Override
    public void addPlayer(String username) {
        getConnection().async().set(REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%username%", username.toLowerCase(Locale.ROOT))
                , REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()));

        // also lpush to player list that contains list of all online players
        getConnection().async().lpush(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), username);
    }

    @Override
    public void removePlayer(String username) {
        String key = REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                .replace("%username%", username.toLowerCase(Locale.ROOT));
        getConnection().async().get(key).thenAccept(storedServer -> {
            if (storedServer == null || storedServer.equals(REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()))) {
                getConnection().async().del(key);

                // also lrem from player list that contains list of all online players
                getConnection().async().lrem(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), 0, username);
            }
        });
    }

    @Override
    public void removeAllServerPlayers() {
        Collection<String> players = getPlayers();
        if (players != null) {
            for (String username : players) {
                if (plugin.getServer().getPlayerExact(username) == null) {
                    removePlayer(username);
                }
            }
        }
    }

    @Override
    public CompletableFuture<String> getPlayerServer(String username) {
        try {
            return getConnection().async().get(REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%username%", username.toLowerCase(Locale.ROOT))).toCompletableFuture();
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to get player server for " + username + ". " + e.getClass().getSimpleName() + " " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Collection<String> getPlayers() {
        try {
            return getConnection().sync().lrange(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), 0, -1);
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to get players. " + e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }
}
