package com.jarlure.layoutcreator.layout;

import com.jarlure.layoutcreator.factory.NameTextEditFactory;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.project.factory.DynamicUIFactory;
import com.jarlure.project.layout.AbstractLayout;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.layout.LayoutHelper;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.property.FontProperty;
import com.jarlure.ui.property.ImageProperty;
import com.jme3.math.ColorRGBA;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jarlure.project.factory.DefaultUIFactory.*;

public class PSLayout extends AbstractLayout {

    public static final String FONT_HEI = "simhei";//黑体
    public static final String FONT_JING_DIAN_LI_BIAN = "JDJLIBIAN";//经典隶变简
    public static final String FONT_TENG_XIANG_JIA_LI = "腾祥嘉丽中黑简";//腾祥嘉丽中黑简

    public static final int BUTTON_STATE_NOTHING = 0;
    public static final int BUTTON_STATE_MOVE_ON = 1;
    public static final int BUTTON_STATE_PRESSED = 2;
    public static final int SELECT_STATE_NOTHING = 0;
    public static final int SELECT_STATE_SELECTED = 1;
    public static final int VISIBLE_STATE_DISABLE = 0 ;
    public static final int VISIBLE_STATE_VISIBLE = 1;
    public static final int VISIBLE_STATE_HIDE = 2;
    public static final int GROUP_UNFOLDED = 0;
    public static final int GROUP_FOLDED = 1;

