package com.jarlure.layoutcreator.entrance;

import com.jarlure.layoutcreator.screen.pscreen.PSScreen;
import com.jarlure.project.state.DecayState;
import com.jarlure.project.state.DefaultEntityDataState;
import com.jarlure.project.state.DelayState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;

import java.awt.*;

public class MyApplication extends SimpleApplication {

    private Frame frame;

    public MyApplication(Frame frame){
        super(new UIRenderState(),new DefaultEntityDataState(),new DecayState(),new DelayState(),new RecordState());
        this.frame =frame;
    }

    @Override
    public void simpleInitApp() {
        getViewPort().setBackgroundColor(new ColorRGBA(0.149f,0.149f,0.149f,1));
        AssetManager.initialize(this);
        InputManager.initialize(this);
        stateManager.attach(new PSScreen(frame));
    }

    @Override
    public void start() {
        frame.setVisible(true);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        frame.setVisible(false);
        frame.dispose();
    }

}