package World;

import java.util.*;

import Entities.Entity;


//Your barnes-hut tree-code algorithm is dead.

//on this one do the reading w/the kernel, instead of the writing.
//Proportional Entropy Telescoping Multi-Scalar Grid - PetMSG = petsalt = PetRock2
public class PetRock2 {
	
	float[][][] data;
	float[][] backup;
	Vector<Entity>[][] bins;
	static float[][] one_over_r_kernel;
	static float[][] one_over_r2_kernel;
	
	int width = 0;
	int height = 0;
	int resolution = 0;
	int depth = 8;
	//int max_sight_range = 50;
	//int convole_kernel_size = 30;
	static int kernel_size = 4;
	public float density_multiplier = 1;
	public float draw_multiplier = 1f;
	float min_dist = 2;

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
	
	public void add(float scale, float fx, float fy) {
		fx /= resolution;
		fy /= resolution;
		for(int d = 0; d < depth; d++) {
			int x = Math.round(fx);
			int y = Math.round(fy);
			data[d][y][x] += scale;
			fx /= 2.0;
			fy /= 2.0;
		}
	}
	
	public float getScalar(float[][] kernel, float fx, float fy) {
		fx /= resolution;
		fy /= resolution;
		float[] column = new float[depth];
		for(int d = 0; d < depth; d++) {
			int x = Math.round(fx);
			int y = Math.round(fy);
			column[d] = data[d][y][x];
			/*
			int cy = y-kern/el_size < 0 ? 0 : y-kernel_size;
			int ky = cy-y+kernel_size;
			for( ; cy < y+kernel_size && cy < data.length; cy++) {
				int cx = x-kernel_size < 0 ? 0 : x-kernel_size;
				int kx = cx-x+kernel_size;
				for( ; cx < x+kernel_size && cx < data[0].length; cx++) {
					data[d][cy][cx] += 0;
					kx++;
				}
				ky++;
			}
			*/
			fx /= 2.0; 
			fy /= 2.0;
		}
		for(int d = depth-1; d > 0; d--)
			column[d] -= column[d-1];
		float ret = 0;
		for(int d = 0; d < depth; d++)
			ret += depth_scale(column[d],d);
		return ret;
	}
	public float depth_scale(float f, float d) {  //d/scale = 1/r = *0.5, 1/r^2 = *0.25
		float div = 1f;
		for( int i = 0; i < d; i++)
			div *= 0.5f;
		return f*div;
	}
	
	
	public PetRock2(int width, int height, int resolution, int depth) {
		this.depth = depth;
		this.width = width;
		this.height = height;
		this.resolution = resolution;
		min_dist = resolution/4;
		data = new float[depth][][];
		for( int j = 0; j < depth; j++) {
			data[j] = new float[1+height/resolution][];
			for( int i = 0; i < data.length; i++)
				data[j][i] = new float[1+width/resolution];
			resolution *= 2;
		}
		if( one_over_r_kernel == null)
			one_over_r_kernel = create_1_over_r_kernel();
		if( one_over_r2_kernel == null)
			one_over_r2_kernel = create_1_over_r2_kernel();
	}
	
	//add interpolation
	public float[] interpolate( float[][] kernel, int x, int y) {
		float rx = (float)x/(float)resolution;
		float ry = (float)y/(float)resolution;
		float x1 = (float)Math.floor(rx);
		float y1 = (float)Math.floor(ry);
		float dx = rx-(float)x1;
		float dy = ry-(float)y1;
		x1*=resolution;
		y1*=resolution;
		dx*=resolution;
		dy*=resolution;
		return bilinear_interpolation(kernel,x1,y1,dx,dy,false); //scalar, gradx, grady
	}
	
	float[] bilinear_interpolation(float[][] kernel, float xl, float yl, float dx, float dy, boolean invert) {
		//scalar,gradx,grady
		if( yl+2 > data[0].length)
			yl = data[0].length-2;
		if( xl+2 > data[0][0].length)
			xl = data[0][0].length-2;
		if( xl < 0) xl = 0;
		if( yl < 0) yl = 0;
		float x0y0 = getScalar(kernel,(float)yl,(float)xl);//grid[yl][xl];
		float x1y0 = getScalar(kernel,(float)yl,(float)xl+resolution);
		float x0y1 = getScalar(kernel,(float)yl+resolution,(float)xl);
		float x1y1 = getScalar(kernel,(float)yl+resolution,(float)xl+resolution);
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
		this.add( amt*density_multiplier, x, y);
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
}