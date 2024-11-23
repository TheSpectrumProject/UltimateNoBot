package org.spectrum.ultimatenobot;

import org.bukkit.plugin.java.JavaPlugin;

public class UltimateNoBot extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 如果没有配置文件，则创建一个默认的config.yml
        getServer().getPluginManager().registerEvents(new PlayerLoginCheck(this), this);
        getServer().getPluginManager().registerEvents(new IPLoginLimiter(this), this);
        getLogger().info("IPLoginLimitPlugin 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("IPLoginLimitPlugin 已禁用！");
    }
}
