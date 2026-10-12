package org.dynmap.pnx;

import cn.nukkit.block.BlockState;
import cn.nukkit.block.BlockProperties;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.registry.Registries;

import org.dynmap.DynmapCore;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.DynmapWorld;
import org.dynmap.Log;
import org.dynmap.MapManager;
import org.dynmap.PlayerList;
import org.dynmap.common.DynmapListenerManager;
import org.dynmap.common.DynmapListenerManager.EventType;
import org.dynmap.common.DynmapPlayer;
import org.dynmap.common.chunk.GenericChunkCache;
import org.dynmap.renderer.DynmapBlockState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynmapPlugin extends PluginBase {
    public DynmapCore core;
    public GenericChunkCache sscache;
    public PlayerList playerList;
    public MapManager mapManager;

    private PNXServer pnxserver;
    private final Map<String, PNXWorld> worlds = new HashMap<>();
    private String[] biomeNames = new String[0];

    public static final Map<Integer, DynmapBlockState> stateByHash = new HashMap<>();

    private static final boolean TRIGGER_DEFAULTS = true;

    @Override
    public void onEnable() {
        Log.setLogger(new PNXLog(getLogger()));

        initializeBlockStates();

        pnxserver = new PNXServer(this);

        core = new DynmapCore();
        core.setPluginVersion(getDescription().getVersion(), "PowerNukkitX");
        core.setMinecraftVersion(getServer().getApiVersion());
        core.setDataFolder(getDataFolder());
        core.setServer(pnxserver);
        core.setTriggerDefault(TRIGGER_DEFAULTS);
        core.setBiomeNames(getBiomeNames());

        if (!core.initConfiguration(null)) {
            getLogger().error("Dynmap core configuration failed - disabling");
            return;
        }

        File permExample = new File(core.getDataFolder(), "permissions.yml.example");
        core.createDefaultFileFromResource("/permissions.yml.example", permExample);

        if (!core.enableCore(null)) {
            getLogger().error("Dynmap core enable failed - disabling");
            return;
        }

        sscache = new GenericChunkCache(core.getSnapShotCacheSize(), core.useSoftRefInSnapShotCache());
        playerList = core.playerList;
        mapManager = core.getMapManager();

        for (Level l : getServer().getLevels().values()) {
            DynmapWorld w = getOrAddWorld(l);
            if (core.processWorldLoad(w)) {
                core.listenerManager.processWorldEvent(EventType.WORLD_LOAD, w);
            }
        }

        getServer().getPluginManager().registerEvents(new PNXListener(this), this);

        core.updateConfigHashcode();
        core.serverStarted();
        getLogger().info("Dynmap (PowerNukkitX) enabled");
    }

    @Override
    public void onDisable() {
        if (core != null) {
            DynmapCommonAPIListener.cleanup(core);
            core.disableCore();
        }
        if (sscache != null) {
            sscache.cleanup();
            sscache = null;
        }
        worlds.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (core == null) {
            return false;
        }
        org.dynmap.common.DynmapCommandSender dsender;
        if (sender instanceof cn.nukkit.Player p) {
            dsender = new PNXPlayer(p);
        } else {
            dsender = new PNXCommandSender(sender);
        }
        return core.processCommand(dsender, command.getName(), label, args);
    }

    public PNXServer getServerInterface() {
        return pnxserver;
    }

    public DynmapWorld getWorldByName(String wname) {
        return worlds.get(wname);
    }

    public PNXWorld getOrAddWorld(Level level) {
        PNXWorld w = worlds.get(level.getName());
        if (w == null) {
            w = new PNXWorld(this, level);
            worlds.put(level.getName(), w);
        } else {
            w.setLevelLoaded(level);
        }
        return w;
    }

    public void removeWorld(Level level) {
        PNXWorld w = worlds.get(level.getName());
        if (w != null) {
            w.setWorldUnloaded();
        }
    }

    public String[] getBiomeNames() {
        return biomeNames;
    }

    public boolean requestEventNotification(EventType type) {
        return true;
    }

    public boolean sendWebChatEvent(String source, String name, String msg) {
        return (core != null) && core.webChat(name, msg);
    }

    public DynmapPlayer wrap(cn.nukkit.Player p) {
        return new PNXPlayer(p);
    }

    public void worldLoaded(Level level) {
        if (core == null) {
            return;
        }
        DynmapWorld w = getOrAddWorld(level);
        if (core.processWorldLoad(w)) {
            core.listenerManager.processWorldEvent(EventType.WORLD_LOAD, w);
        }
    }

    public void worldUnloaded(Level level) {
        if (core == null) {
            return;
        }
        DynmapWorld w = getWorldByName(level.getName());
        if (w != null) {
            core.listenerManager.processWorldEvent(EventType.WORLD_UNLOAD, w);
            core.processWorldUnload(w);
            removeWorld(level);
        }
    }

    public void initializeBlockStates() {
        stateByHash.clear();
        DynmapBlockState.Builder bld = new DynmapBlockState.Builder();
        int legacyId = 0;

        for (String id : new ArrayList<>(Registries.BLOCK.getKeySet())) {
            BlockProperties props;
            try {
                props = Registries.BLOCK.getBlockProperties(id);
            } catch (Exception e) {
                continue;
            }
            if (props == null) {
                continue;
            }
            String blockName = id.contains(":") ? id : ("minecraft:" + id);

            Map<Short, BlockState> states = props.getSpecialValueMap();
            BlockState defState = props.getDefaultState();

            DynmapBlockState base = null;
            int stateIdx = 0;
            List<BlockState> ordered = new ArrayList<>();
            if (defState != null) {
                ordered.add(defState);
            }
            for (BlockState st : states.values()) {
                if (st != defState) {
                    ordered.add(st);
                }
            }

            for (BlockState st : ordered) {
                bld.setBaseState(base)
                   .setStateIndex(stateIdx)
                   .setBlockName(blockName)
                   .setStateName("meta=" + (st.specialValue() & 0xFFFF))
                   .setLegacyBlockID(legacyId);
                applyMaterialFlags(bld, blockName);
                DynmapBlockState dbs = bld.build();
                stateByHash.put(st.blockStateHash(), dbs);
                if (base == null) {
                    base = dbs;
                }
                stateIdx++;
            }
            legacyId++;
        }
        getLogger().info("Initialized " + stateByHash.size() + " Dynmap block states from PowerNukkitX");
    }

    private void applyMaterialFlags(DynmapBlockState.Builder bld, String blockName) {
        if (blockName.equals(DynmapBlockState.AIR_BLOCK)) {
            bld.setAir();
        } else if (blockName.contains("water")) {
            bld.setWaterlogged();
        } else if (blockName.endsWith("_log") || blockName.endsWith("log") || blockName.endsWith("_stem")) {
            bld.setLog();
        } else if (blockName.endsWith("leaves")) {
            bld.setLeaves();
        } else {
            bld.setSolid();
        }
    }
}
