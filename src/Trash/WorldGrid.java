package Trash;

import java.util.*;
import Entities.*;
import java.awt.*;
/*
 * this class reduces the computational time it takes to find inanimate object distances and directions.
 */
/*
 * alter: subtract the ones in the bin from the grid points before interpolation, then add them to position of asker
 * i.e. ones in bins get exact gradients calculated.
 * 
 * take multiplicative inverse of spatial input gradients ans scalars.
 */

public class WorldGrid {
	public static float grid_period = 8.0f;
	public static float grid_density = 1.0f/grid_period;
	//public float[][] rsquared_distance_template;
	public float[][][] resource_scalar_grid;
	public float[][][] resource2_scalar_grid;
	public Vector<Entity>[][][] resource_grid;
	public float[][][] beacon_scalar_grid;
	public Vector<Entity>[][][] beacon_grid;
	public int max_sight_range = 10;
	public static final float density_multiplier = 20.0f;
	
	public WorldGrid(float w, float h) {
		System.out.println("initializing grid...");
		int gridw = (int)(w*grid_density+1);
		int gridh = (int)(h*grid_density+1);
		/*
		rsquared_distance_template = new float[gridh][];
		resource_scalar_grid = new float[Resource.NUM_TYPES][][];
		resource_grid = new Vector[Resource.NUM_TYPES][][];
		for( int y = 0; y < gridh; y++) {
			rsquared_distance_template[y] = new float[gridw];
			for( int x = 0; x < gridw; x++)
				rsquared_distance_template[y][x] = 0;
		}
		*/
		resource_scalar_grid = new float[Resource.NUM_TYPES][][];
		resource2_scalar_grid = new float[Resource.NUM_TYPES][][];
		resource_grid = new Vector[Resource.NUM_TYPES][][];
		for( int i = 0; i < Resource.NUM_TYPES; i++) {
			resource_scalar_grid[i] = new float[gridh][];
			resource2_scalar_grid[i] = new float[gridh][];
			resource_grid[i] = new Vector[gridh][];
			for( int y = 0; y < gridh; y++) {
				resource_scalar_grid[i][y] = new float[gridw];
				resource2_scalar_grid[i][y] = new float[gridw];
				resource_grid[i][y] = new Vector[gridw];
				for( int x = 0; x < gridw; x++) {
					resource_scalar_grid[i][y][x] = 0.00001f;
					resource2_scalar_grid[i][y][x] = 0.00001f;
					resource_grid[i][y][x] = new Vector<Entity>();
				}
			}
		}
		beacon_scalar_grid = new float[Beacon.NUM_TYPES][][];
		beacon_grid = new Vector[Beacon.NUM_TYPES][][];
		for( int i = 0; i < Beacon.NUM_TYPES; i++) {
			beacon_scalar_grid[i] = new float[gridh][];
			beacon_grid[i] = new Vector[gridh][];
			for( int y = 0; y < gridh; y++) {
				beacon_scalar_grid[i][y] = new float[gridw];
				beacon_grid[i][y] = new Vector[gridw];
				for( int x = 0; x < gridw; x++) {
					beacon_scalar_grid[i][y][x] = 0.00001f;
					beacon_grid[i][y][x] = new Vector<Entity>();
				}
			}
		}
		System.out.println("grid initialized.");
	}

