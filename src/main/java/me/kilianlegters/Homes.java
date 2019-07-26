package me.kilianlegters;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Homes implements CommandExecutor {

    private DataManager dataManager;

    public Homes(DataManager dataManager){
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            Player player = (Player) commandSender;

            if (!hasPermission(player)){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (strings.length == 0) {

                if (s.equalsIgnoreCase("home")) {
                    player.spigot().sendMessage(getHomesMessage(player));
                    return true;
                }

                if (s.equalsIgnoreCase("delhome")) {
                    player.spigot().sendMessage(getDelHomesMessage(player));
                    return true;
                }

                player.sendMessage(ChatColor.RED + "Add a home-name behind your command.");
                return true;
            }

            if (strings.length == 1){
                if (s.equalsIgnoreCase("sethome")) {
                    return setHome(player, strings[0].toLowerCase());
                }

                if (s.equalsIgnoreCase("home")) {
                    return tryTeleportHome(player, strings[0].toLowerCase());
                }

                if (s.equalsIgnoreCase("delhome")) {
                    return delHome(player, strings[0].toLowerCase());
                }
            }

            player.sendMessage(ChatColor.RED + "Unknown syntax.");
            return false;

        } else {
            commandSender.sendMessage(ChatColor.RED + "Only for in-game use.");
            return true;
        }
    }

    private boolean hasPermission(Player player){
        for (PermissionAttachmentInfo permissionAttachmentInfo: player.getEffectivePermissions()){
            if (permissionAttachmentInfo.getPermission().contains("reallightweighthomes")){
                return true;
            }
        }
        return false;
    }

    private List<String> getHomes(Player player){
        List<String> holder = new ArrayList<>();
        for (String key: dataManager.getData().getConfigurationSection(player.getUniqueId().toString()).getKeys(false)){
            holder.add(key);
        }
        return holder;
    }

    private TextComponent getHomesMessage(Player player){
        TextComponent message = new TextComponent("Homes: ");
        message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        if (!hasHome(player)){
            TextComponent holder = new TextComponent("none");
            holder.setColor(net.md_5.bungee.api.ChatColor.RED);
            message.addExtra(holder);
        } else {
            int i = 0;
            for (String home: getHomes(player)) {
                TextComponent holder;
                if (i != 0) {
                    holder = new TextComponent(" ,");
                    holder.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                    message.addExtra(holder);
                }
                holder = new TextComponent(home);
                holder.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                holder.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/home " + home));
                holder.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to ").color(net.md_5.bungee.api.ChatColor.GREEN).append(home).color(net.md_5.bungee.api.ChatColor.GOLD).append(".").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
                message.addExtra(holder);
                i++;
            }
        }
        TextComponent holder = new TextComponent(".");
        holder.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        message.addExtra(holder);
        return message;
    }

    private TextComponent getDelHomesMessage(Player player){
        TextComponent message = new TextComponent();
        if (!hasHome(player)){
            TextComponent holder = new TextComponent("You have no home to delete");
            holder.setColor(net.md_5.bungee.api.ChatColor.RED);
            message.addExtra(holder);
        } else {
            TextComponent holder = new TextComponent("Delete home: ");
            holder.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            message.addExtra(holder);
            int i = 0;
            for (String home: getHomes(player)) {
                if (i != 0) {
                    holder = new TextComponent(" ,");
                    holder.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                    message.addExtra(holder);
                }
                holder = new TextComponent(home);
                holder.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                holder.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/delhome " + home));
                holder.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Delete ").color(net.md_5.bungee.api.ChatColor.GREEN).append(home).color(net.md_5.bungee.api.ChatColor.GOLD).append(".").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
                message.addExtra(holder);
                i++;
            }
        }
        TextComponent holder = new TextComponent(".");
        holder.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        message.addExtra(holder);
        return message;
    }

    private boolean hasHome(Player player, String home){
        return dataManager.getData().contains(player.getUniqueId().toString() + "." + home);
    }

    private boolean hasHome(Player player){
        return dataManager.getData().isConfigurationSection(player.getUniqueId().toString()) && dataManager.getData().getConfigurationSection(player.getUniqueId().toString()).getKeys(false).size() != 0;
    }

    private boolean delHome(Player player, String home){

        if (!hasHome(player, home)){
            player.sendMessage(ChatColor.RED + "You do not have a home with name: " + ChatColor.GOLD + home + ChatColor.RED + ".");
            return true;
        }

        dataManager.getData().set(player.getUniqueId().toString() + "." + home, null);
        //realLightWeightHomes.saveConfig();
        player.sendMessage(ChatColor.GREEN + "Home deleted.");
        return true;
    }

    private boolean tryTeleportHome(Player player, String home){
        if (!hasHome(player, home)){
            player.sendMessage(ChatColor.RED + "You do not yet have a home with name: " + ChatColor.GOLD + home + ChatColor.RED + ", use '/sethome " + ChatColor.GOLD + home + ChatColor.RED + "'.");
            return true;
        }
        Location homeLocation = getLocation(dataManager.getData().getString(player.getUniqueId().toString() + "." + home));
        if (!isHomeSafe(homeLocation)) {
            Location holder = getFastSafeHomeLocation(homeLocation);
            if (holder == null) {
                List<Location> queue = new ArrayList<>();
                queue.add(homeLocation);
                player.sendMessage(ChatColor.RED + "Your home is obstructed, looking for the nearest suitable alternative and bringing you there!");
                new CustomRunnable(this, player, home, homeLocation, queue, 1000).runTaskTimer(RealLightWeightHomes.realLightWeightHomes, 0, 1);
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

    private boolean setHome(Player player, String home){
        if (hasHome(player) && !(getHomes(player).size() < getMaxHomes(player))){
            player.sendMessage(ChatColor.RED + "You have already set your max amount of homes, remove one before setting any more.");
            return true;
        }
        if (hasHome(player, home)){
            player.sendMessage(ChatColor.RED + "You already have a home named: " + ChatColor.GOLD + home + ChatColor.RED + ", use a different name or use '/delhome " + ChatColor.GOLD + home + ChatColor.RED + "' first.");
            return true;
        }
        return setHome(player, home, player.getLocation(), false);
    }

    public boolean setHome(Player player, String home, Location location, boolean forced){
        dataManager.getData().set(player.getUniqueId().toString() + "." + home, getString(location));
        //realLightWeightHomes.saveConfig();
        if (forced){
            player.sendMessage(ChatColor.GREEN + "Your home was automatically set!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Home set! You can set " + ChatColor.GOLD + (getMaxHomes(player) - getHomes(player).size()) + ChatColor.GREEN + " more homes.");
        }
        return true;
    }

    private int getMaxHomes(Player player){
        int holder = 1;
        if (player.hasPermission(RealLightWeightHomes.realLightWeightHomes.getName() + "." + 100)){
            holder = 100;
            Bukkit.getLogger().warning("Player has more than 100 available homes, hard-cap is 100.");
        } else {
            for (int i = 1; i < 100; i++) {
                if (player.hasPermission(RealLightWeightHomes.realLightWeightHomes.getName() + "." + i)) {
                    holder = i;
                }
            }
        }
        return holder;
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