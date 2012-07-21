package Entities;
import java.awt.*;

public class Resource extends Entity {
	public static int NUM_TYPES = 3;
	public static float[] decays = new float[]{0,0,0};
	public void do_time(float dt) {
		System.out.println("resource do_time called");
		//this.
		//if( resource.)
	}
	public boolean didDecay() {
		double half_life = 10;
		if( type == 2) {
			double t = getAge()*Math.random();
			if( t > half_life)
				return true;
		}
		return false; 
	}

	public static Color[] resource_colors = new Color[]{
		new Color(0,192,0),
		new Color(192,0,0),
		new Color(0,0,192),
	};
	public static String[] resource_names = new String[]{
		"Plant",
		"Meat",
		"Medicine",
		//"Organic Material", //used for reproduction, repair, and pheromes 
		//"Other" //used for projectiles?
	};
	public void draw(Graphics g) {
	   	 radius = 2;
	   	 g.setColor(color);
		 //g.fillOval((int)(x-radius)+1, (int)(y-radius)+1, (int)(radius+radius)-2, (int)(radius+radius)-2);
		 g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
	}
}
