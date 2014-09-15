/*
 * Copyright 2014-2015 Avanza Bank AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.avanzabank.asterix.remoting.plugin.provider;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import se.avanzabank.asterix.provider.remoting.AsterixRemoteServiceExport;
import se.avanzabank.asterix.service.registry.client.AsterixServiceProperties;
import se.avanzabank.asterix.service.registry.server.ServiceRegistryExporter;

public class AsterixRemotingServiceRegistryExporter implements ServiceRegistryExporter, ApplicationContextAware {
	
	public static final String SPACE_NAME_PROPERTY = "space";
	
	private ApplicationContext applicationContext;
	private GigaSpace gigaSpace;
	
	@Autowired
	public AsterixRemotingServiceRegistryExporter(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public List<AsterixServiceProperties> getProvidedServices() {
		List<AsterixServiceProperties> result = new ArrayList<>();
		for (Object service : applicationContext.getBeansWithAnnotation(AsterixRemoteServiceExport.class).values()) {
			AsterixRemoteServiceExport remoteServiceExport = service.getClass().getAnnotation(AsterixRemoteServiceExport.class);
			for (Class<?> providedApi : remoteServiceExport.value()) {
				AsterixServiceProperties serviceProperties = new AsterixServiceProperties();
				if (!providedApi.isAssignableFrom(service.getClass())) {
					throw new IllegalArgumentException("Cannot export: " + service.getClass() + " as " + providedApi);
				}
				serviceProperties.setApi(providedApi);
				serviceProperties.setProperty(SPACE_NAME_PROPERTY, gigaSpace.getSpace().getName());
				result.add(serviceProperties);
			}
		}
		return result;
	}

}