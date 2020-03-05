package com.jarlure.layoutcreator.state;

import com.jarlure.layoutcreator.entitycomponent.psd.PSDData;
import com.jarlure.layoutcreator.util.SortHelper;
import com.jarlure.layoutcreator.util.txt.XmlFileCreateRecord;
import com.jarlure.layoutcreator.entitycomponent.imported.PsdFile;
import com.jarlure.layoutcreator.entitycomponent.common.Index;
import com.jarlure.layoutcreator.entitycomponent.common.Parent;
import com.jarlure.layoutcreator.entitycomponent.common.Type;
import com.jarlure.layoutcreator.entitycomponent.event.Save;
import com.jarlure.layoutcreator.entitycomponent.mark.Component;
import com.jarlure.layoutcreator.entitycomponent.mark.Img;
import com.jarlure.layoutcreator.entitycomponent.mark.Layer;
import com.jarlure.layoutcreator.entitycomponent.mark.Modified;
import com.jarlure.layoutcreator.entitycomponent.psd.LayerImgData;
import com.jarlure.layoutcreator.entitycomponent.xml.ComponentLink;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgPos;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.psd.PSDFileEditor;
import com.jarlure.layoutcreator.util.psd.PSDFileReader;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.Entity;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.bean.LayoutData;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.util.SavableHelper;
import com.jarlure.project.util.StringHandler;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.texture.Image;
import com.simsilica.es.*;

import java.io.File;
import java.util.*;

public class SaveState extends BaseAppState {

    private EntityData ed;
    private EntitySet saveSet;

    @Override
    protected void initialize(Application app) {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        saveSet = ed.getEntities(Save.class);
    }

    @Override
    protected void cleanup(Application app) {
        saveSet.release();
        saveSet=null;
        ed=null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        saveSet.applyChanges();
        if (saveSet.isEmpty())return;
        saveSet.forEach(entity -> {
            Save save = entity.get(Save.class);
            EntityId importedId = save.getImportedId();
            File psdFile = save.getPsdFile();{
                if (psdFile==null){
                    psdFile = ed.getComponent(importedId,PsdFile.class).getFile();
                }
            }
            File xmlFile = save.getXmlFile();{
                if (xmlFile==null) xmlFile = findXmlFile(psdFile);
            }
            File j3oFile = save.getJ3oFile();{
                if (j3oFile==null) j3oFile = new File(StringHandler.replaceExtension(xmlFile.getAbsolutePath(),"j3o"));
            }
            saveToPsd(importedId,psdFile);
            saveToXml(importedId,xmlFile);
            saveToJ3o(importedId,j3oFile);
            ed.removeComponent(importedId, Modified.class);
            ed.removeEntity(entity.getId());
            if (save.getCallback()!=null) save.getCallback().apply();
        });
    }

    private File findXmlFile(File psdFile) {
        String xmlName = StringHandler.replaceExtension(psdFile.getName(), "xml");
        File result = XmlFileCreateRecord.findRecordByName(xmlName);
        if (result == null) result = new File(StringHandler.replaceExtension(psdFile.getAbsolutePath(), "xml"));
        return result;
    }

