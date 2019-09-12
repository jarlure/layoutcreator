package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.commoninterface.Record;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.Operation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.ui.bean.Direction;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.ScrollConverter;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.effect.TextEditEffect;
import com.jarlure.ui.input.KeyInputListener;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.TextEditKeyInputListener;
import com.jarlure.ui.input.extend.TextEditMouseInputListener;
import com.jarlure.ui.input.extend.VerticalScrollInputListener;
import com.jarlure.ui.property.*;
import com.jarlure.ui.property.common.*;
import com.jarlure.ui.system.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.util.IntMap;
import com.simsilica.es.*;

import java.util.*;

public class LayerPanelState extends AbstractScreenState {

    private static final String MARK_UNIQUE = "√";
    private static final String MARK_REPEAT = "\\$\\d{6}\\w{2}";

    private EntityData ed;
    private EntitySet layerSet;
    private SelectConverter selectConverter;
    private VaryUIComponent scrollBar = new VaryUIComponent();
    private UIComponent scrollUpArrow;
    private UIComponent scroll;
    private UIComponent scrollDownArrow;
    private VaryUIComponent panel = new VaryUIComponent();
    private UIComponent layerGroupItem;
    private UIComponent layerPreviewItem;
    private UIComponent layerTextItem;
    private VaryUIComponent layerNameTextEdit=new VaryUIComponent();
    private int layerGroupIconAndChildrenLayerIconInterval;
    private int layerIconAndLayerNameTextInterval;

    public LayerPanelState() {
        operations.add(new RepeatNameCheckOperation());
        operations.add(new VisibleLayerViewOperation());
        operations.add(new FoldLayerOperation());
        operations.add(new SelectLayerOperation());
        operations.add(new ScrollLayerOperation());
        operations.add(new RenameLayerOperation());
    }

    @Override
    protected void initialize() {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        layerSet = ed.getEntities(Layer.class, Index.class, Name.class, Type.class, Level.class, Parent.class);
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        layerSet.release();
        layerSet = null;
        ed = null;
    }

