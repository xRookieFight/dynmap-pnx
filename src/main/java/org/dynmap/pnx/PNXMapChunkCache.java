package org.dynmap.pnx;

import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.IChunk;

import org.dynmap.DynmapChunk;
import org.dynmap.common.BiomeMap;
import org.dynmap.common.chunk.GenericChunk;
import org.dynmap.common.chunk.GenericChunkSection;
import org.dynmap.common.chunk.GenericMapChunkCache;
import org.dynmap.renderer.DynmapBlockState;

import java.util.List;

public class PNXMapChunkCache extends GenericMapChunkCache {
    private Level level;

    public PNXMapChunkCache(DynmapPlugin plugin) {
        super(plugin.sscache);
    }

    public void setChunks(PNXWorld dw, List<DynmapChunk> chunks) {
        this.level = dw.getLevel();
        super.setChunks(dw, chunks);
    }

    @Override
    protected GenericChunk getLoadedChunk(DynmapChunk chunk) {
        if (level == null) {
            return null;
        }
        if (!level.isChunkLoaded(chunk.x, chunk.z)) {
            return null;
        }
        IChunk ic = level.getChunk(chunk.x, chunk.z, false);
        if (ic == null || !ic.isGenerated()) {
            return null;
        }
        return buildGenericChunk(ic);
    }

    @Override
    protected GenericChunk loadChunk(DynmapChunk chunk) {
        if (level == null) {
            return null;
        }
        IChunk ic = level.getChunk(chunk.x, chunk.z, true);
        if (ic == null || !ic.isGenerated()) {
            return null;
        }
        return buildGenericChunk(ic);
    }

    private GenericChunk buildGenericChunk(IChunk ic) {
        int yMin = level.getMinHeight();
        int yMax = level.getMaxHeight();
        GenericChunk.Builder cb = new GenericChunk.Builder(yMin, yMax);
        cb.coords(ic.getX(), ic.getZ());

        int secMin = yMin >> 4;
        int secMax = (yMax - 1) >> 4;

        GenericChunkSection.Builder sb = new GenericChunkSection.Builder();
        for (int sy = secMin; sy <= secMax; sy++) {
            int baseY = sy << 4;
            if (ic.isSectionEmpty(sy)) {
                continue;
            }
            sb.reset();
            boolean nonEmpty = false;
            byte[] sky = new byte[2048];
            byte[] emit = new byte[2048];
            for (int y = 0; y < 16; y++) {
                int wy = baseY + y;
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        DynmapBlockState bs = stateFor(ic, x, wy, z);
                        if (bs != DynmapBlockState.AIR) {
                            nonEmpty = true;
                        }
                        sb.xyzBlockState(x, y, z, bs);
                        int yzx = (y << 8) | (z << 4) | x;
                        setNibble(sky, yzx, ic.getBlockSkyLight(x, wy, z));
                        setNibble(emit, yzx, ic.getBlockLight(x, wy, z));
                    }
                }
            }
            sb.skyLight(sky);
            sb.emittedLight(emit);

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    sb.xzBiome(x, z, biomeFor(ic, x, baseY, z));
                }
            }
            if (nonEmpty) {
                cb.addSection(sy, sb.build());
            }
        }
        cb.generateSky();
        return cb.build();
    }

    private static void setNibble(byte[] arr, int yzx, int val) {
        int idx = yzx >> 1;
        if ((yzx & 1) == 0) {
            arr[idx] = (byte) ((arr[idx] & 0xF0) | (val & 0x0F));
        } else {
            arr[idx] = (byte) ((arr[idx] & 0x0F) | ((val & 0x0F) << 4));
        }
    }

    private DynmapBlockState stateFor(IChunk ic, int x, int y, int z) {
        BlockState st = ic.getBlockState(x, y, z);
        DynmapBlockState dbs = DynmapPlugin.stateByHash.get(st.blockStateHash());
        return (dbs != null) ? dbs : DynmapBlockState.AIR;
    }

    private BiomeMap biomeFor(IChunk ic, int x, int y, int z) {
        int id = ic.getBiomeId(x, y, z);
        BiomeMap bm = BiomeMap.byBiomeID(id);
        return (bm != null) ? bm : BiomeMap.NULL;
    }
}
