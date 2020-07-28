package util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class JSonExcelConverter {

    public static void convert(JSONArray json, String path) throws IOException{
        ArrayList<String> fields = new ArrayList<>();
        fields.add("id");
        fields.add("lat");
        fields.add("lon");
        //TODO maybe not necessary scan all the array
        for(int i=0; i < json.length()/4; i++) {
            JSONObject elem = json.getJSONObject(0);
            for(String s : elem.keySet()) {
                if(!fields.contains(s))
                    fields.add(s);
            }
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("converted_from_json");
        Row row = sheet.createRow(0);
        for(int i = 0; i < fields.size(); i++)
            row.createCell(i).setCellValue(fields.get(i));
        for(int i = 0; i < json.length(); i++) {
            row = sheet.createRow(i+1);
            JSONObject elem = json.getJSONObject(i);
            for(String s : elem.keySet())
                if(elem.get(s) instanceof Double)
                    row.createCell(fields.indexOf(s)).setCellValue((Double) elem.get(s));
                else
                    row.createCell(fields.indexOf(s)).setCellValue(elem.get(s).toString());
        }

        try(FileOutputStream outputStream = new FileOutputStream(path))
        {
            workbook.write(outputStream);
        }
    }

    public static void convert(String jsonPath, String path) throws IOException{
        File file = new File(jsonPath);
        StringBuilder sb;
        try(
                Scanner reader = new Scanner(file)
        ){
            sb = new StringBuilder();
            while (reader.hasNextLine()) {
                sb.append(reader.nextLine());
            }
            JSONObject obj = new JSONObject(sb.toString());
            convert(obj.getJSONArray("elements"),path);
        }
    }

}
