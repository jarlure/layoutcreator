package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.xml.ComponentConfigureXMLFileEditor;
import com.jarlure.project.bean.commoninterface.Callback;
import com.jarlure.project.bean.commoninterface.Filter;
import com.jarlure.project.bean.commoninterface.Record;
import com.jarlure.project.bean.entitycomponent.Decay;
import com.jarlure.project.bean.entitycomponent.Delay;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.bean.Direction;
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
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.jme3.util.IntMap;
import com.simsilica.es.*;

import java.io.File;
import java.util.*;

public class ComponentPanelState extends AbstractScreenState {

    private EntityData ed;
    private EntitySet componentSet;
    private EntitySet imgSet;
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

    public ComponentPanelState() {
        operations.add(new RepeatNameCheckOperation());
        operations.add(new UpdateImgNameOperation());
        operations.add(new UpdateItemLevelOperation());
        operations.add(new UpdateItemNameOperation());
        operations.add(new UpdatePanelOperation());
        operations.add(new ScrollPanelOperation());
        operations.add(new SelectItemOperation());
        operations.add(new FoldItemOperation());
        operations.add(new AddComponentOperation());
        operations.add(new AddImgOperation());
        operations.add(new DeleteItemOperation());
        operations.add(new MoveComponentItemOperation());
        operations.add(new MoveImgItemOperation());
        operations.add(new RenameComponentOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        componentSet = ed.getEntities(Component.class, Index.class, Name.class, Parent.class);
        imgSet = ed.getEntities(Img.class, Index.class, Name.class, Parent.class);
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        imgSet.release();
        imgSet = null;
        componentSet.release();
        componentSet = null;
        ed = null;
    }

    @Override
    public void setLayout(Layout layout) {
        this.selectConverter = layout.getLayoutNode().get(SelectConverter.class);
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

        Font font = this.componentNameTextEdit.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_TENG_XIANG_JIA_LI).setSize(12).setColor(ColorRGBA.Black);
        AABB componentIconBox = layout.getComponent(PSLayout.COMPONENT_ICON).get(AABB.class);
        AABB secondComponentIconBox = layout.getComponent(PSLayout.SECOND_COMPONENT_ICON_POSITION).get(AABB.class);
        AABB componentNameTextBox = layout.getComponent(PSLayout.COMPONENT_NAME_TEXT).get(AABB.class);
        componentIconAndChildrenIconInterval = (int) (secondComponentIconBox.getXLeft() - componentIconBox.getXLeft());
        componentIconAndComponentNameTextInterval = (int) (componentNameTextBox.getXLeft() - componentIconBox.getXRight());
    }

    @Override
    public void update(float tpf) {
        if (componentSet.applyChanges()) {
            addItems(componentSet.getAddedEntities());
        }
        if (imgSet.applyChanges()) {
            addItems(imgSet.getAddedEntities());
        }
        super.update(tpf);
    }

    private void addItems(Set<Entity> addedEntitySet) {
        if (addedEntitySet.isEmpty()) return;
        addedEntitySet.forEach(entity -> {
            if (null == ed.getComponent(entity.getId(), Item.class)) {
                String name = entity.get(Name.class).getName();
                String type;
                {
                    if (componentSet.containsId(entity.getId())) type = PSLayout.COMPONENT_ITEM;
                    else type = PSLayout.IMG_ITEM;
                }
                int level = getLevel(entity.getId());
                UIComponent item = createItem(name, type, level);
                ed.setComponents(entity.getId(), new Item(item), Visible.TRUE);
            }
        });
    }

    private int getLevel(EntityId id) {
        EntityId parentId = ed.getComponent(id, Parent.class).getParentId();
        int level = 0;
        while (parentId != null) {
            level++;
            parentId = ed.getComponent(parentId, Parent.class).getParentId();
        }
        return level;
    }

    private UIComponent createItem(String name, String type, int level) {
        UIComponent item;
        ChildrenProperty childrenProperty;
        UIComponent groupFoldButton = null;
        UIComponent selectEffectIcon = null;
        UIComponent typeIcon;
        switch (type) {
            case PSLayout.COMPONENT_ITEM:
                item = componentItem.get(UIFactory.class).create();
                childrenProperty = item.get(ChildrenProperty.class);
                groupFoldButton = childrenProperty.getChildByName(PSLayout.COMPONENT_FOLD_BUTTON);
                typeIcon = childrenProperty.getChildByName(PSLayout.COMPONENT_ICON);
                break;
            case PSLayout.IMG_ITEM:
                item = imgItem.get(UIFactory.class).create();
                childrenProperty = item.get(ChildrenProperty.class);
                selectEffectIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                typeIcon = childrenProperty.getChildByName(PSLayout.IMG_ICON);
                break;
            default:
                throw new IllegalStateException();
        }
        float dx = level * componentIconAndChildrenIconInterval;
        if (groupFoldButton != null) groupFoldButton.move(dx, 0);
        if (selectEffectIcon != null) selectEffectIcon.move(dx, 0);
        typeIcon.move(dx, 0);
        if (selectEffectIcon != null) selectEffectIcon.setVisible(false);

        UIComponent componentNameText = childrenProperty.getChildByName(PSLayout.COMPONENT_NAME_TEXT);
        componentNameText.move(dx, 0);
        Font font = componentNameText.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_TENG_XIANG_JIA_LI);
        font.setColor(ColorRGBA.White);
        font.setSize(12);
        AABB nameTextBox = componentNameText.get(AABB.class);
        float componentNameTextWidth = item.get(AABB.class).getXRight() - typeIcon.get(AABB.class).getXRight() - componentIconAndChildrenIconInterval;
        nameTextBox.setWidth(componentNameTextWidth);
        componentNameText.move(typeIcon.get(AABB.class).getXRight() + componentIconAndComponentNameTextInterval - nameTextBox.getXLeft(), 0);
        TextProperty textProperty = componentNameText.get(TextProperty.class);
        textProperty.setAlign(Direction.LEFT);
        textProperty.setText(name);

