package org.dynmap.pnx;

import cn.nukkit.plugin.PluginLogger;
import org.dynmap.utils.DynmapLogger;

public class PNXLog implements DynmapLogger {
    private final PluginLogger log;

    public PNXLog(PluginLogger log) {
        this.log = log;
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void severe(Throwable t) {
        log.error("Exception", t);
    }

    @Override
    public void severe(String s) {
        log.error(s);
    }

    @Override
    public void severe(String s, Throwable t) {
        log.error(s, t);
    }

    @Override
    public void warning(String s) {
        log.warning(s);
    }

    @Override
    public void warning(String s, Throwable t) {
        log.warning(s, t);
    }

    @Override
    public void verboseinfo(String s) {
        log.info(s);
    }
}
