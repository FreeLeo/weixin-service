package com.lingsi.gpt.weixin.examples;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lingsi.gpt.weixin.pay.weixinservice.CodeState;
import com.lingsi.gpt.weixin.pay.weixinservice.Result;

import jakarta.annotation.Resource;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Resource
    private RedisTemplate<String, byte[]> redisTemplate;

    private final List<Map<String, Object>> packages = List.of(
            Map.of(
                    "id", "1",
                    "title", "基础套餐（推荐）",
                    "basic_chat_limit", 10,
                    "advanced_chat_limit", 10,
                    "price", 1,
                    "expiration", -1),
            Map.of(
                    "id", "2",
                    "title", "高级套餐",
                    "basic_chat_limit", -1,
                    "advanced_chat_limit", -1,
                    "price", 100,
                    "expiration", -1));

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @PostMapping("/user")
    public Result<UserRequest> createUser(@RequestBody UserRequest userRequest) {
        return new Result<UserRequest>(CodeState.RESULT_CODE_SUCCESS, userRequest);
    }

    @PostMapping("/notifyPay")
    public void notifyPay() {
        System.out.println("notifyPay start");
        // testConnection();

        // String orderKey = "order:333:111";
        // BoundHashOperations<String, byte[], byte[]> hashOperations =
        // redisTemplate.boundHashOps(orderKey);
        // System.out.println("【status】: "+hashOperations.get("status"));
        // byte[] statusValueBytes = hashOperations.get("status");
        // if (statusValueBytes != null) {
        // System.out.println(new String(statusValueBytes, StandardCharsets.UTF_8));
        // } else {
        // System.out.println("order:333:111 is " + null);
        // }

        String orderKeyPattern = "order:*:111";
        Set<String> matchingKeys = redisTemplate.keys(orderKeyPattern);

        if (!matchingKeys.isEmpty()) {
            // 只取第一个匹配到的键
            String orderKey = matchingKeys.iterator().next();
            BoundHashOperations<String, byte[], byte[]> orderHash = redisTemplate.boundHashOps(orderKey);

            // 使用 hset 方法更新订单状态为 "paid"
            orderHash.put("status".getBytes(StandardCharsets.UTF_8), "paid".getBytes(StandardCharsets.UTF_8));
            orderHash.expire(6 * 30, TimeUnit.DAYS);

            String[] keyParts = orderKey.split(":");
            String userId = keyParts[1]; // user_id 在键名的第二个位置
            byte[] packageIdBytes = orderHash.get("package_id".getBytes(StandardCharsets.UTF_8));
            String package_id = new String(packageIdBytes, StandardCharsets.UTF_8);
            System.out.println(userId + " , " + package_id);
            Map<String, Object> packageMap = getPackageById(package_id);
            storeUserPackage(userId, packageMap);
        }
    }

    public void storeUserPackage(String userId, Map<String, Object> packageInfo) {
        BoundHashOperations<String, byte[], byte[]> userPackage = getUserPackage(userId);
        int basicChatLimit = (int) packageInfo.get("basic_chat_limit");
        int advancedChatLimit = (int) packageInfo.get("advanced_chat_limit");

        if (userPackage != null
                && userPackage.size() > 0
                && userPackage.hasKey("basic_chat_limit".getBytes(StandardCharsets.UTF_8))
                && userPackage.hasKey("advanced_chat_limit".getBytes(StandardCharsets.UTF_8))) {
            basicChatLimit += Integer.parseInt(new String(userPackage.get("basic_chat_limit".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            advancedChatLimit += Integer.parseInt(new String(userPackage.get("advanced_chat_limit".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        }

        String userPackageKey = "user:" + userId + ":package";
        BoundHashOperations<String, byte[], byte[]> orderHash = redisTemplate.boundHashOps(userPackageKey);
        orderHash.put("id".getBytes(StandardCharsets.UTF_8),
                packageInfo.get("id").toString().getBytes(StandardCharsets.UTF_8));
        orderHash.put("title".getBytes(StandardCharsets.UTF_8),
                packageInfo.get("title").toString().getBytes(StandardCharsets.UTF_8));
        orderHash.put("basic_chat_limit".getBytes(StandardCharsets.UTF_8), String.valueOf(basicChatLimit).getBytes(StandardCharsets.UTF_8));
        orderHash.put("advanced_chat_limit".getBytes(StandardCharsets.UTF_8), String.valueOf(advancedChatLimit).getBytes(StandardCharsets.UTF_8));
    }

    public BoundHashOperations<String, byte[], byte[]> getUserPackage(String userId) {
        String userPackageKey = "user:" + userId + ":package";
        BoundHashOperations<String, byte[], byte[]> userHash = redisTemplate.boundHashOps(userPackageKey);
        return userHash;
    }

    public Map<String, Object> getPackageById(String packageId) {
        for (Map<String, Object> packageInfo : packages) {
            if (packageInfo.get("id").equals(packageId)) {
                return packageInfo;
            }
        }
        return null;
    }

    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
}