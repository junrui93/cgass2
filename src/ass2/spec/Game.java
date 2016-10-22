package ass2.spec;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private Terrain myTerrain;
    private Hero hero;
    private Camera camera;

    private boolean nightMode = false;

    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;

        hero = new Hero(myTerrain);

        // attach camera to hero (camera is a child object of the hero)
        camera = new Camera(hero);
        camera.setTranslation(new double[] {0, 0.5, -2});
        camera.setRotation(new double[] {0, 180, 0});
    }
    
    /** 
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel panel = new GLJPanel(caps);
        panel.addGLEventListener(this);
        panel.addKeyListener(this);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File(args[0]);
        //File file = new File("level.json");
        Terrain terrain = LevelIO.load(file);
        Game game = new Game(terrain);
        game.run();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_NORMALIZE);

        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        // Specify how texture values combine with current surface color values.
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);

        // Turn on OpenGL texturing.
        gl.glEnable(GL2.GL_TEXTURE_2D);

        hero.init(gl);
        myTerrain.initAll(gl);
    }

	@Override
	public void display(GLAutoDrawable drawable) {
	    GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor(1, 1, 1, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        // place the camera
        camera.draw(gl);

        // set lighting
        float[] lightDir = myTerrain.getSunlight();
        // w == 0 represents that it's a vector than a point
        // which means it's a directional light
        float[] lightDirVec = {lightDir[0], lightDir[1], lightDir[2], 0};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightDirVec, 0);
        // separate sepcular color from texture
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);

        if (nightMode) {
            // dark sky in night mode
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

            // low sunlight in night mode
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.1f, 0.1f, 0.1f, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {0.1f, 0.1f, 0.1f, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[] {0.1f, 0.1f, 0.1f, 1}, 0);

            // torch light settings
            double[] heroPos = hero.getTranslation();
            double heroAngle = Math.toRadians(hero.getRotation()[1]);

            // attach it to hero
            float[] lightPos = {
                    (float) heroPos[0] - (float) Math.sin(heroAngle) * 0.1f,
                    (float) heroPos[1] + 0.1f,
                    (float) heroPos[2] - (float) Math.cos(heroAngle) * 0.1f,
                    1};
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
//            gl.glPushMatrix();
//            gl.glTranslatef(lightPos[0], lightPos[1], lightPos[2]);
//            GLUT glut = new GLUT();
//            glut.glutSolidSphere(0.05, 16, 16);
//            gl.glPopMatrix();

            float spotDirection[] = {(float) Math.sin(heroAngle), 0, (float) Math.cos(heroAngle)};
            gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_CUTOFF, 30);
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPOT_DIRECTION, spotDirection,0);
            gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_EXPONENT, 2);
            gl.glLightf(GL2.GL_LIGHT1, GL2.GL_LINEAR_ATTENUATION, 1.0f);

            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, new float[] {0.9f, 0.9f, 0.9f, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[] {0.9f, 0.9f, 0.9f, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[] {0.9f, 0.9f, 0.9f, 1}, 0);

        } else {
            gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0.5f, 0.5f, 0.5f, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {1, 1, 1, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[] {1, 1, 1, 1}, 0);

            // turn off the spot light
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, new float[] {0, 0, 0, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[] {0, 0, 0, 1}, 0);
            gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[] {0, 0, 0, 1}, 0);
        }

        hero.draw(gl);
        myTerrain.drawAll(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluPerspective(60, (float) width / (float) height, 0.5, 20); // keep the aspect ratio
	}

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void keyPressed(KeyEvent e) {
        double rad = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                hero.moveForward();
                break;
            case KeyEvent.VK_DOWN:
                hero.moveBackward();
                break;
            case KeyEvent.VK_LEFT:
                hero.turnLeft();
                break;
            case KeyEvent.VK_RIGHT:
                hero.turnRight();
                break;
            case KeyEvent.VK_N:
                nightMode = !nightMode;
                break;
            case KeyEvent.VK_M:
                myTerrain.toggleUseNormalMap();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}
