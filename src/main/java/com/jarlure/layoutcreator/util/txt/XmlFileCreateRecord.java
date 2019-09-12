package com.jarlure.layoutcreator.util.txt;

import com.jarlure.project.util.file.txt.TextFileReader;
import com.jarlure.project.util.file.txt.TextFileWriter;

import java.io.File;

public class XmlFileCreateRecord {

    public static File getRecordFile(){
        return new File("src/main/resources/Recent/xmlFileCreateRecord.txt");
    }

    public static File findRecordByName(String name){
        File record = getRecordFile();
        if (!record.exists())return null;
        if (!name.endsWith(".xml")) name+=".xml";
        TextFileReader reader = new TextFileReader(record);
        String path;
        while ((path=reader.readLine())!=null){
            if (path.endsWith(name))break;
        }
        reader.close();
        if (path==null) return null;
        File xmlFile = new File(path);
        if (!xmlFile.exists()) xmlFile=null;
        return xmlFile;
    }

    public static boolean existRecord(File xmlFile){
        File record = getRecordFile();
        if (!record.exists())return false;
        String filePath=xmlFile.getAbsolutePath();
        TextFileReader reader = new TextFileReader(record);
        String path;
        while ((path=reader.readLine())!=null){
            if (!path.equals(filePath))continue;
            reader.close();
            return true;
        }
        reader.close();
        return false;
    }

    public static void addRecord(File xmlFile){
        File record = getRecordFile();
        File tempFile = new File(record.getParent() + "/temp" + record.getName());
        TextFileWriter writer = new TextFileWriter(tempFile);
        if (record.exists()){
            TextFileReader reader = new TextFileReader(record);
            String path;
            while ((path=reader.readLine())!=null){
                writer.writeLine(path);
            }
            reader.close();
            record.delete();
        }
        if (!record.exists()) record.getParentFile().mkdirs();
        writer.writeLine(xmlFile.getAbsolutePath());
        writer.close();
        if (!tempFile.renameTo(record)) throw new IllegalStateException();
    }

}
