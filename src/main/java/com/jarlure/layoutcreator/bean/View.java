package com.jarlure.layoutcreator.bean;

import com.jarlure.ui.component.UIComponent;
import com.simsilica.es.EntityComponent;

public class View implements EntityComponent {

    private UIComponent view;

    public View(UIComponent view){
        this.view=view;
    }

    public UIComponent getView() {
        return view;
    }

}
