package input_output;

import java.io.IOException;
import java.util.Map;

public abstract class ExternalDumpManager {
    protected String path;
    protected String file;

    public ExternalDumpManager (String path){
        this.path = path;
    }

    public ExternalDumpManager setFile(String file){
        this.file = file;
        return this;
    }

    public ExternalDumpManager setPath(String path){
        this.path = path;
        return this;
    }

    public ExternalDumpManager saveData(Map object, String code) {
        return this;
    }

    public ExternalDumpManager writeDump() throws IOException {
        return this;
    }

    public ExternalDumpManager parseFile() throws IOException {
        return this;
    }

}
