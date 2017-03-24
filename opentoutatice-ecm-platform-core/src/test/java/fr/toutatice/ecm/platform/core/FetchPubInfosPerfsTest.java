/**
 * 
 */
package fr.toutatice.ecm.platform.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;


/**
 * @author david
 *
 */
public class FetchPubInfosPerfsTest {

    /**
     * 
     */
    public FetchPubInfosPerfsTest() {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String[] filesPath = {"/home/david/Public/stat_profiler.txt", "/home/david/Public/stat_profiler_drive.txt"};
        int maxLoop = 500;

        for (int index = 0; index < filesPath.length; index++) {

            int sum = 0;
            int loops = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(filesPath[index]))) {

                String sCurrentLine;

                while ((sCurrentLine = br.readLine()) != null && loops <= maxLoop) {
                    try {
                        sum += Integer.valueOf(StringUtils.trim(sCurrentLine));
                        loops++;
                    } catch (NumberFormatException nfe) {
                        // continue
                    }
                }

                int average = sum / loops;

                String param = index == 1 ? "checking Token" : StringUtils.EMPTY;

                System.out.println("[Average " + param + "]: " + average + "ms (" + loops + " hits)");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
