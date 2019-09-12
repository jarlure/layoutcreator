package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityId;

public class Parent implements EntityComponent {

    private EntityId parentId;

    public Parent(EntityId parentId){
        this.parentId=parentId;
    }

    public EntityId getParentId() {
        return parentId;
    }

}

