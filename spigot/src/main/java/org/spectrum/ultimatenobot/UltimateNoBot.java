package org.spectrum.ultimatenobot;

import org.bukkit.plugin.java.JavaPlugin;

public class UltimateNoBot extends JavaPlugin {

    private long MIN_LOGIN_INTERVAL;
    private String apiUrl;
    private String captchaMessage;
    private String intervalMessage;
    private String maxLoginKickMessage;
    private int maxPlayersPerIP;
    private int TIME_LIMIT;
    private long idleKickInterval;  // 检测间隔（单位：秒）
    private String idleKickMessage; // 踢出消息

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 如果没有配置文件，则创建一个默认的config.yml
        loadConfig();  // 加载配置

        // 获取配置文件中是否启用各个监听器
        boolean enablePlayerLoginCheck = getConfig().getBoolean("enablePlayerLoginCheck", true);
        boolean enableIPLoginLimiter = getConfig().getBoolean("enableIPLoginLimiter", true);
        boolean enableIdleKick = getConfig().getBoolean("enableIdleKick", true);

        // 根据配置启用子类
        if (enablePlayerLoginCheck) {
            getServer().getPluginManager().registerEvents(new PlayerLoginCheck(this), this);
        }
        if (enableIPLoginLimiter) {
            getServer().getPluginManager().registerEvents(new IPLoginLimiter(this), this);
        }
        if (enableIdleKick) {
            new IdleKick(this).start();
        }

        getLogger().info("UltimateNoBot enabled！");
        if (apiUrl == "https://unb.spectra.us.kg:10086/check.php") {
            getLogger().warning("Using Public API! Please note that the public API is limited by the rate and cannot cope with large-scale attacks. Please build your own authentication service or use the paid API");
        }
    }

    // 加载配置
    private void loadConfig() {
        // 主检测
        MIN_LOGIN_INTERVAL = getConfig().getInt("login-interval", 10) * 1000L;  // 从 config.yml 获取登录间隔并转换为毫秒
        apiUrl = getConfig().getString("api_url", "http://127.0.0.1:8080/validatePlayer");
        captchaMessage = getConfig().getString("captcha-message", "Captcha Message");
        intervalMessage = getConfig().getString("interval-message", "Interval Message");
        TIME_LIMIT = getConfig().getInt("bypass-time", 3600);  // 默认 3600 秒
        // 最大限制
        maxLoginKickMessage = getConfig().getString("max-login-kick-message", "Max Login");
        maxPlayersPerIP = getConfig().getInt("max-players-per-ip", 2);

        // 新增配置项：idleKickInterval 和 idleKickMessage
        idleKickInterval = getConfig().getInt("idle-kick-interval", 60) * 20L;  // 配置中单位是秒，转换为ticks（1秒 = 20 ticks）
        idleKickMessage = getConfig().getString("idle-kick-message", "IDLE Kick");
    }

    // 配置 getter 方法
    public String getMaxLoginKickMessage() {
        return maxLoginKickMessage;
    }

    public int getMaxPlayersPerIP() {
        return maxPlayersPerIP;
    }

    public long getMinLoginInterval() {
        return MIN_LOGIN_INTERVAL;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getCaptchaMessage() {
        return captchaMessage;
    }

    public String getIntervalMessage() {
        return intervalMessage;
    }

    public int getTimeLimit() {
        return TIME_LIMIT;
    }

    public long getIdleKickInterval() {
        return idleKickInterval;  // 返回检测间隔（单位：ticks）
    }

    public String getIdleKickMessage() {
        return idleKickMessage;  // 返回踢出消息
    }

    @Override
    public void onDisable() {
        getLogger().info("IPLoginLimitPlugin 已禁用！");
    }
}
