package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class Hero extends GameObject {

    private float[] AMBIENT = {0.6f, 0.6f, 0.6f, 1};
    private float[] DIFFUSE = {0.6f, 0.6f, 0.6f, 1};
    private float[] SPECULAR = {0.8f, 0.8f, 0.8f, 1};
    private float PHONG = 10;

    private static final double STEP_LENGTH = 0.4;
    private static final int TURN_ANGLE = 5;
    private static final double HERO_SIZE = 0.1;

    private double originalHeight = HERO_SIZE;

    private Terrain terrain;
    private MyTexture myTexture;

    public Hero(Terrain terrain) {
        this.terrain = terrain;
        getTranslation()[1] = HERO_SIZE + terrain.altitude(0, 0);
    }

    public void init(GL2 gl) {
        myTexture = new MyTexture(gl, "metal.jpg");
    }

    @Override
    public void drawSelf(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTexture.getTextureId());

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, PHONG);

        GLUT glut = new GLUT();
        gl.glFrontFace(GL2.GL_CW);
        glut.glutSolidTeapot(HERO_SIZE);
        gl.glFrontFace(GL2.GL_CCW);
    }

    public void turnLeft() {
        getRotation()[1] += TURN_ANGLE;
    }

    public void turnRight() {
        getRotation()[1] -= TURN_ANGLE;
    }

    public void moveForward() {
        double degree = getRotation()[1];
        double rad = Math.toRadians(degree);
        getTranslation()[0] += STEP_LENGTH * Math.sin(rad);
        getTranslation()[2] += STEP_LENGTH * Math.cos(rad);
        getTranslation()[0] = MathUtils.clamp(getTranslation()[0], 0, terrain.size().width - 1);
        getTranslation()[2] = MathUtils.clamp(getTranslation()[2], 0, terrain.size().height - 1);
        getTranslation()[1] = HERO_SIZE + terrain.altitude(getTranslation()[0], getTranslation()[2]);
    }

    public void moveBackward() {
        double degree = getRotation()[1];
        double rad = Math.toRadians(degree);
        getTranslation()[0] -= STEP_LENGTH * Math.sin(rad);
        getTranslation()[2] -= STEP_LENGTH * Math.cos(rad);
        getTranslation()[0] = MathUtils.clamp(getTranslation()[0], 0, terrain.size().width - 1);
        getTranslation()[2] = MathUtils.clamp(getTranslation()[2], 0, terrain.size().height - 1);
        getTranslation()[1] = HERO_SIZE + terrain.altitude(getTranslation()[0], getTranslation()[2]);
    }

    public double getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(double originalHeight) {
        this.originalHeight = originalHeight;
        getTranslation()[1] = originalHeight;
    }
}
