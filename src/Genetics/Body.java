package Genetics;

public class Body extends Inheritable {
	public static float init_size = 0.4f;
	public static float min_adult_size = (float)Math.sqrt(init_size*init_size*2.5);
	public static float min_edibility_size_ratio = 1.5f;
	public Trait[] color;// = new Trait[3];
	public Trait[] color2;
	public Trait inside_decoration;
	public Trait inside_decoration2;
	public Trait outside_decoration;
	public Trait outside_decoration2;
	public Trait gender;
	public PhysicalTrait[] massive_traits;
	public PhysicalTrait armor;
	public PhysicalTrait legs; // move rate
	public PhysicalTrait arms; // gather rate
	public PhysicalTrait stomach;
	public PhysicalTrait intestine;
	public PhysicalTrait melee_weap; // ability to pierce armor ( weap - armor =
	public PhysicalTrait battery;
									
	public Trait has_melee_weap;
	public Trait has_armor;

	public Trait energy_storage_efficiency;
	public Trait health_efficiency;
	public Trait movement_efficiency;
	public Trait stasis_efficiency;

	public Trait inertia;

	public Trait birth_size;
	public Trait current_size;
	public Trait adult_size;

	// public Trait storage;
	public Trait growth_rate; // can do any of 3 conversions? //born at half
								// size, grow the rest.
	public Trait compute_rate; // <-how often to recalc behavior outputs
								// (logarithmic)
	public Trait location_blur;
	public Trait num_spatial_samples;
	public Trait genetic_selectivity;
	public Trait mutation_rate;
	public Trait temperature_preference; // for climate adaptation -1 to 1
	public Trait humidity_preference;
	public Trait carnivorous;

	double birth_nutrient = 0;
	double base_nutrient = 0;
	double base_mass = 0;
	double base_size = 0;
	double base_energy = 0;
	double base_armor = 0;
	double base_health = 0;
	double pct_of_adult = 0;

	public float getBaseMass() {
		if( base_mass*pct_of_adult <= 0)
			return 0.00001f;
		return (float)(base_mass*pct_of_adult);
	}

	public float getBaseSize() {
		float f = (float)Math.sqrt(base_size*base_size*pct_of_adult);
		if( f <= 0)
			return 0.00001f;
		return f;
	}

	public float getBaseHealth() {
		if( base_health*pct_of_adult <= 0)
			return 0.00001f;
		return (float)(base_health*pct_of_adult);
	}

	public float getBaseArmor() {
		if( base_armor*pct_of_adult <= 0)
			return 0.00001f;
		return (float)(base_armor*pct_of_adult);
	}

	public float getBasEnergy() {
		if( base_energy*pct_of_adult <= 0)
			return 0.00001f;
		return (float)(base_energy*pct_of_adult);
	}
	public float getBaseNutrient() {
		if( base_nutrient*pct_of_adult - birth_nutrient < 0)
			return 0.0f;
		return (float)(base_nutrient*pct_of_adult - birth_nutrient);
	}
	public float getInitialSize() {
		return adult_size.value < init_size ? adult_size.value : init_size;
	}
	public void setSize(float g) {
		//float temp = current.size.value;
		float f = current_size.value;
		//if( f > g)
			//return;
		current_size.value = g;
		if( current_size.value <= 0)
			current_size.value  = 0.0000000001f;
		if( current_size.value > base_size)
			current_size.value = (float)base_size;
		pct_of_adult = (current_size.value*current_size.value) / (base_size*base_size);
		if( pct_of_adult <= 0f)
			pct_of_adult = 0.000001f;
		if( pct_of_adult > 1f)
			pct_of_adult = 1f;
		//current_size.value = (float)Math.sqrt(adult_size.value*adult_size.value*pct_of_adult);
		
		for (int i = 0; i < massive_traits.length; i++) {
			massive_traits[i].value = (float)(massive_traits[i].adult_value * pct_of_adult);  
		}
		
	}

