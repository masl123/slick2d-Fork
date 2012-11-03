package org.newdawn.slick.font.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.Glyph;

/**
 * A filter to create a distance field from a source image.
 * 
 * <p> Signed distance fields are used in Team Fortress 2 by Valve to enable
 * sharp rendering of bitmap fonts even at high magnifications,
 * using nothing but alpha testing so at no extra runtime cost.
 * 
 * <p> The technique is described in the SIGGRAPH 2007 paper
 * "Improved Alpha-Tested Magniﬁcation for Vector Textures and Special Effects" by Chris Green:
 * <a href="http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf">
 * http://www.valvesoftware.com/publications/2007/SIGGRAPH2007_AlphaTestedMagnification.pdf
 * </a>
 * 
 * @author Orangy
 * @author ttencate
 */
public class DistanceFieldEffect implements ConfigurableEffect
{
	private Color color = Color.white;
	private int spread = 4;
	private int downscale = 8;
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public int getSpread() {
		return spread;
	}
	
	public void setSpread(int spread) {
		this.spread = Math.max(1, spread);
	}
	
	public int getDownscale() {
		return downscale;
	}
	
	public void setDownscale(int downscale) {
		this.downscale = Math.max(1, downscale);
	}
	
	/**
	 * Caclulate the squared distance between two points
	 * 
	 * @param x1 The x coordinate of the first point
	 * @param y1 The y coordiante of the first point
 	 * @param x2 The x coordinate of the second point
	 * @param y2 The y coordinate of the second point
	 * @return The squared distance between two point
	 */
	private static int squareDist(final int x1, final int y1, final int x2, final int y2)
	{
		final int dx = x1 - x2;
		final int dy = y1 - y2;
		return dx*dx + dy*dy;
	}
	
	/**
	 * Process the image into a distance field
	 * 
	 * @param inImage The image to process
	 * @return The distance field image
	 */
	public BufferedImage process(BufferedImage inImage)
	{
		int outWidth = inImage.getWidth() / downscale;
		int outHeight = inImage.getHeight() / downscale;
		
		BufferedImage outImage = new BufferedImage(outWidth, outHeight, BufferedImage.TYPE_4BYTE_ABGR);
		float[][] distances = new float[outWidth][outHeight];
		
		boolean[][] bitmap = new boolean[inImage.getWidth()][inImage.getHeight()];
		for (int x = 0; x < inImage.getWidth(); ++x) {
			for (int y = 0; y < inImage.getHeight(); ++y) {
				// Any colour with one of its channels greater than 128 is considered "inside"
				bitmap[x][y] = (inImage.getRGB(x, y) & 0x808080) != 0;
			}
		}
		
		for (int x=0; x<outImage.getWidth(); x++)
		{
			for (int y=0; y<outImage.getHeight(); y++)
			{
				distances[x][y] = findSignedDistance( (x * downscale) + (downscale / 2),
													  (y * downscale) + (downscale / 2),
													  bitmap);
			}
		}

		for (int x=0; x<outWidth; x++)
		{
			for (int y=0; y<outHeight; y++)
			{
				float d = distances[x][y];
				int alpha = (int) (d * 255);
				outImage.setRGB(x, y, (alpha << 24) | (color.getRGB() & 0xFFFFFF));
			}
		}
		
		return outImage;
	}
	
	/**
	 * Find the signed distance for a given point
	 * 
	 * @param pointX The x coordinate of the point 
	 * @param pointY The y coordinate of the point
	 * @param bitmap The image on which the point exists
	 * @return The signed distance
	 */
	private float findSignedDistance(final int pointX, final int pointY, boolean[][] bitmap)
	{
		final int width = bitmap.length;
		final int height = bitmap[0].length;
		final boolean base = bitmap[pointX][pointY];
		
		int maxDistance = downscale * spread;
		final int startX = Math.max(0, pointX - maxDistance);
		final int endX  = Math.min(width - 1, pointX + maxDistance);
		final int startY = Math.max(0, pointY - maxDistance);
		final int endY = Math.min(height - 1, pointY + maxDistance);

		final int squareRadius = maxDistance * maxDistance;
		int closestSquareDist = squareRadius;
		
		for (int x=startX; x<=endX; x++)
		{
			for (int y=startY; y<=endY; y++)
			{
				final int sqDist = squareDist(pointX, pointY, x, y);
				if (sqDist > squareRadius) {
					continue;
				}
				if (base != bitmap[x][y] && sqDist < closestSquareDist)
				{
					closestSquareDist = sqDist;
				}
			}
		}
		
		float closestDist = (float) Math.sqrt(closestSquareDist);
		if (base)
		{
			return 0.5f + 0.5f * Math.min(1.0f, closestDist / maxDistance);
		}
		else
		{
			return 0.5f - 0.5f * Math.min(1.0f, closestDist / maxDistance);
		}
	}

	@Override
	public void draw(BufferedImage image, Graphics2D g, UnicodeFont unicodeFont, Glyph glyph) {
		int inputWidth = downscale * glyph.getWidth();
		int inputHeight = downscale * glyph.getHeight();
		
		BufferedImage input = new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D inputG = (Graphics2D) input.getGraphics();
		inputG.setTransform(AffineTransform.getScaleInstance(downscale, downscale));
		// We don't really want anti-aliasing, but accurate positioning is nice
		inputG.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		inputG.setColor(Color.WHITE);
		inputG.fill(glyph.getShape());
		
		BufferedImage distanceField = process(input);
		
		g.drawImage(distanceField, new AffineTransform(), null);
	}

	@Override
	public List getValues() {
		List values = new ArrayList();
		values.add(EffectUtil.colorValue("Color", getColor()));
		values.add(EffectUtil.intValue("Downscale", getDownscale(), "Downscale (even number recommended)"));
		values.add(EffectUtil.intValue("Spread", getSpread(), "Spread (in pixels)"));
		return values;
	}

	@Override
	public void setValues(List values) {
		for (Iterator iter = values.iterator(); iter.hasNext();) {
			Value value = (Value)iter.next();
			if (value.getName().equals("Color")) {
				setColor((Color)value.getObject());
			} else if (value.getName().equals("Downscale")) {
				setDownscale((Integer)value.getObject());
			} else if (value.getName().equals("Spread")) {
				setSpread((Integer)value.getObject());
			}
		}
		
	}
	
	@Override
	public String toString() {
		return "Distance field";
	}
}