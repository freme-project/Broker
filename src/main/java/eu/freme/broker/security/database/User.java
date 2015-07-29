package eu.freme.broker.security.database;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


import javax.persistence.CascadeType;


import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Entity
@Table(name = "user")
public class User {

	public static final String roleUser = "ROLE_USER";
	public static final String roleAdmin = "ROLE_ADMIN";

	@Id
	@Column(name = "name")
	private String name;
	
	@JsonIgnore
	private String password;

	private String role;
	
	@OneToMany(mappedBy = "user", cascade=CascadeType.ALL)
	private List<Token> tokens;

	protected User() {
	}

	public User(String name, String password, String role) {
		this.name = name;
		this.password = password;
		this.role = role;
	}

	@Override
	public String toString() {
		return String.format("User[name=%s, role=%s]", name, role);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public List<Token> getTokens() {
		return tokens;
	}

}
