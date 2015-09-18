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

import eu.freme.broker.security.database.OwnedResource;

import javax.persistence.*;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
@Entity
@Table(name = "dataset")
public class Dataset extends OwnedResource{

    @Id
    String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER) //(optional=false,targetEntity = User.class)
    private User owner;

    private AccessLevel accessLevel;
    protected Dataset() {
    }

    public Dataset(String id, User owner, AccessLevel accessLevel) {
        super();
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
        return "Dataset[id="+id+", owner="+owner.toString()+", accessLevel="+accessLevel.toString()+"]";
    }

}
