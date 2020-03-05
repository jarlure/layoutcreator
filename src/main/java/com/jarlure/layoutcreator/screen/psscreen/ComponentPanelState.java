package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.common.*;
import com.jarlure.layoutcreator.entitycomponent.mark.*;
import com.jarlure.layoutcreator.entitycomponent.xml.ComponentLink;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgPos;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.ComponentPanelHelper;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.Record;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.bean.entitycomponent.Delay;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.Vision;
import com.jarlure.ui.converter.ScrollConverter;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.effect.TextEditEffect;
import com.jarlure.ui.input.*;
import com.jarlure.ui.input.extend.ButtonMouseInputListener;
import com.jarlure.ui.input.extend.TextEditKeyInputListener;
import com.jarlure.ui.input.extend.TextEditMouseInputListener;
import com.jarlure.ui.input.extend.VerticalScrollInputListener;
import com.jarlure.ui.property.*;
import com.jarlure.ui.property.common.*;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.es.*;

import java.io.File;
import java.util.*;

public class ComponentPanelState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private VaryUIComponent scrollBar = new VaryUIComponent();
    private UIComponent scrollUpArrow;
    private UIComponent scroll;
    private UIComponent scrollDownArrow;
    private UIComponent insertTipUpperLine;
    private UIComponent insertTipCenterBox;
    private UIComponent insertTipLowerLine;
    private VaryUIComponent panel = new VaryUIComponent();
    private UIComponent componentItem;
    private UIComponent imgItem;
    private VaryUIComponent componentNameTextEdit = new VaryUIComponent();
    private VaryUIComponent addComponentButton = new VaryUIComponent();
    private VaryUIComponent addImgButton = new VaryUIComponent();
    private VaryUIComponent deleteItemButton = new VaryUIComponent();
    private int componentIconAndChildrenIconInterval;
    private int componentIconAndComponentNameTextInterval;

    public ComponentPanelState(){
        operations.add(new ShowComponentItemOperation());
        operations.add(new ScrollPanelOperation());
        operations.add(new FoldItemOperation());
        operations.add(new SelectItemOperation());
        operations.add(new RenameItemOperation());
        operations.add(new AddComponentOperation());
        operations.add(new AddImgOperation());
        operations.add(new DeleteComponentOrImgOperation());
        operations.add(new MoveComponentOrImgOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        deleteItemButton.setValue(null);
        addImgButton.setValue(null);
        addComponentButton.setValue(null);
        componentNameTextEdit.setValue(null);
        imgItem=null;
        componentItem=null;
        panel.setValue(null);
        insertTipLowerLine=null;
        insertTipCenterBox=null;
        insertTipUpperLine=null;
        scrollDownArrow=null;
        scroll=null;
        scrollUpArrow=null;
        scrollBar.setValue(null);
        selectConverter=null;
        ed=null;
    }

    @Override
    public void setLayout(Layout layout) {
        this.selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        this.scrollBar.setValue(layout.getComponent(PSLayout.COMPONENT_SCROLL_BAR));
        this.scrollUpArrow = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_SCROLL_UP_ARROW);
        this.scroll = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_SCROLL);
        this.scrollDownArrow = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_SCROLL_DOWN_ARROW);
        this.panel.setValue(layout.getComponent(PSLayout.COMPONENT_PANEL));
        this.componentItem = layout.getComponent(PSLayout.COMPONENT_ITEM);
        this.imgItem = layout.getComponent(PSLayout.IMG_ITEM);
        this.componentNameTextEdit.setValue(layout.getComponent(PSLayout.COMPONENT_NAME_TEXT_EDIT));
        this.addComponentButton.setValue(layout.getComponent(PSLayout.ADD_COMPONENT_BUTTON));
        this.addImgButton.setValue(layout.getComponent(PSLayout.ADD_IMG_BUTTON));
        this.deleteItemButton.setValue(layout.getComponent(PSLayout.DELETE_COMPONENT_BUTTON));
        this.insertTipUpperLine = layout.getComponent(PSLayout.INSERT_TIP_UPPER_LINE);
        this.insertTipCenterBox = layout.getComponent(PSLayout.INSERT_TIP_CENTER_BOX);
        this.insertTipLowerLine = layout.getComponent(PSLayout.INSERT_TIP_LOWER_LINE);
        ImageHandler.saveImage(insertTipUpperLine.get(ImageProperty.class).getImage(),"C:\\Users\\Administrator\\Desktop\\output2.png");

        Font font = this.componentNameTextEdit.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_TENG_XIANG_JIA_LI).setSize(12).setColor(ColorRGBA.Black);
        AABB componentIconBox = layout.getComponent(PSLayout.COMPONENT_ICON).get(AABB.class);
        AABB secondComponentIconBox = layout.getComponent(PSLayout.SECOND_COMPONENT_ICON_POSITION).get(AABB.class);
        AABB componentNameTextBox = layout.getComponent(PSLayout.COMPONENT_NAME_TEXT).get(AABB.class);
        componentIconAndChildrenIconInterval = (int) (secondComponentIconBox.getXLeft() - componentIconBox.getXLeft());
        componentIconAndComponentNameTextInterval = (int) (componentNameTextBox.getXLeft() - componentIconBox.getXRight());
    }

    private class ShowComponentItemOperation extends AbstractOperation {

        private EntitySet currentImportedSet;
        private EntitySet componentItemSet;
        private EntitySet imgItemSet;
        private EntitySet componentParentSet;//监听组件父结点：更新组件缩进和显示
        private EntitySet layerNameSet;//监听图层名：更新关联的组件图片名
        private EntitySet componentNameSet;//监听组件名：更新组件项上的文本
        private EntitySet imgNameSet;//监听组件名：更新组件项上的文本
        private boolean itemChanged;

        @Override
        public void initialize() {
            currentImportedSet = ed.getEntities(Current.class, Imported.class);
            componentItemSet = ed.getEntities(Component.class, Index.class, Item.class, Visible.class);
            imgItemSet = ed.getEntities(Img.class, Index.class, Item.class, Visible.class);
            componentParentSet = ed.getEntities(Component.class,Parent.class);
            componentNameSet=ed.getEntities(Component.class,Name.class);
            imgNameSet=ed.getEntities(Img.class,Name.class);
            layerNameSet=ed.getEntities(Layer.class,Name.class);
        }

        @Override
        public void cleanup() {
            imgNameSet.release();
            imgNameSet=null;
            componentNameSet.release();
            componentNameSet=null;
            componentParentSet.release();
            componentParentSet=null;
            imgItemSet.release();
            imgItemSet =null;
            componentItemSet.release();
            componentItemSet =null;
            currentImportedSet.release();
            currentImportedSet=null;
        }

        @Override
        public void update(float tpf) {
            if (currentImportedSet.applyChanges()){
                //移除失去焦点的组件项
                if (!currentImportedSet.getRemovedEntities().isEmpty()) {
                    panel.get(ElementProperty.class).removeAll();
                }
                //添加获得焦点的组件项
                if (!currentImportedSet.getAddedEntities().isEmpty()){
                    EntityId importedId = currentImportedSet.getAddedEntities().iterator().next().getId();
                    Set<EntityId> componentSet=ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId),Component.class,CreatedBy.class);
                    componentSet.forEach(componentId->{//创建组件项
                        if (null==ed.getComponent(componentId,Item.class)){
                            String name = ed.getComponent(componentId,Name.class).getName();
                            String type = PSLayout.COMPONENT_ITEM;
                            int level = ComponentPanelHelper.getLevel(componentId, ed);
                            UIComponent item = ComponentPanelHelper.createItem(name, type, level,componentItem,imgItem,componentIconAndChildrenIconInterval,componentIconAndComponentNameTextInterval);
                            ed.setComponents(componentId, new Item(item),new Level(level));
                        }
                        ed.setComponent(componentId,Visible.TRUE);
                    });
                    Set<EntityId> imgSet=ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",importedId), Img.class,CreatedBy.class);
                    imgSet.forEach(imgId->{//创建图片项
                        if (null==ed.getComponent(imgId,Item.class)){
                            String name = ed.getComponent(imgId,Name.class).getName();
                            String type = PSLayout.IMG_ITEM;
                            int level = ComponentPanelHelper.getLevel(imgId, ed);
                            UIComponent item = ComponentPanelHelper.createItem(name, type, level,componentItem,imgItem,componentIconAndChildrenIconInterval,componentIconAndComponentNameTextInterval);
                            ed.setComponents(imgId, new Item(item),new Level(level));
                        }
                        ed.setComponent(imgId, Visible.TRUE);
                    });
                    ComponentPanelHelper.addAllItemToPanel(panel,ed);
                }
                //忽略此次组件项变化
                componentItemSet.applyChanges();
                imgItemSet.applyChanges();
                componentParentSet.applyChanges();
            }
            if (componentParentSet.applyChanges()){
                updateItem(componentParentSet.getRemovedEntities());
                updateItem(componentParentSet.getAddedEntities());
                updateItem(componentParentSet.getChangedEntities());
                itemChanged=true;
            }
            if (layerNameSet.applyChanges()){
                layerNameSet.getChangedEntities().forEach(entity -> {
                    Name name = entity.get(Name.class);
                    ed.findEntities(Filters.fieldEquals(ImgPos.class,"id",entity.getId()),Img.class,ImgPos.class).forEach(imgId->{
                        ed.setComponent(imgId,name);
                    });
                });
            }
            if (componentNameSet.applyChanges()){
                updateItemName(componentNameSet.getChangedEntities());
            }
            if (imgNameSet.applyChanges()){
                updateItemName(imgNameSet.getChangedEntities());
            }
            if (componentItemSet.applyChanges()) itemChanged = true;
            if (imgItemSet.applyChanges()) itemChanged = true;
            if (itemChanged){
                itemChanged=false;
                ComponentPanelHelper.updatePanelForItemChanged(panel,ed);
            }
        }

        private void updateItem(Set<Entity> entitySet){
            if (entitySet.isEmpty())return;
            //更新缩进
            Set<EntityId> levelUpdatedSet = new HashSet<>(entitySet.size());
            entitySet.forEach(entity -> {//收集所有子组件
                Level level = ed.getComponent(entity.getId(),Level.class);
                if (level==null)return;
                Item item = ed.getComponent(entity.getId(),Item.class);
                if (item==null)return;
                int newLevel = ComponentPanelHelper.getLevel(entity.getId(),ed);
                if (level.getLevel()==newLevel)return;
                levelUpdatedSet.add(entity.getId());
                ComponentPanelHelper.getChildAndGrandChildIdSet(entity.getId(),ed,levelUpdatedSet,null);
            });
            if (!levelUpdatedSet.isEmpty()){
                //执行更新缩进
                levelUpdatedSet.forEach(id->{
                    Level level = ed.getComponent(id,Level.class);
                    if (level==null)return;
                    Item item = ed.getComponent(id,Item.class);
                    if (item==null)return;
                    int newLevel = ComponentPanelHelper.getLevel(id,ed);
                    if (level.getLevel()==newLevel)return;
                    ComponentPanelHelper.updateItem(item.getItem(),level.getLevel(),newLevel,componentIconAndChildrenIconInterval,componentIconAndComponentNameTextInterval);
                    ed.setComponent(id,new Level(newLevel));
                });
            }
            //更新折叠
            entitySet.forEach(entity -> {
                Parent parent = ed.getComponent(entity.getId(),Parent.class);
                if (parent==null || !ComponentPanelHelper.isFolded(parent.getParentId(),ed)){
                    ed.setComponent(entity.getId(),Visible.TRUE);
                    ComponentPanelHelper.doFoldOrOpenItem(entity.getId(),ComponentPanelHelper.isFolded(entity.getId(),ed),ed);
                }else{
                    ComponentPanelHelper.doFoldOrOpenItem(parent.getParentId(),true,ed);
                }
            });
        }

        private void updateItemName(Set<Entity> entitySet){
            if (entitySet.isEmpty())return;
            entitySet.forEach(entity -> {
                Item item = ed.getComponent(entity.getId(),Item.class);
                if (item==null)return;
                String toName = ed.getComponent(entity.getId(),Name.class).getName();
                ComponentPanelHelper.updateItem(item.getItem(), toName);
            });
        }

    }

    private class ScrollPanelOperation extends AbstractOperation {

        private CustomPropertyListener firstItemListener = (property, oldValue, newValue) -> {
            if (scrollBar.get(ScrollConverter.class).getPercentHeight() >= 1) {
                scrollBar.setVisible(false);
                return;
            }
            if (SpatialProperty.Property.LOCAL_TRANSLATION.equals(property)) {
                ed.setComponent(ed.createEntity(),new Delay(this::updateScrollPosition));
            }
        };
        private ListPropertyListener<UIComponent> itemAddedListener = new ListPropertyAdapter<UIComponent>() {
            @Override
            public void propertyAdded(int index, UIComponent value) {
                if (index == 0) {
                    UIComponent item = panel.get(ElementProperty.class).get(0);
                    item.get(SpatialProperty.class).addPropertyListener(firstItemListener);
                }
                ed.setComponent(ed.createEntity(),new Delay(() -> {
                    updateScrollSize();
                    updateScrollPosition();
                }));
            }

            @Override
            public void propertyRemoved(int index, UIComponent value) {
                ed.setComponent(ed.createEntity(),new Delay(() -> {
                    updateScrollSize();
                    updateScrollPosition();
                }));
            }
        };
        private PropertyListener<UIComponent> setPanelListener = (oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.get(ElementProperty.class).removePropertyListener(itemAddedListener);
            }
            if (newValue != null) {
                AABB itemListBox = new AABB();
                itemListBox.set(newValue);
                scrollBar.get(ScrollConverter.class).setWindow(newValue.get(AABB.class));
                scrollBar.get(ScrollConverter.class).setObject(itemListBox);

                newValue.get(ElementProperty.class).addPropertyListener(itemAddedListener);
            }
        };
        private MouseInputListener listener = new VerticalScrollInputListener(scrollBar, panel, null) {

            @Override
            public void onWheelRolling(MouseEvent mouse) {
                super.onWheelRolling(mouse);
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(scrollUpArrow, mouse)) setObjectYTop(scroll.get(AABB.class).getYTop() + 1);
                else if (selectConverter.isSelect(scrollDownArrow, mouse))
                    setObjectYTop(scroll.get(AABB.class).getYTop() - 1);
                else super.onLeftButtonPress(mouse);
            }

            @Override
            protected void setObjectYTop(float yTop) {
                yTop = scrollBar.get(ScrollConverter.class).getObjectYTop(yTop);
                float dy = yTop - panel.get(ElementProperty.class).get(0).get(AABB.class).getYTop();
                for (UIComponent component : panel.get(ElementProperty.class).value) {
                    component.move(0, dy);
                }
            }

        };

        @Override
        public void initialize() {
            panel.addPropertyListener(setPanelListener);
        }

        @Override
        public void onEnable() {
            InputManager.add(listener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(listener);
        }

        private void updateScrollSize() {
            ScrollConverter scrollConverter = scrollBar.get(ScrollConverter.class);
            float height = scrollConverter.getPercentHeight();
            if (height >= 1) {
                scrollBar.setVisible(false);
            } else {
                scrollBar.setVisible(true);
                height *= scrollConverter.getFullHeight();
                scroll.get(AABB.class).setHeight(height);
            }
        }

        private void updateScrollPosition() {
            scroll.move(0, scrollBar.get(ScrollConverter.class).getYTop() - scroll.get(AABB.class).getYTop());
        }

    }

    private class FoldItemOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_FOLD_BUTTON, mouse)) {
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    UIComponent icon = selectedItem.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON);
                    SwitchEffect switchEffect = icon.get(SwitchEffect.class);
                    switchEffect.switchToNext();
                    boolean isFolded = PSLayout.GROUP_FOLDED == switchEffect.getIndexOfCurrentImage();
                    EntityId selectedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);

                    Record record = new FoldItemOperationRecord(selectedId, isFolded);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
            }

            private UIComponent getMouseSelected(MouseEvent mouse) {
                for (UIComponent item : panel.get(ElementProperty.class).value) {
                    if (selectConverter.isSelect(item, mouse)) {
                        return item;
                    }
                }
                return null;
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

        private class FoldItemOperationRecord implements Record {

            private EntityId id;
            private boolean folded;

            private FoldItemOperationRecord(EntityId id, boolean folded) {
                this.id = id;
                this.folded = folded;
            }

            @Override
            public boolean undo() {
                ComponentPanelHelper.doFoldOrOpenItem(id, !folded, ed);
                return true;
            }

            @Override
            public boolean redo() {
                ComponentPanelHelper.doFoldOrOpenItem(id, folded, ed);
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class SelectItemOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_FOLD_BUTTON, mouse)) return;
                if (isItemSelected(selectConverter, mouse)) {
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    EntityId selectedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                    boolean isSelectedAlready = null != ed.getComponent(selectedId, Selected.class);
                    Map<EntityId, Boolean> data = new HashMap<>();
                    if (isSelectedAlready) {
                        data.put(selectedId, false);
                    } else {
                        data.put(selectedId, true);
                        selectedId = ed.findEntity(null,Img.class,Item.class,Selected.class);
                        if (selectedId!=null) data.put(selectedId,false);
                        selectedId = ed.findEntity(null,Component.class,Item.class,Selected.class);
                        if (selectedId!=null) data.put(selectedId,false);
                    }

                    Record record = new SelectItemOperationRecord(data);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
            }

            private boolean isItemSelected(SelectConverter selectConverter, MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_ITEM, mouse)) return true;
                if (selectConverter.isSelect(PSLayout.IMG_ITEM, mouse)) return true;
                return false;
            }

            private UIComponent getMouseSelected(MouseEvent mouse) {
                for (UIComponent item : panel.get(ElementProperty.class).value) {
                    if (selectConverter.isSelect(item, mouse)) {
                        return item;
                    }
                }
                return null;
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

        private class SelectItemOperationRecord implements Record {

            private Map<EntityId, Boolean> data;

            private SelectItemOperationRecord(Map<EntityId, Boolean> data) {
                this.data = data;
            }

            @Override
            public boolean undo() {
                data.forEach((id, isSelected) -> {
                    ComponentPanelHelper.doSelectItem(id,!isSelected,ed);
                });
                return true;
            }

            @Override
            public boolean redo() {
                data.forEach((id, isSelected) -> {
                    ComponentPanelHelper.doSelectItem(id,isSelected,ed);
                });
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class RenameItemOperation extends AbstractOperation {

        private EntitySet componentNameSet;
        private VaryUIComponent selectedItemNameText = new VaryUIComponent();
        private FocusProperty focusProperty;
        private Property<Integer> selectFromIndex;
        private Property<Integer> cursorPositionIndex;
        private MouseInputListener nameTextSelectedListener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonDoubleClick(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_NAME_TEXT, mouse)) {
                    UIComponent item = getMouseSelected(mouse);
                    if (item == null) return;
                    if (item.get(UIComponent.NAME).equals(PSLayout.IMG_ITEM)) return;
                    UIComponent layerNameText = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_NAME_TEXT);
                    selectedItemNameText.setValue(layerNameText);
                    focusProperty.setFocus(true);
                }
            }

            private UIComponent getMouseSelected(MouseEvent mouse) {
                for (UIComponent item : panel.get(ElementProperty.class).value) {
                    if (selectConverter.isSelect(item, mouse)) {
                        return item;
                    }
                }
                return null;
            }
        };
        private PropertyListener<UIComponent> selectedItemNameTextChangedListener = (oldValue, newValue) -> {
            if (oldValue == newValue) return;
            if (oldValue != null) {
                String text = componentNameTextEdit.get(TextProperty.class).getText();
                UIComponent item = oldValue.get(ParentProperty.class).getParent();
                EntityId id = ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                Name oldName = ed.getComponent(id, Name.class);
                if (!oldName.getName().equals(text)) {
                    Record record = new RenameComponentOperationRecord(id, oldName, new Name(text));
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
                oldValue.setVisible(true);
                componentNameTextEdit.setVisible(false);
            }
            if (newValue != null) {
                String text = newValue.get(TextProperty.class).getText();
                componentNameTextEdit.get(TextProperty.class).setText(text);
                AABB boxOfNameText = newValue.get(AABB.class);
                AABB boxOfNameTextEdit = componentNameTextEdit.get(AABB.class);
                componentNameTextEdit.move(boxOfNameText.getXLeft() - boxOfNameTextEdit.getXLeft(), boxOfNameText.getYCenter() - boxOfNameTextEdit.getYCenter());
                selectFromIndex.setValue(0);
                cursorPositionIndex.setValue(text.length());
                componentNameTextEdit.get(TextEditEffect.class).select(0,0,0,text.length());
                componentNameTextEdit.setVisible(true);
                newValue.setVisible(false);
            }
        };
        private MouseInputListener nameTextEditMouseListener = new TextEditMouseInputListener(componentNameTextEdit) {
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
        private KeyInputListener nameTextEditKeyListener = new TextEditKeyInputListener(componentNameTextEdit) {
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
            componentNameSet=ed.getEntities(Component.class,Name.class,Item.class);
            selectedItemNameText.addPropertyListener(selectedItemNameTextChangedListener);
            focusProperty=new FocusProperty();
            focusProperty.addPropertyListener((oldValue, newValue) -> {
                if (oldValue==newValue)return;
                if (!newValue) selectedItemNameText.setValue(null);
            });
            selectFromIndex=new Property<>();
            cursorPositionIndex=new Property<>();
            cursorPositionIndex.addInputPropertyFilter(index->{
                if (index<0)return 0;
                return Math.min(index, componentNameTextEdit.get(TextProperty.class).getText().length());
            });
        }

        @Override
        public void cleanup() {
            cursorPositionIndex=null;
            selectFromIndex=null;
            focusProperty=null;
            selectedItemNameText.setValue(null);
            componentNameSet.release();
            componentNameSet=null;
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
            if (focusProperty.isFocus()) componentNameTextEdit.get(TextEditEffect.class).update(tpf);
            //重名检测
            if (componentNameSet.applyChanges()) {
                componentNameSet.getAddedEntities().forEach(entity -> {
                    if (isRepeatName(entity.getId(), entity.get(Name.class).getName())) {
                        markWithYellowColor(entity.getId());
                    }
                });
                componentNameSet.getChangedEntities().forEach(entity -> {
                    if (isRepeatName(entity.getId(), entity.get(Name.class).getName())) {
                        markWithYellowColor(entity.getId());
                    } else resumeWhiteColor(entity.getId());
                });
            }
        }

        private boolean isRepeatName(EntityId id, String newName) {
            for (Entity entity : componentNameSet) {
                if (entity.get(Name.class).getName().equals(newName)) {
                    if (entity.getId().equals(id)) continue;
                    return true;
                }
            }
            return false;
        }

        private void markWithYellowColor(EntityId id) {
            UIComponent item = componentNameSet.getEntity(id).get(Item.class).getItem();
            UIComponent nameText = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_NAME_TEXT);
            nameText.get(FontProperty.class).setColor(ColorRGBA.Yellow);
        }

        private void resumeWhiteColor(EntityId id) {
            UIComponent item = componentNameSet.getEntity(id).get(Item.class).getItem();
            UIComponent nameText = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_NAME_TEXT);
            FontProperty fontProperty = nameText.get(FontProperty.class);
            if (fontProperty.getColor().equals(ColorRGBA.White)) return;
            fontProperty.setColor(ColorRGBA.White);
        }

        private class RenameComponentOperationRecord implements Record {

            private EntityId id;
            private Name fromName;
            private Name toName;

            private RenameComponentOperationRecord(EntityId id, Name fromName, Name toName) {
                this.id = id;
                this.fromName = fromName;
                this.toName = toName;
            }

            @Override
            public boolean undo() {
                ed.setComponent(id, fromName);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                ed.setComponent(id, toName);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class AddComponentOperation extends AbstractOperation {

        private String defaultType;
        private EntitySet layerSet;
        private MouseInputListener listener = new ButtonMouseInputListener(addComponentButton) {

            private int tempNameId = 1;

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(addComponentButton, mouse)) {
                    //创建组件数据
                    CreatedBy createdBy;
                    String name = "组件" + tempNameId++;
                    int index=0;
                    int level=0;
                    Parent parent=null;
                    EntityId selectedId = ed.findEntity(null,Component.class,Selected.class);
                    if (selectedId==null){
                        EntityId importedId = ed.findEntity(null,Current.class,Imported.class);
                        createdBy=new CreatedBy(importedId);
                    }else{
                        createdBy = ed.getComponent(selectedId,CreatedBy.class);
                        index = ed.getComponent(selectedId,Index.class).getIndex();
                        level = ComponentPanelHelper.getLevel(selectedId,ed);
                        parent = ed.getComponent(selectedId,Parent.class);
                    }
                    EntityComponent[] components;{
                        UIComponent item = ComponentPanelHelper.createItem(name,PSLayout.COMPONENT_ITEM,level,componentItem,imgItem,componentIconAndChildrenIconInterval,componentIconAndComponentNameTextInterval);
                        if (parent == null) {
                            components = new EntityComponent[]{
                                    createdBy, new Component(), new ComponentLink(""), new Index(index), new Type(defaultType), new Name(name),new Level(level), new Item(item),Visible.TRUE
                            };
                        } else {
                            components = new EntityComponent[]{
                                    createdBy, new Component(), new ComponentLink(""), new Index(index), new Type(defaultType), new Name(name), parent,new Level(level), new Item(item),Visible.TRUE
                            };
                        }
                    }
                    //记录前后索引值变化
                    EntityId[] componentIdList = ComponentPanelHelper.getCurrentComponentArray(ed);
                    Map<EntityId, Index> fromIndex = new HashMap<>(componentIdList.length + 1, 1);
                    Map<EntityId, Index> toIndex = new HashMap<>(componentIdList.length + 1, 1);
                    for (int i = index; i < componentIdList.length; i++) {
                        EntityId id = componentIdList[i];
                        if (id == null) continue;
                        fromIndex.put(id, new Index(i));
                        toIndex.put(id, new Index(i + 1));
                    }

                    Record record = new AddComponentOperationRecord(ed.createEntity(), components, fromIndex, toIndex);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };

        @Override
        public void initialize() {
            String path = System.getProperty("user.dir") + "/src/main/resources/Interface/layout.dtd";
            File dtdFile = new File(path);
            String[] componentTypeList = ComponentConfigureXMLFileEditor.readComponentType(dtdFile);
            defaultType = componentTypeList.length == 0 ? "" : componentTypeList[0];
            layerSet = ed.getEntities(Layer.class, Item.class);
        }

        @Override
        public void cleanup() {
            layerSet.release();
            layerSet = null;
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
            if (layerSet.applyChanges()) addComponentButton.setVisible(!layerSet.isEmpty());
        }

        private class AddComponentOperationRecord implements Record {

            private EntityId id;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;
            private EntityComponent[] components;

            private AddComponentOperationRecord(EntityId id, EntityComponent[] components, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.id = id;
                this.components = components;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public boolean undo() {
                ed.removeComponent(id, Component.class);
                ed.setComponent(id, new Decay());
                fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                toIndex.forEach((componentId, index) -> ed.setComponent(componentId, index));
                ed.removeComponent(id, Decay.class);
                ed.setComponents(id, components);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class AddImgOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
        private MouseInputListener listener = new ButtonMouseInputListener(addImgButton) {

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(addImgButton, mouse)) {
                    //创建组件数据
                    CreatedBy createdBy;
                    int index;
                    int level;
                    Parent parent;
                    EntityComponent[] components;{
                        EntityId selectedId;
                        if (!componentSelectedSet.isEmpty()){
                            selectedId = componentSelectedSet.iterator().next().getId();
                            createdBy = ed.getComponent(selectedId,CreatedBy.class);
                            index = 0;
                            level = 1+ComponentPanelHelper.getLevel(selectedId,ed);
                            parent = new Parent(selectedId);
                        }
                        else if (!imgSelectedSet.isEmpty()){
                            selectedId = imgSelectedSet.iterator().next().getId();
                            createdBy = ed.getComponent(selectedId,CreatedBy.class);
                            index = ed.getComponent(selectedId, Index.class).getIndex();
                            level = ComponentPanelHelper.getLevel(selectedId,ed);
                            parent = ed.getComponent(selectedId,Parent.class);
                        }
                        else return;
                        String name = "";
                        UIComponent item = ComponentPanelHelper.createItem(name,PSLayout.IMG_ITEM,level,componentItem,imgItem,componentIconAndChildrenIconInterval,componentIconAndComponentNameTextInterval);
                        components = new EntityComponent[]{
                                createdBy, new Img(), new ImgPos(null), new ImgUrl(null), new Index(index), new Name(name), parent,new Level(level), new Item(item),Visible.TRUE
                        };
                    }
                    //记录前后索引值变化
                    EntityId[] imgIdList = ComponentPanelHelper.getChildArray(parent.getParentId(),ed);
                    Map<EntityId, Index> fromIndex = new HashMap<>(imgIdList.length + 1, 1);
                    Map<EntityId, Index> toIndex = new HashMap<>(imgIdList.length + 1, 1);
                    for (int i = index; i < imgIdList.length; i++) {
                        EntityId id = imgIdList[i];
                        if (id == null) continue;
                        fromIndex.put(id, new Index(i));
                        toIndex.put(id, new Index(i + 1));
                    }

                    Record record = new AddImgOperationRecord(ed.createEntity(), components, fromIndex, toIndex);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };

        @Override
        public void initialize() {
            componentSelectedSet = ed.getEntities(Component.class, Selected.class);
            imgSelectedSet = ed.getEntities(Img.class, Selected.class);
        }

        @Override
        public void cleanup() {
            imgSelectedSet.release();
            imgSelectedSet = null;
            componentSelectedSet.release();
            componentSelectedSet = null;
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
            if (componentSelectedSet.applyChanges()){
                if (componentSelectedSet.isEmpty()) addImgButton.setVisible(false);
                else {//如果选中的组件不为空
                    EntityId selectedId = componentSelectedSet.iterator().next().getId();
                    EntityId childId = ed.findEntity(Filters.fieldEquals(Parent.class,"parentId",selectedId),Parent.class,Item.class);
                    //如果子结点为空或类型为图层结点，按钮可用
                    if (childId==null || null!=ed.getComponent(childId,Img.class)) addImgButton.setVisible(true);
                    else addImgButton.setVisible(false);//否则按钮禁用
                }
            }
            if (imgSelectedSet.applyChanges()){
                if (!imgSelectedSet.isEmpty()) addImgButton.setVisible(true);
                else if (componentSelectedSet.isEmpty()) addImgButton.setVisible(false);
            }
        }

        private class AddImgOperationRecord implements Record {

            private EntityId id;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;
            private EntityComponent[] components;

            private AddImgOperationRecord(EntityId id, EntityComponent[] components, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.id = id;
                this.components = components;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public boolean undo() {
                ed.removeComponent(id, Img.class);
                ed.setComponent(id, new Decay());
                fromIndex.forEach((imgId, index) -> ed.setComponent(imgId, index));
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                toIndex.forEach((imgId, index) -> ed.setComponent(imgId, index));
                ed.removeComponent(id, Decay.class);
                ed.setComponents(id, components);
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class DeleteComponentOrImgOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
        private MouseInputListener mouseListener = new ButtonMouseInputListener(deleteItemButton) {

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(deleteItemButton, mouse)) {
                    if (!componentSelectedSet.isEmpty()) {
                        EntityId selectedId = componentSelectedSet.stream().findFirst().get().getId();
                        deleteComponent(selectedId);
                    } else if (!imgSelectedSet.isEmpty()) {
                        EntityId selectedId = imgSelectedSet.stream().findFirst().get().getId();
                        deleteImg(selectedId);
                    }
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };
        private KeyInputListener keyListener = new KeyInputAdapter() {
            @Override
            public void onKeyPressed(KeyEvent key) {
                if (key.getCode() == KeyInput.KEY_DELETE) {
                    if (!componentSelectedSet.isEmpty()) {
                        EntityId selectedId = componentSelectedSet.stream().findFirst().get().getId();
                        deleteComponent(selectedId);
                    } else if (!imgSelectedSet.isEmpty()) {
                        EntityId selectedId = imgSelectedSet.stream().findFirst().get().getId();
                        deleteImg(selectedId);
                    }
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(KeyInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            componentSelectedSet = ed.getEntities(Component.class, Selected.class);
            imgSelectedSet = ed.getEntities(Img.class, Selected.class);
        }

        @Override
        public void cleanup() {
            imgSelectedSet.release();
            imgSelectedSet = null;
            componentSelectedSet.release();
            componentSelectedSet = null;
        }

        @Override
        public void onEnable() {
            InputManager.add(mouseListener);
            InputManager.add(keyListener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(mouseListener);
            InputManager.remove(keyListener);
        }

        @Override
        public void update(float tpf) {
            if (componentSelectedSet.applyChanges()) {
                deleteItemButton.setVisible(!componentSelectedSet.isEmpty() || !imgSelectedSet.isEmpty());
            }
            if (imgSelectedSet.applyChanges()) {
                deleteItemButton.setVisible(!componentSelectedSet.isEmpty() || !imgSelectedSet.isEmpty());
            }
        }

        private void deleteComponent(EntityId componentId) {
            EntityId[] componentArray = ComponentPanelHelper.getCurrentComponentArray(ed);
            Set<EntityId> removedIdSet = ComponentPanelHelper.getChildAndGrandChildIdSet(componentId,ed,new HashSet<>(2),null);
            removedIdSet.add(componentId);
            Map<EntityId, EntityComponent[]> data = new HashMap<>(removedIdSet.size() + 1, 1);
            removedIdSet.forEach(id -> data.put(id, ComponentPanelHelper.getComponents(id, ed)));
            Map<EntityId, Index> fromIndex = new HashMap<>();
            Map<EntityId, Index> toIndex = new HashMap<>();
            for (int i = 0, index = -1; i < componentArray.length; i++) {
                EntityId id = componentArray[i];
                if (removedIdSet.contains(id)) {
                    if (index == -1) index = i;
                    continue;
                }
                if (index == -1) continue;
                fromIndex.put(id, new Index(i));
                toIndex.put(id, new Index(index++));
            }

            Record record = new DeleteItemOperationRecord(componentId,data, fromIndex, toIndex);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private void deleteImg(EntityId imgId) {
            EntityId parentId = ed.getComponent(imgId, Parent.class).getParentId();
            Set<EntityId> childIdSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",parentId),Parent.class);
            childIdSet.remove(imgId);
            int index = ed.getComponent(imgId, Index.class).getIndex();
            Map<EntityId, Index> fromIndex = new HashMap<>(childIdSet.size() + 1, 1);
            Map<EntityId, Index> toIndex = new HashMap<>(childIdSet.size() + 1, 1);
            for (EntityId id : childIdSet) {
                Index indexComponent = ed.getComponent(id, Index.class);
                int i = indexComponent.getIndex();
                if (i > index) {
                    fromIndex.put(id, indexComponent);
                    toIndex.put(id, new Index(i - 1));
                }
            }
            EntityComponent[] components = ComponentPanelHelper.getComponents(imgId,ed);
            Map<EntityId, EntityComponent[]> data = new HashMap<>(2, 1);
            data.put(imgId, components);

            Record record = new DeleteItemOperationRecord(imgId,data, fromIndex, toIndex);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private class DeleteItemOperationRecord implements Record {

            private EntityId id;
            private Map<EntityId, EntityComponent[]> data;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;

            private DeleteItemOperationRecord(EntityId id,Map<EntityId, EntityComponent[]> data, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.id=id;
                this.data = data;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public boolean undo() {
                fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                data.forEach((entityId, components) -> {
                    ed.removeComponent(entityId, Decay.class);
                    ed.setComponents(entityId, components);
                });

                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                data.forEach((entityId, components) -> {
                    for (EntityComponent component : components) {
                        if (component instanceof Component) {
                            ed.removeComponent(entityId, Component.class);
                            ed.setComponent(entityId, new Decay());
                            break;
                        }
                        if (component instanceof Img) {
                            ed.removeComponent(entityId, Img.class);
                            ed.setComponent(entityId, new Decay());
                            break;
                        }
                    }
                });
                toIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));

                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

    }

    private class MoveComponentOrImgOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private final ColorRGBA translucentColor = new ColorRGBA(1, 1, 1, 0.7f);
            private UIComponent selectedItem;
            private EntityId draggedId;
            private boolean isImg;
            private Parent draggedParent;
            private UIComponent dragged;
            private float offsetToYCenter;
            private UIComponent insertTipView;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_FOLD_BUTTON, mouse)) return;
                if (selectConverter.isSelect(PSLayout.COMPONENT_ITEM, mouse)) {
                    selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    draggedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                    isImg = false;
                    EntityId childId = ed.findEntity(Filters.fieldEquals(Parent.class, "parentId", draggedId), Parent.class, Component.class);
                    if (childId != null && ed.getComponent(childId, Visible.class).isVisible()) {
                        //含有子组件的组件项必须折叠后才能移动
                        draggedId = null;
                    }
                    return;
                }
                if (selectConverter.isSelect(PSLayout.IMG_ITEM, mouse)) {
                    selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    draggedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                    isImg = true;
                    draggedParent = ed.getComponent(draggedId, Parent.class);
                }
            }

            @Override
            public void onLeftButtonDragging(MouseEvent mouse) {
                if (draggedId == null) return;
                //更新拖拽效果
                if (dragged == null) {
                    Spatial view = (Spatial) selectedItem.get(UIComponent.VIEW);
                    dragged = new Vision(view);
                    dragged.get(ColorProperty.class).setColor(translucentColor);
                    dragged.setDepth(insertTipCenterBox.getDepth() - 0.01f);
                    getScreen().getState(UIRenderState.class).attachChildToNode(dragged);
                    AABB box = dragged.get(AABB.class);
                    offsetToYCenter = box.getYCenter() - mouse.getPressY();
                }
                AABB draggedBox = dragged.get(AABB.class);
                dragged.move(0, mouse.y - offsetToYCenter - draggedBox.getYCenter());
                AABB panelBox = panel.get(AABB.class);
                float draggedBoxYCenterBeforePanelLimit = draggedBox.getYCenter();
                float dy = panelBox.getYTop() - draggedBox.getYTop();
                if (dy < 0) dragged.move(0, dy);//检查并将拖拽组件限制在面板顶部以下
                else {
                    dy = panelBox.getYBottom() - draggedBox.getYBottom();
                    if (dy > 0) dragged.move(0, dy);//检查并将拖拽组件限制在面板底部以上
                }

                //更新插入位置提示效果
                if (draggedBox.getHeight() > Math.abs(draggedBox.getYCenter() - selectedItem.get(AABB.class).getYCenter())) {
                    //拖拽距离小于组件项的高，前后位置没有发生变化
                    hideInsertTipView();
                    return;
                }
                UIComponent item = getItem(draggedBox.getYCenter());
                EntityId itemId = item == null ? null : ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                if (itemId == draggedId || itemId == null) return;
                if (isImg) {//图片上下移动的情况
                    if (null == ed.getComponent(itemId, Img.class) || !draggedParent.equals(ed.getComponent(itemId, Parent.class))) {
                        hideInsertTipView();
                        return;
                    }
                    AABB itemBox = item.get(AABB.class);
                    if (draggedBoxYCenterBeforePanelLimit > itemBox.getYCenter()) {
                        show(insertTipUpperLine, item);
                    } else {
                        show(insertTipLowerLine, item);
                    }
                } else {//组件上下移动的情况
                    if (null != ed.getComponent(itemId, Img.class)) {
                        hideInsertTipView();
                        return;
                    }
                    EntityId childId = ed.findEntity(Filters.fieldEquals(Parent.class, "parentId", itemId), Parent.class);
                    AABB itemBox = item.get(AABB.class);
                    if (null == childId || null != ed.getComponent(childId, Component.class)) {
                        if (draggedBoxYCenterBeforePanelLimit > 0.5f * (itemBox.getYTop() + itemBox.getYCenter())) {
                            show(insertTipUpperLine, item);
                        } else if (draggedBoxYCenterBeforePanelLimit < 0.5f * (itemBox.getYBottom() + itemBox.getYCenter())) {
                            show(insertTipLowerLine, item);
                        } else show(insertTipCenterBox, item);
                    } else {
                        if (draggedBoxYCenterBeforePanelLimit > itemBox.getYCenter()) {
                            show(insertTipUpperLine, item);
                        } else if (ComponentPanelHelper.isFolded(itemId, ed)) {
                            show(insertTipLowerLine, item);
                        } else hideInsertTipView();
                    }
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (dragged != null) {
                    dragged.get(ColorProperty.class).setColor(ColorRGBA.White);
                    dragged.get(ParentProperty.class).detachFromParent();
                    dragged = null;
                }
                if (insertTipView != null) {
                    UIComponent item = getItem(insertTipView.get(AABB.class).getYCenter());
                    EntityId itemId = ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                    if (isImg) {
                        EntityId[] fromIndexArray = ComponentPanelHelper.getChildArray(draggedParent.getParentId(), ed);
                        int fromIndex = ed.getComponent(draggedId, Index.class).getIndex();
                        int toIndex = ed.getComponent(itemId, Index.class).getIndex();
                        if (insertTipView == insertTipLowerLine) toIndex++;
                        EntityId[] toIndexArray = ComponentPanelHelper.moveImg(fromIndexArray, fromIndex, toIndex);
                        Record record = new MoveImgItemOperationRecord(draggedId,fromIndexArray, toIndexArray);
                        getScreen().getState(RecordState.class).addRecord(record);
                        record.redo();
                    } else {
                        EntityId[] fromIndexArray = ComponentPanelHelper.getCurrentComponentArray(ed);
                        int fromIndex = ed.getComponent(draggedId, Index.class).getIndex();
                        int toIndex = ed.getComponent(itemId, Index.class).getIndex();
                        Parent fromParent = ed.getComponent(draggedId, Parent.class);
                        Parent toParent = null;
                        if (insertTipView == insertTipUpperLine) {
                            toParent = ed.getComponent(itemId, Parent.class);
                        } else if (insertTipView == insertTipCenterBox) {
                            toIndex++;
                            toParent = new Parent(itemId);
                        } else if (insertTipView == insertTipLowerLine) {
                            Set<EntityId> idSet = ComponentPanelHelper.getChildAndGrandChildIdSet(itemId, ed, new HashSet<>(2), null);
                            toIndex++;
                            boolean existVisible = false;
                            for (EntityId id : idSet) {
                                if (ed.getComponent(id, Visible.class).isVisible()) existVisible = true;
                                else toIndex++;
                            }
                            if (existVisible) toParent = new Parent(itemId);
                            else toParent = ed.getComponent(itemId, Parent.class);
                        }
                        EntityId[] toIndexArray = ComponentPanelHelper.moveComponent(fromIndexArray, fromIndex, toIndex, fromParent, toParent, ed);
                        Record record = new MoveComponentItemOperationRecord(draggedId, fromIndexArray, fromParent, toIndexArray, toParent);
                        getScreen().getState(RecordState.class).addRecord(record);
                        record.redo();
                    }
                    insertTipView.setVisible(false);
                    insertTipView = null;
                }
                if (draggedId != null) {
                    draggedId = null;
                    draggedParent = null;
                    selectedItem = null;
                }
            }

            private UIComponent getMouseSelected(MouseEvent mouse) {
                for (UIComponent item : panel.get(ElementProperty.class).value) {
                    if (selectConverter.isSelect(item, mouse)) {
                        return item;
                    }
                }
                return null;
            }

            private UIComponent getItem(float y) {
                ElementProperty elementProperty = panel.get(ElementProperty.class);
                if (elementProperty.isEmpty()) return null;
                for (UIComponent item : elementProperty.value) {
                    AABB box = item.get(AABB.class);
                    if (y >= box.getYBottom()) return item;
                }
                return elementProperty.get(elementProperty.size() - 1);
            }

            private void hideInsertTipView() {
                if (insertTipView == null) return;
                insertTipView.setVisible(false);
                insertTipView = null;
            }

            private void show(UIComponent insertTipView, UIComponent item) {
                float itemCenterY = item.get(AABB.class).getYCenter();
                insertTipView.move(0, itemCenterY - insertTipView.get(AABB.class).getYCenter());
                if (this.insertTipView != insertTipView) {
                    if (this.insertTipView != null) this.insertTipView.setVisible(false);
                    this.insertTipView = insertTipView;
                    this.insertTipView.setVisible(true);
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

        private class MoveImgItemOperationRecord implements Record {

            private EntityId id;
            private EntityId[] fromIndex;
            private EntityId[] toIndex;

            public MoveImgItemOperationRecord(EntityId id,EntityId[] fromIndex, EntityId[] toIndex) {
                this.id=id;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public boolean undo() {
                for (int i = 0; i < fromIndex.length; i++) {
                    ed.setComponent(fromIndex[i], new Index(i));
                }
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                for (int i = 0; i < toIndex.length; i++) {
                    ed.setComponent(toIndex[i], new Index(i));
                }
                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public void release() {
            }

        }

        private class MoveComponentItemOperationRecord implements Record {

            private EntityId id;
            private EntityId[] fromIndexArray;
            private Parent fromParent;
            private EntityId[] toIndexArray;
            private Parent toParent;

            public MoveComponentItemOperationRecord(EntityId id, EntityId[] fromIndexArray, Parent fromParent, EntityId[] toIndexArray, Parent toParent) {
                this.id = id;
                this.fromIndexArray = fromIndexArray;
                this.fromParent = fromParent;
                this.toIndexArray = toIndexArray;
                this.toParent = toParent;
            }

            @Override
            public boolean undo() {
                for (int i = 0; i < fromIndexArray.length; i++) {
                    ed.setComponent(fromIndexArray[i], new Index(i));
                }
                if (fromParent == null) ed.removeComponent(id, Parent.class);
                else ed.setComponent(id, fromParent);

                EntityId importedId = ed.getComponent(id,CreatedBy.class).getCreatorId();
                ed.setComponent(importedId,new Modified());
                return true;
            }

            @Override
            public boolean redo() {
                for (int i = 0; i < toIndexArray.length; i++) {
                    ed.setComponent(toIndexArray[i], new Index(i));
                }
                if (toParent == null) ed.removeComponent(id, Parent.class);
                else ed.setComponent(id, toParent);

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
