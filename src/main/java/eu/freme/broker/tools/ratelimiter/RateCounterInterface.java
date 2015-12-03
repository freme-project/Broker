package eu.freme.broker.tools.ratelimiter;

import eu.freme.broker.exception.ForbiddenException;
import org.springframework.security.core.Authentication;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */
public interface RateCounterInterface {

    void addToStoredRequests(String username, long timestamp, long size, String endpointURI, String userRole) throws ForbiddenException;

}
