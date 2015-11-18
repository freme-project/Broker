package eu.freme.broker.tools.ratelimiter;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 18.11.15.
 */
public class RateCounterObject {

    public Long[] arrayOfTimestamps;
    public int index=1;

    public RateCounterObject(long timestamp, int max_requests){

        this.arrayOfTimestamps =new Long[max_requests];
        this.arrayOfTimestamps[0]=timestamp;
    }
}
