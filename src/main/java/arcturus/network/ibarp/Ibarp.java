package arcturus.network.ibarp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.io.*;

public final class Ibarp extends JavaPlugin {

    private InventoryBackup inventoryBackup;
    private File backupFolder;

    // Store the latest inventory backup for each player
    private final HashMap<String, File> latestBackups = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        inventoryBackup = new InventoryBackup(getDataFolder());
        getLogger().info("Ibarp plugin has been enabled.");
        Bukkit.getConsoleSender().sendMessage("=========================");
        Bukkit.getConsoleSender().sendMessage("IBARP");
        Bukkit.getConsoleSender().sendMessage("Version " + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("Author: Avalanche7CZ");
        Bukkit.getConsoleSender().sendMessage("=========================");


        backupFolder = new File(getDataFolder(), "backups");
        if (!backupFolder.exists()) {
            if (!backupFolder.mkdirs()) {
                getLogger().warning("Failed to create backup folder!");
            }
        }
        BackupUtils backupUtils = new BackupUtils(backupFolder, getLogger());
        getCommand("inventorybackup").setExecutor(new InventoryBackupCommand(inventoryBackup));
        getCommand("inv_backup_load").setExecutor(new InventoryLoadCommand(backupUtils));
    }

    @Override
    public void onDisable() {
        if (!this.isEnabled()) {
            getLogger().warning("Ibarp plugin has not been enabled. Shutting down server.");
            Bukkit.shutdown();
        } else {
            getLogger().info("Ibarp plugin has been disabled.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("inv_backup_load")) {
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
                    for (File file : Objects.requireNonNull(playerFolder.listFiles())) {
                        if (file.isFile() && file.getName().endsWith(".dat.gz")) {
                            backupNames.add(file.getName().replace(".dat.gz", ""));
                        }
                    }
                    return backupNames;
                }
            }
        }
        return Collections.emptyList();
    }
}




