package location_iq;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ExternalJSONDump extends ExternalDumpManager {
    private static final String EXTENSION = ".json";
    private JSONObject jsonFileObject;

    public ExternalJSONDump (String path){
        super(path);
        jsonFileObject = new JSONObject();
    }

    @Override
    public ExternalDumpManager writeDump() throws IOException {
        try (FileWriter f = new FileWriter(path + file + EXTENSION)) {
            f.write(jsonFileObject.toJSONString());
            f.flush();
        }
        jsonFileObject.clear();
        return this;
    }

    @Override
    public ExternalDumpManager saveData(Map object, String code){
        jsonFileObject.put(code,new JSONObject(object));
        return this;
    }
}
