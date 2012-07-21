package Entities;

import java.awt.*;
import java.util.*;

import Genetics.*;
import Genetics.Behavior.*;
import Player.Player;
import World.*;
/*
 * make born at half size, and grow the rest
 */


/*
 * add metabolism rate - food is stored in stomach (amd weighted) until metabolized, cant eat while stomach full
 * also, energy is differnt hten health.
 * energy+mat can be used to repair health.
 * 
 * 
 * allow grabbing and moving organisms
 * and making them attack or eat
 * 
 * spatial actions: move, breed, attack, repair, xfer
 * binary actions: gather food, gather material, drop material, drop beacon, gather beacon, repair,
 * move, breed, attack, repair other, xfer
 * 
 * 
 * 
 */

//ad inertia to genome

public class Organism extends Entity implements iTemporal {
	
	//game balancing / tuning constants
	//public static final boolean organism_vision = true;
	public double amt_armor = 0f;
	public double last_attacked = 0f;
	public double repair_delay = 10*1920f;
	
	public static final double max_growth = 1.01f;
	public static final double grow_thresh1 = 0.9f;
	public static final double grow_thresh2 = 0.8f;
	public static final double grow_rate_multiplier = (1.0/1920.0)*200000000.1f;
	public static final double growth_energy_multiple=0.05f;
	public static final double armor_repair_rate = 0.000001f; 
	public static final double armor_repair_threshold = 0.75f; 
	
	public static final float max_attack_range = 50;
	public static final float max_sight_range = 500;
	public static final float max_sight_range_squared = max_sight_range*max_sight_range;
	public static final boolean organism_vision = true;
	public static final boolean force_gather = false;
	public boolean attacking = false;
	public boolean being_attacked = true;
	public Organism attack_target = null;
	public static final float max_trade_range = 50f;
	public static final float organism_attention = 0.25f;
	public static final float organism_direction_affect = 0.25f;
	public static final float location_blur_multiplier = 4.0f;
	public float dx = 0;
	public float dy = 0;
	public float fx = 0;
	public float fy = 0;
	public static final int num_spatial_samples = 1;
	public float[] to_drop;
	
	public World world;
	//public PsuedoVector rsqrd_distances_from_organisms = new PsuedoVector(); 
	//public PsuedoVector rsqrd_distances_from_pheromes = new PsuedoVector(); 
	//public PsuedoVector[] rsqrd_distances_from_resources = new PsuedoVector[Resource.NUM_TYPES];
	public float[] resource_levels = new float[Resource.NUM_TYPES]; //food, material
	public float max_health = 20;
	public float max_energy = 20;
	public float health = 20;
	public float energy = 20;
	public float[] prev_resource_levels = new float[Resource.NUM_TYPES]; //food, material
	public float prev_health = 20;
	public float prev_energy = 20;
	public float prev_armor = 20;
	public float pregnancy = 0;
	public float cash = 0;
	//public float attack_state = 0;
	//public float breed_state = 0;
	public boolean pregnant = false;
	public AnimalGenome genome;
	
	public InputVector inputs;
	public OutputVector outputs;
	public Body body;
	public Behavior behavior;
	public VisibleTraits visibleTraits;
	//add dx, dy bias outputs. (signed)
	//add 2 noise inputs (gaussian)
	//add gaussian position fuzz before distance lookup.
	
	public Organism() {
		super();
		try {
		
		//System.out.println("organism constructor 1 "+this);
		genome = new AnimalGenome();
		//System.out.println("organism constructor 2 "+this);
		//System.out.println("genome: "+genome);
		
		body = genome.body;
		behavior = genome.behavior;
		inputs = genome.behavior.inputs;
		outputs = genome.behavior.outputs;
		
		//create visible trait vector
		visibleTraits = new VisibleTraits();
		visibleTraits.gender = body.gender;
		visibleTraits.melee_weap = body.melee_weap;
		visibleTraits.armor = body.armor;
		
		visibleTraits.size = body.current_size;
		visibleTraits.color = body.color;
		visibleTraits.signal = behavior.outputs.signal;
		visibleTraits.carnivorous = body.carnivorous;
		visibleTraits.temperature_preference = body.temperature_preference;
		visibleTraits.humidity_preference = body.humidity_preference;
		
		//need to refresh trait pointers now.
		visibleTraits.collectTraits();
		visibleTraits.collectMultiLinear();
		} catch(Exception ex) {
			System.out.println("ex in organism constr: "+ex);
			ex.printStackTrace();
		}
		to_drop = new float[Resource.NUM_TYPES];
		reset();
		//visibleTraits.resource_levels = re; //<--currently not visible.  should only be to friendlies
	}
	//Color last_signal = new Color(0,0,0);
	//boolean eating = false;\
	public void reset() {
		super.reset();
		body.postMakeLogic();
		amt_armor = body.getBaseArmor();
		prev_armor = (float)amt_armor;
		//body.gender.value = -1;//(float)(Math.round(Math.random())*2.0f-1.0f);
		cash = 0;
		max_health = body.getBaseHealth();
		max_energy = body.getBasEnergy();
		health = max_health;
		energy = max_energy;
		prev_health = max_health;
		prev_energy = max_energy;
		pregnancy = 0;
		pregnant = true;
		for( int i = 0; i < resource_levels.length; i++) {
			resource_levels[i] = 0;
			prev_resource_levels[i] = 0;
			to_drop[i] = 0;
		}
		behavior.reset();
		//body.storage.value = 20;
		outputs.breed_state.value = body.gender.value;
		//outputs.gather_resource_state[0].value = 1.0f;
		//outputs.gather_resource_state[1].value = 1.0f;
		outputs.consume_food.value = 1.0f;
		body.getBaseMass();
		state = ACTIVE;
	}
	/*
	public float getBaseMass() {
		base_mass = initial_mass 
		+ body.melee_weap.value*10.0f
		+ body.armor.value*20.0f;
		base_mass = base_mass*body.current_size.value*body.current_size.value;
		return base_mass;
	}*/
	
