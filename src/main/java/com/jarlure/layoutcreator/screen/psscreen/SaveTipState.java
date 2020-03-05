package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.event.TipSave;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

public class SaveTipState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private UIComponent saveTipDialog;
    private UIComponent closeButton;
    private UIComponent yesButton;
    private UIComponent noButton;

    public SaveTipState(){
        operations.add(new ShowDialogOperation());
        operations.add(new PressButtonOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        noButton=null;
        yesButton=null;
        closeButton=null;
        saveTipDialog=null;
        selectConverter=null;
        ed=null;
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);
        saveTipDialog = layout.getComponent(PSLayout.SAVE_TIP_DIALOG);
        closeButton = layout.getComponent(PSLayout.SAVE_TIP_DIALOG_CLOSE_BUTTON);
        yesButton = layout.getComponent(PSLayout.SAVE_TIP_DIALOG_YES_BUTTON);
        noButton = layout.getComponent(PSLayout.SAVE_TIP_DIALOG_NO_BUTTON);
    }

    private class ShowDialogOperation extends AbstractOperation {

        private EntitySet tipSaveSet;

        @Override
        public void initialize() {
            tipSaveSet =ed.getEntities(TipSave.class);
        }

        @Override
        public void cleanup() {
            tipSaveSet.release();
            tipSaveSet =null;
        }

        @Override
        public void update(float tpf) {
            if (tipSaveSet.applyChanges()){
                saveTipDialog.setVisible(!tipSaveSet.isEmpty());
            }
        }

    }

    private class PressButtonOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private UIComponent pressed;

            @Override
            public void onMove(MouseEvent mouse) {
                if (pressed==null)return;
                if (selectConverter.isSelect(pressed,mouse)){
                    pressed.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                    pressed=null;
                }
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(yesButton,mouse)){
                    pressed=yesButton;
                    yesButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
                if (selectConverter.isSelect(noButton,mouse)){
                    pressed=noButton;
                    noButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
                if (selectConverter.isSelect(closeButton,mouse)){
                    pressed=closeButton;
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (pressed==null)return;
                if (selectConverter.isSelect(pressed,mouse)){
                    pressed.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                    EntityId id = ed.findEntity(null, TipSave.class);
                    if (id==null)return;
                    TipSave tip = ed.getComponent(id, TipSave.class);
                    if (pressed==yesButton){
                        tip.getCallback().apply(true);
                    }else if (pressed==noButton){
                        tip.getCallback().apply(false);
                    }
                    ed.findEntities(null, TipSave.class).forEach(entityId -> ed.removeEntity(entityId));
                    pressed=null;
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

}
