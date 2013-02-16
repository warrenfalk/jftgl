/* $Id: FTGLTextureFont.java,v 1.3 2004/11/18 17:55:46 joda Exp $ */
package net.sourceforge.ftgl.glfont;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.util.LinkedList;
import java.util.List;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.FTBBox;
import net.sourceforge.ftgl.glyph.FTGlyph;
import net.sourceforge.ftgl.glyph.FTTextureGlyph;
import net.sourceforge.ftgl.util.FTMath;


/**
 * FTGLTextureFont is a specialisation of the FTFont class for handling Texture mapped fonts
 * @see FTFont
 */
public class FTGLTextureFont extends FTFont
{

	/**
	 * The maximum texture dimension on this OpenGL implemetation
	 */
	private int[] maxTextSize = { 0 }; // TODO was GLsizei

	/**
	 * The minimum texture width required to hold the glyphs
	 */
	private int textureWidth = 0; // TODO was GLsizei

	/**
	 * The minimum texture height required to hold the glyphs
	 */
	private int textureHeight = 0; // TODO was GLsizei

	/**
	 * An array of texture ids
	 */
	private List textureIDList = new LinkedList();

	/**
	 * The max height for glyphs in the current font
	 */
	private int glyphHeight = 0;

	/**
	 * The max width for glyphs in the current font
	 */
	private int glyphWidth = 0;

	/**
	 * A value to be added to the height and width to ensure that glyphs don't overlap in the
	 * texture
	 */
	private int padding = 3;

	/**
	 * 
	 */
	private int numGlyphs;

	/**
	 */
	private int remGlyphs;

	/**
	 */
	private int xOffset = 0;

	/**
	 */
	private int yOffset = 0;


	/**
	 * Open and read a font file. Uses the standard FontRenderContext.
	 * @param fontname font file name.
	 */
	public FTGLTextureFont(String fontname)
	{
		this(Font.decode(fontname));
	}

	/**
	 * Creates a new FTGLTextureFont with the given {@link Font}. Uses the standard FontRenderContext.
	 * @param font The font to create from this FTFont.
	 */
	public FTGLTextureFont(Font font)
	{
		this(font, FTFont.STANDARDCONTEXT);
	}

	/**
	 * Reads the font from the given font name and renders the font to the given fontrendercontext.
	 * @param fontname The fontname.
	 * @param context The FontRenderContext to render with.
	 */
	public FTGLTextureFont(final String fontname, final FontRenderContext context)
	{
		this(Font.decode(fontname), context);
	}

	/**
	 * Creates the FTFont from the given {@link Font}.
	 * @param font
	 * @param context
	 */
	public FTGLTextureFont(final Font font, final FontRenderContext context)
	{
		super(font, context);
		this.remGlyphs = this.font.getNumGlyphs();
		this.numGlyphs = this.font.getNumGlyphs();
//		this.precache();
	}


