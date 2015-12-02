package eu.freme.broker.tools.postprocessing;

import eu.freme.broker.tools.ExceptionHandlerService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {





        if (!(req instanceof HttpServletRequest) || !(res instanceof HttpServletResponse) || req.getParameter("filter")==null) {
            chain.doFilter(req, res);
            return;
        }else{
            CharArrayWriterResponse customResponse = new CharArrayWriterResponse(res);
            chain.doFilter(req, res);
            HttpServletRequest httpRequest = (HttpServletRequest) req;
            HttpServletResponse httpResponse = (HttpServletResponse) res;

            String filterName = httpRequest.getParameter("filter");

            logger.info("using postprocessing filter: "+filterName);
            //httpResponse.


            System.err.println("customResponse: "+customResponse.getOutput());
        }

    }

    @Override
    public void destroy() {

    }
}
