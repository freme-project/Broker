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
import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.security.tools.AccessLevelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */

@MappedSuperclass
public class OwnedResource {

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

	@Lob
	public String content;

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String toString(){
		return "OwnedResource[id="+id+", owner="+owner.toString()+", visibility="+ visibility.toString()+"]";
	}
}
