package com.windstudio.discordwl.bot.Whitelist;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class DataStorage {
    private final FileConfiguration config;

    private final File file;

    public DataStorage(String path) {
        this.file = new File(path);
        try {
            this.file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void setDefault(String key, Object value) {
        if (!this.config.contains(key)) {
            this.config.set(key, value);
            writeToFile();
        }
    }

    public void writeToFile() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void load();

    public abstract void save();
}