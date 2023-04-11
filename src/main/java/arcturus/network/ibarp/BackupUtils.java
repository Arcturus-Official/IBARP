package arcturus.network.ibarp;

import java.io.File;
import java.util.logging.Logger;

public class BackupUtils {
    private final File backupFolder;
    private final Logger logger;

    public BackupUtils(File backupFolder, Logger logger) {
        this.backupFolder = backupFolder;
        this.logger = logger;
    }

    public File getBackupFile(String playerName, String backupTime) {
        String folderName = backupFolder.getPath() + File.separator + playerName;
        File playerFolder = new File(folderName);
        if (!playerFolder.exists()) {
            logger.warning("No backup found for player " + playerName + "!");
            return null;
        }
        String filename = backupTime + ".dat.gz";
        File backupFile = new File(playerFolder, filename);
        if (!backupFile.exists()) {
            logger.warning("No backup found for player " + playerName + " at time " + backupTime + "!");
            return null;
        }

        return backupFile;
    }
}



