package org.spectrum.ultimatenobot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class IPLoginLimiter implements Listener {

    private final Map<String, Integer> ipLoginCount = new HashMap<>();
    private final int maxPlayersPerIP;
    private final String maxLoginKickMessage;

    // 通过主类获取配置
    public IPLoginLimiter(UltimateNoBot plugin) {
        // 使用主类提供的 getter 方法获取配置
        this.maxPlayersPerIP = plugin.getMaxPlayersPerIP();
        this.maxLoginKickMessage = plugin.getMaxLoginKickMessage();
    }

    // 监听玩家登录事件
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerIp = event.getRealAddress().getHostName();  // 获取玩家IP
        ipLoginCount.putIfAbsent(playerIp, 0);  // 如果该IP还没有记录，初始化为0
        int currentCount = ipLoginCount.get(playerIp);  // 获取当前IP的登录人数

        if (currentCount >= maxPlayersPerIP) {
            // 如果该IP已经达到最大玩家数，拒绝新玩家登录，并显示自定义的踢出消息
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, maxLoginKickMessage);
        } else {
            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                ipLoginCount.put(playerIp, currentCount + 1);
            }
        }
    }

    // 监听玩家退出事件
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String playerIp = event.getPlayer().getAddress().getAddress().getHostName();  // 获取玩家IP
        if (ipLoginCount.containsKey(playerIp)) {
            int currentCount = ipLoginCount.get(playerIp);  // 获取当前IP的登录人数

            if (currentCount > 0) {
                // 减少该IP的登录人数
                ipLoginCount.put(playerIp, currentCount - 1);
            }
        }
    }
}
