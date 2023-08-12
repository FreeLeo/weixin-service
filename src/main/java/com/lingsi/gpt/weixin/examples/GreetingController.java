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
import com.lingsi.gpt.weixin.pay.weixinservice.PackageInfo;
import com.lingsi.gpt.weixin.pay.weixinservice.Result;

import jakarta.annotation.Resource;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Resource
    private RedisTemplate<String, byte[]> redisTemplate;

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
            PackageInfo packageInfo = getPackageById(package_id);
            storeUserPackage(userId, packageInfo);
        }
    }

    public void storeUserPackage(String userId, PackageInfo packageInfo) {
        BoundHashOperations<String, byte[], byte[]> userPackage = getUserPackage(userId);
        int basicChatLimit = packageInfo.basic_chat_limit;
        int advancedChatLimit = packageInfo.advanced_chat_limit;

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
                packageInfo.id.getBytes(StandardCharsets.UTF_8));
        orderHash.put("title".getBytes(StandardCharsets.UTF_8),
                packageInfo.title.getBytes(StandardCharsets.UTF_8));
        orderHash.put("basic_chat_limit".getBytes(StandardCharsets.UTF_8), String.valueOf(basicChatLimit).getBytes(StandardCharsets.UTF_8));
        orderHash.put("advanced_chat_limit".getBytes(StandardCharsets.UTF_8), String.valueOf(advancedChatLimit).getBytes(StandardCharsets.UTF_8));

        int expiration = packageInfo.expiration;
        System.out.println("storeUserPackage expiration = " + expiration);
        long currentExpire = 0;
        if (orderHash.getExpire() != null) {
            currentExpire = orderHash.getExpire();
        }
        System.out.println("storeUserPackage currentExpire = " + currentExpire);
        orderHash.expire(currentExpire + expiration, TimeUnit.SECONDS);
    }

    public BoundHashOperations<String, byte[], byte[]> getUserPackage(String userId) {
        String userPackageKey = "user:" + userId + ":package";
        BoundHashOperations<String, byte[], byte[]> userHash = redisTemplate.boundHashOps(userPackageKey);
        return userHash;
    }

    public PackageInfo getPackageById(String packageId) {
        for (PackageInfo packageInfo : PackageInfo.getPackages()) {
            if (packageInfo.id.equals(packageId)) {
                return packageInfo;
            }
        }
        return null;
    }
}