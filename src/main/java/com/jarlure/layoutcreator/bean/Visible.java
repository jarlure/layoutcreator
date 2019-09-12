package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;

public class Visible implements EntityComponent {

    public static final Visible FALSE = new Visible(false);
    public static final Visible TRUE = new Visible(true);

    private boolean visible;

    public Visible(boolean visible){
        this.visible=visible;
    }

    public boolean isVisible() {
        return visible;
    }

}

