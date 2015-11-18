package eu.freme.broker.tools.ratelimiter;

import eu.freme.broker.tools.ExceptionHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */
public class RateLimiterInMemory implements RateCounter {

    private static int max_n;
    private static int max_t;
    private static Date date= new java.util.Date();
    private int i,k;

    private ConcurrentHashMap<String,RateCounterObject> storedRequests;
    RateCounterObject rateCounterObject;

    @Override
    public void addToStoredRequests(String identifier, long timestamp) throws Exception {
        rateCounterObject=storedRequests.get(identifier);

        if (rateCounterObject==null) {
            storedRequests.put(identifier, new RateCounterObject(timestamp));
        }

        else {
            if (rateCounterObject.index==max_n) {
                if (timestamp-rateCounterObject.arr[0]<max_t){
                    throw new Exception("Too many Calls");
                }
                else {

                    //Find first index whose time is still worth looking at
                    i=0;
                    while (max_t<timestamp-rateCounterObject.arr[i]) {
                        i++;
                    }
                    //Move all elements of array left by that index, overwriting all obsolete indices
                    for (k=0;k<max_n-i;k++) {
                        rateCounterObject.arr[k]=rateCounterObject.arr[k+i];
                    }

                    //Set the "last index" of the object correctly again
                    rateCounterObject.index-=k+1;
                }
            }
            else {
                rateCounterObject.arr[rateCounterObject.index++]=timestamp;
            }

        }

    }
}
