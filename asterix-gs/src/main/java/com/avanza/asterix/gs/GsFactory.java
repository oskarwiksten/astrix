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
package com.avanza.asterix.gs;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.j_spaces.core.IJSpace;
/**
 * 
 * @author Elias Lindholm (elilin)
 *
 */
public class GsFactory  {
	
	private final String spaceUrl;

	public GsFactory(String spaceUrl) {
		this.spaceUrl = spaceUrl;
	}

	public GigaSpace create() {
		IJSpace space = new UrlSpaceConfigurer(spaceUrl).create();
		return new GigaSpaceConfigurer(space).create();
	}
	
}
