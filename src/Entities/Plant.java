package Entities;

import java.awt.Graphics;
import java.util.*;

import Genetics.*;
import World.*;

//add eat plant age threshold to behavior output for gathering.
public class Plant extends Resource {
	//need to density on different grid, with constant fertility update.
	//for that need to construct landscapes

	public static Queue<Resource> reuse_queue;
	public static Vector<Resource> all;
	public static final float mass_update_frequency = 0.25f/2.0f;

	public static final float fertility_requirement = 0.55f;//0.3f;
	//public static final float max_density = 80f/20f;
	public static final float spore_radius = 60f;
	public static final long germination_period = (long)(100f*1.5f*1000f*0.35f);
	public static final long germinations_per_life = 10;
	public int num_germinations = 0;
	public long age = 0;
	public long germination_time = 0;
	public float last_relative_age_update = 0;
	public static Vector<Plant> plant_generators;
	//public static Queue<Plant> eventQueue;
	public boolean is_generator = false;
	public long last_germination_time = 0;
	public long time_to_schedule = 0;
	
	public PlantGenome genome;
	
	public static void init() {
		reuse_queue = Time.world.resource_queue[0]; 
		//reuse_queue = new ConcurrentLinkedQueue<Resource>(); // recycle
		all = new Vector<Resource>();
		plant_generators = new Vector<Plant>();
		for( int i = 0; i < World.num_plant_generators; i++) {
			Plant pl = new Plant();
			Time.world.placeResource(pl);
			pl.state = Plant.INACTIVE;
			pl.is_generator = true;
			plant_generators.add(pl);
			pl.schedule(pl.time_to_schedule);
		}
	}
	public Plant() {
		super();
		id = id_enumerator++;
		genome = new PlantGenome();
		reset();
	}
	public void reset() {
		super.reset();
		is_generator = false;
		type = 0;
		color = resource_colors[0];
		age = 0;
		last_germination_time = Time.game_time;
		last_relative_age_update = 0;
		num_germinations = 0;
		mass = 0;//initial_mass;
		germination_time = (long)(Math.random()*(float)germination_period*2.0f)+10;
		//last_execution_time = Time.game_time;
		time_to_schedule = Time.game_time+(long)germination_time;
		radius = 4;
		state = ACTIVE;
		//if( Time.eventQueue != null)
			//Time.eventQueue.add(this);
	}
	public static void retire(Plant p) {
		p.unschedule();
		p.state = INACTIVE;
		reuse_queue.add(p);
	}
	public static Plant makeNew() {
		Plant p = null;
		try {
			p = (Plant)reuse_queue.poll();
		} catch (Exception ex) { }
		if( p == null) {
			p = new Plant();
			all.add(p);
		} else
			p.reset();
		return p;
	}
	/*
	public static void do_time_all(float dt) {
		Iterator<Plant> ipg = plant_generators.iterator();
		while( ipg.hasNext()) {
			Plant pg = (Plant)ipg.next();
			pg.do_time(dt);
		}
		//return;
		for( int i = 0; i < all.size(); i++) {
			Plant p = (Plant)all.get(i);
			if( p.state == ACTIVE)
				p.do_time(dt);
		}
	}*/
	public float getRelativeAge() {
		return (float)((float)(Time.game_time-born)/((float)germinations_per_life*(float)germination_period));
	}
	public void do_time(float dt) {
		try {
			if( state != ACTIVE && !is_generator)
				return;
			
			//last_relative_age_update = (long)((float)age/((float)germinations_per_life*(float)germination_period));
			age = Time.game_time-born;//+= dt;
			float relative_age = getRelativeAge();
			
			//if( relative_age - last_relative_age_update > mass_update_frequency && last_relative_age_update < 0.5) {
			float old_mass_mutliplier = last_relative_age_update > 0.5 ? 1 : last_relative_age_update*2;
			float new_mass_mutliplier = relative_age > 0.5 ? 1 : relative_age*2;
			float old_mass = mass;
			if( old_mass_mutliplier == 0 || new_mass_mutliplier < old_mass_mutliplier)
				mass = initial_mass*new_mass_mutliplier;
			else
				mass *= new_mass_mutliplier / old_mass_mutliplier;
			last_relative_age_update = relative_age;
			Time.world.climate.soil.updateMass(this);
			if( !is_generator)
				;//System.out.println("id: "+id+" age: "+relative_age+" m-oldm:"+(mass-old_mass));
				//world.grid.addResource(this);
				//then remove mass multiplier from organism eating.
			//}
				
			//germination_time -= dt;
			if(Time.game_time - last_germination_time >= germination_time) {
				//System.out.println("sporing");
				last_germination_time = Time.game_time;//germination_time;
				num_germinations++;
				if( this.num_germinations > germinations_per_life && state == ACTIVE && !is_generator) {
					Time.world.climate.soil.removeResource(this);
					retire(this);
					//System.out.println("plant died:"+id);
					return;
				}
				
				//System.out.println("spore");
				germination_time = (long)(Math.random()*(float)germination_period*2.0f)+10L;
				time_to_schedule = last_germination_time+(long)germination_time;
				float nx = x+(float)Time.rand.nextGaussian()*spore_radius;
				float ny = y+(float)Time.rand.nextGaussian()*spore_radius;
				boolean ok = true;
				if( nx < 10 || ny < 10 || nx > Time.world.width-10 || ny > Time.world.height-10)
					ok = false;
				if( ok && Time.world.climate.soil.can_plant((int)nx,(int)ny,fertility_requirement)) {
					//System.out.println("ok, make new now");
					Plant p = makeNew();
					p.reset();
					p.x = nx;
					p.y = ny;
					p.mass = Plant.initial_mass;
					//Plant.world = world;
					p.state = ACTIVE;
					Time.world.climate.soil.addResource(p);
					//System.out.println("new plant: from:"+id+" new:"+p.id+"  t:"+p.next_execution_time+" gt:"+Time.game_time);//+" germ:"+p.germination_time);
					try {
						p.schedule(p.time_to_schedule);
					} catch (Exception ex) {
						System.out.println("ex "+ex);
						ex.printStackTrace();
					}
				}
				//world.grid.fertility.update(p,-p.fertility_requirement);
			}
			schedule(time_to_schedule);
		} catch (Exception ex) {
			System.out.println("ex7 "+ex);
			ex.printStackTrace();
		}
	}
	public void draw(Graphics g) {
		//if( state != ACTIVE)
			//return;
		float radius = 2;
		float rel_age = getRelativeAge()*2;
		if( rel_age>1) rel_age = 1;
		int height = (int)(rel_age*16.0);
		int shadow = (int)((float)height/6.0);
		g.setColor(color);
		g.drawLine((int)(x), (int)(y-0), (int)(x+radius+shadow), (int)(y-0));
		//g.drawLine((int)(x), (int)(y-9), (int)(x-radius*2), (int)(y-9));
		g.fill3DRect((int)(x-radius), (int)(y-height), (int)(radius+radius), (int)height, true);
		//g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
	}
}
