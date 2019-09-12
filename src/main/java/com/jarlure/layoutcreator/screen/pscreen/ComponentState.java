package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.Entity;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.bean.entitycomponent.Delay;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.state.EntityDataState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentState extends AbstractScreenState {

    private static final Logger LOG = Logger.getLogger(ComponentState.class.getName());

    private EntityData ed;
    private EntitySet xmlFileSet;

    @Override
    protected void initialize() {
        ed=getScreen().getState(EntityDataState.class).getEntityData();
        xmlFileSet=ed.getEntities(XmlFile.class);
    }

    @Override
    public void cleanup() {
        xmlFileSet.release();
        xmlFileSet=null;
        ed=null;
    }

    @Override
    public void update(float tpf) {
        if (xmlFileSet.applyChanges()){
            xmlFileSet.getRemovedEntities().stream().findFirst().ifPresent(entity -> removeComponents());
            xmlFileSet.getAddedEntities().stream().findFirst().ifPresent(entity -> {
                File xmlFile = entity.get(XmlFile.class).getFile();
                if (xmlFile!=null && xmlFile.exists()) {
                    ed.setComponent(ed.createEntity(),new Delay((entityId, extra) -> addComponents(xmlFile)));
                }
            });
        }
    }

    private void removeComponents(){
        ed.findEntities(null,Component.class).forEach(entityId -> {
            ed.removeComponent(entityId,Component.class);
            ed.setComponent(entityId,new Decay());
        });
    }

    private void addComponents(File xmlFile){
        final Map<String,EntityId> layerNameIdMap;{
            Set<EntityId> idSet = ed.findEntities(null, Layer.class, Name.class);
            layerNameIdMap= new HashMap<>(idSet.size()+1,1);
            idSet.forEach(entityId -> {
                String name = ed.getComponent(entityId,Name.class).getName();
                layerNameIdMap.put(name,entityId);
            });
        }

        Entity xmlData = ComponentConfigureXMLFileEditor.readComponentConfigure(xmlFile);
        Map<String,EntityId> componentNameIdMap=new HashMap<>(xmlData.size()+1,1);
        for (int i = xmlData.size() - 1; i >= 0; i--) {
            EntityId id = ed.createEntity();
            String type = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Type);
            String name = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Name);
            String link = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Link);
            Entity img = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Img);
            for (int j=0;j<img.size();j++){
                String imgName = img.getValue(j, ComponentConfigureXMLFileEditor.Img.Name);
                String imgUrl = img.getValue(j, ComponentConfigureXMLFileEditor.Img.URL);
                ed.setComponents(ed.createEntity(), new Img(),
                        new ImgPos(layerNameIdMap.get(imgName)),
                        new ImgUrl(layerNameIdMap.get(imgUrl)),
                        new Index(j),new Name(imgName),new Parent(id)
                );
            }
            Entity child = xmlData.getValue(i, ComponentConfigureXMLFileEditor.Component.Child);
            for (int j=0;j<child.size();j++){
                String childName = child.getValue(j, ComponentConfigureXMLFileEditor.Child.Name);
                EntityId childId = componentNameIdMap.get(childName);
                if (childId==null) LOG.log(Level.WARNING,"未找到"+name+"的子组件"+childName);
                else ed.setComponent(childId,new Parent(id));
            }
            ed.setComponents(id,new Component(),new ComponentLink(link),new Index(i),new Type(type),new Name(name),new Parent(null));

            componentNameIdMap.put(name,id);
        }
    }

}
