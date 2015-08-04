package eu.freme.broker.security.tools;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.stereotype.Component;

/**
 * Helper class to deal with security config attributes.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 *
 */
@Component
public class AccessLevelHelper {

	SecurityConfig readAccess = new SecurityConfig("ACCESS_READ");
	SecurityConfig writeAccess = new SecurityConfig("ACCESS_WRITE");

	public AccessLevelHelper() {
		readAccess = new SecurityConfig("ACCESS_READ");
		writeAccess = new SecurityConfig("ACCESS_WRITE");
	}

	public Collection<ConfigAttribute> readAccess() {
		Collection<ConfigAttribute> list = new ArrayList<ConfigAttribute>();
		list.add(readAccess);
		return list;
	}

	public Collection<ConfigAttribute> writeAccess() {
		Collection<ConfigAttribute> list = new ArrayList<ConfigAttribute>();
		list.add(writeAccess);
		return list;
	}

	public Collection<ConfigAttribute> readWriteAccess() {
		Collection<ConfigAttribute> list = new ArrayList<ConfigAttribute>();
		list.add(writeAccess);
		list.add(readAccess);
		return list;
	}

	public boolean hasRead(Collection<ConfigAttribute> col) {
		for (ConfigAttribute ca : col) {
			if (ca.equals(readAccess)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasWrite(Collection<ConfigAttribute> col) {
		for (ConfigAttribute ca : col) {
			if (ca.equals(writeAccess)) {
				return true;
			}
		}
		return false;
	}
}
