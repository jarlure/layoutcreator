package com.jarlure.layoutcreator.entitycomponent.xml;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

public class ImgPos implements EntityComponent {

    private EntityId id;

    public ImgPos(EntityId id){
        this.id=id;
    }

    public EntityId getId() {
        return id;
    }

}
