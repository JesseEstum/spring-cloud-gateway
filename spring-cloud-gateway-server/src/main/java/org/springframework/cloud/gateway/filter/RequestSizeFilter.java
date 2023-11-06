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

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RequestSizeGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

public class RequestSizeFilter implements GatewayFilter {

	private static String PREFIX = "kMGTPE";

	private static String ERROR = "Request size is larger than permissible limit."
			+ " Request size is %s where permissible limit is %s";

	private final RequestSizeGatewayFilterFactory.RequestSizeConfig requestSizeConfig;

	private final GatewayFilterFactory gatewayFilterFactoryInstance;

	public RequestSizeFilter(RequestSizeGatewayFilterFactory.RequestSizeConfig requestSizeConfig,
			GatewayFilterFactory gatewayFilterFactoryInstance) {
		this.requestSizeConfig = requestSizeConfig;
		this.gatewayFilterFactoryInstance = gatewayFilterFactoryInstance;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String contentLength = request.getHeaders().getFirst("content-length");
		if (!ObjectUtils.isEmpty(contentLength)) {
			Long currentRequestSize = Long.valueOf(contentLength);
			if (currentRequestSize > requestSizeConfig.getMaxSize().toBytes()) {
				exchange.getResponse().setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE);
				if (!exchange.getResponse().isCommitted()) {
					exchange.getResponse().getHeaders().add("errorMessage",
							getErrorMessage(currentRequestSize, requestSizeConfig.getMaxSize().toBytes()));
				}
				return exchange.getResponse().setComplete();
			}
		}
		return chain.filter(exchange);
	}

	@Override
	public String toString() {
		return filterToStringCreator(gatewayFilterFactoryInstance).append("max", requestSizeConfig.getMaxSize())
				.toString();
	}

	private static String getErrorMessage(Long currentRequestSize, Long maxSize) {
		return String.format(ERROR, getReadableByteCount(currentRequestSize), getReadableByteCount(maxSize));
	}

	private static String getReadableByteCount(long bytes) {
		int unit = 1000;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = Character.toString(PREFIX.charAt(exp - 1));
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
