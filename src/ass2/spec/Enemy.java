package ass2.spec;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Enemy extends GameObject {

    private float[] AMBIENT = {0.4f, 0.4f, 0.4f, 1};
    private float[] DIFFUSE = {0.5f, 0.5f, 0.5f, 1};
    private float[] SPECULAR = {0.8f, 0.8f, 0.8f, 1};
    private float PHONG = 10;

    private int shaderProgram;
    private int[] bufferIds = new int[3];
    private double[] vertices;
    private double[] texCoords;
    private double[] normals;
    private MyTexture myTexture;

    private int texUnit;

    public void init(GL2 gl) {
        loadObj();

        gl.glGenBuffers(3, bufferIds, 0);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[0]);
        DoubleBuffer verticesBuffer = Buffers.newDirectDoubleBuffer(vertices);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.length * Double.BYTES, verticesBuffer, GL2.GL_STATIC_DRAW);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[1]);
        DoubleBuffer normalsBuffer = Buffers.newDirectDoubleBuffer(normals);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, normals.length * Double.BYTES, normalsBuffer, GL2.GL_STATIC_DRAW);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[2]);
        DoubleBuffer texCoordsBuffer = Buffers.newDirectDoubleBuffer(texCoords);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, texCoords.length * Double.BYTES, texCoordsBuffer, GL2.GL_STATIC_DRAW);

        try {
            shaderProgram = Shader.initShaders(gl, "/shader/enemy_vshader.glsl", "/shader/enemy_fshader.glsl");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        texUnit = gl.glGetUniformLocation(shaderProgram, "texUnit");

        myTexture = new MyTexture(gl, "uvmap.jpg");
    }

    @Override
    public void drawSelf(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, myTexture.getTextureId());

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, AMBIENT, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, DIFFUSE, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, SPECULAR, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, PHONG);

        gl.glUseProgram(shaderProgram);

        gl.glUniform1i(texUnit, 0);

        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[0]);
        gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, 0);

        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[1]);
        gl.glNormalPointer(GL2.GL_DOUBLE, 0, 0);

        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIds[2]);
        gl.glTexCoordPointer(2, GL2.GL_DOUBLE, 0, 0);

        gl.glDrawArrays(GL2.GL_TRIANGLES, 0, vertices.length / 3);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

        gl.glUseProgram(0);
    }

    private void loadObj() {
        Scanner scanner = new Scanner(getClass().getResourceAsStream("/texture/suzanne.obj"));

        List<Vec3> v = new ArrayList<>();
        List<Vec2> vt = new ArrayList<>();
        List<Vec3> vn = new ArrayList<>();
        List<Vec3i> f = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("vt")) {
                String[] words = line.split(" ");
                Vec2 vec2 = new Vec2();
                vec2.x = Double.valueOf(words[1]);
                vec2.y = Double.valueOf(words[2]);
                vt.add(vec2);
            } else if (line.startsWith("vn")) {
                String[] words = line.split(" ");
                Vec3 vec3 = new Vec3();
                vec3.x = Double.valueOf(words[1]);
                vec3.y = Double.valueOf(words[2]);
                vec3.z = Double.valueOf(words[3]);
                vn.add(vec3);
            } else if (line.startsWith("v")) {
                String[] words = line.split(" ");
                Vec3 vec3 = new Vec3();
                vec3.x = Double.valueOf(words[1]);
                vec3.y = Double.valueOf(words[2]);
                vec3.z = Double.valueOf(words[3]);
                v.add(vec3);
            } else if (line.startsWith("f")) {
                String[] words = line.split(" ");
                for (int i = 1; i <= 3; i++) {
                    String[] numbers = words[i].split("/");
                    Vec3i vec3 = new Vec3i();
                    vec3.x = Integer.parseInt(numbers[0]);
                    vec3.y = Integer.parseInt(numbers[1]);
                    vec3.z = Integer.parseInt(numbers[2]);
                    f.add(vec3);
                }
            }
        }

        vertices = new double[3 * f.size()];
        normals = new double[3 * f.size()];
        texCoords = new double[2 * f.size()];

        for (int i = 0; i < f.size(); i++) {
            int a = 3 * i;
            int b = 2 * i;
            Vec3i point = f.get(i);
            Vec3 vertex = v.get(point.x - 1);
            vertices[a] = vertex.x;
            vertices[a + 1] = vertex.y;
            vertices[a + 2] = vertex.z;
            Vec2 texCoord = vt.get(point.y - 1);
            texCoords[b] = texCoord.x;
            texCoords[b + 1] = texCoord.y;
            Vec3 normal = vn.get(point.z - 1);
            normals[a] = normal.x;
            normals[a + 1] = normal.y;
            normals[a + 2] = normal.z;
        }
    }
}

class Vec3 {
    public double x;
    public double y;
    public double z;
}

class Vec2 {
    public double x;
    public double y;
}

class Vec3i {
    public int x;
    public int y;
    public int z;
}