package com.jarlure.layoutcreator.util.psd;

import com.jarlure.layoutcreator.bean.PSDData;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.util.file.FileEditor;

import java.io.File;
import java.nio.ByteBuffer;

public class PSDFileEditor {

    public static void renameLayer(File file,String[] newNameArray, PSDData psdData){
        FileEditor editor = new FileEditor(file);
        if (!file.getName().endsWith(".psd")) throw new IllegalArgumentException("文件格式必须是psd");
        int numberOfLayer = (int) psdData.get(3);
        ByteBuffer buffer = null;
        int totalOffset = 0;
        for (int i=numberOfLayer-1,index=newNameArray.length-1;i>=0;i--){
            PSDData layerData = (PSDData) psdData.get(4 + i);
            int type = (int) layerData.get(layerData.size() - 1);
            if (type<0 || 2<type)continue;
            String oldName = ((LayerImageData) layerData.get(layerData.size() - 3)).getName();
            String newName=newNameArray[index--];
            if (newName.equals(oldName))continue;
            if (buffer==null){
                editor.open();
                buffer=editor.getBufferPointer();
            }
            int namePosition=-1;{
                for (int j=layerData.size()-6;j>=0;j-=3){
                    if (layerData.get(j).hashCode()==1819635305){
                        namePosition=layerData.get(j+1).hashCode()+8;
                        break;
                    }
                }
                if (namePosition==-1) throw new IllegalStateException("找不到namePosition");
            }
            byte[] newNameData = toDataByte(newName);
            int oldNameDataLength;{
                buffer.position(namePosition);
                oldNameDataLength = buffer.getInt();
            }
            int offset = newNameData.length-oldNameDataLength;
            if (offset==0){
                buffer.put(newNameData);
            }else{
                int pos = buffer.position();
                editor.replace(pos,pos+oldNameDataLength,newNameData);
                buffer.position(namePosition);
                buffer.putInt(newNameData.length);

                pos = layerData.get(6).hashCode();
                buffer.position(pos);
                int extra = buffer.getInt()+offset;
                buffer.position(pos);
                buffer.putInt(extra);

                totalOffset+=offset;
            }
        }
        if (totalOffset!=0){
            int pos = psdData.get(2).hashCode();
            buffer.position(pos);
            int len1 = buffer.getInt();
            int len2 = buffer.getInt();
            buffer.position(pos);
            buffer.putInt(len1+totalOffset);
            buffer.putInt(len2+totalOffset);
        }
        editor.close();
    }

    private static byte[] toDataByte(String name){
        try{
            byte[] value = name.getBytes("unicode");
            int size = value.length/2;
            int numberOfName = size-1;
            if (size%2!=0) size--;
            byte[] data = new byte[4+size*2];
            System.arraycopy(value,2,data,4,value.length-2);
            data[0] = (byte) (numberOfName >> 24);
            data[1] = (byte) ((numberOfName >> 16) & 0xff);
            data[2] = (byte) ((numberOfName >> 8) & 0xff);
            data[3] = (byte) (numberOfName & 0xff);
            return data;
        }catch (Exception e){
            throw new IllegalStateException("EditHelper.toUnicodeByte():"+name+"无法转换为unicode");
        }
    }

}
