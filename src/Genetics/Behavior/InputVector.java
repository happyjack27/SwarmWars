package Genetics.Behavior;

import Entities.Resource;
import Genetics.*;

/*add deltas to health energy and resource!  also must put in behaviro map (Add action matrix)
*/
public class InputVector extends Inheritable {
	public SpatialVisualVector spatialVisual;
	public Trait gender;
	public Trait health;
	public Trait energy;
	public Trait armor;
	public Trait melee_weap;
	public Trait delta_health;
	public Trait delta_energy;
	public Trait delta_armor;
	public Trait pregnancy;
	public Trait attack_state;
	public Trait breed_state;
	public Trait[] internal_states;
	public Trait[] internal_resource_levels;
	public Trait[] delta_internal_resource_levels;
	public Trait[] noise;
	public Trait carnivorous;
	
	public void initializeMembers() {
    	spatialVisual = new SpatialVisualVector();
    	gender = new Trait();
    	health = new Trait();
    	energy = new Trait();
    	delta_health = new Trait();
    	delta_energy = new Trait();
    	delta_armor = new Trait();
    	pregnancy = new Trait();
    	attack_state = new Trait();
    	breed_state = new Trait();
    	armor = new Trait();
    	melee_weap = new Trait();
    	internal_states = new Trait[Behavior.NUM_INTERNAL_STATES];
    	internal_resource_levels = new Trait[Resource.NUM_TYPES];
    	delta_internal_resource_levels = new Trait[Resource.NUM_TYPES];
    	carnivorous = new Trait();
    	noise = new Trait[Behavior.NUM_NOISE_INPUTS];
	}

    public void collectTraits() {
    	inheritables = new Inheritable[]{spatialVisual};
    	trait_arrays = new Trait[][]{internal_states,internal_resource_levels,noise,delta_internal_resource_levels};
    	traits = new Trait[]{gender,health,energy,armor,delta_health,delta_energy,delta_armor,pregnancy,attack_state,breed_state,carnivorous,melee_weap};
    }
	
	/*
	
	public InputMix() {
		for( int i = 0; i < internal_states.length; i++)
			internal_states[i] = new Trait();
		for( int i = 0; i < internal_resource_levels.length; i++)
			internal_resource_levels[i] = new Trait();
		for( int i = 0; i < level2.length; i++)
			level2[i] = new Trait();
    	collectMultiLinear();
    }
    
	public void collectMultiLinear() {
		scalars.addAll(spatialVisual.scalars);
		scalars.add(gender);
		scalars.add(pregnant);
		scalars.add(fight_or_flight);
		scalars.add(mate);
		for( int i = 0; i < internal_states.length; i++)
			scalars.add(internal_states[i]);
		for( int i = 0; i < internal_resource_levels.length; i++)
			scalars.add(internal_resource_levels[i]);
		for( int i = 0; i < level2.length; i++)
			scalars.add(level2[i]);
	}
	
	public void fromMultiLinear(Iterator<Trait> it) {
		spatialVisual.fromMultiLinear(it);
		gender = it.next();
		pregnant = it.next();
		fight_or_flight = it.next();
		mate = it.next();
		for( int i = 0; i < internal_states.length; i++)
			internal_states[i] = it.next();
		for( int i = 0; i < internal_resource_levels.length; i++)
			internal_resource_levels[i] = it.next();
		for( int i = 0; i < level2.length; i++)
			level2[i] = it.next();
	}	
	*/
	
	/*
	public InputMix scale(Trait scale, InputMix result) {
		if( result == null) result = new InputMix();
		result.spatialVisual = spatialVisual.scale(scale);
		
		result.gender = this.gender * scale;
		result.pregnant = this.pregnant * scale;
		result.fight_or_flight = this.fight_or_flight * scale;
		result.mate = this.mate * scale;
		for( int i = 0; i < 3; i++) {
			result.internal_states[i] = this.internal_states[i] * scale;
			result.internal_resource_levels[i] = this.internal_resource_levels[i] * scale;
			result.level2[i] = this.level2[i] * scale;
		}
		return result;
	}
	public InputMix multiply(InputMix source, InputMix result) {
		if( result == null) result = new InputMix();
		result.spatialVisual = spatialVisual.multiply(source.spatialVisual);
		
		result.gender = this.gender * source.gender;
		result.pregnant = this.pregnant * source.pregnant;
		result.fight_or_flight = this.fight_or_flight * source.fight_or_flight;
		result.mate = this.mate * source.mate;
		for( int i = 0; i < 3; i++) {
			result.internal_states[i] = this.internal_states[i] * source.internal_states[i];
			result.internal_resource_levels[i] = this.internal_resource_levels[i] * source.internal_resource_levels[i];
			result.level2[i] = this.level2[i] * source.level2[i];
		}
		return result;
	}
	public InputMix add(InputMix source, InputMix result) {
		if( result == null) result = new InputMix();
		result.spatialVisual = spatialVisual.add(source.spatialVisual);
		
		result.gender = this.gender + source.gender;
		result.pregnant = this.pregnant + source.pregnant;
		result.fight_or_flight = this.fight_or_flight + source.fight_or_flight;
		result.mate = this.mate + source.mate;
		for( int i = 0; i < 3; i++) {
			result.internal_states[i] = this.internal_states[i] + source.internal_states[i];
			result.internal_resource_levels[i] = this.internal_resource_levels[i] + source.internal_resource_levels[i];
			result.level2[i] = this.level2[i] + source.level2[i];
		}
		return result;
	
	}
	public InputMix multiply(InputMix source) {
		return multiply(source,new InputMix());
	}
	public InputMix add(InputMix source) {
		return add(source,new InputMix());
	}
	public InputMix scale(Trait source) {
		return scale(source,new InputMix());
	}
	public Trait getTotal() {
		Trait total = spatialVisual.getTotal(); 
		
		total += this.gender;
		total += this.pregnant;
		total += this.fight_or_flight;
		total += this.mate;
		for( int i = 0; i < 3; i++) {
			total += this.internal_states[i];
			total += this.internal_resource_levels[i];
			total += this.level2[i];
		}
		return total;
	}
	*/
}
