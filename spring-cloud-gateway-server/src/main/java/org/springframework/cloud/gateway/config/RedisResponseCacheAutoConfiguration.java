/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.gateway.config;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.filter.factory.cache.GlobalRedisResponseCacheGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.cache.LocalResponseCacheProperties;
import org.springframework.cloud.gateway.filter.factory.cache.RedisResponseCacheGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.cache.ResponseCacheManagerFactory;
import org.springframework.cloud.gateway.filter.factory.cache.keygenerator.CacheKeyGenerator;
import org.springframework.cloud.gateway.filter.factory.cache.provider.RedisCacheManagerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ LocalResponseCacheProperties.class }) // TODO need to
																		// refactor this
																		// into local
																		// response cache
																		// properties vs
																		// redis ones
@ConditionalOnClass({ RedisCache.class, RedisConnectionFactory.class })
@ConditionalOnEnabledFilter(RedisResponseCacheGatewayFilterFactory.class)
public class RedisResponseCacheAutoConfiguration {

	private static final String RESPONSE_CACHE_NAME = "response-cache";

	@Bean
	@Conditional(OnGlobalRedisResponseCacheCondition.class)
	public GlobalRedisResponseCacheGatewayFilter globalRedisResponseCacheGatewayFilter(
			ResponseCacheManagerFactory responseCacheManagerFactory, LocalResponseCacheProperties properties,
			RedisCacheManagerProvider redisCacheManagerProvider) {
		return new GlobalRedisResponseCacheGatewayFilter(responseCacheManagerFactory,
				responseCache(redisCacheManagerProvider.getCacheManager(properties)), properties.getTimeToLive());
	}

	@Bean
	public RedisCacheManagerProvider redisCacheManagerProvider(RedisConnectionFactory redisConnectionFactory) {
		return new RedisCacheManagerProvider(redisConnectionFactory);
	}

	@Bean
	public RedisResponseCacheGatewayFilterFactory redisResponseCacheGatewayFilterFactory(
			ResponseCacheManagerFactory responseCacheManagerFactory, LocalResponseCacheProperties properties,
			RedisCacheManagerProvider redisCacheManagerProvider) {
		return new RedisResponseCacheGatewayFilterFactory(responseCacheManagerFactory, properties.getTimeToLive(),
				properties.getSize(), redisCacheManagerProvider);
	}

	@Bean
	@ConditionalOnMissingBean
	public ResponseCacheManagerFactory responseCacheManagerFactory(CacheKeyGenerator redisResponseCacheKeyGenerator) {
		return new ResponseCacheManagerFactory(redisResponseCacheKeyGenerator);
	}

	@Bean
	public CacheKeyGenerator redisResponseCacheKeyGenerator() {
		return new CacheKeyGenerator();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })

	Cache responseCache(CacheManager cacheManager) {
		return cacheManager.getCache(RESPONSE_CACHE_NAME);
	}

	public static class OnGlobalRedisResponseCacheCondition extends AllNestedConditions {

		OnGlobalRedisResponseCacheCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnProperty(value = "spring.cloud.gateway.enabled", havingValue = "true", matchIfMissing = true)
		static class OnGatewayPropertyEnabled {

		}

		@ConditionalOnProperty(value = "spring.cloud.gateway.filter.redis-response-cache.enabled", havingValue = "true")
		static class OnRedisResponseCachePropertyEnabled {

		}

		@ConditionalOnProperty(name = "spring.cloud.gateway.global-filter.redis-response-cache.enabled",
				havingValue = "true", matchIfMissing = true)
		static class OnGlobalRedisResponseCachePropertyEnabled {

		}

	}

}
