package org.newdawn.slick.tests;
	
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

/**
 * A test of the font rendering capabilities
 *
 * @author kevin
 */
public class FontTest extends BasicGame {
	/** The font we're going to use to render */
	private AngelCodeFont font;
	/** The font we're going to use to render */
	private AngelCodeFont font2;
	/** The image of the font to compare against */
	private Image image;
	/** Custom rendering */
	private float widthMult = 1;
	/** Custom rendering */
	private float width = 0;
	
	
	/**
	 * Create a new test for font rendering
	 */
	public FontTest() {
		super("Font Test");
	}
	
	/**
	 * @see org.newdawn.slick.Game#init(org.newdawn.slick.GameContainer)
	 */
	public void init(GameContainer container) throws SlickException {
		font = new AngelCodeFont("testdata/demo2.fnt","testdata/demo2_00.tga");
		font2 = new AngelCodeFont("testdata/hiero.fnt","testdata/hiero.png");
		image = new Image("testdata/demo2_00.tga", false);
	}

	/**
	 * @see org.newdawn.slick.BasicGame#render(org.newdawn.slick.GameContainer, org.newdawn.slick.Graphics)
	 */
	public void render(GameContainer container, Graphics g) {
		if (g.getFont() instanceof AngelCodeFont) {
			AngelCodeFont f = (AngelCodeFont)g.getFont();
			String t = "testing baseline";
			float w = f.getWidth(t);
			g.drawString(t, 400, 500);
			g.setColor(Color.red);
			int ascent = f.getAscent();
			g.drawLine(400, 500+ascent, 400+w, 500+ascent);
		}
		
		
		
		font.drawString(80, 5, "A Font Example", Color.red);
		font.drawString(100, 32, "We - AV - Here is a more complete line that hopefully");
		font.drawString(100, 36 + font.getHeight("We Here is a more complete line that hopefully"), 
				             "will show some kerning.");
		
		font2.drawString(80, 85, "A Font Example", Color.red);
		font2.drawString(100, 132, "We - AV - Here is a more complete line that hopefully");
		font2.drawString(100, 136 + font2.getHeight("We - Here is a more complete line that hopefully"), 
				             "will show some kerning.");
		image.draw(100,400);
		
		String testStr = "Testing Font";
		font2.drawString(100, 300, testStr);
		g.setColor(Color.white);
		g.drawRect(100,300+font2.getYOffset(testStr),font2.getWidth(testStr),font2.getHeight(testStr)-font2.getYOffset(testStr));
		font.drawString(500, 300, testStr);
		g.setColor(Color.white);
		g.drawRect(500,300+font.getYOffset(testStr),font.getWidth(testStr),font.getHeight(testStr)-font.getYOffset(testStr));
		
		g.setColor(Color.white);
		drawTextRotated(font, "custom font render", 500, 350, width);
		g.drawRect(500, 350, width, font.getLineHeight());
	}
	
	/** 
	 * Advanced text rendering using AngelCodeFont's getGlyph method.
	 * @param font
	 * @param text
	 * @param x
	 * @param y
	 * @param maxWidth
	 */
	private void drawTextRotated(AngelCodeFont font, CharSequence text, float x, float y, float maxWidth) {
		AngelCodeFont.Glyph lastDef = null;
		
		//important: start and end the sheet if we are using drawEmbedded
		font.getImage().startUse();
		
		float startX = x;
		x = 0;
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (c=='\n') {
				y += font.getLineHeight();
				x = 0;
			}
			AngelCodeFont.Glyph def = font.getGlyph(c);
			//glyph not found .. :(
			if (def==null)
				continue;
			//get kerning info
			if (lastDef!=null) {
				x += lastDef.getKerning(c);
			}
			lastDef = def;
			
			if (def.xoffset + def.width + x > maxWidth) 
				break;
			
			//handle drawing here. let's rotate it eh?
			Image subImage = def.image;
			subImage.drawEmbedded(startX + x + def.xoffset, y + def.yoffset, def.width, def.height);
			
			//push next advance
			x += def.xadvance;
		}
		
		font.getImage().endUse();
	}
	
	/**
	 * @see org.newdawn.slick.BasicGame#update(org.newdawn.slick.GameContainer, int)
	 */
	public void update(GameContainer container, int delta) throws SlickException {
		width += delta * 0.1f * widthMult;
		if (width > 250 || width < 0)
			widthMult = widthMult==1 ? -1 : 1;
	}
	
	/**
	 * @see org.newdawn.slick.BasicGame#keyPressed(int, char)
	 */
	public void keyPressed(int key, char c) {
		if (key == Input.KEY_ESCAPE) {
			System.exit(0);
		}
		if (key == Input.KEY_SPACE) {
			try {
				container.setDisplayMode(640, 480, false);
			} catch (SlickException e) {
				Log.error(e);
			}
		}
	}
	
	/** The container we're using */
	private static AppGameContainer container;
	
	/**
	 * Entry point to our test
	 * 
	 * @param argv The arguments passed in the test
	 */
	public static void main(String[] argv) {
		try {
			container = new AppGameContainer(new FontTest());
			container.setDisplayMode(800,600,false);
			container.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
