package com.jarlure.layoutcreator.state;

import com.jarlure.layoutcreator.entitycomponent.imported.PsdFile;
import com.jarlure.layoutcreator.entitycomponent.imported.XmlFile;
import com.jarlure.layoutcreator.entitycomponent.psd.PSDData;
import com.jarlure.layoutcreator.entitycomponent.common.Index;
import com.jarlure.layoutcreator.entitycomponent.common.Level;
import com.jarlure.layoutcreator.entitycomponent.common.Parent;
import com.jarlure.layoutcreator.entitycomponent.common.Type;
import com.jarlure.layoutcreator.entitycomponent.psd.LayerImgData;
import com.jarlure.layoutcreator.entitycomponent.mark.Component;
import com.jarlure.layoutcreator.entitycomponent.mark.Current;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.entitycomponent.mark.Layer;
import com.jarlure.layoutcreator.entitycomponent.xml.ComponentLink;
import com.jarlure.layoutcreator.entitycomponent.mark.Img;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgPos;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.psd.PSDFileReader;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.state.EntityDataState;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.*;

import java.util.*;

public class ImportState extends BaseAppState {

    private EntityData ed;
    private EntitySet importedSet;

    @Override
    protected void initialize(Application app) {
        ed=app.getStateManager().getState(EntityDataState.class).getEntityData();
        importedSet = ed.getEntities(Imported.class);
    }

    @Override
    protected void cleanup(Application app) {
        importedSet.release();
        importedSet=null;
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
        if (importedSet.applyChanges()){
            importedSet.getRemovedEntities().forEach(entity -> {
                removePsdData(entity.getId());
                removeXmlData(entity.getId());
            });
            importedSet.getAddedEntities().forEach(entity -> {
                addPsdData(entity.getId());
                addXmlData(entity.getId());
            });
            updateMarkOfCurrent();
        }
    }

