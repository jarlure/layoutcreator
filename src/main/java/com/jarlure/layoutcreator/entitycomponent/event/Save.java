package com.jarlure.layoutcreator.entitycomponent.event;

import com.jarlure.project.lambda.VoidFunction;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

import java.io.File;

public class Save implements EntityComponent {

    private EntityId importedId;
    private File psdFile;
    private File xmlFile;
    private File j3oFile;
    private VoidFunction callback;

    public Save(EntityId importedId){
        this(importedId,null);
    }

    public Save(EntityId importedId,File psdFile,File xmlFile,File j3oFile){
        this(importedId,psdFile,xmlFile,j3oFile,null);
    }

    public Save(EntityId importedId,VoidFunction callback){
        this(importedId,null,null,null,callback);
    }

    public Save(EntityId importedId,File psdFile,File xmlFile,File j3oFile,VoidFunction callback){
        this.importedId=importedId;
        this.psdFile=psdFile;
        this.xmlFile=xmlFile;
        this.j3oFile=j3oFile;
        this.callback=callback;
    }

    public EntityId getImportedId() {
        return importedId;
    }

    public File getPsdFile() {
        return psdFile;
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public File getJ3oFile() {
        return j3oFile;
    }

    public VoidFunction getCallback() {
        return callback;
    }

}
