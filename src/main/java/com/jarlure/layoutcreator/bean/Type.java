package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;

public class Type implements EntityComponent {

    private String type;

    public Type(String type){
        this.type=type;
    }

    public String getType() {
        return type;
    }

}
