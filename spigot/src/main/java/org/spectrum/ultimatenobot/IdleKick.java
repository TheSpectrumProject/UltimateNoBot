package org.spectrum.ultimatenobot;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class IdleKick {

    private final UltimateNoBot plugin;  // 引用主插件类实例
    private final Map<Player, Vector> playerAngles = new HashMap<>();

    // 构造函数，接收主插件实例
    public IdleKick(UltimateNoBot plugin) {
        this.plugin = plugin;
    }

    // 启动视角检查任务
    public void start() {
        long interval = plugin.getIdleKickInterval();  // 获取检测间隔（单位：ticks）

        new BukkitRunnable() {
            @Override
            public void run() {
                checkPlayerAngles();
            }
        }.runTaskTimer(plugin, 0L, interval); // 使用配置的间隔
    }

    // 检查所有在线玩家的视角
    public void checkPlayerAngles() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 获取当前玩家的视角
            Vector currentAngle = player.getLocation().getDirection();

            // 获取玩家的上一次视角
            Vector lastPlayerAngle = playerAngles.get(player);

            // 如果是第一次检查，初始化视角
            if (lastPlayerAngle == null) {
                playerAngles.put(player, currentAngle);
                return;
            }

            // 计算上次和当前视角的夹角（以弧度为单位）
            double angleDifference = currentAngle.angle(lastPlayerAngle);

            // 如果夹角小于2度（转换成弧度）
            if (angleDifference < Math.toRadians(2)) {
                player.kickPlayer(plugin.getIdleKickMessage());  // 获取踢出消息

                // 清理该玩家的视角数据
                playerAngles.remove(player);
            }

            // 更新该玩家的视角
            playerAngles.put(player, currentAngle);
        }
    }
}
