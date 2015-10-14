package eu.freme.broker.tools.internationalization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.springframework.context.annotation.Scope;


import org.springframework.stereotype.Service;

@Service
@Scope("release")
public class EInternationalizationTempFile extends ServletOutputStream {

	File file;
	FileOutputStream fos;
	long creationTime;

	public EInternationalizationTempFile()
			throws IOException {
		file = File.createTempFile("freme-i18n-temp", "");
		fos = new FileOutputStream(file);
		creationTime = System.currentTimeMillis();
	}

	@Override
	public void close() throws IOException {
		fos.close();
	}

	public void delete() {
		file.delete();
	}

	@Override
	public void write(int b) throws IOException {
		fos.write(b);
	}
	
	public File getFile(){
		return file;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener listener) {
		
	}
}
