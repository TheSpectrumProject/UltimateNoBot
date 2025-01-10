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

    private final Map<String, Long> playerLoginTimes = new HashMap<>();
    private final JavaPlugin plugin;

    // 配置项
    private long MIN_LOGIN_INTERVAL;
    private String apiUrl;
    private String captchaMessage;
    private String intervalMessage;
    private int TIME_LIMIT;

    // 构造函数，传递父类插件实例
    public PlayerLoginCheck(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    // 加载配置
    private void loadConfig() {
        if (plugin instanceof UltimateNoBot) {
            UltimateNoBot ultimateNoBot = (UltimateNoBot) plugin;
            this.MIN_LOGIN_INTERVAL = ultimateNoBot.getMinLoginInterval();
            this.apiUrl = ultimateNoBot.getApiUrl();
            this.captchaMessage = ultimateNoBot.getCaptchaMessage();
            this.intervalMessage = ultimateNoBot.getIntervalMessage();
            this.TIME_LIMIT = ultimateNoBot.getTimeLimit();
        }
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
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, intervalMessage);
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
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, captchaMessage);
                    }
                } else {
                    plugin.getLogger().warning("API error! Response: " + responseCode);
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "API error! Try again later.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "API error! Try again later.");
            }
        }

        // 更新玩家的最后登录时间
        playerLoginTimes.put(playerIp, currentTime);
    }
}
