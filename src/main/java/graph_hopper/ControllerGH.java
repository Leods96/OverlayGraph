package graph_hopper;

import com.graphhopper.PathWrapper;
import location_iq.*;
import location_iq.exceptions.CellTypeException;
import location_iq.exceptions.CheckPointException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ControllerGH {

    private static final String BASE_PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private static final String FILE = BASE_PATH + "geocodedAddresses.xlsx";
    private static final String DUMP_FOLDER = BASE_PATH + "GHDumpFolder\\Depot-Customer\\";
    private static final String CONFIGURATION_FOLDER = BASE_PATH + "configurationData\\";

    private ExcelReader fromReader;
    private ExcelReader toReader;
    private int sheetFromNum;
    private int sheetToNum;
    private ExternalCSVDump dumpManager;
    private ExternalConfigurationManager configurationManager;
    private ResponseManager rm;
    private GraphHopperInstance gh;

    /**
     * Open two file: fromFile and toFile containing the addresses from which compute the distances, and write
     * the results into the dump folder
     * configurationFile useful for the checkPoint, if null skip the configuration
     */
    public ControllerGH(String fromFile, int sheetFromNum, String toFile, int sheetToNum, String dumpFolder, String configurationFile){
        rm = new ResponseManager();
        try {
            this.fromReader = new ExcelReader(fromFile);
            this.toReader = new ExcelReader(toFile);
            this.dumpManager = new ExternalCSVDump(dumpFolder, true);
            if(configurationFile != null)
                this.configurationManager = new ExternalConfigurationManager(configurationFile);
            else
                this.configurationManager = null;
            this.sheetFromNum = sheetFromNum;
            this.sheetToNum = sheetToNum;
        } catch(IOException e) {
            //TODO system exit
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private Point setDumpManagerFile() {
        try {
            dumpManager.setFile(fromReader.getID());
            System.out.println("Starting working of " + fromReader.getID());
            return new Point(fromReader.getID(), fromReader.getLatitude(), fromReader.getLongitude());
        } catch (CellTypeException e) {
            System.out.println("Problem reading a Cell in row: " + fromReader.getRowNumber() + " for file creation...continue to next row..");
            return null;
        }
    }

    private void process() throws IOException {
        CheckPoint cp = null;
        long time;
        Point from;
        while (fromReader.nextRow()) {
            from = setDumpManagerFile();
            if(from == null)
                continue;
            time = System.nanoTime();
            while (toReader.nextRow()) {
                if (toReader.getNumSheet() != fromReader.getNumSheet() || toReader.getRowNumber() != fromReader.getRowNumber()) {
                    cp = standardRequest();
                }
            }
            dumpManager.saveSourceInfo(from.getCode(), from.getLatitude().toString(), from.getLongitude().toString());
            dumpManager.writeDump();
            try {
                configurationManager.createCheckPoint(cp.getFrom(), cp.getTo());
            } catch (Exception e) {
                System.err.println("Check Point file not created for exception");
                e.printStackTrace();
            }
            toReader.initializeIterator();
            System.out.println("This nodes is completed in: " + (System.nanoTime()-time)/1000000000 + " seconds");
        }
    }

    /**
     * perform the request to graphHopper with the read data from fromReader and toReader, and save the data into
     * the dumpManager
     * @return a checkPoint, useful if we want to split the process
     */
    private CheckPoint standardRequest() {
        String fromID = null, toID = null;
        try {
            fromID = fromReader.getID();
            toID = toReader.getID();
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

    /**
     * Initialization of the reader, if the conf file is not present of was not passed will start from the
     * begin of the files
     * @throws CheckPointException
     */
    private void setup() throws CheckPointException {
        gh = new GraphHopperInstance();
        gh.preprocessing();
        try {
            CheckPoint cp = configurationManager.getCheckPoint();
            fromReader.setSheetWithIndex(this.sheetFromNum).initializeIterator(cp.getFrom());
            toReader.setSheetWithIndex(this.sheetToNum).initializeIterator(cp.getTo());
        } catch (FileNotFoundException | NullPointerException e) {
            System.out.println("There isn't a CheckPoint file -> start from beginning");
            fromReader.setSheetWithIndex(this.sheetFromNum).initializeIterator();
            toReader.setSheetWithIndex(this.sheetToNum).initializeIterator();
        }
    }

    /**
     * start of the process
     */
    public void computeDump() {
        try {
            setup();
            long time = System.nanoTime();
            process();
            System.out.println("Elapsed time in second: " + (System.nanoTime()-time)/1000000000);
        } catch (CheckPointException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Problem writing the dump");
            e.printStackTrace();
        }
    }

}
