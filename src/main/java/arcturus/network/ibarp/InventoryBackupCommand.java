package arcturus.network.ibarp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class InventoryBackupCommand implements CommandExecutor {

    private final InventoryBackup inventoryBackup;

    public InventoryBackupCommand(InventoryBackup inventoryBackup) {
        this.inventoryBackup = inventoryBackup;
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "inventorybackup":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    inventoryBackup.backupPlayerInventory(player);
                    player.sendMessage("Your inventory has been backed up.");
                } else if (sender instanceof ConsoleCommandSender) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        inventoryBackup.backupPlayerInventory(player);
                    }
                    sender.sendMessage("Inventories of all online players have been backed up.");
                } else {
                    sender.sendMessage("This command can only be run by players or the console.");
                }
                return true;
            default:
                return false;
        }
    }
}

