package location_iq;

import location_iq.Exceptions.BadResponseException;
import location_iq.Exceptions.CellTypeException;
import location_iq.Exceptions.CheckPointException;
import location_iq.Exceptions.DayRequestFinishedException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static location_iq.Helper.formatSize;

public class Controller {

    private static final String BASE_PATH = "C:\\Users\\leo\\Desktop\\Stage\\Progetto\\Addresses\\";
    private static final String FILE = BASE_PATH + "geocodedAddresses.xlsx";
    private static final String DUMP_FOLDER = BASE_PATH + "csvDistanceDump\\";
    private static final String CONFIGURATION_FOLDER = BASE_PATH + "configurationData\\";
    //<true> = serial used of tokens, <false> = simultaneus used of tokens
    private static final boolean SERIAL_PROCESS = false;

    private String token;
    private ArrayList<String> tokens;
    private boolean tokenFinished = false;
    private LocIQAPI api;
    private ExcellReader fromReader;
    private ExcellReader toReader;
    private ExternalDumpManager dumpManager;
    private ExternalConfigurationManager configurationManager;
    private ResponseManager rm;
    private static long total = 0,free = 0;


    public Controller(){
        api = new LocIQAPI();
        rm = new ResponseManager();
        try {
            fromReader = new ExcellReader(FILE);
            toReader = new ExcellReader(FILE);
            dumpManager = new ExternalCSVDump(DUMP_FOLDER, false);
            configurationManager = new ExternalConfigurationManager(CONFIGURATION_FOLDER);
        }catch(IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void newToken() {
        try {
            this.token = configurationManager.readToken();
            api.setToken(token);
        } catch (EOFException ex) {
            tokenFinished = true;
        }
    }

    public void checkBalance() throws InterruptedException {
        try {
            api.setToken(token);
            rm.printBalance(api.balance());
        }catch(IOException | BadResponseException |DayRequestFinishedException e){
            e.printStackTrace();
        }
        Thread.sleep(1000);
    }

    private boolean nextFromReaderRowForMultipleTokens() {
        try {
            fromReader.nextRow();
            dumpManager.setFile(fromReader.getID());
        }catch(CellTypeException e){
            System.out.println("Problem reading a Cell in row: "+ fromReader.getRowNumber()+ " for file creation...continue to next row..");
            return nextFromReaderRowForMultipleTokens();
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public void printReaders() {
        try {
            System.out.println("From = " + fromReader.getID());
            System.out.println("To = " + toReader.getID());
        } catch (CellTypeException e) {
            System.out.println("Exception with IDs");
        }
    }

    public void proc() throws InterruptedException, IOException {
        CheckPoint cp = null;
        int requestCounter = 0;
        long time = 1;
        boolean skip = false;
        boolean process = true;
        while (process) {
            for (int i = 0; i < tokens.size() && !skip; i++) {
                if (toReader.hasNext()) {
                    toReader.nextRow();
                } else {
                    process = switchContext();
                    skip = true;
                }
                if (!skip) {
                    api.setToken(tokens.get(i));
                    try {
                        System.out.println("Request nr. " + ++requestCounter);
                        printReaders();
                        cp = standarRequest();
                        System.out.println("Request done");
                        if(i == 0)
                            time = System.nanoTime();
                        if(cp != null)
                            checkPoint(cp);
                    } catch (DayRequestFinishedException e) {
                        System.out.println("Possible dayly Request finished");
                        skip = true;
                        process = false;
                        dumpManager.writeDump();
                    }
                }
            }
            skip = false;
            if(process)
                sleep(time);
        }
        if(cp != null)
            checkPoint(cp);
    }

    private boolean switchContext() throws IOException{
        System.out.println("Switch context of Excell Readers");
        toReader.initializeIterator();
        dumpManager.writeDump();
        return fromReader.nextRow();
    }

    public void processWithMultipleTokens() throws InterruptedException, IOException{
        CheckPoint cp = null;
        int requestCounter = 0;
        long time = 1;
        boolean process = nextFromReaderRowForMultipleTokens();
        while(process){
            int cycleCounter = 0;
            for(String t : tokens){
                api.setToken(t);
                if(toReader.hasNext()) {
                    toReader.nextRow();
                } else if(fromReader.hasNext()) {
                    dumpManager.writeDump();
                    process = nextFromReaderRowForMultipleTokens();
                    toReader.initializeIterator();
                } else {
                    process = false;
                }
                if(process) {
                    try {
                        cp = standarRequest();
                        System.out.println(" - "+ ++requestCounter + "° request Performed");
                        if(cp != null)
                            System.out.println("Request between " + cp.getFrom() + " & " + cp.getTo() + "Executed");
                    } catch (DayRequestFinishedException e) {
                        process = false;
                        if (cp != null) {
                            checkPoint(cp);
                        }
                    } catch (IOException e) {
                        System.err.println("IOException !!");
                        e.printStackTrace();
                        checkPoint(cp);
                        process = false;
                    }
                }
                if(cycleCounter == 0) {
                    time = System.nanoTime();
                }
                cycleCounter++;
            }
            dumpManager.writeDump();
            if(process)
                sleep(time);
        }
        if(toReader.isFinished() && fromReader.isFinished())
            configurationManager.deleteCheckPointFile();
    }

    private void sleep(long time) throws InterruptedException{
        time =  System.nanoTime() - time;
        if (time / 1000000 < 1000) {
            System.out.println("sleep for " + (1000 - time / 1000000) + " millisec\n\n");
            Thread.sleep(1000 - time / 1000000);
        }
    }

    private void checkPoint(CheckPoint cp) {
        try {
            configurationManager.createCheckPoint(cp.getFrom(), cp.getTo());
        }catch (IOException e){
            System.err.println("Problem writing the checkPoint File");
            e.printStackTrace();
        }
    }

    private void processWithSerialTokens() throws InterruptedException, IOException {
        boolean newRequest = true;
        CheckPoint cp = null;
        System.out.println("Processing...");
        while (!tokenFinished && fromReader.hasNext()) {
            fromReader.nextRow();
            try {
                dumpManager.setFile(fromReader.getID());
            }catch(CellTypeException e){
                System.out.println("Problem reading a Cell in row: "+ fromReader.getRowNumber()+ " for file creation...continue to next row..");
                continue;
            }
            while (!tokenFinished && toReader.hasNext()) {
                if(newRequest) {
                    toReader.nextRow();
                }
                newRequest = true;
                if (toReader.getNumSheet() != fromReader.getNumSheet() || toReader.getRowNumber() != fromReader.getRowNumber()) {
                    System.out.println("Processing " + fromReader.getRowNumber() + " - " + toReader.getRowNumber());
                    try {
                        cp = standarRequest();
                    }catch (DayRequestFinishedException e) {
                        newToken();
                        newRequest = false; //we have to re-do the same request switching the token
                    }
                    System.out.println("Request done. Let's sleep for 1 sec");
                    Thread.sleep(1000);
                }
            }
            dumpManager.writeDump();
            toReader.initializeIterator();
        }
        if(cp != null && (tokenFinished || !toReader.isFinished() || !fromReader.isFinished())){
            checkPoint(cp);
        }else if(toReader.isFinished() && fromReader.isFinished())
            configurationManager.deleteCheckPointFile();
    }

    public void checkMemory(){
        total = Runtime.getRuntime().totalMemory();
        free = Runtime.getRuntime().freeMemory();
        System.out.println(String.format("Memory usage: Total: " + formatSize(total) + " Free: " + formatSize(free)));
    }

    private CheckPoint standarRequest() throws DayRequestFinishedException, IOException{
        Point from = null;
        Point to = null;
        try {
            from = new Point(fromReader.getID(), fromReader.getLatitude(), fromReader.getLongitude());
            to = new Point(toReader.getID(), toReader.getLatitude(), toReader.getLongitude());
            api.setCoordinates(from, to);
            dumpManager.saveData(rm.filterResponse(api.request()).toMap(), to.getCode());
        } catch (CellTypeException e) {
            System.err.println("Cell type exception reding rowFrom: " + fromReader.getRowNumber() + ", rowTo: " + toReader.getRowNumber());
            return null;
        } catch (BadResponseException e) {
            System.err.println("BadResponseException \ncode = " + e.getCode()
                    + "\n message = " + e.getMessage()
                    + "\n exception = ");
            e.printStackTrace();
            JSONObject response = new JSONObject();
            response.put("code", e.getCode());
            response.put("message", e.getMessage());
            dumpManager.saveData(response.toMap(), to.getCode());
        }
        return new CheckPoint(from.getCode(), to.getCode());
    }

    private void setup() throws CheckPointException, EOFException {
        if(SERIAL_PROCESS) {
            newToken();
            api.setToken(token).setDirectionsAsService().setDirectionOptions(true, false);
        }else {
            tokens = configurationManager.parseToken();
            api.setDirectionsAsService().setDirectionOptions(true, false);
        }
        rm.setForCSV();
        setUpCheckPoint();
    }

    public void setUpCheckPoint() throws CheckPointException{
        try {
            CheckPoint cp = configurationManager.getCheckPoint();
            fromReader.setSheetWithIndex(1).initializeIterator(cp.getFrom());
            fromReader.nextRow();
            toReader.setSheetWithIndex(0).initializeIterator(cp.getTo());
        } catch (FileNotFoundException e) {
            System.out.println("There isn't a CheckPoint file -> start from beginning");
            fromReader.setSheetWithIndex(1).initializeIterator();
            fromReader.nextRow();
            toReader.setSheetWithIndex(0).initializeIterator();
        }
    }

    public void process(){
        try {
            setup();
            if (SERIAL_PROCESS)
                processWithSerialTokens();
            else
                proc();
        }catch(InterruptedException e){
            System.err.println("Problem with sleep");
        }catch (CheckPointException e) {
            e.printStackTrace();
        }catch(EOFException e){
            System.err.println("Problem with token file");
        }catch (IOException e) {
            System.err.println("Problem writing the dump, execution failed and data not saved on file");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Controller c = new Controller();
        c.process();
    }

}

