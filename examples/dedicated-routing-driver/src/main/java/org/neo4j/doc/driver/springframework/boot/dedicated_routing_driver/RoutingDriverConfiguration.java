package org.neo4j.doc.driver.springframework.boot.dedicated_routing_driver;

// tag::custom-config[]

import java.net.URI;
import java.util.List;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutingDriverConfiguration {

	@Bean
	public Driver neo4jDriver(
		@Value("${my.physical.neo4j.uris}") List<URI> routingUris, // <1>
		Neo4jDriverProperties neo4jDriverProperties // <2>
	) {

		AuthToken authToken = neo4jDriverProperties.getAuthToken(); // <3>
		Config config = neo4jDriverProperties.asDriverConfig();

		return GraphDatabase.routingDriver(routingUris, authToken, config); // <4>
	}
}
// end::custom-config[]