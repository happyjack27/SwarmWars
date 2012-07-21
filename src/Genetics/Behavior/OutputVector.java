package Genetics.Behavior;

import Genetics.*;
import Entities.*;

/*
* spatial actions: move, breed, attack, repair, xfer
* binary actions: eat,
* gather food, gather material, drop food, drop material,
* drop beacon, gather beacon, repair self,
* move, breed, attack, repair other
* 
* 
* add distance change to input vector
* add match velocity to output vector
* 
* add mimicing
*/

public class OutputVector extends Inheritable {
	
	//spatial
	public SpatialVisualVector move_target;
	public VisibleTraits attack_or_breed_target;
	//public VisibleTraits repair_target;
	public VisibleTraits attack_or_breed_target_alignment_modifier;
	public VisibleTraits mimic_target;
	
	//action / internal
	public Trait move_state;
	public Trait attack_state;
	public Trait breed_state;
	public Trait mimic_velocity;
	public Trait mimic_attack_state;
	public Trait mimic_breed_state;
	public Trait mimic_repair_state;
	public Trait[] mimic_signal;
	public Trait expected_future_reward;
	public Trait plant_age_threshold;

	
	//action only (resource mgmt)
	public Trait gather_resource_state;
	public Trait consume_resources;
	public Trait drop_resource_state;
	public Trait gather_food_state;
	public Trait consume_food;
	public Trait drop_food_state;
	public Trait gather_meds_state;
	public Trait consume_meds;
	public Trait drop_meds_state;
	public Trait[] drop_beacon_state;
	public Trait[] gather_friendly_beacon_state;
	public Trait[] gather_enemy_beacon_state;
	
	//signaling & internal
	public Trait[] internal_states;
	public Trait[] signal;
	public Trait[] resource_buy_point;
	public Trait[] resource_sell_point;
	
	//null
	public Trait source_trait = null;
	
	public void initializeMembers() {
		//spatial
		move_target = new SpatialVisualVector();
		attack_or_breed_target = new VisibleTraits();
		attack_or_breed_target_alignment_modifier = new VisibleTraits();
		//repair_target = new VisibleTraits();
		mimic_target = new VisibleTraits();
		
		//action / internal
		move_state = new Trait();
		attack_state = new Trait();
		breed_state = new Trait();
		
		expected_future_reward = new Trait();
		
		mimic_velocity = new Trait();
		mimic_attack_state = new Trait();
		mimic_signal = new Trait[Behavior.NUM_SIGNALS];
		
		//dx_bias = new Trait();
		//dy_bias = new Trait();

    	//action only (resource mgmt)
		
		resource_buy_point = new Trait[Resource.NUM_TYPES];
		resource_sell_point = new Trait[Resource.NUM_TYPES];
    	gather_resource_state = new Trait();
    	drop_resource_state = new Trait();
    	gather_food_state = new Trait();
    	drop_food_state = new Trait();
    	gather_meds_state = new Trait();
    	drop_meds_state = new Trait();
		consume_resources = new Trait();
		consume_food = new Trait();
		consume_meds = new Trait();
		plant_age_threshold = new Trait(); 

    	drop_beacon_state = new Trait[Beacon.NUM_TYPES];
    	gather_friendly_beacon_state = new Trait[Beacon.NUM_TYPES];
    	gather_enemy_beacon_state = new Trait[Beacon.NUM_TYPES];
    	
    	//signaling & internal
    	internal_states = new Trait[Behavior.NUM_INTERNAL_STATES];
    	signal = new Trait[Behavior.NUM_SIGNALS];
	}
	
