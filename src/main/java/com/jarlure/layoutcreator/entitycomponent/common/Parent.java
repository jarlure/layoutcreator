package com.jarlure.layoutcreator.entitycomponent.common;

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

    @Override
    public boolean equals(Object obj) {
        if (this==obj)return true;
        if (obj instanceof Parent){
            if (parentId == ((Parent) obj).parentId) return true;
            return parentId != null && parentId.equals(((Parent) obj).parentId);
        }
        return false;
    }
}

