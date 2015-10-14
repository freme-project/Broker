package eu.freme.broker.tools.internationalization;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	ConversionOutputStream conversionStream;

	/*
	 * this holds the output stream that can be written to the user.
	 */
	ServletOutputStream originalOutputStream;

	EInternationalizationAPI api;

	public ConversionHttpServletResponseWrapper(HttpServletResponse response,
			EInternationalizationAPI api, InputStream originalRequest,
			String informat, String outformat) throws ConversionException,
			IOException {
		super(response);

		this.api = api;
		markupInTurtle = new ReaderInputStream(api.convertToTurtleWithMarkups(
				originalRequest, informat));
		originalOutputStream = response.getOutputStream();
		conversionStream = new ConversionOutputStream();
	}

	public ServletOutputStream getOutputStream() {
		return conversionStream;
	}

	public void writeBackToClient(byte[] data) throws IOException {
		InputStream enrichedData = new ByteArrayInputStream(data);
		Reader reader = api.convertBack(markupInTurtle, enrichedData);
		BufferedInputStream is = new BufferedInputStream(new ReaderInputStream(
				reader));
		byte[] buffer = new byte[1024];
		int read = 0;
		int length = 0;
		while ((read = is.read(buffer)) != -1) {
			length += read;
			originalOutputStream.write(buffer, 0, read);
		}
		is.close();
		this.setContentLength(length);
		originalOutputStream.flush();
		originalOutputStream.close();
	}

	@Override
	public void flushBuffer() throws IOException {
		super.flushBuffer();
	}

	class ConversionOutputStream extends ServletOutputStream {

		/*
		 * this buffer records the output stream of the underlying API call.
		 */
		ByteArrayOutputStream baos;
		boolean finished = false;

		public ConversionOutputStream() {
			baos = new ByteArrayOutputStream();
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
			if (!finished) {
				baos.write(b);
			}
		}

		@Override
		public void write(byte[] b) throws IOException {
			if (!finished) {
				baos.write(b);
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (!finished) {
				baos.write(b, off, len);
			}
		}

		@Override
		public void flush() throws IOException {
			super.flush();
			if (!finished) {
				byte[] data = baos.toByteArray();
				baos.reset();
				writeBackToClient(data);
			}
			finished = true;
		}
	}
}
