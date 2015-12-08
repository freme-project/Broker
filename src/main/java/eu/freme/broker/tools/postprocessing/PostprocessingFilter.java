package eu.freme.broker.tools.postprocessing;

import eu.freme.broker.tools.ExceptionHandlerService;
import eu.freme.broker.tools.RDFSerializationFormats;
import eu.freme.common.conversion.rdf.RDFConstants;
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {



        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse) || req.getParameter("filter")==null) {
            chain.doFilter(req, res);
            return;
        }else{
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            HttpServletResponse httpResponse = (HttpServletResponse) res;

            AccessibleHttpServletResponseWrapper responseWrapper = new AccessibleHttpServletResponseWrapper(httpResponse);
            chain.doFilter(httpRequest, responseWrapper);

            ServletOutputStream outputStream = res.getOutputStream();

            String responseContentType = httpResponse.getContentType();
            if(responseContentType!=null){
                responseContentType = responseContentType.split(";")[0].toLowerCase();
                // check, if content is RDF
                RDFConstants.RDFSerialization responseType = rdfSerializationFormats.get(responseContentType);
                if(responseType != null && responseType != RDFConstants.RDFSerialization.JSON && responseType != RDFConstants.RDFSerialization.PLAINTEXT){
                    String responseContent = new String(responseWrapper.getDataStream());

                    //// manipulate cesponseContent here
                    responseContent = "TEST";
                    ////

                    byte[] responseToSend = responseContent.getBytes();
                    httpResponse.setContentLength(responseToSend.length);
                    outputStream.write(responseToSend);
                    outputStream.flush();
                    outputStream.close();
                    return;
                }
            }


            outputStream.write(responseWrapper.getDataStream());
            outputStream.flush();
            outputStream.close();
            return;







        }

    }

    @Override
    public void destroy() {

    }
}
