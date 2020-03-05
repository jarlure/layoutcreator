package com.jarlure.layoutcreator.state;

import com.jarlure.layoutcreator.entitycomponent.imported.PsdFile;
import com.jarlure.layoutcreator.entitycomponent.imported.XmlFile;
import com.jarlure.layoutcreator.entitycomponent.event.Open;
import com.jarlure.layoutcreator.entitycomponent.mark.Current;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.util.txt.XmlFileCreateRecord;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.util.StringHandler;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;

import java.io.File;
import java.util.Set;

public class OpenState extends BaseAppState {

    private EntityData ed;
    private EntitySet openSet;

    @Override
    protected void initialize(Application app) {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        openSet = ed.getEntities(Open.class);
    }

    @Override
    protected void cleanup(Application app) {
        openSet.release();
        openSet=null;
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
        openSet.applyChanges();
        if (openSet.isEmpty())return;
        Open open = openSet.stream().findFirst().get().get(Open.class);
        EntityId theImportedId = findImportedIdWithSameFile(open.getFile());
        if (theImportedId==null){
            File file = open.getFile();
            if (file.getName().endsWith(".psd")) {
                File xmlFile = new File(StringHandler.replaceExtension(file.getAbsolutePath(), "xml"));
                if (!xmlFile.exists()) xmlFile = XmlFileCreateRecord.findRecordByName(xmlFile.getName());
                if (xmlFile == null) ed.setComponents(ed.createEntity(), new Imported(),new Name(file.getName()), new PsdFile(file));
                else ed.setComponents(ed.createEntity(), new Imported(),new Name(file.getName()), new PsdFile(file), new XmlFile(xmlFile));
            }
        } else {
            EntityId id = ed.findEntity(null, Current.class, Imported.class);
            if (id != null) ed.removeComponent(id, Current.class);
            ed.setComponent(theImportedId, new Current());
        }
        openSet.forEach(entity -> ed.removeEntity(entity.getId()));
    }

    private EntityId findImportedIdWithSameFile(File file){
        Set<EntityId> importedSet = ed.findEntities(null,Imported.class);
        for (EntityId importedId:importedSet){
            PsdFile psdFile = ed.getComponent(importedId,PsdFile.class);
            if (psdFile==null)continue;
            if (psdFile.getFile().equals(file))return importedId;
        }
        return null;
    }

}
