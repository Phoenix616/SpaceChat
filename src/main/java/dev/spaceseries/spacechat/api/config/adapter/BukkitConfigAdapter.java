package dev.spaceseries.spacechat.api.config.adapter;

import dev.spaceseries.spacechat.api.config.generic.adapter.ConfigurationAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BukkitConfigAdapter implements ConfigurationAdapter {
    private final Plugin plugin;
    private final File file;
    private YamlConfiguration configuration;

    public BukkitConfigAdapter(Plugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        reload();
    }

    @Override
    public void reload() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        try (InputStream in = plugin.getResource(this.file.getName())) {
            if (in != null) {
                this.configuration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in)));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load " + this.file.getName() + " from jar file!");
        }
    }

    @Override
    public String getString(String path) {
        return this.configuration.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return this.configuration.getString(path, def);
    }

    @Override
    public int getInteger(String path) {
        return this.configuration.getInt(path);
    }

    @Override
    public int getInteger(String path, int def) {
        return this.configuration.getInt(path, def);
    }

    @Override
    public boolean getBoolean(String path) {
        return this.configuration.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return this.configuration.getBoolean(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return this.configuration.getStringList(path);
    }

    @Override
    public List<String> getStringList(String path, List<String> def) {
        return this.configuration.isSet(path) ? this.configuration.getStringList(path) : def;
    }

    @Override
    public List<String> getKeys(String path) {
        return getKeys(path, new ArrayList<>());
    }

    @Override
    public List<String> getKeys(String path, List<String> def) {
        ConfigurationSection section = this.configuration.getConfigurationSection(path);
        if (section == null) {
            return def;
        }

        Set<String> keys = section.getKeys(false);
        return new ArrayList<>(keys);
    }

    @Override
    public Map<String, String> getStringMap(String path) {
        return getStringMap(path, new HashMap<>());
    }

    @Override
    public Map<String, String> getStringMap(String path, Map<String, String> def) {
        Map<String, String> map = new HashMap<>();
        ConfigurationSection section = this.configuration.getConfigurationSection(path);
        if (section == null) {
            return def;
        }

        for (String key : section.getKeys(false)) {
            map.put(key, section.getString(key));
        }

        return map;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}