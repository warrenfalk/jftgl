/* $Id: FTOutlineGlyph.java,v 1.2 2005/07/27 23:14:31 joda Exp $ */
package net.sourceforge.ftgl.glyph;

import java.awt.Shape;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.FTContour;
import net.sourceforge.ftgl.FTGlyphContainer;
import net.sourceforge.ftgl.FTVectoriser;

/**
 * FTOutlineGlyph is a specialisation of FTGlyph for creating outlines.
 * @see FTGlyphContainer
 * @see FTVectoriser
 */
public class FTOutlineGlyph extends FTGlyph
{


	/**
	 * Constructor.
	 * @param glyph The glyph to be processed
	 */
	public FTOutlineGlyph(Shape glyph)
	{
		super(glyph);
	}

	/**
	 * Constructor.
	 * @param glyph The glyph to be processed.
	 * @param advance The advance of the glyph.
	 */
	public FTOutlineGlyph(Shape glyph, float advance)
	{
		super(glyph, advance);
	}


	/**
	 * {@inheritDoc}
	 */
	protected void createDisplayList()
	{
		FTVectoriser vectoriser = new FTVectoriser(this.glyph);
		vectoriser.setGLGLU(this.gl, this.glu);

		int numContours = vectoriser.contourCount();
		if ((numContours < 1) || (vectoriser.pointCount() < 3))
		{
			return;
		}

		this.glList = this.gl.glGenLists(1); // TODO verifyList!
		this.gl.glNewList(this.glList, GL.GL_COMPILE);
		for (int c = 0; c < numContours; ++c)
		{
			final FTContour contour = vectoriser.contour(c);

			this.gl.glBegin(GL.GL_LINE_LOOP);
			for (int p = 0; p < contour.pointCount(); ++p)
			{
				this.gl.glVertex2f((float)contour.getPoint(p)[FTContour.X] /* / 64.0f */, (float)contour.getPoint(p)[FTContour.Y] /*/64.0f*/);
			}
			this.gl.glEnd();
		}
		this.gl.glEndList();
	}

	/**
	 * {@inheritDoc}
	 */
	public float render(final float x, final float y, final float z)
	{
		if (this.gl.glIsList(this.glList))
		{
			this.gl.glTranslatef((float)x, (float)y, 0.0f);
			this.gl.glCallList(this.glList);
			this.gl.glTranslatef((float)-x, (float)-y, 0.0f);
		}

		return advance;
	}

}