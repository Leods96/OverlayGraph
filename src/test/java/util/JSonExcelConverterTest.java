package util;

import junit.framework.TestCase;

public class JSonExcelConverterTest extends TestCase {

    public void testConverter() {
        try {
            JSonExcelConverter.convert("C:\\Users\\leo\\Desktop\\Stage\\Overpass\\HighwayExits.json",
                    "C:\\Users\\leo\\Desktop\\Stage\\Overpass\\HighwayExits.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(true);
    }
}