package com.jarlure.layoutcreator.util;

import com.jarlure.layoutcreator.entitycomponent.common.*;
import com.jarlure.layoutcreator.entitycomponent.mark.Component;
import com.jarlure.layoutcreator.entitycomponent.mark.Current;
import com.jarlure.layoutcreator.entitycomponent.mark.Img;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.entitycomponent.xml.ComponentLink;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgPos;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.lambda.ObjectFunction2Obj;
import com.jarlure.ui.bean.Direction;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.property.*;
import com.jme3.math.ColorRGBA;
import com.jme3.util.IntMap;
import com.simsilica.es.*;

import java.util.*;

public class ComponentPanelHelper {

    public static int getLevel(EntityId id,EntityData ed) {
        Parent parent = ed.getComponent(id, Parent.class);
        EntityId parentId;
        int level=0;
        while (parent!=null){
            level++;
            parentId=parent.getParentId();
            parent=ed.getComponent(parentId,Parent.class);
        }
        return level;
    }

    public static UIComponent createItem(String name, String type, int level, UIComponent componentItem, UIComponent imgItem, float componentIconAndChildrenIconInterval, float componentIconAndComponentNameTextInterval) {
        UIComponent item;
        ChildrenProperty childrenProperty;
        UIComponent groupFoldButton = null;
        UIComponent selectEffectIcon = null;
        UIComponent typeIcon;
        UIComponent componentNameText;
        switch (type) {
            case PSLayout.COMPONENT_ITEM:
                item = componentItem.get(UIFactory.class).create();
                childrenProperty = item.get(ChildrenProperty.class);
                groupFoldButton = childrenProperty.getChildByName(PSLayout.COMPONENT_FOLD_BUTTON);
                typeIcon = childrenProperty.getChildByName(PSLayout.COMPONENT_ICON);
                componentNameText = childrenProperty.getChildByName(PSLayout.COMPONENT_NAME_TEXT);
                break;
            case PSLayout.IMG_ITEM:
                item = imgItem.get(UIFactory.class).create();
                childrenProperty = item.get(ChildrenProperty.class);
                selectEffectIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                typeIcon = childrenProperty.getChildByName(PSLayout.IMG_ICON);
                componentNameText = childrenProperty.getChildByName(PSLayout.IMG_NAME_TEXT);
                break;
            default:
                throw new IllegalStateException();
        }
        float dx = level * componentIconAndChildrenIconInterval;
        if (groupFoldButton != null) groupFoldButton.move(dx, 0);
        if (selectEffectIcon != null) selectEffectIcon.move(dx, 0);
        typeIcon.move(dx, 0);
        if (selectEffectIcon != null) selectEffectIcon.setVisible(false);

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

    public static void addAllItemToPanel(UIComponent panel, EntityData ed){
        EntityId[] componentArray = getCurrentComponentArray(ed);
        ElementProperty elementProperty = panel.get(ElementProperty.class);
        elementProperty.removeAll();
        EntityId componentId;
        UIComponent item;
        Set<EntityId> imgSet;
        IntMap<UIComponent> imgItemMap = new IntMap<>();
        for (int i=0;i<componentArray.length;i++){
            //添加组件
            componentId = componentArray[i];
            item = ed.getComponent(componentId, Item.class).getItem();
            elementProperty.add(item);
            //寻找图片
            imgSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",componentId),Parent.class, Img.class);
            if (imgSet.isEmpty())continue;
            //图片排序
            imgItemMap.clear();
            for (EntityId imgId:imgSet){
                int index = ed.getComponent(imgId,Index.class).getIndex();
                item = ed.getComponent(imgId,Item.class).getItem();
                imgItemMap.put(index,item);
            }
            //添加图片
            for (int j=0;j<imgItemMap.size();j++){
                item=imgItemMap.get(j);
                elementProperty.add(item);
            }
        }
    }

    public static void updateItem(UIComponent item,int fromLevel,int toLevel,float componentIconAndChildrenIconInterval, float componentIconAndComponentNameTextInterval){
        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
        UIComponent componentNameText = childrenProperty.findChildByName(PSLayout.COMPONENT_NAME_TEXT,PSLayout.IMG_NAME_TEXT);
        UIComponent groupFoldButton = null;
        UIComponent selectEffectIcon = null;
        UIComponent typeIcon;{
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

    public static void updateItem(UIComponent item, String toName) {
        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
        UIComponent componentNameText = childrenProperty.findChildByName(PSLayout.COMPONENT_NAME_TEXT,PSLayout.IMG_NAME_TEXT);
        componentNameText.get(TextProperty.class).setText(toName);
    }

    public static void updatePanelForItemChanged(UIComponent panel, EntityData ed){
        EntityId[] componentArray = getCurrentComponentArray(ed);
        List<UIComponent> itemList = new ArrayList<>();
        EntityId componentId;
        UIComponent item;
        boolean visible;
        Set<EntityId> imgSet;
        IntMap<EntityId> imgIdMap = new IntMap<>();
        for (int i=0;i<componentArray.length;i++){
            //添加组件
            componentId = componentArray[i];
            item = ed.getComponent(componentId, Item.class).getItem();
            visible = ed.getComponent(componentId,Visible.class).isVisible();
            if (visible) itemList.add(item);
            //寻找图片
            imgSet = ed.findEntities(Filters.fieldEquals(Parent.class,"parentId",componentId),Parent.class, Img.class);
            if (imgSet.isEmpty())continue;
            //图片排序
            imgIdMap.clear();
            for (EntityId imgId:imgSet){
                int index = ed.getComponent(imgId,Index.class).getIndex();
                imgIdMap.put(index,imgId);
            }
            //添加图片
            for (int j=0;j<imgIdMap.size();j++){
                EntityId imgId=imgIdMap.get(j);
                if (!ed.getComponent(imgId,Visible.class).isVisible())continue;
                item=ed.getComponent(imgId,Item.class).getItem();
                itemList.add(item);
            }
        }
        ElementProperty elementProperty = panel.get(ElementProperty.class);
        if (elementProperty.size() > itemList.size()) {
            elementProperty.remove(itemList.size(), elementProperty.size() - 1);
        }
        for (int i=0;i<itemList.size();i++){
            item = itemList.get(i);
            if (i<elementProperty.size()){
                if (elementProperty.get(i)==item)continue;
                elementProperty.remove(i,elementProperty.size()-1);
            }
            elementProperty.add(item);
        }

    }

    public static void doFoldOrOpenItem(EntityId theId, boolean isFolded, EntityData ed) {
        UIComponent item = ed.getComponent(theId, Item.class).getItem();
        UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON);
        SwitchEffect switchEffect = icon.get(SwitchEffect.class);
        if (isFolded) {
            switchEffect.switchTo(PSLayout.GROUP_FOLDED);
            Set<EntityId> idSet = getChildAndGrandChildIdSet(theId,ed, new HashSet<>(2), null);
            if (idSet.isEmpty())return;
            for (EntityId childId : idSet) {
                ed.setComponent(childId, Visible.FALSE);
            }
        } else {
            switchEffect.switchTo(PSLayout.GROUP_UNFOLDED);
            Set<EntityId> idSet = getChildAndGrandChildIdSet(theId, ed, new HashSet<>(2), (id, parentId) -> {
                if (isFolded(parentId, ed)) return null;
                return id;
            });
            if (idSet.isEmpty())return;
            for (EntityId childId : idSet) {
                ed.setComponent(childId, Visible.TRUE);
            }
        }
    }

    public static boolean isFolded(EntityId id,EntityData ed) {
        UIComponent item = ed.getComponent(id, Item.class).getItem();
        UIComponent icon = item.get(ChildrenProperty.class).getChildByName(PSLayout.COMPONENT_ICON);
        SwitchEffect switchEffect = icon.get(SwitchEffect.class);
        return switchEffect.getIndexOfCurrentImage() == PSLayout.GROUP_FOLDED;
    }

    public static Set<EntityId> getChildAndGrandChildIdSet(EntityId parentId,EntityData ed, Set<EntityId> store, ObjectFunction2Obj<EntityId,EntityId,EntityId> filter) {
        if (store == null) store = new HashSet<>();
        Set<EntityId> idSet = ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentId), Parent.class);
        if (idSet.isEmpty()) return store;
        if (filter == null) {
            store.addAll(idSet);
        } else {
            for (EntityId id : idSet) {
                id = filter.apply(id, parentId);
                if (id == null) continue;
                store.add(id);
            }
        }
        for (EntityId id : idSet) {
            getChildAndGrandChildIdSet(id, ed, store, filter);
        }
        return store;
    }

