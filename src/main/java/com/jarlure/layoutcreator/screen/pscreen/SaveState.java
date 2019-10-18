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
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.util.SavableHelper;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.texture.Image;
import com.simsilica.es.*;

import java.io.File;
import java.util.*;

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
        EntityId[] componentIdList=Helper.getComponentIdList(ed);
        Entity data = Helper.getComponentConfigureData(componentIdList,ed);

        if (data.isEmpty())return;
        if (!XmlFileCreateRecord.existRecord(xmlFile)) XmlFileCreateRecord.addRecord(xmlFile);
        ComponentConfigureXMLFileEditor.writeComponentConfigure(xmlFile,data);
    }

    public void saveToJ3o(File j3oFile){
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()),PSDData.class);
        int layoutHeight = psdData.get(0).hashCode();
        int layoutWidth = psdData.get(1).hashCode();
        List<LayerImageData> imgList = Helper.getImgList(ed);
        if (imgList.isEmpty())return;
        LayoutData layoutData = new LayoutData();
        layoutData.setLayoutWidth(layoutWidth);
        layoutData.setLayoutHeight(layoutHeight);
        layoutData.setImgList(imgList);

        SavableHelper.saveAsJ3OData(j3oFile,layoutData);
    }

    private class SaveOperation extends AbstractOperation {

        private EntitySet saveAskSet;
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

    private static class Helper{

        public static EntityId[] getComponentIdList(EntityData ed){
            Set<EntityId> componentSet = ed.findEntities(null,Component.class,Index.class);
            EntityId[] componentIdList=new EntityId[componentSet.size()];
            componentSet.forEach(entityId -> {
                int index = ed.getComponent(entityId,Index.class).getIndex();
                componentIdList[index]=entityId;
            });
            return componentIdList;
        }

        public static Entity getComponentConfigureData(EntityId[] componentIdList,EntityData ed){
            Entity data = new Entity();
            for (EntityId id : componentIdList) {
                String componentType = ed.getComponent(id, Type.class).getType();
                String componentName = ed.getComponent(id, Name.class).getName();
                String componentLink;{
                    ComponentLink link = ed.getComponent(id, ComponentLink.class);
                    if (link == null) componentLink = null;
                    else componentLink = link.getLink();
                }

                Entity componentImg = new Entity();
                Entity componentChild = new Entity();
                Set<EntityId> idSet = ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", id), Parent.class);
                if (!idSet.isEmpty()) {
                    boolean isImg = null != ed.getComponent(idSet.stream().findFirst().get(), Img.class);
                    if (isImg) {
                        idSet.forEach(entityId -> {
                            int index = ed.getComponent(entityId, Index.class).getIndex();
                            EntityId posId = ed.getComponent(entityId, ImgPos.class).getId();
                            EntityId urlId = ed.getComponent(entityId, ImgUrl.class).getUrl();
                            String imgName = ed.getComponent(posId, Name.class).getName();
                            String imgURL;{
                                if (posId == urlId) imgURL = null;
                                else if (urlId == null) imgURL = "";
                                else imgURL = ed.getComponent(urlId, Name.class).getName();
                            }
                            componentImg.setItem(index, new EnumMap<>(ComponentConfigureXMLFileEditor.Img.class),
                                    ComponentConfigureXMLFileEditor.Img.Name, imgName,
                                    ComponentConfigureXMLFileEditor.Img.URL, imgURL);
                        });
                    } else {
                        idSet.forEach(entityId -> {
                            int index = ed.getComponent(entityId, Index.class).getIndex();
                            String childName = ed.getComponent(entityId, Name.class).getName();
                            componentChild.setItem(index, new EnumMap<>(ComponentConfigureXMLFileEditor.Child.class),
                                    ComponentConfigureXMLFileEditor.Child.Name, childName);
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
            return data;
        }

        public static List<LayerImageData> getImgList(EntityData ed){
            //获取组件的组件图片ID
            List<EntityId> imgIdList = getImgIdList(ed);
            //找到组件图片关联的图层ID
            List<EntityId> layerIdList = getLayerIdByImgId(imgIdList,ed);
            //获取图层ID对应的图层数据
            Map<EntityId,LayerImageData> layerIdDataMap=new HashMap<>(layerIdList.size()+1,1);
            for (EntityId layerId:layerIdList){
                LayerImageData data = getLayerImageData(layerId,ed);
                layerIdDataMap.put(layerId,data);
            }
            return createImgList(imgIdList,layerIdDataMap,ed);
        }

        private static List<EntityId> getImgIdList(EntityData ed){
            List<EntityId> result = new ArrayList<>();
            Set<EntityId> componentIdSet = ed.findEntities(null,Component.class);
            List<EntityId> componentIdList = new ArrayList<>(componentIdSet.size());
            int[] indexList = new int[componentIdSet.size()];
            int i=0;
            for (EntityId componentId:componentIdSet){
                componentIdList.add(componentId);
                indexList[i++]=ed.getComponent(componentId,Index.class).getIndex();
            }
            sortByIndex(componentIdList,indexList);
            for (EntityId componentId:componentIdList){
                Set<EntityId> imgIdSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",componentId),Parent.class,Img.class);
                if (imgIdSet.size()>1){
                    List<EntityId> imgIdList = new ArrayList<>(imgIdSet.size());
                    indexList = new int[imgIdSet.size()];
                    i=0;
                    for (EntityId imgId:imgIdSet){
                        indexList[i++] = ed.getComponent(imgId,Index.class).getIndex();
                        imgIdList.add(imgId);
                    }
                    sortByIndex(imgIdList,indexList);
                    result.addAll(imgIdList);
                }else imgIdSet.stream().findFirst().ifPresent(result::add);
            }
            return result;
        }

        private static List<EntityId> getLayerIdByImgId(List<EntityId> imgIdList,EntityData ed){
            List<EntityId> result = new ArrayList<>();
            for (EntityId entityId:imgIdList){
                EntityId posId = ed.getComponent(entityId,ImgPos.class).getId();
                EntityId urlId = ed.getComponent(entityId,ImgUrl.class).getUrl();
                if (posId!=null && !result.contains(posId)) result.add(posId);
                if (urlId!=null && !result.contains(urlId)) result.add(urlId);
            }
            return result;
        }

        private static <T extends Object> void sortByIndex(List<T> list,int[] indexList){
            for (int i=0;i<indexList.length;i++){
                int index=indexList[i];
                for (int j=i+1;j<indexList.length;j++){
                    if (index>indexList[j]){
                        index=indexList[j];
                        indexList[j]=indexList[i];
                        indexList[i]=index;
                        T element = list.get(j);
                        list.set(j,list.get(i));
                        list.set(i,element);
                    }
                }
            }
        }

        private static LayerImageData getLayerImageData(EntityId layerId,EntityData ed){
            String type = ed.getComponent(layerId,Type.class).getType();
            if (PSLayout.LAYER_GROUP_ITEM.equals(type)){
                Set<EntityId> childIdSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",layerId));
                List<LayerImageData> layerDataList = new ArrayList<>();
                int[] indexList = new int[childIdSet.size()];
                int i=0;
                for (EntityId childId:childIdSet){
                    int index = ed.getComponent(childId,Index.class).getIndex();
                    indexList[i++]=index;
                    LayerImageData data = getLayerImageData(childId,ed);
                    layerDataList.add(data);
                }
                sortByIndex(layerDataList,indexList);
                LayerImageData result = combineToOneBigImage(layerDataList);
                String layerName = ed.getComponent(layerId,LayerImgData.class).getLayerImageData().getName();
                result.setName(layerName);
                return result;
            }else{
                return ed.getComponent(layerId,LayerImgData.class).getLayerImageData();
            }
        }

        private static LayerImageData combineToOneBigImage(List<LayerImageData> data) {
            LayerImageData bigImgData = new LayerImageData(data.get(0));
            for (LayerImageData layerImageData : data) {
                if (layerImageData.getTop() > bigImgData.getTop()) bigImgData.setTop(layerImageData.getTop());
                if (layerImageData.getBottom() < bigImgData.getBottom())
                    bigImgData.setBottom(layerImageData.getBottom());
                if (layerImageData.getLeft() < bigImgData.getLeft()) bigImgData.setLeft(layerImageData.getLeft());
                if (layerImageData.getRight() > bigImgData.getRight()) bigImgData.setRight(layerImageData.getRight());
            }
            Image bigImg = ImageHandler.createEmptyImage(bigImgData.getWidth(), bigImgData.getHeight());
            for (LayerImageData datai : data) {
                if (datai.getImg() == null) continue;
                ImageHandler.drawCombine(bigImg, datai.getImg(), datai.getLeft() - bigImgData.getLeft(), datai.getBottom() - bigImgData.getBottom());
            }
            bigImgData.setImg(bigImg);
            return bigImgData;
        }

        private static List<LayerImageData> createImgList(List<EntityId> imgIdList,Map<EntityId,LayerImageData> layerIdDataMap,EntityData ed){
            List<LayerImageData> result = new ArrayList<>();
            for (EntityId entityId:imgIdList){
                EntityId posId = ed.getComponent(entityId,ImgPos.class).getId();
                EntityId urlId = ed.getComponent(entityId,ImgUrl.class).getUrl();
                if (posId!=null){
                    if (posId==urlId){
                        LayerImageData srcData = layerIdDataMap.get(posId);
                        LayerImageData desData = new LayerImageData(srcData);
                        desData.setName(null);//不需要PS中的图层名
                        result.add(desData);
                    }else{
                        LayerImageData srcData = layerIdDataMap.get(posId);
                        LayerImageData desData = new LayerImageData(srcData);
                        desData.setName(null);//不需要PS中的图层名
                        if (urlId==null) desData.setImg(null);
                        else{
                            LayerImageData urlData = layerIdDataMap.get(urlId);
                            desData.setImg(urlData.getImg());
                        }
                        result.add(desData);
                    }
                }else{
                    result.add(new LayerImageData());
                }
            }
            return result;
        }

    }

}