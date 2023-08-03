package com.lingsi.gpt.weixin.examples;

import java.nio.charset.StandardCharsets;
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
    private RedisTemplate<String, String> redisTemplate;

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
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
        // RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("192.168.3.229", 6379);
        // redisStandaloneConfiguration.setPassword("lizhen-redis");
        // JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
        // jedisConnectionFactory.afterPropertiesSet();
        // RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // redisTemplate.setConnectionFactory(jedisConnectionFactory);

        testConnection();

        // String orderKey = "order:333:111";

        // // 使用 boundHashOps 方法获取 BoundHashOperations 实例
        // BoundHashOperations<byte[], byte[], byte[]> hashOperations = redisTemplate.boundHashOps(orderKey.getBytes(StandardCharsets.UTF_8));

        // // 使用 get 方法读取 status 字段的值
        // byte[] statusValueBytes = hashOperations.get("status".getBytes(StandardCharsets.UTF_8));

        // if (statusValueBytes != null) {
        //     System.out.println(new String(statusValueBytes, StandardCharsets.UTF_8));
        // } else {
        //     System.out.println("order:333:111 is " + null);
        // }
        // HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        // String orderKeyPattern = "order:333:111";
        // Object object = hashOperations.get(orderKeyPattern, "status");
        // System.out.println("order:333:111 is " + object);
        // Set<String> matchingKeys = redisTemplate.keys(orderKeyPattern);

        // if (!matchingKeys.isEmpty()) {
        //     // 只取第一个匹配到的键
        //     String orderKey = matchingKeys.iterator().next();
        //     BoundHashOperations<String, String, Object> hashOperations = redisTemplate.boundHashOps(orderKey);

        //     // 使用 hset 方法更新订单状态为 "paid"
        //     hashOperations.put("status", "paid");
        //     hashOperations.expire(6 * 30, TimeUnit.DAYS);

        //     String[] keyParts = orderKey.split(":");
        //     String userId = keyParts[1]; // user_id 在键名的第二个位置
        //     byte[] packageIdBytes = (byte[]) hashOperations.get("package_id".getBytes(StandardCharsets.UTF_8));
        //     String package_id = new String(packageIdBytes, StandardCharsets.UTF_8);
        //     System.out.println(package_id);

        // }
    }

    public boolean testConnection() {
        try {
            // 获取 ValueOperations 实例
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

            // 设置一个测试 key-value
            String testKey = "testKey";
            String testValue = "testValue";
            valueOps.set(testKey, testValue);

            // 读取测试 key 的 value
            String retrievedValue = valueOps.get(testKey);

            // 检查读取的 value 是否与设置的值一致
            if (testValue.equals(retrievedValue)) {
                System.out.println("Redis 连接测试成功，连接正常！");
                return true;
            } else {
                System.out.println("Redis 连接测试失败，连接异常！");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Redis 连接测试失败，连接异常：" + e.getMessage());
            return false;
        }
    }
}