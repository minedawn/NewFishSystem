package wiki.dawn.newfishsystem;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class ConfigReader {

    private final NewFishSystem plugin;
    private File configFile;
    private FileConfiguration config;
    public Map<ItemStack, Integer> items = new HashMap<>();
    private boolean isEnabledInAllWorlds = true;
    private boolean isCustomItemsEnabled = false;
    public List<String> worlds = new ArrayList<>();
    public List<String> fish_success_messages = new ArrayList<>();
    public List<String> fish_failure_messages = new ArrayList<>();
    public List<String> fish_caught_messages = new ArrayList<>();
    private int totalWeight;

    public ConfigReader(NewFishSystem plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void loadItemConfig() {

        // 获取items配置段
        ConfigurationSection itemsSection = config.getConfigurationSection("items");

        if (itemsSection != null) {
            // 遍历处理每个物品配置
            for (String itemId : itemsSection.getKeys(false)) {
                int weight = itemsSection.getInt(itemId);
                Material material = Material.matchMaterial(itemId);
                if (material == null) {
                    plugin.getLogger().warning("[原钓] >> 配置文件config.yml中，自定义物品" + itemId + "不存在！");
                } else {
                    items.put(new ItemStack(material, 1), weight);
                    setTotalWeight(getTotalWeight() + weight);
                }
            }
        }
        if (getTotalWeight() == 0 || items.isEmpty()) {
            setIsEnabledInAllWorlds(false);
            config.set("isCustomItemsEnabled", "false");
            plugin.getLogger().warning("[原钓] >> 配置文件config.yml中，自定义物品无有效权重或自定义物品格式有误！");
        }
    }

    public List<String> getConfigList(String keyName) {
        return config.getStringList(keyName);
    }

    public String getConfig(String keyName) {
        return config.getString(keyName);
    }

    public void initialization() {
        loadConfig();
        setIsEnabledInAllWorlds(!Objects.equals(getConfig("isEnabledInAllWorlds"), "false"));
        if (!getIsEnabledInAllWorlds()) {
            worlds = getConfigList("world");
        }
        setIsCustomItemsEnabled(Objects.equals(getConfig("isCustomItemsEnabled"), "true"));
        if (getIsCustomItemsEnabled()) {
            loadItemConfig();
        }
        fish_success_messages = getConfigList("fish_success_messages");
        fish_failure_messages = getConfigList("fish_failure_messages");
        fish_caught_messages = getConfigList("fish_caught_messages");
    }

    public boolean getIsEnabledInAllWorlds() {
        return isEnabledInAllWorlds;
    }

    public void setIsEnabledInAllWorlds(boolean isEnabledInAllWorlds) {
        this.isEnabledInAllWorlds = isEnabledInAllWorlds;
    }

    public boolean getIsCustomItemsEnabled() {
        return isCustomItemsEnabled;
    }

    public void setIsCustomItemsEnabled(boolean isCustomItemsEnabled) {
        this.isCustomItemsEnabled = isCustomItemsEnabled;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public int getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }

    public void reloadConfig() {
        setTotalWeight(0);
        // 清空之前的配置和数据
        items.clear();
        worlds.clear();
        fish_success_messages.clear();
        fish_failure_messages.clear();
        fish_caught_messages.clear();
        // 重新加载配置文件和初始化属性
        initialization();
    }
}
