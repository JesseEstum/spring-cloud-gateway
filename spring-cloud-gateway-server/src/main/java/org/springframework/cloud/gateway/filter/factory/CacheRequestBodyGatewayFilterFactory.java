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

package org.springframework.cloud.gateway.filter.factory;

import java.util.List;

import org.springframework.cloud.gateway.filter.CacheRequestBodyFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.web.reactive.function.server.HandlerStrategies;

/**
 * @author weizibin
 */
public class CacheRequestBodyGatewayFilterFactory
		extends AbstractGatewayFilterFactory<CacheRequestBodyGatewayFilterFactory.Config> {

	/**
	 * Backup attribute for cached original request body.
	 */
	public static final String CACHED_ORIGINAL_REQUEST_BODY_BACKUP_ATTR = "cachedOriginalRequestBodyBackup";

	private final List<HttpMessageReader<?>> messageReaders;

	public CacheRequestBodyGatewayFilterFactory() {
		super(CacheRequestBodyGatewayFilterFactory.Config.class);
		this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
	}

	@Override
	public GatewayFilter apply(CacheRequestBodyGatewayFilterFactory.Config config) {
		return new CacheRequestBodyFilter(config, messageReaders, this);
	}

	public static class Config {

		private Class<?> bodyClass;

		public Class<?> getBodyClass() {
			return bodyClass;
		}

		public void setBodyClass(Class<?> bodyClass) {
			this.bodyClass = bodyClass;
		}

	}

}
