package World;

import java.util.Iterator;
import java.util.Vector;

import Entities.*;

public class Soil extends Landscape {
	public Landscape aquifer;
	public Soil(int width, int height, int resolution) {
		super(width,height,resolution);
		/*
		Landscape scratch = new Landscape(width,height,resolution);
		scratch.gaussian();
		aquifer = scratch.convolve(0.25f);
		*/
		aquifer = new Landscape(width,height,resolution);
		aquifer.make_from_convolved_noise(create_convolve_kernel(0.25f));
		float m = (aquifer.max()-aquifer.min())/2;
		aquifer.scale(1f/m);
		System.out.println("fmax: "+m);
		aquifer.exp(2);
		m = aquifer.max();
		aquifer.scale(1f/m);
		aquifer.backup();
	}
	public boolean can_plant(int x,int y,float req) {
		return aquifer.interpolate(x,y)[0] >= req;
	}
	public boolean can_plant(Plant p) {
		return aquifer.interpolate((int)p.x,(int)p.y)[0] >= p.fertility_requirement;
	}
	public void addResource(Entity r) {
		Plant p = (Plant)r;
		if( !can_plant(p)) {
			p.state = Plant.INACTIVE;
			Plant.retire(p);
			return;
		}
		super.addResource(r);
		aquifer.update(p,-p.fertility_requirement);
	}
	public void removeResource(Entity r) {
		Plant p = (Plant)r;
		super.removeResource(r);
		aquifer.update(p,p.fertility_requirement);
	}
	public Vector<Entity> findResources( float fx, float fy, float radius) {
		return super.findResources(fx,fy,radius);
	}
	
	
	public void refresh() {
		aquifer.refresh();
		if( bins == null)
			make_bins();
		set(0);
		for(int i = 0; i < bins.length; i++)
			for(int j = 0; j < bins[0].length; j++) {
				Iterator<Entity> ie = bins[i][j].iterator();
				while( ie.hasNext()) {
					Plant e = (Plant)ie.next();
					if( e.state == Entity.ACTIVE) {
						//e.mass_at_last_update = 0;
						update(e,e.mass);
						aquifer.update(e,-e.fertility_requirement);
						e.mass_at_last_update = e.mass;
					}
				}
			}
	}
}
