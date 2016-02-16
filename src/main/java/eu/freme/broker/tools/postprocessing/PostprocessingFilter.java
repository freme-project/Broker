package eu.freme.broker.tools.postprocessing;

import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import eu.freme.broker.exception.BadRequestException;
import eu.freme.broker.exception.FREMEHttpException;
import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.conversion.rdf.RDFSerializationFormats;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
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
@Order(10)
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
            JSONObject errorResponse = new JSONObject();
            String responseContent = null;
            String responseContentType = RDFConstants.RDFSerialization.JSON.contentType();
            String filterUrl = "/toolbox/filter/documents/" + req.getParameter("filter");
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            HttpServletResponse httpResponse = (HttpServletResponse) res;

            try {

                // get requested format of response
                String outTypeString = httpRequest.getParameter("outformat");
                if(Strings.isNullOrEmpty(outTypeString))
                    outTypeString = httpRequest.getParameter("o");
                if(Strings.isNullOrEmpty(outTypeString) && !Strings.isNullOrEmpty(httpRequest.getHeader("Accept")) && !httpRequest.getHeader("Accept").equals("*/*"))
                    outTypeString = httpRequest.getHeader("Accept").split(";")[0];

                RDFConstants.RDFSerialization outType = RDFConstants.RDFSerialization.CSV;
                if (!Strings.isNullOrEmpty(outTypeString)) {
                    outType = rdfSerializationFormats.get(outTypeString);
                    if(outType == null)
                        throw new BadRequestException("Can not use filter: " + req.getParameter("filter") + " with outformat: "+httpRequest.getParameter("outformat")+" or Accept-header: " + httpRequest.getHeader("Accept"));

                }

                // set Accept header for original request to turtle
                Map<String, String[]> extraParams = new TreeMap<>();
                // delete outformat parameter
                extraParams.put("outformat", new String[]{"turtle"});//new String[]{"turtle"});
                Map<String, String[]> extraHeaders = new TreeMap<>();
                extraHeaders.put("Accept", new String[]{RDFConstants.RDFSerialization.TURTLE.contentType()});
                HttpServletRequest wrappedRequest = new ModifiableParametersWrappedRequest(httpRequest, extraParams,extraHeaders);

                // wrap the response to allow later modification
                AccessibleHttpServletResponseWrapper wrappedResponse = new AccessibleHttpServletResponseWrapper(httpResponse);


                chain.doFilter(wrappedRequest, wrappedResponse);
                String originalResponseContent = new String(wrappedResponse.getDataStream());

                // postprocess only, if original request was successful
                if(wrappedResponse.getStatus()!=HttpStatus.OK.value()) {
                    responseContent = originalResponseContent;
                }else{

                    //// manipulate responseContent here
                    String baseUrl = String.format("%s://%s:%d", httpRequest.getScheme(), httpRequest.getServerName(), httpRequest.getServerPort());

                    HttpResponse<String> response = null;


                    response = Unirest
                            .post(baseUrl + filterUrl)
                            .header("Content-Type", RDFConstants.RDFSerialization.TURTLE.contentType())
                            .header("Accept", outType.contentType())
                            .body(originalResponseContent)
                            .asString();

                    if (response.getStatus() != HttpStatus.OK.value()) {

                        errorResponse = new JSONObject(response.getBody());
                        String errorMessage = errorResponse.getString("message");

                        errorResponse.put("message", "Postprocessing filter failed with status code: "
                                + response.getStatus() + " (" + response.getStatusText() + "). " + errorMessage);

                        responseContent = errorResponse.toString();

                    } else {
                        responseContent = response.getBody();
                        responseContentType = response.getHeaders().getFirst("Content-Type");
                    }
                }
            } catch (JSONException e) {
                errorResponse.put("exception", e.getClass());
                errorResponse.put("path", filterUrl);
                errorResponse.put("message", "Postprocessing with filter: " + req.getParameter("filter") + " failed. Can not parse body of json error response.");
                errorResponse.put("error", "Internal Server Error");
                errorResponse.put("status", 500);
                errorResponse.put("timestamp", System.currentTimeMillis());
                responseContent = errorResponse.toString();
            } catch (UnirestException e) {
                errorResponse.put("exception", e.getClass());
                errorResponse.put("path", filterUrl);
                errorResponse.put("message", "Can not call filter endpoint");
                errorResponse.put("error", "Internal Server Error");
                errorResponse.put("status", 500);
                errorResponse.put("timestamp", System.currentTimeMillis());
                responseContent = errorResponse.toString();
            } catch (Exception e){
                errorResponse.put("exception", e.getClass());
                errorResponse.put("path", filterUrl);
                errorResponse.put("message", e.getMessage());
                errorResponse.put("error", "Internal Server Error");
                errorResponse.put("status", 500);
                errorResponse.put("timestamp", System.currentTimeMillis());
                responseContent = errorResponse.toString();
            }

            ////

            byte[] responseToSendBytes = responseContent.getBytes();

            httpResponse.setContentType(responseContentType);
            httpResponse.setContentLength(responseToSendBytes.length);

            ServletOutputStream outputStream = res.getOutputStream();
            outputStream.write(responseToSendBytes);
            outputStream.flush();
            outputStream.close();

        }
    }

    @Override
    public void destroy() {

    }
}
