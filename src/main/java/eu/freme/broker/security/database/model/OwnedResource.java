/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.broker.security.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */

@MappedSuperclass
public class OwnedResource {

	public enum AccessLevel {PRIVATE,PUBLIC}

	@Id
	public String id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER) //(optional=false,targetEntity = User.class)
	public User owner;

	public AccessLevel accessLevel;

	public OwnedResource(){}

	public OwnedResource(String id, User owner, AccessLevel accessLevel) {
		this.id = id;
		this.owner= owner;
		this.accessLevel = accessLevel;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}


	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString(){
		return "OwnedResource[id="+id+", owner="+owner.toString()+", accessLevel="+accessLevel.toString()+"]";
	}
}
