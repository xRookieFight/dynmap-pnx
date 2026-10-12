package org.dynmap.pnx;

import cn.nukkit.Player;
import cn.nukkit.level.Location;

import org.dynmap.DynmapLocation;
import org.dynmap.common.DynmapPlayer;

import java.net.InetSocketAddress;
import java.util.UUID;

public class PNXPlayer extends PNXCommandSender implements DynmapPlayer {
    private final Player player;
    private int sortWeight;

    public PNXPlayer(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public String getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public DynmapLocation getLocation() {
        Location loc = player.getLocation();
        return new DynmapLocation(player.getLevel().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public String getWorld() {
        if (player.getLevel() == null) {
            return null;
        }
        return player.getLevel().getName();
    }

    @Override
    public InetSocketAddress getAddress() {
        return player.getSocketAddress();
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }

    @Override
    public boolean isSpectator() {
        return player.isSpectator();
    }

    @Override
    public double getHealth() {
        return player.getHealth();
    }

    @Override
    public int getArmorPoints() {
        int total = 0;
        for (cn.nukkit.item.Item armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                total += armor.getArmorPoints();
            }
        }
        return total;
    }

    @Override
    public DynmapLocation getBedSpawnLocation() {
        Location s = player.getSpawn();
        if (s == null) {
            return null;
        }
        return new DynmapLocation(s.getLevel().getName(), s.getX(), s.getY(), s.getZ());
    }

    @Override
    public long getLastLoginTime() {
        return player.getLastLogin();
    }

    @Override
    public long getFirstLoginTime() {
        return player.getFirstPlayed();
    }

    @Override
    public boolean isInvisible() {
        return player.isInvisible();
    }

    @Override
    public int getSortWeight() {
        return sortWeight;
    }

    @Override
    public void setSortWeight(int wt) {
        this.sortWeight = wt;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    public Player getPlayer() {
        return player;
    }
}
