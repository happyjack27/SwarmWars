package Genetics.Behavior;

import java.util.*;

import World.*;

import Genetics.*;
import Entities.*;

//add global plastisity and learning memory traits!  - then can make individs signed log  (make this 2 dnas?)
/*
 * 
 * unsupervised learning:
 * 
 * 1) punish w/guassian noise on contributors to outputs of bad signals?
 * 
 * 2) move to increase outputs on good feedback, decrease on bad?
 * 
 * 3) gaussian / anti-gaussian?  reward*e^(d^2*reward)
 * 
 * do quanturm nueral net (CMA-ES)
 * good = that lives, bad = that dies
 * each forward prop, randomly pick from CMA-ES
 * 
 * store synaptic matrix as eigenmatrix/values. (requires rank-1 updates)
 * can accelerate by having all organisms share the qnn. 
 * 
 * ******use change in reward/punishment to determine goal direction!*******
 * -ouput vector delta * reward vector delta
 * 
 * *add expected future reward output?
 * expected future reward is updated simply as supervised learning.
 * 
 * use action adaptability multiplier on learning rate.
 * -and on emas?
 * 
 * use reward surprise gradient.
 *  delta(actual reward-expected reward)/dt = time direction (continue / revert)
 * 
 *  food sources / gathering - make a base and a food source modifier. 
 * 
 */

public class Behavior extends Inheritable implements iTemporal {
	public static final int NUM_NOISE_INPUTS = 3;
	public static final int NUM_INTERNAL_STATES = 6;
	public static final int NUM_SIGNALS = 3;

	public static final boolean brainOn = true;
	public static final boolean select_on_signals = true;
	public static final boolean auto_renormalize_inputs = false;
	public static final boolean activate_back_propagation = false;
	public static final float global_learning_rate_multiplier = 1.0f;
	public static final float global_memory_ema_multiplier = 1.0f;
	public float previous_reward = 0;
	public float previous_expected_reward = 0;
	// BehavioralControlTraits controlTraits = new BehavioralControlTraits();
	// *stage 0

	// learning traits: (for back propagation learning) this is not implemented
	// yet.
	public Trait global_learning_rate;
	public Trait global_memory_ema;
	public InputVector input_rewards;
	public InputVector scratch;
	// public InputVector ema_input_learning_memories;
	// public OutputVector ema_action_learning_memories;
	// public InputVector input_weight_plasticities;
	// public OutputVector action_weight_plasticities;

	// theser are input/output classes
	public InputVector inputs;
	public OutputVector outputs;
	public InputVector input_memory;
	public OutputVector output_memory;

	// apply this on inputs first
	public InputVector ema_input_filter_periods;

	// apply this on inputs first
	public InputVector input_pre_scale;
	public InputVector input_bias;

	public Vector<OutputVector> action_input_combos;
	public Vector<OutputVector> synaptic_memory;

	// InputMix[] level2;
	// final output conditioning.
	public OutputVector action_pre_squash_scale;
	public OutputVector action_pre_bias;
	public OutputVector action_post_squash_scale;
	public OutputVector action_post_bias;

	// ema filter
	public OutputVector ema_action_filter_period;

	// these are not inheritable and s/b reset to zero at birth
	InputVector ema_input_filter_values;
	OutputVector ema_action_filter_values;
	float[] internal_state_values;// = new float[Behavior.NUM_INTERNAL_STATES];
	InputVector ema_input_learning_values;
	OutputVector ema_action_learning_values;
	
	public float getTemporalGradient(float current_reward, float expected_reward) {
		float curd = current_reward-expected_reward;
		float prevd = previous_reward-previous_expected_reward;
		previous_reward = current_reward;
		previous_expected_reward = expected_reward;
		return curd-prevd;
	}

	public Behavior() {
		super();
		// spatialVisualMix
		/*
		 * action_input_combos = new Vector<ActionMix>();
		 * friendly.collectAllActionMixes(action_input_combos);
		 * enemy.collectAllActionMixes(action_input_combos); for( int i = 0; i <
		 * resources.length; i++) action_input_combos.add(resources[i]); for(
		 * int i = 0; i < friendly_beacons.length; i++)
		 * action_input_combos.add(friendly_beacons[i]); for( int i = 0; i <
		 * enemy_beacons.length; i++) action_input_combos.add(enemy_beacons[i]);
		 * 
		 * //remaining in inputmix action_input_combos.add(gender);
		 * action_input_combos.add(energy);
		 * action_input_combos.add(delta_health);
		 * action_input_combos.add(delta_energy);
		 * action_input_combos.add(pregnancy);
		 * action_input_combos.add(attack_state);
		 * action_input_combos.add(breed_state); for( int i = 0; i <
		 * internal_states.length; i++)
		 * action_input_combos.add(internal_states[i]); for( int i = 0; i <
		 * resources.length; i++)
		 * action_input_combos.add(internal_resource_levels[i]); for( int i = 0;
		 * i < internal_resource_levels.length; i++)
		 * action_input_combos.add(delta_internal_resource_levels[i]); for( int
		 * i = 0; i < delta_internal_resource_levels.length; i++)
		 * action_input_combos.add(delta_internal_resource_levels[i]);
		 */
		reset();
	}

