package me.kilianlegters;

import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;

public class RealLightWeightHomes extends JavaPlugin {

    public static RealLightWeightHomes realLightWeightHomes;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        realLightWeightHomes = this;
        this.dataManager = new DataManager();

        Homes homes = new Homes(dataManager);
        getCommand("sethome").setExecutor(homes);
        getCommand("home").setExecutor(homes);
        getCommand("delhome").setExecutor(homes);
    }

    @Override
    public void onDisable() {
        dataManager.onDisable();
    }
}
