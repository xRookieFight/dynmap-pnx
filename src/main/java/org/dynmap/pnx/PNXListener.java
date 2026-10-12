package org.dynmap.pnx;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.level.Location;

import org.dynmap.common.DynmapListenerManager.EventType;
import org.dynmap.common.DynmapPlayer;

public class PNXListener implements Listener {
    private final DynmapPlugin plugin;

    public PNXListener(DynmapPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (plugin.core == null) {
            return;
        }
        final DynmapPlayer dp = plugin.wrap(e.getPlayer());
        plugin.core.listenerManager.processPlayerEvent(EventType.PLAYER_JOIN, dp);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        if (plugin.core == null) {
            return;
        }
        final DynmapPlayer dp = plugin.wrap(e.getPlayer());
        plugin.core.listenerManager.processPlayerEvent(EventType.PLAYER_QUIT, dp);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(PlayerChatEvent e) {
        if (plugin.core == null) {
            return;
        }
        final DynmapPlayer dp = plugin.wrap(e.getPlayer());
        final String msg = e.getMessage();
        plugin.getServer().getScheduler().scheduleTask(plugin, () ->
                plugin.core.listenerManager.processChatEvent(EventType.PLAYER_CHAT, dp, msg));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        touch(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        touch(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLevelLoad(LevelLoadEvent e) {
        plugin.worldLoaded(e.getLevel());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLevelUnload(LevelUnloadEvent e) {
        plugin.worldUnloaded(e.getLevel());
    }

    private void touch(Location loc) {
        if (plugin.mapManager == null || loc == null || loc.getLevel() == null) {
            return;
        }
        String wname = loc.getLevel().getName();
        if (plugin.sscache != null) {
            plugin.sscache.invalidateSnapshot(wname, loc.getFloorX(), loc.getFloorY(), loc.getFloorZ());
        }
        plugin.mapManager.touch(wname, loc.getFloorX(), loc.getFloorY(), loc.getFloorZ(), "blockchange");
    }
}
