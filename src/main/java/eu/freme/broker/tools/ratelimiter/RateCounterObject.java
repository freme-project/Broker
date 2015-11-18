package eu.freme.broker.tools.ratelimiter;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 18.11.15.
 */
public class RateCounterObject {
    public int max_n=3;

    public Long[] arr= new Long[max_n];
    public int index=1;

    public RateCounterObject(long timestamp){
        this.arr[0]=timestamp;
    }
}
