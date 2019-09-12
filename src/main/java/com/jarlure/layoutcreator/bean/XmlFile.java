package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;

import java.io.File;

public class XmlFile implements EntityComponent {

    private File file;

    public XmlFile(){
    }

    public XmlFile(File file){
        this.file=file;
    }

    public File getFile() {
        return file;
    }

}
