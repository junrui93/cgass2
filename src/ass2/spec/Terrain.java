package ass2.spec;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.awt.*;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private static final int STRIDE = 8;
    private static final String TEX_FILE_NAME = "grass1.png";
    private static final float[] AMBIENT = {0.6f, 0.6f, 0.6f, 1};
    private static final float[] DIFFUSE = {0.6f, 0.6f, 0.6f, 1};
    private static final float[] SPECULAR = {0.2f, 0.2f, 0.2f, 1};
    private static final float PHONG = 0.1f * 128;

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;
    private List<Enemy> myEnemies;

    private double[] vertices;
    private double[] normals;
    private double[] texCoords;
    private short[] indexes;

    private int[] myBufferIds = new int[4];

    private MyTexture myTexture;
    private int shaderProgram;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
        myEnemies = new ArrayList<>();
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * TO BE COMPLETED
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        x = Math.max(0, x);
        z = Math.max(0, z);
        x = Math.min(mySize.width - 1, x);
        z = Math.min(mySize.height - 1, z);
        int x1 = (int) Math.floor(x);
        int x2 = (int) Math.ceil(x);
        int z1 = (int) Math.floor(z);
        int z2 = (int) Math.ceil(z);
        double[] p = {x, z};
        double[] tl = {x1, z1};
        double[] br = {x2, z2};
        if (MathUtils.dist(p, tl) < MathUtils.dist(p, br)) {
            // top left triangle
            double[] p1 = {x1, myAltitude[x1][z2], z2};
            double[] p2 = {x1, myAltitude[x1][z1], z1};
            double[] p3 = {x2, myAltitude[x2][z1], z1};
            double[] q1;
            double[] q2;
            // first interpolate between (p1, p2) and (p1, p3), get q1 and q2
            if (z1 == z2) {
                // point is on the top line (avoid divide by zero in the else section)
                q1 = p2;
                q2 = p3;
            } else {
                q1 = MathUtils.lerp(p1, p2, (z2 - z) / (z2 - z1));
                q2 = MathUtils.lerp(p1, p3, (z2 - z) / (z2 - z1));
            }
            // then interpolate between (q1, q2)
            double[] r;
            if (q2[0] == x1) {
                r = q2;
            } else {
                r = MathUtils.lerp(q1, q2, (x - x1) / (q2[0] - x1));
            }
            return r[1];
        } else {
            // bottom right triangle, process is the same as above
            double[] p1 = {x2, myAltitude[x2][z1], z1};
            double[] p2 = {x1, myAltitude[x1][z2], z2};
            double[] p3 = {x2, myAltitude[x2][z2], z2};
            double[] q1;
            double[] q2;
            if (z1 == z2) {
                q1 = p2;
                q2 = p3;
            } else {
                q1 = MathUtils.lerp(p1, p2, (z - z1) / (z2 - z1));
                q2 = MathUtils.lerp(p1, p3, (z - z1) / (z2 - z1));
            }
            double[] r;
            if (x2 == q1[0]) {
                r = q1;
            } else {
                r = MathUtils.lerp(q1, q2, (x - q1[0]) / (x2 - q1[0]));
            }
            return r[1];
        }
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }

    public void addEnemy(double x, double z, double rotation, double scale) {
        scale *= 0.2;
        double y = scale + altitude(x, z);
        Enemy enemy = new Enemy();
        enemy.setTranslation(new double[] {x, y, z});
        enemy.getRotation()[1] = rotation;
        enemy.setScale(scale);
        myEnemies.add(enemy);
    }

    public void initAll(GL2 gl) {
        init(gl);

        for (Tree tree : trees()) {
            tree.init(gl);
        }

        for (Road road : roads()) {
            road.init(gl, this);
        }

        for (Enemy enemy : myEnemies) {
            enemy.init(gl);
        }
    }

    public void drawAll(GL2 gl) {
        draw(gl);

        for (Tree tree : trees()) {
            tree.draw(gl);
        }

        for (Road road : roads()) {
            road.draw(gl);
        }

        for (Enemy enemy : myEnemies) {
            enemy.draw(gl);
        }
    }

    private void init(GL2 gl) {
        initVerticesAndTexCoords();
        initNormalsAndIndexes();

        // cannot share vertex if we want to use face normals
        // construct another array to put all vertex positions, normals, texture coordinates

        double[] data = new double[8 * indexes.length];

        try {
            for (int i = 0; i < indexes.length; i++) {
                double[] vertex = getVertex(indexes[i]);
                double[] normal = getNormal(i / 3);
                double[] texCoord = getTexCoord(indexes[i]);

                int j = 8 * i;

                data[j] = vertex[0];
                data[j + 1] = vertex[1];
                data[j + 2] = vertex[2];

                data[j + 3] = normal[0];
                data[j + 4] = normal[1];
                data[j + 5] = normal[2];

                data[j + 6] = texCoord[0];
                data[j + 7] = texCoord[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        gl.glGenBuffers(2, myBufferIds, 0);

        DoubleBuffer dataBuffer = Buffers.newDirectDoubleBuffer(data);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, myBufferIds[0]);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, data.length * Double.BYTES, dataBuffer, GL2.GL_STATIC_DRAW);

//        ShortBuffer indexesBuffer = Buffers.newDirectShortBuffer(indexes);
//        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, myBufferIds[1]);
//        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, indexes.length * Short.BYTES, indexesBuffer, GL2.GL_STATIC_DRAW);

        myTexture = new MyTexture(gl, TEX_FILE_NAME);

        try {
            shaderProgram = Shader.initShaders(gl, "/shader/per_pixel_vshader.glsl", "/shader/per_pixel_fshader.glsl");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void draw(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTexture.getTextureId());

        gl.glUseProgram(shaderProgram);
        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "texUnit"), 0);

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, PHONG);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, myBufferIds[0]);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL2.GL_DOUBLE, 8 * Double.BYTES, 0);

        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL2.GL_DOUBLE, 8 * Double.BYTES, 3 * Double.BYTES);

        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(2, GL2.GL_DOUBLE, 8 * Double.BYTES, 6 * Double.BYTES);

        //gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, myBufferIds[1]);

        //gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        //gl.glDrawElements(GL2.GL_TRIANGLES, indexes.length, GL2.GL_UNSIGNED_SHORT, 0);
        gl.glDrawArrays(GL2.GL_TRIANGLES, 0, indexes.length);
        //gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        //gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        gl.glUseProgram(0);

        // just for debug
