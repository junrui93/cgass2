package ass2.spec;

import com.jogamp.opengl.GL2;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private static final int STACKS = 32;
    private static final int SLICES = 32;
    private static final double CYLINDER_RADIUS = 0.1;
    private static final double SPHERE_RADIUS = 0.5;
    private static final double HEIGHT = 1;

    private static final String BARK_TEX_FILE_NAME = "bark.png";
    private static final String LEAF_TEX_FILE_NAME = "leaf2.jpg";

    private static final float[] BARK_AMBIENT = {0.5f, 0.5f, 0.5f, 1};
    private static final float[] BARK_DIFFUSE = {0.5f, 0.5f, 0.5f, 1};
    private static final float[] BARK_SPECULAR = {0.6f, 0.6f, 0.6f, 1};
    private static final float BARK_PHONG = 20;

    private static final float[] LEAF_AMBIENT = {0.6f, 0.6f, 0.6f, 1};
    private static final float[] LEAF_DIFFUSE = {0.6f, 0.6f, 0.6f, 1};
    private static final float[] LEAF_SPECULAR = {0.1f, 0.1f, 0.1f, 1};
    private static final float LEAF_PHONG = 10;

    private double[] myPos;

    private MyTexture barkTexture;
    private MyTexture leafTexture;
    private MyTexture barkNormal;

    private int shaderProgram;
    private boolean useNormalMap = true;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
    }
    
    public double[] getPosition() {
        return myPos;
    }

    public void init(GL2 gl) {
        barkTexture = new MyTexture(gl, BARK_TEX_FILE_NAME);
        leafTexture = new MyTexture(gl, LEAF_TEX_FILE_NAME);
        barkNormal = new MyTexture(gl, "bark_normal.png");

        try {
            shaderProgram = Shader.initShaders(gl, "/shader/bark_vshader.glsl", "/shader/bark_fshader.glsl");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void draw(GL2 gl) {
        double angle = 0;
        double angleInterval = 2 * Math.PI / SLICES;

        gl.glPushMatrix();

        gl.glBindTexture(GL2.GL_TEXTURE_2D, barkTexture.getTextureId());

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, BARK_AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, BARK_DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, BARK_SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, BARK_PHONG);

        gl.glTranslated(myPos[0], myPos[1], myPos[2]);

        // draw bottom circle of the trunk
        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3d(0, -1, 0);
        gl.glTexCoord2d(0, 0);
        gl.glVertex3d(0, 0, 0);
        for (int i = 0; i <= SLICES; i++) {
            double x = CYLINDER_RADIUS * Math.cos(angle);
            double z = CYLINDER_RADIUS * Math.sin(angle);
            gl.glTexCoord2d(x, z);
            gl.glVertex3d(x, 0, z);
            angle += angleInterval;
        }
        gl.glEnd();

        if (useNormalMap)
            gl.glUseProgram(shaderProgram);

        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "textureMap"), 0);
        gl.glActiveTexture(GL2.GL_TEXTURE1);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, barkNormal.getTextureId());
        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "normalMap"), 1);
        int bitangentId = gl.glGetAttribLocation(shaderProgram, "bitangent");

        // draw the trunk surface
        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        //double texTop = HEIGHT / CYLINDER_RADIUS;
        double texTop = 3;
        for (int i = 0; i <= SLICES; i++) {
            double x = CYLINDER_RADIUS * Math.cos(angle);
            double z = CYLINDER_RADIUS * Math.sin(angle);
            gl.glNormal3d(Math.cos(angle), 0, Math.sin(angle));
            gl.glTexCoord2d((double) 2 * i / SLICES, 0);
            gl.glVertex3d(x, 0, z);
            gl.glTexCoord2d((double) 2 * i / SLICES, texTop);
            gl.glVertex3d(x, HEIGHT, z);
            gl.glVertexAttrib3d(bitangentId, 0, 1, 0);
            angle += angleInterval;
        }
        gl.glEnd();

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glActiveTexture(GL2.GL_TEXTURE0);

        gl.glUseProgram(0);

        // draw upper circle of the trunk
        gl.glTranslated(0, HEIGHT, 0);

        gl.glBegin(GL2.GL_TRIANGLE_FAN);
        gl.glNormal3d(0, 1, 0);
        gl.glTexCoord2d(0, 0);
        gl.glVertex3d(0, 0, 0);
        for (int i = 0; i <= SLICES; i++) {
            double x = CYLINDER_RADIUS * Math.cos(angle);
            double z = CYLINDER_RADIUS * Math.sin(angle);
            gl.glTexCoord2d(x, z);
            gl.glVertex3d(x, 0, z);
            angle -= angleInterval;
        }
        gl.glEnd();

        // draw leaves
        gl.glTranslated(0, SPHERE_RADIUS, 0);

        gl.glBindTexture(GL2.GL_TEXTURE_2D, leafTexture.getTextureId());

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, LEAF_AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, LEAF_DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, LEAF_SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, LEAF_PHONG);

        drawSphere(gl, SPHERE_RADIUS);

        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

        gl.glPopMatrix();
    }

    private double r(double t){
        return Math.cos(2 * Math.PI * t);
    }

    private double getY(double t){
        return Math.sin(2 * Math.PI * t);
    }

    private void drawSphere(GL2 gl, double radius){
        double deltaT;

        deltaT = 0.5/ STACKS;
        double ang;
        double delang = 360.0 / SLICES;
        double x1,x2,z1,z2,y1,y2;
        for (int i = 0; i < STACKS; i++)
        {
            double t = -0.25 + i*deltaT;

            gl.glBegin(GL2.GL_TRIANGLE_STRIP);
            for(int j = 0; j <= SLICES; j++)
            {
                ang = j*delang;
                x1=radius * r(t)*Math.cos(ang*2.0*Math.PI/360.0);
                x2=radius * r(t+deltaT)*Math.cos(ang*2.0*Math.PI/360.0);
                y1 = radius * getY(t);

                z1=radius * r(t)*Math.sin(ang*2.0*Math.PI/360.0);
                z2= radius * r(t+deltaT)*Math.sin(ang*2.0*Math.PI/360.0);
                y2 = radius * getY(t+deltaT);

                double normal[] = {x1,y1,z1};

                MathUtils.normalize(normal);

                gl.glNormal3dv(normal,0);
                double tCoord = 1.0/STACKS * i * 3; //Or * 2 to repeat label
                double sCoord = 1.0/SLICES * j * 3;
                gl.glTexCoord2d(sCoord,tCoord);
                gl.glVertex3d(x1,y1,z1);
                normal[0] = x2;
                normal[1] = y2;
                normal[2] = z2;

                MathUtils.normalize(normal);
                gl.glNormal3dv(normal,0);
                tCoord = 1.0/STACKS * (i+1) * 3; //Or * 2 to repeat label
                gl.glTexCoord2d(sCoord,tCoord);
                gl.glVertex3d(x2,y2,z2);

            };
            gl.glEnd();
        }
    }

    public void setUseNormalMap(boolean useNormalMap) {
        this.useNormalMap = useNormalMap;
    }

    public boolean getUseNormalMap() {
        return useNormalMap;
    }
}
