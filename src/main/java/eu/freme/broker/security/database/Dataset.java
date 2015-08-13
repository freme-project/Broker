package eu.freme.broker.security.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
@Entity
@Table(name = "dataset")
public class Dataset extends OwnedResource{

    @Id
    @Column(name = "id")
    protected String id;

    @JsonIgnore
    @ManyToOne(optional=false,targetEntity = User.class)
    protected User owner;

    Dataset() {
    }

    public Dataset(String id, User owner, AccessLevel accessLevel) {
        this.id = id;
        this.owner= owner;
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
