package overlay_matrix_graph;

import location_iq.ExternalCSVDump;

import java.io.*;
import java.util.Map;

public class MatrixOverlayGraphManager {
    private static final String PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private static final String DEPOT_CUSTOMER = "GHDumpFolder\\Depot-Customer\\";
    private static final String DEPOT_DEPOT = "GHDumpFolder\\Depot-depot\\";
    private static final String OVERLAY_GRAPH = "OverlayGraph\\OverlayGraph";

    private MatrixOverlayGraph graph;
    private ExternalCSVDump dump;

    /**
     * If a file with the graph exists this will be loaded, otherwise the graph will be created
     * parsing the dump file and then will be written
     */
    public void loadOrCreateGraph() {
        if(new File(PATH+OVERLAY_GRAPH).exists())
            loadGraph();
        else {
            createGraph();
        }
        graph.print();
    }

    private void createGraph() {
        graph = new MatrixOverlayGraph();
        parse(PATH + DEPOT_DEPOT);
        saveGraph();
        graph.createSupporters();
    }

    private void parse(String path) {
        dump = new ExternalCSVDump(path, true);
        File folder = new File(path);
        File[] files = folder.listFiles();
        for(File f : files)
            try {
                parseDump(f.getName());
            } catch (IOException e) {
                System.err.println("IOException parsing file " + f.getName());
            }
    }

    /**
     * Parse all the file and put all the path n the object Source that will contains all the routes
     * with this node as Origin and then put this object in the first graph hashMap identified by
     * the node's code
     * @param dumpName File to be read with inside the information of all the routes from a specific
     * point and all the others, the name of the file is equal to te source point code.
     * @throws IOException Exception raised if there are problems with the dump file
     */
    private void parseDump(String dumpName) throws IOException {
        dump.createParserFile(dumpName);
        String code = dumpName.replace(".csv","");
        Source source = new Source();
        Map response = dump.parseNext();
        while (response != null) {
            source.addNewPath(response);
            response = dump.parseNext();
        }
        graph.put(code,source);
    }

    /**
     * Write the graph into an external file
     */
    private void saveGraph() {
        try(FileOutputStream fileOut = new FileOutputStream(PATH + OVERLAY_GRAPH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("The Graph was succesfully written to a file");
    }

    /**
     * Read the graph from the external file
     */
    private void loadGraph() {
        try(FileInputStream fis = new FileInputStream(PATH + OVERLAY_GRAPH);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph = (MatrixOverlayGraph) ois.readObject();
            System.out.println("Graph loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prinfGraph() {
        System.out.println("Our Graph is: ");
        graph.print();
    }

    public static void main(String[] args) {
        MatrixOverlayGraphManager m = new MatrixOverlayGraphManager();
        m.loadOrCreateGraph();
    }

}
