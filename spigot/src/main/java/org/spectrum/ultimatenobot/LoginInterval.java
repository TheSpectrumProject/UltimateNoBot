package org.spectrum.ultimatenobot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class LoginInterval implements Listener {

    // 存储玩家最后登录时间（单位：毫秒）
    private final Map<String, Long> playerLoginTimes = new HashMap<>();
    private static long MIN_LOGIN_INTERVAL = 10000L; // 最小登录间隔（10秒）

    // 构造函数：传递插件实例
    public LoginInterval(JavaPlugin plugin) {
        MIN_LOGIN_INTERVAL = plugin.getConfig().getLong("login-interval", 5L);
    }

    // 玩家登录事件
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerIp = event.getRealAddress().getHostName();

        // 获取当前时间
        long currentTime = System.currentTimeMillis();

        // 检查玩家是否有上次登录时间记录
        if (playerLoginTimes.containsKey(playerIp)) {
            long lastLoginTime = playerLoginTimes.get(playerIp);

            // 如果上次登录和此次登录的间隔小于10秒，阻止登录
            if ((currentTime - lastLoginTime) < MIN_LOGIN_INTERVAL) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You must wait at least 10 seconds before logging in again.");
                return;
            }
        }

        // 更新玩家的最后登录时间
        playerLoginTimes.put(playerIp, currentTime);
    }
}
