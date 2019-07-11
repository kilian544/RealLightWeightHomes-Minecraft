package me.kilianlegters;

import com.oracle.deploy.update.UpdateCheck;
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

import javax.jnlp.DownloadService;
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
                    return teleportHome(player);
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

    private boolean teleportHome(Player player){
        if (!hasHome(player)){
            player.sendMessage(ChatColor.RED + "You do not yet have a home, use '/sethome'.");
            return false;
        }

        Location homeLocation = getLocation(realLightWeightHomes.getConfig().getString(player.getUniqueId().toString());

        if (!isHomeSafe(homeLocation)) {
            makeHomeLocationSafe(homeLocation);
        }

        player.teleport(homeLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
        player.sendMessage(ChatColor.GREEN + "Teleporting to home...");
        return true;
    }

    private boolean setHome(Player player){
        if (hasHome(player)){
            player.sendMessage(ChatColor.RED + "You already have a home, use '/delhome' first.");
            return false;
        }

        realLightWeightHomes.getConfig().set(player.getUniqueId().toString(), getString(player.getLocation()));
        realLightWeightHomes.saveConfig();
        player.sendMessage(ChatColor.GREEN + "Home set!");
        return true;
    }

    private Location getLocation(String string){
        String[] strings = string.split("/");
        return new Location(Bukkit.getWorld(UUID.fromString(strings[0])), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]), Float.parseFloat(strings[4]), Float.parseFloat(strings[5])).add(.5, .2, .5);
    }

    private String getString(Location location){
        return location.getWorld().getUID().toString() + "/" + location.getBlockX() + "/" + location.getBlockY() + "/" + location.getBlockZ() + "/" + location.getYaw() + "/" + location.getPitch();
    }

    private Boolean makeHomeLocationSafe(Location location){
        return false;
    }

    private Boolean isHomeSafe(Location location){
        Block lowerBlock = location.getBlock();
        Block upperBlock = lowerBlock.getRelative(BlockFace.UP);
        Block blockBeneath = lowerBlock.getRelative(BlockFace.DOWN);
        return lowerBlock.isPassable() && lowerBlock.getType() != Material.LAVA && upperBlock.isPassable() && upperBlock.getType() != Material.LAVA;
    }

}