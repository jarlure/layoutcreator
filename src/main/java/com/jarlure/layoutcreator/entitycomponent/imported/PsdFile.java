package com.jarlure.layoutcreator.entitycomponent.imported;

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