        return item;
    }

    private UIComponent getMouseSelected(MouseEvent mouse) {
        for (UIComponent item : panel.get(ElementProperty.class).value) {
            if (selectConverter.isSelect(item, mouse)) {
                return item;
            }
        }
        return null;
    }

    private EntityId[] getComponentIdList() {
        EntityId[] result = new EntityId[componentSet.size()];
        if (result.length == 0) return result;
        for (Entity entity : componentSet) {
            int index = entity.get(Index.class).getIndex();
            if (index >= result.length) {
                EntityId[] array = new EntityId[index + 1];
                System.arraycopy(result, 0, array, 0, result.length);
                result = array;
            }
            result[index] = entity.getId();
        }
        return result;
    }

    private Set<EntityId> getChildAndGrandChildIdSet(EntityId parentId, Set<EntityId> store, Filter<EntityId> filter) {
        if (store == null) store = new HashSet<>();
        if (imgSet.containsId(parentId)) return store;
        Set<EntityId> idSet = getChildIdSet(parentId);
        if (idSet.isEmpty()) return store;
        if (filter == null) {
            store.addAll(idSet);
        } else {
            for (EntityId id : idSet) {
                id = filter.filter(id, parentId);
                if (id == null) continue;
                store.add(id);
            }
        }
        for (EntityId id : idSet) {
            getChildAndGrandChildIdSet(id, store, filter);
        }
        return store;
    }

    private Set<EntityId> getChildIdSet(EntityId parentId) {
        return ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentId), Parent.class);
    }

    private class RepeatNameCheckOperation extends AbstractOperation {

        private EntitySet componentNameSet;

        @Override
        public void initialize() {
            componentNameSet = ed.getEntities(Component.class, Name.class, Item.class);
        }

        @Override
        public void cleanup() {
            componentNameSet.release();
            componentNameSet = null;
        }

        @Override
        public void update(float tpf) {
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

    }

    private class UpdateImgNameOperation extends AbstractOperation {

        private EntitySet layerSet;
        private EntitySet imgSet;

        @Override
        public void initialize() {
            layerSet = ed.getEntities(Layer.class, Name.class);
            imgSet = ed.getEntities(Img.class, ImgPos.class);
        }

        @Override
        public void cleanup() {
            imgSet.release();
            imgSet = null;
            layerSet.release();
            layerSet = null;
        }

        @Override
        public void update(float tpf) {
            if (layerSet.applyChanges()) {
                layerSet.getChangedEntities().stream().forEach(entity -> {
                    String name = ed.getComponent(entity.getId(), Name.class).getName();
                    Set<EntityId> imgSet = ed.findEntities(Filters.fieldEquals(ImgPos.class, "id", entity.getId()), ImgPos.class);
                    imgSet.forEach(id -> ed.setComponent(id, new Name(name)));
                });
            }
            if (imgSet.applyChanges()) {
                imgSet.getChangedEntities().stream().forEach(entity -> {
                    EntityId id = entity.get(ImgPos.class).getId();
                    String name = ed.getComponent(id, Name.class).getName();
                    ed.setComponent(entity.getId(), new Name(name));
                });
            }
        }

    }

    private class UpdateItemLevelOperation extends AbstractOperation {

        private EntitySet componentItemSet;
        private EntitySet imgItemSet;

        @Override
        public void initialize() {
            componentItemSet = ed.getEntities(Component.class, Item.class, Parent.class);
            imgItemSet = ed.getEntities(Img.class, Item.class, Parent.class);
        }

        @Override
        public void cleanup() {
            imgItemSet.release();
            imgItemSet = null;
            componentItemSet.release();
            componentItemSet = null;
        }

        @Override
        public void update(float tpf) {
            if (componentItemSet.applyChanges()) {
                if (!componentItemSet.getAddedEntities().isEmpty()) {
                    componentItemSet.getAddedEntities().forEach(entity -> ed.setComponent(entity.getId(), new Level(getLevel(entity.getId()))));
                }
                if (!componentItemSet.getChangedEntities().isEmpty()) {
                    componentItemSet.getChangedEntities().forEach(entity -> updateItem(entity.getId()));
                }
            }
            if (imgItemSet.applyChanges()) {
                if (!imgItemSet.getAddedEntities().isEmpty()) {
                    imgItemSet.getAddedEntities().forEach(entity -> ed.setComponent(entity.getId(), new Level(getLevel(entity.getId()))));
                }
                if (!imgItemSet.getChangedEntities().isEmpty()) {
                    imgItemSet.getChangedEntities().forEach(entity -> updateItem(entity.getId()));
                }
            }
        }

        private void updateItem(EntityId id) {
            UIComponent item = ed.getComponent(id, Item.class).getItem();
            int fromLevel = ed.getComponent(id, Level.class).getLevel();
            int toLevel = getLevel(id);
            updateItem(item, fromLevel, toLevel);
            ed.setComponent(id,new Level(toLevel));
            getChildIdSet(id).forEach(this::updateItem);
        }

        private void updateItem(UIComponent item, int fromLevel, int toLevel) {
            ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
            UIComponent componentNameText = childrenProperty.getChildByName(PSLayout.COMPONENT_NAME_TEXT);
            UIComponent groupFoldButton = null;
            UIComponent selectEffectIcon = null;
            UIComponent typeIcon;
            {
                typeIcon = childrenProperty.getChildByName(PSLayout.COMPONENT_ICON);
                if (typeIcon != null) {
                    groupFoldButton = childrenProperty.getChildByName(PSLayout.COMPONENT_FOLD_BUTTON);
                } else {
                    typeIcon = childrenProperty.getChildByName(PSLayout.IMG_ICON);
                    selectEffectIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                }
            }

            if (fromLevel != toLevel) {
                float dx = (toLevel - fromLevel) * componentIconAndChildrenIconInterval;
                if (groupFoldButton != null) groupFoldButton.move(dx, 0);
                if (selectEffectIcon != null) selectEffectIcon.move(dx, 0);
                typeIcon.move(dx, 0);

                AABB nameTextBox = componentNameText.get(AABB.class);
                float componentNameTextWidth = item.get(AABB.class).getXRight() - typeIcon.get(AABB.class).getXRight() - componentIconAndChildrenIconInterval;
                nameTextBox.setWidth(componentNameTextWidth);
                componentNameText.move(typeIcon.get(AABB.class).getXRight() + componentIconAndComponentNameTextInterval - nameTextBox.getXLeft(), 0);
            }
        }

    }

    private class UpdateItemNameOperation extends AbstractOperation {

        private EntitySet componentItemSet;
        private EntitySet imgItemSet;

        @Override
        public void initialize() {
            componentItemSet = ed.getEntities(Component.class, Name.class, Item.class);
            imgItemSet = ed.getEntities(Img.class, Name.class, Item.class);
        }

        @Override
        public void cleanup() {
            imgItemSet.release();
            imgItemSet = null;
            componentItemSet.release();
            componentItemSet = null;
        }

        @Override
        public void update(float tpf) {
            //监听到Name组件更新后更新对应componentNameText中的文本内容
            if (componentItemSet.applyChanges()) {
                if (!componentItemSet.getChangedEntities().isEmpty()) {
                    componentItemSet.forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        String toName = entity.get(Name.class).getName();
                        updateItem(item, toName);
                    });
                }
            }
            if (imgItemSet.applyChanges()) {
                if (!imgItemSet.getChangedEntities().isEmpty()) {
                    imgItemSet.forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        String toName = entity.get(Name.class).getName();
                        updateItem(item, toName);
                    });
                }
            }
        }

        private void updateItem(UIComponent item, String toName) {
            ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
            UIComponent componentNameText = childrenProperty.getChildByName(PSLayout.COMPONENT_NAME_TEXT);
            componentNameText.get(TextProperty.class).setText(toName);
        }

    }

    private class UpdatePanelOperation extends AbstractOperation {

        private EntitySet componentItemSet;
        private EntitySet imgItemSet;
        private boolean updatePanel;

        @Override
        public void initialize() {
            componentItemSet = ed.getEntities(Component.class, Index.class, Item.class, Visible.class);
            imgItemSet = ed.getEntities(Img.class, Index.class, Item.class, Visible.class);
        }

        @Override
        public void cleanup() {
            imgItemSet.release();
            imgItemSet = null;
            componentItemSet.release();
            componentItemSet = null;
        }

        @Override
        public void update(float tpf) {
            if (componentItemSet.applyChanges()) {
                updatePanel = true;
            }
            if (imgItemSet.applyChanges()) {
                updatePanel = true;
            }
            if (updatePanel) {
                updatePanel = false;
                updatePanel();
            }
        }

        private void updatePanel() {
            EntityId[] componentIdList = getComponentIdList();
            //移除每个不可见的组件Id
            removeInvisibleId(componentIdList);
            componentIdList = removeNullAfterMaxIndex(componentIdList);

            //找到每个组件Id对应的图片Id
            IntMap<EntityId[]> imgIdMap = new IntMap<>();
            for (EntityId id : componentIdList) {
                if (id == null) continue;
                EntityId[] imgIdList = getImgIdList(id);
                //移除每个不可见的图片Id
                removeInvisibleId(imgIdList);
                imgIdList = removeNullAfterMaxIndex(imgIdList);

                if (imgIdList.length > 0) {
                    int index = componentSet.getEntity(id).get(Index.class).getIndex();
                    imgIdMap.put(index, imgIdList);
                }
            }
            List<UIComponent> itemList = toItemList(componentIdList, imgIdMap);

            updatePanel(itemList);
        }

        private void removeInvisibleId(EntityId[] result) {
            for (int i = 0; i < result.length; i++) {
                EntityId id = result[i];
                if (id == null) continue;
                Visible visible = ed.getComponent(id, Visible.class);
                if (visible == null || !visible.isVisible()) {
                    result[i] = null;
                }
            }
        }

        private EntityId[] removeNullAfterMaxIndex(EntityId[] result) {
            if (result.length == 0) return result;
            if (result[result.length - 1] == null) {
                int maxIndex = -1;
                for (int i = result.length - 2; i >= 0; i--) {
                    if (result[i] != null) {
                        maxIndex = i;
                        break;
                    }
                }
                if (maxIndex == -1) return new EntityId[0];
                EntityId[] array = new EntityId[maxIndex + 1];
                System.arraycopy(result, 0, array, 0, array.length);
                result = array;
            }
            return result;
        }

        private EntityId[] getImgIdList(EntityId parentId) {
            Set<EntityId> idSet = getChildIdSet(parentId);
            if (idSet.isEmpty()) return new EntityId[0];
            for (EntityId id : idSet) {
                if (componentSet.containsId(id)) return new EntityId[0];
            }
            EntityId[] result = new EntityId[idSet.size()];
            for (EntityId id : idSet) {
                Entity entity = imgSet.getEntity(id);
                if (entity == null) continue;
                int index = entity.get(Index.class).getIndex();
                if (index > result.length) {
                    EntityId[] array = new EntityId[index + 1];
                    System.arraycopy(result, 0, array, 0, result.length);
                    result = array;
                }
                result[index] = id;
            }
            return result;
        }

        private List<UIComponent> toItemList(EntityId[] componentIdList, IntMap<EntityId[]> imgIdMap) {
            List<UIComponent> result = new ArrayList<>(3 * componentIdList.length);
            for (int i = 0; i < componentIdList.length; i++) {
                EntityId componentId = componentIdList[i];
                if (componentId == null) continue;
                UIComponent componentItem = ed.getComponent(componentId, Item.class).getItem();
                if (componentItem == null) continue;
                result.add(componentItem);
                EntityId[] imgIdList = imgIdMap.get(i);
                if (imgIdList == null) continue;
                if (imgIdList.length == 0) continue;
                for (EntityId imgId : imgIdList) {
                    UIComponent imgItem = ed.getComponent(imgId, Item.class).getItem();
                    if (imgItem == null) continue;
                    result.add(imgItem);
                }
            }
            return result;
        }

        private void updatePanel(List<UIComponent> itemList) {
            ElementProperty elementProperty = panel.get(ElementProperty.class);
            int itemListSize = itemList.size();
            if (itemList.size() < elementProperty.size()) {
                int lastIndex = elementProperty.size() - 1;
                while (lastIndex >= itemListSize) {
                    elementProperty.remove(lastIndex);
                    lastIndex--;
                }
            }
            for (int i = 0; i < elementProperty.size(); i++) {
                if (itemList.get(i) != elementProperty.get(i)) {
                    int lastIndex = elementProperty.size() - 1;
                    while (lastIndex >= i) {
                        elementProperty.remove(lastIndex);
                        lastIndex--;
                    }
                    break;
                }
            }
            if (elementProperty.size() < itemListSize) {
                for (int i = elementProperty.size(); i < itemListSize; i++) {
                    elementProperty.add(itemList.get(i));
                }
            }
        }

    }

    private class ScrollPanelOperation extends AbstractOperation {

        private CustomPropertyListener firstItemListener = (property, oldValue, newValue) -> {
            if (scrollBar.get(ScrollConverter.class).getPercentHeight() >= 1) {
                scrollBar.setVisible(false);
                return;
            }
            if (SpatialProperty.Property.LOCAL_TRANSLATION.equals(property)) {
                ed.setComponent(ed.createEntity(),new Delay((obj, extra) -> updateScrollPosition()));
            }
        };
        private ListPropertyListener<UIComponent> itemAddedListener = new ListPropertyAdapter<UIComponent>() {
            @Override
            public void propertyAdded(int index, UIComponent value) {
                if (index == 0) {
                    UIComponent item = panel.get(ElementProperty.class).get(0);
                    item.get(SpatialProperty.class).addPropertyListener(firstItemListener);
                }
                ed.setComponent(ed.createEntity(),new Delay((obj, extra) -> {
                    updateScrollSize();
                    updateScrollPosition();
                }));
            }

            @Override
            public void propertyRemoved(int index, UIComponent value) {
                ed.setComponent(ed.createEntity(),new Delay((obj, extra) -> {
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

    private class SelectItemOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
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
                        for (Entity entity : imgSelectedSet) {
                            data.put(entity.getId(), false);
                        }
                        for (Entity entity : componentSelectedSet) {
                            data.put(entity.getId(), false);
                        }
                        data.put(selectedId, true);
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
        };

        @Override
        public void initialize() {
            componentSelectedSet = ed.getEntities(Component.class, Item.class, Selected.class);
            imgSelectedSet = ed.getEntities(Img.class, Item.class, Selected.class);
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
            if (componentSelectedSet.applyChanges()) {
                if (!componentSelectedSet.getRemovedEntities().isEmpty()) {
                    componentSelectedSet.getRemovedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                    });
                }
                if (!componentSelectedSet.getAddedEntities().isEmpty()) {
                    componentSelectedSet.getAddedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                    });
                }
            }
            if (imgSelectedSet.applyChanges()) {
                if (!imgSelectedSet.getRemovedEntities().isEmpty()) {
                    imgSelectedSet.getRemovedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                        selectedIcon.setVisible(false);
                        childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                    });
                }
                if (!imgSelectedSet.getAddedEntities().isEmpty()) {
                    imgSelectedSet.getAddedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                        selectedIcon.setVisible(true);
                        childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                    });
                }
            }
        }

        private class SelectItemOperationRecord implements Record {

            private Map<EntityId, Boolean> data;

            private SelectItemOperationRecord(Map<EntityId, Boolean> data) {
                this.data = data;
            }

            @Override
            public void undo() {
                data.forEach((id, isSelected) -> {
                    if (isSelected) ed.removeComponent(id, Selected.class);
                    else ed.setComponent(id, new Selected());
                });
            }

            @Override
            public void redo() {
                data.forEach((id, isSelected) -> {
                    if (isSelected) ed.setComponent(id, new Selected());
                    else ed.removeComponent(id, Selected.class);
                });
            }

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

        private class FoldItemOperationRecord implements Record {

            private EntityId id;
            private boolean folded;

            private FoldItemOperationRecord(EntityId id, boolean folded) {
                this.id = id;
                this.folded = folded;
            }

            @Override
            public void undo() {
                doFoldOrOpenComponentItem(id, !folded);
            }

            @Override
            public void redo() {
                doFoldOrOpenComponentItem(id, folded);
            }

            private void doFoldOrOpenComponentItem(EntityId id, boolean isFolded) {
                UIComponent item = ed.getComponent(id, Item.class).getItem();
                UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON);
                SwitchEffect switchEffect = icon.get(SwitchEffect.class);
                if (isFolded) {
                    switchEffect.switchTo(PSLayout.GROUP_FOLDED);
                    Set<EntityId> idSet = getChildAndGrandChildIdSet(id, new HashSet<>(2), null);
                    for (EntityId childId : idSet) {
                        ed.setComponent(childId, Visible.FALSE);
                    }
                } else {
                    switchEffect.switchTo(PSLayout.GROUP_UNFOLDED);
                    Set<EntityId> idSet = getChildAndGrandChildIdSet(id, new HashSet<>(2), (entityId, extra) -> {
                        EntityId parentId = (EntityId) extra[0];
                        if (isFolded(parentId)) return null;
                        return entityId;
                    });
                    for (EntityId childId : idSet) {
                        ed.setComponent(childId, Visible.TRUE);
                    }
                }
            }

            private boolean isFolded(EntityId id) {
                UIComponent item = ed.getComponent(id, Item.class).getItem();
                UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON);
                SwitchEffect switchEffect = icon.get(SwitchEffect.class);
                return switchEffect.getIndexOfCurrentImage() == PSLayout.GROUP_FOLDED;
            }

        }

    }

    private class AddComponentOperation extends AbstractOperation {

        private String defaultType;
        private EntitySet layerSet;
        private MouseInputListener listener = new ButtonMouseInputListener(addComponentButton) {

            private int tempNameId = 1;

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(addComponentButton, mouse)) {
                    int index = 0;
                    EntityId parentId = null;
                    Set<EntityId> componentSelectedSet = ed.findEntities(null, Component.class, Selected.class);
                    for (EntityId id : componentSelectedSet) {
                        index = ed.getComponent(id, Index.class).getIndex();
                        parentId = ed.getComponent(id, Parent.class).getParentId();
                    }
                    EntityId[] componentIdList = getComponentIdList();
                    Map<EntityId, Index> fromIndex = new HashMap<>(componentIdList.length + 1, 1);
                    Map<EntityId, Index> toIndex = new HashMap<>(componentIdList.length + 1, 1);
                    for (int i = index; i < componentIdList.length; i++) {
                        EntityId id = componentIdList[i];
                        if (id == null) continue;
                        fromIndex.put(id, new Index(i));
                        toIndex.put(id, new Index(i + 1));
                    }
                    EntityComponent[] components = new EntityComponent[]{
                            new Component(), new ComponentLink(""), new Index(index), new Type(defaultType), new Name("组件" + tempNameId), new Parent(parentId)
                    };
                    tempNameId++;

                    Record record = new AddComponentOperationRecord(ed.createEntity(), components, fromIndex, toIndex);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
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
            public void undo() {
                if (componentSet.containsId(id)) {
                    ed.removeComponent(id, Component.class);
                    ed.setComponent(id, new Decay());
                    fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                }
            }

            @Override
            public void redo() {
                toIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.removeComponent(id, Decay.class);
                ed.setComponents(id, components);
            }

        }

    }

    private class AddImgOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
        private MouseInputListener listener = new ButtonMouseInputListener(addImgButton) {
            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(addImgButton, mouse)) {
                    int index = 0;
                    EntityId parentId;
                    if (!componentSelectedSet.isEmpty()) {
                        parentId = componentSelectedSet.stream().findFirst().get().getId();
                    } else if (!imgSelectedSet.isEmpty()) {
                        EntityId id = imgSelectedSet.stream().findFirst().get().getId();
                        index = ed.getComponent(id, Index.class).getIndex();
                        parentId = ed.getComponent(id, Parent.class).getParentId();
                    } else return;
                    Set<EntityId> idSet = getChildIdSet(parentId);
                    Map<EntityId, Index> fromIndex = new HashMap<>(idSet.size() + 1, 1);
                    Map<EntityId, Index> toIndex = new HashMap<>(idSet.size() + 1, 1);
                    for (EntityId id : idSet) {
                        Index indexComponent = ed.getComponent(id, Index.class);
                        int i = indexComponent.getIndex();
                        if (i >= index) {
                            fromIndex.put(id, indexComponent);
                            toIndex.put(id, new Index(i + 1));
                        }
                    }
                    EntityComponent[] components = new EntityComponent[]{
                            new Img(), new ImgPos(null), new ImgUrl(null), new Index(index), new Name(""), new Parent(parentId)
                    };

                    Record record = new AddImgOperationRecord(ed.createEntity(), components, fromIndex, toIndex);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
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
            if (componentSelectedSet.applyChanges()) {
                if (imgSelectedSet.isEmpty()) {
                    if (componentSelectedSet.isEmpty()) addImgButton.setVisible(false);
                    else {
                        addImgButton.setVisible(true);
                        componentSelectedSet.stream().findFirst().ifPresent(
                                entity -> getChildIdSet(entity.getId()).stream().findFirst().ifPresent(childId -> {
                                    if (componentSet.containsId(childId)) addImgButton.setVisible(false);
                                })
                        );
                    }
                }
            }
            if (imgSelectedSet.applyChanges()) {
                if (componentSelectedSet.isEmpty() && imgSelectedSet.isEmpty()) addImgButton.setVisible(false);
                else {
                    imgSelectedSet.stream().findFirst().ifPresent(entity -> addImgButton.setVisible(true));
                }
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
            public void undo() {
                if (imgSet.containsId(id)) {
                    ed.removeComponent(id, Component.class);
                    ed.setComponent(id, new Decay());
                    fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                }
            }

            @Override
            public void redo() {
                toIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.removeComponent(id, Decay.class);
                ed.setComponents(id, components);
            }

        }

    }

    private class DeleteItemOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private EntitySet imgSelectedSet;
        private MouseInputListener mouseListener = new ButtonMouseInputListener(deleteItemButton) {

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

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
            EntityId[] componentIdList = getComponentIdList();
            Set<EntityId> removedIdSet = getChildAndGrandChildIdSet(componentId, new HashSet<>(2), null);
            removedIdSet.add(componentId);
            Map<EntityId, EntityComponent[]> data = new HashMap<>(removedIdSet.size() + 1, 1);
            removedIdSet.forEach(id -> data.put(id, getComponents(id, !componentSet.containsId(id))));
            Map<EntityId, Index> fromIndex = new HashMap<>();
            Map<EntityId, Index> toIndex = new HashMap<>();
            for (int i = 0, index = -1; i < componentIdList.length; i++) {
                EntityId id = componentIdList[i];
                if (removedIdSet.contains(id)) {
                    if (index == -1) index = i;
                    continue;
                }
                if (index == -1) continue;
                fromIndex.put(id, new Index(i));
                toIndex.put(id, new Index(index++));
            }

            Record record = new DeleteItemOperationRecord(data, fromIndex, toIndex);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private void deleteImg(EntityId imgId) {
            EntityId parentId = ed.getComponent(imgId, Parent.class).getParentId();
            Set<EntityId> childIdSet = getChildIdSet(parentId);
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
            EntityComponent[] components = getComponents(imgId, true);
            Map<EntityId, EntityComponent[]> data = new HashMap<>(2, 1);
            data.put(imgId, components);

            Record record = new DeleteItemOperationRecord(data, fromIndex, toIndex);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private EntityComponent[] getComponents(EntityId id, boolean isImg) {
            EntityComponent typeMark = isImg ? ed.getComponent(id, Img.class) : ed.getComponent(id, Component.class);
            EntityComponent componentLink = isImg ? null : ed.getComponent(id, ComponentLink.class);
            EntityComponent imgPos = isImg ? ed.getComponent(id, ImgPos.class) : null;
            EntityComponent imgUrl = isImg ? ed.getComponent(id, ImgUrl.class) : null;
            EntityComponent index = ed.getComponent(id, Index.class);
            EntityComponent componentType = isImg ? null : ed.getComponent(id, Type.class);
            EntityComponent name = ed.getComponent(id, Name.class);
            EntityComponent parent = ed.getComponent(id, Parent.class);
            EntityComponent item = ed.getComponent(id, Item.class);
            EntityComponent visible = ed.getComponent(id, Visible.class);
            EntityComponent level = ed.getComponent(id, Level.class);
            EntityComponent selected = ed.getComponent(id, Selected.class);

            EntityComponent[] components = new EntityComponent[]{
                    typeMark, componentLink, imgPos, imgUrl, index, componentType, name, parent, item, visible, level, selected
            };
            return Arrays.stream(components).filter(Objects::nonNull).toArray(EntityComponent[]::new);
        }

        private class DeleteItemOperationRecord implements Record {

            private Map<EntityId, EntityComponent[]> data;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;

            private DeleteItemOperationRecord(Map<EntityId, EntityComponent[]> data, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.data = data;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public void undo() {
                fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                data.forEach((entityId, components) -> {
                    ed.removeComponent(entityId, Decay.class);
                    ed.setComponents(entityId, components);
                });
            }

            @Override
            public void redo() {
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
            }

        }

    }

    private class MoveComponentItemOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private final ColorRGBA translucentColor = new ColorRGBA(1,1,1,0.7f);
            private EntityId draggedId;
            private UIComponent dragged;
            private float offsetToYCenter;
            private UIComponent insertTipView;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_FOLD_BUTTON, mouse)) return;
                if (selectConverter.isSelect(PSLayout.COMPONENT_ITEM, mouse)){
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    draggedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                }
            }

            @Override
            public void onLeftButtonDragging(MouseEvent mouse) {
                if (draggedId == null) return;
                //更新拖拽效果
                if (dragged ==null){
                    Spatial view = (Spatial) ed.getComponent(draggedId,Item.class).getItem().get(UIComponent.VIEW);
                    dragged =new Vision(view);
                    dragged.get(ColorProperty.class).setColor(translucentColor);
                    dragged.setDepth(insertTipCenterBox.getDepth()-0.01f);
                    getScreen().getState(UIRenderState.class).attachChildToNode(dragged);
                    AABB box = dragged.get(AABB.class);
                    offsetToYCenter =box.getYCenter()-mouse.getPressY();
                }
                AABB draggedBox = dragged.get(AABB.class);
                dragged.move(0, mouse.y - offsetToYCenter - draggedBox.getYCenter());
                AABB panelBox = panel.get(AABB.class);
                float dy = panelBox.getYTop()-draggedBox.getYTop();
                if (dy<0) dragged.move(0,dy);//检查并将拖拽组件限制在面板顶部以下
                else {
                    dy = panelBox.getYBottom()-draggedBox.getYBottom();
                    if (dy>0) dragged.move(0,dy);//检查并将拖拽组件限制在面板底部以上
                }

                //更新插入位置提示效果
                UIComponent item =getItem(draggedBox.getYCenter());
                EntityId itemId = item==null? null : ed.findEntity(Filters.fieldEquals(Item.class,"item",item),Item.class);
                if (itemId==draggedId || itemId==null) return;
                boolean isImg = imgSet.containsId(itemId);
                if (isImg){
                    hideInsertTipView();
                    return;
                }
                boolean isFolded = PSLayout.GROUP_FOLDED == item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON).get(SwitchEffect.class).getIndexOfCurrentImage();
                AABB itemBox = item.get(AABB.class);
                boolean isUpper = isFolded? draggedBox.getYCenter()>0.5f*(itemBox.getYTop()+itemBox.getYCenter()): draggedBox.getYCenter()>itemBox.getYCenter();
                if (isUpper){
                    show(insertTipUpperLine,item);
                    return;
                }
                boolean isLower = !isFolded || draggedBox.getYCenter() < 0.5f * (itemBox.getYBottom() + itemBox.getYCenter());
                EntityId childId = ed.findEntity(Filters.fieldEquals(Parent.class,"parentId",itemId),Parent.class);
                if (isLower){
                    if (!isFolded && childId!=null){
                        hideInsertTipView();
                        return;
                    }
                    show(insertTipLowerLine,item);
                    return;
                }
                if (childId!=null && imgSet.containsId(childId)){
                    hideInsertTipView();
                    return;
                }
                show(insertTipCenterBox,item);
            }

            private UIComponent getItem(float y) {
                ElementProperty elementProperty = panel.get(ElementProperty.class);
                if (elementProperty.isEmpty())return null;
                for (UIComponent item : elementProperty.value) {
                    AABB box = item.get(AABB.class);
                    if (y>=box.getYBottom()) return item;
                }
                return elementProperty.get(elementProperty.size()-1);
            }

            private void hideInsertTipView() {
                if (insertTipView == null) return;
                insertTipView.setVisible(false);
                insertTipView = null;
            }

            private void show(UIComponent insertTipView,UIComponent item){
                float itemCenterY = item.get(AABB.class).getYCenter();
                insertTipView.move(0, itemCenterY - insertTipView.get(AABB.class).getYCenter());
                if (this.insertTipView != insertTipView) {
                    if (this.insertTipView != null) this.insertTipView.setVisible(false);
                    this.insertTipView = insertTipView;
                    this.insertTipView.setVisible(true);
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (dragged!=null){
                    dragged.get(ParentProperty.class).detachFromParent();
                    dragged =null;
                }
                if (insertTipView!=null){
                    UIComponent item = getItem(insertTipView.get(AABB.class).getYCenter());
                    EntityId itemId = ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                    int itemIndex = ed.getComponent(itemId, Index.class).getIndex();
                    if (insertTipView == insertTipCenterBox) {
                        moveComponent(draggedId, itemId, itemIndex + 1);
                    }else {
                        EntityId parentId = ed.getComponent(itemId, Parent.class).getParentId();
                        int insertIndex = itemIndex;
                        if (insertTipView == insertTipLowerLine) insertIndex++;
                        moveComponent(draggedId, parentId, insertIndex);
                    }

                    insertTipView.setVisible(false);
                    insertTipView=null;
                }
                if (draggedId !=null){
                    draggedId =null;
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

        private void moveComponent(EntityId id, EntityId toParentId, int toIndex) {
            Set<EntityId> removedComponentSet = getChildAndGrandChildIdSet(id, new HashSet<>(2), (entityId, extra) -> {
                if (componentSet.containsId(entityId)) return entityId;
                return null;
            });
            int fromMinIndex = ed.getComponent(id, Index.class).getIndex();
            int fromMaxIndex = fromMinIndex;{
                for (EntityId componentId : removedComponentSet) {
                    int index = ed.getComponent(componentId, Index.class).getIndex();
                    if (index > fromMaxIndex) fromMaxIndex = index;
                }
            }
            int size = 1 + fromMaxIndex - fromMinIndex;
            int offset = toIndex - fromMinIndex;
            if (offset==0)return;
            EntityId[] componentIdList = getComponentIdList();
            Map<EntityId, Index> fromIndexMap = new HashMap<>();
            Map<EntityId, Index> toIndexMap = new HashMap<>();
            if (offset < 0) {//上移
                for (int i = toIndex; i < fromMinIndex; i++) {
                    EntityId componentId = componentIdList[i];
                    fromIndexMap.put(componentId, new Index(i));
                    toIndexMap.put(componentId, new Index(i + size));
                }
            } else {//下移
                for (int i = fromMaxIndex + 1; i < toIndex; i++) {
                    EntityId componentId = componentIdList[i];
                    fromIndexMap.put(componentId, new Index(i));
                    toIndexMap.put(componentId, new Index(i - size));
                }
                offset -= size;
            }
            for (int i = fromMinIndex; i <= fromMaxIndex; i++) {
                EntityId componentId = componentIdList[i];
                fromIndexMap.put(componentId, new Index(i));
                toIndexMap.put(componentId, new Index(i + offset));
            }
            Parent fromParent = ed.getComponent(id, Parent.class);

            Record record = new MoveComponentItemOperationRecord(id, fromParent, new Parent(toParentId), fromIndexMap, toIndexMap);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private class MoveComponentItemOperationRecord implements Record{

            private EntityId id;
            private Parent fromParent;
            private Parent toParent;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;

            private MoveComponentItemOperationRecord(EntityId id, Parent fromParent, Parent toParent, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.id = id;
                this.fromParent = fromParent;
                this.toParent = toParent;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public void undo() {
                fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.setComponent(id, fromParent);
            }

            @Override
            public void redo() {
                toIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.setComponent(id, toParent);
            }

        }

    }

    private class MoveImgItemOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private final ColorRGBA translucentColor = new ColorRGBA(1,1,1,0.7f);
            private EntityId draggedId;
            private UIComponent dragged;
            private float offsetToYCenter;
            private UIComponent insertTipView;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.COMPONENT_FOLD_BUTTON, mouse)) return;
                if (selectConverter.isSelect(PSLayout.IMG_ITEM, mouse)){
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    draggedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                }
            }

            @Override
            public void onLeftButtonDragging(MouseEvent mouse) {
                if (draggedId == null) return;
                //更新拖拽效果
                if (dragged ==null){
                    Spatial view = (Spatial) ed.getComponent(draggedId,Item.class).getItem().get(UIComponent.VIEW);
                    dragged =new Vision(view);
                    dragged.get(ColorProperty.class).setColor(translucentColor);
                    dragged.setDepth(insertTipCenterBox.getDepth()-0.01f);
                    getScreen().getState(UIRenderState.class).attachChildToNode(dragged);
                    AABB box = dragged.get(AABB.class);
                    offsetToYCenter =box.getYCenter()-mouse.getPressY();
                }
                AABB draggedBox = dragged.get(AABB.class);
                dragged.move(0, mouse.y - offsetToYCenter - draggedBox.getYCenter());
                AABB panelBox = panel.get(AABB.class);
                float dy = panelBox.getYTop()-draggedBox.getYTop();
                if (dy<0) dragged.move(0,dy);//检查并将拖拽组件限制在面板顶部以下
                else {
                    dy = panelBox.getYBottom()-draggedBox.getYBottom();
                    if (dy>0) dragged.move(0,dy);//检查并将拖拽组件限制在面板底部以上
                }

                //更新插入位置提示效果
                UIComponent item =getItem(draggedBox.getYCenter());
                EntityId itemId = item==null? null : ed.findEntity(Filters.fieldEquals(Item.class,"item",item),Item.class);
                if (itemId==draggedId || itemId==null) return;
                boolean isImg = imgSet.containsId(itemId);
                AABB itemBox = item.get(AABB.class);
                if (isImg){
                    boolean isUpper = draggedBox.getYCenter()>itemBox.getYCenter();
                    if (isUpper) show(insertTipUpperLine,item);
                    else show(insertTipLowerLine,item);
                    return;
                }
                boolean isFolded = PSLayout.GROUP_FOLDED == item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON).get(SwitchEffect.class).getIndexOfCurrentImage();
                if (isFolded){
                    EntityId childId = ed.findEntity(Filters.fieldEquals(Parent.class,"parentId",itemId),Parent.class);
                    if (childId==null || imgSet.containsId(childId)){
                        boolean isNotUpper = draggedBox.getYCenter()<=0.5f*(itemBox.getYTop()+itemBox.getYCenter());
                        boolean isNotLower =  isNotUpper && draggedBox.getYCenter() >= 0.5f * (itemBox.getYBottom() + itemBox.getYCenter());
                        if (isNotUpper && isNotLower) {
                            show(insertTipCenterBox,item);
                            return;
                        }
                    }
                }
                hideInsertTipView();
            }

            private UIComponent getItem(float y) {
                ElementProperty elementProperty = panel.get(ElementProperty.class);
                if (elementProperty.isEmpty())return null;
                for (UIComponent item : elementProperty.value) {
                    AABB box = item.get(AABB.class);
                    if (y>=box.getYBottom()) return item;
                }
                return elementProperty.get(elementProperty.size()-1);
            }

            private void hideInsertTipView() {
                if (insertTipView == null) return;
                insertTipView.setVisible(false);
                insertTipView = null;
            }

            private void show(UIComponent insertTipView,UIComponent item){
                float itemCenterY = item.get(AABB.class).getYCenter();
                insertTipView.move(0, itemCenterY - insertTipView.get(AABB.class).getYCenter());
                if (this.insertTipView != insertTipView) {
                    if (this.insertTipView != null) this.insertTipView.setVisible(false);
                    this.insertTipView = insertTipView;
                    this.insertTipView.setVisible(true);
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                if (dragged!=null){
                    dragged.get(ParentProperty.class).detachFromParent();
                    dragged =null;
                }
                if (insertTipView!=null){
                    UIComponent item = getItem(insertTipView.get(AABB.class).getYCenter());
                    EntityId itemId = ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                    if (insertTipView == insertTipCenterBox) {
                        EntityId oldParentId = ed.getComponent(draggedId,Parent.class).getParentId();
                        int oldIndex = ed.getComponent(draggedId,Index.class).getIndex();
                        moveImg(draggedId,oldParentId,oldIndex,itemId,0);
                    }else {
                        EntityId oldParentId = ed.getComponent(draggedId,Parent.class).getParentId();
                        int oldIndex = ed.getComponent(draggedId,Index.class).getIndex();
                        EntityId newParentId = ed.getComponent(itemId,Parent.class).getParentId();
                        int newIndex = ed.getComponent(itemId, Index.class).getIndex();
                        if (insertTipView == insertTipLowerLine) newIndex++;
                        moveImg(draggedId,oldParentId,oldIndex,newParentId,newIndex);
                    }

                    insertTipView.setVisible(false);
                    insertTipView=null;
                }
                if (draggedId !=null){
                    draggedId =null;
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

        private void moveImg(EntityId id, EntityId fromParentId, int fromIndex, EntityId toParentId, int toIndex) {
            Set<EntityId> fromChildIdSet = getChildIdSet(fromParentId);
            fromChildIdSet.remove(id);
            Map<EntityId, Index> fromIndexMap = new HashMap<>();
            Map<EntityId, Index> toIndexMap = new HashMap<>();
            for (EntityId imgId : fromChildIdSet) {
                Index indexComponent = ed.getComponent(imgId, Index.class);
                int i = indexComponent.getIndex();
                if (i > fromIndex) {
                    fromIndexMap.put(imgId, indexComponent);
                    toIndexMap.put(imgId, new Index(i - 1));
                }
            }
            Set<EntityId> toChildSet;
            if (fromParentId == toParentId) {
                toChildSet = fromChildIdSet;
                if (toIndex > fromIndex) toIndex--;
                for (EntityId imgId : toChildSet) {
                    Index indexComponent = toIndexMap.get(imgId);
                    if (indexComponent == null) indexComponent = ed.getComponent(imgId, Index.class);
                    int i = indexComponent.getIndex();
                    if (i >= toIndex) {
                        fromIndexMap.put(imgId, indexComponent);
                        toIndexMap.put(imgId, new Index(i + 1));
                    }
                }
            } else {
                toChildSet = getChildIdSet(toParentId);
                for (EntityId imgId : toChildSet) {
                    Index indexComponent = ed.getComponent(imgId, Index.class);
                    int i = indexComponent.getIndex();
                    if (i >= toIndex) {
                        fromIndexMap.put(imgId, indexComponent);
                        toIndexMap.put(imgId, new Index(i + 1));
                    }
                }
            }
            fromIndexMap.put(id, new Index(fromIndex));
            toIndexMap.put(id, new Index(toIndex));

            Record record = new MoveImgItemOperationRecord(id, new Parent(fromParentId), new Parent(toParentId), fromIndexMap, toIndexMap);
            getScreen().getState(RecordState.class).addRecord(record);
            record.redo();
        }

        private class MoveImgItemOperationRecord implements Record{

            private EntityId id;
            private Parent fromParent;
            private Parent toParent;
            private Map<EntityId, Index> fromIndex;
            private Map<EntityId, Index> toIndex;

            private MoveImgItemOperationRecord(EntityId id, Parent fromParent, Parent toParent, Map<EntityId, Index> fromIndex, Map<EntityId, Index> toIndex) {
                this.id = id;
                this.fromParent = fromParent;
                this.toParent = toParent;
                this.fromIndex = fromIndex;
                this.toIndex = toIndex;
            }

            @Override
            public void undo() {
                fromIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.setComponent(id, fromParent);
            }

            @Override
            public void redo() {
                toIndex.forEach((entityId, index) -> ed.setComponent(entityId, index));
                ed.setComponent(id, toParent);
            }

        }

    }

    private class RenameComponentOperation extends AbstractOperation {

        private VaryUIComponent selectedItemNameText = new VaryUIComponent();
        private FocusProperty focusProperty = new FocusProperty();
        private Property<Integer> selectFromIndex = new Property<>();
        private Property<Integer> cursorPositionIndex = new Property<>();
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

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
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
                componentNameTextEdit.get(TextEditEffect.class).selectAll();
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
        private PropertyListener<Boolean> focusListener = (oldValue, newValue) -> {
            if (oldValue == newValue) return;
            if (!newValue) selectedItemNameText.setValue(null);
        };

        @Override
        public void initialize() {
            selectedItemNameText.addPropertyListener(selectedItemNameTextChangedListener);
            focusProperty.addPropertyListener(focusListener);
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
            if (focusProperty.isFocus()) {
                componentNameTextEdit.get(TextEditEffect.class).update(tpf);
            }
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
            public void undo() {
                ed.setComponent(id, fromName);
            }

            @Override
            public void redo() {
                ed.setComponent(id, toName);
            }

        }
    }

}