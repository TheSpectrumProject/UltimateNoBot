package org.spectrum.ultimatenobot;

import org.bukkit.plugin.java.JavaPlugin;

public class UltimateNoBot extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 如果没有配置文件，则创建一个默认的config.yml
        // 创建IPLoginLimiter实例并注册事件
        getServer().getPluginManager().registerEvents(new IPLoginLimiter(this), this);
        // 注册玩家加入事件监听器
        getServer().getPluginManager().registerEvents(new GameJoinCheck(this), this);

        getServer().getPluginManager().registerEvents(new LoginInterval(this), this);
        getLogger().info("IPLoginLimitPlugin 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("IPLoginLimitPlugin 已禁用！");
    }
}