	public float getResourceValue(int i) {
		if( i != 1)
			return resource_levels[i];
		if( i == 1) {
			double floor = max_energy < 10 ? max_energy*0.5 : 5;
			if( floor < 0) floor = 0;
			float val = (float)(
					0 
					+body.getBaseNutrient()
					+pregnancy
					+resource_levels[1]
					+(energy < floor ? 0 : (energy - floor)*0.9)
					)
			;
			val = (float)(Math.floor(val*4f)*0.25f);
			return val < 0.25 ? 0 : val;
		}
		return 0;
	}
	public float getTotalMass() {
		float f = body.getBaseMass()+pregnancy;
		for( int i = 0; i <  Resource.NUM_TYPES; i++)
			f+=resource_levels[i];
		return f;
	}
	VisibleTraits vt = new VisibleTraits();
	public void construct_input_vector() {
		inputs.reset();
		//inputs.spatialVisual.friendly.reset();
		//inputs.spatialVisual.enemy.reset();
		for(int i = 0; i < Behavior.NUM_NOISE_INPUTS; i++)
			inputs.noise[i].value = (float)Time.rand.nextGaussian();
		inputs.carnivorous.value = body.carnivorous.value;
		if( body.armor.value == 0)
			inputs.armor.value = 0;
		else
			inputs.armor.value = (float)amt_armor/body.getBaseArmor();
		inputs.energy.value = (float)energy/max_energy;
		inputs.health.value = (float)health/max_health;
		inputs.melee_weap.value = body.melee_weap.value;

		inputs.health.value = health;
		inputs.energy.value = energy;
		inputs.delta_health.value = (health - prev_health)/max_health;
		inputs.delta_energy.value = (energy - prev_energy)/max_energy;
		if( body.getBaseArmor() == 0)
			inputs.delta_armor.value = 0;
		else
			inputs.delta_armor.value = (float)(amt_armor - prev_armor)/body.getBaseArmor();
		prev_health = health;
		prev_energy = energy;
		prev_armor = (float)amt_armor;

		
		//SpatialVisualMix
		for(int l = 0; l < 1/*num_spatial_samples*body.num_spatial_samples.value*/; l++) {
			fx = x ;//+ (float)Genome.rand.nextGaussian()*location_blur_multiplier*body.location_blur.value; 
			fy = y;// + (float)Genome.rand.nextGaussian()*location_blur_multiplier*body.location_blur.value; 
			if( organism_vision && Behavior.brainOn) {
				Iterator<Organism>  io = world.organisms.iterator();
				while( io.hasNext()) {
					Organism o = io.next();
					if( o.state != Organism.ACTIVE || o == this)
						continue;
					float adx = fx-o.x;
					float ady = fy-o.y;
					float dd = (float)Math.sqrt(adx*adx+ady*ady);
					if( dd > max_sight_range)
						continue;
					vt.reset();
					vt.add(o.visibleTraits);
					float dist_mult = (float)(1.0/dd);
					adx += dx-o.dx;
					ady += dy-o.dy;
					float dd2 = (float)Math.sqrt(adx*adx+ady*ady);
					vt.change_in_distance.value = dd2-dd;
					/*
					vt.x.value -= fx;
					vt.y.value -= fy;
					vt.dx.value -= dx;
					vt.dy.value -= dy;
					float dxn = (float)Math.sqrt(vt.x.value*vt.x.value+vt.y.value*vt.y.value);
					float dist_mult = (float)1.0f/dxn;
					vt.x.value += vt.dx.value;
					vt.y.value += vt.dy.value;
					dxn = (float)Math.sqrt(vt.x.value*vt.x.value+vt.y.value*vt.y.value)-dxn;
					vt.x.value = move_rate_affect_scale*dxn; //approaching or moving away from
					vt.dx.value = move_rate_affect_scale*(float)Math.sqrt(vt.dx.value*vt.dx.value+vt.dy.value*vt.dy.value); //velocity differnce
					vt.y.value = 0;
					vt.dy.value = 0;
					*/
					dist_mult *= organism_attention;
					inputs.spatialVisual.organisms.addScaled(vt,dist_mult);
					inputs.spatialVisual.alignment_modifier.addScaled(vt,dist_mult*Player.getAlignment(this, o));
				}
				//inputs.spatialVisual.enemy.recipricol();
				//inputs.spatialVisual.friendly.recipricol();
			}
			//for(int i = 0; i < Resource.NUM_TYPES; i++)
				//inputs.spatialVisual.enemy.resource_levels[i].value = 0; //can't see this
			float[][] resourceStuff = 
				new float[][]{
					world.climate.soil.interpolate((int)fx,(int)fy),
					world.climate.meat.interpolate((int)fx,(int)fy),
					world.climate.meds.interpolate((int)fx,(int)fy)
			};
			//float[][] beaconStuff = world.grid.getBeaconScalarsAndGrads(fx,fy);
			inputs.spatialVisual.resource_density.value = 0;
			inputs.spatialVisual.food_density.value = 0;
			inputs.spatialVisual.meds_density.value = 0;
			for(int i = 0; i < Resource.NUM_TYPES; i++) {
				inputs.spatialVisual.resource_density.value += resourceStuff[i][0];
			}
			inputs.spatialVisual.food_density.value += resourceStuff[0][0]*(1.0-body.carnivorous.value);
			inputs.spatialVisual.food_density.value += resourceStuff[1][0]*body.carnivorous.value;
			inputs.spatialVisual.meds_density.value += resourceStuff[2][0];
			//for(int i = 0; i < Beacon.NUM_TYPES; i++)
				//inputs.spatialVisual.friendly_beacons[i].value = beaconStuff[i][0];
		}

		//others
		inputs.gender.value = body.gender.value;
		inputs.pregnancy.value = pregnancy;
		if( body.melee_weap.value == 0)
			outputs.attack_state.value = 0;
		inputs.attack_state.value = outputs.attack_state.value;
		inputs.breed_state.value = outputs.breed_state.value;
		for( int i = 0; i < Behavior.NUM_INTERNAL_STATES; i++)
			inputs.internal_states[i].value = outputs.internal_states[i].value;
		for( int i = 0; i < Resource.NUM_TYPES; i++)
			inputs.internal_resource_levels[i].value = resource_levels[i];
		for( int i = 0; i < Resource.NUM_TYPES; i++)
			inputs.delta_internal_resource_levels[i].value = resource_levels[i] - prev_resource_levels[i];
		for( int i = 0; i < Resource.NUM_TYPES; i++)
			prev_resource_levels[i] = resource_levels[i];
		//System.out.println("inputs======");
		//inputs.printout(70);
	}
	
