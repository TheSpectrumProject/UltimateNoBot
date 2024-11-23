<?php
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // 读取 JSON 数据
    $input = file_get_contents('php://input');
    $data = json_decode($input, true); // 解码 JSON 数据

    if (isset($data['ip'])) {
        $userIp = $data['ip'];

        // 连接 Redis 数据库
        try {
            $redis = new Redis();
            $redis->connect('127.0.0.1', 6379);  // 连接 Redis 服务器
            

            // 查询 IP 地址是否存在
            $key = 'user_ip:' . $userIp;
            if ($redis->exists($key)) {
                echo json_encode(['exists' => true]);
            } else {
                echo json_encode(['exists' => false]);
            }
        } catch (Exception $e) {
            echo json_encode(['error' => $e->getMessage()]);
        }
    } else {
        echo json_encode(['error' => 'IP address not provided.']);
    }
} else {
    echo json_encode(['error' => 'Invalid request method.']);
}
?>
