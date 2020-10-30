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
        boolean thereIsDestination = result.get(0).getDestination() != null;
        Row row = sheet.createRow(counter++);
        row.createCell(0).setCellValue("Origin");
        row.createCell(1).setCellValue("Distance");
        if(thereIsDestination)
            row.createCell(2).setCellValue("Destination");
        for(DistanceObject elem : result) {
            row = sheet.createRow(counter++);
            row.createCell(0).setCellValue(elem.getOrigin());
            row.createCell(1).setCellValue(elem.getDistance());
            if(thereIsDestination)
                row.createCell(2).setCellValue(elem.getDestination());
        }
    }

    static void printTriangular(List<DistanceObject> result, XSSFSheet sheet) {
        int originCounter = 0; //which origin are we reading and which column we are writing
        String origin = result.get(0).getOrigin();
        sheet.createRow(0);
        int i;
        for (int k = 0; k < result.size(); k = i) {
            for (i = 0; result.get(i + k).getOrigin().compareTo(origin) == 0; i++) {
                if (originCounter == 0)
                    sheet.createRow(i + 1);
                if (i == 0)
                    sheet.getRow(originCounter).createCell(0).setCellValue(origin);
                sheet.getRow(originCounter + i + 1).createCell(originCounter + 1).setCellValue(result.get(i + k).getDistance());
            }
            originCounter++;
            origin = result.get(i + k).getOrigin();
        }
    }

    static void printSquare(List<DistanceObject> result, XSSFSheet sheet) {
        int rowCounter;
        int columnCounter = 1;
        String origin = result.get(0).getOrigin();
        //Creation of rows
        for (rowCounter = 0; result.get(rowCounter).getOrigin().compareTo(origin) == 0; rowCounter++)
            sheet.createRow(rowCounter);
        for (int i = 0; i < result.size()/rowCounter; i ++) {
            for (int j = 0; j < rowCounter; j ++) {
                if (i == j)
                    continue;
                sheet.getRow(j).createCell(columnCounter).setCellValue(result.get(j + i * (rowCounter - 1)).getDistance());
            }
            columnCounter++;
        }

    }
}
