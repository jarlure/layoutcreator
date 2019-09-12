package com.jarlure.layoutcreator.bean;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.ArrayList;
import java.util.List;

public class PSDData implements EntityComponent {

    private List<Object> data;

    public static PSDData create(Object o1,Object o2){
        PSDData data=new PSDData();
        ((EntityData)o1).setComponent(new EntityId(PSDData.class.hashCode()),data);
        return data;
    }

    public PSDData(){
        this.data=new ArrayList<>();
    }

    public int add(Object obj){
        data.add(obj);
        return data.size();
    }

    public Object get(Object index){
        return data.get((Integer) index);
    }

    public int size(){
        return data.size();
    }

}
