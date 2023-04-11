package arcturus.network.ibarp;

import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.TConstructRegistry;
import tconstruct.tools.TinkerTools;






import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InventoryLoadCommand implements CommandExecutor {

    private final BackupUtils backupUtils;
    private final String PERMISSION_LOAD_BACKUP = "ibarp.loadbackup";

    public InventoryLoadCommand(BackupUtils backupUtils) {
        this.backupUtils = backupUtils;
    }

    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION_LOAD_BACKUP)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        switch (cmd.getName().toLowerCase()) {
            case "inv_backup_load":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /inv_backup_load <username> <backup_name>");
                    return true;
                }
                String playerName = args[0];
                String backupName = args[1];
                File backupFile = backupUtils.getBackupFile(playerName, backupName);
                if (!Objects.requireNonNull(backupFile).exists()) {
                    sender.sendMessage(ChatColor.RED + "Backup file not found!");
                    return true;
                }
                try (DataInputStream dis = new DataInputStream(Files.newInputStream(backupFile.toPath()))) {
                    NBTTagCompound root = NBTCompressedStreamTools.a((InputStream) dis);
                    NBTTagList itemList = root.getList("Inventory", 10);
                    ItemStack[] items = new ItemStack[itemList.size()];
                    for (int i = 0; i < itemList.size(); i++) {
                        NBTTagCompound itemTag = itemList.get(i);
                        short id = itemTag.getShort("id");
                        byte count = itemTag.getByte("Count");
                        short damage = itemTag.getShort("Damage");
                        ItemStack item = new ItemStack(id, count, damage);

                        if (itemTag.hasKey("tag")) {
                            NBTTagCompound tag = itemTag.getCompound("tag");
                            if (tag.hasKey("ench")) {
                                NBTTagList enchantList = tag.getList("ench", 10);
                                for (int j = 0; j < enchantList.size(); j++) {
                                    NBTTagCompound enchantTag = enchantList.get(j);
                                    int enchantId = enchantTag.getShort("id");
                                    int enchantLevel = enchantTag.getShort("lvl");
                                    item.addUnsafeEnchantment(Enchantment.getById(enchantId), enchantLevel);
                                }
                            }
                            if (tag.hasKey("CustomPotionEffects")) {
                                NBTTagList effectList = tag.getList("CustomPotionEffects", 10);
                                PotionMeta meta = (PotionMeta) item.getItemMeta();
                                for (int j = 0; j < effectList.size(); j++) {
                                    NBTTagCompound effectTag = effectList.get(j);
                                    PotionEffectType type = PotionEffectType.getById(effectTag.getByte("Id"));
                                    int amplifier = effectTag.getByte("Amplifier");
                                    int duration = effectTag.getInt("Duration");
                                    boolean ambient = effectTag.getBoolean("Ambient");
                                    PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient);
                                    meta.addCustomEffect(effect, true);
                                }
                                item.setItemMeta(meta);
                            }
                        }
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
}
