/* $ Id$
 * Created on 08.11.2004
 */
package net.sourceforge.ftgl.demos;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.OpenType;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import net.java.games.jogl.Animator;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLCapabilities;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLDrawableFactory;
import net.java.games.jogl.GLEventListener;
import net.java.games.jogl.GLU;
import net.java.games.jogl.impl.NativeLibLoader;
import net.sourceforge.ftgl.FTBBox;
import net.sourceforge.ftgl.glfont.FTFont;
import net.sourceforge.ftgl.glfont.FTGLBitmapFont;
import net.sourceforge.ftgl.glfont.FTGLExtrdFont;
import net.sourceforge.ftgl.glfont.FTGLOutlineFont;
import net.sourceforge.ftgl.glfont.FTGLPixmapFont;
import net.sourceforge.ftgl.glfont.FTGLPolygonFont;
import net.sourceforge.ftgl.glfont.FTGLTextureFont;
import net.sourceforge.ftgl.util.loader.LibraryLoader;

/**
 * TODO javadoc
 * 
 * @author joda
 */
public class FTGLDemo implements GLEventListener, KeyListener, ActionListener, MouseWheelListener
{

	public static final int EDITING = 1;
	public static final int INTERACTIVE = 2;

	public static final int FTGL_BITMAP = 0;
	public static final int FTGL_PIXMAP = 1;

	public static final int FTGL_OUTLINE = 2;
	public static final int FTGL_POLYGON = 3;
	public static final int FTGL_EXTRUDE = 4;
	public static final int FTGL_TEXTURE = 5;

	/** number of frames rendered */
	public static volatile int framesCounted = 0;

	/** JOGL GL instance*/
	private GL gl = null;

	/** JOGL GL instance*/
	private GLU glu = null;

	/**
	 * The 6 different types of supported fonts.
	 * <p>Note: previously static FTFont* fonts[6];
	 */
	private FTFont[] fonts = new FTFont[6];

	/**
	 * default font used to display text
	 * <p>Note: previously static FTGLPixmapFont* infoFont;
	 */
	private FTFont infoFont = null;

	private FontRenderContext context = null;
	private Font font = null;
	private Font updateFontTo = null;

	private int current_font = FTGL_EXTRUDE;
	private KeyEvent key = null;
	public volatile boolean running = false;

	private int w_win = 640, h_win = 480;
	private int mode = EDITING;
	private float zoom = 2.0f;

	private Trackball tb = new Trackball();

