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
import org.json.JSONObject;

public class GameJoinCheck implements Listener {

    private static int TIME_LIMIT = 3600;
    private final JavaPlugin plugin;
    private String apiUrl;  // 存储 API 地址

    // 构造函数，接收 JavaPlugin 实例
    public GameJoinCheck(JavaPlugin plugin) {
        this.plugin = plugin;
        // 从 config.yml 读取 API 地址
        this.apiUrl = plugin.getConfig().getString("api_url");

        // 确保 api_url 存在
        if (apiUrl == null || apiUrl.isEmpty()) {
            plugin.getLogger().warning("API 地址未在 config.yml 中配置，使用默认地址");
            apiUrl = "http://127.0.0.1:8080/validatePlayer";  // 默认地址
        }
        TIME_LIMIT = plugin.getConfig().getInt("bypass-time", 3600);  // 默认为 3600 秒
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // 获取玩家的游戏时间，假设它是以秒为单位的 
        long playTime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) / 20; // 换算为秒

        // 如果游戏时间小于设定的时间限制，发送请求
        if (playTime < TIME_LIMIT) {
            try {
                // 创建JSON请求体
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("ip", event.getRealAddress().getHostName());

                // 创建URL连接对象
                URL url = new URL(apiUrl);  // 使用从配置中获取的 API 地址
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
                    // 假设返回的响应是一个简单的布尔值（例如 {"valid": true}）
                    String response = new String(connection.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean isValid = jsonResponse.getBoolean("exists");

                    if (!isValid) {
                        // 如果返回值为 false，则调用 event.disallow() 阻止玩家加入
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "游戏时间不足");
                    }
                } else {
                    // 处理请求失败的情况
                    plugin.getLogger().warning("请求失败，返回码: " + responseCode);
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "无法验证您的信息，请稍后再试。");
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "发生错误，无法验证您的信息。");
            }
        }
    }
}
