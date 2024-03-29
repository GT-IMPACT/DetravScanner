package com.detrav.gui;

import com.detrav.gui.textures.DetravMapTexture;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * Created by wital_000 on 21.03.2016.
 */
public class DetravScannerGUI extends GuiScreen {
	public static final int GUI_ID = 20;
	private final static int minHeight = 128;
	private final static int minWidth = 128;
	private static final ResourceLocation back = new ResourceLocation("gregtech:textures/gui/propick.png");
	private static DetravMapTexture map = null;
	OresList oresList = null;
	private int prevW;
	private int prevH;
	
	public DetravScannerGUI() {
	
	}
	
	public static void newMap(DetravMapTexture aMap) {
		if (map != null) {
			map.deleteGlTexture();
			map = null;
		}
		map = aMap;
		map.loadTexture(null);
	}
	
	
	@Override
	public void drawScreen(int x, int y, float f) {
		this.drawDefaultBackground();
		if (map == null) return;
		
		int currentWidth = Math.max(map.width, minWidth);
		int currentHeight = Math.max(map.height, minHeight);
		int aX = (this.width - currentWidth - 100) / 2;
		int aY = (this.height - currentHeight) / 2;
		
		if (oresList == null || (prevW != width || prevH != height)) {
			oresList = new OresList(
					this, 100, currentHeight, aY, aY + currentHeight, aX + currentWidth, 10, map.packet.ores,
					((name, invert) -> {
						if (map != null) map.loadTexture(null, name, invert);
					})
			);
			prevW    = width;
			prevH    = height;
		}
		
		// draw back for ores
		drawRect(aX, aY, aX + currentWidth + 100, aY + currentHeight, 0xFFC6C6C6);
		map.glBindTexture();
		map.draw(aX, aY);
		oresList.drawScreen(x, y, f);
		mc.getTextureManager().bindTexture(back);
		GL11.glColor4f(0xFF, 0xFF, 0xFF, 0xFF);
		
		// draw corners
		drawTexturedModalRect(aX - 5, aY - 5, 0, 0, 5, 5);//leftTop
		drawTexturedModalRect(aX + currentWidth + 100, aY - 5, 171, 0, 5, 5);//RightTop
		drawTexturedModalRect(aX - 5, aY + currentHeight, 0, 161, 5, 5);//leftDown
		drawTexturedModalRect(aX + currentWidth + 100, aY + currentHeight, 171, 161, 5, 5);//RightDown
		
		// draw edges
		for (int i = aX; i < aX + currentWidth + 100; i += 128) drawTexturedModalRect(i, aY - 5, 5, 0, Math.min(128, aX + currentWidth + 100 - i), 5); //top
		for (int i = aX; i < aX + currentWidth + 100; i += 128) drawTexturedModalRect(i, aY + currentHeight, 5, 161, Math.min(128, aX + currentWidth + 100 - i), 5); //down
		for (int i = aY; i < aY + currentHeight; i += 128) drawTexturedModalRect(aX - 5, i, 0, 5, 5, Math.min(128, aY + currentHeight - i)); //left
		for (int i = aY; i < aY + currentHeight; i += 128) drawTexturedModalRect(aX + currentWidth + 100, i, 171, 5, 5, Math.min(128, aY + currentHeight - i)); //right
		
		int ID = map.packet.ptype;
		final int wh = (map.packet.size * 2 + 1) * 16;
		for (int i = 0; i < wh; i++) {
			for (int j = 0; j < wh; j++) {
				if (map.packet.map[i][j] != null) {
					switch (ID) {
						case 3: // pollution
							break;
						case 2:
							if (i % 16 == 1 && j % 16 == 1) {
								final short fluidSize = map.packet.map[i][j].get((byte) 2);
								short idFluid = map.packet.map[i][j].get((byte) 1);
								final String name = map.packet.metaMap.get(idFluid);
								if (map.selected.equals("All") || map.selected.equals(name)) {
									GL11.glPushMatrix();
									GL11.glScaled(.5d, .5d, .5d);
									Color colorf = Color.WHITE;
									fontRendererObj.drawString(fluidSize + "", (int) ((i + aX + 1) * 10d / 5d), (int) ((j + aY + 19 - fontRendererObj.FONT_HEIGHT) * 10d / 5d), colorf.hashCode(), true);
									GL11.glPopMatrix();
								}
							}
							break;
						case 4: // impact VR ores
						case 5:
						case 6:
						case 7:
						case 8:
							if (i % 16 == 0 && j % 16 == 0) {
								short veinSize = map.packet.map[i][j].get((byte) 2);
								short idVein = map.packet.map[i][j].get((byte) 1);
								final String name = map.packet.metaMap.get(idVein);
								if (map.selected.equals("All") || map.selected.equals(name)) {
									GL11.glPushMatrix();
									GL11.glScaled(.5d, .5d, .5d);
									Color color = Color.WHITE;
									fontRendererObj.drawString(veinSize + "k", (int) ((i + aX + 2) * 10d / 5d), (int) ((j + aY + 20 - fontRendererObj.FONT_HEIGHT) * 10d / 5d), color.hashCode(), true);
									GL11.glPopMatrix();
								}
							}
							break;
					}
				}
			}
		}
		
	}
}