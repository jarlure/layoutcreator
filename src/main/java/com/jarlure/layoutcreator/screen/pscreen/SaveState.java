package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.psd.PSDFileEditor;
import com.jarlure.layoutcreator.util.psd.PSDFileReader;
import com.jarlure.layoutcreator.util.txt.XmlFileCreateRecord;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.Entity;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.bean.LayoutData;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.Operation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.util.SavableHelper;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.*;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class SaveState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private UIComponent saveTipDialog;
    private UIComponent saveTipDialogCloseButton;
    private VaryUIComponent saveTipDialogYesButton=new VaryUIComponent();
    private VaryUIComponent saveTipDialogNoButton=new VaryUIComponent();

    public SaveState(){
        operations.add(new SaveOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);

        saveTipDialog=layout.getComponent(PSLayout.SAVE_TIP_DIALOG);
        saveTipDialogCloseButton=layout.getComponent(PSLayout.SAVE_TIP_DIALOG_CLOSE_BUTTON);
        saveTipDialogYesButton.setValue(layout.getComponent(PSLayout.SAVE_TIP_DIALOG_YES_BUTTON));
        saveTipDialogNoButton.setValue(layout.getComponent(PSLayout.SAVE_TIP_DIALOG_NO_BUTTON));
    }

    public void save(){
        EntityId id = ed.findEntity(null, PsdFile.class);
        if (id==null)return;
        PsdFile psdFile = ed.getComponent(id,PsdFile.class);
        XmlFile xmlFile = ed.getComponent(id,XmlFile.class);
        J3oFile j3oFile = ed.getComponent(id,J3oFile.class);
        saveToPsd(psdFile.getFile());
        saveToXml(xmlFile.getFile());
        saveToJ3o(j3oFile.getFile());
    }

    public void saveToPsd(File psdFile){
        Set<EntityId> idSet = ed.findEntities(null,Layer.class,Index.class,Name.class);
        if (idSet.isEmpty())return;
        String[] nameArray = new String[idSet.size()];
        for (EntityId id:idSet){
            int index = ed.getComponent(id,Index.class).getIndex();
            String name = ed.getComponent(id,Name.class).getName();
            nameArray[index]=name;
        }

        PSDFileReader.read(psdFile, ed);
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()), PSDData.class);
        PSDFileEditor.renameLayer(psdFile,nameArray,psdData);
    }

    public void saveToXml(File xmlFile){
        EntityId[] componentIdList;{
            Set<EntityId> componentSet = ed.findEntities(null,Component.class,Index.class);
            componentIdList=new EntityId[componentSet.size()];
            componentSet.forEach(entityId -> {
                int index = ed.getComponent(entityId,Index.class).getIndex();
                componentIdList[index]=entityId;
            });
        }
        Entity data = new Entity();
        for (EntityId id:componentIdList){
            String componentType = ed.getComponent(id,Type.class).getType();
            String componentName = ed.getComponent(id,Name.class).getName();
            String componentLink;{
                ComponentLink link = ed.getComponent(id,ComponentLink.class);
                if (link==null) componentLink=null;
                else componentLink=link.getLink();
            }

            Entity componentImg = new Entity();
            Entity componentChild = new Entity();
            Set<EntityId> idSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",id),Parent.class);
            if (!idSet.isEmpty()){
                boolean isImg = null != ed.getComponent(idSet.stream().findFirst().get(),Img.class);
                if (isImg){
                    idSet.forEach(entityId -> {
                        int index = ed.getComponent(entityId,Index.class).getIndex();
                        EntityId posId = ed.getComponent(entityId,ImgPos.class).getId();
                        EntityId urlId = ed.getComponent(entityId,ImgUrl.class).getUrl();
                        String imgName = ed.getComponent(posId,Name.class).getName();
                        String imgURL ;{
                            if (posId==urlId) imgURL=null;
                            else if (urlId==null) imgURL="";
                            else imgURL=ed.getComponent(urlId,Name.class).getName();
                        }
                        componentImg.setItem(index,new EnumMap<>(ComponentConfigureXMLFileEditor.Img.class),
                                ComponentConfigureXMLFileEditor.Img.Name,imgName,
                                ComponentConfigureXMLFileEditor.Img.URL,imgURL);
                    });
                }else{
                    idSet.forEach(entityId -> {
                        int index = ed.getComponent(entityId,Index.class).getIndex();
                        String childName = ed.getComponent(entityId,Name.class).getName();
                        componentChild.setItem(index,new EnumMap<>(ComponentConfigureXMLFileEditor.Child.class),
                                ComponentConfigureXMLFileEditor.Child.Name,childName);
                    });
                }
            }

            data.addItem(new EnumMap<>(ComponentConfigureXMLFileEditor.Component.class),
                    ComponentConfigureXMLFileEditor.Component.Type, componentType,
                    ComponentConfigureXMLFileEditor.Component.Name, componentName,
                    ComponentConfigureXMLFileEditor.Component.Link, componentLink,
                    ComponentConfigureXMLFileEditor.Component.Img, componentImg,
                    ComponentConfigureXMLFileEditor.Component.Child, componentChild);
        }

        if (data.isEmpty())return;
        if (!XmlFileCreateRecord.existRecord(xmlFile)) XmlFileCreateRecord.addRecord(xmlFile);
        ComponentConfigureXMLFileEditor.writeComponentConfigure(xmlFile,data);
    }

    public void saveToJ3o(File j3oFile){
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()),PSDData.class);
        int layoutHeight = psdData.get(0).hashCode();
        int layoutWidth = psdData.get(1).hashCode();
        List<LayerImageData> dataList = new ArrayList<>();
        ed.findEntities(null,Img.class).forEach(entityId -> {
            EntityId posId = ed.getComponent(entityId,ImgPos.class).getId();
            EntityId urlId = ed.getComponent(entityId,ImgUrl.class).getUrl();
            if (posId!=null){
                LayerImageData data = ed.getComponent(posId,LayerImgData.class).getLayerImageData();
                if (!dataList.contains(data)) dataList.add(data);
            }
            if (urlId!=null){
                LayerImageData data = ed.getComponent(urlId,LayerImgData.class).getLayerImageData();
                if (!dataList.contains(data)) dataList.add(data);
            }
        });
        if (dataList.isEmpty())return;
        for (int i=dataList.size()-1;i>=0;i--){
            LayerImageData data = dataList.get(i);
            data=new LayerImageData(data);
            data.setName(null);//不需要PS中的图层名
            dataList.set(i,data);
        }
        LayoutData layoutData = new LayoutData();
        layoutData.setLayoutWidth(layoutWidth);
        layoutData.setLayoutHeight(layoutHeight);
        layoutData.setImgList(dataList);

        SavableHelper.saveAsJ3OData(j3oFile,layoutData);
    }

    private class SaveOperation implements Operation {

        private EntitySet saveAskSet;
        private MouseInputListener listener = new MouseInputListener() {

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
                if (selectConverter.isSelect(saveTipDialogYesButton,mouse)){
                    pressed=saveTipDialogYesButton;
                    saveTipDialogYesButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
                if (selectConverter.isSelect(saveTipDialogNoButton,mouse)){
                    pressed=saveTipDialogNoButton;
                    saveTipDialogNoButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
                if (selectConverter.isSelect(saveTipDialogCloseButton,mouse)){
                    pressed=saveTipDialogCloseButton;
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (pressed==null)return;
                if (selectConverter.isSelect(pressed,mouse)){
                    pressed.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                    if (pressed==saveTipDialogCloseButton){
                        saveAskSet.getEntityIds().forEach(entityId -> ed.removeEntity(entityId));
                    }else if (pressed==saveTipDialogYesButton){
                        save();
                        saveAskSet.getEntityIds().forEach(entityId -> {
                            ed.getComponent(entityId, SaveTip.class).getCallback().onDone(true);
                            ed.removeEntity(entityId);
                        });
                    }else if (pressed==saveTipDialogNoButton){
                        saveAskSet.getEntityIds().forEach(entityId -> {
                            ed.getComponent(entityId,SaveTip.class).getCallback().onDone(false);
                            ed.removeEntity(entityId);
                        });
                    }
                    pressed=null;
                }
            }
        };

        @Override
        public void initialize() {
            saveAskSet=ed.getEntities(SaveTip.class);
        }

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
            if (saveAskSet.applyChanges()){
                if (!saveAskSet.getRemovedEntities().isEmpty()){
                    saveTipDialog.setVisible(false);
                }
                if (!saveAskSet.getAddedEntities().isEmpty()){
                    saveTipDialog.setVisible(true);
                }
            }
        }

    }

}