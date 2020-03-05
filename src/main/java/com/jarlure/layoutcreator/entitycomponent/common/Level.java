package com.jarlure.layoutcreator.entitycomponent.common;

import com.simsilica.es.EntityComponent;

public class Level implements EntityComponent {

    private int level;

    public Level(int level){
        this.level=level;
    }

    public int getLevel() {
        return level;
    }

}
