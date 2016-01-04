/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * This source code is derived from the Spring MVC Logging Filter
 * (https://github.com/isrsal/spring-mvc-logger) by Israel Zalmanov.
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

package eu.freme.broker.tools.loggingfilter;


import eu.freme.common.conversion.rdf.RDFSerializationFormats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    protected static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String REQUEST_PREFIX = "Request: ";
    private static final String RESPONSE_PREFIX = "Response: ";
    private AtomicLong id = new AtomicLong(1);

    @Value("${loggingfilter.maxsize:1000}")
    int maxSize;

    @Value("${loggingfilter.whitelist:null}")
    String[] whitelist;

    @Autowired
    RDFSerializationFormats rdfSerializationFormats;

    public LoggingFilter() {
    }

    @PostConstruct
    public void setup(){
        if (whitelist.length==1 && whitelist[0].equals("null")) {
            Object[] formatnames=rdfSerializationFormats.keySet().toArray();
            whitelist= new String[formatnames.length];
            for (int i=0;i<formatnames.length;i++) {
                whitelist[i]=(String)formatnames[i];
            }
        }
    }



    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {


        if(logger.isDebugEnabled()){
            long requestId = id.incrementAndGet();
            request = new RequestWrapper(requestId, request);
            response = new ResponseWrapper(requestId, response);
        }
        try {
            filterChain.doFilter(request, response);
//            response.flushBuffer();
        }
        finally {
            if(logger.isDebugEnabled()){
                logRequest(request);
                logResponse((ResponseWrapper)response);
            }
        }

    }

    /*
     * Truncates requests that exceed the length of loggingfilter.maxsize in application.properties after checking against whitelist.
     */
    private void logRequest(final HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();
        msg.append(REQUEST_PREFIX);
        if(request instanceof RequestWrapper){
            msg.append("request id=").append(((RequestWrapper)request).getId()).append("; ");
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            msg.append("session id=").append(session.getId()).append("; ");
        }
        if(request.getContentType() != null) {
            msg.append("content type=").append(request.getContentType()).append("; ");
        }
        msg.append("uri=").append(request.getRequestURI());
        if(request.getQueryString() != null) {
            msg.append('?').append(request.getQueryString());
        }

        if(request instanceof RequestWrapper && !isMultipart(request)){
            RequestWrapper requestWrapper = (RequestWrapper) request;
            try {
                String charEncoding = requestWrapper.getCharacterEncoding() != null ? requestWrapper.getCharacterEncoding() :
                        "UTF-8";
                if (request.getContentLength()==0 || checkAgainstWhitelist(request)) {
                    String body = new String(requestWrapper.toByteArray(), charEncoding);
                    if (request.getContentLength() >= maxSize) {
                        try {
                            body = body.substring(0, maxSize).concat("... (truncated by LoggingFilter)");
                        } catch (StringIndexOutOfBoundsException e){
                            logger.warn("A Request was made whose Content-Length Header is longer than its actual content");
                        }
                    }
                    msg.append("; payload=").append(body);
                } else {
                    msg.append("; payload ommitted from logging as it is not in the whitelisted mime-types");
                }

            } catch (UnsupportedEncodingException e) {
                logger.warn("Failed to parse request payload", e);
            }

        }
        logger.debug(msg.toString());
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType()!=null && request.getContentType().startsWith("multipart/form-data");
    }
    /*
     * Truncates responses that exceed the length of loggingfilter.maxsize in application.properties after checking against whitelist.
     */
    private void logResponse(final ResponseWrapper response) {
        StringBuilder msg = new StringBuilder();
        msg.append(RESPONSE_PREFIX);
        msg.append("request id=").append((response.getId()));
        try {
            if (checkAgainstWhitelist(response)){
                String body=new String(response.toByteArray(), response.getCharacterEncoding());

                if (response.toByteArray().length>=maxSize){
                    try {
                        body = body.substring(0, maxSize).concat("... (truncated by LoggingFilter)");
                    } catch (StringIndexOutOfBoundsException e) {
                        logger.warn("A Request was made whose Content-Length Header is longer than its actual content");
                    }
                }
                msg.append("; payload=").append(body);
            }
            else {
                msg.append("; payload ommitted from logging as it is not in the whitelisted mime-types");
            }
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to parse response payload", e);
        }
        logger.debug(msg.toString());
    }
    /**
     * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 11.12.15.
     */

    /*
     * Checks if the Content-Type of the given request/response is in the whitelisted
     * Mime-Types for logging. These are specified in the loggingfilter.whitelist parameter
     * in application.properties
     */
    public boolean checkAgainstWhitelist(HttpServletRequest request){
        String compare=request.getParameter("informat");
        if (compare==null){
            compare=request.getContentType();
        }
        return checkAgainstWhitelist(compare);
    }
    public boolean checkAgainstWhitelist(HttpServletResponse response){
        String compare=response.getContentType();
        return checkAgainstWhitelist(compare);
    }

    public boolean checkAgainstWhitelist(String compare) {
        if (compare!=null) {
            for (String s : whitelist) {
                if (compare.startsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

}
