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
package eu.freme.broker.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.input.ReaderInputStream;

public class BodySwappingServletRequest extends HttpServletRequestWrapper {

	private Reader body;

	public BodySwappingServletRequest(HttpServletRequest request, Reader body) {
		super(request);
		this.body = body;
	}

	public BufferedReader getReader() {
		return new BufferedReader(body);
	}
	
	public ServletInputStream getInputStream(){
		return new ServletInputStreamWrapper(body);
	}
	
	@Override
	public String getParameter(String name){
		if( name.toLowerCase().equals("informat")){
			return "turtle";
		} if( name.equals("input") ){
			return null;
		} else{
			return super.getParameter(name);
		}
	}
	
    @Override
    public Map<String, String[]> getParameterMap()
    {
    	TreeMap<String, String[]> map = new TreeMap<String, String[]>();
    	map.putAll(super.getParameterMap());
    	map.put("informat", new String[]{"turtle"});
    	map.remove("input");
    	
        return Collections.unmodifiableMap(map);
    }   
    
    @Override
    public Enumeration<String> getParameterNames()
    {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(final String name)
    {
        return getParameterMap().get(name);
    }



	private class ServletInputStreamWrapper extends ServletInputStream {

		private boolean finished = false;
		private ReaderInputStream ris;

		ServletInputStreamWrapper(Reader reader) {
			ris = new ReaderInputStream(reader);
		}

		@Override
		public int read() throws IOException {
			int i = ris.read();
			if( i == -1 ){
				finished = true;
			}
			return i;
		}

		@Override
		public boolean isFinished() {
			return finished;
		}

		@Override
		public boolean isReady() {
			return !finished;
		}

		@Override
		public void setReadListener(ReadListener listener) {
		}

	}
}
