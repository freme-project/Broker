package eu.freme.broker.tools.internationalization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class BufferStream extends InputStream{
	
	ByteArrayOutputStream baos;
	InputStream innerStream;	
	
	public BufferStream(InputStream innerStream){
		baos = new ByteArrayOutputStream();
		this.innerStream = innerStream;
	}

	@Override
	public int read() throws IOException {
		int i = innerStream.read();
		if( i!=-1){
			baos.write(i);			
		}
		return i;
	}
	
	public InputStream getInputStream(){
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
