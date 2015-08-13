package eu.freme.broker.security.database;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * @author Jonathan Sauder jsauder@campus.tu-berlin.de
 */


public class OwnedResource {

	public enum AccessLevel {PRIVATE,PUBLIC}

	protected AccessLevel accessLevel;

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

}
