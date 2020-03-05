package com.jarlure.layoutcreator.entrance;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.NativeLibraryLoader;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.awt.geom.*;
import java.util.NoSuchElementException;

public class Main {

    public static void main(String[] args) {
        //程序界面尺寸：固定宽高
        int width = 1162;
        int height = 691;
        //创建程序界面自定义外框（左上右上角是圆角，左下右下角是直角的矩形框）
        Frame frame = Helper.createFrame(width, height);
        SimpleApplication app = new MyApplication(frame);
        //设置程序参数配置
        AppSettings settings = new AppSettings(true);
        settings.setWidth(width);
        settings.setHeight(height);
        settings.setFrameRate(30);//固定刷新频率为30帧每秒：减少不必要的内存开销和CPU、GPU资源占用
        //提交程序参数配置，启动程序
        app.setSettings(settings);
        app.setShowSettings(false);//不显示程序参数配置界面：不允许用户调整参数
//        app.start(JmeContext.Type.Canvas);
        app.start();
    }

    private static class Helper {

        private static Frame createFrame(int width, int height) {
            Frame frame = new Frame();
            //将程序界面注册到lwjgl：以后lwjgl将会把画面渲染到该界面上
            Helper.addCanvas(frame);
            //不使用标题栏
            frame.setUndecorated(true);
            //设置程序界面尺寸
            frame.setSize(width, height);
            //居中显示程序界面
            Helper.center(frame);
            //设置程序界面边框外形
            frame.setShape(new TopRoundRectangle2D(0,0,width,height,12,12));
            return frame;
        }

