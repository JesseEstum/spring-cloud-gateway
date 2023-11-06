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

package org.springframework.cloud.gateway.filter;

import java.net.URI;
import java.util.List;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.factory.CacheRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;

public class CacheRequestBodyFilter implements GatewayFilter, Ordered {

	private final CacheRequestBodyGatewayFilterFactory.Config config;

	private final List<HttpMessageReader<?>> messageReaders;

	private final GatewayFilterFactory gatewayFilterFactoryInstance;

	public CacheRequestBodyFilter(CacheRequestBodyGatewayFilterFactory.Config config,
			List<HttpMessageReader<?>> messageReaders, GatewayFilterFactory gatewayFilterFactoryInstance) {
		this.config = config;
		this.messageReaders = messageReaders;
		this.gatewayFilterFactoryInstance = gatewayFilterFactoryInstance;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		URI requestUri = request.getURI();
		String scheme = requestUri.getScheme();

		// Record only http requests (including https)
		if ((!"http".equals(scheme) && !"https".equals(scheme))) {
			return chain.filter(exchange);
		}

		Object cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
		if (cachedBody != null) {
			return chain.filter(exchange);
		}

		return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
			final ServerRequest serverRequest = ServerRequest
					.create(exchange.mutate().request(serverHttpRequest).build(), messageReaders);
			return serverRequest.bodyToMono((config.getBodyClass())).doOnNext(objectValue -> {
				Object previousCachedBody = exchange.getAttributes()
						.put(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR, objectValue);
				if (previousCachedBody != null) {
					// store previous cached body
					exchange.getAttributes().put(
							CacheRequestBodyGatewayFilterFactory.CACHED_ORIGINAL_REQUEST_BODY_BACKUP_ATTR,
							previousCachedBody);
				}
			}).then(Mono.defer(() -> {
				ServerHttpRequest cachedRequest = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
				Assert.notNull(cachedRequest, "cache request shouldn't be null");
				exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
				return chain.filter(exchange.mutate().request(cachedRequest).build()).doFinally(s -> {
					//
					Object backupCachedBody = exchange.getAttributes()
							.get(CacheRequestBodyGatewayFilterFactory.CACHED_ORIGINAL_REQUEST_BODY_BACKUP_ATTR);
					if (backupCachedBody instanceof DataBuffer dataBuffer) {
						DataBufferUtils.release(dataBuffer);
					}
				});
			}));
		});
	}

	@Override
	public String toString() {
		return filterToStringCreator(gatewayFilterFactoryInstance).append("Body class", config.getBodyClass())
				.toString();
	}

	@Override
	public int getOrder() {
		// TODO this needs to ideally reference off of ResponseCacheGatewayFilter instead
		// of being hardcoded
		return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 4;
	}

}
