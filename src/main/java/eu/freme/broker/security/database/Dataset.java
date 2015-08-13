package eu.freme.broker.security.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */

@Entity
@Table(name = "dataset")
public class Dataset {

	public enum AccessLevel {PRIVATE,PUBLIC}


	@Id
	@Column(name = "name")
	private String name;

	@JsonIgnore
	@ManyToOne(optional=false,targetEntity = User.class)
	private User owner;

	private AccessLevel accessLevel;


	protected Dataset() {
	}

	public Dataset(String name, User owner, AccessLevel accessLevel) {
		this.name = name;
		this.owner= owner;
		this.accessLevel = accessLevel;
	}

	@Override
	public String toString() {
		return "Template{" +
				"name='" + name + '\'' +
				", owner=" + owner +
				", accessLevel=" + accessLevel +
				'}';
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

	public String getName() {
		return name;
	}

	public void setId(String name) {
		this.name = name;
	}
}
