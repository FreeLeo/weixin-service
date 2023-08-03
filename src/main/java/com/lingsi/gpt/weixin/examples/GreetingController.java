package com.lingsi.gpt.weixin.examples;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.redis.core.BoundHashOperations;
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
        // RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("192.168.3.229", 6379);
        // redisStandaloneConfiguration.setPassword("lizhen-redis");
        // JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
        // jedisConnectionFactory.afterPropertiesSet();
        // RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // redisTemplate.setConnectionFactory(jedisConnectionFactory);

        String orderKeyPattern = "order:333:111";
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        
        // 使用 get 方法获取 users 的值
        String usersValue = valueOps.get("users");
        System.out.println(usersValue);
        // 使用 keys 方法查找包含指定 orderId 的键
        Set<String> matchingKeys = redisTemplate.keys(orderKeyPattern);

        if (!matchingKeys.isEmpty()) {
            // 只取第一个匹配到的键
            String orderKey = matchingKeys.iterator().next();
            BoundHashOperations<String, byte[], Object> hashOperations = redisTemplate.boundHashOps(orderKey);

            // 使用 hset 方法更新订单状态为 "paid"
            hashOperations.put("status".getBytes(StandardCharsets.UTF_8), "paid".getBytes(StandardCharsets.UTF_8));
            hashOperations.expire(6 * 30, TimeUnit.DAYS);

            String[] keyParts = orderKey.split(":");
            String userId = keyParts[1]; // user_id 在键名的第二个位置
            byte[] packageIdBytes = (byte[]) hashOperations.get("package_id".getBytes(StandardCharsets.UTF_8));
            String package_id = new String(packageIdBytes, StandardCharsets.UTF_8);
            System.out.println(package_id);

        }
    }
}