	VisibleTraits vt2 = new VisibleTraits();
	VisibleTraits vt3 = new VisibleTraits();
	public void get_direction_from_output() {
		float prev_dx = dx;
		float prev_dy = dy;
		dx = 0;//outputs.dx_bias.value;
		dy = 0;//outputs.dy_bias.value;
		VisibleTraits[] alignment_selector = getAlignmentArray(outputs.move_target.organisms,outputs.move_target.alignment_modifier,1f);

		for(int l = 0; l < num_spatial_samples*body.num_spatial_samples.value; l++) {
			fx = x + (float)Time.rand.nextGaussian()*location_blur_multiplier*body.location_blur.value; 
			fy = y + (float)Time.rand.nextGaussian()*location_blur_multiplier*body.location_blur.value; 
			float[][] resourceStuff = 
				new float[][]{
					world.climate.soil.interpolate((int)fx,(int)fy),
					world.climate.meat.interpolate((int)fx,(int)fy),
					world.climate.meds.interpolate((int)fx,(int)fy)
			};
			//float[][] beaconStuff = world.grid.getBeaconScalarsAndGrads(fx,fy);

			for(int i = 0; i < Resource.NUM_TYPES; i++) {
				dx += (float)(outputs.move_target.resource_density.value*resourceStuff[i][1]);
				dy += (float)(outputs.move_target.resource_density.value*resourceStuff[i][2]);
			}
			dx += (float)((1-body.carnivorous.value)*outputs.move_target.food_density.value*resourceStuff[0][1]);
			dy += (float)((1-body.carnivorous.value)*outputs.move_target.food_density.value*resourceStuff[0][2]);
			dx += (float)((body.carnivorous.value)*outputs.move_target.food_density.value*resourceStuff[1][1]);
			dy += (float)((body.carnivorous.value)*outputs.move_target.food_density.value*resourceStuff[1][2]);
			dx += (float)(outputs.move_target.meds_density.value*resourceStuff[2][1]);
			dy += (float)(outputs.move_target.meds_density.value*resourceStuff[2][2]);
				//inputs.spatialVisual.resources[i].value = resourceStuff[i][0];
			
			float[] signal_mimic = new float[Behavior.NUM_SIGNALS];
			for( int i = 0; i < signal_mimic.length; i++)
				signal_mimic[i] = 0;
			if( organism_vision) {
				Iterator<Organism> io = world.organisms.iterator();
				float mdx = 0; float mdy = 0;
				while( io.hasNext()) {
					Organism o = io.next();
					if( o.state != Organism.ACTIVE || o == this)
						continue;
					float adx = fx-o.x;
					float ady = fy-o.y;
					float dd = (float)Math.sqrt(adx*adx+ady*ady);
					if( dd == 0)
						continue;
					if( dd > max_sight_range)
						continue;
					float dist_mult = (float)1.0f/(float)dd;
					adx *= dist_mult*organism_direction_affect; 
					ady *= dist_mult*organism_direction_affect;
					dist_mult *= organism_attention;
					float ddx = (o.dx-dx);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
					float ddy = (o.dy-dy);//*dist_mult*outputs.move_target.friendly.relative_velocity.value;
					float bdx = fx-o.x-ddx;
					float bdy = fy-o.y-ddy;
					float ddb = (float)Math.sqrt(bdx*bdx+bdy*bdy);
					
					
					
					vt.reset();
					vt.size.value = 1.0f;
					vt.change_in_distance.value = ddb-dd;
					//vt.relative_velocity.value = (float)Math.sqrt(ddx*ddx+ddy*ddy);
					vt.scale(dist_mult);
					vt2.reset();
					vt2.add(vt);
					//vt.add(o.visibleTraits);
					//vt.visibleTraits.color[0].value = 0;
					/*vt
					vt.x.value -= x;
					vt.y.value -= y;
					vt.dx.value -= dx;
					vt.dy.value -= dy;
					float dxn = (float)Math.sqrt(vt.x.value*vt.x.value+vt.y.value*vt.y.value);
					vt.x.value += vt.dx.value;
					vt.y.value += vt.dy.value;
					dxn = (float)Math.sqrt(vt.x.value*vt.x.value+vt.y.value*vt.y.value)-dxn;
					vt.x.value = dxn; //approaching or moving away from
					vt.dx.value = (float)Math.sqrt(vt.dx.value*vt.dx.value+vt.dy.value*vt.dy.value); //velocity differnce
					vt.y.value = 0;
					vt.dy.value = 0;
					*/
					
					//vt.dx.value = (float)Math.sqrt(vt.dx.value*vt.dx.value +vt.dy.value *vt.dy.value );
					//vt3.reset();
					//vt3.add(outputs.move_target.organisms);
					//vt3.addScaled(outputs.move_target.alignment_modifier, Player.getAlignment(this,o));
					vt.multiply_inner(alignment_selector[(int)Player.getAlignment(this,o)+1]);
					vt2.multiply_inner(outputs.mimic_target);
					//ddx *= dist_mult*outputs.mimic_target.relative_velocity.value;
					//ddy *= dist_mult*outputs.mimic_target.relative_velocity.value;

					
					
					if( adx != adx || ady != ady)
						continue;
					float m = vt.total();
					if( m == m) {
						mdx += adx * m;
						mdy += ady * m;
					}
					m = vt2.total();
					if( m == m) {
						mdx += ddx * m * outputs.mimic_velocity.value;
						mdy += ddy * m * outputs.mimic_velocity.value;
						for( int i = 0; i < signal_mimic.length; i++)
							signal_mimic[i] += m*(o.behavior.outputs.signal[i].value - behavior.outputs.signal[i].value)*outputs.mimic_signal[i].value;
					}
				}
				dx += mdx;
				dy += mdy;
				for( int i = 0; i < signal_mimic.length; i++)
					;//behavior.outputs.signal[i].value += signal_mimic[i];
			}
		}

		
		float rnorm = (float)(Math.sqrt(dx*dx+dy*dy));
		while( rnorm == 0) {
			dx = (float)(Math.random()-0.5);
			dy = (float)(Math.random()-0.5);
			rnorm = (float)(Math.sqrt(dx*dx+dy*dy));
		}
		rnorm = 1.0f/rnorm;
		
		dx *= rnorm;
		dy *= rnorm;

		//outputs.move_state.value = outputs.move_state.value == 0 ? (float)Math.random()*2f-1f : outputs.move_state.value;
		outputs.move_state.value = outputs.move_state.value < -1 ? -1 : outputs.move_state.value > 1 ? 1 : outputs.move_state.value;
		//outputs.move_state.value = outputs.move_state.value < 0 ? -1 : outputs.move_state.value > 0 ? 1 : outputs.move_state.value;
		//outputs.move_state.value = 1;
		/*
		if( outputs.move_state.value < 0 && false) {
			dx = 0;
			dy = 0;
		} else {*/
		float ds = body.legs.value*body.legs.effect_multiplier*outputs.move_state.value/this.getTotalMass();
			dx *= ds;				
			dy *= ds;				
		//}
		if( dx != dx)
			dx = 0;
		if( dy != dy)
			dy = 0;
		dx = body.inertia.value*prev_dx+(1-body.inertia.value)*dx;
		dy = body.inertia.value*prev_dy+(1-body.inertia.value)*dy;
	}
	
	VisibleTraits vt4 = new VisibleTraits();
	VisibleTraits vt5 = new VisibleTraits();
	VisibleTraits vt6 = new VisibleTraits();
	VisibleTraits[] vt456 = new VisibleTraits[]{vt4,vt5,vt6};
	
