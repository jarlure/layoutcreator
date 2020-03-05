package com.jarlure.layoutcreator.entrance;

import com.jarlure.project.bean.entitycomponent.Delay;
import com.jarlure.project.state.EntityDataState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.EntityData;

public class CommandState extends BaseAppState {

    private EntityData ed;

    @Override
    protected void initialize(Application app) {
        ed=app.getStateManager().getState(EntityDataState.class).getEntityData();
        ed.setComponent(ed.createEntity(),new Delay(()-> System.out.println("Hello World!")));
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
    }

}
