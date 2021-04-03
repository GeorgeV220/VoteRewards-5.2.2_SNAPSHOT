package com.georgev22.voterewards.configmanager;

import com.georgev22.voterewards.utilities.Utils;
import com.google.common.collect.Sets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class CFG {

    private final static Set<CFG> cachedFiles = Sets.newHashSet();

    public static void reloadFiles() {
        cachedFiles.forEach(CFG::reloadFile);
    }

    /* The plugin instance. */
    private final JavaPlugin plugin;
    /* The file's name (without the .yml) */
    private final String fileName;

    /* The yml file configuration. */
    private FileConfiguration fileConfiguration;
    /* The file. */
    private File file;

    private final boolean saveResource;

    public CFG(final JavaPlugin plugin, final String string, final boolean saveResource) {
        this.plugin = plugin;
        this.fileName = string + ".yml";
        this.saveResource = saveResource;
        this.setup();

        cachedFiles.add(this);
    }

    /**
     * Attempts to load the file.
     *
     * @see #reloadFile()
     * @see JavaPlugin#saveResource(String, boolean)
     */
    public void setup() {
        if (!this.plugin.getDataFolder().exists()) {
            if (this.plugin.getDataFolder().mkdir()) {
                Utils.debug(plugin, "Folder " + this.plugin.getDataFolder().getName() + " has been created!");
            }
        }

        this.file = new File(this.plugin.getDataFolder(), this.fileName);

        if (!this.file.exists()) {
            try {
                if (this.file.createNewFile()) {
                    Utils.debug(plugin, "File " + this.file.getName() + " has been created!");
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            if (saveResource) {
                this.plugin.saveResource(this.fileName, true);
            }
        }

        this.reloadFile();
    }

    /**
     * Saves the file configuration.
     *
     * @see FileConfiguration#save(File)
     * @see #getFileConfiguration()
     */
    public void saveFile() {
        try {
            this.getFileConfiguration().save(this.file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the file.
     *
     * @see YamlConfiguration#loadConfiguration(File)
     * @see #file
     */
    public void reloadFile() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * @return the file - The {@link FileConfiguration}.
     */
    public FileConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }

    /**
     * Get the file
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CFG other = (CFG) obj;
        if (fileName == null) {
            return other.fileName == null;
        } else return fileName.equals(other.fileName);
    }
}