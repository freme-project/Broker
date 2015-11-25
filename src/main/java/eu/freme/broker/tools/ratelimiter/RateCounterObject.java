package eu.freme.broker.tools.ratelimiter;

import eu.freme.broker.exception.InternalServerErrorException;
import eu.freme.broker.exception.TooManyRequestsException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 18.11.15.
 */
public class RateCounterObject {


    public int index;
    public long totalSize;

    public ConcurrentLinkedQueue<Long> timestamps;
    public ConcurrentLinkedQueue<Long> sizes;


    public long max_size;
    public int max_requests;
    public long time_frame;

    public RateCounterObject(long time_frame, long timestamp, int max_requests, long size, long max_size){
        this.time_frame = time_frame;
        this.sizes=new ConcurrentLinkedQueue<>();
        this.timestamps= new ConcurrentLinkedQueue<>();
        this.index=0;
        this.totalSize=0;
        this.max_size=max_size;
        this.max_requests=max_requests;

        this.add_entry(timestamp, size);

    }

    public void add_entry(long timestamp, long size) throws TooManyRequestsException {

        if (index >= max_requests-1) {
            if (max_requests!=0  && timestamp - timestamps.peek() < time_frame) {
                throw new TooManyRequestsException("You exceeded the allowed "+max_requests+" requests in "+time_frame/1000+ ". Please try again later.");
            }
            while (timestamps.peek() != null && timestamp - timestamps.peek() > time_frame) {
                timestamps.poll();
                totalSize -= sizes.poll();
                index--;

                //DEBUG
                if (totalSize < 0) {
                    throw new InternalServerErrorException("Something went wrong when calculating sizes of your requests");
                }
            }
        }
        if (index< max_requests -1) {

            timestamps.add(timestamp);
            sizes.add(size);
            totalSize += size;
            index++;

        }

        if (max_size!=0 && totalSize >= max_size*1024) {
            throw new TooManyRequestsException("Your requests exceeded the allowed "+max_size+" kb of data. Please wait until making more requests.");
        }


    }
}
