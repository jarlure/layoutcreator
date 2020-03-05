package com.jarlure.layoutcreator.state;

import com.jarlure.layoutcreator.entitycomponent.event.Close;
import com.jarlure.layoutcreator.entitycomponent.event.Quit;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.project.lambda.VoidFunction;
import com.jarlure.project.state.EntityDataState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.util.Set;

public class QuitState extends BaseAppState {

    private EntityData ed;
    private EntitySet quitSet;

    @Override
    protected void initialize(Application app) {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        quitSet = ed.getEntities(Quit.class);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        quitSet.applyChanges();
        if (quitSet.isEmpty())return;
        VoidFunction callback=()-> {
            if (null==ed.findEntity(null,Imported.class)){
                getApplication().stop();
            }
        };
        Set<EntityId> importedSet = ed.findEntities(null,Imported.class);
        if (importedSet.isEmpty()){
            callback.apply();
        }else{
            importedSet.forEach(importedId->ed.setComponent(ed.createEntity(),new Close(importedId,callback)));
        }
        quitSet.forEach(entity -> ed.removeEntity(entity.getId()));
    }
}
