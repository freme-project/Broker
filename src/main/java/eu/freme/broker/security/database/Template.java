package eu.freme.broker.security.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.eclipse.persistence.internal.codegen.AccessLevel;

import javax.persistence.*;
import java.util.List;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */
@Entity
@Table(name = "template")
public class Template {

	public enum AccessLevel {PRIVATE,PUBLIC}

	@Id
	@Column(name = "id")
	private String id;

	@JsonIgnore
	@ManyToOne(optional= false, targetEntity = User.class )
	private User owner;

	private AccessLevel accessLevel;

	protected Template() {
	}

	public Template(String id, User owner, AccessLevel accessLevel) {
		this.id = id;
		this.owner= owner;
		this.accessLevel = accessLevel;
	}

	@Override
	public String toString() {
		return "Template{" +
				"id='" + id + '\'' +
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
