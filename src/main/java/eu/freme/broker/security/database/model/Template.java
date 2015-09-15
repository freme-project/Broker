package eu.freme.broker.security.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.freme.broker.security.database.OwnedResource;
import eu.freme.broker.security.database.OwnedResource.AccessLevel;

import javax.persistence.*;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
@Entity
@Table(name = "template")
public class Template extends OwnedResource {

    @Id
    @Column(name = "id")
    private String id;

    @JsonIgnore
    @ManyToOne(optional=false,targetEntity = User.class)
    private User owner;
    private AccessLevel accessLevel;
    Template(){ }


    public Template(String id, User owner, AccessLevel accessLevel) {
        this.id = id;
        this.owner= owner;
        this.accessLevel = accessLevel;
        System.out.println(this.accessLevel+"kikikik");
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


}