	/**
	 * {@inheritDoc}
	 */
	public void dispose()
	{
		super.dispose();
		this.deleteTextures();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean faceSize(final float size)
	{
		return this.faceSize(size, new FontRenderContext(null, true, true));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean faceSize(final float size, FontRenderContext context)
	{
		if (!this.textureIDList.isEmpty())
		{
			this.deleteTextures();
			this.remGlyphs = this.font.getNumGlyphs();
			this.numGlyphs = this.font.getNumGlyphs();
		}

		return super.faceSize(size, context);
	}

	private void deleteTextures()
	{
		int[] textureID = new int[this.textureIDList.size()];
		for (int i = 0; i < textureID.length; i++)
			textureID[i] = ((int[])this.textureIDList.get(i))[0];
		this.gl.glDeleteTextures(this.textureIDList.size(), textureID);
		this.textureIDList.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	protected FTGlyph makeGlyph(Shape ftGlyph, float advance)
	{

		if (ftGlyph != null)
		{
			// prevents overwriting previous characters if the last character was very small
			this.glyphHeight = Math.max(this.glyphHeight, ftGlyph.getBounds().height);
			this.glyphWidth = ftGlyph.getBounds().width;

			//TODO maybe workaround.
			if (this.glyphHeight == 0 || this.glyphWidth == 0)
				return new FTTextureGlyph(ftGlyph, 0, 0, 0, 0, 0);

			if (this.textureIDList.isEmpty())
			{
				this.textureIDList.add(this.createTexture());
				this.xOffset = this.padding;
				this.yOffset = this.padding;
			}

			if (this.xOffset > (this.textureWidth -this.padding - this.glyphWidth))
			{
				this.xOffset = this.padding;
				this.yOffset += this.glyphHeight;

				if (this.yOffset > (this.textureHeight - this.padding - this.glyphHeight))
				{
					this.textureIDList.add(this.createTexture());
					this.yOffset = this.padding;
				}
			}

			FTTextureGlyph tempGlyph = new FTTextureGlyph(ftGlyph,
				((int[])this.textureIDList.get(this.textureIDList.size() - 1))[0], this.xOffset, this.yOffset, this.textureWidth, this.textureHeight);
			FTBBox box = tempGlyph.getBBox();
			assert box.upperX-box.lowerX<=this.glyphWidth:"Character width is out of bounds."+(box.upperX-box.lowerX)+" max:"+this.glyphWidth;
			assert box.upperY-box.lowerY<=this.glyphHeight:"Character height is out of bounds."+(box.upperY-box.lowerY)+" max:"+this.glyphWidth;
			this.xOffset += this.glyphWidth + this.padding;

			--this.remGlyphs;
			return tempGlyph;
		}

		return null;
	}

	/**
	 * Get the size of a block of memory required to layout the glyphs Calculates a width and height
	 * based on the glyph sizes and the number of glyphs. It over estimates.
	 */
	private final void calculateTextureSize()
	{
		if (this.maxTextSize[0] == 0)
		{
			this.gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, this.maxTextSize);
			System.err.println("MaxTexture:"+this.maxTextSize[0]);
		}

		this.textureWidth = FTMath.nextPowerOf2((this.remGlyphs * this.glyphWidth) + (this.padding * 2));
		if (this.textureWidth > this.maxTextSize[0])
		{
			this.textureWidth = this.maxTextSize[0];
		}

		//TODO: Validate these calculations
		int h = (this.textureWidth - this.padding * 2) / this.glyphWidth;

		this.textureHeight = FTMath.nextPowerOf2(((this.numGlyphs / h) + 1) * this.glyphHeight);
		this.textureHeight = this.textureHeight > this.maxTextSize[0] ? this.maxTextSize[0] : this.textureHeight;
	}

	/**
	 * Creates a 'blank' OpenGL texture object. The format is GL_ALPHA and the params are
	 * GL_TEXTURE_WRAP_S = GL_CLAMP GL_TEXTURE_WRAP_T = GL_CLAMP GL_TEXTURE_MAG_FILTER = GL_LINEAR
	 * GL_TEXTURE_MIN_FILTER = GL_LINEAR Note that mipmapping is NOT used
	 */
	private final int[] createTexture() // TODO was GLuint
	{
		this.calculateTextureSize();

		int totalMemory = this.textureWidth * this.textureHeight;
		System.err.println("Real width/height:"+this.textureWidth+"x"+this.textureHeight);
		byte[] textureMemory = new byte[totalMemory];
		// memset( textureMemory, 0, totalMemory);

		int[] textID = new int[1];
		this.gl.glGenTextures(1, textID);

		this.gl.glBindTexture(GL.GL_TEXTURE_2D, textID[0]);
		this.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
		this.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
		this.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		this.gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

		this.gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_ALPHA, this.textureWidth, this.textureHeight, 0, GL.GL_ALPHA,
			GL.GL_UNSIGNED_BYTE, textureMemory);

		// delete [] textureMemory;

		return textID;
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(final String string)
	{
		assert renderTexture(0);
		this.gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT);

		this.gl.glEnable(GL.GL_BLEND);
		this.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // GL_ONE

		super.render(string);

		this.gl.glPopAttrib();
	}

	/**
	 * Render to texture.
	 * @param index index of the texture
	 * @return always <code>true</code>
	 */
	public boolean renderTexture(int index)
	{
		if (index>=0 && index < this.textureIDList.size())
		{
			this.gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_CURRENT_BIT);

			this.gl.glEnable(GL.GL_BLEND);
			this.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // GL_ONE
			this.gl.glDepthFunc(GL.GL_ALWAYS);
			int textureID = ((int[])this.textureIDList.get(index))[0];
			int[] activeTextureID = new int[1];
			this.gl.glGetIntegerv(GL.GL_TEXTURE_2D_BINDING_EXT, activeTextureID);
			if(activeTextureID[0] != textureID)
			{
				this.gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
			}
			float sizeX = Math.min(256f, this.textureWidth);
			float sizeY = (sizeX/this.textureWidth)*this.textureHeight;
			sizeX /= 2f;
			sizeY /= 2f;
			this.gl.glBegin(GL.GL_QUADS);
			this.gl.glColor4f(1f,0f,0f,1f);
			this.gl.glVertex2f(-sizeX, -sizeY);
			this.gl.glVertex2f(-sizeX, sizeY);
			this.gl.glVertex2f(sizeX, sizeY);
			this.gl.glVertex2f(sizeX, -sizeY);
			this.gl.glEnd();
			this.gl.glBegin(GL.GL_QUADS);
			this.gl.glTexCoord2f( 0f, 0f);
			this.gl.glVertex2f(-sizeX, -sizeY);

			this.gl.glTexCoord2f(0f, 1f);
			this.gl.glVertex2f(-sizeX, sizeY);

			this.gl.glTexCoord2f(1f, 1f);
			this.gl.glVertex2f(sizeX, sizeY);

			this.gl.glTexCoord2f(1f, 0f);
			this.gl.glVertex2f(sizeX, -sizeY);
			this.gl.glEnd();
			this.gl.glPopAttrib();
		}
		return true;
	}
}