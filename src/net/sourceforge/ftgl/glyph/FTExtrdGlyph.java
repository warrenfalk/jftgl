/* $Id: FTExtrdGlyph.java,v 1.8 2005/07/27 23:14:31 joda Exp $ */
package net.sourceforge.ftgl.glyph;

import java.awt.Shape;
import java.awt.geom.PathIterator;

import net.java.games.jogl.GL;
import net.sourceforge.ftgl.FTContour;
import net.sourceforge.ftgl.FTGlyphContainer;
import net.sourceforge.ftgl.FTMesh;
import net.sourceforge.ftgl.FTTesselation;
import net.sourceforge.ftgl.FTVectoriser;
import net.sourceforge.ftgl.util.Vector3f;

/**
 * FTExtrdGlyph is a specialisation of FTGlyph for creating tessellated extruded polygon glyphs.
 * @see FTGlyphContainer
 * @see FTVectoriser
 */
public class FTExtrdGlyph extends FTGlyph
{

	/**
	 * Distance to extrude the glyph
	 */
	private float depth;


	/**
	 * Constructor. Sets the Error to Invalid_Outline if the glyphs isn't an outline.
	 * @param glyph The Freetype glyph to be processed
	 * @param depth The distance along the z axis to extrude the glyph
	 */
	public FTExtrdGlyph(final Shape glyph, float depth)
	{
		super(glyph);
		this.depth = depth;
		this.bBox.setDepth(-depth);
	}

	public FTExtrdGlyph(final Shape glyph, float depth, float advance)
	{
		super(glyph, advance);
		this.depth = depth;
		this.bBox.setDepth(-depth);
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

		int tesselationIndex;
		this.glList = this.gl.glGenLists(1); // TODO verifyList();
		this.gl.glNewList(this.glList, GL.GL_COMPILE);

		vectoriser.makeMesh(1.0);
		this.gl.glNormal3d(0.0, 0.0, 1.0);

		FTMesh mesh = vectoriser.getMesh();
		for (tesselationIndex = 0; tesselationIndex < mesh.tesselationCount(); ++tesselationIndex)
		{
			FTTesselation subMesh = mesh.getTesselation(tesselationIndex);
			int polygonType = subMesh.getPolygonType();

			this.gl.glBegin(polygonType);
			for (int pointIndex = 0; pointIndex < subMesh.pointCount(); ++pointIndex)
			{
				this.gl.glVertex3f((float)subMesh.getPoint(pointIndex)[FTContour.X] /*/ 64.0f*/, // TODO 64?
					(float)subMesh.getPoint(pointIndex)[FTContour.Y] /*/ 64.0f*/, 0.0f);
			}
			this.gl.glEnd();
		}

		vectoriser.makeMesh(-1.0);
		this.gl.glNormal3d(0.0, 0.0, -1.0);

		mesh = vectoriser.getMesh();
		for (tesselationIndex = 0; tesselationIndex < mesh.tesselationCount(); ++tesselationIndex)
		{
			FTTesselation subMesh = mesh.getTesselation(tesselationIndex);
			int polygonType = subMesh.getPolygonType();

			this.gl.glBegin(polygonType);
			for (int pointIndex = 0; pointIndex < subMesh.pointCount(); ++pointIndex)
			{
				this.gl.glVertex3f((float)subMesh.getPoint(pointIndex)[FTContour.X] /*/ 64.0f*/, // TODO 64?
					(float)subMesh.getPoint(pointIndex)[FTContour.Y] /*/ 64.0f*/, -this.depth);
			}
			this.gl.glEnd();
		}

		int contourFlag = vectoriser.contourFlag();
		boolean nonzero = (contourFlag != PathIterator.WIND_NON_ZERO);

		for (int c = 0; c < vectoriser.contourCount(); ++c)
		{
			FTContour contour = vectoriser.contour(c);
			int numberOfPoints = contour.pointCount();

			Vector3f oldNormal = FTExtrdGlyph.getNormal(contour.getPoint(numberOfPoints-1), contour.getPoint(0));
			this.gl.glBegin(GL.GL_QUAD_STRIP);
			for (int j = 0; j <= numberOfPoints; ++j)
			{
				int index = (j == numberOfPoints) ? 0 : j;
				int nextIndex = (index == numberOfPoints - 1) ? 0 : index + 1;

				Vector3f flatNormal = FTExtrdGlyph.getNormal(contour.getPoint(index), contour.getPoint(nextIndex));
				boolean smoothEdge = Math.abs(flatNormal.angle(oldNormal))<Math.PI/4;
				Vector3f normal;
				if (smoothEdge)
				{
					normal = new Vector3f(flatNormal).add(oldNormal).scale(1/2f);
				}
				else
				{
					normal = oldNormal;
					this.gl.glNormal3f(normal.x, normal.y, 0.0f);
					if (nonzero)// & ft_outline_reverse_fill) //FT_LIB
					{
						this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
							(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f); // TODO 64?
						this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
							(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -this.depth);
					}
					else
					{
						this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
							(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -this.depth); // TODO 64?
						this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
							(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f);
					}
					normal = flatNormal;
					gl.glEnd();
					gl.glBegin(GL.GL_QUAD_STRIP);
				}

				this.gl.glNormal3f(normal.x, normal.y, 0.0f);
				if (nonzero)// & ft_outline_reverse_fill) //FT_LIB
				{
					this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f); // TODO 64?
					this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -this.depth);
				}
				else
				{
					this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -this.depth); // TODO 64?
					this.gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f);
				}
				oldNormal = flatNormal;
			}
			this.gl.glEnd();
			assert displayNormals(vectoriser);
		}
		this.gl.glEndList();
	}

	private boolean displayNormals(FTVectoriser vectoriser)
	{
		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glBegin(GL.GL_LINES);
		for (int c = 0; c < vectoriser.contourCount(); ++c)
		{
			FTContour contour = vectoriser.contour(c);
			int numberOfPoints = contour.pointCount();
			Vector3f oldNormal = FTExtrdGlyph.getNormal(contour.getPoint(0), contour.getPoint(1));
			for (int j = 0; j <= numberOfPoints; ++j)
			{
				int index = (j == numberOfPoints) ? 0 : j;
				int nextIndex = (index == numberOfPoints - 1) ? 0 : index + 1;

				Vector3f flatNormal = FTExtrdGlyph.getNormal(contour.getPoint(index), contour.getPoint(nextIndex));
				boolean smoothEdge = Math.abs(oldNormal.angle(flatNormal))<Math.PI/4;
				Vector3f normal;
				if (smoothEdge)
				{
					normal = new Vector3f(flatNormal).add(oldNormal).scale(1/2f);
				}
				else
				{
					normal = new Vector3f(oldNormal).scale(4f, 4f, 0f);
					gl.glNormal3f(normal.x, normal.y, 0.0f);
					gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f); // TODO 64?
					gl.glVertex3f((float)contour.getPoint(index)[FTContour.X]+normal.x /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y]+normal.y /*/ 64.0f*/, 0.0f); // TODO 64?
					gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -depth);
					gl.glVertex3f((float)contour.getPoint(index)[FTContour.X]+normal.x /*/ 64.0f*/,
						(float)contour.getPoint(index)[FTContour.Y]+normal.y /*/ 64.0f*/, -depth);
				}
