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
package eu.freme.broker.integration_tests.pipelines;

import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.eservices.pipelines.requests.RequestFactory;
import eu.freme.eservices.pipelines.requests.SerializedRequest;
import org.apache.http.HttpStatus;
import org.junit.Test;

/**
 * @author Gerald Haesendonck
 */
public class EntityLinkTest extends PipelinesCommon {

	/**
	 * e-Entity using the Spotlight NER and e-Link using template 3 (Geo pos). All should go well.
	 * @throws UnirestException
	 */
	@Test
	public void testSpotlight() throws UnirestException {
		String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
		SerializedRequest entityRequest = RequestFactory.createEntitySpotlight(data, "en");
		SerializedRequest linkRequest = RequestFactory.createLink("3");	// Geo pos

		sendRequest(HttpStatus.SC_OK, entityRequest, linkRequest);
	}

	/**
	 * e-Entity using FREME NER with database viaf and e-Link using template 3 (Geo pos). All should go well.
	 * @throws UnirestException
	 */
	@Test
	public void testFremeNER() throws UnirestException {
		String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
		SerializedRequest entityRequest = RequestFactory.createEntityFremeNER(data, "en", "viaf");
		SerializedRequest linkRequest = RequestFactory.createLink("3");	// Geo pos

		sendRequest(HttpStatus.SC_OK, entityRequest, linkRequest);
	}

	/**
	 * e-Entity using an unexisting data set to test error reporting.
	 */
	@Test
	public void testWrongDatasetEntity() throws UnirestException {
		String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
		SerializedRequest entityRequest = RequestFactory.createEntityFremeNER(data, "en", "anunexistingdatabase");
		SerializedRequest linkRequest = RequestFactory.createLink("3");	// Geo pos

		sendRequest(HttpStatus.SC_BAD_REQUEST, entityRequest, linkRequest);
	}

	/**
	 * e-Entity using an unexisting language set to test error reporting.
	 */
	@Test
	public void testWrongLanguageEntity() throws UnirestException {
		String data = "This summer there is the Zomerbar in Antwerp, one of the most beautiful cities in Belgium.";
		SerializedRequest entityRequest = RequestFactory.createEntityFremeNER(data, "zz", "viaf");
		SerializedRequest linkRequest = RequestFactory.createLink("3");	// Geo pos

		sendRequest(HttpStatus.SC_BAD_REQUEST, entityRequest, linkRequest);
	}
}
