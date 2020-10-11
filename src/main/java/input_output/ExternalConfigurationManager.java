package input_output;

import input_output.exceptions.CheckPointException;

import java.io.*;
import java.util.Scanner;

/**
 * Object that manage the access to an external file used to save and read the check-points
 */
public class ExternalConfigurationManager {
    private static final String CHECKPOINT_FILE_NAME = "checkPoint.txt";
    private final String path;
    private File checkPointFile = null;

    public ExternalConfigurationManager(String path) {
        this.path = path;
    }

    /**
     * Read the content of the check point file
     * @return CheckPoint object containing the checkpoint's data
     * @throws FileNotFoundException if the file does not exist
     * @throws CheckPointException if the file is corrupted
     */
    public CheckPoint getCheckPoint() throws FileNotFoundException, CheckPointException {
        if(checkPointFile == null)
            this.checkPointFile = new File(path + CHECKPOINT_FILE_NAME);
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
        if(checkPointFile == null)
            this.checkPointFile = new File(path + CHECKPOINT_FILE_NAME);
        if(checkPointFile.exists() && checkPointFile.delete())
                System.out.println("CheckPoint file deleted");
    }

    /**
     * Write the new check point to the file
     * @param from Data that will be saved in the checkpoint
     * @param to Data that will be saved in the checkpoint
     */
    public void createCheckPoint(String from, String to) throws IOException{
        if(checkPointFile == null)
            this.checkPointFile = new File(path + CHECKPOINT_FILE_NAME);
        try(
                FileWriter fw = new FileWriter(checkPointFile);
                PrintWriter printWriter = new PrintWriter(fw)
        ) {
            printWriter.println("from-" + from);
            printWriter.println("to-" + to);
        }
    }
}
