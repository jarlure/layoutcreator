package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.entitycomponent.event.Close;
import com.jarlure.layoutcreator.entitycomponent.event.Open;
import com.jarlure.layoutcreator.entitycomponent.event.Quit;
import com.jarlure.layoutcreator.entitycomponent.event.Save;
import com.jarlure.layoutcreator.entitycomponent.mark.Current;
import com.jarlure.layoutcreator.entitycomponent.mark.Imported;
import com.jarlure.layoutcreator.entitycomponent.imported.PsdFile;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.entitycomponent.Task;
import com.jarlure.project.lambda.VoidFunction1Obj;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.AbstractScreenState;
import com.jarlure.project.screen.screenstate.operation.AbstractOperation;
import com.jarlure.project.state.EntityDataState;
import com.jarlure.project.state.RecordState;
import com.jarlure.project.util.filechooser.FileChooser;
import com.jarlure.project.util.filechooser.filter.FormatFileFilter;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.converter.SelectConverter;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.input.*;
import com.jarlure.ui.property.common.CustomPropertyListener;
import com.jarlure.ui.system.InputManager;
import com.jme3.input.KeyInput;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MenuState extends AbstractScreenState {

    private EntityData ed;
    private SelectConverter selectConverter;

    private List<UIComponent> menuList;
    private List<UIComponent> submenuSelectedList;
    private UIComponent fileOpenSelected;
    private UIComponent fileCloseSelected;
    private UIComponent fileSaveSelected;
    private UIComponent fileSaveToSelected;
    private UIComponent fileQuitSelected;
    private UIComponent editRedoSelected;
    private UIComponent editUndoSelected;

    public MenuState(){
        //注册菜单、子菜单的选中效果
        operations.add(new SelectEffectOperation());
        //注册"文件-打开"操作
        operations.add(new OpenFileOperation());
        //注册"文件-保存"操作
        operations.add(new SaveFileOperation());
        //注册"文件-另存为"操作
        operations.add(new SaveToFileOperation());
        //注册"文件-关闭"操作
        operations.add(new CloseFileOperation());
        //注册"文件-退出"操作
        operations.add(new QuitFileOperation());
        //注册"编辑-前进一步""后退一步"操作
        operations.add(new UndoRedoEditOperation());
    }

    @Override
    protected void initialize() {
        ed = getScreen().getState(EntityDataState.class).getEntityData();
        super.initialize();
    }

    @Override
    public void setLayout(Layout layout) {
        selectConverter = layout.getLayoutNode().get(SelectConverter.class);

        menuList = new ArrayList<>();
        menuList.add(layout.getComponent(PSLayout.MENU_FILE));
        menuList.add(layout.getComponent(PSLayout.MENU_EDIT));
        menuList.add(layout.getComponent(PSLayout.MENU_IMAGE));
        menuList.add(layout.getComponent(PSLayout.MENU_LAYER));
        menuList.add(layout.getComponent(PSLayout.MENU_TYPE));
        menuList.add(layout.getComponent(PSLayout.MENU_SELECT));
        menuList.add(layout.getComponent(PSLayout.MENU_FILTER));
        menuList.add(layout.getComponent(PSLayout.MENU_3D));
        menuList.add(layout.getComponent(PSLayout.MENU_VIEW));
        menuList.add(layout.getComponent(PSLayout.MENU_WINDOW));
        menuList.add(layout.getComponent(PSLayout.MENU_HELP));
        submenuSelectedList=new ArrayList<>();
        fileOpenSelected=layout.getComponent(PSLayout.SUBMENU_FILE_OPEN);
        fileCloseSelected=layout.getComponent(PSLayout.SUBMENU_FILE_CLOSE);
        fileSaveSelected=layout.getComponent(PSLayout.SUBMENU_FILE_SAVE);
        fileSaveToSelected=layout.getComponent(PSLayout.SUBMENU_FILE_SAVE_TO);
        fileQuitSelected=layout.getComponent(PSLayout.SUBMENU_FILE_QUIT);
        editRedoSelected=layout.getComponent(PSLayout.SUBMENU_EDIT_REDO);
        editUndoSelected=layout.getComponent(PSLayout.SUBMENU_EDIT_UNDO);
        submenuSelectedList.add(fileOpenSelected);
        submenuSelectedList.add(fileCloseSelected);
        submenuSelectedList.add(fileSaveSelected);
        submenuSelectedList.add(fileSaveToSelected);
        submenuSelectedList.add(fileQuitSelected);
        submenuSelectedList.add(editRedoSelected);
        submenuSelectedList.add(editUndoSelected);
    }

    private class SelectEffectOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            private UIComponent menuOnMoved;
            private UIComponent menuOnSelected;
            private UIComponent submenuSelectedOnMoved;

            @Override
            public void onMove(MouseEvent mouse) {
                UIComponent lastOnMoved=menuOnMoved;
                UIComponent currentOnMoved=select(menuList,selectConverter,mouse);
                menuOnMoved=currentOnMoved;
                if (menuOnSelected==null){
                    onMove(currentOnMoved,lastOnMoved);
                }
                else if (currentOnMoved!=null){
                    UIComponent lastOnSelected = menuOnSelected;
                    UIComponent currentOnSelected = currentOnMoved;
                    menuOnSelected=currentOnSelected;
                    onPress(currentOnSelected,lastOnSelected);
                }
                else{
                    lastOnMoved = submenuSelectedOnMoved;
                    currentOnMoved =select(submenuSelectedList,selectConverter,mouse);
                    submenuSelectedOnMoved=currentOnMoved;
                    onMove(currentOnMoved,lastOnMoved);
                }
            }

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                UIComponent lastOnSelected = menuOnSelected;
                UIComponent currentOnSelected = menuOnMoved;
                if (currentOnSelected!=null && currentOnSelected==lastOnSelected){
                    menuOnSelected=null;
                    currentOnSelected.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_MOVE_ON);
                }else{
                    menuOnSelected=currentOnSelected;
                    onPress(currentOnSelected,lastOnSelected);
                }
                if (submenuSelectedOnMoved!=null){
                    submenuSelectedOnMoved.get(SwitchEffect.class).switchTo(PSLayout.SELECT_STATE_NOTHING);
                }
            }

            private UIComponent select(List<UIComponent> list, SelectConverter selectConverter, MouseEvent mouse){
                for (UIComponent component:list){
                    if (selectConverter.isSelect(component,mouse)){
                        return component;
                    }
                }
                return null;
            }

            private void onMove(UIComponent currentOnMoved,UIComponent lastOnMoved){
                if (currentOnMoved==lastOnMoved)return;
                if (lastOnMoved!=null){
                    lastOnMoved.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                }
                if (currentOnMoved!=null){
                    currentOnMoved.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_MOVE_ON);
                }
            }

            private void onPress(UIComponent currentOnPressed, UIComponent lastOnPressed){
                if (currentOnPressed==lastOnPressed) return;
                if (lastOnPressed!=null){
                    lastOnPressed.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_NOTHING);
                }
                if (currentOnPressed!=null){
                    currentOnPressed.get(SwitchEffect.class).switchTo(PSLayout.BUTTON_STATE_PRESSED);
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

    private class OpenFileOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {

            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileOpenSelected,mouse)){
                    ed.setComponent(ed.createEntity(),new Task(showFileChooserDialog(path->{
                        if (path==null) return;
                        File file = new File(path);
                        if (!file.exists()) return;
                        if (!file.isFile()) return;
                        if (!file.getName().endsWith(".psd"))return;
                        ed.setComponent(ed.createEntity(),new Open(file));
                    })));
                }
            }

            private Runnable showFileChooserDialog(VoidFunction1Obj<String> callback){
                return () -> {
                    FileChooser fileChooser = new FileChooser();
                    FormatFileFilter formatFileFilter = new FormatFileFilter("psd");
                    fileChooser.addFilter(formatFileFilter);
                    fileChooser.open("打开",System.getProperty("user.dir"));
                    String path = fileChooser.getFilePath();
                    callback.apply(path);
                };
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

    private class SaveFileOperation extends AbstractOperation {

        private EntitySet currentImportedSet;
        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileSaveSelected,mouse)){
                    currentImportedSet.forEach(entity -> ed.setComponent(ed.createEntity(),new Save(entity.getId())));
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            currentImportedSet =ed.getEntities(Current.class,Imported.class);
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
            if (currentImportedSet.applyChanges()){
                fileSaveSelected.setVisible(!currentImportedSet.isEmpty());
            }
        }

    }

    private class SaveToFileOperation extends AbstractOperation {

        private EntitySet currentImportedSet;
        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileSaveToSelected, mouse)) {
                    currentImportedSet.forEach(entity -> {
                        ed.setComponent(ed.createEntity(),new Task(showFileChooserDialog(path->{
                            if (path == null || path.isEmpty()) return;
                            int dotIndex = path.lastIndexOf(".");
                            path = dotIndex == -1 ? path : path.substring(0, dotIndex);
                            EntityId id = entity.getId();
                            File psdFile = ed.getComponent(id, PsdFile.class).getFile();
                            File xmlFile = new File(path + ".xml");
                            File j3oFile = new File(path + ".j3o");
                            ed.setComponent(ed.createEntity(),new Save(id,psdFile,xmlFile,j3oFile));
                        })));
                    });
                }
            }

            private Runnable showFileChooserDialog(VoidFunction1Obj<String> callback) {
                return () ->{
                    FileChooser fileChooser = new FileChooser(FileChooser.Type.SaveFile);
                    FormatFileFilter formatFileFilter = new FormatFileFilter("xml");
                    fileChooser.addFilter(formatFileFilter);
                    fileChooser.open("储存为", System.getProperty("user.dir"));
                    String path = fileChooser.getFilePath();
                    callback.apply(path);
                };
            }
        };

        @Override
        public void initialize() {
            currentImportedSet = ed.getEntities(Current.class,Imported.class);
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
            if (currentImportedSet.applyChanges()){
                fileSaveToSelected.setVisible(!currentImportedSet.isEmpty());
            }
        }

    }

    private class CloseFileOperation extends AbstractOperation {

        private EntitySet importedSet;
        private MouseInputListener listener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileCloseSelected, mouse)) {
                    EntityId id = ed.findEntity(null, Current.class,Imported.class);
                    ed.setComponent(ed.createEntity(),new Close(id));
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            importedSet = ed.getEntities(Imported.class);
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
            if (importedSet.applyChanges()) {
                fileCloseSelected.setVisible(!importedSet.isEmpty());
            }
        }

    }

    private class QuitFileOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileQuitSelected, mouse)) {
                    ed.setComponent(ed.createEntity(),new Quit());
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

    private class UndoRedoEditOperation extends AbstractOperation {

        private EntitySet importedSet;//监听导入文件：导入文件被关闭时将清除所有操作记录以防撤销出错
        private CustomPropertyListener recordOperationListener = new CustomPropertyListener() {
            @Override
            public void propertyChanged(Enum property, Object oldValue, Object newValue) {
                RecordState recordState = getScreen().getState(RecordState.class);
                editRedoSelected.setVisible(!recordState.isRedoDisabled());
                editUndoSelected.setVisible(!recordState.isUndoDisabled());
            }
        };
        private MouseInputListener mouseListener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(editRedoSelected,mouse)){
                    getScreen().getState(RecordState.class).redo();
                }
                if (selectConverter.isSelect(editUndoSelected,mouse)){
                    getScreen().getState(RecordState.class).undo();
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };
        private KeyInputListener keyListener = new KeyInputAdapter() {
            @Override
            public void onKeyPressed(KeyEvent key) {
                if (key.isCtrlPressedOnly() && key.getCode()==KeyInput.KEY_Z){
                    RecordState recordState = getScreen().getState(RecordState.class);
                    if (recordState.isRedoDisabled()){
                        recordState.undo();
                    }else {
                        while (recordState.redo()){
                        }
                    }
                }
                if (key.isCtrlAndShiftPressed() && key.getCode()== KeyInput.KEY_Z){
                    getScreen().getState(RecordState.class).redo();
                }
                if (key.isCtrlAndAltPressed() && key.getCode()==KeyInput.KEY_Z){
                    getScreen().getState(RecordState.class).undo();
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(KeyInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            importedSet=ed.getEntities(Imported.class);
            RecordState recordState=getScreen().getState(RecordState.class);
            recordState.addOperationListener(recordOperationListener);
        }

        @Override
        public void cleanup() {
            RecordState recordState=getScreen().getState(RecordState.class);
            if (recordState!=null) recordState.removeOperationListener(recordOperationListener);
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
            if (importedSet.applyChanges()){
                if (!importedSet.getRemovedEntities().isEmpty()){
                    getScreen().getState(RecordState.class).clearAllRecords();
                }
            }
        }
    }

}

