package com.lingsi.gpt.weixin.pay.weixinservice;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import io.micrometer.common.lang.Nullable;

public class ByteArrayRedisSerializer implements RedisSerializer<byte[]> {

    @Override
    @Nullable
    public byte[] serialize(@Nullable byte[] bytes) throws SerializationException {
        return bytes;
    }

    @Override
    @Nullable
    public byte[] deserialize(@Nullable byte[] bytes) throws SerializationException {
        return bytes;
    }
}