    private void removePsdData(EntityId importedId){
        //图层
        ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId),Layer.class,CreatedBy.class).forEach(layerId -> {
            ed.removeComponent(layerId, Layer.class);
            ed.setComponent(layerId, new Decay());
        });
    }

    private void removeXmlData(EntityId importedId){
        //组件
        ed.findEntities(Filters.fieldEquals(CreatedBy.class, "creatorId", importedId), Component.class, CreatedBy.class).forEach(componentId -> {
            ed.removeComponent(componentId, Component.class);
            ed.setComponent(componentId, new Decay());
        });
        //图片
        ed.findEntities(Filters.fieldEquals(CreatedBy.class, "creatorId", importedId), Img.class, CreatedBy.class).forEach(imgId -> {
            ed.removeComponent(imgId, Img.class);
            ed.setComponent(imgId, new Decay());
        });
    }

    private void addPsdData(EntityId importedId){
        PsdFile psdFile = ed.getComponent(importedId, PsdFile.class);
        if (psdFile==null)return;
        if (psdFile.getFile() == null) return;
        if (!psdFile.getFile().exists()) return;
        PSDFileReader.read(psdFile.getFile(), ed);
        PSDData psdData = ed.getComponent(new EntityId(PSDData.class.hashCode()), PSDData.class);
        int numberOfLayer = (int) psdData.get(3);
        LayerImgData[] layerImgData = new LayerImgData[numberOfLayer];
        Level[] layerLevel = new Level[numberOfLayer];
        Type[] layerType = new Type[numberOfLayer];
        for (int i = 0; i < numberOfLayer; i++) {
            PSDData layerData = (PSDData) psdData.get(4 + i);
            int type = (int) layerData.get(layerData.size() - 1);
            switch (type) {
                case 0:
                    layerType[i] = new Type(PSLayout.LAYER_PREVIEW_ITEM);
                    break;
                case 1:
                    layerType[i] = new Type(PSLayout.LAYER_GROUP_ITEM);
                    break;
                case 2:
                    layerType[i] = new Type(PSLayout.LAYER_TEXT_ITEM);
                    break;
                default:
                    layerType[i] = new Type("");
            }
            layerLevel[i] = new Level((int) layerData.get(layerData.size() - 2));
            layerImgData[i] = new LayerImgData(i, (LayerImageData) layerData.get(layerData.size() - 3));
        }

        EntityId[] id = new EntityId[numberOfLayer];{
            for (int i = 0; i < id.length; i++) {
                if (layerType[i].getType().isEmpty()) continue;
                id[i] = ed.createEntity();
            }
        }
        EntityId[] parentId = new EntityId[numberOfLayer];{
            for (int i = parentId.length - 1; i >= 0; i--) {
                int parentLevel = layerLevel[i].getLevel();
                for (int j = i - 1; j >= 0; j--) {
                    int childLevel = layerLevel[j].getLevel();
                    if (childLevel <= parentLevel) break;
                    if (childLevel - parentLevel == 1) parentId[j] = id[i];
                }
            }
        }
        for (int i = 0, index = 0; i < layerType.length; i++) {
            if (id[i] == null) continue;
            ed.setComponents(id[i],
                    new CreatedBy(importedId),
                    new Layer(),
                    new Index(index),
                    layerType[i],
                    new Name(layerImgData[i].getName()),
                    layerImgData[i],
                    layerLevel[i],
                    new Parent(parentId[i]));
            index++;
        }
    }

    private void addXmlData(EntityId importedId){
        XmlFile xmlFile = ed.getComponent(importedId,XmlFile.class);
        if (xmlFile==null)return;
        if (xmlFile.getFile() == null) return;
        if (!xmlFile.getFile().exists()) return;
        final Map<String,EntityId> layerNameIdMap;{
            Set<EntityId> idSet = ed.findEntities(null, Layer.class, Name.class);
            layerNameIdMap= new HashMap<>(idSet.size()+1,1);
            idSet.forEach(entityId -> {
                String name = ed.getComponent(entityId,Name.class).getName();
                layerNameIdMap.put(name,entityId);
            });
        }

        com.jarlure.project.bean.Entity xmlData = ComponentConfigureXMLFileEditor.readComponentConfigure(xmlFile.getFile());
        Map<String,EntityId> componentNameIdMap=new HashMap<>(xmlData.size()+1,1);
        List<EntityId> indexList = new ArrayList<>(xmlData.size()+1);
        for (int i=0;i<xmlData.size();i++){
            String type = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Type);
            String name = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Name);
            String link = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Link);
            EntityId componentId = componentNameIdMap.get(name);
            if (componentId==null) {
                componentId = ed.createEntity();
                indexList.add(componentId);
            }
            com.jarlure.project.bean.Entity img = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Img);
            if (!img.isEmpty()){
                for (int j = 0; j < img.size(); j++) {
                    String imgName = img.getValue(j, ComponentConfigureXMLFileEditor.Img.Name);
                    String imgUrl = img.getValue(j, ComponentConfigureXMLFileEditor.Img.URL);
                    ed.setComponents(ed.createEntity(), new CreatedBy(importedId), new Img(),
                            new ImgPos(layerNameIdMap.get(imgName)),
                            new ImgUrl(layerNameIdMap.get(imgUrl)),
                            new Index(j), new Name(imgName), new Parent(componentId)
                    );
                }
            }
            com.jarlure.project.bean.Entity child = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Child);
            if (!child.isEmpty()){
                int indexOfParent = indexList.lastIndexOf(componentId);
                for (int j=0;j<child.size();j++){
                    String childName = child.getValue(j, ComponentConfigureXMLFileEditor.Child.Name);
                    EntityId childId = ed.createEntity();
                    componentNameIdMap.put(childName,childId);
                    indexList.add(indexOfParent+1+j,childId);
                    ed.setComponent(childId,new Parent(componentId));
                }
            }
            ed.setComponents(componentId,new CreatedBy(importedId),new Component(),new ComponentLink(link),new Index(i),new Type(type),new Name(name));
        }
        for (int i=0;i<indexList.size();i++){
            ed.setComponent(indexList.get(i),new Index(i));
        }
    }

    private void updateMarkOfCurrent(){
        if (importedSet.isEmpty())return;
        EntityId maxId=null;
        for (Entity entity : importedSet) {
            if (maxId == null) maxId = entity.getId();
            else if (entity.getId().getId() > maxId.getId()) maxId = entity.getId();
        }
        for (EntityId id:ed.findEntities(null,Current.class,Imported.class)){
            if (id==maxId)continue;
            ed.removeComponent(id,Current.class);
        }
        if (null!=ed.getComponent(maxId,Current.class))return;
        ed.setComponent(maxId,new Current());
    }

}
