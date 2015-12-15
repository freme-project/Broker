package eu.freme.broker.tools.postprocessing;

import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import eu.freme.broker.exception.FREMEHttpException;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.conversion.rdf.RDFSerializationFormats;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 02.12.2015.
 */
@Component
@Profile("broker")
public class PostprocessingFilter implements Filter {

    private Logger logger = Logger.getLogger(PostprocessingFilter.class);

    @Autowired
    ExceptionHandlerService exceptionHandlerService;

    @Autowired
    RDFSerializationFormats rdfSerializationFormats;

    @Autowired
    RDFConversionService rdfConversionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {



        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse) || req.getParameter("filter")==null) {
            chain.doFilter(req, res);
        }else{
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            HttpServletResponse httpResponse = (HttpServletResponse) res;

            String outformat = httpRequest.getParameter("outformat");
            if(Strings.isNullOrEmpty(outformat))
                outformat = httpRequest.getParameter("o");
            if(Strings.isNullOrEmpty(outformat)) {
                outformat = httpRequest.getHeader("Accept");
                if(Strings.isNullOrEmpty(outformat))
                    outformat  = httpRequest.getHeader("accept");
                if (!Strings.isNullOrEmpty(outformat))
                    outformat = outformat.split(";")[0].toLowerCase();
            }
            RDFConstants.RDFSerialization outType = rdfSerializationFormats.get(outformat);
            if(outType != null && outType != RDFConstants.RDFSerialization.JSON && outType != RDFConstants.RDFSerialization.PLAINTEXT){
                throw new FREMEHttpException("Can not use filter: "+req.getParameter("filter")+" with outformat: " + outType.contentType());
            }

            // set outformat for original request to turtle
            Map<String, String[]> extraParams = new TreeMap<String, String[]>();
            extraParams.put("outformat", new String[]{RDFConstants.RDFSerialization.TURTLE.contentType()});
            HttpServletRequest wrappedRequest = new ModifiableParametersWrappedRequest(httpRequest, extraParams);

            // wrap the response to allow later modification
            AccessibleHttpServletResponseWrapper wrappedResponse = new AccessibleHttpServletResponseWrapper(httpResponse);

            chain.doFilter(wrappedRequest, wrappedResponse);


            String responseContent = new String(wrappedResponse.getDataStream());
            String responseContentType;

            //// manipulate responseContent here
            try {
                String baseUrl = String.format("%s://%s:%d", httpRequest.getScheme(), httpRequest.getServerName(), httpRequest.getServerPort());

                HttpResponse<String> response = Unirest
                        .post(baseUrl + "/filter")
                        .header("Accept", RDFConstants.RDFSerialization.TURTLE.getMimeType())
                        .header("Content-Type", outType.getMimeType())
                        .queryString("filter-name", req.getParameter("filter"))
                        .body(responseContent)
                        .asString();

                if (response.getStatus() != HttpStatus.OK.value()) {
                    throw new FREMEHttpException(
                            "Postprocessing filter failed with status code: "
                                    + response.getStatus() + " (" + response.getStatusText() + ")",
                            HttpStatus.valueOf(response.getStatus()));
                }

                responseContent = response.getBody();
                responseContentType = response.getHeaders().getFirst("Content-Type");

            } catch (UnirestException e) {
                throw new FREMEHttpException(e.getMessage());
            }
            ////

            byte[] responseToSend = responseContent.getBytes();

            httpResponse.setContentType(responseContentType);
            httpResponse.setContentLength(responseToSend.length);

            ServletOutputStream outputStream = res.getOutputStream();
            outputStream.write(responseToSend);
            outputStream.flush();
            outputStream.close();


        }
    }

    @Override
    public void destroy() {

    }
}