	private String myString = "ftgl" ;///default empty String ?
	private int carat = this.myString.length();
	/**
	 * Set up lighting.
	 */
	private void setUpLighting()
	{
		// Set up lighting.
		float[] light1_ambient = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] light1_diffuse = { 1.0f, 0.9f, 0.9f, 1.0f };
		float[] light1_specular = { 1.0f, 0.7f, 0.7f, 1.0f };
		float[] light1_position = { -1.0f, 1.0f, 1.0f, 0.0f };
		this.gl.glLightfv(GL.GL_LIGHT1, GL.GL_AMBIENT, light1_ambient);
		this.gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, light1_diffuse);
		this.gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, light1_specular);
		this.gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, light1_position);
		this.gl.glEnable(GL.GL_LIGHT1);

		float light2_ambient[] = { 0.2f, 0.2f, 0.2f, 1.0f };
		float light2_diffuse[] = { 0.9f, 0.9f, 0.9f, 1.0f };
		float light2_specular[] = { 0.7f, 0.7f, 0.7f, 1.0f };
		float light2_position[] = { 1.0f, -1.0f, -1.0f, 0.0f };
		this.gl.glLightfv(GL.GL_LIGHT2, GL.GL_AMBIENT, light2_ambient);
		this.gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, light2_diffuse);
		this.gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, light2_specular);
		this.gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, light2_position);
		// glEnableLIGHT2);

		float front_emission[] = { 0.3f, 0.2f, 0.1f, 0.0f };
		float front_ambient[] = { 0.2f, 0.2f, 0.2f, 0.0f };
		float front_diffuse[] = { 0.95f, 0.95f, 0.8f, 0.0f };
		float front_specular[] = { 0.6f, 0.6f, 0.6f, 0.0f };
		this.gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, front_emission);
		this.gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, front_ambient);
		this.gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, front_diffuse);
		this.gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, front_specular);
		this.gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 16.0f);
		this.gl.glColor4fv(front_diffuse);

		this.gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);
		this.gl.glEnable(GL.GL_CULL_FACE);
		this.gl.glColorMaterial(GL.GL_FRONT, GL.GL_DIFFUSE);
		this.gl.glEnable(GL.GL_COLOR_MATERIAL);

		this.gl.glEnable(GL.GL_LIGHTING);
		this.gl.glShadeModel(GL.GL_SMOOTH);
	}

	/**
	 * Create fonts.
	 */
	private void setUpFonts(FontRenderContext context)
	{
		try
		{
			fonts[FTGL_BITMAP]  = new FTGLBitmapFont(this.font, context);
			fonts[FTGL_PIXMAP]  = new FTGLPixmapFont(this.font, context);
			fonts[FTGL_OUTLINE] = new FTGLOutlineFont(this.font, context);
			fonts[FTGL_POLYGON] = new FTGLPolygonFont(this.font, context);
			fonts[FTGL_EXTRUDE] = new FTGLExtrdFont(this.font, context);
			fonts[FTGL_TEXTURE] = new FTGLTextureFont(this.font, context);
		}
		catch (IllegalStateException ex)
		{

			throw ex;
		}
		for (int i=0;i<this.fonts.length;i++)
		{
			// TODO: Fix implementation to support this :D
			// this.fonts[i].faceSize(144f);
		}
		((FTGLExtrdFont)fonts[FTGL_EXTRUDE]).setDepth(20f);

		infoFont = new FTGLPixmapFont(this.font);
		ascenderfont  = new FTGLPolygonFont(this.font.deriveFont(20f));

		infoFont.faceSize(18);
	}

	/**
	 * Verifies if the GL or GLU context changed.
	 * @param drawable The actual drawable.
	 */
	private final void hasContextChanged(GLDrawable drawable)
	{
		if (drawable.getGL() != this.gl || drawable.getGLU() != this.glu)
		{
			this.setGLGLU(drawable.getGL(), drawable.getGLU());
			System.out.println("GL or GLU context changed!");
		}
	}

	private final void hasFontChanged()
	{
		if (this.updateFontTo != null)
		{
			this.font = this.updateFontTo;
			this.setUpFonts(FTFont.STANDARDCONTEXT);
			this.setGLGLU(this.gl, this.glu);
			this.updateFontTo = null;
		}
	}

	/**
	 * This method sets the new GL context, if it has changed.
	 * This method sets the new GLU context, if it has changed.
	 * All engines, deriving from this class have to overwrite this method, to actualise all
	 * variables pointing on the old context (in Object3Ds or Mesh3Ds, e.g.).
	 * When overriding this method, super.setGL(GL gl); must be called!
	 * @param gl The new GL context.
	 * @param glu The new GLU context.
	 */
	protected void setGLGLU(GL gl, GLU glu)
	{
		this.gl = gl;
		this.glu = glu;
		for (int i = 0; i < this.fonts.length; i++)
		{
			if (this.fonts[i]!=null)
			{
				this.fonts[i].setGLGLU(this.gl, this.glu);
				this.fonts[i].clearCache(true);
			}
		}
		infoFont.setGLGLU(this.gl, this.glu);
		ascenderfont.setGLGLU(this.gl, this.glu);
	}

	private FTFont ascenderfont;
	/**
	 * Draws the bounding boxes around the text.
	 */
	void renderFontmetrics()
	{
		FTBBox box = fonts[current_font].getBBox(myString);

		// Draw the bounding box
		this.gl.glDisable(GL.GL_LIGHTING);
		this.gl.glDisable(GL.GL_TEXTURE_2D);
		this.gl.glEnable(GL.GL_LINE_SMOOTH);
		this.gl.glEnable(GL.GL_BLEND);
		this.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE); // ONE_MINUS_SRC_ALPHA

		this.gl.glColor3f(0.0f, 1.0f, 0.0f);
		// Draw the front face
		this.gl.glBegin(GL.GL_LINE_LOOP);
		this.gl.glVertex3f(box.lowerX, box.lowerY, box.lowerZ);
		this.gl.glVertex3f(box.lowerX, box.upperY, box.lowerZ);
		this.gl.glVertex3f(box.upperX, box.upperY, box.lowerZ);
		this.gl.glVertex3f(box.upperX, box.lowerY, box.lowerZ);
		this.gl.glEnd();
		// Draw the back face
		if (current_font == FTGL_EXTRUDE && box.lowerZ != box.upperZ)
		{
			this.gl.glBegin(GL.GL_LINE_LOOP);
			this.gl.glVertex3f(box.lowerX, box.lowerY, box.upperZ);
			this.gl.glVertex3f(box.lowerX, box.upperY, box.upperZ);
			this.gl.glVertex3f(box.upperX, box.upperY, box.upperZ);
			this.gl.glVertex3f(box.upperX, box.lowerY, box.upperZ);
			this.gl.glEnd();
			// Join the faces
			this.gl.glBegin(GL.GL_LINES);
			this.gl.glVertex3f(box.lowerX, box.lowerY, box.lowerZ);
			this.gl.glVertex3f(box.lowerX, box.lowerY, box.upperZ);

			this.gl.glVertex3f(box.lowerX, box.upperY, box.lowerZ);
			this.gl.glVertex3f(box.lowerX, box.upperY, box.upperZ);

			this.gl.glVertex3f(box.upperX, box.upperY, box.lowerZ);
			this.gl.glVertex3f(box.upperX, box.upperY, box.upperZ);

			this.gl.glVertex3f(box.upperX, box.lowerY, box.lowerZ);
			this.gl.glVertex3f(box.upperX, box.lowerY, box.upperZ);
			this.gl.glEnd();
		}

		// Draw the baseline, Ascender and Descender
		this.gl.glBegin(GL.GL_LINES);
		this.gl.glColor3f(0.0f, 0.0f, 1.0f);
		this.gl.glVertex3f(0.0f, 0.0f, 0.0f);
		this.gl.glVertex3f(fonts[current_font].advance(myString), 0.0f, 0.0f);
		this.gl.glVertex3f(0.0f, fonts[current_font].ascender(), 0.0f);
		assert (fonts[current_font].ascender()>=fonts[current_font].descender()):"Ascender is not greater or equal to descender.";
		this.gl.glVertex3f(0.0f, fonts[current_font].descender(), 0.0f);

		this.gl.glEnd();

		// Draw the origin
		this.gl.glColor3f(1.0f, 0.0f, 0.0f);
		this.gl.glPointSize(5.0f);
		this.gl.glBegin(GL.GL_POINTS);
		this.gl.glVertex3f(0.0f, 0.0f, 0.0f);
		this.gl.glEnd();

