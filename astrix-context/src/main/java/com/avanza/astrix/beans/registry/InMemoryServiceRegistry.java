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
package com.avanza.astrix.beans.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.avanza.astrix.beans.core.AstrixSettings;
import com.avanza.astrix.beans.service.AstrixServiceProperties;
import com.avanza.astrix.config.BooleanSetting;
import com.avanza.astrix.config.DynamicConfigSource;
import com.avanza.astrix.config.DynamicPropertyListener;
import com.avanza.astrix.config.GlobalConfigSourceRegistry;
import com.avanza.astrix.config.LongSetting;
import com.avanza.astrix.config.MapConfigSource;
import com.avanza.astrix.config.MutableConfigSource;
import com.avanza.astrix.config.Setting;
import com.avanza.astrix.context.AstrixDirectComponent;
import com.avanza.astrix.provider.component.AstrixServiceComponentNames;
/**
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public class InMemoryServiceRegistry implements DynamicConfigSource, AstrixServiceRegistry, MutableConfigSource {
	
	private final MapConfigSource configSource = new MapConfigSource();
	private String id;
	private String configSourceId;
	private InMemoryServiceRegistryImpl serviceRegistry = new InMemoryServiceRegistryImpl();
	
	public InMemoryServiceRegistry() {
		this.id = AstrixDirectComponent.register(AstrixServiceRegistry.class, serviceRegistry);
		this.configSourceId = GlobalConfigSourceRegistry.register(this);
		this.configSource.set(AstrixSettings.SERVICE_REGISTRY_URI, getServiceUri());
	}
	
	@Override
	public List<AstrixServiceRegistryEntry> listServices() {
		return serviceRegistry.listServices();
	}
	
	@Override
	public <T> AstrixServiceRegistryEntry lookup(String type, String qualifier) {
		return serviceRegistry.lookup(type, qualifier);
	}
	
	@Override
	public <T> void register(AstrixServiceRegistryEntry properties, long lease) {
		serviceRegistry.register(properties, lease);
	}
	
	public String getConfigSourceId() {
		return configSourceId;
	}
	
	public void clear() {
		this.serviceRegistry.clear();
	}

	/**
	 * 
	 * @return
	 * @deprecated replaced by {@link AstrixSettings#SERVICE_REGISTRY_URI}
	 */
	@Deprecated
	public String getConfigEntryName() {
		return AstrixSettings.SERVICE_REGISTRY_URI_PROPERTY_NAME;
	}
	
	public String getServiceUri() {
		return AstrixServiceComponentNames.DIRECT + ":" + this.id;
	}

	public void set(String settingName, String value) {
		this.configSource.set(settingName, value);
	}
	
	public void set(String settingName, long value) {
		this.configSource.set(settingName, Long.toString(value));
	}
	
	@Override
	public <T> void set(Setting<T> setting, T value) {
		this.configSource.set(setting, value);
	}
	
	@Override
	public void set(BooleanSetting setting, boolean value) {
		this.configSource.set(setting, value);
	}
	
	@Override
	public void set(LongSetting setting, long value) {
		this.configSource.set(setting, value);
	}
	
	
	public <T> void registerProvider(Class<T> api, T provider, String subsystem) {
		AstrixServiceRegistryClientImpl serviceRegistryClient = new AstrixServiceRegistryClientImpl(this.serviceRegistry, subsystem);
		serviceRegistryClient.register(api, AstrixDirectComponent.registerAndGetProperties(api, provider), 60_000);
	}
	
	/**
	 * Registers a provider for a given service that belongs to the 'default' subsystem.
	 * 
	 * @param api
	 * @param provider
	 */
	public <T> void registerProvider(Class<T> api, T provider) {
		registerProvider(api, provider, "default");
	}
	
	@Override
	public String get(String propertyName) {
		return configSource.get(propertyName);
	}

	@Override
	public String get(String propertyName, DynamicPropertyListener<String> propertyChangeListener) {
		return configSource.get(propertyName, propertyChangeListener);
	}
	
	private static class InMemoryServiceRegistryImpl implements AstrixServiceRegistry {
		
		private Map<ServiceKey, AstrixServiceRegistryEntry> servicePropertiesByKey = new ConcurrentHashMap<>();
		
		@Override
		public <T> AstrixServiceRegistryEntry lookup(String type, String qualifier) {
			return this.servicePropertiesByKey.get(new ServiceKey(type, qualifier));
		}
		
		
		@Override
		public <T> void register(AstrixServiceRegistryEntry properties, long lease) {
			ServiceKey key = new ServiceKey(properties.getServiceBeanType(), properties.getServiceProperties().get(AstrixServiceProperties.QUALIFIER));
			this.servicePropertiesByKey.put(key, properties);
		}
		
		void clear() {
			this.servicePropertiesByKey.clear();
		}
		
		@Override
		public List<AstrixServiceRegistryEntry> listServices() {
			return new ArrayList<>(servicePropertiesByKey.values());
		}
		
		
	}
}