	public void initializeMembers() {
		inheritables = new Inheritable[] {};
		color = new Trait[3];
		color2 = new Trait[3];
		inside_decoration = new Trait();
		inside_decoration2 = new Trait();
		outside_decoration = new Trait();
		outside_decoration2 = new Trait();
		gender = new Trait();
		energy_storage_efficiency = new Trait();
		health_efficiency = new Trait();
		movement_efficiency = new Trait();
		stasis_efficiency = new Trait();
		/*
		 * public PhysicalTrait( float mass_multiplier, float size_multiplier,
		 * float inactive_energy, float active_energy, float effect_multiplier )
		 * { /* public static final float hit_multiplier = 2f; public static
		 * final float storage_multiplier = 20.0f; public static final float
		 * gather_rate_multiplier = 0.025f; public static final float
		 * drop_rate_multiplier = 0.025f; public static final float
		 * mass_movement_multiplier = 0.00025f; public static final float
		 * gestation_rate_multiplier = 0.0005f; public static final float
		 * eat_rate_multiplier = 0.025f; public static final float
		 * repair_rate_multiplier = 0.0025f; public static final float
		 * move_rate_multiplier = 0.02f; public static final float
		 * move_rate_affect_scale = 0.01f;//1.0f/0.02f;
		 */
		//                            mass size inactive active effect                  nutrients
		//                                                           energy health armor
		melee_weap = new PhysicalTrait(1, 0.5f,  0.125f, 0.25f,   3f,   0f,   0f,   0f,    1f);
		armor =      new PhysicalTrait(1,   0f,  0.00f, 0.01f,   1f,   0f,   0f,   1f,    0f);
		
		stomach =    new PhysicalTrait(1,    1, 0.05f,  0.00f,  20f,   0f,   0f,   0f,    1f);
		battery =    new PhysicalTrait(1,    1, 0.01f,  0.00f,   0f, 1.5f,   0f,   0f,    1f);

		legs =       new PhysicalTrait(1,    1,  0.1f, 0.001f,   4f,   1f,   1f,   0f,    1f);
		arms =       new PhysicalTrait(1,    1,  0.1f,  0.01f,  10f,   1f,   1f,   0f,    1f);
		
		intestine =  new PhysicalTrait(1,    1,  0.1f,  0.00f,   2f,   1f,   1f,   0f,    1f);

		has_melee_weap = new Trait();
		has_armor = new Trait();
		
		inertia = new Trait();
		birth_size = new Trait();
		current_size = new Trait();
		adult_size = new Trait();
		// storage = new Trait();
		compute_rate = new Trait(); // <-how often to recalc behavior outputs
									// (logarithmic)
		growth_rate = new Trait(); // <-how often to recalc behavior outputs
									// (logarithmic)
		location_blur = new Trait();
		num_spatial_samples = new Trait();
		genetic_selectivity = new Trait();
		mutation_rate = new Trait();
		temperature_preference = new Trait();
		humidity_preference = new Trait();
		carnivorous = new Trait();

		// move this stuff to body class
		massive_traits = new PhysicalTrait[] { armor, arms, legs, stomach, intestine, melee_weap, battery};

	}
	public void clip(Trait t) {
		if( t.value < 0) t.value = 0;
		if( t.value > 1) t.value = 1;
	}

	public void postMakeLogic() {
		clip(health_efficiency);
		clip(energy_storage_efficiency);
		clip(stasis_efficiency);
		clip(movement_efficiency);
		
		health_efficiency.value+=0.5f;
		energy_storage_efficiency.value+=0.5f;
		movement_efficiency.value = (float)(movement_efficiency.value+0.5); 
		stasis_efficiency.value = (float)(stasis_efficiency.value+0.5); 
		current_size.value = adult_size.value;
		float size_squared = current_size.value * current_size.value;
		
		if( has_melee_weap.value < 0.5)
			melee_weap.value = 0;
		if( has_armor.value < 0.5)
			armor.value = 0;

		for (int i = 0; i < massive_traits.length; i++)
			massive_traits[i].value *= size_squared;

		base_size = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_size += massive_traits[i].value * massive_traits[i].size_multiplier;
		base_size = (float) Math.sqrt(base_size);
		if( base_size < min_adult_size)
			base_size = min_adult_size;
		// base_size/=2;
		adult_size.value = (float)base_size;

		base_mass = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_mass += massive_traits[i].value * massive_traits[i].mass_multiplier;

		inertia.value = (float) (Math.exp(Math.log(0.75) / (base_mass/10)));// *size.value*size.value;
																		// //inertia.value

		base_energy = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_energy += massive_traits[i].value * massive_traits[i].energy_store;
		base_energy *= energy_storage_efficiency.value;

		base_health = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_health += massive_traits[i].value * massive_traits[i].health_store;
		base_health *= health_efficiency.value;

		base_armor = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_armor += massive_traits[i].value * massive_traits[i].armor_store;
		
		base_nutrient = 0;
		for (int i = 0; i < massive_traits.length; i++)
			base_nutrient += massive_traits[i].value * massive_traits[i].nutritional_value;
		
		for (int i = 0; i < massive_traits.length; i++) {
			massive_traits[i].adult_value = massive_traits[i].value;
		}
		
		birth_nutrient = 0;
		setSize(getInitialSize());
		birth_nutrient = getBaseNutrient()*min_edibility_size_ratio;
		/*
		current_size.value = 
		pct_of_adult = current_size.value  / adult_size.value;
			massive_traits[i].value = massive_traits[i].adult_value;// * pct_of_adult;  
		}*/
			
	}

	public int fromDNA(int[] DNA, int start) {
		int ret = super.fromDNA(DNA, start);
		// adult_size.value = adult_size.value*adult_size.value;
		postMakeLogic();
		return ret;
	}

	public void collectTraits() {
		trait_arrays = new Trait[][] { color, color2, };
		traits = new Trait[] {
				inside_decoration, outside_decoration, inside_decoration2, outside_decoration2, 
				gender, birth_size, current_size, adult_size,
				
				armor, // health
																															// absorb
				legs, // move rate
				arms, // gather rate
				stomach, intestine, inertia, melee_weap, battery,
				
				compute_rate, growth_rate,
				
				has_melee_weap,
				has_armor,

				energy_storage_efficiency,
				health_efficiency,
				movement_efficiency,
				stasis_efficiency,

				location_blur, num_spatial_samples, genetic_selectivity, mutation_rate, temperature_preference, humidity_preference, carnivorous };
	}
}
