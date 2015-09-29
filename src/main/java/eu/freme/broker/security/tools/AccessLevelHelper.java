/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
