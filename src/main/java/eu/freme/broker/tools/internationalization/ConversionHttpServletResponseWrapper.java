package eu.freme.broker.tools.internationalization;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.input.ReaderInputStream;

import eu.freme.i18n.api.EInternationalizationAPI;
import eu.freme.i18n.okapi.nif.converter.ConversionException;

/**
 * ConversionHttpServletResponseWrapper collects the response of a normal API
 * request and uses the e-Internationalization API to convert the the response
 * back to a source format. So it converts NIF to HTML, XLIFF, ...
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class ConversionHttpServletResponseWrapper extends
		HttpServletResponseWrapper {

	/*
	 * this reader holds the input of the original request represented in turtle
	 */
	InputStream markupInTurtle;

	/*
	 * this stream takes original file and
	 */
	DummyOutputStream conversionStream;

	EInternationalizationAPI api;

	public ConversionHttpServletResponseWrapper(HttpServletResponse response,
			EInternationalizationAPI api, InputStream originalRequest,
			String informat, String outformat) throws ConversionException,
			IOException {
		super(response);

		this.api = api;
		markupInTurtle = new ReaderInputStream(api.convertToTurtleWithMarkups(
				originalRequest, informat));
		//originalOutputStream = response.getOutputStream();
		conversionStream = new DummyOutputStream();
	}

	public ServletOutputStream getOutputStream() {
		return conversionStream;
	}

	public byte[] writeBackToClient() throws IOException {
		InputStream enrichedData = conversionStream.getInputStream();
		Reader reader = api.convertBack(markupInTurtle, enrichedData);
		BufferedInputStream is = new BufferedInputStream(new ReaderInputStream(
				reader));
		byte[] buffer = new byte[1024];
		int read = 0;
		long length = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((read = is.read(buffer)) != -1) {
			length += read;
			baos.write(buffer, 0, read);
		}
		is.close();
		setContentLengthLong(length);
		
		return baos.toByteArray();
	}

	@Override
	public void flushBuffer() throws IOException {
		super.flushBuffer();
	}

	class DummyOutputStream extends ServletOutputStream{
		
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
		}

		@Override
		public void write(int b) throws IOException {
			buffer.write(b);
		}
		
		public InputStream getInputStream(){
			return new ByteArrayInputStream(buffer.toByteArray());
		}
	}

}
