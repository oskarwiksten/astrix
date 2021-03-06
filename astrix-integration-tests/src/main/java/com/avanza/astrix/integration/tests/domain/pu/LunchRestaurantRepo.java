/*
 * Copyright 2014 Avanza Bank AB
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
package com.avanza.astrix.integration.tests.domain.pu;

import java.util.Arrays;
import java.util.List;

import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;

import com.avanza.astrix.integration.tests.domain.api.LunchRestaurant;

public class LunchRestaurantRepo {

	private final GigaSpace gigaSpace;

	@Autowired
	public LunchRestaurantRepo(GigaSpace gigaSpace) {
		this.gigaSpace = gigaSpace;
	}
	
	public List<LunchRestaurant> findByFoodType(String foodType) {
		LunchRestaurant template = new LunchRestaurant();
		template.setFoodType(foodType);
		return Arrays.asList(gigaSpace.readMultiple(template));
	}

	public void writeLunchRestaurant(LunchRestaurant restaurant) {
		this.gigaSpace.write(restaurant);
	}

	public LunchRestaurant getByName(String name) {
		return this.gigaSpace.readById(LunchRestaurant.class, name);
	}
}
