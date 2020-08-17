package overlay_matrix_graph;

import junit.framework.TestCase;
import util.Util;

import static org.junit.Assert.fail;

public class DumpCreationTest extends TestCase {

    private final String FROM_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private final String TO_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private final String DUMP_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\ProvinceBoudariesDump\\";
    private final String CONFIG_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\checkPoint\\";
    private final int FROM_SHEET_NUM = 0;
    private final int TO_SHEET_NUM = 0;

    public void testDumpCreation() {
        try {
            Util.creationOfDump(FROM_FILE, FROM_SHEET_NUM, TO_FILE, TO_SHEET_NUM, DUMP_FOLDER, null);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
