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
package se.avanzabank.asterix.context;
/**
 * Asterix-plugins that require 'external' dependencies (to be configured by the user of the framework)
 * implements this interface to get a runtime reference to the given dependency. <p>
 * 
 * In order to be able to autowire the dependencies a ExternalDependencyBean is used. <p>
 * 
 * @author Elias Lindholm (elilin)
 *
 * @param <T>
 */
public interface ExternalDependencyAware<T extends ExternalDependencyBean> {
	
	void setDependency(ExternalDependency<T> instance);
	
	Class<T> getDependencyBeanClass();

}