	public VisibleTraits[] getAlignmentArray(VisibleTraits base, VisibleTraits modifier, float multiplier) {
		vt5.reset();
		vt5.addScaled(outputs.attack_or_breed_target, multiplier);
		vt4.reset();
		vt4.add(vt5);
		vt4.addScaled(outputs.attack_or_breed_target_alignment_modifier, -1*multiplier);
		vt6.reset();
		vt6.add(vt5);
		vt6.addScaled(outputs.attack_or_breed_target_alignment_modifier, multiplier);
		return vt456;
		
	}
	public void do_time(float dt) {
		if( max_energy < 0)
			max_energy = 0;
		if( max_health < 0)
			max_health = 0;
		if( max_energy == 0)
			max_energy = 0.00001f;
		if( max_health == 0)
			max_health = 0.00001f;
		if( health > max_health)
			health = max_health;
		if( energy > max_energy)
			energy = max_energy;
		try {
			//System.out.print(".");
			if( x != x || y != y || health <= 0 || health != health) {
				world.organism_died(this);
				return;
				//state = DEAD;
			}
			float total_mass = getTotalMass();
			energy -= (float)Math.sqrt(dx*dx+dy*dy)*dt*total_mass*body.legs.active_energy*(body.movement_efficiency.value);
			float work = 0;
			for( int i = 0; i < body.massive_traits.length; i++) {
				work += body.massive_traits[i].value*body.massive_traits[i].inactive_energy;
			}
			work *= (body.stasis_efficiency.value);
			energy -= work;
			visibleTraits.energy.value = energy/max_energy;
			visibleTraits.health.value = health/max_health;
			if( cash < 0)
				visibleTraits.cash.value = 0;
			else
				visibleTraits.cash.value = (float)Math.log(cash);

			construct_input_vector();
			inputs.printout(0);
			genome.behavior.do_time(dt); //outputs are signed!
			outputs.printout(0);
			get_direction_from_output();
			//System.out.print("0");
			
			/*
			this.outputs.consume_resources.value = 1.0f;//+= 1.0f; //always eat
			this.outputs.consume_food.value = 0.0f;//+= 1.0f; //always eat
			this.outputs.consume_meds.value = 0.0f;//+= 1.0f; //always eat
			*/
//		this.outputs.consume_food.value += (max_energy-energy)/20f;
			//System.out.println("outputs======");
			//outputs.printout(7);
			//reset();
			if( body.melee_weap.value == 0)
				outputs.attack_state.value = 0;

			if( outputs.attack_state.value > 0 && energy > body.melee_weap.value*body.melee_weap.active_energy) {
				Organism target = world.getTarget(this,getAlignmentArray(outputs.attack_or_breed_target,outputs.attack_or_breed_target_alignment_modifier,-1),max_attack_range,-1f);
				if( target != null) {
					attacking = true;
					attack_target = target;
					target.being_attacked = true;
					float hit = body.melee_weap.value*body.melee_weap.effect_multiplier;
					energy -= body.melee_weap.value*body.melee_weap.active_energy;//(hit + target.body.armor.value*target.body.current_size.value)*num_hits*energy_cost;
					if( hit > 0) {
						target.last_attacked = Time.game_time;
						//float hit = (float)(body.melee_weap.value*hit_multiplier*Math.random());
						//if( hit > target.body.armor.value)
						target.amt_armor -= hit;
						if( target.amt_armor < 0) {
							target.health += target.amt_armor;
							target.amt_armor = 0;
						}
						if(target.health <= 0) {
							//energy -= target.health;//hit + target.body.armor.value;
							world.organism_died(target);
						}
						//if(energy <= 0) {
							//world.organism_died(this);
						//}
					}
				}
			}
			
			//radius = 6.0f;
			float resource_reach = radius+10.0f*(float)Math.sqrt(body.arms.value)+4.0f;//8.0f;//radius+1.0f;
			//System.out.print("1");
			
			//gather resources
			float fullness = 0;
			for( int i = 0; i < Resource.NUM_TYPES; i++)
				fullness += resource_levels[i];
			energy -= body.stomach.active_energy*fullness;
			float amt_to_gather = body.arms.value*body.arms.effect_multiplier*dt;
			if(genome == null)
				System.out.println("genome is null");
			if(body == null)
				System.out.println("body is null");
			if(body.stomach == null)
				System.out.println("body.storage is null");
			if( amt_to_gather > body.stomach.value*body.stomach.effect_multiplier - fullness)
				amt_to_gather = body.stomach.value*body.stomach.effect_multiplier - fullness;
			if( amt_to_gather <= 0)
				;//System.out.print("f");
			float initial_gather_amt = 0;
			//Vector<Resource>[] gatherable_resources = world.grid.findResources(x,y,resource_reach);
			//System.out.println("AT:"+outputs.plant_age_threshold.value);
			for( int i = 0; i < Resource.NUM_TYPES; i++) {
				float val = outputs.gather_resource_state.value;
				if( i == 0) val += (1-body.carnivorous.value)*outputs.gather_food_state.value; 
				if( i == 1) val += (body.carnivorous.value)*outputs.gather_food_state.value; 
				if( i == 2) val += outputs.gather_meds_state.value; 
				if( force_gather) val = 1;
				if( val >= 0.0f && amt_to_gather > 0) {
					//System.out.print((i+5));
					Vector<Entity> vresources =
						i == 0 ? world.climate.soil.findResources(x,y,resource_reach) :
						i == 1 ? world.climate.meat.findResources(x,y,resource_reach) :
						i == 2 ? world.climate.meds.findResources(x,y,resource_reach) :
						null;
					Iterator<Entity> ri = vresources.iterator();
					while( ri.hasNext()) {
						Resource r = (Resource)ri.next();
						/*float mult = 1;
						
						if( i == 0) {
							mult = ((Plant)r).relative_age;
							if( mult < outputs.plant_age_threshold.value)
								continue;
							mult *= 2.0;
							if( mult > 1) mult = 1;
						}*/
						float diff = amt_to_gather;
						if( diff >= r.mass)
							diff = r.mass;
						resource_levels[i] += diff;//*mult;
						amt_to_gather -= diff;//*mult;
						r.mass -= diff;
						if( r.mass <= 0) {
							/*
							if( r.type == 1)
								r.mass = Resource.initial_mass*2;
							else
								r.mass = Resource.initial_mass;
								*/
							world.climate.removeResource(r);
							if( r.type == 2) {
								world.placeResource(r);
								world.climate.addResource(r);
							}
							else if( r.type == 0) {
								Plant.retire((Plant)r);
									//world.placeResource(r);
									//world.grid.addResource(r);
							} else {
								r.state = INACTIVE;
								world.resource_queue[r.type].add(r);
							}
						}
						if( amt_to_gather <= 0)
							break;
					}
				}
			}
			float amt_gathered = initial_gather_amt-amt_to_gather;
			if(amt_gathered < 0) amt_gathered = 0;
			energy -= amt_gathered*body.arms.active_energy;
			//System.out.print("2");
			
			float amt_to_consume = body.intestine.value*body.intestine.effect_multiplier*dt;
			if( amt_to_consume < 0)
				amt_to_consume *= -1;
			float init_to_consume = amt_to_consume;
			for( int k = 0; k < 3 && amt_to_consume > 0; k++) {
				float[] to_consume = new float[Resource.NUM_TYPES];
				for( int i = 0; i < to_consume.length; i++)
					to_consume[i] = outputs.consume_resources.value;
				to_consume[0] += outputs.consume_food.value*(1.0-body.carnivorous.value);
				to_consume[1] += outputs.consume_food.value*(body.carnivorous.value);
				to_consume[2] += outputs.consume_meds.value;
				if( health >= max_health*0.9)
					to_consume[2] = 0;
				for( int i = 0; i < to_consume.length; i++) {
					to_consume[i] = ((to_consume[i] <= 0 || resource_levels[i] <= 0) ? 0 : to_consume[i]);
					if( i == 2 && health >= max_health ) to_consume[i] = 0;
					if( i == 2 && energy <= 0 ) to_consume[i] = 0;
					if( (i==0 || i ==1) && energy >= max_energy ) to_consume[i] = 0;
					if( to_consume[i] != to_consume[i])
						to_consume[i] = 0;
					if( (i==0 || i ==1) && energy <= max_energy ) {
						if( to_consume[i] < max_energy-energy)
							to_consume[i] = max_energy-energy;
					}
				}
				if( health >= max_health*0.9)
					to_consume[2] = 0;
				float total = 0;
				for( int j = 0; j < to_consume.length; j++)
					total += to_consume[j];
				if( total == 0)
					break;
				
				for( int i = 0; i < to_consume.length; i++) {
					if( to_consume[i] <= 0)
						continue;
					to_consume[i]*=amt_to_consume;
					
					if( to_consume[i] > resource_levels[i])
						to_consume[i] = resource_levels[i];
					if( (i==0) || (i==1)) {
						float ed = to_consume[i];
						ed *= i == 0 ? (1-body.carnivorous.value) : (body.carnivorous.value);
						if( ed > max_energy - energy) {
							ed = max_energy - energy;
							ed /= i == 0 ? (1-body.carnivorous.value) : (body.carnivorous.value);
							if( ed != ed) ed = 0;
							to_consume[i] = ed;
						}
						if( to_consume[i] < 0)
							to_consume[i] = 0;
						energy += to_consume[i] * (i == 0 ? (1-body.carnivorous.value) : (body.carnivorous.value));
					} else if( i == 2) {
						if( health >= max_health*0.9)
							to_consume[2] = 0;
						if( to_consume[i] > energy)
							to_consume[i] = energy;
						if( to_consume[i] > max_health-health)
							to_consume[i] = max_health-health;
						if( to_consume[i] < 0)
							to_consume[i] = 0;
						energy -= to_consume[i];
						health += to_consume[i];
					}
					resource_levels[i] -= to_consume[i];
					amt_to_consume -= to_consume[i]; 
					if( amt_to_consume <= 0)
						break;
					
					total = 0;
					for( int j = i+1; j < to_consume.length; j++)
						total += to_consume[j];
					if( total == 0)
						break;
					for( int j = i+1; j < to_consume.length; j++)
						to_consume[j] /= total;
				}
			}
			float amt_consumed = init_to_consume-amt_to_consume;
			if(amt_consumed < 0) amt_consumed = 0;
			energy -= amt_consumed*body.intestine.active_energy;
			//System.out.print("3");
			/*
			if( outputs.consume_food.value >= 0.0f) {
				if( max_energy - energy < amt_to_consume)
					amt_to_consume = max_energy - energy;
				
				for( int i = 0; i < resource_levels.length; i++) {
					float eat_now = amt_to_consume;
					if( resource_levels[i] < amt_to_consume)
						eat_now = resource_levels[i];
					
					resource_levels[i] -= eat_now;
					if( i == 0)
						energy += eat_now*(1-body.carnivorous.value);
					if( i == 1)
						energy += eat_now*(body.carnivorous.value);
					amt_to_consume -= eat_now;
				}
			}
			if( outputs.consume_meds.value >= 0.0f && health < max_health && resource_levels[2] > 0) {
				float amt_to_eat = body.repair_rate.value*repair_rate_multiplier*dt;
				if( max_health-health < amt_to_eat)
					amt_to_eat = max_health-health;
				if( energy < amt_to_eat)
					amt_to_eat = energy;
				if( energy < resource_levels[2])
					amt_to_eat = resource_levels[2];
				resource_levels[2] -= amt_to_eat;
				energy -= amt_to_eat;
				health += amt_to_eat;
			}
			*/
			
			if( pregnant && body.gender.value < 0.0f && false) {
				if( pregnancy < 0) pregnancy = 0;
				if( pregnancy > 20) pregnancy = 20;
				float amt_to_gestate = /*body.gestation_rate.value**/dt;
				if( amt_to_gestate + pregnancy > 20.0f)
					amt_to_gestate = 20.0f - pregnancy;
				if( amt_to_gestate > energy)
					amt_to_gestate = energy;
				if( amt_to_gestate > resource_levels[1])
					amt_to_gestate = resource_levels[1];
				pregnancy += amt_to_gestate;
				resource_levels[1] -= amt_to_gestate;
				energy -= amt_to_gestate;
				if( pregnancy == 20.0f) {
					//world.new_organism(this);
					pregnancy = 0.0f;
				}
			}
			while( energy <= 0) {
				//float diff = gather_rate_multiplier;//*body.gather_rate.value;
				//if( eat_rate_multiplier/*body.eat_rate.value*/ < diff)
					//diff = eat_rate_multiplier/*body.eat_rate.value*/;
				health -= 1;//diff*dt;
				energy += 1;// diff*dt;
			}
			//System.out.print("4");
			
		} catch (Exception ex) {
			System.out.println("ex in organism do time: "+ex);
			ex.printStackTrace();
		}
		
		//repair armor
		
		if( amt_armor < body.getBaseArmor() && energy > max_energy*armor_repair_threshold && last_attacked < Time.game_time - repair_delay) {
			double amt_repair = dt*armor_repair_rate*body.getBaseArmor();
			if( amt_repair > energy-max_energy*armor_repair_threshold)
				amt_repair = energy-max_energy*armor_repair_threshold;
			if( amt_repair > body.getBaseArmor() - amt_armor);
				amt_repair = body.getBaseArmor() - amt_armor;
			amt_armor += amt_repair;
			energy -= amt_repair;
		}
		
		grow();

		if( x != x || y != y || health <= 0 || health != health) {
			world.organism_died(this);
			//state = DEAD;
		}
		
		//if no energy, consume muscles
		/*
		if( pregnant > 0) {
			float diff = dt*gestation_rate_multiplier;
			if( diff > resource_levels[0])
				diff = resource_levels[0];
			if( diff > resource_levels[1])
				diff = resource_levels[1];
			if( diff > 20-pregnancy)
				diff = 20-pregnancy;
			if( diff < 0)
				diff = 0;
			resource_levels[0]-=diff;
			resource_levels[1]-=diff;
			pregnancy += diff;
			if( pregnancy == 20) {
				pregnant = 0;
				pregnancy = 0;
			
			}
		}
		*/
	}
	
