package com.lingsi.gpt.weixin.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.lingsi.gpt.weixin.pay.weixinservice.ByteArrayRedisSerializer;

@Configuration
public class JedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        System.out.println("【Example】Redis connect , " + host + " , " + port);
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory factory) {
        System.out.println("【Example】Redis create redisTemplate");
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new ByteArrayRedisSerializer()); 
        template.setValueSerializer(new ByteArrayRedisSerializer());
        template.setHashValueSerializer(new ByteArrayRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
