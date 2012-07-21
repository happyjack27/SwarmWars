package World;

import java.awt.*;
import java.util.*;

import Entities.*;

public class Landscape {
	float[][] data;
	float[][] backup;
	Vector<Entity>[][] bins;
	static float[][] one_over_r_kernel;
	static float[][] one_over_r2_kernel;
	
	int width = 0;
	int height = 0;
	int resolution = 0;
	//int max_sight_range = 50;
	//int convole_kernel_size = 30;
	static int kernel_size = 60;
	public float density_multiplier = 1;
	public float draw_multiplier = 1f;
	float min_dist = 2;

	public void apply_kernel_all(Landscape source, float[][] kernel) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[0].length; j++)
				apply_kernel(kernel,source.data[i][j],j,i);
	}

	public void make_from_convolved_noise(float[][] convolve_kernel) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[0].length; j++)
				apply_kernel(convolve_kernel,(float)Time.rand.nextGaussian(),j,i);
		center();
		normalize();
		scale(1f/max());
	}
	
	public static float[][] create_convolve_kernel(float rate) {
		float[][] kernel = new float[kernel_size*2+1][kernel_size*2+1];
		//for( int i = 0; i < kernel_size; i++)
			//kernel[i] = new float[]
		for( int y = 0; y < kernel_size*2+1; y++)
			for( int x = 0; x < kernel_size*2+1; x++) {
				float dx = x-kernel_size;
				float dy = y-kernel_size;
				float dist = (float)Math.sqrt(dx*dx+dy*dy);
				float mult = (float)Math.exp(-dist*rate);
				kernel[y][x] = mult; 
			}
		return kernel;
	}
	
	public float[][] create_1_over_r_kernel() {
		float[][] kernel = new float[kernel_size*2+1][kernel_size*2+1];
		for( int y = 0; y < kernel_size*2+1; y++)
			for( int x = 0; x < kernel_size*2+1; x++) {
				float dx = x-kernel_size;
				float dy = y-kernel_size;
				float dist = (float)Math.sqrt(dx*dx+dy*dy)*resolution+min_dist;
				kernel[y][x] = 1.0f/dist; 
			}
		return kernel;
	}
	public float[][] create_1_over_r2_kernel() {
		float[][] kernel = new float[kernel_size*2+1][kernel_size*2+1];
		for( int y = 0; y < kernel_size*2+1; y++)
			for( int x = 0; x < kernel_size*2+1; x++) {
				float dx = x-kernel_size;
				float dy = y-kernel_size;
				float dist = (float)(dx*dx+dy*dy)*resolution*resolution+min_dist;
				kernel[y][x] = 1.0f/dist; 
			}
		return kernel;
	}
	
	public void apply_kernel(float[][] kernel, float scale, int x, int y) {
		int cy = y-kernel_size < 0 ? 0 : y-kernel_size;
		int ky = cy-y+kernel_size;
		for( ; cy < y+kernel_size && cy < data.length; cy++) {
			int cx = x-kernel_size < 0 ? 0 : x-kernel_size;
			int kx = cx-x+kernel_size;
			for( ; cx < x+kernel_size && cx < data[0].length; cx++) {
				data[cy][cx] += kernel[ky][kx]*scale;
				kx++;
			}
			ky++;
		}
	}
	
	public Landscape(int width, int height, int resolution) {
		this.width = width;
		this.height = height;
		this.resolution = resolution;
		min_dist = resolution/4;
		data = new float[1+height/resolution][];
		for( int i = 0; i < data.length; i++)
			data[i] = new float[1+width/resolution];
		if( one_over_r_kernel == null)
			one_over_r_kernel = create_1_over_r_kernel();
		if( one_over_r2_kernel == null)
			one_over_r2_kernel = create_1_over_r2_kernel();
		
	}
	public void backup() {
		backup = new float[1+height/resolution][];
		for( int i = 0; i < data.length; i++)
			backup[i] = new float[1+width/resolution];
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				backup[i][j] = data[i][j];
	}
	
	public void set(float f) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] = f;
	}
	public void gaussian() {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] = (float)Time.rand.nextGaussian();
	}
	public Landscape convolve(float rate) {
		Landscape ret = new Landscape(width,height,resolution);
		//if( true) {
			ret.apply_kernel_all(this, create_convolve_kernel(rate));
			return ret;
		/*}
		int w = data[0].length;
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < w; j++)
				for( int x = i-convole_kernel_size > 0 ? i-convole_kernel_size : 0; x < data.length && x < i+convole_kernel_size; x++)
					for( int y = j-convole_kernel_size > 0 ? j-convole_kernel_size : 0; y < w && y < j+convole_kernel_size; y++) {
						float dx = i-x;
						float dy = j-y;
						float dist = (float)Math.sqrt(dx*dx+dy*dy);
						float mult = (float)Math.exp(-dist*rate);
						ret.data[i][j] += data[x][y]*mult; 
					}
		return ret;*/
	}
	public void add(Landscape l) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] += l.data[i][j];
	}
	public float min() {
		float min = 100000000000000f;
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++) {
				if( data[i][j] < min)
					min = data[i][j];
			}
		return min;
	}
	public float max() {
		float max = -100000000000000f;
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++) {
				if( data[i][j] > max)
					max = data[i][j];
			}
		return max;
	}
	public void normalize() {
		scale(1f/var());
	}
	public void center() {
		shift(-mean());
	}
	public float var() {
		float var = 0;
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++) {
				var += data[i][j]*data[i][j];
			}
		return (float)Math.sqrt(var/(float)(data.length*data[0].length));
	}
	public float mean() {
		float var = 0;
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++) {
				var += data[i][j];
			}
		return (float)var/(float)(data.length*data[0].length);
	}	
	public void multiply(Landscape l) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] *= l.data[i][j];
	}
	public void scale(float f) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] *= f;
	}
	public void exp(float base) {
		float l = (float)Math.log(base);
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] = (float)Math.exp(data[i][j]*l);
	}
	public void shift(float f) {
		for( int i = 0; i < data.length; i++)
			for( int j = 0; j < data[i].length; j++)
				data[i][j] += f;
	}
	//add interpolation
	public float[] interpolate( int x, int y) {
		float rx = (float)x/(float)resolution;
		float ry = (float)y/(float)resolution;
		int x1 = (int)Math.floor(rx);
		int y1 = (int)Math.floor(ry);
		float dx = rx-(float)x1;
		float dy = ry-(float)y1;
		return bilinear_interpolation(data,x1,y1,dx,dy,false); //scalar, gradx, grady
	}
	
	float[] bilinear_interpolation(float[][] grid, int xl, int yl, float dx, float dy, boolean invert) {
		//scalar,gradx,grady
		if( yl+2 > grid.length)
			yl = grid.length-2;
		if( xl+2 > grid[0].length)
			xl = grid[0].length-2;
		if( xl < 0) xl = 0;
		if( yl < 0) yl = 0;
		float x0y0 = grid[yl][xl];
		float x1y0 = grid[yl][xl+1];
		float x0y1 = grid[yl+1][xl];
		float x1y1 = grid[yl+1][xl+1];
		if( invert) {
			x0y0 = 1.0f/x0y0;
			x0y1 = 1.0f/x0y1;
			x1y0 = 1.0f/x1y0;
			x1y1 = 1.0f/x1y1;
		}
		float y0 = x0y0 + (x1y0-x0y0)*dx;
		float y1 = x0y1 + (x1y1-x0y1)*dx;
		float gradxy0 = x1y0-x0y0;
		float gradxy1 = x1y1-x0y1;
		float gradyx0 = x0y1-x0y0;
		float gradyx1 = x1y1-x1y0;
		return new float[]{y0+(y1-y0)*dy,gradxy0 + (gradxy1-gradxy0)*dy,gradyx0 + (gradyx1-gradyx0)*dx};
	}

	//bins logic=======================================================
	
	public void make_bins() {
		bins = new Vector[data.length][];
		for( int y = 0; y < data.length; y++) {
			bins[y] = new Vector[data[y].length];
			for( int x = 0; x < data[y].length; x++) {
				bins[y][x] = new Vector<Entity>();
			}
		}
	}
	public void addResource(Entity r) {
		update(r,r.mass);
		r.mass_at_last_update = r.mass;

		int x = Math.round(r.x/resolution);
		int y = Math.round(r.y/resolution);
		int h = data.length-1;
		int w = data[0].length-1;
		x = x < 0 ? 0 : x > w ? w : x;
		y = y < 0 ? 0 : y > h ? h : y;
		if( bins == null)
			make_bins();
		bins[y][x].add(r);
	}
	public void updateMass(Entity r) {
		update(r,r.mass-r.mass_at_last_update);
		r.mass_at_last_update = r.mass;
	}
	public void removeResource(Entity r) {
		update(r,-r.mass_at_last_update);
		r.mass_at_last_update = 0;
		
		int x = Math.round(r.x/resolution);
		int y = Math.round(r.y/resolution);
		int h = data.length-1;
		int w = data[0].length-1;
		x = x < 0 ? 0 : x > w ? w : x;
		y = y < 0 ? 0 : y > h ? h : y;
		bins[y][x].remove(r);
	}

	public void update(Entity r,float amt) {
		try {
		float xi = r.x/resolution;
		float yi = r.y/resolution;
		float x = Math.round(xi);
		float y = Math.round(yi);
		int h = data.length-1;
		int w = data[0].length-1;
		x = x < 0 ? 0 : x > w ? w : x;
		y = y < 0 ? 0 : y > h ? h : y;
		//if( true) {
		this.apply_kernel(one_over_r_kernel, amt*density_multiplier, (int)x, (int)y);
			//return;
		//}
		//now update scalar grid
		/*
		float startx = x-max_sight_range; if( startx < 0) startx = 0;
		float starty = y-max_sight_range; if( starty < 0) starty = 0;
		int endx = (int)x+max_sight_range; if( endx > w) endx = w;
		int endy = (int)y+max_sight_range; if( endy > h) endy = h;
		float dy = starty-yi;
		//System.out.println("x:"+startx+" "+endx+" y:"+starty+" "+endy);
		float mass_difference = amt*density_multiplier;
		for( int iy = (int)starty; iy < endy; iy++ ) {
			float dx = startx-xi;
			for( int ix = (int)startx; ix < endx; ix++ ) {
				float dist = (float)Math.sqrt(dx*dx+dy*dy)*resolution;
				if( dist < 0.00001f)
					dist = 0.00001f;
				data[iy][ix] += mass_difference/dist;
				//float f = r.distance_function(dist);
				//resource2_scalar_grid[r.type][iy][ix] += f*r.mass;
				dx++;
			}
			dy++;
		}*/
	} catch (Exception ex) {
		System.out.println("ex on remove: "+ex);
		ex.printStackTrace();
	}
	}
	
	public Vector<Entity> findResources( float fx, float fy, float radius) {
		float xi = fx/resolution;
		float yi = fy/resolution;
		int x = (int)Math.floor(xi);
		int y = (int)Math.floor(yi);
		int h = data.length-2;
		int w = data[0].length-2;
		x = x < 0 ? 0 : x > w ? w : x;
		y = y < 0 ? 0 : y > h ? h : y;
		
		float r2 = radius*radius;
		Vector<Entity> ret = new Vector<Entity>();
		if( bins == null)
			make_bins();
		Iterator<Entity>[] its = new Iterator[]{
				bins[y][x].iterator(),
				bins[y][x+1].iterator(),
				bins[y+1][x].iterator(),
				bins[y+1][x+1].iterator()
				};
		for( int j = 0; j < its.length; j++) {
			while( its[j].hasNext()) {
				Entity r = its[j].next();
				float dx = r.x-fx;
				float dy = r.y-fy;
				if( dx*dx+dy*dy <= r2)
					ret.add(r);
			}
		}
		return ret;
	}
	
	public void refresh() {
		if(backup == null)
			set(0);
		else {
			for( int i = 0; i < data.length; i++)
				for( int j = 0; j < data[i].length; j++)
					data[i][j] = backup[i][j];
		}
		if( bins == null)
			make_bins();
		for(int i = 0; i < bins.length; i++)
			for(int j = 0; j < bins[i].length; j++) {
				Iterator<Entity> ie = bins[i][j].iterator();
				while( ie.hasNext()) {
					Entity e = ie.next();
					if( e.state == Entity.ACTIVE) {
						update(e,e.mass);
						e.mass_at_last_update = e.mass;
					}
				}
			}
	}
	public void draw(Graphics g) {
		draw(g,Color.black,Color.gray,Color.white);
	}
	
	public void draw(Graphics g, Color cmin, Color czero, Color cmax) {
		float nr = cmin.getRed();
		float ng = cmin.getGreen();
		float nb = cmin.getBlue();
		float zr = czero.getRed();
		float zg = czero.getGreen();
		float zb = czero.getBlue();
		float pr = cmax.getRed();
		float pg = cmax.getGreen();
		float pb = cmax.getBlue();
		Color[] csp = new Color[128];
		Color[] csn = new Color[128];
		float dr = (pr-zr)/(csp.length-1);
		float dg = (pg-zg)/(csp.length-1);
		float db = (pb-zb)/(csp.length-1);
		for( float i = 0; i < csp.length; i++)
			csp[(int)i] = new Color((int)(zr+dr*i),(int)(zg+dg*i),(int)(zb+db*i));
		dr = (nr-zr)/(csp.length-1);
		dg = (ng-zg)/(csp.length-1);
		db = (nb-zb)/(csp.length-1);
		for( float i = 0; i < csn.length; i++)
			csn[(int)i] = new Color((int)(zr+dr*i),(int)(zg+dg*i),(int)(zb+db*i));
		g.setColor(csp[0]);
		g.fillRect(0, 0, (int)(resolution*data[0].length), (int)(resolution*data.length));

		int r = 0;
		int f = (int)(resolution/1.0);
		int f2 = (int)(resolution/2.0);
		int fy = -f2;
		
		for( int y = 0; y < data.length; y++) {
			int fx = -f2;
			for( int x = 0; x < data[y].length; x++) {
				float d = draw_multiplier*data[y][x];
				if( d < -1) {
					g.setColor(Color.red);
				}
				else if( d > 1){
					g.setColor(Color.blue);
				} else if( d >= 0){
					int plants = (int)(d*(float)csp.length);
					if( plants >= csp.length)
						plants = csp.length-1;
					g.setColor(csp[plants]);
				} else if( d < 0){
					int plants = (int)(-d*(float)csn.length);
					if( plants >= csn.length)
						plants = csn.length-1;
					g.setColor(csn[plants]);
				}
				g.fillRect(fx, fy, f, f);
				fx += resolution;
			}
			fy += resolution;
		}
		return;
	}
}
