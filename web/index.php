<?php
// 如果用户已通过hCaptcha验证，处理验证结果
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $secretKey = 'YOUR_SECRET_KEY_HERE';  // 替换为你的hCaptcha secret key
    $response = $_POST['h-captcha-response'];  // 获取hCaptcha的验证结果

    // 验证hCaptcha
    $verifyUrl = 'https://hcaptcha.com/siteverify';
    $verifyData = [
        'secret' => $secretKey,
        'response' => $response
    ];
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $verifyUrl);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($verifyData));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $verifyResponse = curl_exec($ch);
    curl_close($ch);

    // 解析验证结果
    $verifyResult = json_decode($verifyResponse);
    if ($verifyResult->success) {
        // hCaptcha验证成功

        // 获取用户IP地址
        $userIp = $_SERVER['REMOTE_ADDR'];
        
        // 连接Redis数据库
        try {
            $redis = new Redis();
            $redis->connect('127.0.0.1', 6379);  // 连接Redis服务器

            // 设置Redis键值：IP地址，2分钟过期
            // 如果记录已存在，重置过期时间
            $result = $redis->setex('user_ip:' . $userIp, 120, 1);  // 设置2分钟过期时间（120秒）

            // 验证成功后跳转到指定的成功页面
            header("Location: success.html");
            exit();
        } catch (Exception $e) {
            die("Could not connect to Redis: " . $e->getMessage());
        }
    } else {
        // 验证失败后跳转到失败页面
        header("Location: failure.html");
        exit();
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>UltimateNoBot</title>
    <script src="https://hcaptcha.com/1/api.js" async defer></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: left;  /* 文字左对齐 */
            padding: 50px;
            background-color: #f4f4f4;
        }
        h1 {
            font-size: 28px;  /* 调整标题文字大小 */
            margin-bottom: 20px;  /* 调整标题与内容的间距 */
        }
        p {
            font-size: 16px;  /* 调整段落文字大小 */
            line-height: 1.6;  /* 增加行高，调整行间距 */
            margin: 10px 0;  /* 调整段落间距 */
        }
        .content {
            margin-top: 30px;
        }
        .h-captcha {
            margin-top: 20px;  /* 调整验证码与内容的间距 */
        }
    </style>
</head>
<body>
    <h1>Verify if you're human</h1>
    <p>Please finish the CAPTCHA below.</p>
    

    <form id="captcha-form" action="index.php" method="POST">
        <div class="h-captcha" 
             data-sitekey="YOUR_SITEKEY_HERE" data-callback="onSubmit"></div>
    </form>
    <div class="content">
        <p>If you can't see the CAPTCHA after a while, enable JavaScript.</p>
        <p>Performance and security powered by UltimateNoBot</p>
    </div>

    <script>
        // 当hCaptcha验证完成时，自动提交表单
        function onSubmit(token) {
            // 提交表单
            document.getElementById("captcha-form").submit();
        }
    </script>
</body>
</html>
