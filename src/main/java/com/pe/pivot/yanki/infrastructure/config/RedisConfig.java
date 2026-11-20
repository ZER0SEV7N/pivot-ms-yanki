package com.pe.pivot.yanki.infrastructure.config;

import com.pe.pivot.yanki.domain.model.YankiWallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Reactive Redis configuration for caching YankiWallet lookups by phoneNumber.
 */
@Configuration
public class RedisConfig {

  /**
   * Creates a ReactiveRedisTemplate for YankiWallet objects, using String keys and JSON-serialized values.
   *
   * @param factory - the ReactiveRedisConnectionFactory to create connections to Redis
   * @return - a ReactiveRedisTemplate configured for YankiWallet objects
   */
  @Bean
  public ReactiveRedisTemplate<String, YankiWallet> yankiWalletRedisTemplate(
          ReactiveRedisConnectionFactory factory) {

    // Configure key and value serializers for Redis
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    JacksonJsonRedisSerializer<YankiWallet> valueSerializer =
            new JacksonJsonRedisSerializer<>(YankiWallet.class);

    // Build the RedisSerializationContext with the configured serializers
    RedisSerializationContext<String, YankiWallet> context =
            RedisSerializationContext.<String, YankiWallet>newSerializationContext(keySerializer)
                    .value(valueSerializer)
                    .hashKey(keySerializer)
                    .hashValue(valueSerializer)
                    .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}