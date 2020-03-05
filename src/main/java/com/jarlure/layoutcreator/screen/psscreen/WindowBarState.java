package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.event.Open;
import com.jarlure.layoutcreator.entitycomponent.event.Quit;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.component.VaryUIComponent;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.input.MouseEvent;
import com.jarlure.ui.input.MouseInputAdapter;
import com.jarlure.ui.input.MouseInputListener;
import com.jarlure.ui.input.extend.ButtonMouseInputListener;
import com.jarlure.ui.system.InputManager;
import com.simsilica.es.EntityData;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;

public class WindowBarState extends AbstractScreenState {

    private Frame frame;
    private EntityData ed;
    private SelectConverter selectConverter;
    private UIComponent windowBar;
    private VaryUIComponent minimizeButton=new VaryUIComponent();
    private VaryUIComponent closeButton=new VaryUIComponent();

    public WindowBarState(Frame frame){
        this.frame=frame;
        //注册拖拽窗体操作
        operations.add(new DragWindowOperation());
        //注册点击最小化按钮操作
        operations.add(new MinimizeWindowOperation());
        //注册点击关闭按钮操作
        operations.add(new CloseWindowOperation());
        //注册拖拽文件至窗口打开文件操作
        operations.add(new DragFileToOpenOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        closeButton.setValue(null);
        minimizeButton.setValue(null);
        windowBar=null;
        selectConverter=null;
        ed=null;
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter=layout.getLayoutNode().get(SelectConverter.class);
        windowBar = layout.getComponent(PSLayout.WINDOW_BAR);
        minimizeButton.setValue(layout.getComponent(PSLayout.WINDOW_MINIMIZE_BUTTON));
        closeButton.setValue(layout.getComponent(PSLayout.WINDOW_CLOSE_BUTTON));
    }

    private class DragWindowOperation extends AbstractOperation {

        private boolean pressed;
        private int locationX,locationY;
        private int cursorX,cursorY;

        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                //监听鼠标按下：如果按住了窗体栏，则记录窗体栏位置和鼠标按下位置
                if (selectConverter.isSelect(windowBar,mouse)){
                    pressed=true;
                    locationX = frame.getLocation().x;
                    locationY = frame.getLocation().y;
                    cursorX = MouseInfo.getPointerInfo().getLocation().x;
                    cursorY = MouseInfo.getPointerInfo().getLocation().y;
                }
            }

            @Override
            public void onLeftButtonRelease(MouseEvent mouse) {
                pressed=false;
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

        @Override
        public void update(float tpf) {
            //计算拖拽
            if (pressed){
                Point cursor = MouseInfo.getPointerInfo().getLocation();
                frame.setLocation(locationX-cursorX+cursor.x,locationY-cursorY+cursor.y);
            }
        }

    }

    private class MinimizeWindowOperation extends AbstractOperation {

        private MouseInputListener listener=new ButtonMouseInputListener(minimizeButton) {

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(minimizeButton,mouse)){
                    frame.setState(Frame.ICONIFIED);
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
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

    private class CloseWindowOperation extends AbstractOperation {

        private MouseInputListener listener=new ButtonMouseInputListener(closeButton) {

            @Override
            public void onLeftButtonClick(MouseEvent mouse) {
                if (selectConverter.isSelect(closeButton,mouse)){
                    ed.setComponent(ed.createEntity(),new Quit());
                }
            }

            @Override
            protected SelectConverter getSelectConverter() {
                return selectConverter;
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

    private class DragFileToOpenOperation extends AbstractOperation {

        private DropTarget dropTarget;
        private DropTargetListener listener = new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        Object obj = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (obj instanceof java.util.List){
                            obj = ((java.util.List)obj).iterator().next();
                            if (obj instanceof File){
                                if (((File)obj).getName().endsWith(".psd")){
                                    ed.setComponent(ed.createEntity(),new Open((File) obj));
                                }
                            }
                        }
                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        @Override
        public void initialize() {
            dropTarget = new DropTarget(frame, DnDConstants.ACTION_COPY_OR_MOVE,listener);
        }

        @Override
        public void cleanup() {
            dropTarget.removeDropTargetListener(listener);
            dropTarget=null;
        }

    }

}