package eu.freme.broker.security.database;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.freme.broker.security.database.User;
/**
 * @author Jan Nehring - jan.nehring@dfki.de
 */
@Entity
@Table(name = "token")
public class Token {
	@Id
	String token;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;
	
	Date creationDate;
	
	Date lastUsedDate;
	
	protected Token() {
	}

	public Token(String token, User user, Date creationDate, Date lastUsedDate) {
		super();
		this.token = token;
		this.user = user;
		this.creationDate = creationDate;
		this.lastUsedDate = lastUsedDate;
	}

	public Token(String token, User user) {
		super();
		this.token = token;
		this.user = user;
		this.creationDate = new Date();
		this.lastUsedDate = creationDate;
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

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUsedDate() {
		return lastUsedDate;
	}

	public void setLastUsedDate(Date lastUsedDate) {
		this.lastUsedDate = lastUsedDate;
	}

	
}
