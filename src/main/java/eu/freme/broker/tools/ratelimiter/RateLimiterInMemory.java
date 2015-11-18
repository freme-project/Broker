package eu.freme.broker.tools.ratelimiter;

import eu.freme.broker.exception.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */


public class RateLimiterInMemory implements RateCounterInterface {

    @Value("${ratelimiter.time_frame}")
    private String time_frame_str;
    private int time_frame;

    @Value("${ratelimiter.max_requests}")
    private String max_requests_str;
    private int max_requests;

    private int i,k;

    private ConcurrentHashMap<String,RateCounterObject> storedRequests = new ConcurrentHashMap<>();
    RateCounterObject rateCounterObject;

    public RateLimiterInMemory(){
    }

    @Override
    public void setup() {
        this.time_frame=Integer.parseInt(time_frame_str);
        this.max_requests=Integer.parseInt(max_requests_str);
    }

    @Override
    public void addToStoredRequests(String identifier, long timestamp) throws ForbiddenException {

        System.err.println(max_requests);
        try {
            rateCounterObject = storedRequests.get(identifier);
            //Array is full, now we have to compare timestamps
            if (rateCounterObject.index == time_frame) {
                if (timestamp - rateCounterObject.arrayOfTimestamps[0] < max_requests) {
                    throw new ForbiddenException("Too many calls - made "+ time_frame +" Requests in "+ (timestamp - rateCounterObject.arrayOfTimestamps[0])+" miliseconds");
                } else {

                    //Find first index whose time is still worth looking at
                    i = 0;
                    while (i< time_frame && max_requests < timestamp - rateCounterObject.arrayOfTimestamps[i]) {
                        i++;
                    }
                    //Move all elements of array left by that index, overwriting all obsolete indices
                    for (k = 0; k < time_frame - i; k++) {
                        rateCounterObject.arrayOfTimestamps[k] = rateCounterObject.arrayOfTimestamps[k + i];
                    }

                    //Set the "last index" of the object correctly again
                    rateCounterObject.index -= k + 1;
                }
            }
            //Array not full, the user can still make requests
            else {
                rateCounterObject.arrayOfTimestamps[rateCounterObject.index++] = timestamp;
            }
        } catch (NullPointerException e) {
            storedRequests.put(identifier, new RateCounterObject(timestamp, max_requests));
        }

    }
}
