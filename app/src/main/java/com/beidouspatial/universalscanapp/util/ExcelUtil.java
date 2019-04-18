package com.beidouspatial.universalscanapp.util;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    private String filepath;
    private InputStream excelStream;
    private List<Map<String, String>> list = new ArrayList<>();

    private Integer limit;
    private Integer startNum;
    private Integer sheetIndex;


    public ExcelUtil(String filepath) {
        super();
        this.filepath = filepath;
    }

    private void readExcel2Stream(String fileName) throws Exception {
        DataInputStream dis = new DataInputStream(new FileInputStream(fileName));
        ByteArrayOutputStream dos = new ByteArrayOutputStream();
        byte[] b = new byte[102];
        int length;
        while ((length = dis.read(b)) != -1) {
            dos.write(b, 2, length - 2);
            dos.flush();
        }
        dis.close();
        dos.close();
        excelStream = new FileInputStream(fileName);
    }

    private void parseXlsxSuffix() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(excelStream);
        XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        if (startNum == null) {
            startNum = sheet.getFirstRowNum();
        }
        int rows = sheet.getLastRowNum();
        if (rows == 0) return;
        XSSFRow row0 = sheet.getRow(startNum);
        Map<Integer, String> colsName = CellValue.getMapColNames(row0);
        Object[] cols = colsName.keySet().toArray();
        Map<String, String> line;
        for (int i = startNum + 1, num = 0; i <= rows; i++) {
            if (sheet.getRow(i) == null)
                continue;
            line = new HashMap<>();
            for (Object col : cols) {
                String data = CellValue.getStringValue(sheet.getRow(i)
                        .getCell((Integer) col), null);
                String colName = colsName.get(col).trim().toLowerCase();
                if (data == null || data.trim().equals("")) {
                    continue;
                }
                line.put(colName, data.trim());
            }
            if (line.size() > 0) {
                list.add(line);
                num++;
                if (num >= limit) {
                    return;
                }
            }
        }
    }

    private void parseXlsSuffix() throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(excelStream);
        HSSFSheet sheet = workbook.getSheetAt(sheetIndex);
        if (startNum == null) {
            startNum = sheet.getFirstRowNum();
        }
        int rows = sheet.getLastRowNum();
        if (rows == 0) return;
        HSSFRow row0 = sheet.getRow(startNum);
        Map<Integer, String> colsName = CellValue.getMapColNames(row0);
        Object[] cols = colsName.keySet().toArray();
        Map<String, String> line = null;
        for (int i = startNum + 1, num = 0; i <= rows; i++) {
            if (sheet.getRow(i) == null)
                continue;
            line = new HashMap<>();
            for (Object col : cols) {
                String data = CellValue.getStringValue(sheet.getRow(i)
                        .getCell((Integer) col), null);
                String colName = colsName.get(col).trim().toLowerCase();
                if (data == null || data.trim().equals("")) {
                    continue;
                }
                line.put(colName, data.trim());
            }
            if (line.size() > 0) {
                list.add(line);
                num++;
                if (num >= limit) {
                    return;
                }
            }
        }
    }

    private void parseExcel() throws IOException {
        String fileSiffix = filepath.substring(filepath.lastIndexOf(".") + 1);
        if ("xls".equalsIgnoreCase(fileSiffix))
            parseXlsSuffix();
        else if ("xlsx".equalsIgnoreCase(fileSiffix))
            parseXlsxSuffix();
    }

    public List<Map<String, String>> getDatas(Integer limit, Integer startNum, Integer sheetIndex) throws Exception {
        if (filepath == null || "".equals(filepath.trim()))
            throw new Exception("请配置读取文件路径");
        this.limit = limit;
        this.startNum = startNum;
        this.sheetIndex = sheetIndex;
        readExcel2Stream(filepath);
        parseExcel();
        return list;
    }

}