	public void addResource(Resource r) {
		add(r,resource_scalar_grid,resource2_scalar_grid,resource_grid);
	}
	public void addBeacon(Beacon r) {
		add(r,beacon_scalar_grid,beacon_scalar_grid,beacon_grid);
	}
	public void removeResource(Resource r) {
		remove(r,resource_scalar_grid,resource2_scalar_grid,resource_grid);
	}
	public void removeBeacon(Beacon r) {
		remove(r,beacon_scalar_grid,beacon_scalar_grid,beacon_grid);
	}
	public void add(Entity r, float[][][] resource_scalar_grid, float[][][] resource2_scalar_grid, Vector<Entity>[][][] resource_grid) {
		try {
		float xi = r.x*grid_density;
		float yi = r.y*grid_density;
		float x = Math.round(xi);
		float y = Math.round(yi);
		int h = resource_scalar_grid[0].length-1;
		int w = resource_scalar_grid[0][0].length-1;
		if( x < 0) x = 0;
		if( y < 0) y = 0;
		if( x > w) x = w;
		if( y > h) y = h;
		
		//add to resource grid bin
		resource_grid[r.type][(int)y][(int)x].add(r);

		//now update scalar grid
		float startx = x-max_sight_range; if( startx < 0) startx = 0;
		float starty = y-max_sight_range; if( starty < 0) starty = 0;
		int endx = (int)x+max_sight_range; if( endx > w) endx = w;
		int endy = (int)y+max_sight_range; if( endy > h) endy = h;
		float grid_mult = grid_period*grid_period;
		float dy = starty-yi;
		//System.out.println("x:"+startx+" "+endx+" y:"+starty+" "+endy);
		for( int iy = (int)starty; iy < endy; iy++ ) {
			float dx = startx-xi;
			for( int ix = (int)startx; ix < endx; ix++ ) {
				float dist = (float)Math.sqrt(dx*dx+dy*dy)*grid_period;
				float f = r.distance_function(dist);
				resource_scalar_grid[r.type][iy][ix] += r.mass*density_multiplier/dist;
				resource2_scalar_grid[r.type][iy][ix] += f*r.mass;
				dx++;
			}
			dy++;
		}
	} catch (Exception ex) {
		System.out.println("ex on remove: "+ex);
		ex.printStackTrace();
	}
	}
	public void remove(Entity r, float[][][] resource_scalar_grid, float[][][] resource2_scalar_grid, Vector<Entity>[][][] resource_grid) {
		try {
		float xi = r.x*grid_density;
		float yi = r.y*grid_density;
		float x = Math.round(xi);
		float y = Math.round(yi);
		int h = resource_scalar_grid[0].length-1;
		int w = resource_scalar_grid[0][0].length-1;
		if( x < 0) x = 0;
		if( y < 0) y = 0;
		if( x > w) x = w;
		if( y > h) y = h;
		
		//remove from resource grid bin
		resource_grid[r.type][(int)y][(int)x].remove(r);

		//now update scalar grid
		float startx = x-max_sight_range; if( startx < 0) startx = 0;
		float starty = y-max_sight_range; if( starty < 0) starty = 0;
		int endx = (int)x+max_sight_range; if( endx > w) endx = w;
		int endy = (int)y+max_sight_range; if( endy > h) endy = h;
		//float grid_mult = grid_period*grid_period;
		float dy = starty-yi;
		for( int iy = (int)starty; iy < endy; iy++ ) {
			float dx = startx-xi;
			for( int ix = (int)startx; ix < endx; ix++ ) {
				float dist = (float)Math.sqrt(dx*dx+dy*dy)*grid_period;
				float f = r.distance_function(dist);
				resource_scalar_grid[r.type][iy][ix] -= r.mass*density_multiplier/dist;
				resource2_scalar_grid[r.type][iy][ix] -= f*r.mass;
				dx++;
			}
			dy++;
		}
		} catch (Exception ex) {
			System.out.println("ex on remove: "+ex);
			ex.printStackTrace();
		}
	}
	
	/*
	public void updateResource(Resource r, float dmass) {
		float xi = r.x*grid_density;
		float yi = r.y*grid_density;
		float x = Math.round(xi);
		float y = Math.round(yi);
		int h = resource_scalar_grid[0].length-1;
		int w = resource_scalar_grid[0][0].length-1;
		if( x < 0) x = 0;
		if( y < 0) y = 0;
		if( x > w) x = w;
		if( y > h) y = h;
		
		//now update scalar grid
		float startx = x-max_sight_range; if( startx < 0) startx = 0;
		float starty = y-max_sight_range; if( starty < 0) starty = 0;
		int endx = (int)x+max_sight_range; if( endx > w) endx = w;
		int endy = (int)y+max_sight_range; if( endy > h) endy = h;
		float grid_mult = grid_period*grid_period;
		float dy = starty-yi;
		for( int iy = (int)starty; iy < endy; y++ ) {
			float dx = startx-xi;
			for( int ix = (int)startx; ix < endx; x++ ) {
				resource_scalar_grid[r.type][iy][ix] += r.distance_function((dx*dx+dy*dy)*grid_mult)*dmass;
				dx++;
			}
			dy++;
		}
	}*/

