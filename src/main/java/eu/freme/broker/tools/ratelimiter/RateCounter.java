package eu.freme.broker.tools.ratelimiter;

import org.springframework.security.core.Authentication;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */
public interface RateCounter {

    public void addToStoredRequests(String identifier, long timestamp) throws Exception;

}
