package World;

import Entities.*;

import java.util.*;
import java.util.concurrent.*;
import Genetics.*;

import java.awt.*;
import javax.swing.*;

//dead organisms turn into food!
//armor absorbs as much as melee of same size puts out, but increases movement cost the same as well

//need to draw to image then translate and resize it. (get viewable area)
//wormholes for larger universe to keep matrix small? (have to reset on transfer)
//or a 3x3 matrix of matrix set, that gets, shifted/reset on movement over a tile?  (world lists are thus tiled)
//resouce and pherome maps pre-calculated on a grid-base system (w/gradient), thus memory-based rather than calculation based?
//(changes then cause recalc of local area)
//this would not allow for a limited viewing angle.
/*
 * make pheromes beacon, give ability to pick one up (becomes material)
 * make impassables in grid.  these also have a squared distance effect, recorded on grid.
 * grid scalar and gradient is done by interpolating neighboring grid points.
 * thus food can only be on grid points?  or grid marked specially if has food in it.
 */

/*
 * for spatial combat, equalize time to engagement. - using linear cones from units.
 * wager so as to prefer engaging healthy friendlies and unhealthy enemies
 * 
 */
import Genetics.Behavior.*;
import Player.*;

public class World implements iTemporal {
	public static World world;
	Vector<Player> players;
	public static final int num_players = 5;
	public static final int num_organisms = 50;
	public static final int num_breeding_attempts = 20;
	public static final int num_plant_generators = num_organisms;
	public static final float max_debt = -100;
	public static final float max_breeding_distance = 500f;
	static int deaths = 0;
	static float selectivity_multipier = 0.4f;//0.35f;
	static float mutation_rate_multiplier = (float)(1.0/32.0); 
	public float width = 500;
	public float height = 500;
	public static float ema_rate = 0.25f/(float)num_organisms;
	public static float ema_time_between_death_rate = 0.5f/num_organisms;
	public static float time_between_deaths = 70;
	//public static long game_time = 0;
	
	

	public Vector<Organism> organisms = new Vector<Organism>();
	public Vector<Plant> plant_generators = new Vector<Plant>();
	public Vector<Resource>[] resources = new Vector[Resource.NUM_TYPES];
	public Vector<Beacon>[] beacons = new Vector[Beacon.NUM_TYPES];
	public Queue<Organism> organism_queue = new ConcurrentLinkedQueue<Organism>(); // recycle
																					// these
	public Queue<Resource>[] resource_queue;
	public Queue<Beacon>[] beacon_queue;
	public WorldView view = new WorldView();
	public Climate climate = null;// new WorldGrid();
	public static OutputVector average_biases;
	public static OutputVector average_scales;
	public static Body average_bodies;
	public static InputVector average_inputs;
	public static InputVector average_squared_inputs;

	public World() {
		world = this;
		players = new Vector<Player>();
		for( int i = 0; i < num_players; i++) {
			Player p = new Player();
			p.id = i;
			players.add(p); 
		}
		
		
		average_bodies = new Body();
		average_biases = new OutputVector();
		average_scales = new OutputVector();
		average_inputs = new InputVector();
		average_squared_inputs = new InputVector();
		for( int i = 0; i < average_bodies.scalars.size(); i++)
			average_bodies.scalars.get(i).value = 0.5f;
		resource_queue = new ConcurrentLinkedQueue[Resource.NUM_TYPES]; // recycle
																		// these
		for (int i = 0; i < resource_queue.length; i++)
			resource_queue[i] = new ConcurrentLinkedQueue<Resource>(); // recycle
																		// these
		beacon_queue = new ConcurrentLinkedQueue[Beacon.NUM_TYPES]; // recycle
																	// these
		for (int i = 0; i < beacon_queue.length; i++)
			beacon_queue[i] = new ConcurrentLinkedQueue<Beacon>(); // recycle
																	// these
	}
	public void do_time(float dt) {
		climate.do_time(dt);
		//Plant.do_time_all(dt);
		move_organisms(dt);
		//update_distances();
		//Iterator<Organism> it = organisms.iterator();
		//randomize order that organisms are processed
		int[] order = new int[organisms.size()];
		for( int i = 0; i < order.length; i++)
			order[i] = i;
		int temp;
		for( int i = 0; i < order.length; i++) {
			int t2 = Time.rand.nextInt(order.length);
			temp = order[t2];
			order[t2] = order[i];
			order[i] = temp;
		}
		for( int i = 0; i < order.length; i++) {//while (it.hasNext()) {
			Organism o = organisms.get(order[i]);//it.next();
			if (o.state == o.ACTIVE)
				o.do_time(dt);
		}
		//it = organisms.iterator();
		for( int i = 0; i < organisms.size(); i++) {//while (it.hasNext()) {
			Organism o = organisms.get(i);
			if( o.state != Organism.ACTIVE)
				continue;
			if (o.energy <= 0 || o.health <= 0)
				organism_died(o);
		}
		// do pherome decay
		// do organism behavior selection
		// do organism behavior
	}

