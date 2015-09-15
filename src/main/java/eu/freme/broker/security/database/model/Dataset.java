package eu.freme.broker.security.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.freme.broker.security.database.OwnedResource;
import eu.freme.broker.security.database.OwnedResource.AccessLevel;

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
