package com.jarlure.layoutcreator.entitycomponent.xml;

import com.simsilica.es.EntityComponent;

public class ComponentLink implements EntityComponent {

    private String link;

    public ComponentLink(String link){
        if (link==null) link="";
        this.link=link;
    }

    public String getLink() {
        return link;
    }

}
