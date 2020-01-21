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
package org.neo4j.driver.springframework.boot.test.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.Driver;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverAutoConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Configures an instance of the test harness, either Neo4j 3.5.x ({@code ServerControls}) or 4.0.x ({@code Neo4j})
 *
 * @author Michael J. Simons
 * @soundtrack Body Count - Born Dead
 * @since 4.0.0.1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Driver.class)
@AutoConfigureBefore(Neo4jDriverAutoConfiguration.class)
@Import(TestHarnessDriverConfiguration.class)
public class Neo4jTestHarnessAutoConfiguration {

	public static final String TEST_HARNESS_3X = "org.neo4j.harness.ServerControls";
	public static final String TEST_HARNESS_4X = "org.neo4j.harness.Neo4j";

	static final Log LOG = LogFactory.getLog(Neo4jTestHarnessAutoConfiguration.class);
	static final String BEAN_NAME = "neo4jTestHarness";

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = TEST_HARNESS_3X)
	@ConditionalOnMissingBean(type = TEST_HARNESS_3X)
	@Import(ServerControlsRegistrar.class)
	static class ServerControlsConfiguration {
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(name = TEST_HARNESS_4X)
	@ConditionalOnMissingBean(type = TEST_HARNESS_4X)
	@Import(Neo4jRegistrar.class)
	static class Neo4jConfiguration {
	}

	static class ServerControlsRegistrar implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata,
			BeanDefinitionRegistry registry,
			BeanNameGenerator importBeanNameGenerator
		) {
			LOG.debug("Using Neo4j test harness 3.5");

			BeanDefinition factoryBeanDefinition;
			factoryBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition("org.neo4j.harness.TestServerBuilders")
				.setFactoryMethod("newInProcessBuilder")
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();

			String factoryBean = importBeanNameGenerator.generateBeanName(factoryBeanDefinition, registry);
			registry.registerBeanDefinition(factoryBean, factoryBeanDefinition);

			BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(TEST_HARNESS_3X)
				.setFactoryMethodOnBean("newServer", factoryBean)
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();

			registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
		}
	}

	static class Neo4jRegistrar implements ImportBeanDefinitionRegistrar {

		@Override
		public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata,
			BeanDefinitionRegistry registry,
			BeanNameGenerator importBeanNameGenerator
		) {
			LOG.debug("Using Neo4j test harness 4.0");

			BeanDefinition factoryBeanDefinition;
			factoryBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition("org.neo4j.harness.Neo4jBuilders")
				.setFactoryMethod("newInProcessBuilder")
				.setInitMethodName("withDisabledServer")
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();

			String factoryBean = importBeanNameGenerator.generateBeanName(factoryBeanDefinition, registry);
			registry.registerBeanDefinition(factoryBean, factoryBeanDefinition);

			BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(TEST_HARNESS_4X)
				.setFactoryMethodOnBean("build", factoryBean)
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();

			registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
		}
	}
}
