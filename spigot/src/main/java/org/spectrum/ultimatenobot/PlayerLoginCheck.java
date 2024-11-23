package org.spectrum.ultimatenobot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class PlayerLoginCheck implements Listener {

    // 存储玩家最后登录时间（单位：毫秒）
    private final Map<String, Long> playerLoginTimes = new HashMap<>();
    private static long MIN_LOGIN_INTERVAL = 10000L; // 最小登录间隔（10秒）
    private static int TIME_LIMIT = 3600;  // 最低游戏时长，单位为秒
    private final JavaPlugin plugin;
    private String apiUrl;  // 存储 API 地址

    // 构造函数，传递插件实例
    public PlayerLoginCheck(JavaPlugin plugin) {
        this.plugin = plugin;
        MIN_LOGIN_INTERVAL = plugin.getConfig().getLong("login-interval", 10000L);  // 从 config.yml 获取登录间隔
        apiUrl = plugin.getConfig().getString("api_url");

        // 确保 api_url 存在
        if (apiUrl == null || apiUrl.isEmpty()) {
            plugin.getLogger().warning("API 地址未在 config.yml 中配置，使用默认地址");
            apiUrl = "http://127.0.0.1:8080/validatePlayer";  // 默认地址
        }

        TIME_LIMIT = plugin.getConfig().getInt("bypass-time", 3600);  // 默认 3600 秒
    }

    // 玩家登录事件
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerIp = event.getRealAddress().getHostName();
        long currentTime = System.currentTimeMillis();
        Player player = event.getPlayer();

        // 检查登录间隔
        if (playerLoginTimes.containsKey(playerIp)) {
            long lastLoginTime = playerLoginTimes.get(playerIp);
            if ((currentTime - lastLoginTime) < MIN_LOGIN_INTERVAL) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You must wait at least " + MIN_LOGIN_INTERVAL / 1000 + " seconds before logging in again.");
                return;
            }
        }

        // 检查游戏时间
        long playTime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 20; // 换算为秒
        if (playTime < TIME_LIMIT) {
            try {
                // 创建JSON请求体
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("ip", event.getRealAddress().getHostName());

                // 创建URL连接对象
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // 写入请求体
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // 获取响应
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean isValid = jsonResponse.getBoolean("exists");

                    if (!isValid) {
                        // 如果验证失败，阻止玩家登录
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "游戏时间不足");
                    }
                } else {
                    plugin.getLogger().warning("请求失败，返回码: " + responseCode);
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "无法验证您的信息，请稍后再试。");
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "发生错误，无法验证您的信息。");
            }
        }

        // 更新玩家的最后登录时间
        playerLoginTimes.put(playerIp, currentTime);
    }
}
