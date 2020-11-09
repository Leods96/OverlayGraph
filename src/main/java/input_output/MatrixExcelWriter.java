package input_output;

import controllers.exceptions.OutputFileException;
import objects.DistanceObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public interface MatrixExcelWriter {

    /**
     * Write the distance matrix into an excel file
     * @param pathToWrite path of the new file
     * @param result A no-empty list of DistanceObject
     * @param outputFormat The format of the distance matrix
     */
    static void write(String pathToWrite, List<DistanceObject> result, OutputFormat outputFormat) throws OutputFileException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("result");

            switch (outputFormat) {
                case EXCEL_FILE_LIST:
                    printFileList(result, sheet);
                    break;
                case TRIANGULAR_DISTANCE_MATRIX:
                    printTriangular(result, sheet);
                    break;
                case SQUARE_DISTANCE_MATRIX:
                    printSquare(result, sheet);
                    break;
            }

            try (FileOutputStream outputStream = new FileOutputStream(pathToWrite + ".xlsx")) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            throw new OutputFileException();
        }
    }

    static void printFileList(List<DistanceObject> result, XSSFSheet sheet) {
        int counter = 0;
        Row row = sheet.createRow(counter);
        counter++;
        row.createCell(0).setCellValue("Origin");
        row.createCell(1).setCellValue("Distance");
        row.createCell(2).setCellValue("Destination");
        for(DistanceObject elem : result) {
            row = sheet.createRow(counter);
            row.createCell(0).setCellValue(elem.getOrigin());
            row.createCell(1).setCellValue(elem.getDistance());
            row.createCell(2).setCellValue(elem.getDestination());
            counter++;
        }
    }

    static void printTriangular(List<DistanceObject> result, XSSFSheet sheet) {
        int rowCounter;
        for (rowCounter = 0; result.get(rowCounter).getOrigin().compareTo(result.get(0).getOrigin()) == 0; rowCounter++)
            sheet.createRow(rowCounter);
        sheet.createRow(rowCounter);
        int k = 0;
        for (int i = 0; i < rowCounter; i++) {
            sheet.getRow(i).createCell(0).setCellValue(result.get(k).getOrigin());
            for (int j = i + 1; j < rowCounter + 1; j ++) {
                sheet.getRow(j).createCell(i + 1).setCellValue(result.get(k).getDistance());
                k++;
            }
        }
        sheet.getRow(rowCounter).createCell(0).setCellValue(result.get(k - rowCounter + 2).getDestination());
    }

    static void printSquare(List<DistanceObject> result, XSSFSheet sheet) {
        int rowCounter;
        String origin = result.get(0).getOrigin();
        //Creation of rows
        for (rowCounter = 0; result.get(rowCounter).getOrigin().compareTo(origin) == 0; rowCounter++)
            sheet.createRow(rowCounter);
        sheet.createRow(rowCounter);
        int k = 0;
        for (int i = 0; i < rowCounter + 1; i ++) {
            sheet.getRow(i).createCell(0).setCellValue(result.get(k).getOrigin());
            for (int j = 0; j < rowCounter + 1; j ++) {
                if (i == j)
                    continue;
                sheet.getRow(j).createCell(i + 1).setCellValue(result.get(k).getDistance());
                k++;
            }
        }
    }
}
