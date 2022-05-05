package dev.spaceseries.spacechat.listener;

import dev.spaceseries.spacechat.Messages;
import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.Channel;
import dev.spaceseries.spacechat.model.ChatType;
import dev.spaceseries.spacechat.model.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final SpaceChatPlugin plugin;

    public ChatListener(SpaceChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens for chat messages
     * At the MONITOR priority (runs near LAST) to accommodate for plugins that block chat (mutes, anti-bots, etc)
     *
     * @param event The event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        // clear recipients to "cancel"
        event.getRecipients().clear();

        // check if they have chat enabled
        User user = plugin.getUserManager().get(event.getPlayer().getUniqueId());
        if (user != null && !user.hasChatEnabled(ChatType.PUBLIC)) {
            Messages.getInstance(plugin).chatDisabled.message(event.getPlayer());
            return;
        }

        // get player's current channel
        plugin.getServerSyncServiceManager().getDataService().getCurrentChannel(event.getPlayer().getUniqueId()).thenAccept(current -> {
            // if not null, send through channel manager
            if (current != null && event.getPlayer().hasPermission(current.getPermission())) {
                plugin.getChannelManager().send(event.getPlayer(), event, event.getMessage(), current);
                return;
            }

            // get chat format manager, send chat packet (this method also sets the format in console)
            plugin.getChatFormatManager().send(event, event.getMessage());
        });
    }
}
