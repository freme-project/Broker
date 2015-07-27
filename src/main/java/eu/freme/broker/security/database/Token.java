package eu.freme.broker.security.database;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.freme.broker.security.database.User;

@Entity
@Table(name = "token")
public class Token {
	@Id
	String token;

	@ManyToOne(fetch = FetchType.EAGER)
	private User user;
	
	protected Token() {
	}

	public Token(String token, User user) {
		super();
		this.token = token;
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
