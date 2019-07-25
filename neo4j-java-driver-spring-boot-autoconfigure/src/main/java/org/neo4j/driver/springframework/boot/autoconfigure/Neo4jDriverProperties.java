/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.driver.springframework.boot.autoconfigure;

import static org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties.DriverSettings.LoadBalancingStrategy.*;
import static org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties.*;
import static org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties.TrustSettings.Strategy.*;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.net.ServerAddressResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;

/**
 * Used to configure an instance of the {@link org.neo4j.driver.Driver Neo4j-Java-Driver}.
 *
 * @author Michael J. Simons
 */
@ConfigurationProperties(prefix = PREFIX)
public class Neo4jDriverProperties {

	static final String PREFIX = "org.neo4j.driver";

	/**
	 * The uri this driver should connect to. The driver supports bolt, bolt+routing or neo4j as schemes.
	 */
	private URI uri = URI.create("bolt://localhost:7687");

	/**
	 * The authentication the driver is supposed to use. Maybe null.
	 */
	private Authentication authentication = new Authentication();

	/**
	 * The configuration of the connection pool.
	 */
	private PoolSettings pool = new PoolSettings();

	/**
	 * Detailed configuration of the driver.
	 */
	private DriverSettings config = new DriverSettings();

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(
		Authentication authentication) {
		this.authentication = authentication;
	}

	public PoolSettings getPool() {
		return pool;
	}

	public void setPool(PoolSettings pool) {
		this.pool = pool;
	}

	public DriverSettings getConfig() {
		return config;
	}

	public void setConfig(DriverSettings config) {
		this.config = config;
	}

	public AuthToken getAuthToken() {
		return this.authentication.asAuthToken();
	}

	public Config asDriverConfig() {

		Config.ConfigBuilder builder = Config.builder();
		this.pool.applyTo(builder);
		this.config.applyTo(builder);

		return builder.withLogging(new Neo4jSpringJclLogging()).build();
	}

	public static class Authentication {

		/**
		 * The login of the user connecting to the database.
		 */
		private String username;

		/** The password of the user connecting to the database. */
		private String password;

		/**
		 * The realm to connect to.
		 */
		private String realm;

		/** A kerberos ticket for connecting to the database. Mutual exclusive with a given username. */
		private String kerberosTicket;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getRealm() {
			return realm;
		}

		public void setRealm(String realm) {
			this.realm = realm;
		}

		public String getKerberosTicket() {
			return kerberosTicket;
		}

		public void setKerberosTicket(String kerberosTicket) {
			this.kerberosTicket = kerberosTicket;
		}

		AuthToken asAuthToken() {

			Predicate<String> isNotEmpty = s -> !(s == null || s.isEmpty());

			boolean hasUsername = isNotEmpty.test(this.username);
			boolean hasPassword = isNotEmpty.test(this.password);
			boolean hasKerberosTicket = isNotEmpty.test(this.kerberosTicket);

			if (hasUsername && hasKerberosTicket) {
				throw new InvalidConfigurationPropertyValueException(PREFIX + ".authentication",
					"username=" + username + ",kerberos-ticket=" + kerberosTicket,
					"Cannot specify both username and kerberos ticket.");
			}

			if (hasUsername && hasPassword) {
				return AuthTokens.basic(username, password, realm);
			}

			if (hasKerberosTicket) {
				return AuthTokens.kerberos(kerberosTicket);
			}

			return AuthTokens.none();
		}
	}

	public static class PoolSettings {
		/**
		 * Flag, if metrics are enabled.
		 */
		private boolean metricsEnabled = false;

		/**
		 * Flag, if leaked sessions logging is enabled.
		 */
		private boolean logLeakedSessions = false;

		/**
		 * The maximum amount of connections in the connection pool towards a single database.
		 */
		private int maxConnectionPoolSize = org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_POOL_SIZE;

		/**
		 * Pooled connections that have been idle in the pool for longer than this timeout will be tested before they are used again.
		 */
		private Duration idleTimeBeforeConnectionTest;

		/**
		 * Pooled connections older than this threshold will be closed and removed from the pool.
		 */
		private Duration maxConnectionLifetime = Duration
			.ofMillis(org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_MAX_CONNECTION_LIFETIME);

