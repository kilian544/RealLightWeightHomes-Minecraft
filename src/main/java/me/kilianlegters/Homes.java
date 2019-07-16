package me.kilianlegters;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Homes implements CommandExecutor {

    private RealLightWeightHomes realLightWeightHomes = RealLightWeightHomes.realLightWeightHomes;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player) commandSender;

            if (strings.length == 0) {
                if (s.equalsIgnoreCase("sethome")) {
                    return setHome(player);
                }

                if (s.equalsIgnoreCase("home")) {
                    return tryTeleportHome(player);
                }

                if (s.equalsIgnoreCase("delhome")) {
                    return delHome(player);
                }
            }

            player.sendMessage(ChatColor.RED + "Unknown syntax.");
            return false;

        } else {
            commandSender.sendMessage(ChatColor.RED + "Only for in-game use.");
            return true;
        }
    }

    private boolean hasHome(Player player){
        return realLightWeightHomes.getConfig().contains(player.getUniqueId().toString());
    }

    private boolean delHome(Player player){
        if (!hasHome(player)){
            player.sendMessage(ChatColor.RED + "You do not yet have a home to delete, use '/sethome'.");
            return false;
        }

        realLightWeightHomes.getConfig().set(player.getUniqueId().toString(), null);
        realLightWeightHomes.saveConfig();
        player.sendMessage(ChatColor.GREEN + "Home deleted.");
        return true;
    }

    private boolean tryTeleportHome(Player player){
        if (!hasHome(player)){
            player.sendMessage(ChatColor.RED + "You do not yet have a home, use '/sethome'.");
            return false;
        }
        Location homeLocation = getLocation(realLightWeightHomes.getConfig().getString(player.getUniqueId().toString()));
        if (!isHomeSafe(homeLocation)) {
            Location holder = getFastSafeHomeLocation(homeLocation);
            if (holder == null) {
                List<Location> queue = new ArrayList<>();
                queue.add(homeLocation);
                player.sendMessage(ChatColor.RED + "Your home is obstructed, looking for the nearest suitable alternative and bringing you there!");
                new CustomRunnable(this, player, homeLocation, queue, 1000).runTaskTimer(realLightWeightHomes, 0, 1);
                return true;
            }
            homeLocation = holder;
        }
        teleport(player, homeLocation);
        return true;
    }

    public void teleport(Player player, Location location){
        player.teleport(location.add(0.5, 0.2, 0.5));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
        player.sendMessage(ChatColor.GREEN + "Teleporting to home...");
    }

    private boolean setHome(Player player){
        if (hasHome(player)){
            player.sendMessage(ChatColor.RED + "You already have a home, use '/delhome' first.");
            return false;
        }
        return setHome(player, player.getLocation(), false);
    }

    public boolean setHome(Player player, Location location, boolean forced){
        realLightWeightHomes.getConfig().set(player.getUniqueId().toString(), getString(location));
        realLightWeightHomes.saveConfig();
        if (forced){
            player.sendMessage(ChatColor.GREEN + "Your home was automatically set!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Home set!");
        }
        return true;
    }

    private Location getLocation(String string){
        String[] strings = string.split("/");
        return new Location(Bukkit.getWorld(UUID.fromString(strings[0])), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]), Float.parseFloat(strings[4]), Float.parseFloat(strings[5]));
    }

    private String getString(Location location){
        return location.getWorld().getUID().toString() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ() + "/" + location.getYaw() + "/" + location.getPitch();
    }

    private Boolean isHomeSafe(Location location){
        return isHomeSafe(location.getBlock());
    }

    private Boolean isHomeSafe(Block block){
        Block upperBlock = block.getRelative(BlockFace.UP);
        Block blockBeneath = block.getRelative(BlockFace.DOWN);
        return block.isPassable() && block.getType() != Material.LAVA &&
                upperBlock.isPassable() && upperBlock.getType() != Material.LAVA &&
                !blockBeneath.isPassable() &&
                block.getY() > 1;
    }

    private Location getFastSafeHomeLocation(Location location){
        Block block = location.getBlock();
        while (block.getRelative(BlockFace.DOWN).isPassable() && block.getY() > 0){
            block = block.getRelative(BlockFace.DOWN);
        }

        if (isHomeSafe(block)){
            return block.getLocation();
        } else {
            return null;
        }
    }

    public Location getSafeHomeLocation(Location location, List<Location> queue, int iterations){
        for (int i = 0; i < iterations; i++) {
            Location locationToCheck = queue.remove(0);
            if (locationToCheck.getBlock().isPassable()){
                Location fastSafeHomeLocation = getFastSafeHomeLocation(locationToCheck);
                if (fastSafeHomeLocation != null) {
                    return fastSafeHomeLocation;
                }
            }
            queue.addAll(getDivergingLocations(location, locationToCheck));
        }
        return null;
    }

    private List<Location> getDivergingLocations(Location origin, Location check){
        int accuracy = 5;
        List<Location> holder = new ArrayList<>();
        int xSign = Integer.signum(check.getBlockX() - origin.getBlockX()),
                ySign = Integer.signum(check.getBlockY() - origin.getBlockY()),
                zSign = Integer.signum(check.getBlockZ() - origin.getBlockZ());

        if (xSign != 0){
            holder.add(check.clone().add(xSign * accuracy, 0,0));
        } else {
            holder.add(check.clone().add(-1 * accuracy, 0, 0));
            holder.add(check.clone().add(1 * accuracy, 0, 0));
        }
        if (ySign != 0){
            holder.add(check.clone().add(0, ySign * accuracy, 0));
        } else {
            holder.add(check.clone().add(0, -1 * accuracy, 0));
            holder.add(check.clone().add(0, 1 * accuracy, 0));
        }
        if (zSign != 0){
            holder.add(check.clone().add(0, 0, zSign * accuracy));
        } else {
            holder.add(check.clone().add(0, 0, -1 * accuracy));
            holder.add(check.clone().add(0, 0, 1 * accuracy));
        }
        return holder;
    }

}