	public Organism(World theworld) {
		this();
		world = theworld;
		/*
		for( int i = 0; i < rsqrd_distances_from_resources.length; i++)
			rsqrd_distances_from_resources[i] = new PsuedoVector();
		for( int i = 0; i < harvesting.length; i++)
			harvesting[i] = true;
		resource_levels[0] = 20;
		pregnant = (float)Math.random();
		if( pregnant < 0.5)
			pregnant = 0;
			*/
	}
	
	
	static int display_mode = 0;
	static int num_display_modes = 3;
	public static void changeDisplayMode() {
		display_mode++;
		display_mode %= num_display_modes; 
	}
	double madx = 0;
	double mady = 0;
	public void drawOutsideDecoration(Graphics g) {
		madx = madx*0.8+dx*0.2;
		mady = mady*0.8+dy*0.2;
		
		float cr = (float)(((body.color[0].value-0.5)*2.0+0.5)*256.0); cr = cr != cr ? 128 : (cr < 0 ? 0 : (cr > 255 ? 255 : cr)); 
		float cg = (float)(((body.color[1].value-0.5)*2.0+0.5)*256.0); cg = cg != cg ? 128 : (cg < 0 ? 0 : (cg > 255 ? 255 : cg));
		float cb = (float)(((body.color[2].value-0.5)*2.0+0.5)*256.0); cb = cb != cb ? 128 : (cb < 0 ? 0 : (cb > 255 ? 255 : cb));
		//float cr = (float)(((body.color2[0].value-0.5)*2.0+0.5)*256.0); cr = cr != cr ? 128 : (cr < 0 ? 0 : (cr > 255 ? 255 : cr)); 
		//float cg = (float)(((body.color2[1].value-0.5)*2.0+0.5)*256.0); cg = cg != cg ? 128 : (cg < 0 ? 0 : (cg > 255 ? 255 : cg));
		//float cb = (float)(((body.color2[2].value-0.5)*2.0+0.5)*256.0); cb = cb != cb ? 128 : (cb < 0 ? 0 : (cb > 255 ? 255 : cb));
		double theta = Math.atan2(mady, madx);
		double xe = radius*Math.cos(theta);
		double ye = radius*Math.sin(theta);
		double xen = radius*Math.cos(theta+Math.PI/2);
		double yen = radius*Math.sin(theta+Math.PI/2);
		
		float melee_weap_size = 12.5f*(float)Math.sqrt(body.melee_weap.value)/radius;
		double xem = xe*melee_weap_size;
		double yem = ye*melee_weap_size;
		double xenm = xen*melee_weap_size;
		double yenm = yen*melee_weap_size;

		int[] xp1 = new int[]{(int)x,(int)(x+xem+xenm),(int)(x+xem*2f+xenm*0.5f),(int)(x+xem+xenm*0.75f)};
		int[] yp1 = new int[]{(int)y,(int)(y+yem+yenm),(int)(y+yem*2f+yenm*0.5f),(int)(y+yem+yenm*0.75f)};
		int[] xp2 = new int[]{(int)x,(int)(x+xem-xenm),(int)(x+xem*2f-xenm*0.5f),(int)(x+xem-xenm*0.75f)};
		int[] yp2 = new int[]{(int)y,(int)(y+yem-yenm),(int)(y+yem*2f-yenm*0.5f),(int)(y+yem-yenm*0.75f)};
		g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
		g.fillPolygon(xp1,yp1,xp1.length);
		g.fillPolygon(xp2,yp2,xp2.length);
		g.setColor(Color.BLACK);//randomColor());//Color.red.brighter());
		g.drawPolygon(xp1,yp1,xp1.length);
		g.drawPolygon(xp2,yp2,xp2.length);
		
		g.setColor(Color.black);
		g.drawLine((int)(x), (int)(y), (int)(x-xe*2), (int)(y-ye*2));
		double eye = 1;
		g.fillOval((int)(x+xe*eye+xen*0.4-2),(int)(y+ye*eye+yen*0.4-2),4,4);
		g.fillOval((int)(x+xe*eye-xen*0.4-2),(int)(y+ye*eye-yen*0.4-2),4,4);
		double mult = 1.6;
		xe*=mult;
		ye*=mult;
		xen*=mult;
		yen*=mult;
		double nrot = 0.8;
		double rot = 0.4;
		g.drawLine((int)(x+xen), (int)(y+yen), (int)(x-xen), (int)(y-yen));
		g.drawLine((int)(x+xen*nrot+xe*rot), (int)(y+yen*nrot+ye*rot), (int)(x-xen*nrot-xe*rot), (int)(y-yen*nrot-ye*rot));
		g.drawLine((int)(x+xen*nrot-xe*rot), (int)(y+yen*nrot-ye*rot), (int)(x-xen*nrot+xe*rot), (int)(y-yen*nrot+ye*rot));
	}
	public void drawInsideDecoration(Graphics g) {
		if(!drawinside)
			return;
		double theta = Math.atan2(mady, madx);
		double xe = radius*Math.cos(theta)*0.3;
		double ye = radius*Math.sin(theta)*0.3;
		double xen = radius*Math.cos(theta+Math.PI/2)*0.4;
		double yen = radius*Math.sin(theta+Math.PI/2)*0.4;

		float cr = (float)(((body.color2[0].value-0.5)*2.0+0.5)*256.0); cr = cr != cr ? 128 : (cr < 0 ? 0 : (cr > 255 ? 255 : cr)); 
		float cg = (float)(((body.color2[1].value-0.5)*2.0+0.5)*256.0); cg = cg != cg ? 128 : (cg < 0 ? 0 : (cg > 255 ? 255 : cg));
		float cb = (float)(((body.color2[2].value-0.5)*2.0+0.5)*256.0); cb = cb != cb ? 128 : (cb < 0 ? 0 : (cb > 255 ? 255 : cb));
		/*
		if( display_mode < 3 && false) {
			g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
		} else if( display_mode == 3) {
		} else
			g.setColor(Color.black);
		*/
		g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
		//g.setColor(new Color((int)(cr*0.66),(int)(cg*0.66),(int)(cb*0.66)));//randomColor());//Color.red.brighter());
		if( being_attacked)
			g.setColor(Color.red.darker());
		if( body.inside_decoration.value < 0.0 && body.inside_decoration2.value > 0.0) {
			double sep = 0.9;
			double x1 = x+xe*sep+xen*sep;
			double y1 = y+ye*sep+yen*sep;
			double x2 = x-xe*sep+xen*sep;
			double y2 = y-ye*sep+yen*sep;
			double x3 = x+xe*sep-xen*sep;
			double y3 = y+ye*sep-yen*sep;
			double x4 = x-xe*sep-xen*sep;
			double y4 = y-ye*sep-yen*sep;
			double r = radius*0.25;
			g.fillOval((int)(x1-r), (int)(y1-r), (int)(r+r), (int)(r+r));
			g.fillOval((int)(x2-r), (int)(y2-r), (int)(r+r), (int)(r+r));
			g.fillOval((int)(x3-r), (int)(y3-r), (int)(r+r), (int)(r+r));
			g.fillOval((int)(x4-r), (int)(y4-r), (int)(r+r), (int)(r+r));
		
		}
		if( body.inside_decoration.value < 0.0 && body.inside_decoration2.value < 0.0) {
			//g.setColor(Color.black);
			//g.setColor(color);//Color.blue);
			//madx = madx*0.8+dx*0.2;
			//mady = mady*0.8+dy*0.2;
			double xen1 = 1*Math.cos(theta+Math.PI/2)*0.3;
			double yen1 = 1*Math.sin(theta+Math.PI/2)*0.3;
			//double xe2 = radius*Math.cos(theta+Math.PI)*0.5;
			//double ye2 = radius*Math.sin(theta+Math.PI)*0.5;
			
			//g.drawLine(arg0, arg1, arg2, arg3)
			double ml = 1.5;
		
			g.drawLine((int)(x+xe*ml), (int)(y+ye*ml), (int)(x-xe*ml), (int)(y-ye*ml));
			g.drawLine((int)(x+xe*ml+xen1), (int)(y+ye*ml+yen1), (int)(x-xe*ml+xen1), (int)(y-ye*ml+yen1));
			g.drawLine((int)(x+xe*ml-xen1), (int)(y+ye*ml-yen1), (int)(x-xe*ml-xen1), (int)(y-ye*ml-yen1));
			
			g.drawLine((int)(x+xe+xen), (int)(y+ye+yen), (int)(x-xe+xen), (int)(y-ye+yen));
			g.drawLine((int)(x+xe+xen+xen1), (int)(y+ye+yen+yen1), (int)(x-xe+xen+xen1), (int)(y-ye+yen+yen1));
			g.drawLine((int)(x+xe+xen-xen1), (int)(y+ye+yen-yen1), (int)(x-xe+xen-xen1), (int)(y-ye+yen-yen1));
			
			g.drawLine((int)(x+xe-xen), (int)(y+ye-yen), (int)(x-xe-xen), (int)(y-ye-yen));
			g.drawLine((int)(x+xe-xen+xen1), (int)(y+ye-yen+yen1), (int)(x-xe-xen+xen1), (int)(y-ye-yen+yen1));
			g.drawLine((int)(x+xe-xen-xen1), (int)(y+ye-yen-yen1), (int)(x-xe-xen-xen1), (int)(y-ye-yen-yen1));
			//g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
			
		} else if( body.inside_decoration.value > 0.25) {
			
		}
	}
	public long last_grow_time = 0;
	public void grow() {
		if( body.current_size.value == body.adult_size.value)
			return;
		
		if( last_grow_time < born)
			last_grow_time = born;
		double ti = (double)(Time.game_time-last_grow_time);
		last_grow_time = Time.game_time;
		if( energy <= max_energy*grow_thresh1)
			return;
		if( body.growth_rate.value <= 0)
			body.growth_rate.value = 1;
		body.growth_rate.value = 1;
		/*if( ti <= 0)
			ti = 10;*/
		double amt = (double)body.getBaseSize()*
			(double)grow_rate_multiplier*
			(double)body.growth_rate.value*
			ti
			;
		//System.out.println("ti: "+ti);
		
		if( amt > energy - max_energy*grow_thresh2)
			amt = (double)(energy - max_energy*grow_thresh2);
		if( amt <= 0)
			return;
		
		double f = body.getBaseSize();
		double g = Math.sqrt(f*f+amt*growth_energy_multiple);
		if( g <= f)
			return;
		if( g > body.adult_size.value)
			g = body.adult_size.value;
		if( g>f*max_growth)
			g = f*max_growth;

		double prev_max_energy = max_energy;
		float prev_max_health = max_health;
		body.setSize((float)g);
		g = body.getBaseSize();
		//System.out.println("growing: " + ((g/f-1.0)*100.0) +" %");
		amt = (g*g-f*f)/growth_energy_multiple;
		max_energy = body.getBasEnergy();
		max_health = body.getBaseHealth();
		radius = body.getBaseSize();
		energy -= amt;
		//if( energy*max_energy/prev_max_energy <= max_energy*0.80f);
			//energy *= max_energy/prev_max_energy;
		health *= max_health/prev_max_health;
		if( energy < max_energy*0.7);
			energy = max_energy*0.7f;
	}

