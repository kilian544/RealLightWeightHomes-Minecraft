package me.kilianlegters;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class CustomRunnable extends BukkitRunnable {

    private Homes homes;
    private Player player;
    private String home;
    private Location homeLocation;
    private List<Location> queue;
    private int iterations;

    public CustomRunnable(Homes homes, Player player, String home, Location homeLocation, List<Location> queue, int iterations){
        this.homes = homes;
        this.player = player;
        this.home = home;
        this.homeLocation = homeLocation;
        this.queue = queue;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        Location holder = this.homes.getSafeHomeLocation(homeLocation, queue, iterations);
        if (holder != null){
            this.homes.teleport(player, holder.setDirection(homeLocation.getDirection()));
            this.homes.setHome(player, home, holder, true);
            cancel();
        }
    }
}