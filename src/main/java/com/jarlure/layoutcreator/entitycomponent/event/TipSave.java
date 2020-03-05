package com.jarlure.layoutcreator.entitycomponent.event;

import com.jarlure.project.lambda.VoidFunction1Boolean;
import com.simsilica.es.EntityComponent;

public class TipSave implements EntityComponent {

    private VoidFunction1Boolean callback;

    public TipSave(VoidFunction1Boolean callback){
        this.callback=callback;
    }

    public VoidFunction1Boolean getCallback() {
        return callback;
    }

}
