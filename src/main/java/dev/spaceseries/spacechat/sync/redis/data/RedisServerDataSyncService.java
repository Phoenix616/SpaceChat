package dev.spaceseries.spacechat.sync.redis.data;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.sync.ServerDataSyncService;
import dev.spaceseries.spacechat.sync.ServerSyncServiceManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.spaceseries.spacechat.config.SpaceChatConfigKeys.*;

public class RedisServerDataSyncService extends ServerDataSyncService {

    /**
     * Pool
     */
    private final JedisPool pool;

    /**
     * Construct server sync service
     *
     * @param serviceManager service manager
     */
    public RedisServerDataSyncService(SpaceChatPlugin plugin, ServerSyncServiceManager serviceManager) {
        super(plugin, serviceManager);

        // initialize pool
        this.pool = this.getServiceManager().getRedisProvider().provide();
    }

    /**
     * Subscribes a player to a channel
     *
     * @param uuid    uuid
     * @param channel channel
     */
    @Override
    public void subscribeToChannel(UUID uuid, Channel channel) {
        try (Jedis jedis = pool.getResource()) {
            // update

            // lpush new channel
            jedis.lpush(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%uuid%", uuid.toString()), channel.getHandle());

            // also lpush to master channels list that contains a list of uuids for every player subscribed to that given channel
            jedis.lpush(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%channel%", channel.getHandle()), uuid.toString());
        }
    }

    /**
     * Unsubscribes a player from a channel
     *
     * @param uuid              uuid
     * @param subscribedChannel subscribed channel
     */
    @Override
    public void unsubscribeFromChannel(UUID uuid, Channel subscribedChannel) {
        try (Jedis jedis = pool.getResource()) {
            // update

            // lrem channel
            jedis.lrem(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%uuid%", uuid.toString()), 0, subscribedChannel.getHandle());

            // also lrem from master channels list that contains a list of uuids for every player subscribed to that given channel
            jedis.lrem(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%channel%", subscribedChannel.getHandle()), 0, uuid.toString());
        }
    }

    /**
     * Updates the current channel that a player is talking in
     *
     * @param uuid    uuid
     * @param channel channel
     */
    @Override
    public void updateCurrentChannel(UUID uuid, Channel channel) {
        try (Jedis jedis = pool.getResource()) {
            // update key
            if (channel != null)
                jedis.set(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                        .replace("%uuid%", uuid.toString()), channel.getHandle());
            else {
                // get current
                Channel current = getCurrentChannel(uuid);
                if (current != null)
                    jedis.del(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                            .replace("%uuid%", uuid.toString()), current.getHandle());
            }
        }
    }

    /**
     * Gets a list of subscribed channels
     *
     * @param uuid uuid
     * @return channels
     */
    @Override
    public List<Channel> getSubscribedChannels(UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            List<Channel> channels = jedis.lrange(REDIS_PLAYER_SUBSCRIBED_CHANNELS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%uuid%", uuid.toString()), 0, -1).stream()
                    .map(s -> plugin.getChannelManager().get(s, null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            jedis.close();
            return channels;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Gets the current channel that a player is talking in
     *
     * @param uuid uuid
     * @return channel
     */
    @Override
    public Channel getCurrentChannel(UUID uuid) {
        try (Jedis jedis = pool.getResource()) {
            Channel channel = plugin.getChannelManager().get(jedis.get(REDIS_PLAYER_CURRENT_CHANNEL_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%uuid%", uuid.toString())), null);
            jedis.close();
            return channel;
        }
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
    public List<UUID> getSubscribedUUIDs(Channel channel) {
        try (Jedis jedis = pool.getResource()) {
            // get list of uuids of players who've subscribed to a given channel
            List<String> uuids = jedis.lrange(REDIS_CHANNELS_SUBSCRIBED_UUIDS_LIST_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%channel%", channel.getHandle()), 0, -1);
            jedis.close();
            // map and return
            return uuids.stream()
                    .map(UUID::fromString)
                    .filter(u -> {
                        Player p = Bukkit.getPlayer(u);
                        return p != null && p.isOnline();
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void addPlayer(String username) {
        try (Jedis jedis = pool.getResource()) {
            jedis.set(REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%username%", username.toLowerCase(Locale.ROOT))
                    , REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()));

            // also lpush to player list that contains list of all online players
            jedis.lpush(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), username);
        }
    }

    @Override
    public void removePlayer(String username) {
        try (Jedis jedis = pool.getResource()) {
            String key = REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%username%", username.toLowerCase(Locale.ROOT));
            String storedServer = jedis.get(key);
            if (storedServer == null || storedServer.equals(REDIS_SERVER_IDENTIFIER.get(plugin.getSpaceChatConfig().getAdapter()))) {
                jedis.del(key);

                // also lrem from player list that contains list of all online players
                jedis.lrem(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), 0, username);
            }
        }
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
    public String getPlayerServer(String username) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(REDIS_ONLINE_PLAYERS_SERVER_KEY.get(plugin.getSpaceChatConfig().getAdapter())
                    .replace("%username%", username.toLowerCase(Locale.ROOT)));
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to get player server for " + username + ". " + e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }

    @Override
    public Collection<String> getPlayers() {
        try (Jedis jedis = pool.getResource()) {
            return jedis.lrange(REDIS_ONLINE_PLAYERS_KEY.get(plugin.getSpaceChatConfig().getAdapter()), 0, -1);
        } catch (Exception e) {
            plugin.getLogger().warning("Unable to get players. " + e.getClass().getSimpleName() + " " + e.getMessage());
            return null;
        }
    }
}
