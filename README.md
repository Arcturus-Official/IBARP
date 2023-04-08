# IBARP

This is a plugin for the Minecraft server that allows players' inventories to be backed up and restored. When a player runs the /inventorybackup command, a backup file is created in the plugin's data folder that contains the player's inventory. The plugin also provides a command /inv_backup_load <username> <backup_name> that allows a player's inventory to be restored from a backup file.

The plugin uses the NBT format to serialize the player's inventory to a file. It also uses GZIP compression to reduce the size of the backup files.

The onEnable method creates a folder for storing the backup files if it doesn't exist yet. It also registers the /inventorybackup and /inv_backup_load commands with the server's command system.

The backupPlayerInventory method is called when a player runs the /inventorybackup command. It gets the player's inventory contents and serializes them to a backup file using NBT and GZIP compression. The backup file is named with the player's username and a timestamp to make it unique.

The onCommand method handles both the /inventorybackup and /inv_backup_load commands. The /inventorybackup command creates a backup of the player's inventory and sends a confirmation message to the player. If the command is run from the console, it creates backups for all online players.

The /inv_backup_load command restores a player's inventory from a backup file. It takes two arguments: the username of the player to restore the inventory for and the name of the backup file to restore from. It loads the backup file from disk, deserializes the inventory using NBT, and sets the player's inventory to the restored items. It sends a confirmation message to the command sender if the restore was successful, or an error message if it failed.

The onTabComplete method is used to provide tab completion for the /inv_backup_load command. It suggests player names and backup file names based on the arguments passed so far.
