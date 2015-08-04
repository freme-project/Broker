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
