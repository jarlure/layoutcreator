package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.Bundle;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.AbstractScreen;

import java.awt.Frame;

public class PSScreen extends AbstractScreen {

    private Frame frame;

    public PSScreen(Frame frame){
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }

    @Override
    protected Class<? extends Layout> getLayoutClass() {
        return PSLayout.class;
    }

    @Override
    protected void initialize() {
        screenStates.add(new TestState());
        screenStates.add(new LayerState());
        screenStates.add(new ComponentState());
        screenStates.add(new SaveState());

        screenStates.add(new WindowBarState());
        screenStates.add(new MenuState());
        screenStates.add(new SceneState());
        screenStates.add(new LayerPanelState());
        screenStates.add(new ComponentDialogState());
        screenStates.add(new ComponentPanelState());
        screenStates.add(new ComponentPropertyPanelState());
        screenStates.add(new ImgPropertyPanelState());
        setEnabled(true);
        setVisible(true);
    }

    @Override
    protected Bundle getBundle() {
        return new Bundle();
    }

}
