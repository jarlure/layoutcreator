package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.common.Selected;
import com.jarlure.layoutcreator.entitycomponent.common.Type;
import com.jarlure.layoutcreator.entitycomponent.mark.Component;
import com.jarlure.layoutcreator.entitycomponent.xml.ComponentLink;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.Record;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.effect.TextEditEffect;
import com.jarlure.ui.input.KeyInputListener;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.TextEditKeyInputListener;
import com.jarlure.ui.input.extend.TextEditMouseInputListener;
import com.jarlure.ui.property.*;
import com.jarlure.ui.property.common.Property;
import com.jarlure.ui.property.common.PropertyListener;
import com.jarlure.ui.system.InputManager;
import com.jme3.math.ColorRGBA;
import com.simsilica.es.*;

import java.io.File;
import java.util.Set;

public class ComponentPropertyPanelState extends AbstractScreenState {

    private String[] componentTypeList;
    private EntityData ed;
    private EntitySet componentSelectedSet;
    private SelectConverter selectConverter;
    private UIComponent typeText;
    private VaryUIComponent typeButton = new VaryUIComponent();
    private UIComponent typeListSelectedItem;
    private UIComponent typeListItem;
    private VaryUIComponent typeListPanel = new VaryUIComponent();
    private VaryUIComponent textEdit=new VaryUIComponent();
    private UIComponent nameText;
    private UIComponent linkText;

    public ComponentPropertyPanelState(){
        operations.add(new OpenTypeListOperation());
        operations.add(new SelectTypeFromTypeListOperation());
        operations.add(new EditNameOperation());
        operations.add(new EditLinkOperation());
    }

    @Override
    protected void initialize() {
        String path = System.getProperty("user.dir") + "/src/main/resources/Interface/layout.dtd";
        File dtdFile = new File(path);
        componentTypeList= ComponentConfigureXMLFileEditor.readComponentType(dtdFile);
        ed=getScreen().getState(EntityDataState.class).getEntityData();
        componentSelectedSet=ed.getEntities(Component.class, Type.class, Name.class, ComponentLink.class, Selected.class);
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        componentSelectedSet.release();
        componentSelectedSet=null;
        ed=null;
    }

