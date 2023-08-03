package com.lingsi.gpt.weixin.pay.weixinservice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;

import org.apache.commons.logging.Log;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Jedis;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingsi.gpt.weixin.examples.UserRequest;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.RsaCryptoUtil;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class PrePayController {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/prepay")
    public Result<UserRequest> createUser(@RequestBody UserRequest userRequest) {
        return new Result<UserRequest>(CodeState.RESULT_CODE_SUCCESS, userRequest);
    }

    public static String appId = System.getenv("GPT_PAY_APP_ID");
    /** 商户号 */
    public static String merchantId = System.getenv("GPT_PAY_MERCHANT_ID");
    /** 商户API私钥路径 */
    public static String privateKeyPath = System.getenv("GPT_PAY_PRIVATE_KEY_PATH");
    /** 商户证书序列号 */
    public static String merchantSerialNumber = System.getenv("GPT_PAY_MERCHANT_SERIAL_NUMBER");
    /** 商户APIV3密钥 */
    public static String apiV3key = System.getenv("GPT_PAY_API_V3_KEY");

    private PrivateKey privateKey = null;

    private final List<Map<String, Object>> packages = List.of(
        Map.of(
            "id", "1",
            "title", "基础套餐（推荐）",
            "basic_chat_limit", 10,
            "advanced_chat_limit", 10,
            "price", 1,
            "expiration", -1
        ),
        Map.of(
            "id", "2",
            "title", "高级套餐",
            "basic_chat_limit", -1,
            "advanced_chat_limit", -1,
            "price", 100,
            "expiration", -1
        )
    );


    @CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:5000" })
    @PostMapping("/payPre")
    public Result payPre(@RequestBody Order order) {
        System.out.println("【DEBUG】, payPre start.");
        if (privateKeyPath == null || privateKeyPath.isEmpty()) {
            return new Result<String>(CodeState.RESULT_CODE_FAIL, "商户私钥路径为空");
        }
        // 使用自动更新平台证书的RSA配置
        // 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3key)
                .build();
        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        // request.setXxx(val)设置所需参数，具体参数可见Request定义
        PrepayRequest request = new PrepayRequest();
        Amount amount = new Amount();
        amount.setTotal(order.getAmount());
        request.setAmount(amount);
        request.setAppid(appId);
        request.setMchid(merchantId);
        request.setDescription(order.getTitle());
        request.setNotifyUrl("http://23.251.52.214:5697/pay/notify");
        request.setOutTradeNo(order.getOrder_id());
        // 调用下单方法，得到应答
        PrepayResponse response = service.prepay(request);
        // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
        PrePay prePay = new PrePay();
        prePay.setCodeUrl(response.getCodeUrl());
        return new Result<PrePay>(CodeState.RESULT_CODE_SUCCESS, prePay);
    }

    @CrossOrigin(origins = { "http://localhost:3000", "http://127.0.0.1:5000" })
    @PostMapping("/pay/notify")
    public Result notifyResult(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("【DEBUG】, /pay/notify start.");
        // 应答对象
        Map<String, String> map = new HashMap<>();

        try {

            // 处理参数
            String serialNumber = request.getHeader("Wechatpay-Serial");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String signature = request.getHeader("Wechatpay-Signature");// 请求头Wechatpay-Signature
            // 获取请求体
            String body = HttpUtils.readData(request);

            // 构造微信请求体
            NotificationRequest wxRequest = new NotificationRequest.Builder().withSerialNumber(serialNumber)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(body)
                    .build();
            Notification notification = null;
            try {

                /**
                 * 使用微信支付回调请求处理器解析构造的微信请求体
                 * 在这个过程中会进行签名验证，并解密加密过的内容
                 * 签名源码： com.wechat.pay.contrib.apache.httpclient.cert; 271行开始
                 * 解密源码： com.wechat.pay.contrib.apache.httpclient.notification 76行
                 * com.wechat.pay.contrib.apache.httpclient.notification 147行 使用私钥获取AesUtil
                 * com.wechat.pay.contrib.apache.httpclient.notification 147行 使用Aes对称解密获得原文
                 */
                if (privateKey == null) {
                    privateKey = PemUtil.loadPrivateKeyFromPath(privateKeyPath);
                }
                NotificationHandler notificationHandler = new NotificationHandler(getVerifier(privateKey),
                        apiV3key.getBytes(StandardCharsets.UTF_8));
                notification = notificationHandler.parse(wxRequest);
            } catch (Exception e) {
                System.out.println("【DEBUG】, /pay/notify error1.");
                e.printStackTrace();
                return new Result<String>(CodeState.RESULT_CODE_FAIL, "");
            }

            // 从notification中获取解密报文,并解析为HashMap
            String plainText = notification.getDecryptData();
            ObjectMapper objectMapper = new ObjectMapper();
            // 将字符串转换为 JsonNode
            JsonNode jsonNode = objectMapper.readTree(plainText);

            // 处理订单
            System.out.println("【DEBUG】, /pay/notify , " + plainText);
            // 假设你从通知数据中获取了订单信息，例如订单ID和用户ID
            String orderId = jsonNode.get("out_trade_no").asText();
            String orderKeyPattern = "order:*:" + orderId;

            // 使用 keys 方法查找包含指定 orderId 的键
            Set<String> matchingKeys = redisTemplate.keys(orderKeyPattern);

            if (!matchingKeys.isEmpty()) {
                // 只取第一个匹配到的键
                String orderKey = matchingKeys.iterator().next();
                BoundHashOperations<String, byte[], byte[]> hashOperations = redisTemplate.boundHashOps(orderKey);

                // 使用 hset 方法更新订单状态为 "paid"
                hashOperations.put("status".getBytes(StandardCharsets.UTF_8), "paid".getBytes(StandardCharsets.UTF_8));
                hashOperations.expire(6 * 30, TimeUnit.DAYS);

                String[] keyParts = orderKey.split(":");
                String userId = keyParts[1]; // user_id 在键名的第二个位置
                byte[] packageIdBytes = hashOperations.get("package_id".getBytes(StandardCharsets.UTF_8));
                String package_id = new String(packageIdBytes, StandardCharsets.UTF_8);
                Map<String, Object> packageMap = getPackageById(package_id);
                storeUserPackage(userId, packageMap);

            }

            // 成功应答
            return new Result<String>(CodeState.RESULT_CODE_SUCCESS, "");

        } catch (Exception e) {
            System.out.println("【DEBUG】, /pay/notify error2.");
            e.printStackTrace();
            return new Result<String>(CodeState.RESULT_CODE_FAIL, "");
        }
    }

    public Verifier getVerifier(PrivateKey merchantPrivateKey) throws IOException, NotFoundException {

        // 获取证书管理器单例实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();

        // 向证书管理器增加需要自动更新平台证书的商户信息
        try {
            // 该方法底层已实现同步线程更新证书
            // 详见beginScheduleUpdate()方法
            certificatesManager.putMerchant(merchantId, new WechatPay2Credentials(merchantId,
                    new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                    apiV3key.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException | HttpCodeException e) {
            e.printStackTrace();
        }

        return certificatesManager.getVerifier(merchantId);
    }

    public void storeUserPackage(String userId, Map<String, Object> packageInfo) {
        Map<String, Object> currentPackage = getUserPackage(userId);
        int basicChatLimit = (int) packageInfo.get("basic_chat_limit");
        int advancedChatLimit = (int) packageInfo.get("advanced_chat_limit");

        if (currentPackage != null) {
            basicChatLimit += (int) currentPackage.get("basic_chat_limit");
            advancedChatLimit += (int) currentPackage.get("advanced_chat_limit");
        }

        String userPackageKey = "user:" + userId + ":package";
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(userPackageKey, "id", packageInfo.get("id"));
        hashOperations.put(userPackageKey, "title", packageInfo.get("title"));
        hashOperations.put(userPackageKey, "basic_chat_limit", basicChatLimit);
        hashOperations.put(userPackageKey, "advanced_chat_limit", advancedChatLimit);
    }

    public Map<String, Object> getUserPackage(String userId) {
        String userPackageKey = "user:" + userId + ":package";
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> packageInfo = hashOperations.entries(userPackageKey);

        return packageInfo;
    }

    public Map<String, Object> getPackageById(String packageId) {
        for (Map<String, Object> packageInfo : packages) {
            if (packageInfo.get("id").equals(packageId)) {
                return packageInfo;
            }
        }
        return null;
    }
}