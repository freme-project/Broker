package eu.freme.broker.tools.postprocessing;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import eu.freme.broker.exception.FREMEHttpException;
import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.conversion.rdf.RDFSerializationFormats;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

            AccessibleHttpServletResponseWrapper responseWrapper = new AccessibleHttpServletResponseWrapper(httpResponse);
            chain.doFilter(httpRequest, responseWrapper);

            ServletOutputStream outputStream = res.getOutputStream();

            // get content type of response
            String responseContentType = httpResponse.getContentType();
            if(responseContentType!=null){
                responseContentType = responseContentType.split(";")[0].toLowerCase();
                // check, if content is RDF
                RDFConstants.RDFSerialization responseType = rdfSerializationFormats.get(responseContentType);
                if(responseType != null && responseType != RDFConstants.RDFSerialization.JSON && responseType != RDFConstants.RDFSerialization.PLAINTEXT){
                    String responseContent = new String(responseWrapper.getDataStream());

                    //// manipulate responseContent here
                    try {
                        Model model = rdfConversionService.unserializeRDF(responseContent,responseType);
                        Model result;
                        if(req.getParameter("filter").toLowerCase().equals("extract-entities-only")) {
                            String queryString =
                                    "PREFIX itsrdf: <http://www.w3.org/2005/11/its/rdf#>\n" +
                                    "CONSTRUCT {?s itsrdf:taIdentRef ?o} WHERE {?s itsrdf:taIdentRef ?o}";
                            Query qry = QueryFactory.create(queryString);
                            QueryExecution qe = QueryExecutionFactory.create(qry, model);
                            /*ResultSet rs = qe.execSelect();
                            responseContent = "";
                            while(rs.hasNext()){
                                responseContent += rs.next().toString()+"\n";
                            }*/
                            result = qe.execConstruct();
                        }else{
                            throw new FREMEHttpException("unknown filter: "+req.getParameter("filter"));
                        }
                        responseContent = rdfConversionService.serializeRDF(result, responseType);
                    } catch (Exception e) {
                        throw new FREMEHttpException(e.getMessage());
                    }
                    ////

                    byte[] responseToSend = responseContent.getBytes();
                    httpResponse.setContentLength(responseToSend.length);
                    outputStream.write(responseToSend);
                    outputStream.flush();
                    outputStream.close();
                }else{
                    throw new FREMEHttpException("Can not use filter: "+req.getParameter("filter")+" with response type: "+responseContentType);
                }
            }

            outputStream.write(responseWrapper.getDataStream());
            outputStream.flush();
            outputStream.close();

        }
    }

    @Override
    public void destroy() {

    }
}
