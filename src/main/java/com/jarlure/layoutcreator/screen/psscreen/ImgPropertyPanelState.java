package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.common.Selected;
import com.jarlure.layoutcreator.entitycomponent.mark.Img;
import com.jarlure.layoutcreator.entitycomponent.mark.Layer;
import com.jarlure.layoutcreator.entitycomponent.mark.Modified;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgPos;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.Record;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.property.TextProperty;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.*;

import java.util.Set;

public class ImgPropertyPanelState extends AbstractScreenState {

    private EntityData ed;
    private EntitySet layerSelectedSet;
    private EntitySet imgSelectedSet;
    private SelectConverter selectConverter;
    private UIComponent nameText;
    private UIComponent nameButton;
    private UIComponent urlEnableCheckBox;
    private UIComponent urlText;
    private UIComponent urlButton;

    public ImgPropertyPanelState() {
        operations.add(new QuoteSelectedLayerToNameOperation());
        operations.add(new EnableUrlOperation());
        operations.add(new QuoteSelectedLayerToUrlOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        layerSelectedSet = ed.getEntities(Layer.class, Selected.class);
        imgSelectedSet = ed.getEntities(Img.class, ImgPos.class, ImgUrl.class, Selected.class);
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        imgSelectedSet.release();
        imgSelectedSet = null;
        layerSelectedSet.release();
        layerSelectedSet = null;
        ed = null;
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);
        nameText = layout.getComponent(PSLayout.IMG_PROPERTY_NAME_TEXT);
        nameButton = layout.getComponent(PSLayout.IMG_PROPERTY_NAME_QUOTE_BUTTON);
        urlEnableCheckBox = layout.getComponent(PSLayout.IMG_PROPERTY_URL_ENABLE_CHECK_BOX);
        urlText = layout.getComponent(PSLayout.IMG_PROPERTY_URL_TEXT);
        urlButton = layout.getComponent(PSLayout.IMG_PROPERTY_URL_QUOTE_BUTTON);
    }

    @Override
    public void update(float tpf) {
        layerSelectedSet.applyChanges();
        if (imgSelectedSet.applyChanges()) {
            updatePropertyPanel(imgSelectedSet.getAddedEntities());
            updatePropertyPanel(imgSelectedSet.getChangedEntities());
        }
        super.update(tpf);
    }

    private void updatePropertyPanel(Set<Entity> selectedSet) {
        if (selectedSet.isEmpty()) return;
        selectedSet.stream().findFirst().ifPresent(entity -> {
            EntityId posId = ed.getComponent(entity.getId(), ImgPos.class).getId();
            EntityId urlId = ed.getComponent(entity.getId(), ImgUrl.class).getUrl();
            String name = posId == null ? "" : ed.getComponent(posId, Name.class).getName();
            String url = urlId == null ? "" : ed.getComponent(urlId, Name.class).getName();
            nameText.get(TextProperty.class).setText(name);
            urlText.get(TextProperty.class).setText(url);
            if (url.isEmpty()) {
                urlEnableCheckBox.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                urlText.setVisible(false);
                urlButton.setVisible(false);
                urlText.setVisible(false);
            } else {
                urlEnableCheckBox.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                urlText.setVisible(true);
                urlButton.setVisible(true);
                urlText.setVisible(true);
            }
        });
    }

    private class QuoteSelectedLayerToNameOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private boolean selected;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(nameButton, mouse)) {
                    selected = true;
                    nameButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(nameButton, mouse)) {
                    if (layerSelectedSet.isEmpty()) return;
                    layerSelectedSet.stream().findFirst().ifPresent(layerEntity ->
                            imgSelectedSet.stream().findFirst().ifPresent(imgEntity -> {
                                Name oldImgName = ed.getComponent(imgEntity.getId(),Name.class);
                                ImgPos oldImgPos = ed.getComponent(imgEntity.getId(), ImgPos.class);
                                ImgUrl oldImgUrl = ed.getComponent(imgEntity.getId(), ImgUrl.class);
                                Name newImgName = ed.getComponent(layerEntity.getId(),Name.class);
                                ImgPos newImgPos = new ImgPos(layerEntity.getId());
                                ImgUrl newImgUrl = oldImgUrl;
                                if (oldImgPos.getId() == null || oldImgPos.getId().equals(oldImgUrl.getUrl()))
                                    newImgUrl = new ImgUrl(layerEntity.getId());
                                Record record = new QuoteSelectedLayerToNameOperationRecord(imgEntity.getId(),oldImgName, oldImgPos, oldImgUrl,newImgName, newImgPos, newImgUrl);
                                getScreen().getState(RecordState.class).addRecord(record);
                                record.redo();
                            })
                    );
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (selected) {
                    selected = false;
                    nameButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
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

        private class QuoteSelectedLayerToNameOperationRecord implements Record {

            private EntityId id;
            private Name fromImgName;
            private ImgPos fromImgPos;
            private ImgUrl fromImgUrl;
            private Name toImgName;
            private ImgPos toImgPos;
            private ImgUrl toImgUrl;

            private QuoteSelectedLayerToNameOperationRecord(EntityId id,Name fromImgName, ImgPos fromImgPos, ImgUrl fromImgUrl,Name toImgName, ImgPos toImgPos, ImgUrl toImgUrl) {
                this.id = id;
                this.fromImgName = fromImgName;
                this.fromImgPos = fromImgPos;
                this.fromImgUrl = fromImgUrl;
                this.toImgName = toImgName;
                this.toImgPos = toImgPos;
                this.toImgUrl = toImgUrl;
            }

            @Override
            public boolean undo() {
                ed.setComponents(id, fromImgName, fromImgPos, fromImgUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponents(id, toImgName, toImgPos, toImgUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class EnableUrlOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(urlEnableCheckBox, mouse)) {
                    if (imgSelectedSet.isEmpty()) return;
                    SwitchEffect switchEffect = urlEnableCheckBox.get(SwitchEffect.class);
                    switchEffect.switchToNext();
                    if (PSLayout.SELECT_STATE_NOTHING == switchEffect.getIndexOfCurrentImage()) {
                        imgSelectedSet.stream().findFirst().ifPresent(entity -> {
                            ImgUrl oldImgUrl = ed.getComponent(entity.getId(), ImgUrl.class);

                            Record record = new EnableUrlOperationRecord(entity.getId(), oldImgUrl, new ImgUrl(null));
                            getScreen().getState(RecordState.class).addRecord(record);
                            record.redo();
                        });
                    } else {
                        imgSelectedSet.stream().findFirst().ifPresent(entity -> {
                            ImgPos oldImgPos = ed.getComponent(entity.getId(), ImgPos.class);
                            if (oldImgPos.getId()!=null){
                                ImgUrl oldImgUrl = ed.getComponent(entity.getId(), ImgUrl.class);

                                Record record = new EnableUrlOperationRecord(entity.getId(), oldImgUrl, new ImgUrl(oldImgPos.getId()));
                                getScreen().getState(RecordState.class).addRecord(record);
                                record.redo();
                            }
                        });
                    }
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
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

        private class EnableUrlOperationRecord implements Record {

            private EntityId id;
            private ImgUrl fromUrl;
            private ImgUrl toUrl;

            private EnableUrlOperationRecord(EntityId id, ImgUrl fromUrl, ImgUrl toUrl) {
                this.id = id;
                this.fromUrl = fromUrl;
                this.toUrl = toUrl;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id, fromUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id, toUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }
        }

    }

    private class QuoteSelectedLayerToUrlOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private boolean selected;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(urlButton, mouse)) {
                    selected = true;
                    urlButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                }
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(urlButton, mouse)) {
                    if (layerSelectedSet.isEmpty()) return;
                    layerSelectedSet.stream().findFirst().ifPresent(layerEntity ->
                            imgSelectedSet.stream().findFirst().ifPresent(imgEntity -> {
                                ImgUrl oldImgUrl = ed.getComponent(imgEntity.getId(), ImgUrl.class);
                                Record record = new QuoteSelectedLayerToUrlOperationRecord(imgEntity.getId(), oldImgUrl, new ImgUrl(layerEntity.getId()));
                                getScreen().getState(RecordState.class).addRecord(record);
                                record.redo();
                            })
                    );
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (selected) {
                    selected = false;
                    urlButton.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
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

        private class QuoteSelectedLayerToUrlOperationRecord implements Record {

            private EntityId id;
            private ImgUrl fromUrl;
            private ImgUrl toUrl;

            private QuoteSelectedLayerToUrlOperationRecord(EntityId id, ImgUrl fromUrl, ImgUrl toUrl) {
                this.id = id;
                this.fromUrl = fromUrl;
                this.toUrl = toUrl;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id, fromUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id, toUrl);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

}
