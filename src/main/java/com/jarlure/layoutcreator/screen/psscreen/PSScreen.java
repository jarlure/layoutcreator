package com.jarlure.layoutcreator.screen.psscreen;

import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.Bundle;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.AbstractScreen;

import java.awt.*;

public class PSScreen extends AbstractScreen {

    private Frame frame;

    public PSScreen(Frame frame){
        this.frame = frame;
    }

    @Override
    protected Class<? extends Layout> getLayoutClass() {
        return PSLayout.class;
    }

    @Override
    protected void initialize() {
        //注册窗体框控制器（顶部、顶部右上方）
        screenStates.add(new WindowBarState(frame));
        //注册菜单栏控制器（顶部左上方）
        screenStates.add(new MenuState());
        //注册选项卡控制器（上方）
        screenStates.add(new FileTabState());
        //注册预览界面控制器（中部）
        screenStates.add(new SceneState());
        //注册图层面板控制器（右边）
        screenStates.add(new LayerPanelState());
        //注册组件面板开关控制器
        screenStates.add(new ComponentDialogState());
        //注册组件面板控制器
        screenStates.add(new ComponentPanelState());
        //注册组件属性面板控制器
        screenStates.add(new ComponentPropertyPanelState());
        //注册图片属性面板控制器
        screenStates.add(new ImgPropertyPanelState());
        //注册保存提示控制器
        screenStates.add(new SaveTipState());

        setEnabled(true);
        setVisible(true);
    }

    @Override
    protected Bundle getBundle() {
        return null;
    }

}
