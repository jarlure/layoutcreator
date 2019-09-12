package com.jarlure.layoutcreator.bean;

import com.jarlure.project.bean.commoninterface.Callback;
import com.simsilica.es.EntityComponent;

public class SaveTip implements EntityComponent {

    private Callback<Boolean> callback;

    public SaveTip(Callback<Boolean> callback){
        this.callback=callback;
    }

    public Callback<Boolean> getCallback() {
        return callback;
    }

}
