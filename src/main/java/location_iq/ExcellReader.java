package location_iq;

import location_iq.exceptions.CellTypeException;
import location_iq.exceptions.CheckPointException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class ExcellReader {
    private String filename;
    private int numSheet;
    private XSSFWorkbook workBook = null;
    private XSSFSheet workSheet = null;
    private Iterator<Row> rowIterator;
    private Row rowOnWorking;
    private boolean finished = false;

    public XSSFSheet getWorkSheet(){
        return this.workSheet;
    }

    public ExcellReader(String fileName) throws IOException {
        openFile(fileName);
    }

    public ExcellReader initializeIterator(String identifier) throws CheckPointException {
        this.rowIterator = workSheet.rowIterator();
        Iterator<Row> temp = workSheet.rowIterator();
        if(!temp.hasNext())
            throw new CheckPointException("File vuoto");
        temp.next();
        boolean equal = false;
        while(temp.hasNext() && !equal)
            try {
                equal = compareCell(temp.next().getCell(0), identifier);
                rowIterator.next();
            }catch(CellTypeException e){
                //skip this cell
            }
        if(equal)
            return this;
        throw new CheckPointException("Impossible to retrieve the check point '" + identifier + "' into the file");
    }

    public ExcellReader initializeIterator(){
        this.rowIterator = workSheet.rowIterator();
        if(rowIterator.hasNext())
            this.rowOnWorking = this.rowIterator.next(); //Jump the first useless line
        return this;
    }

    public int getNumSheet() {
        return numSheet;
    }

    public boolean nextRow(){
        if(!rowIterator.hasNext()) {
            this.finished = true;
            return false;
        }
        rowOnWorking = rowIterator.next();
        return true;
    }

    public boolean hasNext(){
        if(!rowIterator.hasNext())
            this.finished = true;
        return rowIterator.hasNext();
    }

    public boolean isFinished() {
        return finished;
    }

    public int getRowNumber(){
        return rowOnWorking.getRowNum();
    }

    public int getNumberOfRowsInSheet() {
        if (workSheet != null)
            return this.workSheet.getLastRowNum();
        return 0;
    }

    public String getID() throws CellTypeException{
        return getStringCellValue(rowOnWorking.getCell(0));
    }

    public Double getLatitude() throws CellTypeException{
        return getDoubleCellValue(rowOnWorking.getCell(1));
    }

    public Double getLongitude() throws CellTypeException{
        return getDoubleCellValue(rowOnWorking.getCell(2));
    }

    private boolean compareCell(Cell cell, String identifier) throws CellTypeException{
        return getStringCellValue(cell).equalsIgnoreCase(identifier);
    }

    private String getStringCellValue(Cell cell) throws CellTypeException{
        try {
            int type = cell.getCellType();
            switch (type) {
                case 0:
                    return Double.toString(cell.getNumericCellValue());
                case 1:
                    return cell.getStringCellValue();
                case 2:
                    return cell.getCellFormula();
                case 3:
                    return "";
                case 4: {
                    if (cell.getBooleanCellValue())
                        return "true";
                    return "false";
                }
                default:
                    throw new CellTypeException("Impossible to obtain a string value from Cell( " +
                            cell.getRowIndex() + " - " + cell.getColumnIndex() + " )");
            }
        } catch (NullPointerException e) {
            throw new CellTypeException("Empty cell");
        }
    }

    private Double getDoubleCellValue(Cell cell) throws CellTypeException{
        try {
            if (cell.getCellType() == 0)
                return cell.getNumericCellValue();
            throw new CellTypeException("Impossible to obtain a double value from Cell( " +
                    cell.getRowIndex() + " - " + cell.getColumnIndex() + " )");
        } catch (NullPointerException e) {
            throw new CellTypeException("Empty cell");
        }
    }

    public ExcellReader setSheetWithIndex(int index){
        if(index < 0 || index > numSheet)
            throw new IndexOutOfBoundsException("wrong sheet index");
        this.workSheet = workBook.getSheetAt(index);
        return this;
    }

    public ExcellReader setSheetWithName(String name){
        XSSFSheet sheet = workBook.getSheet(name);
        if(sheet != null)
            this.workSheet = sheet;
        else
            throw new IndexOutOfBoundsException("wrong sheet name");
        return this;
    }

    private ExcellReader openFile(String fileName) throws IOException {
        try (
                FileInputStream file = new FileInputStream(new File(fileName))
        )
        {
            workBook = new XSSFWorkbook(file);
            this.numSheet = workBook.getNumberOfSheets(); //Get number of sheets from the workbook
        }
        return this;
    }

    public ExcellReader randomRow() {
        int rowIndex = new Random().nextInt(this.workSheet.getLastRowNum());
        rowOnWorking = workSheet.getRow(rowIndex);
        return this;
    }

}
