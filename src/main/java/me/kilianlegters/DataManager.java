package me.kilianlegters;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;

public class DataManager {

    private FileConfiguration configuration = RealLightWeightHomes.realLightWeightHomes.getConfig();
    private File dataFile;
    private FileConfiguration dataFileConfiguration;

    public DataManager(){
        onEnable();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RealLightWeightHomes.realLightWeightHomes, new Runnable() {
            @Override
            public void run() {
                saveData();
            }
        }, 20, 20 * 60 * 5);
    }

    private void onEnable(){
        loadData();
        tryMigrateDataFromPreV03();
        upDateVersion();
    }

    public void onDisable(){
        saveData();
        saveConfig();
    }

    public FileConfiguration getData() {
        return dataFileConfiguration;
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public void saveData(){
        Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Data.yml saved.");
        try {
            dataFileConfiguration.save(dataFile);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveConfig(){
        Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Config.yml saved.");
        RealLightWeightHomes.realLightWeightHomes.saveConfig();
    }

    private FileConfiguration loadData() {
        File holder = getFile("data.yml");
        this.dataFileConfiguration = getFileConfiguration(holder);
        this.dataFile = holder;
        Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Data.yml loaded.");
        return dataFileConfiguration;
    }

    private File getFile(String fileName){
        String[] holder = fileName.split(".");
        if (holder.length > 2){
            Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Filename contains multiple extensions/'.' in name. Using: " + holder[0] + ".yml");
            fileName = holder[0]+".yml";
        }
        if (holder.length == 1){
            fileName += ".yml";
        }

        File file = new File(RealLightWeightHomes.realLightWeightHomes.getDataFolder(), fileName);

        try {
            if (!file.exists()) {
                RealLightWeightHomes.realLightWeightHomes.getDataFolder().mkdirs();
                file.createNewFile();
                Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Creating: " + file.getAbsolutePath());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return file;
    }

    private FileConfiguration getFileConfiguration(File file){
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return fileConfiguration;
    }

    private void upDateVersion(){
        if (!configuration.isSet("version")){
            Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "version wasn't set, setting now.");
        } else {
            if (configuration.getString("version").equalsIgnoreCase(RealLightWeightHomes.realLightWeightHomes.getDescription().getVersion())){
                Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Version in config.yml same as plugin.");
                return;
            }
        }

        Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "Updating version in config to match plugin.");
        configuration.set("version", RealLightWeightHomes.realLightWeightHomes.getDescription().getVersion());
        saveConfig();
    }

    private void tryMigrateDataFromPreV03(){

        if (configuration.isSet("version")){
            return;
        }

        if (configuration.getKeys(false).size() == 0) {
            return;
        }

        Bukkit.getLogger().info("[" + RealLightWeightHomes.realLightWeightHomes.getDescription().getFullName() + "] " + "No version in existing config detected. Migrating data to post 0.3 update.");
        for (String key: configuration.getKeys(false)){
            getData().set(key + "." + "home", configuration.get(key));
            getConfig().set(key, null);
        }
        saveData();
        saveConfig();
    }

}
