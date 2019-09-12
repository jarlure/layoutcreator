package com.jarlure.layoutcreator.util.psd;

import com.jarlure.layoutcreator.bean.PSDData;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.util.file.FileReader;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.texture.Image;
import com.simsilica.es.EntityData;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

public class PSDFileReader {

    public static Object read(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = N(v1, v2);
        v4 = N(v3, 2);
        v5 = S(v2, v4);
        v6 = K(v3, v5);
        v7 = S(v6, 5);
        return v7;
    }

    public static Object A(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = R(v3, 2);
        v5 = Math.abs(v4.hashCode());
        v6 = J(v2, v5);
        while (v5.hashCode() > 0) {
            v7 = S(v2, v5);
            v8 = R(v3, 4);
            v9 = J(v7, v8);
            v8 = R(v3, 4);
            v9 = J(v7, v8);
            v8 = R(v3, 4);
            v9 = J(v7, v8);
            v8 = R(v3, 4);
            v9 = J(v7, v8);
            v8 = R(v3, 2);
            v9 = J(v7, v8);
            v8 = M(v8, v3);
            v9 = J(v7, v8);

            v8 = S(v3, v3);
            v9 = J(v7, v8);
            v8 = X(v1, v7);
            v5 = v5.hashCode() - 1;
        }
        return v6;
    }

    public static Object D(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = R(v3, 1);
        v5 = (4 - (1 + v4.hashCode()) % 4) % 4;
        v6 = v4.hashCode() + v5.hashCode();
        v7 = N(v3, v6.hashCode());
        v8 = S(v3, v4);
        return v8;
    }

    public static Object E(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = N(v3, 4);
        v5 = R(v4, 4);
        v6 = N(v4, v5);
        v7 = R(v6, 4);
        v8 = N(v6, v7);
        v9 = Y(v1, v2);
        return v9;
    }

    public static Object J(Object o1, Object o2) {
        if (o1 == null) return o2;
        if (o1 instanceof MappedByteBuffer) {
            if (o2 instanceof byte[]) return ((MappedByteBuffer) o1).get((byte[]) o2);
            if (o2 instanceof Integer) return ((MappedByteBuffer) o1).position((Integer) o2);
            return ((MappedByteBuffer) o1).position();
        }
        if (o1 instanceof PSDData) return ((PSDData) o1).add(o2);
        if (o1 instanceof FileReader) return ((FileReader) o1).close();

        return o1.hashCode();
    }

    public static Object K(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = J(v3, 14);
        v5 = R(v4, 4);
        v6 = R(v4, 4);
        v7 = J(v2, v5);
        v8 = J(v2, v6);
        v9 = E(v1, v2);
        return v9;
    }

    public static Object L(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = R(v2, 0);
        v5 = R(v2, 1);
        v6 = R(v2, 3);
        v7 = 4;
        while (v6.hashCode() > 0) {
            v8 = R(v2, v7);
            Object v10, v11, v12, v13, v14, v15, v16, v17, v18, v19;
            v10 = R(v8, 0);
            v11 = R(v8, 1);
            v12 = R(v8, 2);
            v13 = R(v8, 3);
            v14 = R(v8, 4);
            v15 = R(v8, 5);
            v16 = 6;
            v17 = v12.hashCode() - v10.hashCode();
            v18 = v13.hashCode() - v11.hashCode();
            if (v14.hashCode() != 4)
                throw new UnsupportedOperationException("目前仅支持RGBA图层的读写操作，请在PS中打开该PSD文件，依次找到图像->模式->RGB颜色勾选之。保存后再试");
            if (v15.hashCode() < 0) v15 = v15.hashCode() + 256;
            if (v16.hashCode() != 0) v16 = N(v8, v16);
            if (v17.hashCode() != 0 && v18.hashCode() != 0) {
                v10 = v4.hashCode() - 1 - v10.hashCode();
                v12 = v4.hashCode() - v12.hashCode();
                v19 = new LayerImageData(v10.hashCode(), v12.hashCode(), v11.hashCode(), v13.hashCode());
            } else v19 = new LayerImageData();
            v10 = N(v19, v16);
            v11 = J(v8, v19);
            int[][] a = ImageHandler.CompressionMethod.decompress((ByteBuffer) v3, v17.hashCode(), v18.hashCode());
            int[][] r = ImageHandler.CompressionMethod.decompress((ByteBuffer) v3, v17.hashCode(), v18.hashCode());
            int[][] g = ImageHandler.CompressionMethod.decompress((ByteBuffer) v3, v17.hashCode(), v18.hashCode());
            int[][] b = ImageHandler.CompressionMethod.decompress((ByteBuffer) v3, v17.hashCode(), v18.hashCode());
            if (a != null) {
                for (int i = v17.hashCode() - 1; i >= 0; i--) {
                    for (int j = v18.hashCode() - 1; j >= 0; j--) {
                        a[i][j] *= v15.hashCode() / 255f;
                    }
                }
                v12 = ImageHandler.toImage(new int[][][]{r, g, b, a}, true);
                v13 = S(v19, v12);
            }

            v7 = v7.hashCode() + 1;
            v6 = v6.hashCode() - 1;
        }
        v9 = P(v1, v2);
        return v9;
    }

    public static Object M(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = Math.abs(v1.hashCode());
        while (v3.hashCode() > 0) {
            v4 = N(v2, 2);
            v5 = N(v2, 2);
            v6 = N(v2, 2);
            v3 = v3.hashCode() - 1;
        }
        v7 = R(v2, 8);
        v8 = R(v2, 1);
        v9 = R(v2, 3);
        return v8;
    }

