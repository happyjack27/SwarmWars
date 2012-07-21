package World;

import java.awt.*;

import Entities.*;

public class Climate extends Temporal {
	int width = 0;
	int height = 0;
	int resolution = 0;
	
	//Landscape scratch;

	public Landscape meat;
	public Soil soil;
	public Landscape meds;
	
	public Landscape temperature;
	public Landscape humidity;
	public Landscape elevation;
	
	public void do_time(float dt) {
		if(Time.tick_count % 250 == 0) {
			soil.refresh();
			meat.refresh();
			meds.refresh();
			System.out.println("refreshed grid");
		}
	}
	
	public Climate(int width, int height, int resolution) {
		super();
		System.out.println("initalizing climate...");
		this.width = width;
		this.height = height;
		this.resolution = resolution;
		//scratch = new Landscape(width,height,resolution);
		System.out.println("initalizing meat...");
		meat = new Landscape(width,height,resolution);
		System.out.println("initalizing soil...");
		soil = new Soil(width,height,resolution);
		System.out.println("initalizing meds...");
		meds = new Landscape(width,height,resolution);
		
		System.out.println("initalizing temperature...");
		temperature = new Landscape(width,height,resolution);
		

		temperature.make_from_convolved_noise(Landscape.create_convolve_kernel(0.1f));
		System.out.println("initalizing humidity...");
		humidity = new Landscape(width,height,resolution);
		humidity.make_from_convolved_noise(Landscape.create_convolve_kernel(0.1f));
		System.out.println("initalizing elevation...");
		elevation = new Landscape(width,height,resolution);
		elevation.make_from_convolved_noise(Landscape.one_over_r_kernel);
		System.out.println("climate initalized.");
	}
	
	public void addResource(Entity r) {
		if( r.type == 0)
			soil.addResource(r);
		else if( r.type == 1)
			meat.addResource(r);
		else if( r.type == 2)
			meds.addResource(r);
	}
	public void removeResource(Entity r) {
		if( r.type == 0)
			soil.removeResource(r);
		else if( r.type == 1)
			meat.removeResource(r);
		else if( r.type == 2)
			meds.removeResource(r);
	}
	public void updateMass(Entity r) {
		if( r.type == 0)
			soil.updateMass(r);
		else if( r.type == 1)
			meat.updateMass(r);
		else if( r.type == 2)
			meds.updateMass(r);
	}

	
	static int display_mode = 4;
	static int num_display_modes = 8;
	public static void changeDisplayMode() {
		display_mode++;
		display_mode %= num_display_modes; 
	}

	public void draw(Graphics g) {
		if( display_mode == 5) {
			soil.aquifer.draw(g,Color.black,Color.black,Color.blue);
			return;
		}
		if( display_mode == 6) {
			temperature.draw(g,Color.blue,Color.white,Color.red);
			return;
		}
		if( display_mode == 7) {
			elevation.draw(g,Color.black,Color.gray,Color.white);
			return;
		}
		float draw_multiplier = 20f;
		float br = 226;
		float bg = 226;
		float bb = 128;
		float pr = Resource.resource_colors[0].getRed();
		float pg = Resource.resource_colors[0].getGreen();
		float pb = Resource.resource_colors[0].getBlue();
		Color[] cs = new Color[128];
		float dr = (pr-br)/(cs.length-1);
		float dg = (pg-bg)/(cs.length-1);
		float db = (pb-bb)/(cs.length-1);
		for( float i = 0; i < cs.length; i++)
			cs[(int)i] = new Color((int)(br+dr*i),(int)(bg+dg*i),(int)(bb+db*i));
		g.setColor(cs[0]);
		g.fillRect(0, 0, (int)(resolution*soil.data[0].length), (int)(resolution*soil.data.length));
		if( display_mode == 3)
			return;
		if( display_mode == 4) {
			//Color c = new Color(0,0,0);
			int r = 0;
			int f = (int)(resolution/1.0);
			int f2 = (int)(resolution/2.0);
			int fy = -f2;
			
			for( int y = 0; y < soil.data.length; y++) {
				int fx = -f2;
				for( int x = 0; x < soil.data[y].length; x++) {
					float d = draw_multiplier*soil.data[y][x];
					int plants = (int)(d*(float)cs.length*0.05f*0.1f);
					if( plants > 0) {
						if( plants >= cs.length)
							plants = cs.length-1;
						g.setColor(cs[plants]);
						g.fillRect(fx, fy, f, f);
					}
					fx += resolution;
				}
				fy += resolution;
			}
			return;
		}
		int r = display_mode;
		Landscape source = r == 0 ? soil : r == 1 ? meat : r == 2 ? meds : null;
		g.setColor(Resource.resource_colors[r]);
		int fy = 0;
		for( int y = 0; y < source.data.length; y++) {
			float fx = 0;
			for( int x = 0; x < soil.data[y].length; x++) {
				float f = draw_multiplier*source.data[y][x];
				f = (float)Math.sqrt(f);
				g.drawRect((int)(fx-f/2), (int)(fy-f/2), (int)f, (int)f);
				fx += resolution;
			}
			fy += resolution;
		}
	}

}
