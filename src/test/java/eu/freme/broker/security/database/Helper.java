package eu.freme.broker.security.database;

import java.util.Iterator;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
public class Helper {

    public static <T> int count(Iterable<T> itr){
        Iterator<T> itr2 = itr.iterator();
        int counter=0;
        while(itr2.hasNext()){
            counter++;
            itr2.next();
        }
        return counter;
    }
}