        private static void addCanvas(Window window) {
            NativeLibraryLoader.loadNativeLibrary("lwjgl", true);
            Canvas canvas = new Canvas();
            window.add(canvas);
            try {
                Display.setParent(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void center(Window window) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension screenSize = tk.getScreenSize();
            int screenHeight = screenSize.height;
            int screenWidth = screenSize.width;
            window.setLocation((screenWidth - window.getWidth()) / 2, (screenHeight - window.getHeight()) / 2);
        }

    }

    /**
     * 左上角右上角是圆角左下角右下角是直角的矩形形状
     */
    private static class TopRoundRectangle2D implements Shape {

        private RoundRectangle2D shape;

        public TopRoundRectangle2D(double x, double y, double w, double h, double arcw, double arch){
            shape = new RoundRectangle2D.Double(x, y, w, h, arcw, arch);
        }

        @Override
        public Rectangle getBounds() {
            return shape.getBounds();
        }

        @Override
        public Rectangle2D getBounds2D() {
            return shape.getBounds2D();
        }

        @Override
        public boolean contains(double x, double y) {
            double x0 = shape.getX();
            if (x<x0)return false;
            if (x0+shape.getWidth()<x)return false;
            double y0 = shape.getY();
            if (y<y0)return false;
            if (y0+shape.getHeight()<y)return false;
            if (y0+shape.getHeight()/2<y)return true;

            return shape.contains(x,y);
        }

        @Override
        public boolean contains(Point2D p) {
            return contains(p.getX(),p.getY());
        }

        @Override
        public boolean intersects(double x, double y, double w, double h) {
            return shape.intersects(x,y,w,h);
        }

        @Override
        public boolean intersects(Rectangle2D r) {
            return shape.intersects(r);
        }

        @Override
        public boolean contains(double x, double y, double w, double h) {
            if (shape.isEmpty() || w <= 0 || h <= 0) {
                return false;
            }
            return (contains(x, y) &&
                    contains(x + w, y) &&
                    contains(x, y + h) &&
                    contains(x + w, y + h));
        }

        @Override
        public boolean contains(Rectangle2D r) {
            return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at) {
            return new TopRoundRectIterator(shape,at);
        }

        @Override
        public PathIterator getPathIterator(AffineTransform at, double flatness) {
            return new FlatteningPathIterator(getPathIterator(at), flatness);
        }

    }

    private static class TopRoundRectIterator implements PathIterator {
        double x, y, w, h, aw, ah;
        AffineTransform affine;
        int index;

        TopRoundRectIterator(RoundRectangle2D rr, AffineTransform at) {
            this.x = rr.getX();
            this.y = rr.getY();
            this.w = rr.getWidth();
            this.h = rr.getHeight();
            this.aw = Math.min(w, Math.abs(rr.getArcWidth()));
            this.ah = Math.min(h, Math.abs(rr.getArcHeight()));
            this.affine = at;
            if (aw < 0 || ah < 0) {
                // Don't draw anything...
                index = ctrlpts.length;
            }
        }

        /**
         * Return the winding rule for determining the insideness of the
         * path.
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         */
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        /**
         * Tests if there are more points to read.
         * @return true if there are more points to read
         */
        public boolean isDone() {
            return index >= ctrlpts.length;
        }

        /**
         * Moves the iterator to the next segment of the path forwards
         * along the primary direction of traversal as long as there are
         * more points in that direction.
         */
        public void next() {
            index++;
        }

        private static final double angle = Math.PI / 4.0;
        private static final double a = 1.0 - Math.cos(angle);
        private static final double b = Math.tan(angle);
        private static final double c = Math.sqrt(1.0 + b * b) - 1 + a;
        private static final double cv = 4.0 / 3.0 * a * b / c;
        private static final double acv = (1.0 - cv) / 2.0;

        // For each array:
        //     4 values for each point {v0, v1, v2, v3}:
        //         point = (x + v0 * w + v1 * arcWidth,
        //                  y + v2 * h + v3 * arcHeight);
        private static double ctrlpts[][] = {
                {  0.0,  0.0,  0.0,  0.5 },
                {  0.0,  0.0,  1.0, 0.0},
                {  1.0,  0.0,  1.0, 0.0 },
                {  1.0,  0.0,  0.0,  0.5 },
                {  1.0,  0.0,  0.0,  acv,
                        1.0, -acv,  0.0,  0.0,
                        1.0, -0.5,  0.0,  0.0 },
                {  0.0,  0.5,  0.0,  0.0 },
                {  0.0,  acv,  0.0,  0.0,
                        0.0,  0.0,  0.0,  acv,
                        0.0,  0.0,  0.0,  0.5 },
                {},
        };
        private static int types[] = {
                SEG_MOVETO,
                SEG_LINETO,
                SEG_LINETO,
                SEG_LINETO, SEG_CUBICTO,
                SEG_LINETO, SEG_CUBICTO,
                SEG_CLOSE,
        };

        /**
         * Returns the coordinates and type of the current path segment in
         * the iteration.
         * The return value is the path segment type:
         * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
         * A float array of length 6 must be passed in and may be used to
         * store the coordinates of the point(s).
         * Each point is stored as a pair of float x,y coordinates.
         * SEG_MOVETO and SEG_LINETO types will return one point,
         * SEG_QUADTO will return two points,
         * SEG_CUBICTO will return 3 points
         * and SEG_CLOSE will not return any points.
         * @see #SEG_MOVETO
         * @see #SEG_LINETO
         * @see #SEG_QUADTO
         * @see #SEG_CUBICTO
         * @see #SEG_CLOSE
         */
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("roundrect iterator out of bounds");
            }
            double ctrls[] = ctrlpts[index];
            int nc = 0;
            for (int i = 0; i < ctrls.length; i += 4) {
                coords[nc++] = (float) (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
                coords[nc++] = (float) (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
            }
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, nc / 2);
            }
            return types[index];
        }

        /**
         * Returns the coordinates and type of the current path segment in
         * the iteration.
         * The return value is the path segment type:
         * SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CUBICTO, or SEG_CLOSE.
         * A double array of length 6 must be passed in and may be used to
         * store the coordinates of the point(s).
         * Each point is stored as a pair of double x,y coordinates.
         * SEG_MOVETO and SEG_LINETO types will return one point,
         * SEG_QUADTO will return two points,
         * SEG_CUBICTO will return 3 points
         * and SEG_CLOSE will not return any points.
         * @see #SEG_MOVETO
         * @see #SEG_LINETO
         * @see #SEG_QUADTO
         * @see #SEG_CUBICTO
         * @see #SEG_CLOSE
         */
        public int currentSegment(double[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("roundrect iterator out of bounds");
            }
            double ctrls[] = ctrlpts[index];
            int nc = 0;
            for (int i = 0; i < ctrls.length; i += 4) {
                coords[nc++] = (x + ctrls[i + 0] * w + ctrls[i + 1] * aw);
                coords[nc++] = (y + ctrls[i + 2] * h + ctrls[i + 3] * ah);
            }
            if (affine != null) {
                affine.transform(coords, 0, coords, 0, nc / 2);
            }
            return types[index];
        }
    }

}