	public void draw(Graphics g) {
		this.radius = 5.0f*body.getBaseSize();//body.current_size.value;
		double radius = this.radius;
		//dx = (float)(dx*0.8 + (float)((Math.random()-0.5)*0.03));
		//dy = (float)(dy*0.8 + (float)((Math.random()-0.5)*0.03));
		try {
			drawOutsideDecoration(g);
		} catch (Exception ex) {
			System.out.println("ex in organism draw "+ex);
			ex.printStackTrace();
		}
		try {
			//show stats
			if( display_mode == 0) {
				y += radius;
				g.setColor(Color.gray);
				g.fillRect((int)x-10,(int)y+10, (int)20, 3+4+(body.armor.value > 0 ? 4 : 0));
				
				g.setColor(Color.yellow);
				if( max_energy <= 0)
					max_energy = 0.0001f;
				if( max_health <= 0)
					max_health = 0.0001f;
				if( energy > max_energy)
					energy = max_energy;
				if( health > max_health)
					health = max_health;
				if( amt_armor > body.getBaseArmor())
					amt_armor = body.getBaseArmor();
				g.fillRect((int)x-11,(int)y+10, (int)(20f*energy/max_energy), 3);
				g.setColor(Resource.resource_colors[0]);
				g.fillRect((int)x-11,(int)y+14, (int)(20f*health/max_health), 3);
				if( body.armor.adult_value > 0) {
					g.setColor(Color.BLUE);
					g.fillRect((int)x-11,(int)y+18, (int)(20f*amt_armor/body.getBaseArmor()), 3);
					g.setColor(Color.black);
					g.drawRect((int)x-12,(int)y+17, (int)20, 4);
					g.drawRect((int)x-12+4,(int)y+13, (int)4, 8);
					g.drawRect((int)x-12+12,(int)y+13, (int)4, 8);
				}
				g.setColor(Color.black);
				g.drawRect((int)x-12,(int)y+13, (int)20, 4);
				g.drawRect((int)x-12,(int)y+9, (int)20, 4);
				g.drawRect((int)x-12+4,(int)y+9, (int)4, 8);
				g.drawRect((int)x-12+12,(int)y+9, (int)4, 8);
				y -= radius;
				//decorations: spots, stripes
				
				y -= radius;
				float fx = x-10;
				for(int i = 0; i < Resource.NUM_TYPES && i < 3; i++) {
					g.setColor(Resource.resource_colors[i]);
					g.fillRect((int)fx,(int)y-18, (int)(resource_levels[i]), 3);
					fx+=resource_levels[i];
				}
				y += radius;
/*
				g.setColor(Resource.resource_colors[1]);
				g.fillRect((int)(x+resource_levels[0])-10,(int)y-18, (int)resource_levels[1], 3);

				g.setColor(Resource.resource_colors[0]);
				g.fillRect((int)x-10,(int)y-14, (int)(20.0f-20.0f*body.carnivorous.value), 3);
				g.setColor(Resource.resource_colors[1]);
				g.fillRect((int)(x-10f+20.0f-20.0f*body.carnivorous.value),(int)y-14, (int)(20.0f*body.carnivorous.value), 3);
	*/			
			}
			//g.fillRect((int)(x+resource_levels[0])-10,(int)y-14, (int)resource_levels[1], 3);
			/*
			g.setColor(Color.red);
			g.fillRect((int)x-10,(int)y-20, (int)pregnancy, 3);
			*/
			//g.setColor(Color.yellow.darker());
			//float hairs = ody.temperature_preference.value*4;
			

			
			g.setColor(Color.black);
			//g.drawLine((int)x, (int)y-10, (int)x, (int)y+10);
			//g.drawLine((int)x-6, (int)y-9, (int)x+6, (int)y+9);
			//g.drawLine((int)x+6, (int)y-9, (int)x-6, (int)y+9);
			//g.fillOval((int)x+10,(int)y-5,3,3);
			//g.fillOval((int)x+10,(int)y+2,3,3);
			//g.drawLine((int)x-13, (int)y+1, (int)x, (int)y+7);
			//g.drawLine((int)x-13, (int)y+1, (int)x, (int)y-6);
			//radius = 6;
			
			
			float cr = (float)(((body.color[0].value-0.5)*2.0+0.5)*256.0); cr = cr != cr ? 128 : (cr < 0 ? 0 : (cr > 255 ? 255 : cr)); 
			float cg = (float)(((body.color[1].value-0.5)*2.0+0.5)*256.0); cg = cg != cg ? 128 : (cg < 0 ? 0 : (cg > 255 ? 255 : cg));
			float cb = (float)(((body.color[2].value-0.5)*2.0+0.5)*256.0); cb = cb != cb ? 128 : (cb < 0 ? 0 : (cb > 255 ? 255 : cb));
			

			//draw armor
			g.setColor(Color.red.darker());
			double armor_volume=radius*radius+body.getBaseArmor()*4.0;
			float armor_radius = (float)Math.sqrt(armor_volume);//(float)(radius+10.0*Math.sqrt(amt_armor)/radius);
			drawBody(x,y,armor_radius,g,g.getColor(),g.getColor());
			//g.fillOval((int)(x-armor_radius), (int)(y-armor_radius), (int)(armor_radius+armor_radius), (int)(armor_radius+armor_radius));

			g.setColor(Color.black);
			armor_volume=radius*radius+amt_armor*4.0;
			armor_radius = (float)Math.sqrt(armor_volume);//(float)(radius+10.0*Math.sqrt(amt_armor)/radius);
			drawBody(x,y,armor_radius,g,g.getColor(),g.getColor());
			//g.fillOval((int)(x-armor_radius), (int)(y-armor_radius), (int)(armor_radius+armor_radius), (int)(armor_radius+armor_radius));
			
			if( health < 0)
				health = 0;
			if( max_health <= 0)
				max_health = 0.0001f;
			if( health > max_health)
				health = max_health;
			float hamt = health/max_health;
			float cr0 = 128;
			float cg0 = 0;
			float cb0 = 0;
			cr = cr*hamt+(1-hamt)*cr0;
			cg = cg*hamt+(1-hamt)*cg0;
			cb = cb*hamt+(1-hamt)*cb0;

			
			if( display_mode < 3) {
				g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
			} else if( display_mode == 3) {
				g.setColor(new Color((int)cr/2,(int)cg/2,(int)cb/2));//randomColor());//Color.red.brighter());
			} else
				g.setColor(Color.black);
			
			float cr2 = (float)(((body.color2[0].value-0.5)*2.0+0.5)*256.0); cr2 = cr2 != cr2 ? 128 : (cr2 < 0 ? 0 : (cr2 > 255 ? 255 : cr2)); 
			float cg2 = (float)(((body.color2[1].value-0.5)*2.0+0.5)*256.0); cg2 = cg2 != cg2 ? 128 : (cg2 < 0 ? 0 : (cg2 > 255 ? 255 : cg2));
			float cb2 = (float)(((body.color2[2].value-0.5)*2.0+0.5)*256.0); cb2 = cb2 != cb2 ? 128 : (cb2 < 0 ? 0 : (cb2 > 255 ? 255 : cb2));

			cr2 = cr2*hamt+(1-hamt)*cr0;
			cg2 = cg2*hamt+(1-hamt)*cg0;
			cb2 = cb2*hamt+(1-hamt)*cb0;
			Color c2 = new Color((int)cr2,(int)cg2,(int)cb2);
			//draw being attacked
			if( being_attacked) {
				g.setColor(Color.red.darker());
				c2 = Color.red.darker();
			} else {
				
			}
			//g.setColor(color);//Color.blue);
			drawBody(x,y,(float)radius,g,g.getColor(),c2);
			/*
				if( body.armor.value > 0.5) {
					radius++;
					g.drawOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
					if( body.armor.value > 0.75) {
						radius++;
						g.drawOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
					}
				}
			}*/
			if( display_mode == 2) {
				radius = this.radius/2;
				if( radius < 3)
					radius = 3;
				//if( Math.random() < 0.2)
					//outputs.signal.value = ema_color(randomColor(),outputs.signal.value,(float)1.0);
				cr = outputs.signal[0].value*128+128; cr = cr != cr ? 128 : (cr < 0 ? 0 : (cr > 255 ? 255 : cr)); 
				cg = outputs.signal[1].value*128+128; cg = cg != cg ? 128 : (cg < 0 ? 0 : (cg > 255 ? 255 : cg));
				cb = outputs.signal[2].value*128+128; cb = cb != cb ? 128 : (cb < 0 ? 0 : (cb > 255 ? 255 : cb));
				g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
				g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
				g.setColor(new Color(0,0,0));
				//g.drawOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
			}
			
			if( owner != null) {
				g.setColor(Color.black);
				g.drawString(""+owner.id, (int)x+10, (int)y+3);
			}

			if( attacking && attack_target.state == Organism.ACTIVE) {
				float dx = x-attack_target.x;
				float dy = y-attack_target.y;
				float dd = (float)Math.sqrt(dx*dx+dy*dy);
				if( dd <= max_attack_range) {
					g.setColor(Color.red);
					//radius = 10;
					//g.drawOval((int)(x-10), (int)(y-10), (int)(radius+radius), (int)(radius+radius));
					g.drawLine((int)x, (int)y, (int)attack_target.x, (int)attack_target.y);
				}
			}
			if( being_attacked) {
				g.setColor(Color.red);
				g.drawLine((int)x-10, (int)y-10, (int)x+10, (int)y+10);
				g.drawLine((int)x+10, (int)y-10, (int)x-10, (int)y+10);
			}

		} catch (Exception ex) {
			System.out.println("ex in organism draw "+ex);
			ex.printStackTrace();
		}
		try {
			if( display_mode != 2)
				drawInsideDecoration(g);
		} catch (Exception ex) {
			System.out.println("ex in organism draw "+ex);
			ex.printStackTrace();
		}
		attacking = false;
		being_attacked = false;
	}
	boolean drawinside = true;
	public void drawBody(float x, float y, float radius, Graphics g, Color c1, Color c2) {
		drawinside = true;
		float cutoff = 0.3f;
		
		double theta = Math.atan2(mady, madx);
		double xe = radius*Math.cos(theta);
		double ye = radius*Math.sin(theta);
		double xen = radius*Math.cos(theta+Math.PI/2);
		double yen = radius*Math.sin(theta+Math.PI/2);
		g.setColor(c1);
		
		if( body.outside_decoration.value <= 0 && body.outside_decoration2.value >= 0.0) { 
			g.fillOval((int)(x-radius), (int)(y-radius), (int)(radius+radius), (int)(radius+radius));
			return;
		}
		if( body.outside_decoration.value >= 0 && body.outside_decoration2.value >= 0.0) {
			int[] xp1 = new int[]{
					(int)(x+xe+xen*0.5),(int)(x+xe-xen*0.5),(int)(x-xen),
					(int)(x-xe*1.5),
					//(int)(x-xe+xen*0.5),
					(int)(x+xen)
					};
			int[] yp1 = new int[]{
					(int)(y+ye+yen*0.5),(int)(y+ye-yen*0.5),(int)(y-yen),
					(int)(y-ye*1.5),
					//(int)(y-ye+yen*0.5),
					(int)(y+yen)
					};
			g.fillPolygon(xp1,yp1,xp1.length);
			return;
		}

		
		/*float melee_weap_size = 12.5f*(float)Math.sqrt(body.melee_weap.value)/radius;
		double xem = xe*melee_weap_size;
		double yem = ye*melee_weap_size;
		double xenm = xen*melee_weap_size;
		double yenm = yen*melee_weap_size;
*/
		if( body.outside_decoration.value <= 0.0 && body.outside_decoration2.value <= 0.0) {
			float prop = 0.5f;
			float rad1 = (float)Math.sqrt(radius*radius*prop);
			float rad2 = (float)Math.sqrt(radius*radius*prop);
			float xe1 = (float)xe/radius;
			float ye1 = (float)ye/radius;
			float xen1 = (float)xen/radius;
			float yen1 = (float)yen/radius;
			double x1 = x+xe-xe1*rad1;
			double y1 = y+ye-ye1*rad1;
			double x2 = x1-xe1*(rad1);
			double y2 = y1-ye1*(rad1);
			g.fillOval((int)(x1-rad1), (int)(y1-rad1), (int)(rad1+rad1), (int)(rad1+rad1));
			g.fillOval((int)(x2-rad2), (int)(y2-rad2), (int)(rad2+rad2), (int)(rad2+rad2));
			int[] xp1 = new int[]{
					(int)(x1+xen1*rad1),
					(int)(x2+xen1*rad1),
					(int)(x2-xen1*rad1),
					(int)(x1-xen1*rad1),
					};
			int[] yp1 = new int[]{
					(int)(y1+yen1*rad1),
					(int)(y2+yen1*rad1),
					(int)(y2-yen1*rad1),
					(int)(y1-yen1*rad1),
					};
			if( body.inside_decoration.value > 0.0 && body.inside_decoration2.value < 0.0) {
				g.setColor(c2);
			}

			g.fillPolygon(xp1,yp1,xp1.length);
			return;
		}

		if( body.outside_decoration.value >= 0.0 && body.outside_decoration2.value <= 0.0) {
			float prop = 0.2f;
			float rad1 = (float)Math.sqrt(radius*radius*prop);
			float rad2 = (float)Math.sqrt(radius*radius*(1.0-prop));
			float xe1 = (float)xe/radius;
			float ye1 = (float)ye/radius;
			double x1 = x+xe-xe1*rad1;
			double y1 = y+ye-ye1*rad1;
			double x2 = x1-xe1*(rad1+rad2);
			double y2 = y1-ye1*(rad1+rad2);
			g.fillOval((int)(x1-rad1), (int)(y1-rad1), (int)(rad1+rad1), (int)(rad1+rad1));
			if( body.inside_decoration.value >= 0.0)
				g.setColor(c2);
			g.fillOval((int)(x2-rad2), (int)(y2-rad2), (int)(rad2+rad2), (int)(rad2+rad2));
			drawinside = false;
			return;
		}
		//g.setColor(new Color((int)cr,(int)cg,(int)cb));//randomColor());//Color.red.brighter());
		//g.fillPolygon(xp2,yp2,xp2.length);
		//g.setColor(Color.BLACK);//randomColor());//Color.red.brighter());
		//g.drawPolygon(xp1,yp1,xp1.length);

	}
}