//				System.out.print("Contourflag:");
//				switch (vectoriser.contourFlag())
//				{
//					case PathIterator.WIND_EVEN_ODD:
//						System.out.println("even odd");
//						break;
//					case PathIterator.WIND_NON_ZERO:
//						System.out.println("non zero");
//						break;
//					default:
//						System.out.println("unknown");
//						break;
//				}
				normal = new Vector3f(flatNormal).scale(4f, 4f, 0f);
				gl.glNormal3f(normal.x, normal.y, 0.0f);
				gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
					(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, 0.0f); // TODO 64?
				gl.glVertex3f((float)contour.getPoint(index)[FTContour.X]+normal.x /*/ 64.0f*/,
					(float)contour.getPoint(index)[FTContour.Y]+normal.y /*/ 64.0f*/, 0.0f); // TODO 64?
				gl.glVertex3f((float)contour.getPoint(index)[FTContour.X] /*/ 64.0f*/,
					(float)contour.getPoint(index)[FTContour.Y] /*/ 64.0f*/, -depth);
				gl.glVertex3f((float)contour.getPoint(index)[FTContour.X]+normal.x /*/ 64.0f*/,
					(float)contour.getPoint(index)[FTContour.Y]+normal.y /*/ 64.0f*/, -depth);
				oldNormal = flatNormal;
			}
		}
		gl.glEnd();
		gl.glPopAttrib();
		return true;
	}

	/**
	 * Calculate the normal vector to 2 points. This is 2D and ignores the z component. The normal
	 * will be normalised
	 * @param a
	 * @param b
	 * @return
	 */
	private static Vector3f getNormal(double[] a, double[] b)
	{
		float vectorX = (float)(a[FTContour.X] - b[FTContour.X]);
		float vectorY = (float)(a[FTContour.Y] - b[FTContour.Y]);

		float length = (float)Math.sqrt(vectorX * vectorX + vectorY * vectorY);

		if (length > 0.0f)
		{
			length = 1 / length;
		}
		else
		{
			length = 0.0f;
		}

		return new Vector3f((float)vectorY * length, (float)-vectorX * length, 0.0f);
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
	public float render(final float x, final float y, final float z)
	{
		if (this.gl.glIsList(this.glList))
		{
			this.gl.glTranslatef((float)x, (float)y, 0);
			this.gl.glCallList(this.glList);
			this.gl.glTranslatef((float)-x, (float)-y, 0);
		}
		return advance;
	}

}