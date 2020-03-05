package com.jarlure.layoutcreator.state;

import com.jarlure.layoutcreator.entitycomponent.event.TipSave;
import com.jarlure.layoutcreator.entitycomponent.event.Close;
import com.jarlure.layoutcreator.entitycomponent.event.Save;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.entitycomponent.mark.Modified;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.lambda.VoidFunction;
import com.jarlure.project.state.EntityDataState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

public class CloseState extends BaseAppState {

    private EntityData ed;
    private EntitySet closeSet;

    @Override
    protected void initialize(Application app) {
        ed=app.getStateManager().getState(EntityDataState.class).getEntityData();
        closeSet=ed.getEntities(Close.class);
    }

    @Override
    protected void cleanup(Application app) {
        closeSet.release();
        closeSet=null;
        ed=null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        closeSet.applyChanges();
        if (closeSet.isEmpty())return;
        closeSet.forEach(entity -> {
            Close close = entity.get(Close.class);
            EntityId importedId = close.getImportedId();
            VoidFunction superCallback = close.getCallback();
            VoidFunction callback = ()->{
                ed.removeComponent(importedId, Imported.class);
                ed.setComponent(importedId,new Decay());
                if (superCallback != null) superCallback.apply();
            };
            if (null!=ed.getComponent(importedId, Modified.class)){
                ed.setComponent(ed.createEntity(),new TipSave(yes->{
                    if (yes) ed.setComponent(ed.createEntity(),new Save(importedId,callback));
                    else callback.apply();
                }));
            }else{
                callback.apply();
            }
        });
        closeSet.forEach(entity -> ed.removeEntity(entity.getId()));
    }
}
