package input_output;

import input_output.exceptions.CheckPointException;
import objects.CheckPoint;

import java.io.*;
import java.util.Scanner;

import static controllers.Controller.cpPath;

/**
 * Object that manage the access to an external file used to save and read the check-points
 * It is useful during long computation to split the work into different steps
 */
public class ExternalCheckPointManager {
    private static final String CHECKPOINT_FILE = "checkPoint.txt";
    private static final String CHECK_POINT_DATA = "graphData.txt";
    private File checkPointFile = null;

    public ExternalCheckPointManager() {
        //Empty constructor
    }

    /**
     * Read the content of the check point file
     * @return CheckPoint object containing the checkpoint's data
     * @throws FileNotFoundException if the file does not exist
     * @throws CheckPointException if the file is corrupted
     */
    public CheckPoint getCheckPoint() throws FileNotFoundException, CheckPointException {
        if(checkPointFile == null)
            this.checkPointFile = new File(cpPath + "\\" + CHECKPOINT_FILE);
        if(!checkPointFile.exists())
            throw new FileNotFoundException("CheckPoint file no exists");
        String[] from;
        String[] to;
        try (Scanner scannerCP = new Scanner(checkPointFile)) {
            from = scannerCP.nextLine().split("-");
            to = scannerCP.nextLine().split("-");
        }
        checkFileCorruption(from, to);
        return new CheckPoint(from[1],to[1]);
    }

    public CheckPoint getFileData() throws FileNotFoundException{
        this.checkPointFile = new File(cpPath + "\\" + CHECK_POINT_DATA);
        CheckPoint cp = new CheckPoint();
        try (Scanner scannerCP = new Scanner(checkPointFile)) {
            cp.setGraphName(scannerCP.nextLine());
            cp.setInputFilePath(scannerCP.nextLine());
            cp.setFileIndex(Integer.parseInt(scannerCP.nextLine()));
            cp.setKdTree(Boolean.parseBoolean(scannerCP.nextLine()));
        }
        this.checkPointFile = null;
        return cp;
    }

    /**
     * Check the correctness of the check point file, if this is corrupted the exception is raised
     * @param from content of the file
     * @param to content of the file
     * @throws CheckPointException Raised if the file is corrupted
     */
    private void checkFileCorruption(String[] from, String[] to) throws CheckPointException {
        if(!from[0].equalsIgnoreCase("from") || !to[0].equalsIgnoreCase("to"))
            throw new CheckPointException("Check point file corrupted");
        if(from.length != 2 || to.length != 2)
            throw new CheckPointException("Check point file corrupted");
    }

    /**
     * Delete the check point file
     */
    public void deleteCheckPointFile() {
        this.checkPointFile = new File(cpPath + "\\" + CHECKPOINT_FILE);
        if(checkPointFile.exists() && checkPointFile.delete())
                System.out.println("CheckPoint file deleted");
        this.checkPointFile = new File(cpPath + "\\" + CHECK_POINT_DATA);
        if(checkPointFile.exists() && checkPointFile.delete())
            System.out.println("CheckPointData file deleted");
    }

    /**
     * Write the new check point to the file
     * @param from Data that will be saved in the checkpoint
     * @param to Data that will be saved in the checkpoint
     */
    public void createCheckPoint(String from, String to) throws IOException{
        if(checkPointFile == null)
            this.checkPointFile = new File(cpPath + "\\" + CHECKPOINT_FILE);
        try(
                FileWriter fw = new FileWriter(checkPointFile);
                PrintWriter printWriter = new PrintWriter(fw)
        ) {
            printWriter.println("from-" + from);
            printWriter.println("to-" + to);
        }
    }

    public void createCheckPointFileData(String inputAddressFile, int sheetIndex, String graphName, boolean useKdTree) throws IOException{
        this.checkPointFile = new File(cpPath + "\\" + CHECK_POINT_DATA);
        try(
                FileWriter fw = new FileWriter(checkPointFile);
                PrintWriter printWriter = new PrintWriter(fw)
        ) {
            printWriter.println(graphName);
            printWriter.println(inputAddressFile);
            printWriter.println(sheetIndex);
            printWriter.println(useKdTree);
        }
    }
}
