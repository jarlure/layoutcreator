package com.jarlure.layoutcreator.entitycomponent.psd;

import com.jarlure.project.bean.LayerImageData;
import com.simsilica.es.EntityComponent;

public class LayerImgData implements EntityComponent {

    private int index;
    private LayerImageData layerImageData;

    public LayerImgData(int index, LayerImageData layerImageData){
        this.index=index;
        this.layerImageData=layerImageData;
    }

    public int getIndex() {
        return index;
    }

    public String getName(){
        if (layerImageData==null)return "";
        return layerImageData.getName();
    }

    public LayerImageData getLayerImageData() {
        return layerImageData;
    }
}
