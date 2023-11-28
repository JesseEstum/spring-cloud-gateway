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

package org.springframework.cloud.gateway.filter.factory.cache;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.factory.cache.provider.CacheManagerProvider;
import org.springframework.util.unit.DataSize;

/**
 * {@link org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory} of
 * {@link ResponseCacheGatewayFilter}.
 *
 * By default, a global cache (defined as properties in the application) is used. For
 * specific route configuration, parameters can be added following
 * {@link RouteCacheConfiguration} class.
 *
 * @author Marta Medio
 * @author Ignacio Lozano
 */
@ConditionalOnProperty(value = "spring.cloud.gateway.filter.local-response-cache.enabled", havingValue = "true")
public class LocalResponseCacheGatewayFilterFactory extends ResponseCacheGatewayFilterFactory {

	// TODO backwards compatible constructor
	public LocalResponseCacheGatewayFilterFactory(ResponseCacheManagerFactory cacheManagerFactory,
			Duration defaultTimeToLive) {
		super(RouteCacheConfiguration.class);
		this.cacheManagerFactory = cacheManagerFactory;
		this.defaultTimeToLive = defaultTimeToLive;
		this.defaultSize = null;
	}

	// TODO backwards compatible constructor
	public LocalResponseCacheGatewayFilterFactory(ResponseCacheManagerFactory cacheManagerFactory,
			Duration defaultTimeToLive, DataSize defaultSize) {
		super(RouteCacheConfiguration.class);
		this.cacheManagerFactory = cacheManagerFactory;
		this.defaultTimeToLive = defaultTimeToLive;
		this.defaultSize = defaultSize;
	}

	// TODO delete???
	public LocalResponseCacheGatewayFilterFactory(ResponseCacheManagerFactory cacheManagerFactory,
			Duration defaultTimeToLive, DataSize defaultSize, CacheManagerProvider cacheManagerProvider) {
		super(cacheManagerFactory, defaultTimeToLive, defaultSize, cacheManagerProvider);
	}

	// @PostConstruct
	// public void setCacheManagerProvider(CacheManagerProvider cacheManagerProvider) {
	// this.cacheManagerProvider = cacheManagerProvider;
	// }

}
