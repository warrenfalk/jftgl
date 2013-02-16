/* $ Id$
 * Created on 11.11.2004
 */
package net.sourceforge.ftgl.glfont;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.glyph.FTGlyph;
import net.sourceforge.ftgl.glyph.FTPixmapGlyph;

/**
 * FTGLPixmapFont is a specialisation of the FTFont class for handling
 * Pixmap (Grey Scale) fonts
 * 
 * @see     FTFont
 * @author joda
 */
public class FTGLPixmapFont extends FTFont
{

	/**
	 * @param font
	 */
	public FTGLPixmapFont(Font font)
	{
		this(font, FTFont.STANDARDCONTEXT);
	}
	/**
	 * @param font
	 * @param context
	 */
	public FTGLPixmapFont(Font font, FontRenderContext context)
	{
		super(font, context);
	}

	/**
	 * @param fontname
	 */
	public FTGLPixmapFont(String fontname)
	{
		this(Font.decode(fontname));
	}

	/**
	 * @param fontname
	 * @param context
	 */
	public FTGLPixmapFont(String fontname, FontRenderContext context)
	{
		this(Font.decode(fontname), context);
	}
	/**
	 * Construct a FTPixmapGlyph.
	 * 
	 * @param g The glyph index NOT the char code.
	 * @return  An FTPixmapGlyph or <code>null</code> on failure.
	 */
	protected FTGlyph makeGlyph(Shape ftGlyph, float advance)
	{
		if( ftGlyph!=null)
		{
			FTPixmapGlyph tempGlyph = new FTPixmapGlyph(ftGlyph);
			return tempGlyph;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createDisplayList()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 * Renders a string of characters
	 * 
	 * @param string    'C' style string to be output.
	 */
	public void render(String string)
	{
		this.gl.glPushAttrib( GL.GL_ENABLE_BIT | GL.GL_PIXEL_MODE_BIT | GL.GL_COLOR_BUFFER_BIT);
		this.gl.glPushClientAttrib( GL.GL_CLIENT_PIXEL_STORE_BIT);

		this.gl.glEnable(GL.GL_BLEND);
		this.gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		this.gl.glDisable( GL.GL_TEXTURE_2D);

		super.render(string);

		this.gl.glPopClientAttrib();
		this.gl.glPopAttrib();
	}



}