package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.*;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.converter.ScrollConverter;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.HorizontalScrollInputListener;
import com.jarlure.ui.input.extend.VerticalScrollInputListener;
import com.jarlure.ui.property.AABB;
import com.jarlure.ui.property.ChildrenProperty;
import com.jarlure.ui.property.ParentProperty;
import com.jarlure.ui.property.SpatialProperty;
import com.jarlure.ui.property.common.CustomPropertyListener;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.simsilica.es.*;

import java.util.HashSet;
import java.util.Set;

public class SceneState extends AbstractScreenState {

    private EntityData ed;
    private EntitySet layerSet;
    private EntitySet layerVisibleSet;
    private SelectConverter selectConverter;
    private VaryUIComponent scene = new VaryUIComponent();
    private VaryUIComponent horizontalScrollBar = new VaryUIComponent();
    private UIComponent scrollLeftArrow;
    private UIComponent scrollHorizontal;
    private UIComponent scrollRightArrow;
    private VaryUIComponent verticalScrollBar = new VaryUIComponent();
    private UIComponent scrollUpArrow;
    private UIComponent scrollVertical;
    private UIComponent scrollDownArrow;
    private VaryUIComponent layerViewNode = new VaryUIComponent();

    public SceneState() {
        operations.add(new TransformSceneOperation());
        operations.add(new ScrollSceneOperation());
        operations.add(new TranslucentSceneOperation());
    }

    @Override
    protected void initialize() {
        ed = app.getStateManager().getState(EntityDataState.class).getEntityData();
        layerSet = ed.getEntities(Layer.class, Index.class, Name.class, Type.class, LayerImgData.class, Parent.class);
        layerVisibleSet = ed.getEntities(Layer.class, View.class, Visible.class);
        layerViewNode.setValue(new UINode("layerViewNode"));
        layerViewNode.setDepth(-1);
        super.initialize();
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

    @Override
    public void cleanup() {
        super.cleanup();
        layerViewNode = null;
        layerSet.release();
        layerVisibleSet.release();
        layerSet = null;
        layerVisibleSet = null;
        ed = null;
    }

    @Override
    public void update(float tpf) {
        if (layerSet.applyChanges()) {
            removeLayerView(layerSet.getRemovedEntities());
            addLayerView(layerSet.getAddedEntities());
        }
        if (layerVisibleSet.applyChanges()) {
            layerVisibleSet.getChangedEntities().forEach(entity ->
                    entity.get(View.class).getView().setVisible(entity.get(Visible.class).isVisible())
            );
        }
        super.update(tpf);
    }

    private void removeLayerView(Set<Entity> removedEntitySet) {
        if (removedEntitySet.isEmpty()) return;
        if (layerSet.isEmpty() || layerSet.size() == layerSet.getAddedEntities().size()) {
            layerViewNode.get(ChildrenProperty.class).removeAll();
        } else {
            removedEntitySet.forEach(entity -> {
                View layerView = ed.getComponent(entity.getId(), View.class);
                if (layerView != null) {
                    UIComponent component = layerView.getView();
                    if (component != null && component.exist(ParentProperty.class)) {
                        component.get(ParentProperty.class).detachFromParent();
                    }
                }
            });
        }
    }

    private void addLayerView(Set<Entity> addedEntitySet) {
        if (addedEntitySet.isEmpty()) return;
        //创建图层视图
        UIFactory factory = new DefaultUIFactory();
        addedEntitySet.forEach(entity -> {
            String type = entity.get(Type.class).getType();
            String name = entity.get(Name.class).getName();
            LayerImageData data = entity.get(LayerImgData.class).getLayerImageData();
            UIComponent view;
            {
                if (type.equals(PSLayout.LAYER_GROUP_ITEM)) {
                    view = factory.create(DefaultUIFactory.Node, name, new UIComponent[0]);
                } else {
                    view = factory.create(DefaultUIFactory.Picture, name, data);
                }
            }
            ed.setComponents(entity.getId(), new View(view), Visible.TRUE);
        });
        //设置图层视图的关系
        UIComponent[] layerView = new UIComponent[layerSet.size()];
        int[] parent = new int[layerView.length];
        layerSet.forEach(entity -> {
            int index = entity.get(Index.class).getIndex();
            EntityId parentId = entity.get(Parent.class).getParentId();
            int parentIndex = -1;
            if (parentId != null) parentIndex = layerSet.getEntity(parentId).get(Index.class).getIndex();
            UIComponent view = ed.getComponent(entity.getId(), View.class).getView();
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
        private Set<Geometry> translucentViewSet = new HashSet<>();
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
                if (!componentSelectedSet.getRemovedEntities().isEmpty()) {
                    if (!translucentViewSet.isEmpty()) {
                        translucentViewSet.forEach(view -> view.getMaterial().setColor("Color", ColorRGBA.White));
                        translucentViewSet.clear();
                    }
                }
                if (!componentSelectedSet.getAddedEntities().isEmpty()) {
                    componentSelectedSet.getAddedEntities().stream().findFirst().ifPresent(selectedComponentEntity -> {
                        Set<EntityId> store = new HashSet<>();
                        findChildrenAndGrandChildrenImgUrl(selectedComponentEntity.getId(), store);
                        if (!store.isEmpty()) {
                            Set<Geometry> opacityViewSet = new HashSet<>(store.size());
                            store.forEach(url -> {
                                UIComponent view = layerVisibleSet.getEntity(url).get(View.class).getView();
                                findChildrenAndGrandChildrenGeometry(view, opacityViewSet);
                            });
                            if (!opacityViewSet.isEmpty()) {
                                layerVisibleSet.forEach(entity -> {
                                    Spatial view = (Spatial) entity.get(View.class).getView().get(UIComponent.VIEW);
                                    if (view instanceof Geometry && !opacityViewSet.contains(view)) {
                                        ((Geometry) view).getMaterial().setColor("Color", translucentColor);
                                        translucentViewSet.add((Geometry) view);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }

        private void findChildrenAndGrandChildrenImgUrl(EntityId parentId, Set<EntityId> store) {
            ed.findEntities(Filters.fieldEquals(Parent.class, "parentId", parentId), Parent.class).forEach(entityId -> {
                Img img = ed.getComponent(entityId, Img.class);
                if (img == null) findChildrenAndGrandChildrenImgUrl(entityId, store);
                else {
                    EntityId urlId = ed.getComponent(entityId, ImgUrl.class).getUrl();
                    if (urlId != null) store.add(urlId);
                }
            });
        }

        private void findChildrenAndGrandChildrenGeometry(UIComponent component, Set<Geometry> store) {
            if (component.exist(ChildrenProperty.class)) {
                for (UIComponent child : component.get(ChildrenProperty.class).value) {
                    findChildrenAndGrandChildrenGeometry(child, store);
                }
            } else {
                Spatial view = (Spatial) component.get(UIComponent.VIEW);
                if (view instanceof Geometry) store.add((Geometry) view);
            }
        }

    }

}