		/**
		 * Acquisition of new connections will be attempted for at most configured timeout.
		 */
		private Duration connectionAcquisitionTimeout = Duration
			.ofMillis(org.neo4j.driver.internal.async.pool.PoolSettings.DEFAULT_CONNECTION_ACQUISITION_TIMEOUT);

		public boolean isLogLeakedSessions() {
			return logLeakedSessions;
		}

		public void setLogLeakedSessions(boolean logLeakedSessions) {
			this.logLeakedSessions = logLeakedSessions;
		}

		public int getMaxConnectionPoolSize() {
			return maxConnectionPoolSize;
		}

		public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
			this.maxConnectionPoolSize = maxConnectionPoolSize;
		}

		public Duration getIdleTimeBeforeConnectionTest() {
			return idleTimeBeforeConnectionTest;
		}

		public void setIdleTimeBeforeConnectionTest(Duration idleTimeBeforeConnectionTest) {
			this.idleTimeBeforeConnectionTest = idleTimeBeforeConnectionTest;
		}

		public Duration getMaxConnectionLifetime() {
			return maxConnectionLifetime;
		}

		public void setMaxConnectionLifetime(Duration maxConnectionLifetime) {
			this.maxConnectionLifetime = maxConnectionLifetime;
		}

		public Duration getConnectionAcquisitionTimeout() {
			return connectionAcquisitionTimeout;
		}

		public void setConnectionAcquisitionTimeout(Duration connectionAcquisitionTimeout) {
			this.connectionAcquisitionTimeout = connectionAcquisitionTimeout;
		}

		public boolean isMetricsEnabled() {
			return metricsEnabled;
		}

		public void setMetricsEnabled(boolean metricsEnabled) {
			this.metricsEnabled = metricsEnabled;
		}

