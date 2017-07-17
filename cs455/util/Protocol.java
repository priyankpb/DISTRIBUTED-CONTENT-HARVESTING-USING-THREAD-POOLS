package cs455.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author YANK
 */
public class Protocol {

    public static int RECURSION_LEVEL = 5;
    public static String MAIL_SCHEME = "mail";
    
    public static final byte TASK_HANDOFF = 2;
    public static final byte ACKNOWLEDGEMENT = 3;
    public static final byte TASK_COMPLETE = 4;
    public static final byte TASK_INCOMPLETE = 5;
    
    public static final String FILE_STRUCTURE_IN = "in";
    public static final String FILE_STRUCTURE_OUT = "out";
    
    public static class URLS {

        public static final List<String> urls = new ArrayList<>();
        public static final Set<String> domainNames = new HashSet<>();
        public static String BMB = "http://www.bmb.colostate.edu/";
        public static String BIOLOGY = "http://www.biology.colostate.edu/";
        public static String CHEM = "http://www.chem.colostate.edu/";
        public static String CS = "http://www.cs.colostate.edu/cstop/index.html";
        public static String MATH = "http://www.math.colostate.edu/";
        public static String PHYSICS = "http://www.physics.colostate.edu/";
        public static String PSYCHOLOGY = "http://www.colostate.edu/Depts/Psychology/";
        public static String STAT = "http://www.stat.colostate.edu/";

        public static void init() {
//            System.out.println("-Static block called-");
//            if (BMB.endsWith("/")) {
//                BMB = BMB.substring(0, BMB.length() - 1);
//            }
//            if (BIOLOGY.endsWith("/")) {
//                BIOLOGY = BIOLOGY.substring(0, BIOLOGY.length() - 1);
//            }
//            if (CHEM.endsWith("/")) {
//                CHEM = CHEM.substring(0, CHEM.length() - 1);
//            }
//            if (CS.endsWith("/")) {
//                CS = CS.substring(0, CS.length() - 1);
//            }
//            if (MATH.endsWith("/")) {
//                MATH = MATH.substring(0, MATH.length() - 1);
//            }
//            if (PHYSICS.endsWith("/")) {
//                PHYSICS = PHYSICS.substring(0, PHYSICS.length() - 1);
//            }
//            if (PSYCHOLOGY.endsWith("/")) {
//                PSYCHOLOGY = PSYCHOLOGY.substring(0, PSYCHOLOGY.length() - 1);
//            }
//            if (STAT.endsWith("/")) {
//                STAT = STAT.substring(0, STAT.length() - 1);
//            }
            Protocol.URLS.urls.add(BMB);
            Protocol.URLS.urls.add(BIOLOGY);
            Protocol.URLS.urls.add(CHEM);
            Protocol.URLS.urls.add(CS);
            Protocol.URLS.urls.add(MATH);
            Protocol.URLS.urls.add(PHYSICS);
            Protocol.URLS.urls.add(PSYCHOLOGY);
            Protocol.URLS.urls.add(STAT);

            try {
                for (String url : urls) {
                    domainNames.add(new URL(url).getHost());
                }
            } catch (MalformedURLException ex) {
                System.err.println("Malformed URL found while setting domain names");
            }
//            System.out.println("-Domain names-");
//            for (String domainName : domainNames) {
//                System.out.println(domainName);
//            }
        }
    }
}