    public void collectTraits() {
    	inheritables = new Inheritable[]{move_target,attack_or_breed_target,attack_or_breed_target_alignment_modifier,mimic_target};
    	trait_arrays = new Trait[][]{internal_states,gather_friendly_beacon_state,gather_enemy_beacon_state,drop_beacon_state,signal,mimic_signal,resource_buy_point,resource_sell_point};
    	traits = new Trait[]{move_state,attack_state,breed_state,consume_food,consume_resources,consume_meds,mimic_velocity,mimic_attack_state,expected_future_reward,gather_resource_state,drop_resource_state,gather_food_state,drop_food_state,gather_meds_state,drop_meds_state,plant_age_threshold};//,dx_bias,dy_bias};
    }
}
		/*
		 * 	Trait[] level2;
	Trait[] internal_states;

	//action only (resource mgmt)
	Trait[] harvest_resource;
	Trait[] transfer_resource;
	Trait[] drop_resource;
	Trait[] drop_pherome;
	Trait[] signal;

	
	/*
	public ActionMix scale(Trait scale, ActionMix result) {
		if( result == null) result = new ActionMix();
		
		result.move = move.scale(scale);
		result.attack = attack.scale(scale);
		result.breed = breed.scale(scale);
		result.transfer = transfer.scale(scale);
		result.regen = this.regen * scale;
		result.fight_or_flight = this.fight_or_flight * scale;
		result.mate = this.mate * scale;
		for( int i = 0; i < 3; i++) {
			result.level2[i] = this.level2[i] * scale;
			result.internal_states[i] = this.internal_states[i] * scale;
			result.harvest_resource[i] = this.harvest_resource[i] * scale;
			result.transfer_resource[i] = this.transfer_resource[i] * scale;
			result.drop_resource[i] = this.drop_resource[i] * scale;
			result.drop_pherome[i] = this.drop_pherome[i] * scale;
			result.signal[i] = this.signal[i] * scale;
		}
		return result;
	}
	public ActionMix multiply(ActionMix source, ActionMix result) {
		if( result == null) result = new ActionMix();
		
		result.move = move.multiply(source.move);
		result.attack = attack.multiply(source.attack);
		result.breed = breed.multiply(source.breed);
		result.transfer = transfer.multiply(source.transfer);
		result.regen = this.regen * source.regen;
		result.fight_or_flight = this.fight_or_flight * source.fight_or_flight;
		result.mate = this.mate * source.mate;
		for( int i = 0; i < 3; i++) {
			result.level2[i] = this.level2[i] * source.level2[i];
			result.internal_states[i] = this.internal_states[i] * source.internal_states[i];
			result.harvest_resource[i] = this.harvest_resource[i] * source.harvest_resource[i];
			result.transfer_resource[i] = this.transfer_resource[i] * source.transfer_resource[i];
			result.drop_resource[i] = this.drop_resource[i] * source.drop_resource[i];
			result.drop_pherome[i] = this.drop_pherome[i] * source.drop_pherome[i];
			result.signal[i] = this.signal[i] * source.signal[i];
		}
		return result;
	}
	public ActionMix add(ActionMix source, ActionMix result) {
		if( result == null) result = new ActionMix();
		
		result.move = move.add(source.move);
		result.attack = attack.add(source.attack);
		result.breed = breed.add(source.breed);
		result.transfer = transfer.add(source.transfer);
		result.regen = this.regen + source.regen;
		result.fight_or_flight = this.fight_or_flight + source.fight_or_flight;
		result.mate = this.mate + source.mate;
		for( int i = 0; i < 3; i++) {
			result.level2[i] = this.level2[i] + source.level2[i];
			result.internal_states[i] = this.internal_states[i] + source.internal_states[i];
			result.harvest_resource[i] = this.harvest_resource[i] + source.harvest_resource[i];
			result.transfer_resource[i] = this.transfer_resource[i] + source.transfer_resource[i];
			result.drop_resource[i] = this.drop_resource[i] + source.drop_resource[i];
			result.drop_pherome[i] = this.drop_pherome[i] + source.drop_pherome[i];
			result.signal[i] = this.signal[i] + source.signal[i];
		}
		return result;
	}
	public ActionMix multiply(ActionMix source) {
		return multiply(source,new ActionMix());
	}
	public ActionMix add(ActionMix source) {
		return add(source,new ActionMix());
	}
	public ActionMix scale(Trait scale) {
		return scale(scale,new ActionMix());
	}	*/

//senstive to inputs
//internal states
//spatial output actions - move, attack, breed, xfer
//nonspatial actions