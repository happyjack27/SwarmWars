package Genetics;
import java.util.*;
import Genetics.Behavior.*;

public class AnimalGenome extends SuperGenome {
	public Body body;
	public Behavior behavior;
	public static GenomeTemplate template;

	public AnimalGenome() {
		super();
		if( template == null) {
			template = new GenomeTemplate(new AnimalGenome(true),new AnimalGenome(true),new AnimalGenome(true));
		}
		
	}
	public AnimalGenome(boolean is_template) {
		super();
	}
	public int fromDNA(int[] sourceDNA, int start) {
		int ret = super.fromDNA(sourceDNA, start);
		template.apply(this);
		body.gender.value = Math.round(Math.random())*2-1;
		behavior.action_post_bias.attack_state.value += 0.7;

		behavior.action_post_bias.move_state.value += 100;
		
		
		//body.stomach.value *= storage_multiplier;

		
		return ret;
	}
	
	public void initializeMembers() {
		body = new Body();
		behavior = new Behavior();
	}
	
    public void collectTraits() {
    	inheritables = new Inheritable[]{
    			body,
    			behavior,
    	};
    }
	public void initAsBiasTemplate() {
		super.initAsBiasTemplate();

		Iterator<OutputVector> iam = behavior.action_input_combos.iterator();
		while( iam.hasNext()) {
			OutputVector am = iam.next();
			Iterator<Trait> ith = am.scalars.iterator();
			while( ith.hasNext())
				ith.next().value = -0.5f;
			
		}
	}
	public void initAsScaleTemplate() {
		super.initAsScaleTemplate();
		
		Iterator<OutputVector> iam = behavior.action_input_combos.iterator();
		while( iam.hasNext()) {
			OutputVector am = iam.next();
			Iterator<Trait> ith = am.scalars.iterator();
			while( ith.hasNext())
				ith.next().value = 4.0f;
			
		}
	}
    

	public Trait[] getSignedTraits() {
		return new Trait[]{
				body.inside_decoration,
				body.outside_decoration,
				body.inside_decoration2,
				body.outside_decoration2,
				body.gender,
				body.temperature_preference,
				body.humidity_preference
		};
	}
	
	public Trait[] getNegativeLogarithmicTraits() {
		return new Trait[]{
				//body.movement_efficiency,
				body.birth_size,
				body.compute_rate,
		};
	}
	
	public Trait[] getSignedLogarithmicTraits() {
		return new Trait[]{
				body.current_size,
				body.adult_size,
				body.armor,
				body.arms,
				body.legs,
				body.stomach,
				body.intestine,
				body.stomach,
				body.melee_weap,
				/*body.max_energy_consumption_rate,
				body.storage,
				body.repair_rate,
				body.eat_rate,
				body.gather_rate,
				body.drop_rate,
				body.gestation_rate,*/
				body.growth_rate,
				behavior.global_learning_rate,
				behavior.global_memory_ema,
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
}