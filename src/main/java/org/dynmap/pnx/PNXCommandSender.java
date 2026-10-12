package org.dynmap.pnx;

import cn.nukkit.command.CommandSender;

import org.dynmap.common.DynmapCommandSender;

public class PNXCommandSender implements DynmapCommandSender {
    protected CommandSender sender;

    public PNXCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPrivilege(String privid) {
        return hasPermissionNode("dynmap." + privid);
    }

    @Override
    public void sendMessage(String msg) {
        if (sender != null) {
            sender.sendMessage(msg);
        }
    }

    @Override
    public boolean isConnected() {
        return sender != null;
    }

    @Override
    public boolean isOp() {
        return (sender != null) && sender.isOp();
    }

    @Override
    public boolean hasPermissionNode(String node) {
        return (sender != null) && sender.hasPermission(node);
    }
}
