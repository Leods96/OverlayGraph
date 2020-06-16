package analysis;

import location_iq.ExternalCSVDump;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to perform analytical operation over the dumps file and other objects
 */

public class RoutesAnalyzer {
    private static final String PRECOMPUTED_INFO_PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\GHDumpFolder\\";
    private static final String DEPOT_CUSTOMER = "Depot-Customer\\";
    private static final String DEPOT_DEPOT = "Depot-depot\\";

    private final HashMap<String, Integer> streets;
    private ExternalCSVDump dump;

    public RoutesAnalyzer(){
        streets = new HashMap<>();
    }

    /**
     * Execution of the parse of the given files
     * @throws IOException
     */
    public void parse() throws IOException{
        parseTwice(PRECOMPUTED_INFO_PATH + DEPOT_DEPOT);
        //parseTwice(PRECOMPUTED_INFO_PATH + DEPOT_CUSTOMER);
        streets.forEach((name, occurrencies) ->
                System.out.println("Name: " + name + " is present: " + occurrencies + " times"));
    }

    /**
     * Parse the give file
     * @param path path of the file to parse
     * @throws IOException exception raised by the file management
     */
    public void parseTwice(String path) throws IOException{
        dump = new ExternalCSVDump(path, true);
        File folder = new File(path);
        File[] files = folder.listFiles();

        for(File f : files)
            parseDump(f.getName());
    }

    /**
     * Create an hashmap with all the streets name and the occurrences of this specific street in all the paths
     * @param dumpName Name of the dump to parse
     * @throws IOException
     */
    public void parseDump(String dumpName) throws IOException {
        System.out.println("Processing " + dumpName);
        dump.createParserFile(dumpName);
        Map response = dump.parseNext();
        while (response != null) {
            if(!response.get("code").toString().equalsIgnoreCase("Failed request")) {
                String[] array = response.get("route").toString().split(",");
                for(String s:array) {
                    if(s.contains("name") && s.split("=").length > 1){
                        String name = (s.split("="))[1];
                        if(!name.equalsIgnoreCase("")) {
                            if(streets.containsKey(name))
                                streets.put(name,streets.get(name)+1);
                            else
                                streets.put(name,1);
                        }
                    }
                }
            }
            response = dump.parseNext();
        }
    }

    public static void main(String[] args) {
        RoutesAnalyzer r = new RoutesAnalyzer();
        try {
            r.parse();
        } catch (IOException e) {
            System.err.println("Problem with dumps");
            e.printStackTrace();
        }
    }
}
