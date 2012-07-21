package Genetics;

public interface iGenome {
	public Trait[] getSignedTraits();
	
	public Trait[] getNegativeLogarithmicTraits();
	
	public Trait[] getSignedLogarithmicTraits();
	
	public Inheritable[] getSignedInheritables();
	
	public Inheritable[] getSignedLogarithmicInheritables();
	
	public Inheritable[] getPositiveLogarithmicInheritables();
	
	public Inheritable[] getNegativeLogarithmicInheritables();

}
/*
	public Trait[] getSignedTraits() {
		return new Trait[]{
				body.gender,
				body.temperature_preference,
				body.humidity_preference
		};
	}
	
	public Trait[] getNegativeLogarithmicTraits() {
		return new Trait[]{
				body.movement_efficiency,
				body.compute_rate,
		};
	}
	
	public Trait[] getSignedLogarithmicTraits() {
		return new Trait[]{
				body.max_energy_consumption_rate,
				body.storage,
				body.repair_rate,
				body.eat_rate,
				body.gather_rate,
				body.drop_rate,
				body.gestation_rate,
				body.growth_rate,
				behavior.global_learning_rate,
				behavior.global_memory_ema,
				body.armor,
				body.melee_weap,
				body.melee_rate
		};
	}
	
	public Inheritable[] getSignedInheritables() {
		return new Inheritable[]{
				behavior.input_bias,
				behavior.action_post_bias,
				behavior.action_pre_bias,
    			behavior.input_rewards,
		};
	}
	
	public Inheritable[] getSignedLogarithmicInheritables() {
		return new Inheritable[]{
				behavior.input_pre_scale,
				behavior.action_pre_squash_scale,
				behavior.action_post_squash_scale,
		};
	}
	
	public Inheritable[] getPositiveLogarithmicInheritables() {
		return new Inheritable[]{
    	    	behavior.ema_input_filter_periods,
    	    	behavior.ema_action_filter_period,
    	    	//behavior.ema_input_learning_memories,
    	    	//behavior.ema_action_learning_memories,
		};
	}
	
	public Inheritable[] getNegativeLogarithmicInheritables() {
		return new Inheritable[]{
    			//behavior.input_weight_plasticities, //implemented as learning rates
    			//behavior.action_weight_plasticities,
		};
	}
*/