package input_output;

import input_output.exceptions.FileInWrongFormatException;
import objects.ParamsObject;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

import static controllers.Controller.*;

public class ExternalFileManager {

    private String path = "";
    private File file;

    /**
     * Constructor of the ExternalFileManager object
     * check if the work directory already exists, if this directory does not exist it will be created
     * @param dirName name of the work directory
     */
    public ExternalFileManager (String dirName) {
        this.file = new File(dirName);
        if (!file.exists())
            file.mkdir();
    }

    public ExternalFileManager () {

    }

    public boolean fileExists(String path) {
        if(this.path.compareTo(path) != 0)
            file = new File (path);
        return file.exists();
    }

    public void fileInCorrectFormat(String path, String[] list) throws FileInWrongFormatException {
        if(this.path.compareTo(path) != 0)
            file = new File (path);
        for(String s : list)
            if(path.endsWith("." + s))
                return;
        throw new FileInWrongFormatException("The format of this file is not acceptable\n" +
                "acceptable format: " + Arrays.toString(list));
    }

    public List<String> getGraphs() {
        file = new File(graphPath);
        return Arrays.stream(Objects.requireNonNull(file.listFiles(File::isDirectory))).map(File::getName).collect(Collectors.toList());
    }

    public void deleteGraph(String graphName) throws IOException {
        file = new File(graphPath + "\\" + graphName);
        deleteDirectoryRecursion(file);
    }

    public void deleteInnerFiles(String path) throws IOException {
        file = new File(path);
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
    }

    public void deleteDirectoryRecursion(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }

    public boolean dirContainsFiles(String path) {
        file = new File(path);
        if (!file.isDirectory())
            return false;
        return file.listFiles().length > 0;
    }

    public boolean createFolderFromPath(String path) {
        file = new File(path);
        return file.mkdir();
    }

    public void createConfigFile(ParamsObject po) throws IOException {
        if(!fileExists(graphConfigPath))
            createFolderFromPath(graphConfigPath);
        else if (fileExists(graphConfigPath + "\\configuration.json")) {
            ParamsObject oldPo = readConfigFile();
            po.update(oldPo);
        }
        JSONObject obj = new JSONObject();

        obj.put("angleHint", po.isAngleHint());
        obj.put("overlayOnly", po.isOverlayOnly());
        obj.put("thresholdNeighbour", po.getNeighbourThreshold());
        obj.put("neighbourDistance", po.getNeighbourDistance());
        obj.put("NumberNN", po.getNumberNN());
        obj.put("splitOnLatitude", po.isSplitLatitude());

        try (FileWriter fw = new FileWriter(graphConfigPath + "\\configuration.json")) {
            fw.write(obj.toString());
        }
    }

    public ParamsObject readConfigFile () throws IOException {
        if(!fileExists(graphConfigPath) || !fileExists(graphConfigPath + "\\configuration.json")) {
            return null;
        }
        ParamsObject po = new ParamsObject();
        JSONObject obj;
        try (Scanner sc = new Scanner(new File(graphConfigPath + "\\configuration.json")))
        {
            obj = new JSONObject(sc.nextLine());
        }

        try { po.setAngleHint((Boolean) obj.get("angleHint")); } catch (Exception e) {}
        try { po.setOverlayOnly((Boolean)obj.get("overlayOnly")); } catch (Exception e) {}
        try { po.setNeighbourThreshold((Integer) obj.get("thresholdNeighbour")); } catch (Exception e) {}
        try { po.setNeighbourDistance((Integer) obj.get("neighbourDistance")); } catch (Exception e) {}
        try { po.setNumberNN((Integer)obj.get("NumberNN")); } catch (Exception e) {}
        try { po.setSplitLatitude((Boolean)obj.get("splitOnLatitude")); } catch (Exception e) {}

        return po;
    }

}