    public static Object N(Object o1, Object o2) {
        if (o1 == null) return o2;
        if (o1 instanceof PSDData) {
            PSDData data = (PSDData) o1;
            for (int i = o2.hashCode(), size = data.size() - 2; i < size; i++) {
                o1 = data.get(i);
                if (o1.hashCode() != 1819635305) continue;
                o2 = data.get(i + 2);
                if (o2 instanceof byte[]) {
                    byte[] src = (byte[]) o2;
                    int j = (src[3] & 0xff) | ((src[2] << 8) & 0xff00) | ((src[1] << 24) >>> 8) | (src[0] << 24);
                    byte[] des = new byte[j + j];
                    System.arraycopy(src, 4, des, 0, des.length);
                    try {
                        return new String(des, "unicode");
                    } catch (UnsupportedEncodingException e) {
                        return "";
                    }
                } else return "";
            }
            return "";
        }
        if (o1 instanceof File) return new FileReader((File) o1);
        if (o1 instanceof FileReader) return ((FileReader) o1).open();
        if (o1 instanceof MappedByteBuffer) {
            if (o2 instanceof byte[]) return ((MappedByteBuffer) o1).get((byte[]) o2);
            if (o2 instanceof Integer)
                return ((MappedByteBuffer) o1).position(((MappedByteBuffer) o1).position() + (Integer) o2);
            return ((MappedByteBuffer) o1).position();
        }
        if (o1 instanceof LayerImageData) ((LayerImageData) o1).setName((String) o2);

        return o1.hashCode();
    }

    public static Object P(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v7, v6, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = J(v1, v2);
        v4 = R(v2, 0);
        v5 = R(v2, 1);
        v6 = R(v2, 3);
        v7 = 4;
        v8 = 0;
        while (v6.hashCode() > 0) {
            v9 = R(v2, v7);
            Object v10, v11, v12, v13, v14, v15, v16, v17, v18, v19;
            v10 = v8;
            v11 = 0;
            for (int i = 7; true; i++) {
                v12 = R(v9, i);
                if (v12 instanceof LayerImageData) break;
                if (v12.hashCode() == 1819501428 || v12.hashCode() == 1819501675) v11 = 1;
                if (v12.hashCode() == 1417237352) v11 = 2;
            }
            if (((LayerImageData) v12).getName().hashCode() == -126892447) {
                v11 = -v11.hashCode();
                v8 = v8.hashCode() + 1;
            } else if (v11.hashCode() == 1) {
                v8 = v8.hashCode() - 1;
                v10 = v8;
            }
            v12 = J(v9, v10);
            v13 = J(v9, v11);

            v7 = v7.hashCode() + 1;
            v6 = v6.hashCode() - 1;
        }
        v9 = null;
        return v9;
    }

    public static Object R(Object o1, Object o2) {
        if (o1 == null) return o2;
        if (o1 instanceof MappedByteBuffer) {
            int o3 = (int) o2;
            if (o3 == 1) o2 = ((MappedByteBuffer) o1).get();
            else if (o3 == 2) o2 = ((MappedByteBuffer) o1).getShort();
            else if (o3 == 4) o2 = ((MappedByteBuffer) o1).getInt();
            else {
                o2 = new byte[(Integer) o2];
                ((MappedByteBuffer) o1).get((byte[]) o2);
            }
            return o2;
        }
        if (o1 instanceof PSDData) return ((PSDData) o1).get(o2);

        return o1.hashCode();
    }

    public static Object S(Object o1, Object o2) {
        if (o1 == null) return o2;
        if (o1 instanceof EntityData) return PSDData.create(o1, o2);
        if (o1 instanceof PSDData) {
            o2 = new PSDData();
            J(o1, o2);
            return o2;
        }
        if (o1 instanceof FileReader) return ((FileReader) o1).getBufferPointer();
        if (o1 instanceof MappedByteBuffer) {
            if (o2 instanceof byte[]) return ((MappedByteBuffer) o1).get((byte[]) o2);
            if (o2 instanceof Integer) return ((MappedByteBuffer) o1).position((Integer) o2);
            return ((MappedByteBuffer) o1).position();
        }
        if (o1 instanceof LayerImageData) ((LayerImageData) o1).setImg((Image) o2);
        return o1.hashCode();
    }

    public static Object X(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = R(v3, 4);
        v5 = S(v3, v3);
        v6 = R(v3, 4);
        v7 = N(v3, v6);
        v8 = R(v7, 4);
        v9 = N(v7, v8);
        Object v10, v11, v12, v13, v14, v15, v16, v17, v18, v19;
        v10 = D(v1, v2);
        v11 = v4.hashCode() + v5.hashCode();
        while (v10.hashCode() < v11.hashCode()) {
            v12 = R(v9, 4);
            v13 = R(v9, 4);
            v14 = J(v2, v13);
            v15 = J(v2, v10);
            v16 = R(v9, 4);
            v17 = R(v9, v16.hashCode());
            v18 = J(v2, v17);
            v10 = S(v9, v9);
        }
        v19 = J(v3, v11);
        return v19;
    }

    public static Object Y(Object o1, Object o2) {
        Object v1, v2, v3, v4, v5, v6, v7, v8, v9;
        v1 = o1;
        v2 = o2;
        v3 = S(v1, v2);
        v4 = S(v3, v3);
        v5 = J(v2, v4);
        v6 = N(v3, 4);
        v7 = N(v6, 4);
        v8 = A(v1, v2);
        v9 = L(v1, v2);
        return v9;
    }

}
