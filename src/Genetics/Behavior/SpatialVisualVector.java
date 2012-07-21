package Genetics.Behavior;

import Entities.*;
import Genetics.*;

public class SpatialVisualVector extends Inheritable {
	public VisibleTraits organisms;
	public VisibleTraits alignment_modifier;
	public Trait resource_density;
	public Trait food_density;
	public Trait meds_density;
	public Trait[] friendly_beacons;
	public Trait[] enemy_beacons;
	
	public void initializeMembers() {
		organisms = new VisibleTraits();
		alignment_modifier = new VisibleTraits();
	
    	resource_density = new Trait();
    	food_density = new Trait();
    	meds_density = new Trait();
    	friendly_beacons = new Trait[Beacon.NUM_TYPES];
    	enemy_beacons = new Trait[Beacon.NUM_TYPES];
	}

    public void collectTraits() {
    	inheritables = new Inheritable[]{organisms,alignment_modifier};
    	trait_arrays = new Trait[][]{friendly_beacons,enemy_beacons};
    	traits = new Trait[]{resource_density,food_density,meds_density};
    }
    /*
     * construct this by summing the values over the distance squared of the components.
     * (and subtracting self)
     * 
     * also construct vector as weights of each source (stored per source in nxn matrix)
     */
	
		/*
		for( int i = 0; i < resources.length; i++)
			resources[i] = new Trait();
		for( int i = 0; i < friendly_pheromes.length; i++)
			friendly_pheromes[i] = new Trait();
		for( int i = 0; i < enemy_pheromes.length; i++)
			enemy_pheromes[i] = new Trait();
    	collectMultiLinear();
    }
    
	public void collectMultiLinear() {
		scalars.addAll(friendly.scalars);
		scalars.addAll(enemy.scalars);
		for( int i = 0; i < resources.length; i++)
			scalars.add(resources[i]);
		for( int i = 0; i < friendly_pheromes.length; i++)
			scalars.add(friendly_pheromes[i]);
		for( int i = 0; i < enemy_pheromes.length; i++)
			scalars.add(enemy_pheromes[i]);
	}
	public void fromMultiLinear(Iterator<Trait> it) {
		friendly.fromMultiLinear(it);
		enemy.fromMultiLinear(it);
		for( int i = 0; i < resources.length; i++)
			resources[i] = it.next();
		for( int i = 0; i < friendly_pheromes.length; i++)
			friendly_pheromes[i] = it.next();
		for( int i = 0; i < enemy_pheromes.length; i++)
			enemy_pheromes[i] = it.next();
	}	*/
	
/*
	public SpatialVisualMix scale(float scale, SpatialVisualMix result) {
		if( result == null) result = new SpatialVisualMix();
		
		result.friendly = friendly.scale(scale);
		result.enemy = enemy.scale(scale);
		for( int i = 0; i < 3; i++) {
			result.resources[i] = this.resources[i] * scale;
			result.friendly_pheromes[i] = this.friendly_pheromes[i] * scale;
			result.enemy_pheromes[i] = this.enemy_pheromes[i] * scale;
		}
		return result;
	}
	public SpatialVisualMix multiply(SpatialVisualMix source, SpatialVisualMix result) {
		if( result == null) result = new SpatialVisualMix();
		
		result.friendly = friendly.multiply(source.friendly);
		result.enemy = enemy.multiply(source.enemy);
		for( int i = 0; i < 3; i++) {
			result.resources[i] = this.resources[i] * source.resources[i];
			result.friendly_pheromes[i] = this.friendly_pheromes[i] * source.friendly_pheromes[i];
			result.enemy_pheromes[i] = this.enemy_pheromes[i] * source.enemy_pheromes[i];
		}
		return result;
	}
	public SpatialVisualMix add(SpatialVisualMix source, SpatialVisualMix result) {
		if( result == null) result = new SpatialVisualMix();
		
		result.friendly = friendly.add(source.friendly);
		result.enemy = enemy.add(source.enemy);
		for( int i = 0; i < 3; i++) {
			result.resources[i] = this.resources[i] + source.resources[i];
			result.friendly_pheromes[i] = this.friendly_pheromes[i] + source.friendly_pheromes[i];
			result.enemy_pheromes[i] = this.enemy_pheromes[i] + source.enemy_pheromes[i];
		}
		return result;
	}
	public SpatialVisualMix multiply(SpatialVisualMix source) {
		return multiply(source,new SpatialVisualMix());
	}
	public SpatialVisualMix add(SpatialVisualMix source) {
		return add(source,new SpatialVisualMix());
	}	
	public SpatialVisualMix scale(float scale) {
		return scale(scale,new SpatialVisualMix());
	}	
	public float getTotal() {
		float total = 0;
		
		total += friendly.getTotal();
		total += enemy.getTotal();
		for( int i = 0; i < 3; i++) {
			total += this.resources[i];
			total += this.friendly_pheromes[i];
			total += this.enemy_pheromes[i];
		}
		return total;
	}
	*/
}