    @Override
    public void setLayout(Layout layout) {
        this.selectConverter = layout.getLayoutNode().get(SelectConverter.class);
        this.scrollBar.setValue(layout.getComponent(PSLayout.LAYER_SCROLL_BAR));
        this.scrollUpArrow = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_SCROLL_UP_ARROW);
        this.scroll = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_SCROLL);
        this.scrollDownArrow = scrollBar.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_SCROLL_DOWN_ARROW);
        this.panel.setValue(layout.getComponent(PSLayout.LAYER_PANEL));
        this.layerGroupItem=layout.getComponent(PSLayout.LAYER_GROUP_ITEM);
        this.layerPreviewItem=layout.getComponent(PSLayout.LAYER_PREVIEW_ITEM);
        this.layerTextItem=layout.getComponent(PSLayout.LAYER_TEXT_ITEM);
        this.layerNameTextEdit.setValue(layout.getComponent(PSLayout.LAYER_NAME_TEXT_EDIT));

        AABB layerGroupIconBox = layout.getComponent(PSLayout.LAYER_GROUP_ICON).get(AABB.class);
        AABB secondLayerPreviewIconBox = layout.getComponent(PSLayout.SECOND_LAYER_PREVIEW_ICON_POSITION).get(AABB.class);
        AABB layerNameTextBox = layout.getComponent(PSLayout.LAYER_NAME_TEXT).get(AABB.class);
        layerGroupIconAndChildrenLayerIconInterval = (int) (secondLayerPreviewIconBox.getXLeft()-layerGroupIconBox.getXLeft());
        layerIconAndLayerNameTextInterval= (int) (layerNameTextBox.getXLeft()-layerGroupIconBox.getXRight());
    }

    @Override
    public void update(float tpf) {
        if (layerSet.applyChanges()) {
            removeLayerItem(layerSet.getRemovedEntities());
            addLayerItem(layerSet.getAddedEntities());
        }
        super.update(tpf);
    }

    private void removeLayerItem(Set<Entity> removedEntitySet) {
        if (removedEntitySet.isEmpty()) return;
        if (layerSet.isEmpty() || layerSet.size() == layerSet.getAddedEntities().size()) {
            panel.get(ElementProperty.class).removeAll();
        } else {
            removeLayerItemFromPanel(removedEntitySet);
        }
    }

    private void removeLayerItemFromPanel(Set<Entity> removedEntitySet) {
        ElementProperty elementProperty = panel.get(ElementProperty.class);
        int[] minIndex = new int[]{elementProperty.size()};
        removedEntitySet.forEach(entity -> {
            UIComponent item = ed.getComponent(entity.getId(), Item.class).getItem();
            int index = elementProperty.indexOf(item);
            if (index != -1) {
                if (index < minIndex[0]) minIndex[0] = index;
                elementProperty.remove(index);
            }
        });
        List<UIComponent> store = new ArrayList<>(elementProperty.size() - minIndex[0]);
        while (elementProperty.size() > minIndex[0]) {
            UIComponent item = elementProperty.remove(minIndex[0]);
            if (item == null) continue;
            store.add(item);
        }
        for (UIComponent item : store) {
            elementProperty.add(item);
        }
    }

    private void addLayerItem(Set<Entity> addedEntitySet) {
        if (addedEntitySet.isEmpty()) return;
        //创建图层项
        addedEntitySet.forEach(entity -> {
            String type = entity.get(Type.class).getType();
            int level = entity.get(Level.class).getLevel();
            String name = entity.get(Name.class).getName();
            UIComponent item = createItem(name, type, level);
            ed.setComponent(entity.getId(), new Item(item));
        });
        addLayerItemToPanel(panel.get(ElementProperty.class).size(), addedEntitySet);
    }

    private void addLayerItemToPanel(int insert, Set<Entity> addedEntitySet) {
        if (addedEntitySet.isEmpty()) return;
        ElementProperty elementProperty = panel.get(ElementProperty.class);
        IntMap<UIComponent> map = new IntMap<>(addedEntitySet.size() + 1, 1);
        addedEntitySet.forEach(entity -> {
            int index = entity.get(Index.class).getIndex();
            UIComponent item = ed.getComponent(entity.getId(), Item.class).getItem();
            map.put(index, item);
        });
        List<UIComponent> store = new ArrayList<>(elementProperty.size() - insert);
        while (elementProperty.size() > insert) {
            UIComponent item = elementProperty.remove(insert);
            store.add(item);
        }
        for (int i = layerSet.size() - insert; i >= 0; i--) {
            UIComponent item = map.get(i);
            if (item == null) continue;
            elementProperty.add(item);
        }
        for (UIComponent item : store) {
            elementProperty.add(item);
        }
    }

    private UIComponent createItem(String name, String type, int level) {
        UIComponent item;
        ChildrenProperty childrenProperty;
        UIComponent groupFoldButton = null;
        UIComponent selectEffectIcon = null;
        UIComponent typeIcon;
        switch (type) {
            case PSLayout.LAYER_GROUP_ITEM:
                item=layerGroupItem.get(UIFactory.class).create();
                childrenProperty=item.get(ChildrenProperty.class);
                groupFoldButton= childrenProperty.getChildByName(PSLayout.LAYER_GROUP_FOLD_BUTTON);
                typeIcon= childrenProperty.getChildByName(PSLayout.LAYER_GROUP_ICON);
                break;
            case PSLayout.LAYER_PREVIEW_ITEM:
                item=layerPreviewItem.get(UIFactory.class).create();
                childrenProperty=item.get(ChildrenProperty.class);
                selectEffectIcon= childrenProperty.getChildByName(PSLayout.LAYER_PREVIEW_SELECTED_ICON);
                typeIcon= childrenProperty.getChildByName(PSLayout.LAYER_PREVIEW_ICON);
                break;
            case PSLayout.LAYER_TEXT_ITEM:
                item=layerTextItem.get(UIFactory.class).create();
                childrenProperty=item.get(ChildrenProperty.class);
                selectEffectIcon= childrenProperty.getChildByName(PSLayout.LAYER_TEXT_SELECTED_ICON);
                typeIcon= childrenProperty.getChildByName(PSLayout.LAYER_TEXT_ICON);
                break;
            default:
                throw new IllegalStateException();
        }
        float dx = level * layerGroupIconAndChildrenLayerIconInterval;
        if (groupFoldButton != null) groupFoldButton.move(dx, 0);
        if (selectEffectIcon != null) selectEffectIcon.move(dx, 0);
        typeIcon.move(dx, 0);
        if (selectEffectIcon!=null) selectEffectIcon.setVisible(false);

        UIComponent layerNameText = childrenProperty.getChildByName(PSLayout.LAYER_NAME_TEXT);
        Font font = layerNameText.get(FontProperty.class).getFont();
        font.setName(PSLayout.FONT_HEI);
        font.setColor(ColorRGBA.White);
        font.setSize(14);
        AABB nameTextBox = layerNameText.get(AABB.class);
        float layerNameTextWidth=item.get(AABB.class).getXRight()-typeIcon.get(AABB.class).getXRight()-layerIconAndLayerNameTextInterval;
        nameTextBox.setWidth(layerNameTextWidth);
        layerNameText.move(item.get(AABB.class).getXRight()-nameTextBox.getXRight(),0);
        TextProperty textProperty = layerNameText.get(TextProperty.class);
        textProperty.setAlign(Direction.LEFT);
        textProperty.setText(name);

        UIComponent visibleIcon = childrenProperty.getChildByName(PSLayout.LAYER_VISIBLE_ICON);
        visibleIcon.get(SwitchEffect.class).switchTo(PSLayout.VISIBLE_STATE_VISIBLE);

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

    private class RepeatNameCheckOperation implements Operation{

        private EntitySet layerNameSet;

        @Override
        public void initialize() {
            layerNameSet =ed.getEntities(Layer.class,Name.class,Item.class);
        }

        @Override
        public void cleanup() {
            layerNameSet.release();
            layerNameSet=null;
        }

        @Override
        public void update(float tpf) {
            if (layerNameSet.applyChanges()){
                if (!layerNameSet.getAddedEntities().isEmpty()){
                    Entity[] addedLayerNameList = layerNameSet.getAddedEntities().toArray(new Entity[0]);
                    String[] layerName = new String[addedLayerNameList.length];
                    for (int i=0;i<addedLayerNameList.length;i++){
                        layerName[i]=addedLayerNameList[i].get(Name.class).getName();
                    }
                    String[] layerNameMarked = markLayerName(layerName);
                    for (int i=0;i<layerName.length;i++){
                        String newName = layerNameMarked[i];
                        if (newName.equals(layerName[i]))continue;
                        EntityId id = addedLayerNameList[i].getId();
                        ed.setComponent(id,new Name(newName));
                        UIComponent nameText = ed.getComponent(id,Item.class).getItem().get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_NAME_TEXT);
                        Font font=nameText.get(FontProperty.class).getFont();
                        if (newName.endsWith(MARK_UNIQUE)) font.setColor(ColorRGBA.Green);
                        else font.setColor(ColorRGBA.Yellow);
                        nameText.get(TextProperty.class).setText(newName);
                    }
                }
            }
        }

        private String[] markLayerName(String[] layerName){
            String[] result = new String[layerName.length];
            for (int i=0;i<result.length;i++){
                result[i]=layerName[i];
            }
            for (int i=0;i<result.length;i++){
                String name = result[i];
                result[i]=null;
                name = markLayerName(name,result);
                result[i] = name;
            }
            return result;
        }

        private String markLayerName(String name,String[] nameArray){
            String realName = removeMark(name);
            if (realName.equals(name)) name+=MARK_UNIQUE;
            if (!existRepeatName(name,nameArray))return name;
            name=addRepeatMark(realName);
            while (existRepeatName(name,nameArray)){
                name=addRepeatMark(realName);
            }
            return name;
        }

        private String removeMark(String name){
            if (name.endsWith(MARK_UNIQUE))return name.substring(0,name.length()-1);
            return name.split(MARK_REPEAT)[0];
        }

        private boolean existRepeatName(String name,String[] nameArray){
            for (String i:nameArray){
                if (name.equals(i))return true;
            }
            return false;
        }

        private String addRepeatMark(String name){
            return name + '$' + getDateNumber() + getRandomChar() + getRandomChar();
        }

        private String getDateNumber(){
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR)-2000;
            int month = cal.get(Calendar.MONTH)+1;
            int day = cal.get(Calendar.DATE);

            StringBuilder builder=new StringBuilder(6);
            if (year<10)builder.append('0');
            builder.append(year);
            if (month<10)builder.append('0');
            builder.append(month);
            if (day<10)builder.append('0');
            builder.append(day);
            return builder.toString();
        }

        private char getRandomChar(){
            int value = (int)(Math.random()*63);
            if (value<10){
                return (char) ('0'+value);
            }else if (value<36){
                return (char) ('A'+(value-10));
            }else if (value<62){
                return (char) ('a'+(value-36));
            }else {
                return '_';
            }
        }

    }

    private class VisibleLayerViewOperation implements Operation {

        private EntitySet layerVisibleSet;
        private MouseInputListener listener = new MouseInputListener() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.LAYER_VISIBLE_ICON, mouse)) {
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    UIComponent visibleIcon = selectedItem.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_VISIBLE_ICON);
                    setVisible(selectedItem, PSLayout.VISIBLE_STATE_VISIBLE != visibleIcon.get(SwitchEffect.class).getIndexOfCurrentImage());
                }
            }

            private void setVisible(UIComponent item, boolean visible) {
                EntityId id = ed.findEntity(Filters.fieldEquals(Item.class, "item", item), Item.class);
                if (id == null) return;

                Map<EntityId, Visible> data = new HashMap<>();
                if (visible) {
                    data.put(id, Visible.TRUE);
                    //更新父节点状态
                    Parent parent = ed.getComponent(id, Parent.class);
                    while (parent.getParentId() != null) {
                        EntityId parentId = parent.getParentId();
                        if (!ed.getComponent(parentId, Visible.class).isVisible()) {
                            data.put(parentId, Visible.TRUE);
                        }
                        parent = ed.getComponent(parentId, Parent.class);
                    }
                } else {
                    data.put(id, Visible.FALSE);
                }

                Record record = new HideLayerOperationRecord(data);
                getScreen().getState(RecordState.class).addRecord(record);
                record.redo();
            }

        };

        @Override
        public void initialize() {
            layerVisibleSet = ed.getEntities(Layer.class, Item.class, Visible.class, Parent.class);
        }

        @Override
        public void cleanup() {
            layerVisibleSet.release();
            layerVisibleSet = null;
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
            if (layerVisibleSet.applyChanges()) {
                if (!layerVisibleSet.getChangedEntities().isEmpty()) {
                    layerVisibleSet.getChangedEntities().forEach(this::updateVisibleIcon);
                }
            }
        }

        private void updateVisibleIcon(Entity entity) {
            UIComponent item = entity.get(Item.class).getItem();
            UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_VISIBLE_ICON);
            SwitchEffect switchEffect = icon.get(SwitchEffect.class);
            int stateBefore = switchEffect.getIndexOfCurrentImage();
            boolean visible = entity.get(Visible.class).isVisible();
            if (visible) {
                EntityId parentId = entity.get(Parent.class).getParentId();
                Entity parentEntity = parentId == null ? null : layerVisibleSet.getEntity(parentId);
                boolean parentVisible = parentEntity == null || parentEntity.get(Visible.class).isVisible();
                if (parentVisible) switchEffect.switchTo(PSLayout.VISIBLE_STATE_VISIBLE);
                else switchEffect.switchTo(PSLayout.VISIBLE_STATE_DISABLE);
            } else switchEffect.switchTo(PSLayout.VISIBLE_STATE_HIDE);
            int stateAfter = switchEffect.getIndexOfCurrentImage();
            if (stateBefore != stateAfter) {
                ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", entity.getId()), Parent.class)
                        .forEach(child -> updateVisibleIcon(layerVisibleSet.getEntity(child)));
            }
        }

        private class HideLayerOperationRecord implements Record {

            private Map<EntityId, Visible> data;

            private HideLayerOperationRecord(Map<EntityId, Visible> data) {
                this.data = data;
            }

            @Override
            public void undo() {
                data.forEach((id, visible) -> {
                    if (visible.isVisible()) {
                        ed.setComponent(id, Visible.FALSE);
                    } else {
                        ed.setComponent(id, Visible.TRUE);
                    }
                });
            }

            @Override
            public void redo() {
                data.forEach((id, visible) -> ed.setComponent(id, visible));
            }

        }

    }

    private class FoldLayerOperation implements Operation {

        private MouseInputListener listener = new MouseInputListener() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.LAYER_GROUP_FOLD_BUTTON, mouse)) {
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    UIComponent icon = selectedItem.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_GROUP_ICON);
                    SwitchEffect switchEffect = icon.get(SwitchEffect.class);
                    switchEffect.switchToNext();
                    boolean isFolded = PSLayout.GROUP_FOLDED == switchEffect.getIndexOfCurrentImage();
                    EntityId selectedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                    Record record = new FoldLayerOperationRecord(selectedId, isFolded);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputListener instance) {
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

        private class FoldLayerOperationRecord implements Record {

            private EntityId id;
            private boolean folded;

            private FoldLayerOperationRecord(EntityId id, boolean folded) {
                this.id = id;
                this.folded = folded;
            }

            @Override
            public void undo() {
                doFoldOrOpenLayerItem(id, !folded);
            }

            @Override
            public void redo() {
                doFoldOrOpenLayerItem(id, folded);
            }

            private void doFoldOrOpenLayerItem(EntityId id, boolean isFolded) {
                UIComponent item = ed.getComponent(id, Item.class).getItem();
                UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_GROUP_ICON);
                SwitchEffect switchEffect = icon.get(SwitchEffect.class);
                if (isFolded) {
                    switchEffect.switchTo(PSLayout.GROUP_FOLDED);
                    Set<Entity> removedEntitySet = new HashSet<>();
                    findChildrenAndGrandChildrenLayerItem(layerSet.getEntity(id), removedEntitySet);
                    removeLayerItemFromPanel(removedEntitySet);
                } else {
                    switchEffect.switchTo(PSLayout.GROUP_UNFOLDED);
                    Set<Entity> addedEntitySet = new HashSet<>();
                    findOpenedChildrenAndGrandChildrenLayerItem(layerSet.getEntity(id), addedEntitySet);
                    int insert = 1 + panel.get(ElementProperty.class).indexOf(item);
                    addLayerItemToPanel(insert, addedEntitySet);
                }
            }

            private void findChildrenAndGrandChildrenLayerItem(Entity parentEntity, Set<Entity> store) {
                ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentEntity.getId()), Parent.class).forEach(entityId -> {
                    Entity entity = layerSet.getEntity(entityId);
                    store.add(entity);
                    findChildrenAndGrandChildrenLayerItem(entity, store);
                });
            }

            private void findOpenedChildrenAndGrandChildrenLayerItem(Entity parentEntity, Set<Entity> store) {
                ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentEntity.getId()), Parent.class).forEach(entityId -> {
                    Entity entity = layerSet.getEntity(entityId);
                    store.add(entity);
                    UIComponent item = ed.getComponent(entityId, Item.class).getItem();
                    UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_GROUP_ICON);
                    if (icon != null) {
                        if (PSLayout.GROUP_UNFOLDED == icon.get(SwitchEffect.class).getIndexOfCurrentImage()) {
                            findOpenedChildrenAndGrandChildrenLayerItem(entity, store);
                        }
                    }
                });
            }

        }

    }

    private class SelectLayerOperation implements Operation {

        private EntitySet layerSelectedSet;
        private MouseInputListener listener = new MouseInputListener() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.LAYER_VISIBLE_ICON, mouse)) return;
                if (selectConverter.isSelect(PSLayout.LAYER_GROUP_FOLD_BUTTON, mouse)) return;
                if (isItemSelected(selectConverter,mouse)) {
                    UIComponent selectedItem = getMouseSelected(mouse);
                    if (selectedItem == null) return;
                    EntityId selectedId = ed.findEntity(Filters.fieldEquals(Item.class, "item", selectedItem), Item.class);
                    Map<EntityId, Boolean> data = new HashMap<>();
                    if (layerSelectedSet.getEntity(selectedId) == null) {
                        for (Entity entity : layerSelectedSet) {
                            data.put(entity.getId(), false);
                        }
                        data.put(selectedId, true);
                    } else {
                        for (Entity entity : layerSelectedSet) {
                            if (entity.getId().equals(selectedId)) continue;
                            data.put(entity.getId(), false);
                        }
                    }

                    Record record = new SelectLayerOperationRecord(data);
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }
                else if (selectConverter.isSelect(panel,mouse)){
                    layerSelectedSet.forEach(entity -> ed.removeComponent(entity.getId(),Selected.class));
                }
            }

            private boolean isItemSelected(SelectConverter selectConverter,MouseEvent mouse){
                if (selectConverter.isSelect(PSLayout.LAYER_GROUP_ITEM, mouse))return true;
                if (selectConverter.isSelect(PSLayout.LAYER_PREVIEW_ITEM, mouse))return true;
                if (selectConverter.isSelect(PSLayout.LAYER_TEXT_ITEM, mouse))return true;
                return false;
            }

        };

        @Override
        public void initialize() {
            layerSelectedSet = ed.getEntities(Layer.class, Item.class, Selected.class);
        }

        @Override
        public void cleanup() {
            layerSelectedSet.release();
            layerSelectedSet = null;
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
            if (layerSelectedSet.applyChanges()) {
                if (!layerSelectedSet.getRemovedEntities().isEmpty()) {
                    layerSelectedSet.getRemovedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.LAYER_PREVIEW_SELECTED_ICON);
                        if (selectedIcon == null)
                            selectedIcon = childrenProperty.getChildByName(PSLayout.LAYER_TEXT_SELECTED_ICON);
                        if (selectedIcon != null) selectedIcon.setVisible(false);
                        childrenProperty.getChildByName(PSLayout.LAYER_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                    });
                }
                if (!layerSelectedSet.getAddedEntities().isEmpty()) {
                    layerSelectedSet.getAddedEntities().forEach(entity -> {
                        UIComponent item = entity.get(Item.class).getItem();
                        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
                        UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.LAYER_PREVIEW_SELECTED_ICON);
                        if (selectedIcon == null) selectedIcon = childrenProperty.getChildByName(PSLayout.LAYER_TEXT_SELECTED_ICON);
                        if (selectedIcon != null) selectedIcon.setVisible(true);
                        childrenProperty.getChildByName(PSLayout.LAYER_ITEM_BACKGROUND).get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
                    });
                }
            }
        }

        private class SelectLayerOperationRecord implements Record {

            private Map<EntityId, Boolean> data;

            private SelectLayerOperationRecord(Map<EntityId, Boolean> data) {
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

    private class ScrollLayerOperation implements Operation {

        private boolean updateScrollSize = false;
        private boolean updateScrollPosition = false;
        private MouseInputListener listener = new VerticalScrollInputListener(scrollBar, panel, null) {

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

            @Override
            public void onWheelRolling(MouseEvent mouse) {
                super.onWheelRolling(mouse);
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
        private PropertyListener<UIComponent> panelAddedListener = new PropertyListener<UIComponent>() {
            @Override
            public void propertyChanged(UIComponent oldValue, UIComponent newValue) {
                if (oldValue != null) {
                    oldValue.get(ElementProperty.class).removePropertyListener(itemAddListener);
                }
                if (newValue != null) {
                    AABB itemListBox = new AABB();
                    itemListBox.set(newValue);
                    scrollBar.get(ScrollConverter.class).setWindow(newValue.get(AABB.class));
                    scrollBar.get(ScrollConverter.class).setObject(itemListBox);

                    newValue.get(ElementProperty.class).addPropertyListener(itemAddListener);
                }
            }
        };
        private ListPropertyListener<UIComponent> itemAddListener = new ListPropertyListener<UIComponent>() {
            @Override
            public void propertyAdded(int index, UIComponent[] value) {
                propertyAdded(index, value[0]);
            }

            @Override
            public void propertyAdded(int index, UIComponent value) {
                if (index == 0) {
                    UIComponent item = panel.get(ElementProperty.class).get(0);
                    item.get(SpatialProperty.class).addPropertyListener(firstItemListener);
                }
                updateScrollSize = true;
                updateScrollPosition = true;
            }

            @Override
            public void propertyRemoved(int[] index, Object[] value) {
                propertyRemoved(0, null);
            }

            @Override
            public void propertyRemoved(int index, UIComponent value) {
                updateScrollSize = true;
                updateScrollPosition = true;
            }
        };
        private EnumPropertyListener firstItemListener = (property, oldValue, newValue) -> {
            if (scrollBar.get(ScrollConverter.class).getPercentHeight() >= 1) {
                scrollBar.setVisible(false);
                return;
            }
            if (SpatialProperty.Property.LOCAL_TRANSLATION.equals(property)) {
                updateScrollPosition = true;
            }
        };

        @Override
        public void initialize() {
            panel.addPropertyListener(panelAddedListener);
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
            if (updateScrollSize) {
                updateScrollSize = false;
                updateScrollSize();
            }
            if (updateScrollPosition) {
                updateScrollPosition = false;
                updateScrollPosition();
            }
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
            if (scrollBar.isVisible()) {
                scroll.move(0, scrollBar.get(ScrollConverter.class).getYTop() - scroll.get(AABB.class).getYTop());
            }
        }

    }

    private class RenameLayerOperation implements Operation{

        private VaryUIComponent selectedItemNameText=new VaryUIComponent();
        private FocusProperty focusProperty=new FocusProperty();
        private Property<Integer> selectFromIndex=new Property<>();
        private Property<Integer> cursorPositionIndex=new Property<>();
        private MouseInputListener nameTextSelectedListener = new MouseInputListener() {
            @Override
            public void onLeftButtonDoubleClick(MouseEvent mouse) {
                if (selectConverter.isSelect(PSLayout.LAYER_NAME_TEXT,mouse)){
                    UIComponent item = getMouseSelected(mouse);
                    if (item==null)return;
                    UIComponent layerNameText = item.get(ChildrenProperty.class).getChildByName(PSLayout.LAYER_NAME_TEXT);
                    selectedItemNameText.setValue(layerNameText);
                    focusProperty.setFocus(true);
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputListener instance) {
            }
        };
        private PropertyListener<UIComponent> selectedItemNameTextChangedListener = (oldValue, newValue) -> {
            if (oldValue==newValue)return;
            if (oldValue!=null){
                //判断是否需要更新名字、名字是否发生重复
                String text = layerNameTextEdit.get(TextProperty.class).getText();
                String oldText = oldValue.get(TextProperty.class).getText();
                ColorRGBA oldColor = oldValue.get(FontProperty.class).getColor();
                UIComponent item = oldValue.get(ParentProperty.class).getParent();
                EntityId id = ed.findEntity(Filters.fieldEquals(Item.class,"item",item),Item.class);
                Name oldName = ed.getComponent(id,Name.class);
                if (!text.equals(oldName.getName())) {//如果改动了文本内容
                    Record record;
                    String newName = text;
                    if (getOperation(RepeatNameCheckOperation.class).removeMark(text).equals(text)) newName=text+MARK_UNIQUE;
                    EntityId found = ed.findEntity(Filters.fieldEquals(Name.class,"name",newName),Name.class,Layer.class);
                    if (found==null || found==id) {//如果新名字确实是独一无二的 （如果用户只是删除了√标记，就会出现found==id的情况）
                        record=new RenameLayerOperationRecord(oldValue,oldText,oldColor,oldName,newName,ColorRGBA.White,new Name(newName),id);
                    }else{
                        ColorRGBA redColor = new ColorRGBA(1,85f/255,63f/255,1);
                        record=new RenameLayerOperationRecord(oldValue,oldText,oldColor,null,text,redColor,null,null);
                    }
                    getScreen().getState(RecordState.class).addRecord(record);
                    record.redo();
                }else if (!oldColor.equals(ColorRGBA.White)){//将提示色更新为普通色
                    oldValue.get(FontProperty.class).setColor(ColorRGBA.White);
                }
                oldValue.setVisible(true);
                layerNameTextEdit.setVisible(false);
            }
            if (newValue!=null){
                String text = newValue.get(TextProperty.class).getText();
                layerNameTextEdit.get(TextProperty.class).setText(text);
                AABB boxOfNameText = newValue.get(AABB.class);
                AABB boxOfNameTextEdit = layerNameTextEdit.get(AABB.class);
                layerNameTextEdit.move(boxOfNameText.getXLeft()-boxOfNameTextEdit.getXLeft(),boxOfNameText.getYCenter()-boxOfNameTextEdit.getYCenter());
                selectFromIndex.setValue(0);
                cursorPositionIndex.setValue(text.length());
                layerNameTextEdit.get(TextEditEffect.class).selectAll();
                layerNameTextEdit.setVisible(true);
                newValue.setVisible(false);
            }
        };
        private MouseInputListener nameTextEditMouseListener = new TextEditMouseInputListener(layerNameTextEdit) {
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
        private KeyInputListener nameTextEditKeyListener = new TextEditKeyInputListener(layerNameTextEdit) {
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
            if (oldValue==newValue)return;
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
            if (focusProperty.isFocus()){
                layerNameTextEdit.get(TextEditEffect.class).update(tpf);
            }
        }

        private class RenameLayerOperationRecord implements Record{

            private EntityId id;
            private UIComponent layerNameText;
            private String fromText;
            private ColorRGBA fromColor;
            private Name fromName;
            private String toText;
            private ColorRGBA toColor;
            private Name toName;

            private RenameLayerOperationRecord(UIComponent layerNameText,String fromText,ColorRGBA fromColor,Name fromName,String toText,ColorRGBA toColor,Name toName,EntityId id){
                this.id=id;
                this.layerNameText=layerNameText;
                this.fromText=fromText;
                this.fromColor=fromColor;
                this.fromName=fromName;
                this.toText=toText;
                this.toColor=toColor;
                this.toName=toName;
            }

            @Override
            public void undo() {
                if (fromText.equals(toText)){
                    layerNameText.get(FontProperty.class).setColor(fromColor);
                }else{
                    layerNameText.get(FontProperty.class).getFont().setColor(fromColor);
                    layerNameText.get(TextProperty.class).setText(fromText);
                }
                if (fromName!=null) ed.setComponent(id,fromName);
            }

            @Override
            public void redo() {
                if (fromText.equals(toText)){
                    layerNameText.get(FontProperty.class).setColor(toColor);
                }else{
                    layerNameText.get(FontProperty.class).getFont().setColor(toColor);
                    layerNameText.get(TextProperty.class).setText(toText);
                }
                if (toName!=null) ed.setComponent(id,toName);
            }
        }

    }

}
