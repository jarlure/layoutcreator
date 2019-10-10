package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.PsdFile;
import com.jarlure.layoutcreator.bean.SaveTip;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.ButtonMouseInputListener;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.EntityData;

import java.awt.*;

public class WindowBarState extends AbstractScreenState {

    private Frame frame;
    private EntityData ed;
    private SelectConverter selectConverter;
    private UIComponent windowBar;
    private VaryUIComponent minimizeButton=new VaryUIComponent();
    private VaryUIComponent closeButton=new VaryUIComponent();

    public WindowBarState(){
        operations.add(new DragWindowOperation());
        operations.add(new MinimizeWindowOperation());
        operations.add(new CloseWindowOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        frame = ((PSScreen)screen).getFrame();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        windowBar = layout.getComponent(PSLayout.WINDOW_BAR);
        minimizeButton.setValue(layout.getComponent(PSLayout.WINDOW_MINIMIZE_BUTTON));
        closeButton.setValue(layout.getComponent(PSLayout.WINDOW_CLOSE_BUTTON));
    }

    private class DragWindowOperation extends AbstractOperation {

        private boolean pressed;
        private int locationX,locationY;
        private int cursorX,cursorY;

        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(windowBar,mouse)){
                    pressed=true;
                    locationX = frame.getLocation().x;
                    locationY = frame.getLocation().y;
                    cursorX = MouseInfo.getPointerInfo().getLocation().x;
                    cursorY = MouseInfo.getPointerInfo().getLocation().y;
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                pressed=false;
            }
        };

        @Override
        public void onEnable() {
            InputManager.add(listener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(listener);
        }

        @Override
        public void update(float tpf) {
            if (pressed){
                Point cursor = MouseInfo.getPointerInfo().getLocation();
                frame.setLocation(locationX-cursorX+cursor.x,locationY-cursorY+cursor.y);
            }
        }

    }

    private class MinimizeWindowOperation extends AbstractOperation {

        private MouseInputListener listener=new ButtonMouseInputListener(minimizeButton) {

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(minimizeButton,mouse)){
                    frame.setState(Frame.ICONIFIED);
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };

        @Override
        public void onEnable() {
            InputManager.add(listener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(listener);
        }

    }

    private class CloseWindowOperation extends AbstractOperation {

        private MouseInputListener listener=new ButtonMouseInputListener(closeButton) {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                super.onLeftButtonPress(mouse);
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(closeButton,mouse)){
                    if (ed.findEntity(null,PsdFile.class)==null) app.stop();
                    else ed.setComponent(ed.createEntity(),new SaveTip((isSaved, extra) ->app.stop()));
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };

        @Override
        public void onEnable() {
            InputManager.add(listener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(listener);
        }

    }

}