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

import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Necessary setup to provide a {@link org.neo4j.harness.ServerControls test harness} instance.
 *
 * @author Michael J. Simons
 * @soundtrack NMZS - Egotrip
 * @since 4.0.0.1
 */
final class Neo4jTestHarnessRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
		BeanNameGenerator importBeanNameGenerator) {

		final BeanDefinition beanDefinition = BeanDefinitionBuilder
			.rootBeanDefinition(Neo4jTestHarnessFactory.class)
			.setScope(BeanDefinition.SCOPE_SINGLETON)
			.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
			.getBeanDefinition();

		final String beanName = importBeanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	static class Neo4jTestHarnessFactory implements FactoryBean<ServerControls> {

		@Override
		public ServerControls getObject() {
			return TestServerBuilders.newInProcessBuilder()
				.newServer();
		}

		@Override
		public Class<?> getObjectType() {
			return ServerControls.class;
		}
	}
}
