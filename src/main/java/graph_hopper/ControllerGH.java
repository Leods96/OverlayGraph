package graph_hopper;

import com.graphhopper.PathWrapper;
import location_iq.*;
import location_iq.Exceptions.CellTypeException;
import location_iq.Exceptions.CheckPointException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ControllerGH {

    private static final String BASE_PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private static final String FILE = BASE_PATH + "geocodedAddresses.xlsx";
    private static final String DUMP_FOLDER = BASE_PATH + "GHDumpFolder\\Depot-Customer\\";
    private static final String CONFIGURATION_FOLDER = BASE_PATH + "configurationData\\";

    private ExcellReader fromReader;
    private ExcellReader toReader;
    private ExternalCSVDump dumpManager;
    private ExternalConfigurationManager configurationManager;
    private ResponseManager rm;
    private GH_Test gh;

    public ControllerGH(){
        rm = new ResponseManager();
        try {
            fromReader = new ExcellReader(FILE);
            toReader = new ExcellReader(FILE);
            dumpManager = new ExternalCSVDump(DUMP_FOLDER, true);
            configurationManager = new ExternalConfigurationManager(CONFIGURATION_FOLDER);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private boolean setDumpManagerFile() {
        try {
            dumpManager.setFile(fromReader.getID());
            System.out.println("Starting working of depot " + fromReader.getID());
            return true;
        } catch (CellTypeException e) {
            System.out.println("Problem reading a Cell in row: " + fromReader.getRowNumber() + " for file creation...continue to next row..");
            return false;
        }
    }

    public void process() throws IOException{
        CheckPoint cp;
        //TODO remove this, for test
        boolean remove = true;
        long time;
        //TODO remove this, for test
        while (fromReader.nextRow() && remove) {
            if(!setDumpManagerFile())
                continue;
            time = System.nanoTime();
            while (toReader.nextRow()) {
                if (toReader.getNumSheet() != fromReader.getNumSheet() || toReader.getRowNumber() != fromReader.getRowNumber()) {
                    cp = standardRequest();
                }
                //TODO remove this, for test
                dumpManager.writeDump();
            }
            dumpManager.writeDump();
            toReader.initializeIterator();
            System.out.println("This Depot is completed in: " + (System.nanoTime()-time)/1000000000 + " seconds");
            //TODO remove this, for test
            remove = false;
        }
    }

    public CheckPoint standardRequest() {
        String fromID = null, toID = null;
        try {
            fromID = fromReader.getID();
            toID = toReader.getID();
            System.out.println("From: " + fromID + " to: " + toID);
            PathWrapper response = gh.routing(new double[]{fromReader.getLatitude(), fromReader.getLongitude()},
                    new double[]{toReader.getLatitude(), toReader.getLongitude()});
            dumpManager.saveDataForGH(rm.filterResponse(response).toMap(), toID);
        } catch (RuntimeException e) {
            dumpManager.saveData(new JSONObject().put("message",e.getMessage()).toMap(),toID);
        } catch (CellTypeException e) {
            System.err.println("Cell type exception reding rowFrom: " + fromReader.getRowNumber() + ", rowTo: " + toReader.getRowNumber());
            return null;
        }
        return new CheckPoint(fromID, toID);
    }

    public void setup() throws CheckPointException {
        gh = new GH_Test();
        gh.preprocessing();
        try {
            CheckPoint cp = configurationManager.getCheckPoint();
            fromReader.setSheetWithIndex(1).initializeIterator(cp.getFrom());
            toReader.setSheetWithIndex(0).initializeIterator(cp.getTo());
        } catch (FileNotFoundException e) {
            System.out.println("There isn't a CheckPoint file -> start from beginning");
            fromReader.setSheetWithIndex(1).initializeIterator();
            toReader.setSheetWithIndex(0).initializeIterator();
        }
    }

    public static void main(String[] args) {
        ControllerGH c = new ControllerGH();
        try {
            c.setup();
            long time = System.nanoTime();
            c.process();
            System.out.println("Elapsed time in second: " + (System.nanoTime()-time)/1000000000);
        } catch (CheckPointException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Problem writing the dump");
            e.printStackTrace();
        }
    }


}
