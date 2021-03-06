package location_iq;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExternalCSVDump extends ExternalDumpManager {
    private static final String EXTENSION = ".csv";
    //private static final String[] HEADERS_GH = {"Code","Time","Distance","Route"};
    //private static final String[] HEADERS_LIQ = {"Code","Summary","Route"};
    private static final String[] HEADERS_GH = {"Code","Time","Distance"};
    private static final String[] HEADERS_LIQ = {"Code","Summary"};

    private String[] HEADERS;
    private boolean usingGH;
    private ArrayList<String[]> csvFileObject = null;
    private Iterator<CSVRecord> records = null;
    private boolean sourceDataParsed = false;
    private boolean sourceDataWritten = false;
    private String[] sourceInfo;

    public ExternalCSVDump (String path, boolean gh){
        super(path);
        if(gh)
            HEADERS = HEADERS_GH;
        else
            HEADERS = HEADERS_LIQ;
        this.usingGH = gh;
        csvFileObject = new ArrayList<>();
    }

    public ExternalDumpManager writeDump() throws IOException {
        boolean append = new File(path + file + EXTENSION).exists();
        FileWriter out = new FileWriter(path + file + EXTENSION, append);
        CSVPrinter printer = null;
        try {
            if (!append)
                printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(HEADERS));
            else
                printer = new CSVPrinter(out, CSVFormat.DEFAULT);
            if(sourceDataWritten) //write the source data in the first line
                printer.printRecord(sourceInfo);
            for (String[] elem : csvFileObject)
                printer.printRecord(elem);
        } finally {
            printer.close();
            sourceDataWritten = false;
        }
        csvFileObject.clear();
        return this;
    }

    public ExternalDumpManager saveSourceInfo(String code, String latitude, String longitude) {
        sourceInfo = new String[3];
        sourceInfo[0] = code;
        sourceInfo[1] = latitude;
        sourceInfo[2] = longitude;
        sourceDataWritten = true;
        return this;
    }

    public ExternalDumpManager saveData(Map object, String code) {
        String[] temp = new String[3];
        temp[0] = code;
        if(object.containsKey("message")){
            temp[1] = "Failed request";
            temp[2] = object.get("message").toString();
        } else {
            temp[1] = object.get("summary").toString();
            temp[2] = object.get("routes").toString();
        }
        csvFileObject.add(temp);
        return this;
    }

    public ExternalDumpManager saveDataForGH(Map object, String code) {
        //TODO modified -> not using the route -> is to heavy
        String[] temp = new String[3];
        temp[0] = code;
        temp[1] = object.get("time").toString();
        temp[2] = object.get("distance").toString();
        //temp[3] = object.get("routes").toString();
        csvFileObject.add(temp);
        return this;
    }

    public ExternalDumpManager createParserFile(String file) throws IOException{
        Reader in = new FileReader(path + file);
        this.records = CSVFormat.DEFAULT
                .withHeader(HEADERS)
                .withFirstRecordAsHeader()
                .parse(in)
                .iterator();
        this.sourceDataParsed = false;
        return this;
    }

    public Map<String, Object> parseSourceInfo() {
        if(records.hasNext() && !sourceDataParsed){
            sourceDataParsed = true; //2 means that the info are read
            HashMap<String, Object> m = new HashMap<>();
            CSVRecord record = records.next();
            m.put("code", record.get(0));
            m.put("Latitude", record.get(1));
            m.put("Longitude", record.get(2));
            return m;
        }
        return null;
    }

    public Map parseNext() {
        if(records.hasNext()){
            CSVRecord record;
            if (!sourceDataParsed) {
                //if the first line containing the source info is not parsed this line will be skipped
                record = records.next();
                sourceDataParsed = true;
            }
            record = records.next();
            Map m = new HashMap();
            if ("Failed request".equalsIgnoreCase(record.get(1))) {
                m.put("code", "Failed request");
            } else {
                m.put("code", record.get(0));
                m.put("time", record.get(1));
                m.put("distance", record.get(2));
                //m.put("route", record.get(3));
            }
            return m;
        }
        return null;
    }

}