	public void initializeMembers() {
		action_input_combos = new Vector<OutputVector>();
		synaptic_memory = new Vector<OutputVector>();
		input_rewards = new InputVector();
		scratch = new InputVector();
		// ema_input_learning_memories = new InputVector();
		// ema_action_learning_memories = new OutputVector();
		// input_weight_plasticities = new InputVector();
		// action_weight_plasticities = new OutputVector();
		global_memory_ema = new Trait();
		global_learning_rate = new Trait();

		// theser are input/output classes
		inputs = new InputVector();
		outputs = new OutputVector();
		input_memory = new InputVector();
		output_memory = new OutputVector();

		// apply this on inputs first
		ema_input_filter_periods = new InputVector();

		// apply this on inputs first
		input_pre_scale = new InputVector();
		input_bias = new InputVector();

		// final output conditioning.
		action_pre_squash_scale = new OutputVector();
		action_pre_bias = new OutputVector();
		action_post_squash_scale = new OutputVector();
		action_post_bias = new OutputVector();

		// ema filter
		ema_action_filter_period = new OutputVector();

		// these are not inheritable and s/b reset to zero at birth
		ema_input_filter_values = new InputVector();
		ema_action_filter_values = new OutputVector(); // not part of
														// inheritables
		ema_input_learning_values = new InputVector();
		ema_action_learning_values = new OutputVector();

		internal_state_values = new float[Behavior.NUM_INTERNAL_STATES];

		//variance = new InputVector();
		//variance.set();
	}

	public void collectTraits() {

		inheritable_arrays = new Inheritable[][] {};
		inheritables = new Inheritable[] { input_rewards,/*
														 * ema_input_learning_memories
														 * ,
														 * ema_action_learning_memories
														 * ,
														 * input_weight_plasticities
														 * ,
														 * action_weight_plasticities
														 * ,
														 */

		ema_input_filter_periods, input_pre_scale, input_bias, action_pre_squash_scale, action_pre_bias, action_post_squash_scale, action_post_bias, ema_action_filter_period };

		// now create the neural net input-action matrix and add it to
		// inheritables
		Inheritable[] ih2 = new Inheritable[inheritables.length + inputs.scalars.size()];
		int i = 0;
		for (i = 0; i < inheritables.length; i++)
			ih2[i] = inheritables[i];
		action_input_combos = new Vector<OutputVector>();
		synaptic_memory = new Vector<OutputVector>();
		Iterator<Trait> it = inputs.scalars.iterator();
		while (it.hasNext()) {
			Trait t = it.next();
			OutputVector am = new OutputVector();
			am.source_trait = t;
			action_input_combos.add(am);
			ih2[i++] = am;
			am = new OutputVector();
			am.source_trait = t;
			synaptic_memory.add(am);
		}
		inheritables = ih2;

		trait_arrays = new Trait[][] {};
		traits = new Trait[] { global_learning_rate, global_memory_ema };
	}

	public void reset() {
		Iterator<Trait> iter;
		iter = ema_input_filter_values.scalars.iterator();
		while (iter.hasNext())
			iter.next().value = 0;
		iter = ema_action_filter_values.scalars.iterator();
		while (iter.hasNext())
			iter.next().value = 0;
		iter = ema_input_learning_values.scalars.iterator();
		while (iter.hasNext())
			iter.next().value = 0;
		iter = ema_action_learning_values.scalars.iterator();
		while (iter.hasNext())
			iter.next().value = 0;

		if (activate_back_propagation) {
			iter = input_memory.scalars.iterator();
			while (iter.hasNext())
				iter.next().value = 0;
			iter = output_memory.scalars.iterator();
			while (iter.hasNext())
				iter.next().value = 0;
			Iterator<OutputVector> iter2 = synaptic_memory.iterator();
			while (iter2.hasNext()) {
				iter = iter2.next().scalars.iterator();
				while (iter.hasNext())
					iter.next().value = 0;
			}
		}

		for (int i = 0; i < internal_state_values.length; i++)
			internal_state_values[0] = 0;
	}

	InputVector squared_inputs = new InputVector();
	InputVector variance;
	InputVector inputs_squared = new InputVector();

