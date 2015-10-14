package eu.freme.broker.tools.internationalization;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.vfs.util.Os;

public class TestWrapper extends HttpServletResponseWrapper {
	
	ServletOutputStream os;
	
	public TestWrapper(HttpServletResponse response) throws IOException {
		super(response);
		os = new CapitalizeStream(response.getOutputStream());
	}

	public ServletOutputStream getOutputStream(){
		return os;
	}
	
	class CapitalizeStream extends ServletOutputStream{

		OutputStream os;
		
		public CapitalizeStream(OutputStream os){
			this.os = os;
		}
		
		public void close() throws IOException{
			os.close();
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
		}

		@Override
		public void write(int b) throws IOException {
			char orig = (char)b;
			char upper = Character.toUpperCase(orig);
			os.write((int)upper);
		}
	}

}
