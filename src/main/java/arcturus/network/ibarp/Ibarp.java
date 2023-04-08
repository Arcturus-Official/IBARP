package arcturus.network.ibarp;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.io.DataOutputStream;






public final class Ibarp extends JavaPlugin implements CommandExecutor {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
    private File backupFolder;

    // Store the latest inventory backup for each player
    private final HashMap<String, File> latestBackups = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Ibarp plugin has been enabled.");
        backupFolder = new File(getDataFolder(), "backups");
        if (!backupFolder.exists()) {
            if (!backupFolder.mkdirs()) {
                getLogger().warning("Failed to create backup folder!");
            }
        }
        getCommand("inventorybackup").setExecutor(this);
        getCommand("inv_backup_load").setExecutor(this);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Ibarp plugin has been disabled.");
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        } else if (args.length == 2) {
            String playerName = args[0];
            File playerFolder = new File(backupFolder.getPath() + File.separator + playerName);
            if (playerFolder.exists() && playerFolder.isDirectory()) {
                List<String> backupNames = new ArrayList<>();
                for (File file : playerFolder.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".dat.gz")) {
                        backupNames.add(file.getName().replace(".dat.gz", ""));
                    }
                }
                return backupNames;
            }
        }
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "inventorybackup":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    backupPlayerInventory(player);
                    player.sendMessage("Your inventory has been backed up.");
                } else if (sender instanceof ConsoleCommandSender) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        backupPlayerInventory(player);
                    }
                    sender.sendMessage("Inventories of all online players have been backed up.");
                } else {
                    sender.sendMessage("This command can only be run by players or the console.");
                }
                return true;
            case "inv_backup_load":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /inv_backup_load <username> <backup_name>");
                    return true;
                }
                String playerName = args[0];
                String backupName = args[1];
                File backupFile = getBackupFile(playerName, backupName);
                if (!backupFile.exists()) {
                    sender.sendMessage(ChatColor.RED + "Backup file not found!");
                    return true;
                }
                try (DataInputStream dis = new DataInputStream(new FileInputStream(backupFile))) {
                    NBTTagCompound root = NBTCompressedStreamTools.a((InputStream) dis);
                    NBTTagList itemList = root.getList("Inventory", 10);
                    ItemStack[] items = new ItemStack[itemList.size()];
                    for (int i = 0; i < itemList.size(); i++) {
                        NBTTagCompound itemTag = itemList.get(i);
                        short id = itemTag.getShort("id");
                        byte count = itemTag.getByte("Count");
                        short damage = itemTag.getShort("Damage");
                        ItemStack item = new ItemStack(id, count, damage);
                        items[i] = item;
                    }
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        player.getInventory().setContents(items);
                        sender.sendMessage(ChatColor.GREEN + "Inventory restored for player " + playerName + " from backup " + backupName + ".");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player not found on server!");
                    }
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Failed to load backup file: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "An unexpected error occurred while loading the backup file: " + e.getMessage());
                    e.printStackTrace();
                }
                return true;

            default:
                return false;
        }
    }


    private void backupPlayerInventory(Player player) {
        String playerName = player.getName();
        ItemStack[] items = player.getInventory().getContents();

        LocalDateTime now = LocalDateTime.now();
        String folderName = backupFolder + File.separator + playerName;
        File playerFolder = new File(folderName);

        if (!playerFolder.exists()) {
            if (!playerFolder.mkdirs()) {
                getLogger().warning("Failed to create backup folder for player " + playerName + "!");
                return;
            }
        }

        String filename = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".dat.gz";
        File backupFile = new File(playerFolder, filename);

        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(backupFile))) {
            NBTTagCompound root = new NBTTagCompound();
            NBTTagList itemList = new NBTTagList();

            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    CraftItemStack craftItemStack = (CraftItemStack) items[i];
                    net.minecraft.server.v1_7_R4.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(craftItemStack);
                    nmsItemStack.save(itemTag);
                    itemList.add(itemTag);
                }
            }

            root.set("Inventory", itemList);
            NBTCompressedStreamTools.a(root, (DataOutput) new DataOutputStream(gos));

            // Update the latest backup file for the player
            latestBackups.put(playerName, backupFile);

        } catch (IOException e) {
            getLogger().warning("Failed to backup inventory for player " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            getLogger().warning("An unexpected error occurred while backing up the inventory for player " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

        private File getBackupFile(String playerName, String backupTime) {
            String folderName = backupFolder.getPath() + File.separator + playerName;
            File playerFolder = new File(folderName);
            if (!playerFolder.exists()) {
                getLogger().warning("No backup found for player " + playerName + "!");
                return null;
            }
            String filename = backupTime + ".dat.gz";
            File backupFile = new File(playerFolder, filename);
            if (!backupFile.exists()) {
                getLogger().warning("No backup found for player " + playerName + " at time " + backupTime + "!");
                return null;
            }

            return backupFile;
        }
    }


