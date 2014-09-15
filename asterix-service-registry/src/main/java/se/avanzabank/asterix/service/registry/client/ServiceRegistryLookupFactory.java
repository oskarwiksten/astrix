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
package se.avanzabank.asterix.service.registry.client;

import java.util.Arrays;
import java.util.List;

import se.avanzabank.asterix.context.AsterixApiDescriptor;
import se.avanzabank.asterix.context.AsterixBeanAware;
import se.avanzabank.asterix.context.AsterixBeans;
import se.avanzabank.asterix.context.AsterixFactoryBean;
import se.avanzabank.asterix.context.AsterixPlugins;

public class ServiceRegistryLookupFactory<T> implements AsterixFactoryBean<T>, AsterixBeanAware {

	private Class<T> api;
	private AsterixApiDescriptor descriptor;
	private AsterixBeans beans;
	private AsterixPlugins plugins;

	public ServiceRegistryLookupFactory(AsterixApiDescriptor descriptor,
										Class<T> api,
										AsterixPlugins plugins) {
		this.descriptor = descriptor;
		this.api = api;
		this.plugins = plugins;
	}

	@Override
	public T create(String qualifier) {
		AsterixServiceRegistry serviceRegistry = beans.getBean(AsterixServiceRegistry.class);
		AsterixServiceProperties serviceProperties = serviceRegistry.lookup(api, qualifier); // TODO: might fail
		if (serviceProperties == null) {
			throw new RuntimeException(String.format("Misssing entry in service-registry api=%s qualifier=%s: ", api.getName(), qualifier));
		}
		AsterixServiceRegistryComponent serviceRegistryComponent = getComponent(serviceProperties);
		return serviceRegistryComponent.createService(descriptor, api, serviceProperties);
	}
	
	private AsterixServiceRegistryComponent getComponent(AsterixServiceProperties serviceProperties) {
		String componentName = serviceProperties.getComponent();
		if (componentName == null) {
			throw new IllegalArgumentException("Expected a componentName to be set on serviceProperties: " + serviceProperties);
		}
		return plugins.getPlugin(AsterixServiceRegistryComponents.class).getComponent(componentName);
	}

	@Override
	public List<Class<?>> getBeanDependencies() {
		return Arrays.<Class<?>>asList(AsterixServiceRegistry.class);
	}

	@Override
	public Class<T> getBeanType() {
		return this.api;
	}

	@Override
	public void setAsterixBeans(AsterixBeans beans) {
		this.beans = beans;
	}

}