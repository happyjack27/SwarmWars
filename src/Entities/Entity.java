package Entities;
import java.awt.*;

import Player.*;
import World.Temporal;
import World.Time;


public abstract class Entity extends Temporal {
	public long born = 0;
	public static int id_enumerator = 0;
	public int id = 0;
	
	public float x = 0;
	public float y = 0;
	public float radius = 0;
	public int type = 0;
	public Color color = Color.black;
	public int state = 0;
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	public float mass = 4;
	public static float initial_mass = 4;
	public float mass_at_last_update = 0;
	public Player owner = null;
	
	public int getAge() { return (int)(Time.game_time - born); }
	public void reset() {
		born = Time.game_time;
		mass_at_last_update = 0;
	}
	
	public void draw(Graphics g) {
	   	 g.setColor(color);
		 g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
	}
	
	public static Color randomColor() {
		int r = (int)(Math.random()*256.0);
		int g = (int)(Math.random()*256.0);
		int b = (int)(Math.random()*256.0);
		return new Color(r,g,b);
	}
	public static boolean invert = true;
	public float distance_function(float distance) {
		float f = distance*distance;
		//return (float)Math.exp(-f);
		return 1.0f/(float)f;
	}


}
