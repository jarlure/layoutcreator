package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.Item;
import com.jarlure.layoutcreator.bean.Selected;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.screen.screenstate.operation.Operation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.effect.TextEditEffect;
import com.jarlure.ui.effect.TextLineEditEffect;
import com.jarlure.ui.input.*;
import com.jarlure.ui.property.*;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.texture.Image;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.Filters;
import com.simsilica.es.Name;

public class TestState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private PSLayout layout;

    public TestState(){
        operations.add(new PrintOperation());
    }

    @Override
    protected void initialize() {
        ed=getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        this.layout= (PSLayout) layout;
    }

    private class PrintOperation extends AbstractOperation {


        private MouseInputListener mouseListener = new MouseInputAdapter() {

            private boolean pressed;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
            }


            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                pressed=false;
            }
        };
        private KeyInputListener keyListener = new KeyInputAdapter() {
            @Override
            public void onKeyPressed(KeyEvent key) {
                switch (key.getCode()){
                    case KeyInput.KEY_LSHIFT:
//                        EntityId id = ed.findEntity(null,Selected.class);
//                        UIComponent item = ed.getComponent(id,Item.class).getItem();
//                        UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_NAME_TEXT);
//                        Image img = icon.get(ImageProperty.class).getImage();

//                        UIComponent component = layout.getComponent(PSLayout.COMPONENT_NAME_TEXT_EDIT);
                        Image img = layout.getComponent(PSLayout.COMPONENT_NAME_TEXT_EDIT).get(ImageProperty.class).getImage();
                        ImageHandler.saveImage(img,"C:\\Users\\Administrator\\Desktop\\output.png");
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(KeyInputAdapter instance) {
            }
        };

        @Override
        public void onEnable() {
            InputManager.add(mouseListener);
            InputManager.add(keyListener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(mouseListener);
            InputManager.remove(keyListener);
        }

    }

}
