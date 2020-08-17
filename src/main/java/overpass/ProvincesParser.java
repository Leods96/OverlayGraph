package overpass;

import location_iq.exceptions.BadResponseException;
import org.json.JSONObject;
import util.JSonExcelConverter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class ProvincesParser {

    private static final String NODES_FOLDER = "Nodes\\";
    private static final String CHECKPOINT_FILE_NAME = "checkPoint.txt";
    private static final String INFO_FILE_NAME = "ItalyInfo(NoSardegna).json";
    private static final String EXCEPTION_FILE_NAME = "Names.txt";

    private static final String QUERY_INIT = "[output:json];" +
            "area[name=\"";
    private static final String QUERY_END = "\"]->.a;" +
            "relation(area.a)[type=\"boundary\"][\"admin_level\"~\"4|6|7\"][\"boundary\"=\"administrative\"]->.boundary;" +
            "way(r.boundary)->.boundary;" +
            "way(area.a)[highway][highway~\"trunk|primary|secondary|trunk_link|primary_link|secondary_link\"]->.highway;" +
            "node(w.highway)(around.boundary:100);" +
            "out meta;";
    private String folderPath;
    private String query;


    public ProvincesParser(String folderPath) {
        this.folderPath = folderPath + "\\";
    }

    public void process(boolean exception) throws IOException, BadResponseException{
        String province = readCheckPoint();
        Iterator<String> iterator;
        if(exception)
            iterator = readExceptionNames();
        else
            iterator = readCountryInfo();
        try {
            if (province != null) {
                while (iterator.next().compareTo(province) != 0) ;
                createQuery(province);
                writeToFile(performQuery(), province);
            }
            while (iterator.hasNext()) {
                province = iterator.next();
                createQuery(province);
                writeToFile(performQuery(), province);
            }
        } catch (BadResponseException e) {
            writeCheckPoint(province);
            System.err.println("BadResponseException, checkPoint created");
            throw e;
        }
    }

    private String readCheckPoint() {
        try (Scanner scanner = new Scanner(new File(folderPath + CHECKPOINT_FILE_NAME)))
        {
            return scanner.hasNextLine() ? scanner.nextLine() : null;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void writeCheckPoint(String province) throws IOException {
        try (
                FileWriter fw = new FileWriter(new File(folderPath + CHECKPOINT_FILE_NAME));
                PrintWriter printWriter = new PrintWriter(fw)
        ) {
            printWriter.println(province);
        }
    }

    private Iterator<String> readCountryInfo() throws IOException {
        try(Scanner scanner = new Scanner(new File(folderPath + INFO_FILE_NAME)))
        {
            StringBuilder sb = new StringBuilder();
            while(scanner.hasNextLine())
                sb.append(scanner.nextLine());
            JSONObject json = new JSONObject(sb.toString());
            String[] data = json.get("sub_names").toString().split("~");
            return Arrays.stream(data).iterator();
        }
    }

    private Iterator<String> readExceptionNames() throws IOException {
        try(Scanner scanner = new Scanner(new File(folderPath + EXCEPTION_FILE_NAME)))
        {
            ArrayList<String> array = new ArrayList<>();
            while(scanner.hasNextLine())
                array.add(scanner.nextLine());
            return array.stream().iterator();
        }
    }

    private void createQuery(String province) {
        this.query = QUERY_INIT + province + QUERY_END;
    }

    private JSONObject performQuery() throws IOException, BadResponseException {
        return new OverpassAPI().request(query, true);
    }

    private void writeToFile(JSONObject json, String province) throws IOException{
        JSonExcelConverter.convert(json.getJSONArray("elements"), folderPath + NODES_FOLDER + province.replaceAll("/","-") + ".xlsx");
    }


}
