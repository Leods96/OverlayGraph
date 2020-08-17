package location_iq;

import location_iq.exceptions.CheckPointException;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ExternalConfigurationManager {
    private static final String TOKEN_FILE_NAME = "token.txt";
    private static final String CHECKPOINT_FILE_NAME = "checkPoint.txt";
    private String path;
    private String token;
    private Scanner scannerToken;
    private File tokenFile = null;
    private File checkPointFile = null;


    public ExternalConfigurationManager(String path) throws FileNotFoundException {
        //tokenFile = new File(path + TOKEN_FILE_NAME);
        //scannerToken = new Scanner(tokenFile);
        this.path = path;
    }

    public String readToken() throws EOFException {
        if(scannerToken.hasNextLine())
            return scannerToken.nextLine();
        throw new EOFException();
    }

    public ArrayList<String> parseToken() {
        ArrayList<String> tokens = new ArrayList<>();
        while(scannerToken.hasNextLine())
            tokens.add(scannerToken.nextLine());
        return tokens;
    }

    public CheckPoint getCheckPoint() throws FileNotFoundException, CheckPointException {
        if(checkPointFile == null)
            this.checkPointFile = new File(path + CHECKPOINT_FILE_NAME);
        if(!checkPointFile.exists())
            throw new FileNotFoundException("CheckPoint file no exists");
        Scanner scannerCP = new Scanner(checkPointFile);
        String[] from = scannerCP.nextLine().split("-");
        String[] to = scannerCP.nextLine().split("-");
        scannerCP.close();
        if(!from[0].equalsIgnoreCase("from") || !to[0].equalsIgnoreCase("to"))
            throw new CheckPointException("Incorrect file format");
        return new CheckPoint(from[1],to[1]);
    }

    public void deleteCheckPointFile() {
        if(checkPointFile == null)
            this.checkPointFile = new File(path + CHECKPOINT_FILE_NAME);
        if(checkPointFile.exists())
            if(checkPointFile.delete())
                System.out.println("CheckPoint file deleted");
    }

    public void createCheckPoint(String from, String to) throws IOException {
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
