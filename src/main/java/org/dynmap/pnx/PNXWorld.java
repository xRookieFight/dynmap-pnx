package org.dynmap.pnx;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;

import org.dynmap.DynmapChunk;
import org.dynmap.DynmapLocation;
import org.dynmap.DynmapWorld;
import org.dynmap.utils.MapChunkCache;

import java.util.List;

public class PNXWorld extends DynmapWorld {
    public static final int ENV_OVERWORLD = 0;
    public static final int ENV_NETHER = 1;
    public static final int ENV_END = 2;

    private final DynmapPlugin plugin;
    private Level level;
    private final boolean isnether;
    private final boolean istheend;
    private final boolean skylight;
    private final String env;
    private final DynmapLocation spawnloc = new DynmapLocation();

    private static int defaultSeaLevel(int dimension) {
        return (dimension == ENV_OVERWORLD) ? 63 : 0;
    }

    public PNXWorld(DynmapPlugin plugin, Level level) {
        super(level.getName(), level.getMaxHeight() - level.getMinHeight(),
                defaultSeaLevel(level.getDimension()), level.getMinHeight());
        this.plugin = plugin;
        this.level = level;
        this.isnether = level.getDimension() == ENV_NETHER;
        this.istheend = level.getDimension() == ENV_END;
        this.skylight = !(isnether || istheend);
        if (isnether) {
            env = "nether";
        } else if (istheend) {
            env = "the_end";
        } else {
            env = "normal";
        }
        setTitle(level.getName());
        for (int ll = 0; ll < 16; ll++) {
            float v = ll / 15.0f;
            setBrightnessTableEntry(ll, v / (4.0f - 3.0f * v));
        }
    }

    public Level getLevel() {
        return level;
    }

    public void setLevelLoaded(Level level) {
        this.level = level;
    }

    @Override
    public boolean isNether() {
        return isnether;
    }

    public boolean isTheEnd() {
        return istheend;
    }

    @Override
    public DynmapLocation getSpawnLocation() {
        if (level != null) {
            Position s = level.getSpawnLocation();
            spawnloc.x = s.getX();
            spawnloc.y = s.getY();
            spawnloc.z = s.getZ();
            spawnloc.world = getName();
        }
        return spawnloc;
    }

    @Override
    public long getTime() {
        return (level != null) ? level.getTime() : -1;
    }

    @Override
    public boolean hasStorm() {
        return (level != null) && level.isRaining();
    }

    @Override
    public boolean isThundering() {
        return (level != null) && level.isThundering();
    }

    @Override
    public boolean isLoaded() {
        return level != null;
    }

    @Override
    public void setWorldUnloaded() {
        getSpawnLocation();
        level = null;
    }

    @Override
    public int getLightLevel(int x, int y, int z) {
        return (level != null) ? level.getFullLight(new cn.nukkit.math.Vector3(x, y, z)) : -1;
    }

    @Override
    public int getHighestBlockYAt(int x, int z) {
        return (level != null) ? level.getHighestBlockAt(x, z) : -1;
    }

    @Override
    public boolean canGetSkyLightLevel() {
        return skylight;
    }

    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        return (level != null) ? level.getBlockSkyLightAt(x, y, z) : -1;
    }

    @Override
    public String getEnvironment() {
        return env;
    }

    @Override
    public MapChunkCache getChunkCache(List<DynmapChunk> chunks) {
        if (level == null) {
            return null;
        }
        PNXMapChunkCache c = new PNXMapChunkCache(plugin);
        c.setChunks(this, chunks);
        return c;
    }
}
