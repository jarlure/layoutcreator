package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.common.Selected;
import com.jarlure.layoutcreator.entitycomponent.mark.Component;
import com.jarlure.layoutcreator.entitycomponent.mark.Img;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.ButtonMouseInputListener;
import com.jarlure.ui.input.extend.SpringButtonMouseInputListener;
import com.jarlure.ui.property.SpatialProperty;
import com.jarlure.ui.property.common.PropertyListener;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;

public class ComponentDialogState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private VaryUIComponent dialogSwitchButton = new VaryUIComponent();
    private VaryUIComponent componentRelationDialog = new VaryUIComponent();
    private UIComponent componentPropertyDialog;
    private UIComponent componentPropertyContent;
    private UIComponent imgPropertyContent;

    public ComponentDialogState() {
        //注册开关组件关系面板操作
        operations.add(new ShowRelationDialogOperation());
        //注册显示组件属性面板操作
        operations.add(new ShowPropertyDialogOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);
        dialogSwitchButton.setValue(layout.getComponent(PSLayout.SHOW_COMPONENT_PANEL_DIALOG_BUTTON));
        componentRelationDialog.setValue(layout.getComponent(PSLayout.COMPONENT_DIALOG));
        componentPropertyDialog = layout.getComponent(PSLayout.PROPERTY_DIALOG);
        componentPropertyContent = layout.getComponent(PSLayout.COMPONENT_PROPERTY_CONTENT);
        imgPropertyContent = layout.getComponent(PSLayout.IMG_PROPERTY_CONTENT);
    }

    private class ShowRelationDialogOperation extends AbstractOperation {

        private MouseInputListener listener = new SpringButtonMouseInputListener(dialogSwitchButton) {

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (selectConverter.isSelect(dialogSwitchButton, mouse)) {
                    super.onLeftButtonRelease(mouse);
                    componentRelationDialog.setVisible(state == ButtonMouseInputListener.PRESSED);
                }
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

    private class ShowPropertyDialogOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
        private PropertyListener<UIComponent> setDialogListener = (oldDialog, newDialog) ->{
            //监听组件关系面板
            newDialog.get(SpatialProperty.class).addPropertyListener((property, oldValue, newValue) -> {
                if (property == SpatialProperty.Property.CULL_HINT) {
                    if (componentRelationDialog.isVisible()){//如果组件关系面板处于显示状态
                        //如果有选中的组件，则显示组件属性面板
                        if (!componentSelectedSet.isEmpty() || !imgSelectedSet.isEmpty()){
                            componentPropertyDialog.setVisible(true);
                        }
                    }else componentPropertyDialog.setVisible(false);//否则关闭组件属性面板
                }
            });
        };

        @Override
        public void initialize() {
            componentSelectedSet = ed.getEntities(Component.class, Selected.class);
            imgSelectedSet = ed.getEntities(Img.class, Selected.class);
            componentRelationDialog.addPropertyListener(setDialogListener);
        }

        @Override
        public void cleanup() {
            componentRelationDialog.removePropertyListener(setDialogListener);
            imgSelectedSet.release();
            imgSelectedSet = null;
            componentSelectedSet.release();
            componentSelectedSet = null;
        }

        @Override
        public void update(float tpf) {
            if (componentSelectedSet.applyChanges()) {
                if (!componentSelectedSet.getRemovedEntities().isEmpty()) {
                    if (componentPropertyContent.isVisible()) componentPropertyDialog.setVisible(false);
                }
                if (!componentSelectedSet.getAddedEntities().isEmpty()) {
                    componentPropertyDialog.setVisible(true);
                    imgPropertyContent.setVisible(false);
                    componentPropertyContent.setVisible(true);
                }
            }
            if (imgSelectedSet.applyChanges()) {
                if (!imgSelectedSet.getRemovedEntities().isEmpty()) {
                    if (imgPropertyContent.isVisible()) componentPropertyDialog.setVisible(false);
                }
                if (!imgSelectedSet.getAddedEntities().isEmpty()) {
                    componentPropertyDialog.setVisible(true);
                    componentPropertyContent.setVisible(false);
                    imgPropertyContent.setVisible(true);
                }
            }
        }

    }

}