    @Override
    public void setLayout(Layout layout) {
        this.selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        this.typeText = layout.getComponent(PSLayout.COMPONENT_PROPERTY_TYPE_TEXT);
        this.typeButton.setValue(layout.getComponent(PSLayout.COMPONENT_PROPERTY_TYPE_BUTTON));
        this.typeListSelectedItem=layout.getComponent(PSLayout.COMPONENT_PROPERTY_TYPE_LIST_SELECT_ITEM);
        this.typeListItem=layout.getComponent(PSLayout.COMPONENT_PROPERTY_TYPE_LIST_ITEM);
        this.typeListPanel.setValue(layout.getComponent(PSLayout.COMPONENT_PROPERTY_TYPE_LIST_PANEL));
        this.textEdit.setValue(layout.getComponent(PSLayout.COMPONENT_PROPERTY_TEXT_EDIT));
        this.nameText=layout.getComponent(PSLayout.COMPONENT_PROPERTY_NAME_TEXT);
        this.linkText=layout.getComponent(PSLayout.COMPONENT_PROPERTY_LINK_TEXT);

        ElementProperty elementProperty = typeListPanel.get(ElementProperty.class);
        for (String type:componentTypeList){
            UIComponent item = createTypeListItem(type);
            elementProperty.add(item);
        }
        Font font = typeListSelectedItem.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_TENG_XIANG_JIA_LI);
        font.setSize(14);
        font.setColor(ColorRGBA.White);
    }

    private UIComponent createTypeListItem(String type){
        UIComponent item = typeListItem.get(UIFactory.class).create();
        Font font = item.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_TENG_XIANG_JIA_LI);
        font.setSize(14);
        font.setColor(ColorRGBA.Black);
        item.get(TextProperty.class).setText(type);
        return item;
    }

    @Override
    public void update(float tpf) {
        if (componentSelectedSet.applyChanges()){
            updatePropertyPanel(componentSelectedSet.getAddedEntities());
            updatePropertyPanel(componentSelectedSet.getChangedEntities());
        }
        super.update(tpf);
    }

    private void updatePropertyPanel(Set<Entity> selectedSet){
        if (selectedSet.isEmpty())return;
        selectedSet.stream().findFirst().ifPresent(entity -> {
            String type = entity.get(Type.class).getType();
            String name = entity.get(Name.class).getName();
            String link = entity.get(ComponentLink.class).getLink();
            typeText.get(TextProperty.class).setText(type);
            nameText.get(TextProperty.class).setText(name);
            linkText.get(TextProperty.class).setText(link);
        });
    }

    private class OpenTypeListOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(typeButton,mouse)){
                    typeButton.get(SwitchEffect.class).switchToNext();
                    typeListPanel.setVisible(!typeListPanel.isVisible());
                    typeListSelectedItem.setVisible(false);
                    if (typeListPanel.isVisible()){
                        componentSelectedSet.stream().findFirst().ifPresent(entity -> {
                            String type = ed.getComponent(entity.getId(),Type.class).getType();
                            if (!type.isEmpty()){
                                for (int i=0;i<componentTypeList.length;i++){
                                    if (type.equals(componentTypeList[i])){
                                        UIComponent item = typeListPanel.get(ElementProperty.class).get(i);
                                        typeListSelectedItem.get(TextProperty.class).setText(type);
                                        float dy = item.get(AABB.class).getYTop() - typeListSelectedItem.get(AABB.class).getYTop();
                                        typeListSelectedItem.move(0,dy);
                                        typeListSelectedItem.setVisible(true);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }else if (typeListPanel.isVisible()){
                    typeButton.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                    typeListPanel.setVisible(false);
                    typeListSelectedItem.setVisible(false);
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

    }

    private class SelectTypeFromTypeListOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            @Override
            public void onMove(MouseEvent mouse) {
                if (selectConverter.isSelect(typeListPanel,mouse)){
                    UIComponent item = getMouseSelected(mouse);
                    if (item==null)return;
                    int index = typeListPanel.get(ElementProperty.class).indexOf(item);
                    typeListSelectedItem.get(TextProperty.class).setText(componentTypeList[index]);
                    float dy = item.get(AABB.class).getYTop() - typeListSelectedItem.get(AABB.class).getYTop();
                    typeListSelectedItem.move(0,dy);
                    if (!typeListSelectedItem.isVisible()) typeListSelectedItem.setVisible(true);
                }
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(typeListSelectedItem,mouse)){
                    componentSelectedSet.stream().findFirst().ifPresent(entity -> {
                        Type fromType = ed.getComponent(entity.getId(),Type.class);
                        String type = typeListSelectedItem.get(TextProperty.class).getText();

                        Record record = new SelectTypeFromTypeListOperationRecord(entity.getId(),fromType,new Type(type));
                        getScreen().getState(RecordState.class).addRecord(record);
                        record.redo();
                    });
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

        private UIComponent getMouseSelected(MouseEvent mouse){
            for (UIComponent item:typeListPanel.get(ElementProperty.class).value){
                if (selectConverter.isSelect(item,mouse)){
                    return item;
                }
            }
            return null;
        }

        private class SelectTypeFromTypeListOperationRecord implements Record {

            private EntityId id;
            private Type fromType;
            private Type toType;

            private SelectTypeFromTypeListOperationRecord(EntityId id,Type fromType,Type toType){
                this.id=id;
                this.fromType=fromType;
                this.toType=toType;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id,fromType);
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id,toType);
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class EditNameOperation extends AbstractOperation {

        private EntityId id;
        private FocusProperty focusProperty;
        private Property<Integer> selectFromIndex;
        private Property<Integer> cursorPositionIndex;
        private MouseInputListener nameTextSelectedListener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(nameText,mouse)){
                    if (textEdit.isVisible())return;
                    if (componentSelectedSet.isEmpty())return;
                    id=componentSelectedSet.stream().findFirst().get().getId();
                    focusProperty.setFocus(true);
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };
        private PropertyListener<Boolean> focusListener = new PropertyListener<Boolean>() {
            @Override
            public void propertyChanged(Boolean oldValue, Boolean newValue) {
                if (oldValue==newValue)return;
                if (newValue){
                    String text = nameText.get(TextProperty.class).getText();
                    textEdit.get(TextProperty.class).setText(text);
                    AABB boxOfNameText = nameText.get(AABB.class);
                    AABB boxOfTextEdit = textEdit.get(AABB.class);
                    textEdit.move(boxOfNameText.getXLeft()-boxOfTextEdit.getXLeft(),boxOfNameText.getYCenter()-boxOfTextEdit.getYCenter());
                    selectFromIndex.setValue(0);
                    cursorPositionIndex.setValue(text.length());
                    textEdit.get(TextEditEffect.class).select(0,0,0,text.length());
                    nameText.setVisible(false);
                    textEdit.setVisible(true);
                }else{
                    String text = textEdit.get(TextProperty.class).getText();
                    Name oldName = ed.getComponent(id,Name.class);
                    if (!oldName.getName().equals(text)){
                        Record record = new EditNameOperationRecord(id,oldName,new Name(text));
                        getScreen().getState(RecordState.class).addRecord(record);
                        record.redo();
                    }
                    nameText.setVisible(true);
                    textEdit.setVisible(false);
                }
            }
        };
        private MouseInputListener nameTextEditMouseListener = new TextEditMouseInputListener(textEdit) {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (!focusProperty.isFocus())return;//不允许自动设置焦点为true
                super.onLeftButtonPress(mouse);
            }

            @Override
            public SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public Property<Integer> getCursorPositionIndex() {
                return cursorPositionIndex;
            }

            @Override
            public Property<Integer> getSelectFromIndex() {
                return selectFromIndex;
            }

            @Override
            public FocusProperty getFocusProperty() {
                return focusProperty;
            }
        };
        private KeyInputListener nameTextEditKeyListener = new TextEditKeyInputListener(textEdit) {
            @Override
            public Property<Integer> getCursorPositionIndex() {
                return cursorPositionIndex;
            }

            @Override
            public Property<Integer> getSelectFromIndex() {
                return selectFromIndex;
            }

            @Override
            public FocusProperty getFocusProperty() {
                return focusProperty;
            }
        };

        @Override
        public void initialize() {
            focusProperty=new FocusProperty();
            focusProperty.addPropertyListener(focusListener);
            selectFromIndex=new Property<>();
            cursorPositionIndex=new Property<>();
            cursorPositionIndex.addInputPropertyFilter(index -> {
                if (index<0)return 0;
                return Math.min(index, textEdit.get(TextProperty.class).getText().length());
            });
        }

        @Override
        public void cleanup() {
            cursorPositionIndex=null;
            selectFromIndex=null;
            focusProperty=null;
            id=null;
        }

        @Override
        public void onEnable() {
            InputManager.add(nameTextSelectedListener);
            InputManager.add(nameTextEditMouseListener);
            InputManager.add(nameTextEditKeyListener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(nameTextSelectedListener);
            InputManager.remove(nameTextEditMouseListener);
            InputManager.remove(nameTextEditKeyListener);
        }

        @Override
        public void update(float tpf) {
            if (focusProperty.isFocus()) textEdit.get(TextEditEffect.class).update(tpf);
        }

        private class EditNameOperationRecord implements Record{

            private EntityId id;
            private Name fromName;
            private Name toName;

            private EditNameOperationRecord(EntityId id,Name fromName,Name toName){
                this.id=id;
                this.fromName=fromName;
                this.toName=toName;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id,fromName);
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id,toName);
                return true;
            }

            @Override
            public void release() {
            }
        }

    }

    private class EditLinkOperation extends AbstractOperation {

        private EntityId id;
        private FocusProperty focusProperty;
        private Property<Integer> selectFromIndex;
        private Property<Integer> cursorPositionIndex;
        private MouseInputListener linkTextSelectedListener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(linkText,mouse)){
                    if (textEdit.isVisible())return;
                    if (componentSelectedSet.isEmpty())return;
                    id=componentSelectedSet.stream().findFirst().get().getId();
                    focusProperty.setFocus(true);
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };
        private PropertyListener<Boolean> focusListener = new PropertyListener<Boolean>() {
            @Override
            public void propertyChanged(Boolean oldValue, Boolean newValue) {
                if (oldValue==newValue)return;
                if (newValue){
                    String text = linkText.get(TextProperty.class).getText();
                    textEdit.get(TextProperty.class).setText(text);
                    AABB boxOfNameText = linkText.get(AABB.class);
                    AABB boxOfTextEdit = textEdit.get(AABB.class);
                    textEdit.move(boxOfNameText.getXLeft()-boxOfTextEdit.getXLeft(),boxOfNameText.getYCenter()-boxOfTextEdit.getYCenter());
                    selectFromIndex.setValue(0);
                    cursorPositionIndex.setValue(text.length());
                    textEdit.get(TextEditEffect.class).select(0,0,0,text.length());
                    linkText.setVisible(false);
                    textEdit.setVisible(true);
                }else{
                    String text = textEdit.get(TextProperty.class).getText();
                    ComponentLink oldLink = ed.getComponent(id,ComponentLink.class);
                    if (!oldLink.getLink().equals(text)){
                        Record record = new EditLinkOperationRecord(id,oldLink,new ComponentLink(text));
                        getScreen().getState(RecordState.class).addRecord(record);
                        record.redo();
                    }
                    linkText.setVisible(true);
                    textEdit.setVisible(false);
                }
            }
        };
        private MouseInputListener linkTextEditMouseListener = new TextEditMouseInputListener(textEdit) {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (!focusProperty.isFocus())return;//不允许自动设置焦点为true
                super.onLeftButtonPress(mouse);
            }

            @Override
            public SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public Property<Integer> getCursorPositionIndex() {
                return cursorPositionIndex;
            }

            @Override
            public Property<Integer> getSelectFromIndex() {
                return selectFromIndex;
            }

            @Override
            public FocusProperty getFocusProperty() {
                return focusProperty;
            }
        };
        private KeyInputListener linkTextEditKeyListener = new TextEditKeyInputListener(textEdit) {
            @Override
            public Property<Integer> getCursorPositionIndex() {
                return cursorPositionIndex;
            }

            @Override
            public Property<Integer> getSelectFromIndex() {
                return selectFromIndex;
            }

            @Override
            public FocusProperty getFocusProperty() {
                return focusProperty;
            }
        };

        @Override
        public void initialize() {
            focusProperty=new FocusProperty();
            focusProperty.addPropertyListener(focusListener);
            selectFromIndex=new Property<>();
            cursorPositionIndex=new Property<>();
            cursorPositionIndex.addInputPropertyFilter(index -> {
                if (index<0)return 0;
                return Math.min(index, textEdit.get(TextProperty.class).getText().length());
            });
        }

        @Override
        public void cleanup() {
            cursorPositionIndex=null;
            selectFromIndex=null;
            focusProperty=null;
            id=null;
        }

        @Override
        public void onEnable() {
            InputManager.add(linkTextSelectedListener);
            InputManager.add(linkTextEditMouseListener);
            InputManager.add(linkTextEditKeyListener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(linkTextSelectedListener);
            InputManager.remove(linkTextEditMouseListener);
            InputManager.remove(linkTextEditKeyListener);
        }

        @Override
        public void update(float tpf) {
            if (focusProperty.isFocus()) textEdit.get(TextEditEffect.class).update(tpf);
        }

        private class EditLinkOperationRecord implements Record{

            private EntityId id;
            private ComponentLink fromName;
            private ComponentLink toName;

            private EditLinkOperationRecord(EntityId id,ComponentLink fromLink,ComponentLink toLink){
                this.id=id;
                this.fromName=fromLink;
                this.toName=toLink;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id,fromName);
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id,toName);
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

}
