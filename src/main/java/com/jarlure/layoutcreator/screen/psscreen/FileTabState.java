package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.event.Close;
import com.jarlure.layoutcreator.entitycomponent.mark.Current;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.entitycomponent.mark.Modified;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.SortHelper;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.NinePatchEffect;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.property.*;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.util.ImageHandler;
import com.simsilica.es.*;

import java.util.HashMap;
import java.util.Map;

public class FileTabState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private UIComponent fileTabBackground;
    private UIComponent fileTabCloseButton;
    private UIComponent fileTabPanel;
    private float closeButtonRightToBackgroundRight;
    private Map<EntityId,UIComponent> fileTabMap;

    public FileTabState(){
        operations.add(new ShowFileTabOperation());
        operations.add(new SwitchImportedOperation());
        operations.add(new CloseImportedOperation());
    }

    @Override
    protected void initialize() {
        ed=getScreen().getState(EntityDataState.class).getEntityData();
        fileTabMap=new HashMap<>();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        fileTabBackground=layout.getComponent(PSLayout.FILE_TAB_BACKGROUND);
        fileTabCloseButton=layout.getComponent(PSLayout.FILE_TAB_CLOSE_BUTTON);
        fileTabPanel=layout.getComponent(PSLayout.FILE_TAB_PANEL);

        closeButtonRightToBackgroundRight = fileTabCloseButton.get(AABB.class).getXRight()-fileTabBackground.get(AABB.class).getXRight();
    }

    private class ShowFileTabOperation extends AbstractOperation {

        private EntitySet importedSet;
        private EntitySet currentImportedSet;
        private EntitySet importedModifiedSet;

        @Override
        public void initialize() {
            importedSet=ed.getEntities(Imported.class);
            currentImportedSet=ed.getEntities(Current.class,Imported.class);
            importedModifiedSet=ed.getEntities(Imported.class, Modified.class);
        }

        @Override
        public void cleanup() {
            importedModifiedSet.release();
            importedModifiedSet=null;
            currentImportedSet.release();
            currentImportedSet=null;
            importedSet.release();
            importedSet=null;
        }

        @Override
        public void update(float tpf) {
            if (importedSet.applyChanges()){
                importedSet.getRemovedEntities().forEach(entity -> {
                    UIComponent item = fileTabMap.remove(entity.getId());
                    if (item!=null) item.get(ParentProperty.class).detachFromParent();
                });
                importedSet.getAddedEntities().forEach(entity -> {
                    UIComponent tab = createTab();
                    String name = ed.getComponent(entity.getId(),Name.class).getName();
                    updateTabText(tab,name);
                    fileTabPanel.get(ElementProperty.class).add(tab);
                    fileTabMap.put(entity.getId(),tab);
                });
                updateTabPosition();
            }
            if (currentImportedSet.applyChanges()){
                currentImportedSet.getRemovedEntities().forEach(entity -> {
                    UIComponent tab = fileTabMap.get(entity.getId());
                    if (tab!=null) {
                        UIComponent background =tab.get(ChildrenProperty.class).getChildByName(PSLayout.FILE_TAB_BACKGROUND);
                        background.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                    }
                });
                currentImportedSet.getAddedEntities().forEach(entity -> {
                    UIComponent tab = fileTabMap.get(entity.getId());
                    if (tab!=null) {
                        UIComponent background =tab.get(ChildrenProperty.class).getChildByName(PSLayout.FILE_TAB_BACKGROUND);
                        background.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_PRESSED);
                    }
                });
            }
            if (importedModifiedSet.applyChanges()){
                importedModifiedSet.getRemovedEntities().forEach(entity -> {
                    UIComponent tab = fileTabMap.get(entity.getId());
                    if (tab==null)return;
                    String text = tab.get(TextProperty.class).getText();
                    if (text==null || text.isEmpty())return;
                    if ('*'==text.charAt(text.length()-1)){
                        updateTabText(tab,text.substring(0,text.length()-1));
                    }
                });
                importedModifiedSet.getAddedEntities().forEach(entity -> {
                    UIComponent tab = fileTabMap.get(entity.getId());
                    if (tab==null)return;
                    String text = tab.get(TextProperty.class).getText();
                    if (text==null || text.isEmpty()|| '*'!=text.charAt(text.length()-1)){
                        updateTabText(tab,text+"*");
                    }
                });
                if (!importedModifiedSet.getRemovedEntities().isEmpty()||!importedModifiedSet.getAddedEntities().isEmpty()){
                    updateTabPosition();
                }
            }
        }

        private UIComponent createTab(){
            UIComponent background=fileTabBackground.get(UIFactory.class).create();
            UIComponent closeButton=fileTabCloseButton.get(UIFactory.class).create();

            FontProperty fontProperty =fileTabBackground.get(FontProperty.class);
            background.set(FontProperty.class,fontProperty);

            background.setVisible(true);
            closeButton.setVisible(true);

            UIComponent result = new UINode("fileTab");
            result.get(ChildrenProperty.class).attachChild(background,closeButton);

            for (Class clazz:new Class[]{FontProperty.class,TextProperty.class,NinePatchEffect.class}){
                result.set(clazz,background.get(clazz));
            }

            return result;
        }

        private void updateTabText(UIComponent tab,String text){
            ChildrenProperty childrenProperty = tab.get(ChildrenProperty.class);
            UIComponent background = childrenProperty.getChildByName(PSLayout.FILE_TAB_BACKGROUND);
            UIComponent closeButton = childrenProperty.getChildByName(PSLayout.FILE_TAB_CLOSE_BUTTON);
            FontProperty fontProperty = background.get(FontProperty.class);
            float textWidth = ImageHandler.measureText(fontProperty.getFont(),text);
            float width = fileTabBackground.get(NinePatchEffect.class).getMinPixelWidth()+textWidth;
            AABB backgroundBox = background.get(AABB.class);
            float xLeft = backgroundBox.getXLeft();
            backgroundBox.setWidth(width);
            tab.get(TextProperty.class).setText(text);
            background.move(xLeft-backgroundBox.getXLeft(),0);
            closeButton.move(backgroundBox.getXRight()-closeButton.get(AABB.class).getXRight()+closeButtonRightToBackgroundRight,0);
        }

        private void updateTabPosition(){
            if (fileTabMap.isEmpty())return;
            EntityId[] importedIdArray = SortHelper.sortByEntityId(fileTabMap.keySet(),ed);
            UIComponent lastBackground=fileTabBackground;
            for (EntityId importedId:importedIdArray){
                UIComponent tab = fileTabMap.get(importedId);
                ChildrenProperty childrenProperty = tab.get(ChildrenProperty.class);
                UIComponent background = childrenProperty.getChildByName(PSLayout.FILE_TAB_BACKGROUND);
                float dx = lastBackground.get(AABB.class).getXLeft()-background.get(AABB.class).getXLeft();
                background.move(dx,0);
                childrenProperty.getChildByName(PSLayout.FILE_TAB_CLOSE_BUTTON).move(dx,0);
                lastBackground=background;
            }
        }

    }

    private class SwitchImportedOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter(){

            private UIComponent fileTab;
            private boolean isCurrentImported;

            @Override
            public void onMove(MouseEvent mouse) {
                if (fileTab!=null){
                    if (selectConverter.isSelect(fileTab,mouse))return;
                    if (!isCurrentImported) fileTab.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                    fileTab=null;
                }
                if (selectConverter.isSelect(fileTabPanel,mouse)){
                    if (selectConverter.isSelect(PSLayout.FILE_TAB_BACKGROUND,mouse)){
                        for (UIComponent tab:fileTabPanel.get(ElementProperty.class).value){
                            tab = tab.get(ChildrenProperty.class).getChildByName(PSLayout.FILE_TAB_BACKGROUND);
                            if (tab.get(AABB.class).contains(mouse.x,mouse.y)){
                                fileTab=tab;
                                SwitchEffect switchEffect = tab.get(SwitchEffect.class);
                                isCurrentImported = PSLayout.BUTTON_STATE_PRESSED==switchEffect.getIndexOfCurrentImage();
                                if (!isCurrentImported) switchEffect.switchTo(PSLayout.BUTTON_STATE_MOVE_ON);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (fileTab==null)return;
                if (isCurrentImported)return;
                if (selectConverter.isSelect(fileTab,mouse)){
                    EntityId currentImportedId = ed.findEntity(null,Current.class,Imported.class);
                    if (currentImportedId!=null) ed.removeComponent(currentImportedId,Current.class);
                    for (Map.Entry<EntityId,UIComponent> entry:fileTabMap.entrySet()){
                        if (selectConverter.isSelect(entry.getValue(),mouse)){
                            currentImportedId=entry.getKey();
                            break;
                        }
                    }
                    ed.setComponent(currentImportedId,new Current());
                    isCurrentImported=true;
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

    private class CloseImportedOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter(){

            private UIComponent closeButton;
            private int state;
            private EntityId key;

            @Override
            public void onMove(MouseEvent mouse) {
                if (closeButton!=null){
                    if (selectConverter.isSelect(closeButton,mouse))return;
                    closeButton.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                    closeButton=null;
                    key=null;
                }
                if (selectConverter.isSelect(PSLayout.FILE_TAB_CLOSE_BUTTON,mouse)){
                    for (Map.Entry<EntityId,UIComponent> entry:fileTabMap.entrySet()){
                        if (entry.getValue().get(AABB.class).contains(mouse.x,mouse.y)){
                            key=entry.getKey();
                            closeButton = entry.getValue().get(ChildrenProperty.class).getChildByName(PSLayout.FILE_TAB_CLOSE_BUTTON);
                            state=PSLayout.BUTTON_STATE_MOVE_ON;
                            closeButton.get(SwitchEffect.class).switchTo(state);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (closeButton==null)return;
                if (state==PSLayout.BUTTON_STATE_PRESSED)return;
                state=PSLayout.BUTTON_STATE_PRESSED;
                closeButton.get(SwitchEffect.class).switchTo(state);
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (closeButton==null)return;
                if (state==PSLayout.BUTTON_STATE_MOVE_ON)return;
                state=PSLayout.BUTTON_STATE_MOVE_ON;
                closeButton.get(SwitchEffect.class).switchTo(state);
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (closeButton==null)return;
                if (key==null)return;
                if (selectConverter.isSelect(closeButton,mouse)){
                    ed.setComponent(ed.createEntity(),new Close(key));
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