	int prev_t = 0;

	public void doBackPropagation(float dt, float reward, float ema_rate, float learn_rate) {
		try {
		} catch (Exception ex) {

		}
	}

	public void do_time(float dt) {
		try {
			float ema_rate = global_memory_ema.value * global_memory_ema_multiplier / World.time_between_deaths;
			float learn_rate = global_learning_rate.value * global_learning_rate_multiplier / World.time_between_deaths;

			// create action mix
			outputs.reset();
			if (brainOn) { // if( activate_back_propagation) {
				if (activate_back_propagation) {
					scratch.reset();
					scratch.add(inputs);
					scratch.multiply_inner(input_rewards);
					doBackPropagation(dt, scratch.total(), ema_rate, learn_rate);
				}

				if (activate_back_propagation) {
					input_memory.scale(1 - ema_rate);
					input_memory.addScaled(inputs, ema_rate);
				}

				// scale and add bias to inputs
				World.average_inputs.scale(1.0f - World.ema_rate * 0.02f);
				World.average_inputs.addScaled(inputs, World.ema_rate * 0.02f);

				if (auto_renormalize_inputs) {
					squared_inputs.reset();
					squared_inputs.add(inputs);
					squared_inputs.multiply_inner(inputs);
					World.average_squared_inputs.scale(1.0f - World.ema_rate * 0.002f);
					World.average_squared_inputs.addScaled(squared_inputs, World.ema_rate * 0.002f);
					// update renormalization vector
					if (Time.tick_count - prev_t > 10) {
						prev_t = Time.tick_count;
						squared_inputs.reset();
						squared_inputs.add(inputs);
						variance.reset();
						variance.add(World.average_squared_inputs);
						inputs_squared.reset();
						inputs_squared.add(World.average_inputs);
						inputs_squared.multiply_inner(World.average_inputs);
						variance.addScaled(inputs_squared, -1.0f);
						variance.rsqrt();
					}

					// now renormalize inputs
					inputs.addScaled(World.average_inputs, -1.0f);
					inputs.multiply_inner(variance);
				}

				inputs.multiply_inner(input_pre_scale);
				inputs.add(input_bias);

				// apply all mixers
				Iterator<OutputVector> oi = action_input_combos.iterator();
				Iterator<OutputVector> smi = synaptic_memory.iterator();
				if (activate_back_propagation) {
					while (oi.hasNext()) {
						OutputVector am = oi.next();
						OutputVector sm = smi.next();
						sm.scale(1 - ema_rate);
						sm.addScaled(am, ema_rate * am.source_trait.value);
						outputs.addScaled(am, am.source_trait.value);
					}
				} else {
					while (oi.hasNext()) {
						OutputVector am = oi.next();
						outputs.addScaled(am, am.source_trait.value);
					}
				}
				// System.out.println("inputs.scalars.size() "+inputs.scalars.size());
				//float scale_factor = (float) Math.sqrt(1.0 / (float) inputs.scalars.size());
				// System.out.println("outputs pre scale======");
				// outputs.printout(7);
				// System.out.println("scale_factor "+scale_factor);
				// outputs.scale(scale_factor);
				// System.out.println("outputs post scale======");
				// outputs.printout(7);

				// do final ops to action mix
				outputs.multiply_inner(action_pre_squash_scale);
				outputs.add(action_pre_bias);
				outputs.squash();
				// System.out.println("outputs post squash======");
				// outputs.printout(7);
			}
			// System.out.println("action_post_squash_scale======");
			// outputs.printout(7);
			outputs.add(action_post_bias);
			outputs.multiply_inner(action_post_squash_scale);
			if (!select_on_signals) {
				for (int i = 0; i < NUM_SIGNALS; i++)
					outputs.attack_or_breed_target.signal[i].value = 0;
			}

			// clamp outputs
			for (int i = 0; i < 3; i++) {
				outputs.signal[i].value = outputs.signal[i].value > 1 ? 1 : outputs.signal[i].value < -1 ? -1 : outputs.signal[i].value;
			}

			if (activate_back_propagation) {
				output_memory.scale(1 - ema_rate);
				output_memory.addScaled(outputs, ema_rate);
			}
			// System.out.println("action_post_bias======");
			// outputs.printout(7);
			World.average_biases.scale(1.0f - World.ema_rate * 0.02f);
			World.average_biases.addScaled(action_post_bias, World.ema_rate * 0.02f);
			World.average_scales.scale(1.0f - World.ema_rate * 0.02f);
			World.average_scales.addScaled(action_post_squash_scale, World.ema_rate * 0.02f);
		} catch (Exception ex) {
			System.out.println("ex in behavior do time: " + ex);
			ex.printStackTrace();
		}

	}
}
