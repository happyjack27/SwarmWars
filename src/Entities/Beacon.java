package Entities;

import java.awt.Color;
import java.awt.Graphics;

public class Beacon extends Entity {
	public static int NUM_TYPES = 2;

	public static int lt1 = 216;
	public static Color[] beacon_colors = new Color[]{
		new Color(255,lt1,lt1),
		new Color(lt1,255,lt1),
		new Color(lt1,lt1,255),
	};
	public static String[] pherome_names = new String[]{
		"Beacon R",
		"Beacon G",
		"Beacon B",
	};
	public void do_time(float dt) {
		
	}
	public void draw(Graphics g) {
		/*
	   	 radius = 10;
	   	 g.setColor(color.brighter());
		 g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
		 */
	   	 radius = 8;
	   	 g.setColor(color);
		 g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
	}

}
