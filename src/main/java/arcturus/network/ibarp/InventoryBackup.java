package arcturus.network.ibarp;


import org.apache.logging.log4j.Logger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagString;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;
import java.io.File;

public class InventoryBackup {

    private File backupFolder;
    private Logger logger;
    private Map<String, File> latestBackups;

    public InventoryBackup(File backupFolder) {
        this.backupFolder = backupFolder;
        this.latestBackups = new HashMap<>();
    }

    public void backupPlayerInventory(Player player) {
        String playerName = player.getName();
        ItemStack[] items = player.getInventory().getContents();

        LocalDateTime now = LocalDateTime.now();
        String playerBackupFolderName = backupFolder + File.separator + "backups" + File.separator + playerName;
        File playerFolder = new File(playerBackupFolderName);

        if (!playerFolder.exists()) {
            if (!playerFolder.mkdirs()) {
                logger.warn("Failed to create backup folder for player " + playerName + "!");
                return;
            }
        }

        String filename = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".dat.gz";
        File backupFile = new File(playerFolder, filename);

        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(backupFile))) {
            NBTTagCompound root = new NBTTagCompound();
            NBTTagList itemList = new NBTTagList();

            // Backup inventory contents
            for (ItemStack item : items) {
                if (item != null) {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    CraftItemStack craftItemStack = (CraftItemStack) item;
                    net.minecraft.server.v1_7_R4.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(craftItemStack);
                    nmsItemStack.save(itemTag);

                    int damage = item.getDurability();
                    if (damage > 0) {
                        itemTag.setShort("Damage", (short) damage);
                    }

                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();

                        if (meta.hasDisplayName()) {
                            itemTag.setString("display.Name", meta.getDisplayName());
                        }

                        if (meta.hasLore()) {
                            NBTTagList loreList = new NBTTagList();
                            for (String lore : meta.getLore()) {
                                loreList.add(new NBTTagString(lore));
                            }
                            itemTag.set("display.Lore", loreList);
                        }

                        if (meta instanceof PotionMeta) {
                            PotionMeta potionMeta = (PotionMeta) meta;
                            List<PotionEffect> effects = potionMeta.getCustomEffects();
                            if (!effects.isEmpty()) {
                                NBTTagList effectList = new NBTTagList();
                                for (PotionEffect effect : effects) {
                                    NBTTagCompound effectTag = new NBTTagCompound();
                                    effectTag.setByte("Id", (byte) effect.getType().getId());
                                    effectTag.setByte("Amplifier", (byte) effect.getAmplifier());
                                    effectTag.setInt("Duration", effect.getDuration());
                                    effectTag.setBoolean("Ambient", effect.isAmbient());
                                    effectList.add(effectTag);
                                }
                                itemTag.set("CustomPotionEffects", effectList);
                            }
                        }

                        if (meta instanceof EnchantmentStorageMeta) {
                            EnchantmentStorageMeta esMeta = (EnchantmentStorageMeta) meta;
                            Map<Enchantment, Integer> enchantments = esMeta.getStoredEnchants();
                            if (!enchantments.isEmpty()) {
                                NBTTagList enchantmentList = new NBTTagList();
                                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                                    Enchantment ench = entry.getKey();
                                    Integer level = entry.getValue();
                                    NBTTagCompound enchTag = new NBTTagCompound();
                                    enchTag.setShort("id", (short) ench.getId());
                                    enchTag.setShort("lvl", level.shortValue());
                                    enchantmentList.add(enchTag);
                                }
                                itemTag.set("Enchantments", enchantmentList);
                            }
                        } else {
                            Map<Enchantment, Integer> enchantments = item.getEnchantments();
                            if (!enchantments.isEmpty()) {
                                NBTTagList enchantmentList = new NBTTagList();
                                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                                    Enchantment ench = entry.getKey();
                                    Integer level = entry.getValue();
                                    NBTTagCompound enchTag = new NBTTagCompound();
                                    enchTag.setShort("id", (short) ench.getId());
                                    enchTag.setShort("lvl", level.shortValue());
                                    enchantmentList.add(enchTag);
                                }
                                itemTag.set("Enchantments", enchantmentList);
                            }
                        }
                    }


                    int armor = item.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                    if (armor > 0) {
                        itemTag.setInt("generic.armor", armor);
                    }

                    itemList.add(itemTag);
                }
            }

            root.set("Inventory", itemList);
            NBTCompressedStreamTools.a(root, (DataOutput) new DataOutputStream(gos));

            // Update the latest backup file for the player
            latestBackups.put(playerName, backupFile);

        } catch (IOException e) {
            logger.warn("Failed to backup inventory for player " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.warn("An unexpected error occurred while backing up the inventory for player " + playerName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

