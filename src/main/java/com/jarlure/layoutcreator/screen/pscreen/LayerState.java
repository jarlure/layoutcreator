package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.psd.PSDFileReader;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.state.EntityDataState;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.es.Name;

import java.io.File;

public class LayerState extends AbstractScreenState {

    private EntityData ed;
    private EntitySet psdFileSet;

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        psdFileSet = ed.getEntities(PsdFile.class);
    }

    @Override
    public void cleanup() {
        psdFileSet.release();
        psdFileSet = null;
        ed = null;
    }

    @Override
    public void update(float tpf) {
        if (psdFileSet.applyChanges()) {
            if (!psdFileSet.getRemovedEntities().isEmpty()) {
                removeLayer();
            }
            if (!psdFileSet.getAddedEntities().isEmpty()) {
                psdFileSet.getAddedEntities().stream().findFirst().ifPresent(entity ->
                        addLayer(entity.get(PsdFile.class).getFile())
                );
            }
        }
    }

    private void removeLayer() {
        ed.findEntities(null, Layer.class).forEach(entityId -> {
            ed.removeComponent(entityId, Layer.class);
            ed.setComponent(entityId, new Decay());
        });
    }

    private void addLayer(File psdFile) {
        if (psdFile == null) return;
        if (!psdFile.exists()) return;
        PSDFileReader.read(psdFile, ed);
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

}
