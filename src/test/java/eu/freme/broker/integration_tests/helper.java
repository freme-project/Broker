package eu.freme.broker.integration_tests;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jonathan on 28.07.15.
 */
public class helper {

    public static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        StringBuilder bldr = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            bldr.append(line);
            bldr.append("\n");
        }
        reader.close();
        return bldr.toString();
    }




}
