package com.jarlure.layoutcreator.util;

import com.jarlure.layoutcreator.entitycomponent.common.Index;
import com.jme3.util.IntMap;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

public class SortHelper {

    public static EntityId[] sortByIndex(Set<EntityId> idSet, EntityData ed){
        if (idSet.isEmpty())return new EntityId[0];
        IntMap<EntityId> idMap=new IntMap<>(idSet.size());
        int[] indexArray=new int[idSet.size()];
        int i=0;
        for (EntityId id:idSet){
            int index = ed.getComponent(id, Index.class).getIndex();
            idMap.put(index,id);
            indexArray[i++]=index;
        }
        Arrays.sort(indexArray);
        EntityId[] result= new EntityId[indexArray.length];
        for (i=0;i<result.length;i++){
            result[i]=idMap.get(indexArray[i]);
        }
        return result;
    }

    public static EntityId[] sortByEntityId(Set<EntityId> idSet,EntityData ed){
        EntityId[] result = new EntityId[idSet.size()];
        idSet.toArray(result);
        Arrays.sort(result,(a, b) -> {
            if (a.getId()>b.getId())return 1;
            else if (a.getId()<b.getId()) return -1;
            return 0;
        });
        return result;
    }

}
