package input_output;

import input_output.exceptions.FileInWrongFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static controllers.Controller.graphPath;

public class ExternalFileManager {

    private String workDirPath;
    private String path = "";
    private File file;

    public ExternalFileManager(String workDirPath) {
        this.workDirPath = workDirPath;
    }

    public void fileExists(String path) throws FileNotFoundException {
        if(this.path.compareTo(path) != 0)
            file = new File (path);
        if (!file.exists())
            throw new FileNotFoundException(path + " file does not exist");
    }

    public void fileInCorrectFormat(String path, String[] list) throws FileInWrongFormatException {
        if(this.path.compareTo(path) != 0)
            file = new File (path);
        for(String s : list)
            if(path.endsWith("." + s))
                return;
        throw new FileInWrongFormatException("The format of this file is not accettable\n" +
                "Accettable format: " + Arrays.toString(list));
    }

    public List<String> getGraphs() {
        file = new File(graphPath);
        return Arrays.stream(Objects.requireNonNull(file.listFiles(File::isDirectory))).map(File::getName).collect(Collectors.toList());
    }

    public void deleteGraph(String graphName) {
        file = new File(graphPath + "\\" + graphName);
        file.delete();
    }
}