    public static final String BACKGROUND = "background";
    public static final String WINDOW_BAR = "windowBar";
    public static final String WINDOW_CLOSE_BUTTON = "windowCloseButton";
    public static final String WINDOW_MINIMIZE_BUTTON = "windowMinimizeButton";
    public static final String LAYER_PANEL = "layerPanel";
    public static final String LAYER_SCROLL_DOWN_ARROW = "layerScrollDownArrow";
    public static final String LAYER_SCROLL = "layerScroll";
    public static final String LAYER_SCROLL_UP_ARROW = "layerScrollUpArrow";
    public static final String LAYER_SCROLL_BAR = "layerScrollBar";
    public static final String SECOND_LAYER_PREVIEW_ICON_POSITION = "secondLayerPreviewIconPosition";
    public static final String LAYER_ITEM_BACKGROUND = "layerItemBackground";
    public static final String LAYER_NAME_TEXT = "layerNameText";
    public static final String LAYER_NAME_TEXT_EDIT = "layerNameTextEdit";
    public static final String LAYER_TEXT_ICON = "layerTextIcon";
    public static final String LAYER_TEXT_SELECTED_ICON = "layerTextSelectedIcon";
    public static final String LAYER_PREVIEW_ICON = "layerPreviewIcon";
    public static final String LAYER_PREVIEW_SELECTED_ICON = "layerPreviewSelectedIcon";
    public static final String LAYER_GROUP_ICON = "layerGroupIcon";
    public static final String LAYER_GROUP_FOLD_BUTTON = "layerGroupFoldButton";
    public static final String LAYER_VISIBLE_ICON = "layerVisibleIcon";
    public static final String LAYER_TEXT_ITEM = "layerTextItem";
    public static final String LAYER_PREVIEW_ITEM = "layerPreviewItem";
    public static final String LAYER_GROUP_ITEM = "layerGroupItem";
    public static final String SCENE = "scene";
    public static final String SCENE_SCROLL_DOWN_ARROW = "sceneScrollDownArrow";
    public static final String SCENE_VERTICAL_SCROLL = "sceneVerticalScroll";
    public static final String SCENE_SCROLL_UP_ARROW = "sceneScrollUpArrow";
    public static final String SCENE_VERTICAL_SCROLL_BAR = "sceneVerticalScrollBar";
    public static final String SCENE_SCROLL_RIGHT_ARROW = "sceneScrollRightArrow";
    public static final String SCENE_HORIZONTAL_SCROLL = "sceneHorizontalScroll";
    public static final String SCENE_SCROLL_LEFT_ARROW = "sceneScrollLeftArrow";
    public static final String SCENE_HORIZONTAL_SCROLL_BAR = "sceneHorizontalScrollBar";
    public static final String FILE_TAB_BACKGROUND = "fileTabBackground";
    public static final String FILE_TAB_CLOSE_BUTTON = "fileTabCloseButton";
    public static final String PROPERTY_DIALOG_BACKGROUND = "propertyDialogBackground";
    public static final String IMG_PROPERTY_BACKGROUND = "imgPropertyBackground";
    public static final String IMG_PROPERTY_URL_QUOTE_BUTTON = "imgPropertyUrlQuoteButton";
    public static final String IMG_PROPERTY_URL_TEXT = "imgPropertyUrlText";
    public static final String IMG_PROPERTY_URL_ENABLE_CHECK_BOX = "imgPropertyUrlEnableCheckBox";
    public static final String IMG_PROPERTY_URL = "imgPropertyUrl";
    public static final String IMG_PROPERTY_NAME_QUOTE_BUTTON = "imgPropertyNameQuoteButton";
    public static final String IMG_PROPERTY_NAME_TEXT = "imgPropertyNameText";
    public static final String IMG_PROPERTY_NAME = "imgPropertyName";
    public static final String IMG_PROPERTY_CONTENT = "imgPropertyContent";
    public static final String COMPONENT_PROPERTY_BACKGROUND = "componentPropertyBackground";
    public static final String COMPONENT_PROPERTY_LINK_TEXT = "componentPropertyLinkText";
    public static final String COMPONENT_PROPERTY_NAME_TEXT = "componentPropertyNameText";
    public static final String COMPONENT_PROPERTY_TEXT_EDIT = "componentPropertyTextEdit";
    public static final String COMPONENT_PROPERTY_TYPE_BUTTON = "componentPropertyTypeButton";
    public static final String COMPONENT_PROPERTY_TYPE_TEXT = "componentPropertyTypeText";
    public static final String COMPONENT_PROPERTY_TYPE_LIST_PANEL = "componentPropertyTypeListPanel";
    public static final String COMPONENT_PROPERTY_TYPE_LIST_ITEM = "componentPropertyTypeListItem";
    public static final String COMPONENT_PROPERTY_TYPE_LIST_SELECT_ITEM = "componentPropertyTypeListSelectItem";
    public static final String COMPONENT_PROPERTY_TYPE = "componentPropertyType";
    public static final String COMPONENT_PROPERTY_CONTENT = "componentPropertyContent";
    public static final String PROPERTY_DIALOG = "propertyDialog";
    public static final String COMPONENT_DIALOG_BACKGROUND = "componentDialogBackground";
    public static final String DELETE_COMPONENT_BUTTON = "deleteComponentButton";
    public static final String ADD_IMG_BUTTON = "addImgButton";
    public static final String ADD_COMPONENT_BUTTON = "addComponentButton";
    public static final String COMPONENT_PANEL = "componentPanel";
    public static final String COMPONENT_SCROLL_DOWN_ARROW = "componentScrollDownArrow";
    public static final String COMPONENT_SCROLL = "componentScroll";
    public static final String COMPONENT_SCROLL_UP_ARROW = "componentScrollUpArrow";
    public static final String COMPONENT_SCROLL_BAR = "componentScrollBar";
    public static final String COMPONENT_DIALOG = "componentDialog";
    public static final String SECOND_COMPONENT_ICON_POSITION = "secondComponentIconPosition";
    public static final String COMPONENT_ITEM_BACKGROUND = "componentItemBackground";
    public static final String COMPONENT_NAME_TEXT = "componentNameText";
    public static final String COMPONENT_NAME_TEXT_EDIT = "componentNameTextEdit";
    public static final String IMG_ICON = "imgIcon";
    public static final String IMG_SELECTED_ICON = "imgSelectedIcon";
    public static final String COMPONENT_ICON = "componentIcon";
    public static final String COMPONENT_FOLD_BUTTON = "componentFoldButton";
    public static final String IMG_ITEM = "imgItem";
    public static final String COMPONENT_ITEM = "componentItem";
    public static final String INSERT_TIP_LOWER_LINE = "insertTipLowerLine";
    public static final String INSERT_TIP_CENTER_BOX = "insertTipCenterBox";
    public static final String INSERT_TIP_UPPER_LINE = "insertTipUpperLine";
    public static final String SHOW_COMPONENT_PANEL_DIALOG_BUTTON = "showComponentPanelDialogButton";
    public static final String MENU_HELP = "menuHelp";
    public static final String SUBMENU_HELP = "submenuHelp";
    public static final String MENU_WINDOW = "menuWindow";
    public static final String SUBMENU_WINDOW = "submenuWindow";
    public static final String MENU_VIEW = "menuView";
    public static final String SUBMENU_VIEW = "submenuView";
    public static final String MENU_3D = "menu3D";
    public static final String SUBMENU_3D = "submenu3D";
    public static final String MENU_FILTER = "menuFilter";
    public static final String SUBMENU_FILTER = "submenuFilter";
    public static final String MENU_SELECT = "menuSelect";
    public static final String SUBMENU_SELECT = "submenuSelect";
    public static final String MENU_TYPE = "menuType";
    public static final String SUBMENU_TYPE = "submenuType";
    public static final String MENU_LAYER = "menuLayer";
    public static final String SUBMENU_LAYER = "submenuLayer";
    public static final String MENU_IMAGE = "menuImage";
    public static final String SUBMENU_IMAGE = "submenuImage";
    public static final String MENU_EDIT = "menuEdit";
    public static final String SUBMENU_EDIT_BACKGROUND = "submenuEditBackground";
    public static final String SUBMENU_EDIT_UNDO = "submenuEditUndo";
    public static final String SUBMENU_EDIT_REDO = "submenuEditRedo";
    public static final String SUBMENU_EDIT = "submenuEdit";
    public static final String MENU_FILE = "menuFile";
    public static final String SUBMENU_FILE_BACKGROUND = "submenuFileBackground";
    public static final String SUBMENU_FILE_QUIT = "submenuFileQuit";
    public static final String SUBMENU_FILE_SAVE_TO = "submenuFileSaveTo";
    public static final String SUBMENU_FILE_SAVE = "submenuFileSave";
    public static final String SUBMENU_FILE_CLOSE = "submenuFileClose";
    public static final String SUBMENU_FILE_OPEN = "submenuFileOpen";
    public static final String SUBMENU_FILE = "submenuFile";
    public static final String SAVE_TIP_DIALOG_BACKGROUND = "saveTipDialogBackground";
    public static final String SAVE_TIP_DIALOG_CLOSE_BUTTON = "saveTipDialogCloseButton";
    public static final String SAVE_TIP_DIALOG_NO_BUTTON = "saveTipDialogNoButton";
    public static final String SAVE_TIP_DIALOG_YES_BUTTON = "saveTipDialogYesButton";
    public static final String SAVE_TIP_DIALOG = "saveTipDialog";

