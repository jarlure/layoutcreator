package com.jarlure.layoutcreator.screen.pscreen;

import com.jarlure.layoutcreator.bean.J3oFile;
import com.jarlure.layoutcreator.bean.PsdFile;
import com.jarlure.layoutcreator.bean.SaveTip;
import com.jarlure.layoutcreator.bean.XmlFile;
import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.layoutcreator.util.txt.XmlFileCreateRecord;
import com.jarlure.project.bean.commoninterface.Callback;
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
        operations.add(new SelectEffectOperation());
        operations.add(new OpenFileOperation());
        operations.add(new SaveFileOperation());
        operations.add(new SaveToFileOperation());
        operations.add(new CloseFileOperation());
        operations.add(new QuitFileOperation());
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
                    showFileChooserDialog((path, extra) -> {
                        if (path==null) return;
                        File file = new File(path);
                        if (!file.exists()) return;
                        if (!file.isFile()) return;
                        if (!file.getName().endsWith(".psd"))return;
                        final File psdFile= file;
                        final File xmlFile= XmlFileCreateRecord.findRecordByName(toXmlExtension(psdFile.getName()));
                        final File j3oFile;{
                            if (xmlFile == null) j3oFile = new File(path.replace(".psd", ".j3o"));
                            else j3oFile = new File(xmlFile.getPath().replace(".xml", ".j3o"));
                        }
                        final EntityId id = ed.findEntity(null,PsdFile.class);
                        if (id==null){
                            checkAndSetFile(psdFile,xmlFile,j3oFile);
                        }else{
                            ed.setComponent(ed.createEntity(),new SaveTip((isSaved, extra1) ->{
                                ed.removeEntity(id);
                                checkAndSetFile(psdFile,xmlFile,j3oFile);
                            }));
                        }
                    });
                }
            }

            private String toXmlExtension(String psdName){
                StringBuilder builder = new StringBuilder(psdName.length());
                builder.append(psdName,0,psdName.lastIndexOf('.')).append(".xml");
                return builder.toString();
            }

            private void showFileChooserDialog(Callback<String> callback){
                new Thread(() -> {
                    FileChooser fileChooser = new FileChooser();
                    FormatFileFilter formatFileFilter = new FormatFileFilter("psd");
                    fileChooser.addFilter(formatFileFilter);
                    fileChooser.open("打开",System.getProperty("user.dir"));
                    String path = fileChooser.getFilePath();
                    callback.onDone(path);
                }).start();
            }

            private void checkAndSetFile(File psdFile,File xmlFile,File j3oFile){
                if (xmlFile==null){
                    xmlFile=new File(toXmlExtension(psdFile.getAbsolutePath()));
                }
                ed.setComponents(ed.createEntity(),new PsdFile(psdFile),new XmlFile(xmlFile),new J3oFile(j3oFile));
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

        private EntitySet psdFileSet;
        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileSaveSelected,mouse)){
                    getScreen().getState(SaveState.class).save();
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            psdFileSet =ed.getEntities(PsdFile.class);
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
            if (psdFileSet.applyChanges()){
                fileSaveSelected.setVisible(!psdFileSet.isEmpty());
            }
        }

    }

    private class SaveToFileOperation extends AbstractOperation {

        private EntitySet psdFileSet;
        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileSaveToSelected,mouse)){
                    showFileChooserDialog((path, extra) -> {
                        if (path==null || path.isEmpty())return;
                        int dotIndex = path.lastIndexOf(".");
                        path = dotIndex == -1 ? path : path.substring(0, dotIndex);
                        EntityId id = ed.findEntity(null,PsdFile.class);
                        File psdFile = ed.getComponent(id,PsdFile.class).getFile();
                        File xmlFile = new File(path+".xml");
                        File j3oFile = new File(path+".j3o");
                        SaveState saveState = getScreen().getState(SaveState.class);
                        saveState.saveToPsd(psdFile);
                        saveState.saveToXml(xmlFile);
                        saveState.saveToJ3o(j3oFile);
                        ed.setComponents(id,new XmlFile(xmlFile),new J3oFile(j3oFile));
                    });
                }
            }

            private void showFileChooserDialog(Callback<String> callback){
                new Thread(() -> {
                    FileChooser fileChooser = new FileChooser(FileChooser.Type.SaveFile);
                    FormatFileFilter formatFileFilter = new FormatFileFilter("xml");
                    fileChooser.addFilter(formatFileFilter);
                    fileChooser.open("储存为",System.getProperty("user.dir"));
                    String path = fileChooser.getFilePath();
                    callback.onDone(path);
                }).start();
            }
        };

        @Override
        public void initialize() {
            psdFileSet =ed.getEntities(PsdFile.class);
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
            if (psdFileSet.applyChanges()){
                fileSaveToSelected.setVisible(!psdFileSet.isEmpty());
            }
        }

    }

    private class CloseFileOperation extends AbstractOperation {

        private EntitySet psdFileSet;
        private MouseInputListener listener=new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileCloseSelected,mouse)){
                    EntityId id = ed.findEntity(null,PsdFile.class);
                    ed.setComponent(ed.createEntity(),new SaveTip((isSaved, extra) ->
                            ed.removeEntity(id))
                    );
                }
            }

            @Override
            public void foldAnonymousInnerClassCode(MouseInputAdapter instance) {
            }
        };

        @Override
        public void initialize() {
            psdFileSet =ed.getEntities(PsdFile.class);
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
            if (psdFileSet.applyChanges()){
                fileCloseSelected.setVisible(!psdFileSet.isEmpty());
            }
        }

    }

    private class QuitFileOperation extends AbstractOperation {

        private MouseInputListener listener = new MouseInputAdapter() {
            @Override
            public void onLeftButtonPress(MouseEvent mouse) {
                if (selectConverter.isSelect(fileQuitSelected, mouse)) {
                    EntityId id = ed.findEntity(null,PsdFile.class);
                    if (id==null) app.stop();
                    else ed.setComponent(ed.createEntity(), new SaveTip((isSaved, extra) -> app.stop()));
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
                if (key.isAltAndShiftPressed() && key.getCode()== KeyInput.KEY_Z){
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
            RecordState recordState=getScreen().getState(RecordState.class);
            recordState.addOperationListener(recordOperationListener);
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

    }

}

