package com.jarlure.layoutcreator.entitycomponent.event;

import com.jarlure.project.lambda.VoidFunction;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

public class Close implements EntityComponent {

    private EntityId importedId;
    private VoidFunction callback;

    public Close(EntityId importedId) {
        this.importedId = importedId;
    }

    public Close(EntityId importedId, VoidFunction callback) {
        this.importedId = importedId;
        this.callback = callback;
    }

    public EntityId getImportedId() {
        return importedId;
    }

    public VoidFunction getCallback() {
        return callback;
    }

}
