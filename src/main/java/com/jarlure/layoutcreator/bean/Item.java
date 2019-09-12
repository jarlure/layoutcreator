package com.jarlure.layoutcreator.bean;

import com.jarlure.ui.component.UIComponent;
import com.simsilica.es.EntityComponent;

public class Item implements EntityComponent {

    private UIComponent item;

    public Item(UIComponent item){
        this.item=item;
    }

    public UIComponent getItem() {
        return item;
    }

}
