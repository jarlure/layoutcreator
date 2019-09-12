package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

public class ImgUrl implements EntityComponent {

    private EntityId url;

    public ImgUrl(EntityId url){
        this.url=url;
    }

    public EntityId getUrl() {
        return url;
    }

}
