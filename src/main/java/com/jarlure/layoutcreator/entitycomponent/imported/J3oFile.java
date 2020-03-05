package com.jarlure.layoutcreator.entitycomponent.imported;

import com.simsilica.es.EntityComponent;

import java.io.File;

public class J3oFile implements EntityComponent {

    private File file;

    public J3oFile(){
    }

    public J3oFile(File file){
        this.file=file;
    }

    public File getFile() {
        return file;
    }

}