	public Vector<Resource>[] findResources( float x, float y, float radius) {
		float xi = x*grid_density;
		float yi = y*grid_density;
		int x_low = (int)Math.floor(xi);
		int y_low = (int)Math.floor(yi);
		int h = resource_scalar_grid[0].length-2;
		int w = resource_scalar_grid[0][0].length-2;
		if( x_low < 0) x_low = 0;
		if( y_low < 0) y_low = 0;
		if( x_low > w) x_low = w;
		if( y_low > h) y_low = h;
		
		float r2 = radius*radius;
		Vector<Resource>[] ret = new Vector[resource_grid.length];
		for( int i = 0; i < ret.length; i++) {
			ret[i] = new Vector<Resource>();
			Iterator<Resource>[] its = new Iterator[]{
					resource_grid[i][y_low][x_low].iterator(),
					resource_grid[i][y_low][x_low+1].iterator(),
					resource_grid[i][y_low+1][x_low].iterator(),
					resource_grid[i][y_low+1][x_low+1].iterator()
					};
			for( int j = 0; j < its.length; j++) {
				while( its[j].hasNext()) {
					Resource r = its[j].next();
					float dx = r.x-x;
					float dy = r.y-y;
					if( dx*dx+dy*dy < r2)
						ret[i].add(r);
				}
			}
		}
		return ret;
	}

	/*
	public void createSquaredDistanceTemplate(int radius) {
		int tot = radius*2+1;
		float dist_mult = grid_density*grid_density;
		rsquared_distance_template = new float[tot][];
		for( int i = 0; i < tot; i++)
			rsquared_distance_template[i] = new float[tot];
		float fy = -radius;
		for( int y = 0; y < tot; y++) {
			float fx = -radius;
			if( fx == 0 && fy == 0)
				continue;
			for( int x = 0; x < tot; x++) {
				rsquared_distance_template[y+radius][x+radius] = dist_mult/(fx*fx+fy*fy);
				fx++;
			}
			fy++;
		}
		rsquared_distance_template[radius][radius] = 1000;
	}*/
	
	public float[][][] getResourceScalarsAndGrads(float x, float y) {
		float xi = x*grid_density;
		float yi = y*grid_density;
		float fx_low = (float)Math.floor(xi);
		float fy_low = (float)Math.floor(yi);
		float dx = xi-fx_low;
		float dy = yi-fy_low;
		int x_low = (int)fx_low;
		int y_low = (int)fy_low;
		float[][] ret = new float[resource_grid.length][];
		for( int i = 0; i < resource_grid.length; i++)
			ret[i] = bilinear_interpolation(resource_scalar_grid[i],x_low,y_low,dx,dy,false);  //density
		float[][] ret2 = new float[resource_grid.length][];
		for( int i = 0; i < resource_grid.length; i++)
			ret2[i] = bilinear_interpolation(resource2_scalar_grid[i],x_low,y_low,dx,dy,true); //scarcity
		return new float[][][]{ret,ret2};
	}
	
	public float[][] getBeaconScalarsAndGrads(float x, float y) {
		float xi = x*grid_density;
		float yi = y*grid_density;
		float fx_low = (float)Math.floor(xi);
		float fy_low = (float)Math.floor(yi);
		float dx = xi-fx_low;
		float dy = yi-fy_low;
		int x_low = (int)fx_low;
		int y_low = (int)fy_low;
		float[][] ret = new float[beacon_grid.length][];
		for( int i = 0; i < beacon_grid.length; i++)
			ret[i] = bilinear_interpolation(beacon_scalar_grid[i],x_low,y_low,dx,dy,false);
		return ret;
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
	public void draw(Graphics g) {
		if( true)
			return;
		g.setColor(Color.red);
		for( int y = 0; y < resource2_scalar_grid[0].length; y++) {
			for( int x = 0; x < resource2_scalar_grid[0][y].length; x++) {
				float f = 1.0f/resource2_scalar_grid[0][y][x];
				f = (float)Math.sqrt(f);
				g.drawRect(x*10-(int)(f/2), y*10-(int)(f/2), (int)f, (int)f);
			}
		}
		g.setColor(Color.green);
		for( int y = 0; y < resource2_scalar_grid[0].length; y++) {
			for( int x = 0; x < resource2_scalar_grid[0][y].length; x++) {
				float f = 1.0f*resource_scalar_grid[0][y][x];
				f = (float)Math.sqrt(f);
				g.drawRect(x*10-(int)(f/2), y*10-(int)(f/2), (int)f, (int)f);
			}
		}
		
	}
}