    public static void doSelectItem(EntityId theId, boolean isSelected, EntityData ed) {
        UIComponent item = ed.getComponent(theId,Item.class).getItem();
        ChildrenProperty childrenProperty = item.get(ChildrenProperty.class);
        if (isSelected){
            ed.setComponent(theId,new Selected());
            UIComponent background;
            if (null!=ed.getComponent(theId,Img.class)){
                background = childrenProperty.getChildByName(PSLayout.IMG_ITEM_BACKGROUND);
                UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                selectedIcon.setVisible(true);
            }else{
                background = childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND);
            }
            background.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_SELECTED);
        }else{
            ed.removeComponent(theId,Selected.class);
            UIComponent background;
            if (null!=ed.getComponent(theId,Img.class)){
                background = childrenProperty.getChildByName(PSLayout.IMG_ITEM_BACKGROUND);
                UIComponent selectedIcon = childrenProperty.getChildByName(PSLayout.IMG_SELECTED_ICON);
                selectedIcon.setVisible(false);
            }else{
                background = childrenProperty.getChildByName(PSLayout.COMPONENT_ITEM_BACKGROUND);
            }
            background.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
        }
    }

    public static EntityId[] getCurrentComponentArray(EntityData ed) {
        EntityId importedId = ed.findEntity(null, Current.class, Imported.class);
        if (importedId == null) return new EntityId[0];
        Set<EntityId> componentSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class, "creatorId", importedId), CreatedBy.class, Component.class, Item.class);
        EntityId[] componentArray = new EntityId[componentSet.size()];
        componentSet.forEach(componentId -> {
            int index = ed.getComponent(componentId, Index.class).getIndex();
            componentArray[index] = componentId;
        });
        return componentArray;
    }

    public static EntityId[] getChildArray(EntityId parentId,EntityData ed){
        Set<EntityId> idSet = ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentId), Parent.class);
        EntityId[] childArray = new EntityId[idSet.size()];
        for (EntityId id:idSet){
            int index = ed.getComponent(id,Index.class).getIndex();
            childArray[index]=id;
        }
        return childArray;
    }

    public static EntityComponent[] getComponents(EntityId id, EntityData ed) {
        boolean isImg = null != ed.getComponent(id, Img.class);
        EntityComponent createdBy = ed.getComponent(id,CreatedBy.class);
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
                createdBy,typeMark, componentLink, imgPos, imgUrl, index, componentType, name, parent, item, visible, level, selected
        };
        return Arrays.stream(components).filter(Objects::nonNull).toArray(EntityComponent[]::new);
    }

    public static EntityId[] moveImg(EntityId[] imgArray,int fromIndex,int toIndex){
        EntityId[] result = new EntityId[imgArray.length];
        System.arraycopy(imgArray,0,result,0,imgArray.length);
        if (fromIndex==toIndex)return result;
        if (fromIndex<toIndex){
            System.arraycopy(imgArray,fromIndex+1,result,fromIndex,toIndex-fromIndex-1);
            result[toIndex-1]=imgArray[fromIndex];
        }else{
            System.arraycopy(imgArray,toIndex,result,toIndex+1,fromIndex-toIndex);
            result[toIndex]=imgArray[fromIndex];
        }
        return result;
    }

    public static EntityId[] moveComponent(EntityId[] componentArray,int fromIndex,int toIndex,Parent fromParent,Parent toParent,EntityData ed){
        EntityId[] result = new EntityId[componentArray.length];
        System.arraycopy(componentArray,0,result,0,componentArray.length);
        if (fromIndex==toIndex)return result;
        EntityId id = componentArray[fromIndex];
        Set<EntityId> idSet = getChildAndGrandChildIdSet(id,ed,new HashSet<>(2),(childId,parentId)->{
            if (null == ed.getComponent(childId,Component.class)) return null;
            return childId;
        });
        if (fromIndex<toIndex){
            System.arraycopy(componentArray,fromIndex+1+idSet.size(),result,fromIndex,toIndex-fromIndex-1-idSet.size());
            System.arraycopy(componentArray,fromIndex,result,toIndex-1-idSet.size(),1+idSet.size());
        }else{
            System.arraycopy(componentArray,fromIndex,result,toIndex,1+idSet.size());
            System.arraycopy(componentArray,toIndex,result,toIndex+1+idSet.size(),fromIndex-toIndex);
        }
        return result;
    }

}
