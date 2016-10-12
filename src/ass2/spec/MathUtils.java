package ass2.spec;

public class MathUtils {

    public static double[] lerp(double[] p1, double[] p2, double f) {
        int len = Math.min(p1.length, p2.length);
        double[] p = new double[len];
        for (int i = 0; i < len; i++) {
            p[i] = p1[i] + f * (p2[i] - p1[i]);
        }
        return p;
    }

    public static double dist(double[] p1, double[] p2) {
        double sum = 0;
        int len = Math.min(p1.length, p2.length);
        for (int i = 0; i < len; i++) {
            sum += (p1[i] - p2[i]) * (p1[i] - p2[i]);
        }
        return Math.sqrt(sum);
    }

    public static double getMagnitude(double [] n){
        double mag = 0;
        for (int i = 0; i < n.length; i++) {
            mag += n[i] * n[i];
        }
        mag = Math.sqrt(mag);
        return mag;
    }

    public static void normalize(double [] n){
        double  mag = getMagnitude(n);
        for (int i = 0; i < n.length; i++) {
            n[i] /= mag;
        }
    }

    public static double[] cross(double u [], double v[]){
        double crossProduct[] = new double[3];
        crossProduct[0] = u[1]*v[2] - u[2]*v[1];
        crossProduct[1] = u[2]*v[0] - u[0]*v[2];
        crossProduct[2] = u[0]*v[1] - u[1]*v[0];
        return crossProduct;
    }

    public static double[] getNormal(double[] p0, double[] p1, double[] p2){
        double u[] = {p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2]};
        double v[] = {p2[0] - p0[0], p2[1] - p0[1], p2[2] - p0[2]};
        double[] normal = cross(u,v);
        normalize(normal);
        return normal;
    }

    public static void add(double[] p1, double[] p2) {
        assert p1.length == p2.length;
        int len = p1.length;
        for (int i = 0; i < len; i++) {
            p1[i] += p2[i];
        }
    }

    public static void sub(double[] p1, double[] p2) {
        assert p1.length == p2.length;
        int len = p1.length;
        for (int i = 0; i < len; i++) {
            p1[i] -= p2[i];
        }
    }

    public static void mult(double[] p, double val) {
        for (int i = 0; i < p.length; i++) {
            p[i] *= val;
        }
    }

    public static double[][] mult(double[][] p, double[][] q) {
        assert p.length == q.length;
        assert p.length == p[0].length;
        assert q.length == q[0].length;
        int len = p.length;

        double[][] m = new double[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                m[i][j] = 0;
                for (int k = 0; k < len; k++) {
                    m[i][j] += p[i][k] * q[k][j];
                }
            }
        }
        return m;
    }

    public static double[] mult(double[][] m, double[] v) {
        assert m.length == m[0].length;
        assert m.length == v.length;
        int len = v.length;

        double[] u = new double[len];
        for (int i = 0; i < len; i++) {
            u[i] = 0;
            for (int j = 0; j < len; j++) {
                u[i] += m[i][j] * v[j];
            }
        }
        return u;
    }

    public static double[][] identity(int len) {
        double[][] mat = new double[len][len];
        for (int i = 0; i < len; i++) {
            mat[i][i] = 1;
        }
        return mat;
    }

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    // calculate tangent vector from vertices and texture coordinates from a triangle
    // hasn't been used
    public static double[] tangent(double[] p1, double[] p2, double[] p3, double[] uv1, double[] uv2, double[] uv3) {
        double[] edge1 = p2.clone();
        sub(edge1, p1);
        double[] edge2 = p3.clone();
        sub(edge2, p1);
        double[] deltaUV1 = uv2.clone();
        sub(deltaUV1, uv1);
        double[] deltaUV2 = uv3.clone();
        sub(deltaUV2, uv1);

        double f = 1.0 / (deltaUV1[0] * deltaUV2[1] - deltaUV2[0] * deltaUV1[1]);

        double[] tangent = new double[3];
        tangent[0] = f * (deltaUV2[1] * edge1[0] - deltaUV1[1] * edge2[0]);
        tangent[1] = f * (deltaUV2[1] * edge1[1] - deltaUV1[1] * edge2[1]);
        tangent[2] = f * (deltaUV2[1] * edge1[2] - deltaUV1[1] * edge2[2]);
        normalize(tangent);

        double[] bitangent = new double[3];
        bitangent[0] = f * (-deltaUV2[0] * edge1[0] + deltaUV1[0] * edge2[0]);
        bitangent[1] = f * (-deltaUV2[0] * edge1[1] + deltaUV1[0] * edge2[1]);
        bitangent[2] = f * (-deltaUV2[0] * edge1[2] + deltaUV1[0] * edge2[2]);
        normalize(bitangent);
        return tangent;
    }
}
