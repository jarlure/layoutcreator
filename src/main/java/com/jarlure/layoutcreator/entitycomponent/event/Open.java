package com.jarlure.layoutcreator.entitycomponent.event;

import com.simsilica.es.EntityComponent;

import java.io.File;

public class Open implements EntityComponent {

    private File file;

    public Open(File file){
        this.file=file;
    }

    public File getFile() {
        return file;
    }

}
