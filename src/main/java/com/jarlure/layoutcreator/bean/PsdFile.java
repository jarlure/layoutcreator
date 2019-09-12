package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;

import java.io.File;

public class PsdFile implements EntityComponent {

    private File file;

    public PsdFile(){
    }

    public PsdFile(File file){
        this.file=file;
    }

    public File getFile() {
        return file;
    }

}
