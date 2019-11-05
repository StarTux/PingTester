package com.cavetale.pingtester;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PingTesterPlugin extends JavaPlugin {
    private Field pingField;
    private Method handleMethod;

    String format(String str, Object... args) {
        str = ChatColor.translateAlternateColorCodes('&', str);
        if (args.length != 0) str = String.format(str, args);
        return str;
    }

    void message(CommandSender sender, String s, Object... args) {
        sender.sendMessage(format(s, args));
    }

    String getPingQuality(int i) {
        if (i == 0) return "" + ChatColor.YELLOW + "N/A";
        if (i < 100) return "" + ChatColor.GREEN + ChatColor.BOLD + "Outstanding";
        if (i < 250) return "" + ChatColor.GREEN + "Excellent";
        if (i < 350) return "" + ChatColor.YELLOW + "Okay";
        return "" + ChatColor.DARK_RED + "High network latency";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (!sender.hasPermission("pingtester.getping.other")) return false;
            Player player = getServer().getPlayer(args[0]);
            if (player == null) {
                message(sender, "&cPlayer not found: %s", args[0]);
                return true;
            }
            int ping = getPing(player);
            if (ping < 0) ping = 0;
            message(sender, "&bPing of &3%s&b:&r %d (%s&r)",
                    player.getName(), ping, getPingQuality(ping));
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                message(sender, "&cPlayer expected");
                return true;
            }
            int ping = getPing((Player) sender);
            if (ping < 0) ping = 0;
            message(sender, "&bYour ping:&r %d (%s&r)", ping, getPingQuality(ping));
            return true;
        }
        return false;
    }

    public Method getHandleMethod(Object obj) {
        if (handleMethod == null) {
            try {
                handleMethod = obj.getClass().getMethod("getHandle", new Class[0]);
            } catch (NoSuchMethodException nsme) {
                nsme.printStackTrace();
            }
        }
        return handleMethod;
    }

    public Field getPingField(Object obj) {
        if (pingField == null) {
            try {
                pingField = obj.getClass().getField("ping");
            } catch (NoSuchFieldException nsfe) {
                nsfe.printStackTrace();
            }
        }
        return pingField;
    }

    public int getPing(Player player) {
        Object obj;
        Field field;
        Method method = getHandleMethod(player);
        if (method == null) {
            getLogger().warning("Method \"getHandle()\" not found.");
            return 0;
        }
        obj = null;
        try {
            obj = method.invoke(player, new Object[0]);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return 0;
        }
        field = getPingField(obj);
        if (field == null) {
            getLogger().warning("Field \"ping\" not found.");
            return 0;
        }
        try {
            return field.getInt(obj);
        } catch (IllegalAccessException iae) {
            return 0;
        }
    }
}
