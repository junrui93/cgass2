package ass2.spec;

import com.jogamp.opengl.GL2;

public class Camera extends GameObject {

    public Camera(GameObject parent) {
        super(parent);
    }

    @Override
    public void draw(GL2 gl) {
        double[] tv = globalTranslation();
        double[] rv = globalRotation();
        gl.glRotated(-rv[0], 1, 0, 0);
        gl.glRotated(-rv[1], 0, 1, 0);
        gl.glRotated(-rv[2], 0, 0, 1);
        gl.glTranslated(-tv[0], -tv[1], -tv[2]);
    }
}
