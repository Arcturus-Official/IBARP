# IBARP

IBARP is a plugin for Minecraft server that provides inventory backup and restore functionality for players

Native minecraft version: 1.7.10

## Features

- Create backups of player inventories using /inventorybackup command.
- Restore player inventories using /inv_backup_load <username> <backup_date> command.
- Serialization of inventory to backup file using NBT format.
- GZIP compression used to reduce backup file size.
- Automatic creation of backup folder on plugin enable.

## Example of backupPlayerInventory

```java
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
```

This method is called when a player runs the /inventorybackup command. It creates a backup file in the plugin's data folder with the player's username and timestamp as the file name. It then gets the player's inventory contents and serializes them to the backup file using NBT and GZIP compression.


If anyone is interested in contributing to IBARP or has some ideas to improve the plugin, I would be more than happy to discuss it.

## In development

- Tinker Construct support
