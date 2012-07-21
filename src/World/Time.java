package World;

import java.util.*;
import java.util.concurrent.*;

import Entities.*;
import Util.*;
import _UI.*;

public class Time implements iTemporal {
	public static World world;

	static PriorityBlockingQueue<Temporal> eventQueue;
	public static long game_time = 0;
	public static long last_event = 0;
	public static float time_scale = 1f;
	public static GameTick gameTick = null;
	public static DrawTimer drawTimer = null;
	public static Random rand = new Random();
	static int tick_period = 0;
	static long last_tick = 0;
	static long game_time_start = 0;
	public static long last_death = 0;
	public static int tick_count = 0;

	
	public static void init() {
		if(eventQueue == null) {
			eventQueue = new PriorityBlockingQueue<Temporal>();
		}
		game_time = 0;
		rand.setSeed(new Date().getTime());
}
	public static void initWorld() {
		world = new World();
		world.initialize(1366,768, new int[]{30,0,15});
	}

	public void do_time(float dt) {
		game_time += dt;
		world.do_time(dt);
		if( eventQueue == null)
			return;
		while (true) {
			Temporal p = eventQueue.peek();
			if( p == null || p.next_execution_time > game_time)
				break;
			p = eventQueue.poll();
			p.is_scheduled = false;

			//temporal consistency check
			if( last_event > p.next_execution_time)
				System.out.println("===========events processed out of order! "+(last_event-p.next_execution_time));
			last_event = p.next_execution_time;

			
			p.do_time(game_time-p.last_execution_time);
			p.last_execution_time = game_time;
			
		}
	}


	public void start_time(int period, float scale) {
		time_scale = scale;
		tick_period = period;
		java.util.Timer t = new java.util.Timer();
		if (gameTick == null)
			gameTick = new GameTick();
		else
			gameTick.cancel();
		last_tick = new Date().getTime();
		game_time_start = last_tick;
		last_death = last_tick;
		t.schedule(gameTick, period, period);
	}
	

	public void start_draw_timer(int period) {
		java.util.Timer t = new java.util.Timer();
		if (drawTimer == null)
			drawTimer = new DrawTimer();
		else
			drawTimer.cancel();
		t.schedule(drawTimer, period, period);
	}
	class DrawTimer extends TimerTask {
		public void run() {
			world.view.repaint();
			
		}
	}

	class GameTick extends TimerTask {
		public void run() {
			long lt = new Date().getTime();
			//game_time += time_scale * tick_period;
			//do_time(time_scale * tick_period);
			do_time(time_scale * tick_period);// (float) (lt - last_tick));
			last_tick = lt;
			Time.tick_count++;
			if( Time.tick_count % 100 == 0)
				displayStats();
		}
	}

	public void displayStats() {
		System.out.println("body===========");
		System.out.println("selectivity: "+World.average_bodies.genetic_selectivity.value);
		System.out.println("mutation: "+World.average_bodies.mutation_rate.value);
		float in = World.average_bodies.inertia.value;
		in = in*2 - 1;
		System.out.println("inertia: "+in);
		System.out.println("loc blur: "+World.average_bodies.location_blur.value);
		System.out.println("spat samples: "+World.average_bodies.num_spatial_samples.value);
		System.out.println("storage: "+World.average_bodies.stomach.value);
		System.out.println("melee: "+World.average_bodies.melee_weap.value);
		System.out.println("armor: "+World.average_bodies.armor.value);
		System.out.println("carniv: "+World.average_bodies.carnivorous.value);
		System.out.println("outputs===========");
		System.out.println("move: "+World.average_biases.move_state.value+", "+World.average_scales.move_state.value);
		System.out.println("gather: "+World.average_biases.gather_food_state.value+", "+World.average_scales.gather_food_state.value);
		System.out.println("density: "+World.average_biases.move_target.food_density.value+", "+World.average_scales.move_target.food_density.value);
		//System.out.println("organism: "+World.average_biases.move_target.friendly.size.value+", "+World.average_scales.move_target.friendly.size.value);
		System.out.println("mimic targ: "+World.average_biases.mimic_target.size.value+", "+World.average_scales.mimic_target.size.value);
		System.out.println("mimic vel: "+World.average_biases.mimic_velocity.value+", "+World.average_scales.mimic_velocity.value);
		//System.out.println("breed energy: "+World.average_biases.breed_target.energy.value+", "+World.average_scales.breed_target.energy.value);
		//System.out.println("breed supply: "+World.average_biases.breed_target.resource_levels[0].value+", "+World.average_scales.breed_target.resource_levels[0].value);
		System.out.println("attack: "+World.average_biases.attack_state.value+", "+World.average_scales.attack_state.value);
		
		//outputs.move_target.friendly
		//System.out.println("storage: "+World.average_outputs.move_state.value);
	}

}
