package Genetics;

public class PhysicalTrait extends Trait {
	public float mass_multiplier = 10f;
	public float size_multiplier = 1;
	public float inactive_energy = 0.75f;//0.1f;
	public float active_energy = 0.75f;
	public float effect_multiplier = 1f;//0.2f;
	public float energy_store = 10f;
	public float health_store = 5f;
	public float armor_store = 10f;
	public float nutritional_value = 10.0f;
	public float adult_value = 1f;
	
	//add energy store and health store
	public PhysicalTrait(
			float mass_multiplier,
			float size_multiplier,
			float inactive_energy,
			float active_energy,
			float effect_multiplier,
			float energy_store,
			float health_store,
			float armor_store,
			float nutritional_value
			) {
		super();
		this.mass_multiplier *= mass_multiplier;
		this.size_multiplier *= size_multiplier;
		this.inactive_energy *= inactive_energy;
		this.active_energy *= active_energy;
		this.effect_multiplier *= effect_multiplier;
		this.energy_store *= energy_store;
		this.health_store *= health_store;
		this.armor_store *= armor_store;
		this.nutritional_value *= nutritional_value;
	}
}
