package graph_hopper;

import com.graphhopper.PathWrapper;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import input_output.*;
import input_output.exceptions.CellTypeException;
import input_output.exceptions.CheckPointException;
import objects.Point;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.IOException;

import static controllers.Controller.ghPath;

public class ControllerGH {

    private ExcelReader fromReader;
    private ExcelReader toReader;
    private int sheetFromNum;
    private int sheetToNum;
    private ExternalCSVDump dumpManager;
    private ExternalConfigurationManager configurationManager;
    private final ResponseManager rm;
    private GraphHopperInstance gh;

    /**
     * Constructor for the ControllerGH Object
     * Creation of two ExcelReader: fromFile and toFile to read the addresses from which compute
     * the distances
     * Creation of an ExternalDumpManager object to write the results into the dump folder
     * Creation of ExternalConfigurationManager useful for the checkPoint, if null skip the configuration
     * @param fromFile file from which read the source
     * @param toFile  file from which read the destination (generally same as fromFile)
     * @param dumpFolder path where the dump of the computation is written
     * @param configurationFile path where to read the conf file
     */
    public ControllerGH(String fromFile, int sheetFromNum, String toFile, int sheetToNum, String dumpFolder, String configurationFile) throws IOException {
        rm = new ResponseManager();
        this.fromReader = new ExcelReader(fromFile);
        this.toReader = new ExcelReader(toFile);
        this.dumpManager = new ExternalCSVDump(dumpFolder);
        if(configurationFile != null)
            this.configurationManager = new ExternalConfigurationManager(configurationFile);
        else
            this.configurationManager = null;
        this.sheetFromNum = sheetFromNum;
        this.sheetToNum = sheetToNum;
    }

    /**
     * Change the dumpManagerFile configuration in order to work with a new dump each time
     * @return Point containing the information of the starting point for each route that will be
     * computed, if the Cell contains error the return is null
     */
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
     * perform the request to graphHopper with the read data from fromReader and toReader
     * and save the data into the dumpManager
     * @return CheckPoint, useful if we want to split the process
     */
    private CheckPoint standardRequest() {
        String fromID = null, toID = null;
        try {
            fromID = fromReader.getID();
            toID = toReader.getID();
            PathWrapper response = gh.routing
                    (
                        new double[]{fromReader.getLatitude(), fromReader.getLongitude()},
                        new double[]{toReader.getLatitude(), toReader.getLongitude()}
                    );
            dumpManager.saveDataForGH(rm.filterResponse(response), toID);
        } catch (RuntimeException e) {
            dumpManager.saveData(new JSONObject().put("message",e.getMessage()).toMap(),toID);
        } catch (CellTypeException e) {
            System.err.println("Cell type exception reading rowFrom: " + fromReader.getRowNumber() + ", rowTo: " + toReader.getRowNumber());
            return null;
        }
        return new CheckPoint(fromID, toID);
    }

    /**
     * Creation and initialization of the GraphHopperInstance
     * Initialization of the readers, if the configurationFile is not present
     * the reader will start from the beginning of the file
     * @throws CheckPointException raised if the check point file is corrupted
     */
    private void setup() throws CheckPointException {
        gh = new GraphHopperInstance();
        gh.preprocessing(ghPath);
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
     * run the setup and then the process function in order to compute the processing
     */
    public void computeDump() throws CheckPointException, IOException {
        setup();
        long time = System.nanoTime();
        process();
        System.out.println("Elapsed time in second: " + (System.nanoTime()-time)/1000000000);
    }

}
