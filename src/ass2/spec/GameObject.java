package ass2.spec;

import com.jogamp.opengl.GL2;

public class GameObject {

    private double[] translation = new double[3];
    private double[] rotation = new double[3];
    private double scale = 1;

    private GameObject parent;

    public GameObject() {}

    public GameObject(GameObject parent) {
        this.parent = parent;
    }

    public double[][] translationMatrix() {
        double[][] mat = MathUtils.identity(4);
        for (int i = 0; i < 3; i++)
            mat[i][3] = translation[i];
        return mat;
    }

    public double[][] rotationMatrix() {
        double[][] rx = MathUtils.identity(4);
        rx[0][0] = Math.cos(Math.toRadians(rotation[0]));
        rx[0][1] = Math.sin(Math.toRadians(rotation[0]));
        rx[1][0] = -rx[0][1];
        rx[1][1] = rx[0][0];

        double[][] ry = MathUtils.identity(4);
        ry[0][0] = Math.cos(Math.toRadians(rotation[1]));
        ry[0][2] = Math.sin(Math.toRadians(rotation[1]));
        ry[2][0] = -ry[0][2];
        ry[2][2] = ry[0][0];

        double[][] rz = MathUtils.identity(4);
        rz[1][1] = Math.cos(Math.toRadians(rotation[2]));
        rz[1][2] = Math.sin(Math.toRadians(rotation[2]));
        rz[2][1] = -rz[1][2];
        rz[2][2] = rz[1][1];

        return MathUtils.mult(rx, MathUtils.mult(ry, rz));
    }

    public double[][] scaleMatrix() {
        double[][] mat = MathUtils.identity(4);
        for (int i = 0; i < 3; i++)
            mat[i][i] = scale;
        return mat;
    }

    public double[][] modelMatrix() {
        double[][] t = translationMatrix();
        double[][] r = rotationMatrix();
        double[][] s = scaleMatrix();
        return MathUtils.mult(t, MathUtils.mult(r, s));
    }

    public double[][] globalModelMatrix() {
        double[][] m = modelMatrix();
        if (parent == null) {
            return m;
        }
        return MathUtils.mult(parent.globalModelMatrix(), m);
    }

    public double[] globalTranslation() {
        double[] tv = getTranslation();
        if (parent == null) {
            return tv;
        }
        double[][] pm = parent.globalModelMatrix();
        double[] tv2 = new double[] {tv[0], tv[1], tv[2], 1};
        double[] res = MathUtils.mult(pm, tv2);
        return new double[] {res[0], res[1], res[2]};
    }

    public double[] globalRotation() {
        double[] r = getRotation();
        double[] pr = getParent().getRotation();
        double[] gr = new double[r.length];
        for (int i = 0; i < r.length; i++) {
            gr[i] = r[i] + pr[i];
        }
        return gr;
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();

        double[] t = getTranslation();
        double[] r = getRotation();
        double s = getScale();
        gl.glTranslated(t[0], t[1], t[2]);
        gl.glRotated(r[0], 1, 0, 0);
        gl.glRotated(r[1], 0, 1, 0);
        gl.glRotated(r[2], 0, 0, 1);
        gl.glScaled(s, s, s);

        drawSelf(gl);

        gl.glPopMatrix();
    }

    public void drawSelf(GL2 gl) {}

    public double[] getTranslation() {
        return translation;
    }

    public void setTranslation(double[] translation) {
        this.translation = translation;
    }

    public double[] getRotation() {
        return rotation;
    }

    public void setRotation(double[] rotation) {
        this.rotation = rotation;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public GameObject getParent() {
        return parent;
    }

    public void setParent(GameObject parent) {
        this.parent = parent;
    }
}
