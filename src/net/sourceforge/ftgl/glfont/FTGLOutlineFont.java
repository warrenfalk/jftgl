/* $Id: FTGLOutlineFont.java,v 1.1 2004/11/12 19:44:23 funsheep Exp $ */
package net.sourceforge.ftgl.glfont;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.glyph.FTGlyph;
import net.sourceforge.ftgl.glyph.FTOutlineGlyph;

/**
 * FTGLOutlineFont is a specialisation of the FTFont class for handling Vector Outline fonts
 * @see FTFont
 */
public class FTGLOutlineFont extends FTFont
{

	/**
	 * Open and read a font file. Sets Error flag.
	 * @param fontname font file name.
	 */
	public FTGLOutlineFont(final String fontname)
	{
		this(Font.decode(fontname));
	}

	/**
	 * Creates a new FTGLOutlineFont from the given {@link Font}.
	 * @param font The font from to create a new OutlineFont.
	 */
	public FTGLOutlineFont(final Font font)
	{
		this(font, FTFont.STANDARDCONTEXT);
	}

	/**
	 * Open and reads a font file. Renders the font with the given FontRenderContext.
	 * @param fontname The name of the font.
	 * @param context The rendercontext.
	 */
	public FTGLOutlineFont(final String fontname, final FontRenderContext context)
	{
		this(Font.decode(fontname), context);
	}

	/**
	 * Creates a new FTGLOutlineFont with the specified {@link Font}. Uses the given FontRenderContext.
	 * @param font The font from which to create this FTFont.
	 * @param context The rendercontext.
	 */
	public FTGLOutlineFont(final Font font, final FontRenderContext context)
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
		this.gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_HINT_BIT | GL.GL_LINE_BIT | GL.GL_COLOR_BUFFER_BIT);

		this.gl.glDisable(GL.GL_TEXTURE_2D);

		this.gl.glEnable(GL.GL_LINE_SMOOTH);
		this.gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
		this.gl.glEnable(GL.GL_BLEND);
		this.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // GL_ONE

		super.render(string);

		this.gl.glPopAttrib();

	}

	/**
	 * {@inheritDoc}
	 */
	protected final FTGlyph makeGlyph(Shape ftGlyph, float advance)
	{
		if (ftGlyph != null)
		{
			FTOutlineGlyph tempGlyph = new FTOutlineGlyph(ftGlyph, advance);
			return tempGlyph;
		}
		return null;
	}

}