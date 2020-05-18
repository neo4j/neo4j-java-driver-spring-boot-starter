/*
 * Copyright (c) 2019-2020 "Neo4j,"
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
package org.neo4j.doc.driver.springframework.boot.ogm_integration;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.neo4j.bookmark.BookmarkManager;
import org.springframework.data.neo4j.web.support.OpenSessionInViewInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This tests demonstrates that the automatic configuration for the driver integrates will with Spring Boot, meaning that
 * there's still a transaction manager, open session in view interceptor and bookmark support.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ImportAutoConfiguration(Neo4jDriverAutoConfiguration.class)
// This is necessary, as the test slice cannot know about it
@ContextConfiguration(initializers = { ApplicationTest.Initializer.class })
public class ApplicationTest {

	@Container
	private static final Neo4jContainer neo4jContainer = new Neo4jContainer<>();

	@Autowired
	private Driver driver;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private BookmarkManager bookmarkManager;

	@Test
	void contextLoads(@Autowired ObjectProvider<OpenSessionInViewInterceptor> openSessionInViewInterceptorProvider) {
		assertThat(driver).isNotNull();
		assertThat(sessionFactory).isNotNull();
		assertThat(transactionManager).isNotNull();
		assertThat(bookmarkManager).isNotNull();
		openSessionInViewInterceptorProvider.ifAvailable(i -> fail("There should not be an OpenSessionInViewInterceptor by default."));
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {

			TestPropertyValues.of(
				"org.neo4j.driver.uri=" + neo4jContainer.getBoltUrl(),
				"org.neo4j.driver.authentication.username=neo4j",
				"org.neo4j.driver.authentication.password=" + neo4jContainer.getAdminPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
