package com.jarlure.layoutcreator.entitycomponent.common;

import com.simsilica.es.EntityComponent;

public class Index implements EntityComponent {

    private int index;

    public Index(int index){
        this.index=index;
    }

    public int getIndex() {
        return index;
    }

}
