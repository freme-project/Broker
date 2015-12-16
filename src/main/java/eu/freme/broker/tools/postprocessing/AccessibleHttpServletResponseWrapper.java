package eu.freme.broker.tools.postprocessing;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 08.12.2015.
 */
public class AccessibleHttpServletResponseWrapper extends HttpServletResponseWrapper {

    ByteArrayOutputStream output;
    AccessibleServletOutputStream filterOutput;
    //HttpResponseStatus status = HttpResponseStatus.OK;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @param response The response to be wrapped
     * @throws IllegalArgumentException if the response is null
     */
    public AccessibleHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (filterOutput == null) {
            filterOutput = new AccessibleServletOutputStream(output);
        }
        return filterOutput;
    }

    public byte[] getDataStream() {
        return output.toByteArray();
    }




}