	public void organism_died(Organism o) {
		try {
			Player owner = o.owner;
			if( owner != null) {
				owner.organisms.remove(o);
			if( o.owner != null && o.state != o.INACTIVE)
				o.owner.num_organisms--;
			}
		deaths++;
		float tbd = (float) (Time.last_tick-Time.last_death)*Time.time_scale*(float)num_organisms/1000f;
		Time.last_death = Time.last_tick;
		time_between_deaths += (tbd - time_between_deaths)*this.ema_time_between_death_rate;
		o.radius *= 2;
		o.state = o.INACTIVE;
		float r2 = o.radius * o.radius;
		float meat = o.getResourceValue(1);
		for (float f = meat; f > 0;) {
			Resource r = null;
			try {
				r = this.resource_queue[1].poll();
			} catch (Exception ex) { }
			if( r == null) {
				r = new Resource();
				r.type = 1;
				r.color = Resource.resource_colors[1];
				resources[1].add(r);
				placeResource(r);
			}
			r.born = Time.game_time;
			//int index = (int) (Math.random() * (float) resources[0].size());
			//Resource r = resources[0].get(index);
			//float m = r.mass;
			r.mass = r.initial_mass*2;
			r.state = Resource.ACTIVE;
			//grid.removeResource(r);
			//r.mass = m;
			if( o.x < 10) o.x = 10;
			if( o.y < 10) o.y = 10;
			if( o.x > width-10) o.x = width-10;
			if( o.y > height-10) o.y = height-10;

			float dx = (float) Math.random() * o.radius * 2.0f - o.radius;
			float dy = (float) Math.random() * o.radius * 2.0f - o.radius;
			while (dx * dx + dy * dy > r2 || o.x+dx < 10 || o.y+dy < 10 || o.x+dx > width-10 || o.y+dy > height-10) {
				dx = (float) Math.random() * o.radius * 2.0f - o.radius;
				dy = (float) Math.random() * o.radius * 2.0f - o.radius;
			}
			r.x = o.x + dx;
			r.y = o.y + dy;
			f -= r.mass;
			climate.addResource(r);
		}
		for( int i = 0; i < Resource.NUM_TYPES; i++) {
			if( i == 1)
				continue;
			float mat = o.getResourceValue(i);
			if(true) {
				for (float f = mat; f > 0;) {
					Resource r;
					if( i == 2) {
						int index = (int) (Math.random() * (float) resources[i].size());
						r = resources[i].get(index);
						//float m = r.mass;
						//r.mass = r.initial_mass;
						if( r.state == Resource.ACTIVE)
							climate.removeResource(r);
						r.state = Resource.ACTIVE;
						r.mass = r.initial_mass;
					} else {
						r = Plant.makeNew();
						r.mass = r.initial_mass;
					}
					
					float dx = (float) Math.random() * o.radius * 2.0f - o.radius;
					float dy = (float) Math.random() * o.radius * 2.0f - o.radius;
					while (dx * dx + dy * dy > r2) {
						dx = (float) Math.random() * o.radius * 2.0f - o.radius;
						dy = (float) Math.random() * o.radius * 2.0f - o.radius;
					}
					r.x = o.x + dx;
					r.y = o.y + dy;
					if (r.mass > f)
						r.mass = f;
					f -= r.mass;
					climate.addResource(r);
				}
			}
		}
		organism_queue.add(o);
		new_organism(o,owner);
		/*
		 * placeResource(o); o.pregnancy = 0; o.resource_levels[1] = 0;
		 * o.resource_levels[0] = 20; o.health = 20; o.radius = 20;
		 */
		} catch (Exception ex) {
			System.out.println("ex in organism died: "+ex);
			ex.printStackTrace();
		}
	}

	public class WorldView extends JPanel {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Rectangle rect = super.getBounds();

			g.setColor(Color.white);
			g.fillRect(0, 0, (int) width, (int) height);
			climate.draw(g);

			for (int i = 0; i < beacons.length; i++) {
				Iterator<Beacon> pi = beacons[i].iterator();
				while (pi.hasNext()) {
					Beacon r = pi.next();
					if (r.state != r.ACTIVE)
						continue;
					r.draw(g);
				}
			}