    private void saveToPsd(EntityId importedId,File psdFile){
        Set<EntityId> layerSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class, "creatorId", importedId), CreatedBy.class, Layer.class, Index.class, Name.class);
        if (layerSet.isEmpty())return;
        if (!PsdHelper.isNameChanged(layerSet,ed))return;

        PSDFileReader.read(psdFile, ed);
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()), PSDData.class);
        String[] nameArray = PsdHelper.getNameArray(layerSet,ed);
        PSDFileEditor.renameLayer(psdFile,nameArray,psdData);
    }

    private void saveToXml(EntityId importedId,File xmlFile){
        EntityId[] componentIdList=XmlHelper.getComponentIdList(importedId,ed);
        Entity data = XmlHelper.getComponentConfigureData(componentIdList,ed);

        if (data.isEmpty())return;
        if (!XmlFileCreateRecord.existRecord(xmlFile)) XmlFileCreateRecord.addRecord(xmlFile);
        ComponentConfigureXMLFileEditor.writeComponentConfigure(xmlFile,data);
    }

    public void saveToJ3o(EntityId importedId,File j3oFile){
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()),PSDData.class);
        int layoutHeight = psdData.get(0).hashCode();
        int layoutWidth = psdData.get(1).hashCode();
        List<LayerImageData> imgList = J3oHelper.getImgList(importedId,ed);
        if (imgList.isEmpty())return;
        LayoutData layoutData = new LayoutData();
        layoutData.setLayoutWidth(layoutWidth);
        layoutData.setLayoutHeight(layoutHeight);
        layoutData.setImgList(imgList);

        SavableHelper.saveAsJ3OData(j3oFile,layoutData);
    }

    private static class PsdHelper {

        private static boolean isNameChanged(Set<EntityId> layerSet, EntityData ed){
            for (EntityId layerId:layerSet){
                String oldName = ed.getComponent(layerId, LayerImgData.class).getLayerImageData().getName();
                String newName = ed.getComponent(layerId, Name.class).getName();
                if (!oldName.equals(newName))return true;
            }
            return false;
        }

        private static String[] getNameArray(Set<EntityId> layerSet,EntityData ed){
            String[] result = new String[layerSet.size()];
            for (EntityId layerId:layerSet){
                int index = ed.getComponent(layerId,Index.class).getIndex();
                String newName = ed.getComponent(layerId,Name.class).getName();
                result[index]=newName;
            }
            return result;
        }

    }

    private static class XmlHelper {

        private static EntityId[] getComponentIdList(EntityId importedId,EntityData ed){
            Set<EntityId> componentSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId),CreatedBy.class,Component.class,Index.class);
            return SortHelper.sortByIndex(componentSet,ed);
        }

        private static Entity getComponentConfigureData(EntityId[] componentIdList,EntityData ed){
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
                    EntityId[] idArray = SortHelper.sortByIndex(idSet,ed);
                    boolean isImg = null != ed.getComponent(idArray[0], Img.class);
                    if (isImg) {
                        for (EntityId imgId:idArray){
                            EntityId posId = ed.getComponent(imgId, ImgPos.class).getId();
                            EntityId urlId = ed.getComponent(imgId, ImgUrl.class).getUrl();
                            String imgName = ed.getComponent(posId, Name.class).getName();
                            String imgURL;{
                                if (posId == urlId) imgURL = null;
                                else if (urlId == null) imgURL = "";
                                else imgURL = ed.getComponent(urlId, Name.class).getName();
                            }
                            componentImg.addItem(new EnumMap<>(ComponentConfigureXMLFileEditor.Img.class),
                                    ComponentConfigureXMLFileEditor.Img.Name, imgName,
                                    ComponentConfigureXMLFileEditor.Img.URL, imgURL);
                        }
                    } else {
                        for (EntityId componentId:idArray){
                            String childName = ed.getComponent(componentId, Name.class).getName();
                            componentChild.addItem( new EnumMap<>(ComponentConfigureXMLFileEditor.Child.class),
                                    ComponentConfigureXMLFileEditor.Child.Name, childName);
                        }
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

    }

    private static class J3oHelper {

        private static List<LayerImageData> getImgList(EntityId importedId,EntityData ed){
            //获取关联图片的图层数据（或是合并后的图层数据）
            Set<EntityId> imgSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId),CreatedBy.class,Img.class);
            Map<EntityId,LayerImageData> layerMap=new HashMap<>();
            imgSet.forEach(imgId->{
                ImgPos imgPos = ed.getComponent(imgId,ImgPos.class);
                if (imgPos!=null && imgPos.getId()!=null && !layerMap.containsKey(imgPos.getId())) {
                    layerMap.put(imgPos.getId(),getLayerImageData(imgPos.getId(),ed));
                }
                ImgUrl imgUrl = ed.getComponent(imgId,ImgUrl.class);
                if (imgUrl!=null && imgUrl.getUrl()!=null && !layerMap.containsKey(imgUrl.getUrl())) {
                    layerMap.put(imgUrl.getUrl(),getLayerImageData(imgUrl.getUrl(),ed));
                }
            });
            //获取组件列表
            Set<EntityId> componentSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId),CreatedBy.class,Component.class);
            EntityId[] componentArray=SortHelper.sortByIndex(componentSet,ed);
            String[] nameArray = new String[componentArray.length];{
                for (int i=0;i<nameArray.length;i++){
                    nameArray[i]=ed.getComponent(componentArray[i],Name.class).getName();
                }
            }
            List<LayerImageData> result = new ArrayList<>(componentArray.length);
            for (EntityId componentId : componentArray) {
                //获取组件的组件图片ID
                imgSet = ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", componentId), Parent.class, Img.class);
                if (imgSet.isEmpty()) continue;
                //排序
                EntityId[] imgArray = SortHelper.sortByIndex(imgSet,ed);

                //按顺序获取关联的图层数据
                for (EntityId imgId:imgArray){
                    LayerImageData data = new LayerImageData();
                    data.setName("");
                    ImgPos imgPos = ed.getComponent(imgId, ImgPos.class);
                    if (imgPos != null && imgPos.getId()!=null) {
                        LayerImageData src = layerMap.get(imgPos.getId());
                        data.setLeft(src.getLeft());
                        data.setBottom(src.getBottom());
                        data.setRight(src.getRight());
                        data.setTop(src.getTop());
                    }
                    ImgUrl imgUrl = ed.getComponent(imgId, ImgUrl.class);
                    if (imgUrl != null && imgUrl.getUrl()!=null) {
                        LayerImageData src = layerMap.get(imgUrl.getUrl());
                        data.setName(src.getName());
                        data.setImg(src.getImg());
                    }
                    result.add(data);
                }
            }
            return result;
        }

        private static LayerImageData getLayerImageData(EntityId layerId,EntityData ed){
            String type = ed.getComponent(layerId, Type.class).getType();
            if (PSLayout.LAYER_GROUP_ITEM.equals(type)){
                Set<EntityId> childIdSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",layerId));
                EntityId[] childIdArray = SortHelper.sortByIndex(childIdSet,ed);
                LayerImageData[] dataArray = new LayerImageData[childIdArray.length];
                for (int i=0;i<childIdArray.length;i++){
                    dataArray[i]=getLayerImageData(childIdArray[i],ed);
                }
                return combineToOneBigImage(dataArray);
            }else{
                return ed.getComponent(layerId,LayerImgData.class).getLayerImageData();
            }
        }

        private static LayerImageData combineToOneBigImage(LayerImageData[] dataArray) {
            LayerImageData bigImgData = new LayerImageData(dataArray[0]);
            for (LayerImageData data : dataArray) {
                if (data.getTop() > bigImgData.getTop()) bigImgData.setTop(data.getTop());
                if (data.getBottom() < bigImgData.getBottom())
                    bigImgData.setBottom(data.getBottom());
                if (data.getLeft() < bigImgData.getLeft()) bigImgData.setLeft(data.getLeft());
                if (data.getRight() > bigImgData.getRight()) bigImgData.setRight(data.getRight());
            }
            Image bigImg = ImageHandler.createEmptyImage(bigImgData.getWidth(), bigImgData.getHeight());
            for (LayerImageData data : dataArray) {
                if (data.getImg() == null) continue;
                ImageHandler.drawCombine(bigImg, data.getLeft() - bigImgData.getLeft(), data.getBottom() - bigImgData.getBottom(),data.getImg());
            }
            bigImgData.setImg(bigImg);
            return bigImgData;
        }

    }

}
