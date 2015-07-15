package eu.freme.broker.tools;

import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;

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