			for (int i = 0; i < resources.length; i++) {
				//Iterator<Resource> ri = resources[i].iterator();
				for( int j = 0; j < resources[i].size(); j++) {
					Resource r = resources[i].get(j);
					if (r.state != r.ACTIVE)
						continue;
					r.draw(g);
				}
			}
			
			Iterator<Organism> oi = organisms.iterator();
			while (oi.hasNext()) {
				Organism r = oi.next();
				if (r.state != r.ACTIVE)
					continue;
				r.draw(g);
			}
			g.setColor(Color.black);
			g.drawString("Average lifespan: "+time_between_deaths, 10, 20);
			g.drawString("Generation: "+((float)deaths/(float)num_organisms), 10, 40);
			//g.drawText("",0,0,0,0);
			//time_between_deaths
			if( players != null  ) {
				int y = 60;
				Iterator<Player> ip = players.iterator();
				while( ip.hasNext()) {
					Player p = ip.next();
					if( p != null) {
						p.draw(g,10,y);
						y+=20;
					}
				}
			}
		}
	}

	public void placeResource(Entity resource) {
		resource.x = (float) (Math.random() * (width-20)+10);
		resource.y = (float) (Math.random() * (height-20)+10);
		resource.state = Resource.ACTIVE;
		resource.radius = 2;
		resource.mass = 4;
	}

	public float getMatchPct(int[] mDNA, int[] fDNA) {
		int bitcount = 0;
		for (int i = 0; i < mDNA.length; i++)
			bitcount += Trait.numberOfSetBits(mDNA[i] ^ fDNA[i]);
		bitcount = mDNA.length * 32 - bitcount;
		return (float) bitcount / (float) (mDNA.length * 32);
	}


	public void initialize(float w, float h, int[] resource_levels) {
		System.out.println("initializing world...");

		width = w;
		height = h;
		climate = new Climate((int)w, (int)h,10);
		resources = new Vector[Resource.NUM_TYPES];

		// place resource randomly.
		System.out.println("adding resources...");
		Plant.init();
		resources[0] = Plant.all;
		for (int i = 1; i < Resource.NUM_TYPES; i++) {
			if( i == 0)
				;
			else
				resources[i] = new Vector<Resource>();
			for (int j = 0; j < resource_levels[i]; j++) {
				System.out.print(".");
				Resource r = new Resource();
				r.type = i;
				r.state = r.ACTIVE;
				r.color = Resource.resource_colors[i];
				placeResource(r);
				climate.addResource(r);
				resources[i].add(r);
				// placeResource(r);
				// resources[i].add(r);
			}
		}
		System.out.println("adding beacons...");
		beacons = new Vector[Beacon.NUM_TYPES];
		for (int i = 0; i < Beacon.NUM_TYPES; i++) {
			beacons[i] = new Vector<Beacon>();
			for (int j = 0; j < 0; j++) {
				Beacon r = new Beacon();
				r.type = i;
				r.state = r.ACTIVE;
				r.color = Beacon.beacon_colors[i];
				placeResource(r);
				//grid.addBeacon(r);
				beacons[i].add(r);
				// placeResource(r);
				// pheromes.add(r);
			}
		}
		System.out.println("adding organisms...");
		for (int i = 0; i < num_organisms; i++) {
			Player pp = players.get(i % players.size()); 
			//if( pp != null)
				//pp.num_organisms++;
			Organism o = new_organism(null,pp);
		}
		System.out.println("world initalized.");
	}

	public Organism new_organism(Organism source, Player owner) {
		//if( source != null) //disable prengnacy
			//return;
		try {
			Organism o = null;
			o = organism_queue.poll();
			if (o == null) {
				o = new Organism(this);
				organisms.add(o);
			}
			/*
			if( source == null) {
				o.genome.fromDNA(o.genome.newDNA(o.genome.scalars.size()),0);
			} else {
				o.genome.fromDNA(source.genome.toDNA(),0);
			}*/
			//int[] mDNA;
			//int[] fDNA;
			Organism mate1 = null;
			Organism mate2 = null;
			if( organisms.size() > num_organisms*(1.0/2.0)) {
				int genome_bits = organisms.get(0).genome.toDNA().length*32;
				int index = (int)(Math.random()*(float)organisms.size());
				int tries = 0;
				for( tries = 0; tries < num_breeding_attempts; tries++) {
					index = (int)(Math.random()*(float)organisms.size());
					mate1 = organisms.get(index);
					if( source != null && source.owner != null && mate1.owner != source.owner)
						continue;
					while( mate1.state != Organism.ACTIVE
							|| mate1.genome == null || mate1.genome.toDNA() == null) {
						index = (int)(Math.random()*(float)organisms.size());
						mate1 = organisms.get(index);
					}
					mate2 = selectMate(mate1,selectivity_multipier+0.05f*(float)tries);
					if( mate2 != null)
						break;
				}
				//System.out.println("tries: "+tries); 
				if( mate2 != null) {
					o.genome.fromDNA(o.genome.offspringDNA(mate1.genome.toDNA(), mate2.genome.toDNA(), (int)(o.body.mutation_rate.value*(float)genome_bits*mutation_rate_multiplier)),0);
					o.owner = Math.random() > 0.5 ? mate1.owner : mate2.owner;
				} else {
					o.genome.fromDNA(o.genome.newDNA(o.genome.scalars.size()),0);
					if( owner != null)
						o.owner = owner;
				}
			} else
				o.genome.fromDNA(o.genome.newDNA(o.genome.scalars.size()),0);
				
			//o.genome.fromDNA(o.genome.offspringDNA(o.genome.toDNA(), organisms.get(index).genome.toDNA(), 16),0);
			if( owner != null)
				o.owner = owner;
			
			o.reset();
			if( mate1 != null && mate2 != null) {
				float tot = mate1.energy+mate2.energy;
				o.energy = tot/3; 
				if( o.energy > o.max_energy)
					o.energy = o.max_energy;
				float m1 = mate1.energy / tot; 
				mate1.energy-=m1*o.energy;
				mate2.energy-=(1-m1)*o.energy;
				for( int i = 0; i < Resource.NUM_TYPES; i++) {
					o.resource_levels[i] = (mate1.resource_levels[i]+mate2.resource_levels[i])/3; 
					mate1.resource_levels[i]*=(2.0/3.0);
					mate2.resource_levels[i]*=(2.0/3.0);
				}
			} else {
				o.energy = o.max_energy/2; 
			}
			if( o.energy < 5) {
				if( 5 > o.max_energy/2 )
					o.energy = o.max_energy/2;
				else
					o.energy = 5;
			}
			//o.energy = o.max_energy/2; 
			
			o.radius=o.body.getBaseSize();
			/*
			o.max_health = 20.0f*o.body.adult_size.value*o.body.adult_size.value;
			o.max_energy = 20.0f*o.body.adult_size.value*o.body.adult_size.value;
			o.health = 20.0f*o.body.adult_size.value*o.body.adult_size.value;
			o.energy = 20.0f*o.body.adult_size.value*o.body.adult_size.value;
			*/
			//o.body.storage.value = 20.0f*o.body.adult_size.value*o.body.adult_size.value;
			o.state = o.ACTIVE;
			//o.color = Entity.randomColor();
			placeResource(o);
			if( source != null && false) {
				o.x = source.x;
				o.y = source.y;
			}
			if( mate1 != null && mate2 != null) {
				o.x = (float)((mate1.x));//+mate2.x)/2.0);
				o.y = (float)((mate1.y));//+mate2.y)/2.0);
			}
			World.average_bodies.scale(1-World.ema_rate);
			World.average_bodies.addScaled(o.body,World.ema_rate);
			
			if( o.owner != null) {
				o.owner.num_organisms++;
				if( o.owner.organisms == null)
					organisms = new Vector<Organism>();
				o.owner.organisms.add(o);
			}
			return o;
		} catch (Exception ex) {
			System.out.println("ex in new organism "+ex);
			ex.printStackTrace();
		}
		return null;
	}
	
	VisibleTraits vt = new VisibleTraits();
	public Organism selectMate(Organism thiso, float selectivity_multipier) {
		VisibleTraits[] alignment_selector = thiso.getAlignmentArray(thiso.behavior.outputs.attack_or_breed_target,thiso.behavior.outputs.attack_or_breed_target_alignment_modifier,1);
		int genome_bits = thiso.genome.toDNA().length*32;
		int threshold = (int)(((float)genome_bits)*selectivity_multipier*thiso.genome.body.genetic_selectivity.value);
		//System.out.println("threshold: "+(selectivity_multipier*thiso.genome.body.genetic_selectivity.value));

		Iterator<Organism> io = organisms.iterator(); 
		float fx = thiso.x + (float)Time.rand.nextGaussian()*Organism.location_blur_multiplier*thiso.body.location_blur.value; 
		float fy = thiso.y + (float)Time.rand.nextGaussian()*Organism.location_blur_multiplier*thiso.body.location_blur.value;
		float best = -1000000.0f;
		Organism obest = null;
		while( io.hasNext()) {
			Organism o = io.next();
			if( o.state != Organism.ACTIVE || o == thiso) {
				//System.out.print("X");
				continue;
			}
			if( o.genome == null || o.genome.toDNA() == null) {
				System.out.print("!");
				continue;
			}
			int alignment = (int)Player.getAlignment(o,thiso);
			if( alignment < 0)
				continue;
			
			float adx = fx-o.x;
			float ady = fy-o.y;
			float dd = (float)Math.sqrt(adx*adx+ady*ady);
			if( dd == 0 || dd > max_breeding_distance) {
				//System.out.print("o");
				continue;
			}
			float dist_mult = (float)1.0f/dd;
			//adx *= dist_mult; 
			//ady *= dist_mult;
			float ddx = (o.dx-thiso.dx);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
			float ddy = (o.dy-thiso.dy);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
			float bdx = fx-o.x-ddx;
			float bdy = fy-o.y-ddy;
			float ddb = (float)Math.sqrt(bdx*bdx+bdy*bdy);
			
			vt.reset();
			vt.size.value = 1.0f;
			vt.change_in_distance.value = (float)ddb-dd;
			//vt.relative_velocity.value = (float)Math.sqrt(ddx*ddx+ddy*ddy);
			vt.scale(dist_mult);
			vt.multiply_inner(alignment_selector[alignment]);
			float m = vt.total()+(float)(Time.rand.nextGaussian()*0.001);
			if( m > best) {
				if( AnimalGenome.findHammingDistance(thiso.genome.toDNA(), o.genome.toDNA()) > threshold) {
					//System.out.print(".");
					continue;
				}
				//System.out.print("*");
				best = m;
				obest = o;
			} else 
				;//System.out.print("_");
		}
		return obest;
	}
	public Organism getTarget(Organism thiso, VisibleTraits[] targetSelectors, float max_range, float alignment) {
		Iterator<Organism> io = organisms.iterator(); 
		float fx = thiso.x + (float)Time.rand.nextGaussian()*Organism.location_blur_multiplier*thiso.body.location_blur.value; 
		float fy = thiso.y + (float)Time.rand.nextGaussian()*Organism.location_blur_multiplier*thiso.body.location_blur.value;
		float best = -1000000.0f;
		Organism obest = null;
		while( io.hasNext()) {
			Organism o = io.next();
			if( o.state != Organism.ACTIVE || o == thiso) {
				//System.out.print("X");
				continue;
			}
			float al = Player.getAlignment(thiso,o);
			if( alignment*al < 0)
				continue;
			float adx = fx-o.x;
			float ady = fy-o.y;
			float dd = (float)Math.sqrt(adx*adx+ady*ady);
			if( dd == 0 || dd > max_range) {
				//System.out.print("o");
				continue;
			}
			float dist_mult = (float)1.0f/dd;
			//adx *= dist_mult; 
			//ady *= dist_mult;
			float ddx = (o.dx-thiso.dx);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
			float ddy = (o.dy-thiso.dy);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
			float bdx = fx-o.x-ddx;
			float bdy = fy-o.y-ddy;
			float ddb = (float)Math.sqrt(bdx*bdx+bdy*bdy);
			
			vt.reset();
			vt.size.value = 1.0f;
			vt.change_in_distance.value = (float)ddb-dd;
			//vt.relative_velocity.value = (float)Math.sqrt(ddx*ddx+ddy*ddy);
			vt.scale(dist_mult);
			vt.multiply_inner(targetSelectors[(int)al+1]);
			float m = (vt.total()+(float)(Time.rand.nextGaussian()*0.0001));
			if( m > best) {
				best = m;
				obest = o;
			}
		}
		return obest;
	}
	

	public void move_organisms(float dt) {
		Iterator<Organism> it = organisms.iterator();
		while (it.hasNext()) {
			Organism o = it.next();
			if (o.state != o.ACTIVE)
				continue;
			o.x += o.dx * dt;
			o.y += o.dy * dt;
			if (o.x < 0)
				o.x = 0;
			if (o.x > width)
				o.x = width;
			if (o.y < 0)
				o.y = 0;
			if (o.y > height)
				o.y = height;
			/*
			o.visibleTraits.dx.value = o.dx;
			o.visibleTraits.dy.value = o.dy;
			o.visibleTraits.x.value = o.x;
			o.visibleTraits.y.value = o.y;
			*/
		}
	}

}
