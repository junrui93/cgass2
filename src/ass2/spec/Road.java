package ass2.spec;

import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private static final int SAMPLES_PER_SEG = 64;
    private static final int TEX_PER_SEG = 16;
    private static final double VERY_SMALL_VALUE = 0.0000001;
    private static final String TEX_FILE_NAME = "road.jpg";
    private static final double H_INTERVAL = 0.01;

    private float[] AMBIENT = {0.5f, 0.5f, 0.5f, 1};
    private float[] DIFFUSE = {0.5f, 0.5f, 0.5f, 1};
    private float[] SPECULAR = {0.6f, 0.6f, 0.6f, 1};
    private float PHONG = 20;

    private Terrain myTerrain;
    private List<Double> myPoints;
    private double myWidth;

    private MyTexture myTexture;

    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        if (i == size()) {
            i -= 1;
            t = 1;
        } else {
            t = t - i;
        }

        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


    private double b2(int i, double t) {
        switch (i) {
            case 0:
                return (1-t) * (1-t);
            case 1:
                return 2 * (1-t) * t;
            case 2:
                return t * t;
        }
        return 0;
    }

    // get the tangent degree of point t (0 <= t <= size())
    public double tanDeg(double t) {
        int i = (int)Math.floor(t);
        if (i == size()) {
            i -= 1;
            t = 1;
        } else {
            t = t - i;
        }
        // add a very small value to smooth the start and the end point
        if (t == 0) {
            t = VERY_SMALL_VALUE;
        } else if (t >= 1) {
            t = 1 - VERY_SMALL_VALUE;
        }

        i *= 6;

        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i);

        double x = 3 * (b2(0, t) * (x1 - x0) + b2(1, t) * (x2 - x1) + b2(2, t) * (x3 - x2));
        double y = 3 * (b2(0, t) * (y1 - y0) + b2(1, t) * (y2 - y1) + b2(2, t) * (y3 - y2));

        return Math.toDegrees(Math.atan2(y, x));
    }

    public void init(GL2 gl, Terrain terrain) {
        myTexture = new MyTexture(gl, TEX_FILE_NAME);
        setMyTerrain(terrain);
    }

    public void draw(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTexture.getTextureId());

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, PHONG);

        double[] p1 = new double[2];
        double[] p2 = new double[2];
        int sampleSize = size() * SAMPLES_PER_SEG;
        double ratio = TEX_PER_SEG / SAMPLES_PER_SEG;
        double[] p = point(0);
        double alt = myTerrain.altitude(p[0], p[1]) + 0.001;

        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= sampleSize; i++) {
            double t = (double) i * size() / sampleSize;
            p = point(t);
            double angle = tanDeg(t);
            double angle1 = angle + 90;
            double dx = myWidth / 2 * Math.cos(Math.toRadians(angle1));
            double dy = myWidth / 2 * Math.sin(Math.toRadians(angle1));
            // p2 is a point on the top line of the road
            // p1 is a point on the bottom line of the road
            p1[0] = p[0] + dx;
            p1[1] = p[1] + dy;
            p2[0] = p[0] - dx;
            p2[1] = p[1] - dy;
            gl.glNormal3d(0, 1, 0); // flat road
            gl.glTexCoord2d(0, i * ratio);
            gl.glVertex3d(p2[0], alt, p2[1]);
            gl.glTexCoord2d(1, i * ratio);
            gl.glVertex3d(p1[0], alt, p1[1]);
        }
        gl.glEnd();

        // try to extrude the road over the terrain, not good
//        int rows = (int) Math.ceil(myWidth / H_INTERVAL) + 1;
//        double[][][] points = new double[rows][sampleSize + 1][3];
//        for (int i = 0; i <= sampleSize; i++) {
//            double t = (double) i * size() / sampleSize;
//            p = point(t);
//            double angle = tanDeg(t) + 90;
//            double cos = Math.cos(Math.toRadians(angle));
//            double sin = Math.sin(Math.toRadians(angle));
//            double[] top = new double[3];
//            top[0] = p[0] - myWidth / 2 * cos;
//            top[2] = p[1] - myWidth / 2 * sin;
//            top[1] = myTerrain.altitude(top[0], top[2]);
//            points[0][i] = top.clone();
//            for (int j = 1; j < rows; j++) {
//                double h = j * H_INTERVAL;
//                h = Math.min(myWidth, h);
//                double[] mid = new double[3];
//                mid[0] = top[0] + h * cos;
//                mid[2] = top[2] + h * sin;
//                mid[1] = myTerrain.altitude(mid[0], mid[2]);
//                points[j][i] = mid.clone();
//            }
//        }
//
//        gl.glBegin(GL2.GL_TRIANGLES);
//        for (int i = 0; i < points.length - 1; i++) {
//            for (int j = 0; j < points[0].length - 1; j++) {
//                double[] v1 = points[i][j];
//                double[] v2 = points[i + 1][j];
//                double[] v3 = points[i + 1][j + 1];
//                double[] v4 = points[i][j + 1];
//
//                double[] normal1 = MathUtils.getNormal(v1, v2, v3);
//                double[] normal2 = MathUtils.getNormal(v1, v3, v4);
//
//                gl.glNormal3dv(normal1, 0);
//                gl.glTexCoord2d((double) i / points.length, j * ratio);
//                gl.glVertex3dv(v1, 0);
//                gl.glTexCoord2d((double) (i + 1) / points.length, j * ratio);
//                gl.glVertex3dv(v2, 0);
//                gl.glTexCoord2d((double) (i + 1) / points.length, (j + 1) * ratio);
//                gl.glVertex3dv(v3, 0);
//
//                gl.glNormal3dv(normal2, 0);
//                gl.glTexCoord2d((double) i / points.length, j * ratio);
//                gl.glVertex3dv(v1, 0);
//                gl.glTexCoord2d((double) (i + 1) / points.length, (j + 1) * ratio);
//                gl.glVertex3dv(v3, 0);
//                gl.glTexCoord2d((double) i / points.length, (j + 1) * ratio);
//                gl.glVertex3dv(v4, 0);
//            }
//        }
//        gl.glEnd();

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
    }

    public static void main(String[] args) {
        System.out.println(Math.toDegrees(Math.atan2(-1, 0)));
    }

    public void setMyTerrain(Terrain myTerrain) {
        this.myTerrain = myTerrain;
    }
}
