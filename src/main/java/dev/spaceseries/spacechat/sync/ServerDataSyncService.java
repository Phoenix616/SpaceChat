package dev.spaceseries.spacechat.sync;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ServerDataSyncService extends ServerSyncService {

    /**
     * Construct server sync service
     *
     * @param serviceManager service manager
     */
    public ServerDataSyncService(SpaceChatPlugin plugin, ServerSyncServiceManager serviceManager) {
        super(plugin, serviceManager);
    }

    /**
     * Subscribes a player to a channel
     *
     * @param uuid    uuid
     * @param channel channel
     */
    public abstract void subscribeToChannel(UUID uuid, Channel channel);

    /**
     * Unsubscribes a player from a channel
     *
     * @param uuid              uuid
     * @param subscribedChannel subscribed channel
     */
    public abstract void unsubscribeFromChannel(UUID uuid, Channel subscribedChannel);

    /**
     * Updates the current channel that a player is talking in
     *
     * @param uuid    uuid
     * @param channel channel
     */
    public abstract void updateCurrentChannel(UUID uuid, Channel channel);

    /**
     * Gets a list of subscribed channels
     *
     * @param uuid uuid
     * @return channels
     */
    public abstract List<Channel> getSubscribedChannels(UUID uuid);

    /**
     * Gets the current channel that a player is talking in
     *
     * @param uuid uuid
     * @return channel
     */
    public abstract Channel getCurrentChannel(UUID uuid);

    /**
     * Gets a list of all of the uuids of players who are currently subscribed to a given channel
     * <p>
     * This only includes the uuids of players who are currently online
     *
     * @param channel channel
     * @return uuids
     */
    public abstract List<UUID> getSubscribedUUIDs(Channel channel);

    /**
     * Add a player to the online list of the current server
     *
     * @param username  the name of the player
     */
    public abstract void addPlayer(String username);

    /**
     * Remove a player from the online list of the current server
     *
     * @param username  the name of the player
     */
    public abstract void removePlayer(String username);

    /**
     * Remove all players from the online list of the current server that are offline
     */
    public abstract void removeAllServerPlayers();

    /**
     * Get the server that a player is online at or null if they aren't online
     *
     * @return the server or null
     */
    public abstract String getPlayerServer(String username);

    /**
     * Get all known players
     *
     * @return all known players
     */
    public abstract Collection<String> getPlayers();
}