//		this.gl.glLineWidth(3f);
//		this.gl.glBegin(GL.GL_LINES);
//		this.gl.glColor3f(1f,0f,0f);
//		final float LENGTH = 28f;
//		this.gl.glVertex3f(LENGTH,0f,0f);
//		this.gl.glVertex3f(0f,0f,0f);
//		this.gl.glColor3f(0f,1f,0f);
//		this.gl.glVertex3f(0f,LENGTH,0f);
//		this.gl.glVertex3f(0f,0f,0f);
//		this.gl.glColor3f(0f,0f,1f);
//		this.gl.glVertex3f(0f,0f,LENGTH);
//		this.gl.glVertex3f(0f,0f,0f);
//		this.gl.glEnd();

		this.gl.glLineWidth(1f);
	}

	/**
	 * 
	 */
	void renderFontInfo()
	{
		this.gl.glMatrixMode(GL.GL_PROJECTION);
		this.gl.glLoadIdentity();
		this.glu.gluOrtho2D(0, w_win, 0, h_win);
		this.gl.glMatrixMode(GL.GL_MODELVIEW);
		this.gl.glLoadIdentity();

		// draw mode
		this.gl.glColor3f(1.0f, 1.0f, 1.0f);
		this.gl.glRasterPos2f(20.0f, h_win - (20.0f + infoFont.ascender()));

		switch (mode)
		{
			case EDITING:
				infoFont.render("Edit Mode");
				break;
			case INTERACTIVE:
				break;
		}

		// draw font type
		this.gl.glRasterPos2i(20, 20);
		switch (this.current_font)
		{
			case FTGL_BITMAP:
				infoFont.render("Bitmap Font");
				break;
			case FTGL_PIXMAP:
				infoFont.render("Pixmap Font");
				break;
			case FTGL_OUTLINE:
				infoFont.render("Outline Font");
				break;
			case FTGL_POLYGON:
				infoFont.render("Polygon Font");
				break;
			case FTGL_EXTRUDE:
				infoFont.render("Extruded Font");
				break;
			case FTGL_TEXTURE:
				infoFont.render("Texture Font");
				break;
		}
		this.gl.glRasterPos2f(20.0f, 20.0f + infoFont.ascender() - infoFont.descender());
		infoFont.render(this.font.getFontName());
	}

	/**
	 * 
	 */
	public void do_display()
	{
		switch (current_font)
		{
			case FTGL_BITMAP:
			case FTGL_PIXMAP:
			case FTGL_OUTLINE:
				break;
			case FTGL_POLYGON:
				this.gl.glDisable(GL.GL_BLEND);
				setUpLighting();
				break;
			case FTGL_EXTRUDE:
				this.gl.glEnable(GL.GL_DEPTH_TEST);
				this.gl.glDisable(GL.GL_BLEND);
				setUpLighting();
				break;
			case FTGL_TEXTURE:
				this.gl.glEnable(GL.GL_TEXTURE_2D);
				this.gl.glDisable(GL.GL_DEPTH_TEST);
				setUpLighting();
				this.gl.glNormal3f(0.0f, 0.0f, 1.0f);
				break;

		}


		if (this.fonts[current_font]!=null)
		{
			FTBBox box = this.fonts[current_font].getBBox(myString);
			this.gl.glPushMatrix();
			this.gl.glTranslatef(-box.getWidth() / 2.0f, -box.getHeight() / 2.0f, 0f);

			this.renderCarat();

			this.gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			// If you do want to switch the color of bitmaps rendered with glBitmap,
			// you will need to explicitly call glRasterPos3f (or its ilk) to lock
			// in a changed current color.
			this.gl.glRasterPos3f(0f,0f,0f);

			this.fonts[current_font].render(myString);

			this.renderFontmetrics();
			this.gl.glPopMatrix();
		}
		this.renderFontInfo();
	}

	private void renderCarat()
	{
		if (this.carat>=0)
		{
			float caratPos = this.fonts[this.current_font].advance(this.myString.substring(0,this.carat));
			this.gl.glPushAttrib(GL.GL_LIGHTING_BIT | GL.GL_LINE_BIT | GL.GL_TEXTURE_BIT);
			this.gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
			this.gl.glLineWidth(12f);
			this.gl.glDisable(GL.GL_LIGHTING);
			this.gl.glDisable(GL.GL_TEXTURE_2D);
			this.gl.glBegin(GL.GL_LINES);
			this.gl.glVertex3f(caratPos, this.fonts[this.current_font].ascender(), 0.0f);
			this.gl.glVertex3f(caratPos, this.fonts[this.current_font].descender(), 0.0f);
			this.gl.glEnd();
			this.gl.glPopAttrib();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void display(GLDrawable drawable)
	{
		this.hasContextChanged(drawable);
		this.hasFontChanged();

		this.gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		this.processKey();
		SetCamera();

		switch (current_font)
		{
			case FTGL_BITMAP:
			case FTGL_PIXMAP:
				this.gl.glRasterPos2i(w_win / 2, h_win / 2);
				this.gl.glTranslatef(w_win / 2f, h_win / 2f, 0.0f);
				break;
			case FTGL_OUTLINE:
			case FTGL_POLYGON:
			case FTGL_EXTRUDE:
			case FTGL_TEXTURE:
				this.gl.glMultMatrixf(tb.tbMatrix(new float[16]));
				break;
		}

		this.gl.glPushMatrix();

		do_display();

		this.gl.glPopMatrix();

		FTGLDemo.framesCounted++;
		// glutSwapBuffers();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(GLDrawable drawable)
	{
		this.setUpFonts(this.context);
		this.hasContextChanged(drawable);
		this.hasFontChanged();

		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

		this.gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		this.gl.glClearColor(0.13f, 0.17f, 0.32f, 0.0f);
		this.gl.glColor3f(1.0f, 1.0f, 1.0f);

		this.gl.glEnable(GL.GL_CULL_FACE);
		this.gl.glFrontFace(GL.GL_CCW);

		this.gl.glEnable(GL.GL_DEPTH_TEST);

		this.gl.glEnable(GL.GL_POLYGON_OFFSET_LINE);
		this.gl.glPolygonOffset(1.0f, 1.0f); // ????

		SetCamera();

		this.tb.tbInit(MouseEvent.BUTTON1);

	}

	/**
	 * 
	 * <p>Note: previously void myinit
	 * @param font
	 * @param context
	 * @param drawable
	 */
	public FTGLDemo(Font font, GLDrawable drawable, FontRenderContext context)
	{
		this.font = font;
		this.context = context;
		drawable.addMouseMotionListener(this.tb);
		drawable.addMouseWheelListener(this);
		drawable.addMouseListener(this.tb);
		drawable.addKeyListener(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>Note: previously void parsekey(char key, int x, int y)
	 */
	public void keyTyped(KeyEvent e)
	{
		char keyChar = e.getKeyChar();
		if (keyChar!=KeyEvent.CHAR_UNDEFINED && keyChar!=KeyEvent.VK_BACK_SPACE)
		{
			if (mode == INTERACTIVE)
			{
				myString = String.valueOf(keyChar);
			}
			else
			{
				myString = myString.substring(0,this.carat)+keyChar+myString.substring(this.carat);
				this.carat++;
			}
		}
		// glutPostRedisplay();
	}

	/**
	 * {@inheritDoc}
	 * <p>Note: added to allow zooming
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK)==0)
		{
			float amount = -e.getWheelRotation();
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)>0)
				amount /= 10;
			this.zoom += amount;
			if (this.zoom<=0)
				this.zoom = 1f;
			return;
		}
		else
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK)>0)
			{
				FTGLExtrdFont f = (FTGLExtrdFont)this.fonts[FTGL_EXTRUDE];

				float amount = -e.getWheelRotation();
				if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK)>0)
					amount *= 10;
				f.setDepth(f.getDepth() + amount);
				return;
			}
	}


	/**
	 * Processes the keys delivered by the KeyListener,
	 * <p>Note: keys will be processed in the JOGL's rendering thread.
	 */
	public void processKey()
	{
		if (this.key!=null)
		{
			switch (this.key.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE:
					this.running = true;
					break;
				case KeyEvent.VK_BACK_SPACE:
					if (this.carat>0)
					{
						StringBuffer sb = new StringBuffer(this.myString);
						sb.deleteCharAt(this.carat-1);
						this.myString = sb.toString();
						this.carat = this.carat-1;
					}
					break;
				case KeyEvent.VK_ENTER:
					if (mode == EDITING)
					{
						mode = INTERACTIVE;
						this.carat = -1;
						if (this.myString!=null && this.myString.length()>0)
							this.myString = this.myString.substring(0,1);
						else
							this.myString = "a";
					}
					else
					{
						this.carat = Math.max(this.myString.length()-1,0);
						mode = EDITING;
					}
					break;
				case KeyEvent.VK_LEFT:
					this.carat = Math.max(this.carat-1,0);
					break;
				case KeyEvent.VK_RIGHT:
					this.carat = Math.min(this.carat+1,this.myString.length());
					break;
				case KeyEvent.VK_PAGE_UP:
					this.current_font++;
					if (this.current_font >= this.fonts.length)
						this.current_font = 0;
					break;
				case KeyEvent.VK_PAGE_DOWN:
					this.current_font--;
					if (this.current_font < 0)
						this.current_font = this.fonts.length-1;
					break;
				case KeyEvent.VK_F1:
					this.myString = "ftgl";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F2:
					this.myString = "Thou shall not steal.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F3:
					this.myString = "My birthday present from me.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F4:
					this.myString = "Sorry, I wrecked your car.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F5:
					this.myString = "Happy wife, happy life.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F6:
					this.myString = "Call your mom, she worries.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F7:
					this.myString = "Ding Ding Bling Bling.";
					this.carat = this.myString.length();
					break;
				case KeyEvent.VK_F8:
					this.myString = "\u6FB3\u9580";
					this.carat = this.myString.length();
					break;
				default:
//					if (this.key.getKeyChar()==KeyEvent.CHAR_UNDEFINED)
//						System.err.println("Ignored:"+this.key);
					break;
			}
			this.key = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void displayChanged(GLDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
		this.hasContextChanged(drawable);
		this.hasFontChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void reshape(GLDrawable drawable, int x, int y, int width, int height)
	{
		this.hasContextChanged(drawable);
		this.hasFontChanged();

		this.gl.glMatrixMode(GL.GL_MODELVIEW);
		this.gl.glViewport(0, 0, width, height);
		this.gl.glLoadIdentity();

		this.w_win = width;
		this.h_win = height;
		SetCamera();

		this.tb.tbReshape(this.w_win, this.h_win);
	}

	void SetCamera()
	{
		switch (current_font)
		{
			case FTGL_BITMAP:
			case FTGL_PIXMAP:
				this.gl.glMatrixMode(GL.GL_PROJECTION);
				this.gl.glLoadIdentity();
				this.glu.gluOrtho2D(0, w_win, 0, h_win);
				this.gl.glMatrixMode(GL.GL_MODELVIEW);
				this.gl.glLoadIdentity();
				break;
			case FTGL_OUTLINE:
			case FTGL_POLYGON:
			case FTGL_EXTRUDE:
			case FTGL_TEXTURE:
				this.gl.glMatrixMode(GL.GL_PROJECTION);
				this.gl.glLoadIdentity();
				this.glu.gluPerspective(90, (float)w_win / h_win, 1, 1000);
				this.gl.glMatrixMode(GL.GL_MODELVIEW);
				this.gl.glLoadIdentity();
				this.glu.gluLookAt(0.0, 0.0, h_win / zoom, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
				break;
		}
	}

	public static void main(String[] args)
	{
		NativeLibLoader.disableLoading();
		new LibraryLoader().load("jogl");
		// glutInitDisplayMode(GLUT_DEPTH | GLUT_RGB | GLUT_DOUBLE | GLUT_MULTISAMPLE);
		// glutInitWindowPosition(50, 50);
		// glutInitWindowSize( w_win, h_win);

		GLCapabilities glc = new GLCapabilities();
		glc.setSampleBuffers(true);
		glc.setDoubleBuffered(true);
		GLDrawableFactory glf = GLDrawableFactory.getFactory();
		GLCanvas drawable = glf.createGLCanvas(glc);

		System.out.println("DRAWABLE GL IS: " + drawable.getGL().getClass().getName());
		System.out.println("DRAWABLE GLU IS: " + drawable.getGLU().getClass().getName());

		final JFrame frame = new JFrame("FTGL TEST");
		frame.setBounds(50,50, 640, 480);
		final Animator animator = new Animator(drawable);
		//final Thread t = Thread.currentThread();

		String [] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		JComboBox box = new JComboBox(allfonts);

		drawable.setSize(640, 480);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(drawable, BorderLayout.CENTER);
		frame.getContentPane().add(box, BorderLayout.SOUTH);
		frame.pack();

		FTGLDemo meshDemo = new FTGLDemo(Font.decode(allfonts[0]).deriveFont(172f), drawable,
			//((Graphics2D) frame.getGraphics()).getFontRenderContext()
			FTFont.STANDARDCONTEXT
			);

		box.addActionListener(meshDemo);
		drawable.addGLEventListener(meshDemo);
		drawable.requestFocus();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		animator.start();
		System.out.println(meshDemo.toString());
		try
		{
			int count_alt = 0;
			long time_alt = System.currentTimeMillis();
			while (!Thread.interrupted() && !meshDemo.running)
			{
				Thread.sleep(5000);

				long time = System.currentTimeMillis();
				int count = FTGLDemo.framesCounted;

				long dt = time - time_alt;
				int dc = count - count_alt;
				count_alt = count;
				System.out.println("" + dc + " frames in " + dt / 1000f + " seconds = "
					+ (1000f * dc / dt) + " FPS at " + drawable.getWidth() + "x"+ drawable.getHeight());

				time_alt = time;
			}
		}
		catch (InterruptedException e)
		{
			System.out.println("Thread was interrupted.");
		}
		animator.stop();
		drawable.setEnabled(false);
		//System.exit(0);

		// glutMainLoop();
	}

	/**
	 * {@inheritDoc}
	 */
	public void keyPressed(KeyEvent e)
	{
		// nothing
	}
	/**
	 * {@inheritDoc}
	 */
	public void keyReleased(KeyEvent e)
	{
		this.key = e;
		e.consume();
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof JComboBox)
		{
			JComboBox b = (JComboBox) e.getSource();
			String selfont = b.getSelectedItem().toString();
			this.updateFontTo = Font.decode(selfont).deriveFont(72f);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		switch (this.mode)
		{
			case EDITING:
				sb.append("\tMode: Edit Mode\n");
				break;
			case INTERACTIVE:
				sb.append("\tMode: Interactive Mode\n");
				break;
		}
		sb.append("\tType: ");
		switch (this.current_font)
		{
			case FTGL_BITMAP:
				sb.append("Bitmap Font\n");
				break;
			case FTGL_PIXMAP:
				sb.append("Pixmap Font\n");
				break;
			case FTGL_OUTLINE:
				sb.append("Outline Font\n");
				break;
			case FTGL_POLYGON:
				sb.append("Polygon Font\n");
				break;
			case FTGL_EXTRUDE:
				sb.append("Extruded Font\n");
				break;
			case FTGL_TEXTURE:
				sb.append("Texture Font\n");
				break;
		}

		if (this.font instanceof OpenType) sb.append(" (OpenType) ");

		sb.append("\tFontfile: ");
		sb.append(this.font.getFontName());
		sb.append("\n");
		return sb.toString();
	}

}