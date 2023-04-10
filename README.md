# IBARP

IBARP is a plugin for Minecraft server that provides inventory backup and restore functionality for players

Native minecraft version: 1.7.10

## Features

- Create backups of player inventories using /inventorybackup command. 
- If the command is used on the console, it creates a backup for all online players.
- Restore player inventories using /inv_backup_load <username> <backup_date> command.
- Serialization of inventory to backup file using NBT format.
- GZIP compression used to reduce backup file size.
- Automatic creation of backup folder on plugin enable.

This method backupPlayerInventory is called when a player runs the /inventorybackup command. It creates a backup file in the plugin's data folder with the player's username and timestamp as the file name. It then gets the player's inventory contents and serializes them to the backup file using NBT and GZIP compression.

If anyone is interested in contributing to IBARP or has some ideas to improve the plugin, I would be more than happy to discuss it.

## In development

- Modded items support
