package eu.freme.broker.security.token;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import eu.freme.common.security.database.User;

@Entity
public class Token {
	@Id
	String token;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "name")
	private User user;

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
