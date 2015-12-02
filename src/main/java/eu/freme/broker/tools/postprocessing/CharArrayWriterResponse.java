package eu.freme.broker.tools.postprocessing;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 02.12.2015.
 */
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CharArrayWriterResponse extends ServletResponseWrapper {

    private final CharArrayWriter charArray = new CharArrayWriter();

    public CharArrayWriterResponse(ServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(charArray);
    }

    public String getOutput() {
        return charArray.toString();
    }
}