package dev.spaceseries.spacechat.channel;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.loader.ChannelLoader;
import dev.spaceseries.spacechat.model.ChannelType;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.manager.MapManager;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;

public class ChannelManager extends MapManager<String, Channel> {

    /**
     * The format loader
     */
    private final ChannelLoader channelLoader;

    private final SpaceChatPlugin plugin;

    /**
     * Initializes
     */
    public ChannelManager(SpaceChatPlugin plugin) {
        this.plugin = plugin;

        // create format manager
        this.channelLoader = new ChannelLoader(plugin,
                ChannelType.NORMAL.getSectionKey().toLowerCase(Locale.ROOT)
        );

        // load
        this.loadFormats();
    }

    /**
     * Loads formats
     */
    public void loadFormats() {
        channelLoader.load(this);
    }

    /**
     * Sends a chat message using the applicable format
     *
     * @param player  The player
     * @param event   The event
     * @param message The message
     */
    public void send(Player player, @Nullable AsyncPlayerChatEvent event, String message, Channel channel) {
        // if no permission, unsubscribe them
        if (!player.hasPermission(channel.getPermission())) {
            plugin.getServerSyncServiceManager().getDataService().unsubscribeFromChannel(player.getUniqueId(), channel);
            return;
        }

        // send chat message
        plugin.getChatManager().sendChatMessage(player, message, channel.getFormat(), event);
    }
}
