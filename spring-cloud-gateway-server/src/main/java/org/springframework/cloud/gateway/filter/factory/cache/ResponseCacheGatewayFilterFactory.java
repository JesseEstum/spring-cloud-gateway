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

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.util.unit.DataSize;

/**
 * Base class for multiple implementations of response caching.
 */
public abstract class ResponseCacheGatewayFilterFactory<C> extends AbstractGatewayFilterFactory<C> {

	/**
	 * Exchange attribute name to track if the request has been already process by cache
	 * at route filter level.
	 */
	public static final String LOCAL_RESPONSE_CACHE_FILTER_APPLIED = "LocalResponseCacheGatewayFilter-Applied";

	protected ResponseCacheManagerFactory cacheManagerFactory;

	protected Duration defaultTimeToLive;

	protected DataSize defaultSize;

	protected ResponseCacheGatewayFilterFactory(Class configClass) {
		super(configClass);
	}

	// public ResponseCacheGatewayFilterFactory(ResponseCacheManagerFactory
	// cacheManagerFactory,
	// Duration defaultTimeToLive, CacheManagerProvider cacheManagerProvider) {
	// this(cacheManagerFactory, defaultTimeToLive, null, cacheManagerProvider);
	// }

	// public ResponseCacheGatewayFilterFactory(ResponseCacheManagerFactory
	// cacheManagerFactory,
	// Duration defaultTimeToLive, DataSize defaultSize, CacheManagerProvider
	// cacheManagerProvider) {
	// super(RouteCacheConfiguration.class);
	// this.cacheManagerFactory = cacheManagerFactory;
	// this.defaultTimeToLive = defaultTimeToLive;
	// this.defaultSize = defaultSize;
	// this.cacheManagerProvider = cacheManagerProvider;
	// }

	// @Override
	// public GatewayFilter apply(RouteCacheConfiguration config) {
	// LocalResponseCacheProperties cacheProperties = mapRouteCacheConfig(config);
	//
	// Cache routeCache = cacheManagerProvider.getCacheManager(cacheProperties)
	// .getCache(config.getRouteId() + "-cache");
	// return new ResponseCacheGatewayFilter(cacheManagerFactory.create(routeCache,
	// cacheProperties.getTimeToLive()));
	//
	// }

	// private LocalResponseCacheProperties mapRouteCacheConfig(RouteCacheConfiguration
	// config) {
	// Duration timeToLive = config.getTimeToLive() != null ? config.getTimeToLive() :
	// defaultTimeToLive;
	// DataSize size = config.getSize() != null ? config.getSize() : defaultSize;
	//
	// LocalResponseCacheProperties responseCacheProperties = new
	// LocalResponseCacheProperties();
	// responseCacheProperties.setTimeToLive(timeToLive);
	// responseCacheProperties.setSize(size);
	// return responseCacheProperties;
	// }

}
