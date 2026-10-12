package org.dynmap.pnx;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.ServerScheduler;

import org.dynmap.DynmapChunk;
import org.dynmap.DynmapWorld;
import org.dynmap.common.DynmapPlayer;
import org.dynmap.common.DynmapServerInterface;
import org.dynmap.common.DynmapListenerManager.EventType;
import org.dynmap.utils.MapChunkCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class PNXServer extends DynmapServerInterface {
    private final DynmapPlugin plugin;
    private final Server server;

    public PNXServer(DynmapPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Override
    public void scheduleServerTask(Runnable run, long delay) {
        ServerScheduler sched = server.getScheduler();
        sched.scheduleDelayedTask(plugin, run, (int) delay);
    }

    @Override
    public <T> Future<T> callSyncMethod(Callable<T> task) {
        FutureTask<T> ft = new FutureTask<>(task);
        if (isServerThread()) {
            ft.run();
        } else {
            server.getScheduler().scheduleTask(plugin, ft);
        }
        return ft;
    }

    @Override
    public DynmapPlayer[] getOnlinePlayers() {
        var online = server.getOnlinePlayers().values();
        DynmapPlayer[] arr = new DynmapPlayer[online.size()];
        int i = 0;
        for (Player p : online) {
            arr[i++] = new PNXPlayer(p);
        }
        return arr;
    }

    @Override
    public void reload() {
        plugin.onDisable();
        plugin.onEnable();
    }

    @Override
    public DynmapPlayer getPlayer(String name) {
        Player p = server.getPlayerExact(name);
        return (p != null) ? new PNXPlayer(p) : null;
    }

    @Override
    public DynmapPlayer getOfflinePlayer(String name) {
        Player p = server.getPlayerExact(name);
        return (p != null) ? new PNXPlayer(p) : null;
    }

    @Override
    public Set<String> getIPBans() {
        return new HashSet<>(server.getIPBans().getEntires().keySet());
    }

    @Override
    public String getServerName() {
        return server.getName();
    }

    @Override
    public boolean isPlayerBanned(String pid) {
        return server.getNameBans().isBanned(pid);
    }

    @Override
    public String stripChatColor(String s) {
        return cn.nukkit.utils.TextFormat.clean(s);
    }

    @Override
    public boolean requestEventNotification(EventType type) {
        return plugin.requestEventNotification(type);
    }

    @Override
    public boolean sendWebChatEvent(String source, String name, String msg) {
        return plugin.sendWebChatEvent(source, name, msg);
    }

    @Override
    public void broadcastMessage(String msg) {
        server.broadcastMessage(msg);
    }

    @Override
    public String[] getBiomeIDs() {
        return plugin.getBiomeNames();
    }

    @Override
    public double getCacheHitRate() {
        return (plugin.sscache != null) ? plugin.sscache.getHitRate() : 0.0;
    }

    @Override
    public void resetCacheStats() {
        if (plugin.sscache != null) {
            plugin.sscache.resetStats();
        }
    }

    @Override
    public DynmapWorld getWorldByName(String wname) {
        return plugin.getWorldByName(wname);
    }

    @Override
    public Set<String> checkPlayerPermissions(String player, Set<String> perms) {
        Player p = server.getPlayerExact(player);
        Set<String> granted = new HashSet<>();
        if (p != null) {
            for (String perm : perms) {
                if (p.hasPermission("dynmap." + perm)) {
                    granted.add(perm);
                }
            }
        }
        return granted;
    }

    @Override
    public boolean checkPlayerPermission(String player, String perm) {
        Player p = server.getPlayerExact(player);
        return (p != null) && p.hasPermission("dynmap." + perm);
    }

    @Override
    public MapChunkCache createMapChunkCache(DynmapWorld w, List<DynmapChunk> chunks,
            boolean blockdata, boolean highesty, boolean biome, boolean rawbiome) {
        PNXMapChunkCache c = (PNXMapChunkCache) w.getChunkCache(chunks);
        if (c == null) {
            return null;
        }
        if (chunks.size() == 0) {
            c.loadChunks(0);
        }
        c.setChunkDataTypes(blockdata, biome, highesty, rawbiome);
        return c;
    }

    @Override
    public int getMaxPlayers() {
        return server.getMaxPlayers();
    }

    @Override
    public int getCurrentPlayers() {
        return server.getOnlinePlayers().size();
    }

    @Override
    public int getBlockIDAt(String wname, int x, int y, int z) {
        return -1;
    }

    @Override
    public int isSignAt(String wname, int x, int y, int z) {
        return -1;
    }

    @Override
    public double getServerTPS() {
        return server.getTicksPerSecond();
    }

    @Override
    public String getServerIP() {
        return server.getIp();
    }

    @Override
    public boolean isServerThread() {
        return server.isPrimaryThread();
    }

    public List<DynmapWorld> getWorlds() {
        List<DynmapWorld> list = new ArrayList<>();
        for (Level l : server.getLevels().values()) {
            list.add(plugin.getOrAddWorld(l));
        }
        return list;
    }
}
