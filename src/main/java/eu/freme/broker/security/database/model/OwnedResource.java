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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import eu.freme.broker.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.io.IOException;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */

@MappedSuperclass
public class OwnedResource implements JsonSerializable {

	public enum Visibility {
		PRIVATE,
		PUBLIC;
		public static Visibility getByString(String value){
			if(value!=null && value.toLowerCase().equals("private"))
				return PRIVATE;
			if(value!=null && !value.toLowerCase().equals("public"))
				throw new BadRequestException("Wrong value for visibility level: \""+value+"\". Has to be either \"private\" or \"public\".");
			return PUBLIC;
		}
	}

	@Id
	public String id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER) //(optional=false,targetEntity = User.class)
	public User owner;

	public Visibility visibility;

	public OwnedResource(){}

	public OwnedResource(String id, User owner, Visibility visibility) {
		this.id = id;
		this.owner = owner;
		this.visibility = visibility;
	}

	public OwnedResource(String id, Visibility visibility) throws AccessDeniedException{
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		this.owner = (User) authentication.getPrincipal();
		this.id = id;
		this.visibility = visibility;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
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
		return "OwnedResource[id="+id+", owner="+owner.toString()+", visibility="+ visibility.toString()+"]";
	}

	@Override
	public void serialize(com.fasterxml.jackson.core.JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
		jsonGenerator.writeStartObject();
		jsonGenerator.writeStringField("id", this.getId());
		jsonGenerator.writeStringField("visibility", this.getVisibility().name());
		jsonGenerator.writeEndObject();
	}

	@Override
	public void serializeWithType(JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonProcessingException {

	}
}
