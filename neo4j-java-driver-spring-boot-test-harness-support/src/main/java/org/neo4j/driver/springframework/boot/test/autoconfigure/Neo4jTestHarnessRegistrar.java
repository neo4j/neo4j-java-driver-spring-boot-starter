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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Necessary setup to provide a Neo4j test harness instance.
 *
 * @author Michael J. Simons
 * @soundtrack NMZS - Egotrip
 * @since 4.0.0.1
 */
final class Neo4jTestHarnessRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
		BeanNameGenerator importBeanNameGenerator) {

		String neo4j = "org.neo4j.harness.Neo4j";
		String serverControls = "org.neo4j.harness.ServerControls";

		String embeddedInstanceType;
		String factoryBean;
		BeanDefinition factoryBeanDefinition;
		String factoryMethod;
		if (ClassUtils.isPresent(neo4j, this.getClass().getClassLoader())) {
			factoryBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition("org.neo4j.harness.Neo4jBuilders")
				.setFactoryMethod("newInProcessBuilder")
				.setInitMethodName("withDisabledServer")
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();
			embeddedInstanceType = neo4j;
			factoryBean = importBeanNameGenerator.generateBeanName(factoryBeanDefinition, registry);
			factoryMethod = "build";
		} else if (ClassUtils.isPresent(serverControls, this.getClass().getClassLoader())) {
			factoryBeanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition("org.neo4j.harness.TestServerBuilders")
				.setFactoryMethod("newInProcessBuilder")
				.setScope(BeanDefinition.SCOPE_SINGLETON)
				.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
				.getBeanDefinition();
			embeddedInstanceType = serverControls;
			factoryBean = importBeanNameGenerator.generateBeanName(factoryBeanDefinition, registry);
			factoryMethod = "newServer";
		} else {
			throw new IllegalArgumentException(
				"Missing module: neo4j-java-driver-spring-boot-test-harness-?x-support");
		}

		registry.registerBeanDefinition(factoryBean, factoryBeanDefinition);

		final BeanDefinition beanDefinition = BeanDefinitionBuilder
			.rootBeanDefinition(embeddedInstanceType)
			.setFactoryMethodOnBean(factoryMethod, factoryBean)
			.setScope(BeanDefinition.SCOPE_SINGLETON)
			.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
			.getBeanDefinition();

		final String beanName = importBeanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}
}
