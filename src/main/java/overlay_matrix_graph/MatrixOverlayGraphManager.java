package overlay_matrix_graph;

import com.sun.org.apache.xalan.internal.lib.NodeInfo;
import location_iq.ExcellReader;
import location_iq.Exceptions.CellTypeException;
import location_iq.ExternalCSVDump;
import location_iq.Point;
import org.omg.CORBA.Object;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.quadTree.QuadTreeNode;

import java.io.*;
import java.util.Map;

public class MatrixOverlayGraphManager {
    private static final String PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private static final String DEPOT_CUSTOMER = "GHDumpFolder\\Depot-Customer\\";
    private static final String DEPOT_DEPOT = "GHDumpFolder\\Depot-depot\\";
    private static final String OVERLAY_GRAPH = "OverlayGraph\\OverlayGraph";
    private static final String OVERLAY_GRAPH_QUADTREE = "OverlayGraph\\QuadTree";
    private static final String POINTS_GEOCODE = "geocodedAddresses.xlsx";

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
            System.out.println("No file with the graph exists");
            createGraph();
        }
        //graph.print();
    }

    private void createGraph() {
        System.out.println("The graph will be created from scratch");
        graph = new MatrixOverlayGraph();
        parse(PATH + DEPOT_DEPOT);
        saveGraph();
        graph.createSupporters();
        saveSupporters();
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
     * Parse all the file and put all the path in the object Source that will contains all the routes
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
        try {
            source.setNodeInfo(parseNodeInfo(code));
            graph.put(code,source);
        } catch (IOException e) {
            System.out.println("The point " + code + " has not be put into the graph for this error: ");
            e.printStackTrace();
        }
    }

    private Point parseNodeInfo(String code) throws IOException {
        ExcellReader exr = new ExcellReader(PATH + POINTS_GEOCODE);
        exr.setSheetWithIndex(1).initializeIterator(); //TODO no be hardcoded
        while(exr.nextRow()) {
            try {
                if(exr.getID().equalsIgnoreCase(code))
                    return new Point(code, exr.getLatitude(), exr.getLongitude());
            } catch(CellTypeException e) {
                System.err.println("Exception reading " + code + "'s data");
            }
        }
        throw new IOException(code + "'s data not founded");
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
        System.out.println("The Graph was successfully written to a file");
    }

    private void saveSupporters() {
        try(FileOutputStream fileOut = new FileOutputStream(PATH + OVERLAY_GRAPH_QUADTREE);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph.getSupporters());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Graph's supporters were successfully written to a file");
    }

    /**
     * Read the graph from the external file
     */
    private void loadGraph() {
        System.out.println("The Graph will be loaded from an external file");
        try(FileInputStream fis = new FileInputStream(PATH + OVERLAY_GRAPH);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph = (MatrixOverlayGraph) ois.readObject();
            System.out.println("Graph loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSupporters();
    }

    public void loadSupporters() {
        try(FileInputStream fis = new FileInputStream(PATH + OVERLAY_GRAPH_QUADTREE);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph.setQuadTreeSupport((QuadTreeNode) ois.readObject());
            System.out.println("supporters loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OverlayResponse route(Point fromPoint, Point toPoint) throws NodeCodeNotInOverlayGraphException{
            return graph.route(fromPoint, toPoint);
    }

    public void printGraph() {
        System.out.println("Our Graph is: ");
        graph.print();
    }


}
