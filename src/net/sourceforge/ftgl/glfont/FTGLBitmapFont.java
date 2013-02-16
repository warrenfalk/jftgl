/* $Id */
package net.sourceforge.ftgl.glfont;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.glyph.FTBitmapGlyph;
import net.sourceforge.ftgl.glyph.FTGlyph;

/**
 * FTGLBitmapFont is a specialisation of the FTFont class for handling Bitmap fonts
 * 
 * @see FTFont
 */
public class FTGLBitmapFont extends FTFont
{


	/**
	 * Open and read a font file. Sets Error flag.
	 * @param fontname font file name.
	 */
	public FTGLBitmapFont(String fontname)
	{
		this(Font.decode(fontname));
	}

	/**
	 * Creates a new font from the given font with the standard FontRenderContext.
	 * @param font The font from which to construct this FTFont.
	 */
	public FTGLBitmapFont(Font font)
	{
		this(font, FTFont.STANDARDCONTEXT);
	}

	/**
	 * Reads the font from the given font name and renders the font to the given fontrendercontext.
	 * @param fontname The fontname.
	 * @param context The FontRenderContext to render with.
	 */
	public FTGLBitmapFont(final String fontname, final FontRenderContext context)
	{
		this(Font.decode(fontname), context);
	}

	/**
	 * Creates the FTFont from the given {@link Font} and renders it to the given fontrendercontext.
	 * @param font	The font to create this FTFont from.
	 * @param context The rendercontext to render the glyphs with.
	 */
	public FTGLBitmapFont(final Font font, final FontRenderContext context)
	{
		super(font, context);
	}


	/**
	 * {@inheritDoc}
	 */
	public void dispose()
	{
		super.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	public void render(String string)
	{
		this.gl.glPushClientAttrib(GL.GL_CLIENT_PIXEL_STORE_BIT);
		this.gl.glPushAttrib(GL.GL_ENABLE_BIT);

		this.gl.glPixelStorei(GL.GL_UNPACK_LSB_FIRST, GL.GL_FALSE);
		this.gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

		this.gl.glDisable(GL.GL_BLEND);

		super.render(string);

		this.gl.glPopAttrib();
		this.gl.glPopClientAttrib();
	}

	/**
	 * {@inheritDoc}
	 */
	protected FTGlyph makeGlyph(Shape ftGlyph, float advance)
	{

		if (ftGlyph != null)
		{
			FTBitmapGlyph tempGlyph = new FTBitmapGlyph(ftGlyph, advance);
			return tempGlyph;
		}

		return null;
	}

}