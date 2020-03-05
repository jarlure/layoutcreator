package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.common.*;
import com.jarlure.layoutcreator.entitycomponent.mark.*;
import com.jarlure.layoutcreator.entitycomponent.psd.LayerImgData;
import com.jarlure.layoutcreator.entitycomponent.xml.ImgUrl;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.Picture;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.converter.ScrollConverter;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.HorizontalScrollInputListener;
import com.jarlure.ui.input.extend.VerticalScrollInputListener;
import com.jarlure.ui.property.*;
import com.jarlure.ui.property.common.CustomPropertyListener;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.texture.Texture;
import com.simsilica.es.*;

import java.util.HashSet;
import java.util.Set;

public class SceneState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;
    private VaryUIComponent scene=new VaryUIComponent();
    private VaryUIComponent horizontalScrollBar=new VaryUIComponent();
    private UIComponent scrollLeftArrow;
    private UIComponent scrollHorizontal;
    private UIComponent scrollRightArrow;
    private VaryUIComponent verticalScrollBar=new VaryUIComponent();
    private UIComponent scrollUpArrow;
    private UIComponent scrollVertical;
    private UIComponent scrollDownArrow;
    private VaryUIComponent layerViewNode=new VaryUIComponent();

    public SceneState(){
        //注册显示、更新图层预览窗操作
        operations.add(new ShowLayerViewOperation());
        //注册拖拽图层预览窗操作
        operations.add(new TransformSceneOperation());
        //注册图层预览窗的滚动条操作
        operations.add(new ScrollSceneOperation());
        //注册选中组件时的半透明效果操作（凸显选中组件的图层）
        operations.add(new TranslucentSceneOperation());
    }

    @Override
    protected void initialize() {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        layerViewNode.setValue(new UINode("layerViewNode"));
        layerViewNode.setDepth(-1);
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        layerViewNode.setValue(null);
        scrollDownArrow=null;
        scrollVertical=null;
        scrollUpArrow=null;
        verticalScrollBar.setValue(null);
        scrollRightArrow=null;
        scrollHorizontal=null;
        scrollLeftArrow=null;
        horizontalScrollBar.setValue(null);
        scene.setValue(null);
        selectConverter=null;
        ed=null;
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);
        scene.setValue(layout.getComponent(PSLayout.SCENE));
        horizontalScrollBar.setValue(layout.getComponent(PSLayout.SCENE_HORIZONTAL_SCROLL_BAR));
        scrollLeftArrow = layout.getComponent(PSLayout.SCENE_SCROLL_LEFT_ARROW);
        scrollHorizontal = layout.getComponent(PSLayout.SCENE_HORIZONTAL_SCROLL);
        scrollRightArrow = layout.getComponent(PSLayout.SCENE_SCROLL_RIGHT_ARROW);
        verticalScrollBar.setValue(layout.getComponent(PSLayout.SCENE_VERTICAL_SCROLL_BAR));
        scrollUpArrow = layout.getComponent(PSLayout.SCENE_SCROLL_UP_ARROW);
        scrollVertical = layout.getComponent(PSLayout.SCENE_VERTICAL_SCROLL);
        scrollDownArrow = layout.getComponent(PSLayout.SCENE_SCROLL_DOWN_ARROW);
        horizontalScrollBar.get(ScrollConverter.class).setWindow(scene.get(AABB.class));
        horizontalScrollBar.get(ScrollConverter.class).setObject(layerViewNode.get(AABB.class));
        verticalScrollBar.get(ScrollConverter.class).setWindow(scene.get(AABB.class));
        verticalScrollBar.get(ScrollConverter.class).setObject(layerViewNode.get(AABB.class));
    }

    @Override
    protected void onEnable() {
        app.getStateManager().getState(UIRenderState.class).getNode().get(ChildrenProperty.class).attachChild(layerViewNode);
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (layerViewNode.exist(ParentProperty.class)) layerViewNode.get(ParentProperty.class).detachFromParent();
    }

    private class ShowLayerViewOperation extends AbstractOperation{

        private EntitySet currentImportedSet;
        private EntitySet layerViewVisibleSet;

        @Override
        public void initialize() {
            currentImportedSet=ed.getEntities(Current.class, Imported.class);
            layerViewVisibleSet =ed.getEntities(Layer.class,View.class,Visible.class);
        }

        @Override
        public void cleanup() {
            layerViewVisibleSet.release();
            layerViewVisibleSet=null;
            currentImportedSet.release();
            currentImportedSet=null;
        }

        @Override
        public void update(float tpf) {
            if (currentImportedSet.applyChanges()){
                currentImportedSet.getRemovedEntities().forEach(entity -> {
                    Set<EntityId> removedLayerSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",entity.getId()),CreatedBy.class, Layer.class);
                    removeLayerView(removedLayerSet);
                });
                currentImportedSet.getAddedEntities().forEach(entity -> {
                    Set<EntityId> addedLayerSet = ed.findEntities(Filters.fieldEquals(CreatedBy.class,"creatorId",entity.getId()),CreatedBy.class, Layer.class);
                    addLayerView(addedLayerSet);
                });
            }
            if (layerViewVisibleSet.applyChanges()) {
                layerViewVisibleSet.getChangedEntities().forEach(entity ->
                        entity.get(View.class).getView().setVisible(entity.get(Visible.class).isVisible())
                );
            }
        }

        private void removeLayerView(Set<EntityId> removedLayerSet) {
            if (removedLayerSet.isEmpty())return;
            //移除图层视图的关系
            layerViewNode.get(ChildrenProperty.class).removeAll();
        }

        private void addLayerView(Set<EntityId> addedLayerSet) {
            if (addedLayerSet.isEmpty()) return;
            //创建图层视图
            addedLayerSet.forEach(id -> {
                View view = ed.getComponent(id,View.class);
                if (view==null){
                    view=createLayerView(id);
                    ed.setComponent(id,view);
                }
                ed.setComponent(id,Visible.TRUE);
            });
            //设置图层视图的关系
            UIComponent[] layerView = new UIComponent[addedLayerSet.size()];
            int[] parent = new int[layerView.length];
            addedLayerSet.forEach(layerId->{
                int index = ed.getComponent(layerId, Index.class).getIndex();
                EntityId parentId = ed.getComponent(layerId, Parent.class).getParentId();
                int parentIndex = -1;
                if (parentId != null) parentIndex = ed.getComponent(parentId,Index.class).getIndex();
                UIComponent view = ed.getComponent(layerId,View.class).getView();
                if (view.exist(ChildrenProperty.class)) view.get(ChildrenProperty.class).removeAll();
                layerView[index] = view;
                parent[index] = parentIndex;
            });
            ChildrenProperty childrenProperty = layerViewNode.get(ChildrenProperty.class);
            childrenProperty.removeAll();
            layerViewNode.get(SpatialProperty.class).setLocalTranslation(0, 0, layerViewNode.getDepth());
            layerViewNode.get(SpatialProperty.class).setLocalScale(Vector3f.UNIT_XYZ);
            for (int i = 0; i < layerView.length; i++) {
                int parentIndex = parent[i];
                if (parentIndex == -1) childrenProperty.attachChild(layerView[i]);
                else layerView[parentIndex].get(ChildrenProperty.class).attachChild(layerView[i]);
            }
            //居中显示
            AABB sceneBox = scene.get(AABB.class);
            AABB viewBox = layerViewNode.get(AABB.class);
            float scale = 0.8f * Math.min(sceneBox.getWidth() / viewBox.getWidth(), sceneBox.getHeight() / viewBox.getHeight());
            if (scale < 1f) layerViewNode.scale(scale);
            layerViewNode.move(sceneBox.getXCenter() - viewBox.getXCenter(), sceneBox.getYCenter() - viewBox.getYCenter());
        }

        private View createLayerView(EntityId layerId){
            String type = ed.getComponent(layerId,Type.class).getType();
            String name = ed.getComponent(layerId,Name.class).getName();
            LayerImageData data = ed.getComponent(layerId, LayerImgData.class).getLayerImageData();
            UIComponent view;{
                if (type.equals(PSLayout.LAYER_GROUP_ITEM)) {
                    view = new UINode(name);
                } else {
                    view = new Picture(name,data.getImg());
                    AABB box = view.get(AABB.class);
                    view.move(data.getLeft()-box.getXLeft(),data.getBottom()-box.getYBottom());
                }
            }
            return new View(view);
        }

    }

    private class TransformSceneOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private boolean pressed;

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(scene, mouse)) {
                    pressed = true;
                }
            }

            @Override
            public void onLeftButtonDragging(MouseEvent mouse) {
                if (pressed) {
                    layerViewNode.move(mouse.dx, mouse.dy);
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                pressed = false;
            }

            @Override
            public void onWheelRolling(MouseEvent mouse) {
                if (selectConverter.isSelect(scene, mouse)) {
                    float scale = mouse.dw > 0 ? 1.25f : 0.8f;
                    layerViewNode.scale(scale);
                    scale = 1 - scale;
                    Vector3f worldTranslation = layerViewNode.get(SpatialProperty.class).getWorldTranslation();
                    float x = mouse.x - worldTranslation.getX();
                    float y = mouse.y - worldTranslation.getY();
                    layerViewNode.move(scale * x, scale * y);
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

    }

    private class ScrollSceneOperation extends AbstractOperation {

        private MouseInputListener verticalScrollListener = new VerticalScrollInputListener(verticalScrollBar, scene, layerViewNode) {

            @Override
            public void onWheelRolling(MouseEvent mouse) {
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(scrollUpArrow, mouse))
                    setObjectYTop(scrollVertical.get(AABB.class).getYTop() + 1);
                else if (selectConverter.isSelect(scrollDownArrow, mouse))
                    setObjectYTop(scrollVertical.get(AABB.class).getYTop() - 1);
                else super.onLeftButtonPress(mouse);
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };
        private MouseInputListener horizontalScrollListener = new HorizontalScrollInputListener(horizontalScrollBar, layerViewNode) {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(scrollLeftArrow, mouse))
                    setObjectXLeft(scrollHorizontal.get(AABB.class).getXLeft() - 1);
                else if (selectConverter.isSelect(scrollRightArrow, mouse))
                    setObjectXLeft(scrollHorizontal.get(AABB.class).getXLeft() + 1);
                else super.onLeftButtonPress(mouse);
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
            }

        };
        private CustomPropertyListener layerViewListener = new CustomPropertyListener() {

            @Override
            public void propertyChanged(Enum property, Object oldValue, Object newValue) {
                if (layerViewNode.get(ChildrenProperty.class).isEmpty()) {
                    verticalScrollBar.setVisible(false);
                    horizontalScrollBar.setVisible(false);
                    return;
                }
                if (SpatialProperty.Property.WORLD_SCALE.equals(property)) {
                    updateScrollSize();
                    updateScrollPosition();
                }
                if (SpatialProperty.Property.WORLD_TRANSLATION.equals(property)) {
                    updateScrollPosition();
                }
            }

            private void updateScrollSize() {
                float height = verticalScrollBar.get(ScrollConverter.class).getPercentHeight();
                if (height < 1) {
                    verticalScrollBar.setVisible(true);
                    height *= verticalScrollBar.get(ScrollConverter.class).getFullHeight();
                    scrollVertical.get(AABB.class).setHeight(height);
                } else {
                    verticalScrollBar.setVisible(false);
                }
                float width = horizontalScrollBar.get(ScrollConverter.class).getPercentWidth();
                if (width < 1) {
                    horizontalScrollBar.setVisible(true);
                    width *= horizontalScrollBar.get(ScrollConverter.class).getFullWidth();
                    scrollHorizontal.get(AABB.class).setWidth(width);
                } else {
                    horizontalScrollBar.setVisible(false);
                }
            }

            private void updateScrollPosition() {
                if (verticalScrollBar.isVisible()) {
                    scrollVertical.move(0, verticalScrollBar.get(ScrollConverter.class).getYTop() - scrollVertical.get(AABB.class).getYTop());
                }
                if (horizontalScrollBar.isVisible()) {
                    scrollHorizontal.move(horizontalScrollBar.get(ScrollConverter.class).getXLeft() - scrollHorizontal.get(AABB.class).getXLeft(), 0);
                }
            }

        };

        @Override
        public void initialize() {
            layerViewNode.get(SpatialProperty.class).addPropertyListener(layerViewListener);
        }

        @Override
        public void onEnable() {
            InputManager.add(verticalScrollListener);
            InputManager.add(horizontalScrollListener);
        }

        @Override
        public void onDisable() {
            InputManager.remove(verticalScrollListener);
            InputManager.remove(horizontalScrollListener);
        }

    }

    private class TranslucentSceneOperation extends AbstractOperation {

        private EntitySet componentSelectedSet;
        private Set<UIComponent> translucentViewSet = new HashSet<>();
        private ColorRGBA translucentColor = new ColorRGBA(1, 1, 1, 0.1f);

        @Override
        public void initialize() {
            componentSelectedSet = ed.getEntities(Component.class, Selected.class);
        }

        @Override
        public void cleanup() {
            componentSelectedSet.release();
            componentSelectedSet = null;
        }

        @Override
        public void update(float tpf) {
            if (componentSelectedSet.applyChanges()) {
                if (!componentSelectedSet.getRemovedEntities().isEmpty()){
                    if (!translucentViewSet.isEmpty()) {
                        translucentViewSet.forEach(view->view.get(ColorProperty.class).setColor(ColorRGBA.White));
                        translucentViewSet.clear();
                    }
                }
                if (!componentSelectedSet.getAddedEntities().isEmpty()){
                    EntityId selectedId = componentSelectedSet.iterator().next().getId();
                    Set<UIComponent> selectedView=new HashSet<>(2);
                    getLayerViewSelected(selectedId,selectedView);
                    if (selectedView.isEmpty())return;
                    Set<UIComponent> currentView = new HashSet<>();
                    getLayerView(layerViewNode,currentView);
                    translucentViewSet.addAll(currentView);
                    translucentViewSet.removeAll(selectedView);
                    translucentViewSet.forEach(view->view.get(ColorProperty.class).setColor(translucentColor));
                }
            }
        }

        private void getLayerViewSelected(EntityId parentId, Set<UIComponent> store){
            ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentId), Parent.class).forEach(childId->{
                if (null!=ed.getComponent(childId,Component.class)){
                    getLayerViewSelected(childId,store);
                }else if (null!=ed.getComponent(childId,Img.class)){
                    EntityId urlId = ed.getComponent(childId, ImgUrl.class).getUrl();
                    if (urlId==null)return;
                    View view = ed.getComponent(urlId,View.class);
                    if (view==null)return;
                    getLayerView(view.getView(),store);
                }
            });
        }

        private void getLayerView(UIComponent component,Set<UIComponent> store){
            if (component.exist(ChildrenProperty.class)){
                for (UIComponent child:component.get(ChildrenProperty.class).value){
                    getLayerView(child,store);
                }
            }else store.add(component);
        }

    }

}