    @Override
    public Map<String, Class<Layout>> getLinkedComponentLayoutMap() {
        Map<String,Class<Layout>> map = new HashMap<>();
        return map;
    }

    @Override
    protected UINode createComponent(List<LayerImageData> layerImageData) {
        DefaultUIFactory defaultUIFactory = new DefaultUIFactory();

        LayoutHelper helper=new LayoutHelper(layerImageData);
        helper.add(Picture, BACKGROUND, 168);
        helper.add(Picture, WINDOW_BAR, 167);
        helper.add(Picture, WINDOW_CLOSE_BUTTON, 166,165,164);
        helper.add(Picture, WINDOW_MINIMIZE_BUTTON, 163,162,161);
        helper.add(Panel, LAYER_PANEL, 160,159,158);
        helper.add(Picture, LAYER_SCROLL_DOWN_ARROW, 157);
        helper.add(Picture, LAYER_SCROLL, 156);
        helper.add(Picture, LAYER_SCROLL_UP_ARROW, 155);
        helper.add(ScrollBar, LAYER_SCROLL_BAR, LAYER_SCROLL_DOWN_ARROW,LAYER_SCROLL,LAYER_SCROLL_UP_ARROW);
        helper.add(Picture, SECOND_LAYER_PREVIEW_ICON_POSITION, 154);
        helper.add(Picture, LAYER_ITEM_BACKGROUND, 153,152);
        helper.add(Picture, LAYER_NAME_TEXT, 151);
        helper.add(Picture, LAYER_NAME_TEXT_EDIT, 150);
        helper.add(Picture, LAYER_TEXT_ICON, 149);
        helper.add(Picture, LAYER_TEXT_SELECTED_ICON, 148);
        helper.add(Picture, LAYER_PREVIEW_ICON, 147);
        helper.add(Picture, LAYER_PREVIEW_SELECTED_ICON, 146);
        helper.add(Picture, LAYER_GROUP_ICON, 145,144);
        helper.add(Picture, LAYER_GROUP_FOLD_BUTTON, 143);
        helper.add(Picture, LAYER_VISIBLE_ICON, 142,141,140);
        helper.add(Panel, LAYER_TEXT_ITEM, LAYER_ITEM_BACKGROUND,LAYER_NAME_TEXT,LAYER_TEXT_ICON,LAYER_TEXT_SELECTED_ICON,LAYER_VISIBLE_ICON);
        helper.add(Panel, LAYER_PREVIEW_ITEM, LAYER_ITEM_BACKGROUND,LAYER_NAME_TEXT,LAYER_PREVIEW_ICON,LAYER_PREVIEW_SELECTED_ICON,LAYER_VISIBLE_ICON);
        helper.add(Panel, LAYER_GROUP_ITEM, LAYER_ITEM_BACKGROUND,LAYER_NAME_TEXT,LAYER_GROUP_ICON,LAYER_GROUP_FOLD_BUTTON,LAYER_VISIBLE_ICON);
        helper.add(Picture, SCENE, 139);
        helper.add(Picture, SCENE_SCROLL_DOWN_ARROW, 138);
        helper.add(Picture, SCENE_VERTICAL_SCROLL, 137);
        helper.add(Picture, SCENE_SCROLL_UP_ARROW, 136);
        helper.add(ScrollBar, SCENE_VERTICAL_SCROLL_BAR, SCENE_SCROLL_DOWN_ARROW,SCENE_VERTICAL_SCROLL,SCENE_SCROLL_UP_ARROW);
        helper.add(Picture, SCENE_SCROLL_RIGHT_ARROW, 135);
        helper.add(Picture, SCENE_HORIZONTAL_SCROLL, 134);
        helper.add(Picture, SCENE_SCROLL_LEFT_ARROW, 133);
        helper.add(ScrollBar, SCENE_HORIZONTAL_SCROLL_BAR, SCENE_SCROLL_RIGHT_ARROW,SCENE_HORIZONTAL_SCROLL,SCENE_SCROLL_LEFT_ARROW);
        helper.add(Picture, FILE_TAB_BACKGROUND, 132);
        helper.add(Picture, FILE_TAB_CLOSE_BUTTON, 131);
        helper.add(Picture, PROPERTY_DIALOG_BACKGROUND, 130);
        helper.add(Picture, IMG_PROPERTY_BACKGROUND, 129,128,127,126,125,124);
        helper.add(Picture, IMG_PROPERTY_URL_QUOTE_BUTTON, 123,122);
        helper.add(Picture, IMG_PROPERTY_URL_TEXT, 121);
        helper.add(Picture, IMG_PROPERTY_URL_ENABLE_CHECK_BOX, 120,119);
        helper.add(Node, IMG_PROPERTY_URL, IMG_PROPERTY_URL_QUOTE_BUTTON,IMG_PROPERTY_URL_TEXT,IMG_PROPERTY_URL_ENABLE_CHECK_BOX);
        helper.add(Picture, IMG_PROPERTY_NAME_QUOTE_BUTTON, 118,117);
        helper.add(Picture, IMG_PROPERTY_NAME_TEXT, 116);
        helper.add(Node, IMG_PROPERTY_NAME, IMG_PROPERTY_NAME_QUOTE_BUTTON,IMG_PROPERTY_NAME_TEXT);
        helper.add(Node, IMG_PROPERTY_CONTENT, IMG_PROPERTY_BACKGROUND,IMG_PROPERTY_URL,IMG_PROPERTY_NAME);
        helper.add(Picture, COMPONENT_PROPERTY_BACKGROUND, 115,114,113,112,111,110);
        helper.add(Picture, COMPONENT_PROPERTY_LINK_TEXT, 109);
        helper.add(Picture, COMPONENT_PROPERTY_NAME_TEXT, 108);
        helper.add(Picture, COMPONENT_PROPERTY_TEXT_EDIT, 107);
        helper.add(Picture, COMPONENT_PROPERTY_TYPE_BUTTON, 106,105);
        helper.add(Picture, COMPONENT_PROPERTY_TYPE_TEXT, 104,103);
        helper.add(Panel, COMPONENT_PROPERTY_TYPE_LIST_PANEL, 102,101,100);
        helper.add(Picture, COMPONENT_PROPERTY_TYPE_LIST_ITEM, 99);
        helper.add(Picture, COMPONENT_PROPERTY_TYPE_LIST_SELECT_ITEM, 98);
        helper.add(Node, COMPONENT_PROPERTY_TYPE, COMPONENT_PROPERTY_TYPE_BUTTON,COMPONENT_PROPERTY_TYPE_TEXT,COMPONENT_PROPERTY_TYPE_LIST_PANEL);
        helper.add(Node, COMPONENT_PROPERTY_CONTENT, COMPONENT_PROPERTY_BACKGROUND,COMPONENT_PROPERTY_LINK_TEXT,COMPONENT_PROPERTY_NAME_TEXT,COMPONENT_PROPERTY_TEXT_EDIT,COMPONENT_PROPERTY_TYPE);
        helper.add(Dialog, PROPERTY_DIALOG, PROPERTY_DIALOG_BACKGROUND,IMG_PROPERTY_CONTENT,COMPONENT_PROPERTY_CONTENT);
        helper.add(Picture, COMPONENT_DIALOG_BACKGROUND, 97);
        helper.add(Picture, DELETE_COMPONENT_BUTTON, 96,95,94);
        helper.add(Picture, ADD_IMG_BUTTON, 93,92,91);
        helper.add(Picture, ADD_COMPONENT_BUTTON, 90,89,88);
        helper.add(Panel, COMPONENT_PANEL, 87,86,85);
        helper.add(Picture, COMPONENT_SCROLL_DOWN_ARROW, 84);
        helper.add(Picture, COMPONENT_SCROLL, 83);
        helper.add(Picture, COMPONENT_SCROLL_UP_ARROW, 82);
        helper.add(ScrollBar, COMPONENT_SCROLL_BAR, COMPONENT_SCROLL_DOWN_ARROW,COMPONENT_SCROLL,COMPONENT_SCROLL_UP_ARROW);
        helper.add(Dialog, COMPONENT_DIALOG, COMPONENT_DIALOG_BACKGROUND,DELETE_COMPONENT_BUTTON,ADD_IMG_BUTTON,ADD_COMPONENT_BUTTON,COMPONENT_PANEL,COMPONENT_SCROLL_BAR);
        helper.add(Picture, SECOND_COMPONENT_ICON_POSITION, 81);
        helper.add(Picture, COMPONENT_ITEM_BACKGROUND, 80,79);
        helper.add(Picture, COMPONENT_NAME_TEXT, 78);
        helper.add(Picture, COMPONENT_NAME_TEXT_EDIT, 77);
        helper.add(Picture, IMG_ICON, 76);
        helper.add(Picture, IMG_SELECTED_ICON, 75);
        helper.add(Picture, COMPONENT_ICON, 74,73);
        helper.add(Picture, COMPONENT_FOLD_BUTTON, 72);
        helper.add(Panel, IMG_ITEM, COMPONENT_ITEM_BACKGROUND,COMPONENT_NAME_TEXT,IMG_ICON,IMG_SELECTED_ICON);
        helper.add(Panel, COMPONENT_ITEM, COMPONENT_ITEM_BACKGROUND,COMPONENT_NAME_TEXT,COMPONENT_ICON,COMPONENT_FOLD_BUTTON);
        helper.add(Picture, INSERT_TIP_LOWER_LINE, 71);
        helper.add(Picture, INSERT_TIP_CENTER_BOX, 70);
        helper.add(Picture, INSERT_TIP_UPPER_LINE, 69);
        helper.add(Picture, SHOW_COMPONENT_PANEL_DIALOG_BUTTON, 68,67,66);
        helper.add(Picture, MENU_HELP, 65,64,63);
        helper.add(Picture, SUBMENU_HELP, 62);
        helper.add(Picture, MENU_WINDOW, 61,60,59);
        helper.add(Picture, SUBMENU_WINDOW, 58);
        helper.add(Picture, MENU_VIEW, 57,56,55);
        helper.add(Picture, SUBMENU_VIEW, 54);
        helper.add(Picture, MENU_3D, 53,52,51);
        helper.add(Picture, SUBMENU_3D, 50);
        helper.add(Picture, MENU_FILTER, 49,48,47);
        helper.add(Picture, SUBMENU_FILTER, 46);
        helper.add(Picture, MENU_SELECT, 45,44,43);
        helper.add(Picture, SUBMENU_SELECT, 42);
        helper.add(Picture, MENU_TYPE, 41,40,39);
        helper.add(Picture, SUBMENU_TYPE, 38);
        helper.add(Picture, MENU_LAYER, 37,36,35);
        helper.add(Picture, SUBMENU_LAYER, 34);
        helper.add(Picture, MENU_IMAGE, 33,32,31);
        helper.add(Picture, SUBMENU_IMAGE, 30);
        helper.add(Picture, MENU_EDIT, 29,28,27);
        helper.add(Picture, SUBMENU_EDIT_BACKGROUND, 26);
        helper.add(Picture, SUBMENU_EDIT_UNDO, 25,24);
        helper.add(Picture, SUBMENU_EDIT_REDO, 23,22);
        helper.add(Node, SUBMENU_EDIT, SUBMENU_EDIT_BACKGROUND,SUBMENU_EDIT_UNDO,SUBMENU_EDIT_REDO);
        helper.add(Picture, MENU_FILE, 21,20,19);
        helper.add(Picture, SUBMENU_FILE_BACKGROUND, 18);
        helper.add(Picture, SUBMENU_FILE_QUIT, 17,16);
        helper.add(Picture, SUBMENU_FILE_SAVE_TO, 15,14);
        helper.add(Picture, SUBMENU_FILE_SAVE, 13,12);
        helper.add(Picture, SUBMENU_FILE_CLOSE, 11,10);
        helper.add(Picture, SUBMENU_FILE_OPEN, 9,8);
        helper.add(Node, SUBMENU_FILE, SUBMENU_FILE_BACKGROUND,SUBMENU_FILE_QUIT,SUBMENU_FILE_SAVE_TO,SUBMENU_FILE_SAVE,SUBMENU_FILE_CLOSE,SUBMENU_FILE_OPEN);
        helper.add(Picture, SAVE_TIP_DIALOG_BACKGROUND, 7);
        helper.add(Picture, SAVE_TIP_DIALOG_CLOSE_BUTTON, 6);
        helper.add(Picture, SAVE_TIP_DIALOG_NO_BUTTON, 5,4,3);
        helper.add(Picture, SAVE_TIP_DIALOG_YES_BUTTON, 2,1,0);
        helper.add(Dialog, SAVE_TIP_DIALOG, SAVE_TIP_DIALOG_BACKGROUND,SAVE_TIP_DIALOG_CLOSE_BUTTON,SAVE_TIP_DIALOG_NO_BUTTON,SAVE_TIP_DIALOG_YES_BUTTON);

        helper.set(LAYER_NAME_TEXT_EDIT,new NameTextEditFactory());
        helper.set(COMPONENT_NAME_TEXT_EDIT,new NameTextEditFactory());
        helper.set(COMPONENT_PROPERTY_TEXT_EDIT,new NameTextEditFactory());

        helper.set(LAYER_GROUP_ITEM,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_PREVIEW_ITEM,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_TEXT_ITEM,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_VISIBLE_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_GROUP_FOLD_BUTTON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_GROUP_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_PREVIEW_SELECTED_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_PREVIEW_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_TEXT_SELECTED_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_TEXT_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_NAME_TEXT,new DynamicUIFactory(defaultUIFactory));
        helper.set(LAYER_ITEM_BACKGROUND,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_ITEM,new DynamicUIFactory(defaultUIFactory));
        helper.set(IMG_ITEM,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_FOLD_BUTTON,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(IMG_SELECTED_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(IMG_ICON,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_NAME_TEXT,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_ITEM_BACKGROUND,new DynamicUIFactory(defaultUIFactory));
        helper.set(COMPONENT_PROPERTY_TYPE_LIST_ITEM,new DynamicUIFactory(defaultUIFactory));

        return helper.create(getClass().getSimpleName());
    }

    @Override
    protected void configureUIComponent() {
        String[] menuName=new String[]{
                MENU_FILE,MENU_EDIT,MENU_IMAGE,MENU_LAYER,MENU_TYPE,MENU_SELECT,MENU_FILTER,MENU_3D,MENU_VIEW,MENU_WINDOW,MENU_HELP
        };
        String[] submenuName=new String[]{
                SUBMENU_FILE,SUBMENU_EDIT,SUBMENU_IMAGE,SUBMENU_LAYER,SUBMENU_TYPE,SUBMENU_SELECT,SUBMENU_FILTER,SUBMENU_3D,SUBMENU_VIEW,SUBMENU_WINDOW,SUBMENU_HELP
        };
        for (int i = 0; i < menuName.length; i++) {
            UIComponent submenu = getComponent(submenuName[i]);
            submenu.setVisible(false);
            UIComponent menu = getComponent(menuName[i]);
            SwitchEffect switchEffect = menu.get(SwitchEffect.class);
            menu.get(ImageProperty.class).addPropertyListener((oldValue, newValue) -> submenu.setVisible(BUTTON_STATE_PRESSED==switchEffect.getIndexOfCurrentImage()));
        }
        String[] invisible=new String[]{
                SAVE_TIP_DIALOG
                ,SUBMENU_FILE_CLOSE,SUBMENU_FILE_SAVE,SUBMENU_FILE_SAVE_TO,SUBMENU_EDIT_UNDO,SUBMENU_EDIT_REDO
                ,SCENE_HORIZONTAL_SCROLL_BAR,SCENE_VERTICAL_SCROLL_BAR
                ,SECOND_LAYER_PREVIEW_ICON_POSITION,LAYER_SCROLL_BAR,LAYER_NAME_TEXT_EDIT
                ,COMPONENT_DIALOG,COMPONENT_SCROLL_BAR,ADD_COMPONENT_BUTTON,ADD_IMG_BUTTON,DELETE_COMPONENT_BUTTON
                ,INSERT_TIP_UPPER_LINE,INSERT_TIP_CENTER_BOX,INSERT_TIP_LOWER_LINE,SECOND_COMPONENT_ICON_POSITION,COMPONENT_NAME_TEXT_EDIT
                ,PROPERTY_DIALOG,COMPONENT_PROPERTY_TYPE_LIST_PANEL,COMPONENT_PROPERTY_TYPE_LIST_SELECT_ITEM,COMPONENT_PROPERTY_TEXT_EDIT
//                ,PROPERTY_DIALOG,COMPONENT_PROPERTY_TYPE_LIST_PANEL,COMPONENT_PROPERTY_TYPE_LIST_TEXT,COMPONENT_PROPERTY_TYPE_LIST_ITEM
        };
        for (String name:invisible){
            getComponent(name).setVisible(false);
        }
        getComponent(SAVE_TIP_DIALOG).setDepth(3);
        for (String submenu:submenuName){
            getComponent(submenu).setDepth(2);
        }
        getComponent(INSERT_TIP_UPPER_LINE).setDepth(1.2f);
        getComponent(INSERT_TIP_CENTER_BOX).setDepth(1.2f);
        getComponent(INSERT_TIP_LOWER_LINE).setDepth(1.2f);
        getComponent(COMPONENT_NAME_TEXT_EDIT).setDepth(1.2f);
        getComponent(COMPONENT_DIALOG).setDepth(1);
        getComponent(COMPONENT_PROPERTY_TYPE_LIST_SELECT_ITEM).setDepth(1.2f);
        getComponent(COMPONENT_PROPERTY_TEXT_EDIT).setDepth(1.2f);
        getComponent(PROPERTY_DIALOG).setDepth(1);

        String[] propretyTextArray = new String[]{
                COMPONENT_PROPERTY_TYPE_TEXT,COMPONENT_PROPERTY_NAME_TEXT,COMPONENT_PROPERTY_LINK_TEXT
                ,IMG_PROPERTY_NAME_TEXT,IMG_PROPERTY_URL_TEXT
        };

        for (String text:propretyTextArray){
            Font font = getComponent(text).get(FontProperty.class).getFont();
            font.setName(FONT_TENG_XIANG_JIA_LI);
            font.setColor(ColorRGBA.White);
            font.setSize(12);
        }

//        getComponent(COMPONENT_ITEM).set(UIFactory.class,recycleUIFactory);
//        getComponent(COMPONENT_PROPERTY_TYPE_LIST_ITEM).set(UIFactory.class,recycleUIFactory);

//        String[] propertyTextArray = new String[]{
//                COMPONENT_PROPERTY_TYPE_TEXT,COMPONENT_PROPERTY_NAME_TEXT,COMPONENT_PROPERTY_LINK_TEXT,
//                IMG_PROPERTY_NAME_TEXT,IMG_PROPERTY_URL_TEXT
//        };
//        for (String text:propertyTextArray){
//            Font font = getComponent(text).get(FontProperty.class).getFont();
//            font.setName(Font.TENG_XIANG_JIA_LI);
//            font.setColor(ColorRGBA.White);
//            font.setSize(12);
//        }
    }

}
