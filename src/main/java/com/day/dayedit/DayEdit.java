package com.day.dayedit;

import com.day.dayedit.commands.DayEditCommand;
import com.day.dayedit.gui.MenuListener;
import org.bukkit.plugin.java.JavaPlugin;

public class DayEdit extends JavaPlugin {

    private static DayEdit instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        DayEditCommand commandExecutor = new DayEditCommand(this);
        getCommand("dayedit").setExecutor(commandExecutor);
        getCommand("dayedit").setTabCompleter(commandExecutor);

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        getLogger().info("DayEdit ha sido activado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("DayEdit ha sido desactivado.");
    }

    public static DayEdit getInstance() {
        return instance;
    }

    public String getMsg(String path) {
        String msg = getConfig().getString("mensajes." + path, "");
        String prefix = getConfig().getString("prefix", "");
        return prefix + msg;
    }
          }
