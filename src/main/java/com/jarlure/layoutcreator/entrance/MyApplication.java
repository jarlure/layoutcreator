package com.jarlure.layoutcreator.entrance;

import com.jarlure.layoutcreator.screen.psscreen.PSScreen;
import com.jarlure.layoutcreator.state.*;
import com.jarlure.project.state.*;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.system.lwjgl.LwjglCanvas;

import java.awt.*;

public class MyApplication extends SimpleApplication {

    private Frame frame;

    public MyApplication(Frame frame) {
        super(new UIRenderState(), //UI渲染服务，也是所有UIComponent的根节点
                new DefaultEntityDataState(), //默认的实体组件系统服务，相当于数据库
                new DecayState(), //实体回收服务，用于延时删除实体（延时删除的好处是与实体绑定的数据不会突然消失无法挽回和由于依赖而导致的空指针异常）
                new DelayState(), //延时函数服务，用于延时触发函数或其他线程希望主线程执行某段函数的情况
                new RecordState(),//操作记录服务，用于记录用户操作和撤销重做用户操作
                new TaskState());//线程任务服务，用于执行子线程任务
        this.frame = frame;
    }

    @Override
    public void simpleInitApp() {
        AssetManager.initialize(this);
        InputManager.initialize(this);

        if (context instanceof LwjglCanvas) {//（供开发者使用的）指令模式：该模式下没有UI界面
            //请在该类中配置指令
            stateManager.attach(new CommandState());
        } else {
            //设置场景背景为黑灰色
            getViewPort().setBackgroundColor(new ColorRGBA(0.149f,0.149f,0.149f,1));
            //注册Screen类
            stateManager.attach(new PSScreen(frame));
        }
        //注册服务类
        stateManager.attach(new ImportState());
        stateManager.attach(new OpenState());
        stateManager.attach(new SaveState());
        stateManager.attach(new CloseState());
        stateManager.attach(new QuitState());
    }

    @Override
    public void start() {
        frame.setVisible(true);
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        frame.setVisible(false);
        frame.dispose();
    }

}