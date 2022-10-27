package com.tanhua.server.config;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Map;


@Configuration
public class RedisCacheConfig {

    //设置redis缓存的失效时间
    private static final ImmutableMap<Object, Object> cacheMap;

    static {
        cacheMap = ImmutableMap.builder().put("videos", Duration.ofSeconds(30L)).build();
    }

    //配置RedisCacheManagerBuilderCustomizer对象
    @Bean
    public RedisCacheManagerBuilderCustomizer getCustomizer() {
        return (builder) -> {
            for (Map.Entry<Object, Object> entry : cacheMap.entrySet()) {
                builder.withCacheConfiguration(entry.getKey().toString(),
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl((Duration) entry.getValue()));
            }
        };
    }
}
