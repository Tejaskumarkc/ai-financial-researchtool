package com.assignmente.researchtool.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ExcelService {

    public byte[] generateExcel(String jsonData) {
        try {
            // clean AI text
            jsonData = jsonData.replace("\n","")
                    .replace("\t","")
                    .replace("}{","},{")
                    .trim();

            if(!jsonData.startsWith("[")){
                jsonData = "[" + jsonData + "]";
            }

            JSONArray arr = new JSONArray(jsonData);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Financial Data");

            // header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Year");
            header.createCell(1).setCellValue("Revenue");
            header.createCell(2).setCellValue("Profit");

            for(int i=0;i<arr.length();i++){
                JSONObject obj = arr.getJSONObject(i);
                Row row = sheet.createRow(i+1);

                row.createCell(0).setCellValue(obj.optString("Year"));
                row.createCell(1).setCellValue(obj.optString("Revenue"));
                row.createCell(2).setCellValue(obj.optString("Profit"));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return out.toByteArray();

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
