/* $Id: FTPolyGlyph.java,v 1.2 2005/07/27 23:14:31 joda Exp $ */
package net.sourceforge.ftgl.glyph;

import java.awt.Shape;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.FTContour;
import net.sourceforge.ftgl.FTGlyphContainer;
import net.sourceforge.ftgl.FTMesh;
import net.sourceforge.ftgl.FTTesselation;
import net.sourceforge.ftgl.FTVectoriser;

//#include "FTPolyGlyph.h"
//#include "FTVectoriser.h"

/**
 * FTPolyGlyph is a specialisation of FTGlyph for creating tessellated
 * polygon glyphs.
 * 
 * @see FTGlyphContainer
 * @see FTVectoriser
 * 
 */
public class FTPolyGlyph extends FTGlyph
{

	/**
	 * Creates a new FTPolyGlyph
	 * @param glyph The Shape the glyph represents.
	 */
	public FTPolyGlyph(Shape glyph)
	{
		super(glyph);
	}

	/**
	 * Creates a new FTPolyGlyph.
	 * @param glyph The shape th eglyph represents.
	 * @param advance The advance factor of this glyph.
	 */
	public FTPolyGlyph(Shape glyph, float advance)
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

		if ((vectoriser.contourCount() < 1) || (vectoriser.pointCount() < 3))
		{
			return;
		}

		vectoriser.makeMesh(1.0);

		this.glList = this.gl.glGenLists(1);			//TODO verify list!
		this.gl.glNewList(this.glList, GL.GL_COMPILE);

		this.gl.glNormal3d(0.0, 0.0, 1.0);

		final FTMesh mesh = vectoriser.getMesh();
		for (int index = 0; index < mesh.tesselationCount(); ++index)
		{
			final FTTesselation subMesh = mesh.getTesselation(index);
			int polyonType = subMesh.getPolygonType();

			this.gl.glBegin(polyonType);
			for (int x = 0; x < subMesh.pointCount(); ++x)
			{
				this.gl.glVertex3f((float) subMesh.getPoint(x)[FTContour.X] /*/ 64.0f */, (float) subMesh.getPoint(x)[FTContour.Y] /*/ 64.0f */, 0.0f);
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
		if (this.gl.glIsList(glList))
		{
			this.gl.glTranslatef((float) x, (float) y, 0.0f);
			this.gl.glCallList(glList);
			this.gl.glTranslatef((float) -x, (float) -y, 0.0f);
		}

		return advance;
	}
}