		private void applyTo(Config.ConfigBuilder builder) {

			if (logLeakedSessions) {
				builder.withLeakedSessionsLogging();
			}
			builder.withMaxConnectionPoolSize(maxConnectionPoolSize);
			if (idleTimeBeforeConnectionTest != null) {
				builder
					.withConnectionLivenessCheckTimeout(idleTimeBeforeConnectionTest.toMillis(), TimeUnit.MILLISECONDS);
			}
			builder.withMaxConnectionLifetime(maxConnectionLifetime.toMillis(), TimeUnit.MILLISECONDS);
			builder.withConnectionAcquisitionTimeout(connectionAcquisitionTimeout.toMillis(), TimeUnit.MILLISECONDS);

			if (metricsEnabled) {
				builder.withDriverMetrics();
			} else {
				builder.withoutDriverMetrics();
			}
		}
	}

	public static class DriverSettings {

		public enum LoadBalancingStrategy {
			ROUND_ROBIN,
			LEAST_CONNECTED;

			Config.LoadBalancingStrategy toInternalRepresentation() {
				return Config.LoadBalancingStrategy.valueOf(this.name());
			}
		}

		/**
		 * Flag, if the driver should use encrypted traffic.
		 */
		private boolean encrypted = true;

		/**
		 * Specify how to determine the authenticity of an encryption certificate provided by the Neo4j instance we are connecting to. Defaults to trust all.
		 */
		private TrustSettings trustSettings = new TrustSettings();

		/**
		 * Provide an alternative load balancing strategy for the routing driver to use.
		 */
		private LoadBalancingStrategy loadBalancingStrategy = LEAST_CONNECTED;

		/**
		 * Specify socket connection timeout.
		 */
		private Duration connectionTimeout = Duration.ofSeconds(5);

		/**
		 * Specify the maximum time transactions are allowed to retry.
		 */
		private Duration maxTransactionRetryTime = Duration
			.ofMillis(org.neo4j.driver.internal.retry.RetrySettings.DEFAULT.maxRetryTimeMs());

		/**
		 * Specify a custom server address resolver used by the routing driver to resolve the initial address used to create the driver.
		 */
		private Class<? extends ServerAddressResolver> serverAddressResolverClass;

		public boolean isEncrypted() {
			return encrypted;
		}

		public void setEncrypted(boolean encrypted) {
			this.encrypted = encrypted;
		}

		public TrustSettings getTrustSettings() {
			return trustSettings;
		}

		public void setTrustSettings(TrustSettings trustSettings) {
			this.trustSettings = trustSettings;
		}

		public LoadBalancingStrategy getLoadBalancingStrategy() {
			return loadBalancingStrategy;
		}

		public void setLoadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
			this.loadBalancingStrategy = loadBalancingStrategy;
		}

		public Duration getConnectionTimeout() {
			return connectionTimeout;
		}

		public void setConnectionTimeout(Duration connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
		}

		public Duration getMaxTransactionRetryTime() {
			return maxTransactionRetryTime;
		}

		public void setMaxTransactionRetryTime(Duration maxTransactionRetryTime) {
			this.maxTransactionRetryTime = maxTransactionRetryTime;
		}

		public Class<? extends ServerAddressResolver> getServerAddressResolverClass() {
			return serverAddressResolverClass;
		}

		public void setServerAddressResolverClass(
			Class<? extends ServerAddressResolver> serverAddressResolverClass) {
			this.serverAddressResolverClass = serverAddressResolverClass;
		}

		private void applyTo(Config.ConfigBuilder builder) {

			if (encrypted) {
				builder.withEncryption();
			} else {
				builder.withoutEncryption();
			}
			builder.withTrustStrategy(trustSettings.toInternalRepresentation());
			builder.withLoadBalancingStrategy(loadBalancingStrategy.toInternalRepresentation());
			builder.withConnectionTimeout(connectionTimeout.toMillis(), TimeUnit.MILLISECONDS);
			builder.withMaxTransactionRetryTime(maxTransactionRetryTime.toMillis(), TimeUnit.MILLISECONDS);

			if (serverAddressResolverClass != null) {
				builder.withResolver(BeanUtils.instantiateClass(serverAddressResolverClass));
			}
		}
	}

	public static class TrustSettings {

		public enum Strategy {

			TRUST_ALL_CERTIFICATES,

			TRUST_CUSTOM_CA_SIGNED_CERTIFICATES,

			TRUST_SYSTEM_CA_SIGNED_CERTIFICATES
		}

		/**
		 * Configures the strategy to use use.
		 */
		private TrustSettings.Strategy strategy = TRUST_ALL_CERTIFICATES;

		/**
		 * The file of the certificate to use.
		 */
		private File certFile;

		/**
		 * Flag, if hostname verification is used.
		 */
		private boolean hostnameVerificationEnabled = false;

		public TrustSettings.Strategy getStrategy() {
			return strategy;
		}

		public void setStrategy(TrustSettings.Strategy strategy) {
			this.strategy = strategy;
		}

		public File getCertFile() {
			return certFile;
		}

		public void setCertFile(File certFile) {
			this.certFile = certFile;
		}

		public boolean isHostnameVerificationEnabled() {
			return hostnameVerificationEnabled;
		}

		public void setHostnameVerificationEnabled(boolean hostnameVerificationEnabled) {
			this.hostnameVerificationEnabled = hostnameVerificationEnabled;
		}

		Config.TrustStrategy toInternalRepresentation() {
			String propertyName = Neo4jDriverProperties.PREFIX + ".config.trust-settings";

			Config.TrustStrategy internalRepresentation;
			switch (strategy) {
				case TRUST_ALL_CERTIFICATES:
					internalRepresentation = Config.TrustStrategy.trustAllCertificates();
					break;
				case TRUST_SYSTEM_CA_SIGNED_CERTIFICATES:
					internalRepresentation = Config.TrustStrategy.trustSystemCertificates();
					break;
				case TRUST_CUSTOM_CA_SIGNED_CERTIFICATES:
					if (this.certFile == null || !this.certFile.isFile()) {
						throw new InvalidConfigurationPropertyValueException(propertyName, strategy.name(),
							"Configured trust strategy requires a certificate file.");
					}
					internalRepresentation = Config.TrustStrategy.trustCustomCertificateSignedBy(certFile);
					break;
				default:
					throw new InvalidConfigurationPropertyValueException(propertyName, strategy.name(),
						"Unknown strategy.");
			}

			if (hostnameVerificationEnabled) {
				internalRepresentation.withHostnameVerification();
			} else {
				internalRepresentation.withoutHostnameVerification();
			}

			return internalRepresentation;
		}
	}
}