//        gl.glDisable(GL2.GL_TEXTURE_2D);
//        gl.glDisable(GL2.GL_LIGHTING);
//        gl.glColor3d(1, 0, 0);
//        gl.glBegin(GL2.GL_LINES);
//        for (int i = 0; i < indexes.length; i += 1) {
//            double[] v = getVertex(indexes[i]);
//            double[] normal = getNormal(i/3);
//            gl.glVertex3dv(v, 0);
//            MathUtils.mult(normal, 0.3);
//            MathUtils.add(v, normal);
//            gl.glVertex3dv(v, 0);
//        }
//        gl.glEnd();
//        gl.glEnable(GL2.GL_TEXTURE_2D);
//        gl.glEnable(GL2.GL_LIGHTING);
    }

    // number of triangles
    public int meshSize() {
        return 2 * (mySize.width - 1) * (mySize.height - 1);
    }

    // number of vertices
    private int vertexSize() {
        return mySize.width * mySize.height;
    }

    private double[] getVertex(int i) {
        return Arrays.copyOfRange(vertices, 3 * i, 3 * (i + 1));
    }

    private void setVertex(int i, double[] v) {
        assert v.length == 3;
        int b = 3 * i;
        vertices[b] = v[0];
        vertices[b + 1] = v[1];
        vertices[b + 2] = v[2];
    }

    private double[] getNormal(int i) {
        return Arrays.copyOfRange(normals, 3 * i, 3 * (i + 1));
    }

    private void setNormal(int i, double[] n) {
        assert n.length == 3;
        int b = 3 * i;
        normals[b] = n[0];
        normals[b + 1] = n[1];
        normals[b + 2] = n[2];
    }

    private double[] getTexCoord(int i) {
        return Arrays.copyOfRange(texCoords, 2 * i, 2 * (i + 1));
    }

    private void setTexCoord(int i, double[] t) {
        assert t.length == 2;
        int b = 2 * i;
        normals[b] = t[0];
        normals[b + 1] = t[1];
    }

    private void initVerticesAndTexCoords() {
        vertices = new double[3 * vertexSize()];
        texCoords = new double[2 * vertexSize()];

        int width = mySize.width;
        int height = mySize.height;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int a = 3 * (i * width + j);
                vertices[a] = j;
                vertices[a + 1] = myAltitude[j][i];
                vertices[a + 2] = i;

                int b = 2 * (i * width + j);
                texCoords[b] = j;
                texCoords[b + 1] = i;
            }
        }
    }

    private void initNormalsAndIndexes() {
        normals = new double[3 * meshSize()];
        indexes = new short[3 * meshSize()];

        int width = mySize.width;
        int height = mySize.height;

        for (int i = 0; i < height - 1; i++) {
            for (int j = 0; j < width - 1; j++) {
                int k1 = i * width + j;
                int k2 = (i + 1) * width + j;
                int k3 = (i + 1) * width + j + 1;
                int k4 = i * width + j + 1;

                double[] n1 =  MathUtils.getNormal(getVertex(k1), getVertex(k2), getVertex(k4));
                double[] n2 =  MathUtils.getNormal(getVertex(k2), getVertex(k3), getVertex(k4));

                int a = 2 * (i * (width - 1) + j);

                setNormal(a, n1);
                setNormal(a + 1, n2);

                int b = 3 * a;

                indexes[b] = (short) k1;
                indexes[b + 1] = (short) k2;
                indexes[b + 2] = (short) k4;
                indexes[b + 3] = (short) k2;
                indexes[b + 4] = (short) k3;
                indexes[b + 5] = (short) k4;
            }
        }
    }

    public void toggleUseNormalMap() {
        for (Tree tree : trees()) {
            tree.setUseNormalMap(!tree.getUseNormalMap());
        }
    }
}
