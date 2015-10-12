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
package eu.freme.broker.tools.internationalization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.io.input.ReaderInputStream;

public class BodySwappingServletResponse extends HttpServletResponseWrapper{

	private ServletOutputStreamWrapper outputStream;

	public BodySwappingServletResponse(HttpServletResponse response, OutputStream newOutput) {
		super(response);
		outputStream = new ServletOutputStreamWrapper(newOutput);
	}

	public ServletOutputStream getOutputStream(){
		return outputStream;
	}

	private class ServletOutputStreamWrapper extends ServletOutputStream {

		private boolean finished = false;
		OutputStream os;
		
		public ServletOutputStreamWrapper(OutputStream os) {
			this.os = os;
		}

		@Override
		public boolean isReady() {
			return !finished;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
		}

		@Override
		public void write(int b) throws IOException {
		}
		
		public void writeStream(InputStream is) throws IOException{
			int b=0;
			while((b=is.read()) != -1){
				write(b);
			}
		}
	}
}
