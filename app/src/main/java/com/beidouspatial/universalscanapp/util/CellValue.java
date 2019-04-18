package com.beidouspatial.universalscanapp.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellValue {

    /**
     * 返回Excel2003去掉空行的记录数(xls)
     */
    public static int getRightRows(HSSFSheet sheet) {
        int rsRows = sheet.getPhysicalNumberOfRows();
        int nullCellNum;
        int afterRows = rsRows;
        HSSFRow row;
        String cell;
        for (int i = sheet.getFirstRowNum(); i < rsRows; i++) {
            row = sheet.getRow(i);
            nullCellNum = 0;
            if (row == null) {
                afterRows--;
                continue;
            }
            int rsCols = row.getPhysicalNumberOfCells();
            try {
                for (int j = 0; j < rsCols; j++) {
                    cell = row.getCell(j) == null ? "" : row.getCell(j).toString();
                    if ("".equalsIgnoreCase(cell.trim())) {
                        nullCellNum++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (nullCellNum >= rsCols) {
                afterRows--;
            }
        }
        return afterRows;
    }

    /**
     * 返回Excel2007去掉空行的记录数(xlsx)
     */
    public static int getRightRows(XSSFSheet sheet) {
        int rsRows = sheet.getPhysicalNumberOfRows();
        int nullCellNum;
        int afterRows = rsRows;
        XSSFRow row;
        String cell;
        for (int i = sheet.getFirstRowNum(); i < rsRows; i++) {
            row = sheet.getRow(i);
            nullCellNum = 0;
            if (row == null) {
                afterRows--;
                continue;
            }
            int rsCols = row.getPhysicalNumberOfCells();
            for (int j = 0; j < rsCols; j++) {
                cell = row.getCell(j) == null ? "" : row.getCell(j).toString();
                if ("".equals(cell.trim())) {
                    nullCellNum++;
                }
            }
            if (nullCellNum >= rsCols) {
                afterRows--;
            }
        }
        return afterRows;
    }

    /**
     * excel2003单元格内容读取
     */
    @SuppressWarnings("deprecation")
    public static String getStringValue(HSSFCell cell, String defaultValue) {
        String strReturn = defaultValue;
        try {
            if (cell == null)
                return defaultValue;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    strReturn = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    try {
                        boolean isDate = HSSFDateUtil.isCellDateFormatted(cell);
                        if (isDate) {
                            if (strReturn == null) {
                                strReturn = cell.getDateCellValue().toLocaleString();
                            }
                        } else {
                            double d = cell.getNumericCellValue();
                            strReturn = keepScore(d);
                        }
                    } catch (Exception e) {
                        double d = cell.getNumericCellValue();
                        strReturn = keepScore(d);
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    strReturn = String.valueOf(cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    strReturn = defaultValue;
                    break;
                default:
                    strReturn = defaultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            strReturn = defaultValue;
        }
        return strReturn;
    }

    /**
     * excel2007单元格内容读取
     */
    @SuppressWarnings("deprecation")
    public static String getStringValue(XSSFCell cell, String defaultValue) {
        String strReturn = defaultValue;
        try {
            if (cell == null)
                return defaultValue;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    strReturn = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    boolean isDate = HSSFDateUtil.isCellDateFormatted(cell);
                    if (isDate) {
                        if (strReturn == null) {
                            strReturn = cell.getDateCellValue().toLocaleString();
                        }
                    } else {
                        double d = cell.getNumericCellValue();
                        strReturn = keepScore(d);
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    strReturn = String.valueOf(cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    strReturn = defaultValue;
                    break;
                default:
                    strReturn = defaultValue;

            }
        } catch (Exception e) {
            e.printStackTrace();
            strReturn = defaultValue;
        }
        return strReturn;
    }


    /**
     * 获取excel2003标题行列名组成的list
     */
    public static List<String> getColNames(HSSFRow row) {
        int cols = row.getPhysicalNumberOfCells();
        List<String> colNames = new ArrayList<>();

        for (int i = 0; i < cols; i++) {
            String colName = getStringValue(row.getCell(i), "");
            if (colName != null && !colName.equals(""))
                colNames.add(colName.trim().toLowerCase());
        }

        return colNames;
    }

    /**
     * 获取excel2003标题行列名组成的list
     */
    public static Map<Integer, String> getMapColNames(HSSFRow row) {
        int cols = row.getPhysicalNumberOfCells();
        Map<Integer, String> colNames = new HashMap<>();
        for (int i = 0; i < cols; i++) {
            String colName = getStringValue(row.getCell(i), "");
            if (colName.equals("")) {
                cols++;
            }
            if (colName != null && !colName.equals("")) {
                colNames.put(i, colName.trim());
            }
        }
        return colNames;
    }

    /**
     * 获取excel2007标题行列名组成的list
     */
    public static List<String> getColNames(XSSFRow row) {
        int cols = row.getPhysicalNumberOfCells();
        List<String> colNames = new ArrayList<>();
        for (int i = 0; i < cols; i++) {
            String colName = getStringValue(row.getCell(i), "");
            if (colName != null && !colName.equals(""))
                colNames.add(colName.trim().toLowerCase());
        }
        return colNames;
    }

    /**
     * 获取excel2007标题行列名组成的list
     */
    public static Map<Integer, String> getMapColNames(XSSFRow row) {
        int cols = row.getPhysicalNumberOfCells();
        Map<Integer, String> colNames = new HashMap<>();
        for (int i = 0; i < cols; i++) {
            String colName = getStringValue(row.getCell(i), "");
            if (colName.equals("")) {
                cols++;
            }
            if (colName != null && !colName.equals(""))
                colNames.put(i, colName.trim());
        }
        return colNames;
    }

    /**
     * 获取txt标题行列名组成的list(默认第一个非空行为标题行)
     */
    public static List<String> getColNames(String fileName, String separator) {
        BufferedReader in = null;
        try {
            List<String> colNames = new ArrayList<String>();
            in = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = in.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    for (String str : line.split(separator)) {
                        colNames.add(str.trim());
                    }
                    return colNames;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取txt标题行列名组成的list(默认第一个非空行为标题行)
     */
    public static Map<Integer, String> getMapColNames(String fileName, String separator) {
        BufferedReader in = null;
        try {
            Map<Integer, String> colNames = new HashMap<Integer, String>();
            in = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = in.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    for (int i = 0; i < line.split(separator).length; i++) {
                        String str = line.split(separator)[i];
                        colNames.put(i, str.trim());
                    }
                    return colNames;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Map<Integer, String> getMapColNames(InputStream fis, String separator) {
        BufferedReader in = null;
        try {
            Map<Integer, String> colNames = new HashMap<>();
            in = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = in.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    for (int i = 0; i < line.split(separator).length; i++) {
                        String str = line.split(separator)[i];
                        colNames.put(i, str.trim());
                    }
                    return colNames;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取txt标题行列名组成的list(默认第一个非空行为标题行)
     */
    public static List<String> getColNames(InputStream is, boolean firstHeader, String separator) {
        BufferedReader in = null;
        try {
            List<String> colNames = new ArrayList<String>();
            in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean isRun = true;
            while (isRun) {
                line = in.readLine();
                if (line == null) {
                    continue;
                }
                isRun = false;
                if (!firstHeader) {
                    line = in.readLine();
                }
            }
            if (!"".equals(line.trim())) {
                for (int i = 0; i < line.split(separator).length; i++) {
                    String str = line.split(separator)[i];
                    colNames.add(str.trim());
                }
                return colNames;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取txt标题行列名组成的list(默认第一个非空行为标题行)
     */
    public static Map<Integer, String> getMapColNames(InputStream is, boolean firstHeader, String separator) {
        BufferedReader in = null;
        try {
            Map<Integer, String> colNames = new HashMap<Integer, String>();
            in = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean isRun = true;
            while (isRun) {
                line = in.readLine();
                if (line == null) {
                    continue;
                }
                isRun = false;
                if (!firstHeader) {
                    line = in.readLine();
                }
            }
            if (!"".equals(line.trim())) {
                for (int i = 0; i < line.split(separator).length; i++) {
                    String str = line.split(separator)[i];
                    colNames.put(i, str.trim());
                }
                return colNames;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取txt内容组成的list(默认第一个非空行为标题行)
     */
    public static List<String[]> getTxtContents(String fileName, String separator) {
        BufferedReader in = null;
        try {
            List<String[]> colNames = new ArrayList<>();
            in = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = in.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    colNames.add(line.split(separator));
                }
            }
            colNames.remove(0);
            return colNames;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<String[]> getTxtContents(InputStream fis, String separator) {
        BufferedReader in = null;
        try {
            List<String[]> colNames = new ArrayList<>();
            in = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = in.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    colNames.add(line.split(separator));
                }
            }
            colNames.remove(0);
            return colNames;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 返回xml文件对应的内容
     */
    public static Document getXmlDoc(File file) {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(file);
        } catch (Exception e) {
            InputStream in = null;
            InputStreamReader strInStream = null;
            try {
                in = new FileInputStream(file);
                strInStream = new InputStreamReader(in, "GBK");
                document = reader.read(strInStream);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (strInStream != null) strInStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    if (in != null) in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
        return document;
    }

    /**
     * 返回xml文件对应的内容
     */
    public static Document getXmlDoc(InputStream fis) {
        Document document = null;
        SAXReader reader = new SAXReader();
        try {
            BufferedReader input = null;
            PushbackInputStream pis = new PushbackInputStream(fis, 1024);
            String bomEncoding = getBOMEncoding(pis);
            if (bomEncoding == null) {
                input = new BufferedReader(new InputStreamReader(pis, "UTF-8"));
            } else {
                input = new BufferedReader(new InputStreamReader(pis,
                        bomEncoding));
            }
            document = reader.read(input);
        } catch (Exception e) {
            try {
                InputStreamReader strInStream = new InputStreamReader(fis, "GBK");
                document = reader.read(strInStream);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return document;
    }

    /**
     * 获取xml最后一级节点名组成的list
     */
    public static List<String> getLastLevelNodeNames(Element element0) {
        List<String> nodeNames = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        List<Element> elements = element0.elements();
        for (Element ele : elements) {
            nodeNames.add(ele.getName());
        }
        return nodeNames;
    }

    private static String getBOMEncoding(PushbackInputStream is) throws IOException {
        String encoding = null;
        int[] bytes = new int[3];
        bytes[0] = is.read();
        bytes[1] = is.read();
        bytes[2] = is.read();
        if (bytes[0] == 0xFE && bytes[1] == 0xFF) {
            encoding = "UTF-16BE";
            is.unread(bytes[2]);
        } else if (bytes[0] == 0xFF && bytes[1] == 0xFE) {
            encoding = "UTF-16LE";
            is.unread(bytes[2]);
        } else if (bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF) {
            encoding = "UTF-8";
        } else {
            for (int i = bytes.length - 1; i >= 0; i--) {
                is.unread(bytes[i]);
            }
        }
        return encoding;
    }

    private static String keepScore(double d) {
        String result;
        if (d > 0) {
            if (d - (int) d < Double.MIN_VALUE) {
                result = Integer.toString((int) d);
            } else {
                DecimalFormat df = new DecimalFormat("#.###########");
                result = df.format(d);
            }
        } else {
            if ((int) d - d < Double.MIN_VALUE) {
                result = Integer.toString((int) d);
            } else {
                DecimalFormat df = new DecimalFormat("#.###########");
                result = df.format(d);
            }
        }
        return result;
    }

}