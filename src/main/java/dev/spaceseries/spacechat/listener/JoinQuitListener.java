package dev.spaceseries.spacechat.listener;

import dev.spaceseries.spacechat.SpaceChatPlugin;
import dev.spaceseries.spacechat.model.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final SpaceChatPlugin plugin;

    public JoinQuitListener(SpaceChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        User user = plugin.getUserManager().get(event.getPlayer().getUniqueId());

        // update
        plugin.getUserManager().update(user);

        // invalidate
        plugin.getUserManager().invalidate(user.getUuid());

        // remove from online list
        plugin.getServerSyncServiceManager().getDataService().removePlayer(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        // handle with user manager
        plugin.getUserManager().use(event.getUniqueId(), (user) -> {
            // if username not equal, update
            if (!event.getName().equals(user.getUsername())) {
                plugin.getUserManager().update(new User(plugin, user.getUuid(), event.getName(), user.getDate(), user.getSubscribedChannels(), user.getLastMessaged(), user.getIgnored(), user.getDisabledChats()));
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPostJoin(PlayerJoinEvent event) {
        // add to online list
        plugin.getServerSyncServiceManager().getDataService().addPlayer(event.getPlayer().getName());
    }
}
