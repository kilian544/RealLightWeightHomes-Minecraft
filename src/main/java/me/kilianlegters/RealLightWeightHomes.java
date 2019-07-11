package me.kilianlegters;

import org.bukkit.plugin.java.JavaPlugin;

public class RealLightWeightHomes extends JavaPlugin {

    public static RealLightWeightHomes realLightWeightHomes;

    @Override
    public void onEnable() {

        realLightWeightHomes = this;

        Homes homes = new Homes();
        getCommand("sethome").setExecutor(homes);
        getCommand("home").setExecutor(homes);
        getCommand("delhome").setExecutor(homes);
    }

}
