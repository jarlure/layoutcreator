package com.jarlure.layoutcreator.factory;

import com.jarlure.layoutcreator.layout.PSLayout;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.factory.AbstractUIFactory;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.ui.bean.Font;
import com.jarlure.ui.component.Picture;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.effect.NinePatchEffect;
import com.jarlure.ui.effect.TextEditEffect;
import com.jarlure.ui.effect.TextLineEditEffect;
import com.jarlure.ui.property.*;
import com.jarlure.ui.property.common.CustomPropertyListener;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.jme3.util.NativeObjectManager;

import java.nio.ByteBuffer;


public class NameTextEditFactory extends AbstractUIFactory {

    public static final int MAX_TEXT_NUMBER = 40;

    @Override
    public UIComponent create(String type, String name, LayerImageData... data) {
        if (type.equals(DefaultUIFactory.Picture)){
            boolean[][] edge = new boolean[4][];
            Image img = ImageHandler.cutNinePatchImage(data[0].getImg(),edge);
            UIComponent picture= new Picture(name, img);
            picture.move(data[0].getLeft()+1, data[0].getBottom()+1);

            FontProperty fontProperty;{
                fontProperty = picture.get(FontProperty.class);
                Font font = fontProperty.getFont();
                font.setName(PSLayout.FONT_HEI);
                font.setSize(14);
                font.setColor(ColorRGBA.Black);
            }
            final Image textDrawImg ;{
                int width = fontProperty.getSize()*MAX_TEXT_NUMBER;//最多显示40个字
                int height = (int) picture.get(AABB.class).getHeight();
                textDrawImg=ImageHandler.createEmptyImage(width,height);
            }
            CustomPropertyListener listener = (property, oldValue, newValue) -> {
                if (newValue==null)return;
                if (newValue.equals(oldValue))return;
                if (property.equals(TextProperty.Property.SRC))return;
                if (property.equals(TextProperty.Property.DES))return;
                //更新文本图片缓存
                ImageHandler.drawColor(textDrawImg, ColorRGBA.BlackNoAlpha);
                Font font = picture.get(FontProperty.class).getFont();
                TextProperty textProperty = picture.get(TextProperty.class);
                String text = textProperty.getText();
                int[] newImgTextPos = ImageHandler.drawFont(textDrawImg,font,text,textProperty.getStartX(),textProperty.getStartY(),textDrawImg.getWidth()-textProperty.getStartX(),textProperty.getEndY(),textProperty.getAlign());
                //更新文本编辑框的尺寸
                if (property.equals(TextProperty.Property.TEXT)){
                    int width = newImgTextPos[newImgTextPos.length-2]+2*font.getSize();
                    int minWidth = picture.get(NinePatchEffect.class).getSrc().getWidth();
                    width = Math.max(width,minWidth);
                    width = Math.min(width,textDrawImg.getWidth());
                    picture.get(AABB.class).setWidth(width);
                }
                //更新imgTextPos
                int[] imgTextPos = textProperty.getTextPosInImg();
                for (int i=newImgTextPos.length-2,endX=textProperty.getEndX();i>=0;i-=2){
                    if (newImgTextPos[i]<endX){
                        if (i==newImgTextPos.length-2) imgTextPos=newImgTextPos;
                        else {
                            imgTextPos=new int[i+2];
                            System.arraycopy(newImgTextPos,0,imgTextPos,0,imgTextPos.length);
                        }
                        break;
                    }
                }
                //更新显示图片
                Image des = textProperty.getDes();
                if (des!=null){
                    des.dispose();
                    if (!NativeObjectManager.UNSAFE) {
                        for (ByteBuffer buf : des.getData()) {
                            BufferUtils.destroyDirectBuffer(buf);
                        }
                    }
                }
                des = ImageHandler.clone(textProperty.getSrc());
                ImageHandler.drawCombine(des,textDrawImg,0,0);
                picture.get(ImageProperty.class).setImage(des);
                textProperty.setDes(des,imgTextPos);
            };
            fontProperty.addPropertyListener(listener);

            TextProperty textProperty = new TextProperty();
            textProperty.addPropertyListener(listener);
            picture.set(TextProperty.class,textProperty);

            TextEditEffect textEditEffect = new TextLineEditEffect(fontProperty,textProperty){
                protected void drawSelect() {
                    Image des = textProperty.getDes();
                    if (des==null)return;
                    int[] textPosInImg = textProperty.getTextPosInImg();
                    int minIndex = Math.min(fromIndex,toIndex);
                    int maxIndex = Math.max(fromIndex,toIndex);
                    int startX=Math.max(textPosInImg[2*minIndex],textProperty.getStartX());
                    int startY=Math.max(textPosInImg[2*minIndex+1],textProperty.getStartY());
                    int endX=Math.min(textPosInImg[2*maxIndex],textProperty.getEndX());
                    int endY=Math.min(startY+(int)Math.ceil(1.1f*fontProperty.getSize()),textProperty.getEndY());
                    if (startX==endX)return;
                    ImageRaster srcRaster = ImageRaster.create(textDrawImg);
                    ImageRaster desRaster = ImageRaster.create(des);
                    ColorRGBA color=new ColorRGBA();
                    float tolerance=0.4f;//经验参数，透明度小于该值表示背景色；否则表示字体色
                    textMarkForSelection=new int[0];
                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            srcRaster.getPixel(x, y, color);

                            if (color.getAlpha()<tolerance) desRaster.setPixel(x, y, selectionBackgroundColor);
                            else desRaster.setPixel(x, y, selectionTextColor);
                        }
                    }
                }

                protected void clearSelect() {
                    if (textMarkForSelection==null)return;
                    Image src = textProperty.getSrc();
                    Image des = textProperty.getDes();
                    if (src==null)return;
                    if (des==null)return;
                    ImageHandler.drawCut(des,0,0,src,0,0,src.getWidth(),src.getHeight());
                    ImageHandler.drawCombine(des,textDrawImg,0,0);
                    textMarkForSelection=null;
                }
            };
            picture.set(TextEditEffect.class,textEditEffect);

            ImageProperty imageProperty = picture.get(ImageProperty.class);
            NinePatchEffect ninePatchEffect = new NinePatchEffect(img,edge,imageProperty,textProperty);
            picture.set(NinePatchEffect.class,ninePatchEffect);
            ninePatchEffect.setSize(img.getWidth(),img.getHeight());

            picture.get(SpatialProperty.class).addPropertyListener((property, oldValue, newValue) -> {
                if (SpatialProperty.Property.WORLD_SCALE.equals(property)){
                    AABB box = picture.get(AABB.class);
                    float width = box.getWidth();
                    float height = box.getHeight();
                    picture.get(NinePatchEffect.class).setSize(width,height);
                }
            });

            return picture;
        }

        return null;
    }

}
