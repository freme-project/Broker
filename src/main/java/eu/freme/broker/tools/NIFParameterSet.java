/**
 * Copyright (C) 2015 Felix Sasaki (Felix.Sasaki@dfki.de)
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
package eu.freme.broker.tools;

import eu.freme.conversion.rdf.RDFConstants.RDFSerialization;

/**
 * describes nif parameters as defined in
 * http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class NIFParameterSet {

	String input;
	RDFSerialization informat;
	RDFSerialization outformat;
	String prefix;
	

	public NIFParameterSet(String input, RDFSerialization informat,
			RDFSerialization outformat, String prefix) {
		super();
		this.input = input;
		this.informat = informat;
		this.outformat = outformat;
		this.prefix = prefix;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public RDFSerialization getInformat() {
		return informat;
	}

	public void setInformat(RDFSerialization informat) {
		this.informat = informat;
	}

	public RDFSerialization getOutformat() {
		return outformat;
	}

	public void setOutformat(RDFSerialization outformat) {
		this.outformat = outformat;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
