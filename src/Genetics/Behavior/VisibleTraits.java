package Genetics.Behavior;

import Entities.Resource;
import Genetics.*;

public class VisibleTraits extends Inheritable {// implements MultiLinearOld<VisibleTraits> {
    static float MALE = -1;
    static float FEMALE = 1;
    //Trait range_weap = new Trait();
    public Trait gender; //0
    public Trait melee_weap; //1
    public Trait armor; //1
    public Trait size; //2
    public Trait[] color; //345
    public Trait decoration;
    public Trait[] signal; //678
    public Trait[] resource_levels; //9 10 //s/b energy value, medicine.
    public Trait change_in_distance; 
	public Trait temperature_preference; //for climate adaptation -1 to 1
	public Trait humidity_preference;
	public Trait carnivorous;
	public Trait energy;
	public Trait health;
	public Trait cash;
    
    public void initializeMembers() {
        gender = new Trait(); 
        melee_weap = new Trait();
        size = new Trait();
        cash = new Trait();
        color = new Trait[3];
        signal = new Trait[Behavior.NUM_SIGNALS];
        resource_levels = new Trait[Resource.NUM_TYPES];
        change_in_distance = new Trait();
		temperature_preference = new Trait(); 
		humidity_preference = new Trait(); 
    	carnivorous = new Trait();
        energy = new Trait();
        health = new Trait();
        armor = new Trait();
    }
    
    public void collectTraits() {
        inheritables = new Inheritable[]{};
    	trait_arrays = new Trait[][]{color,signal,resource_levels};
    	traits = new Trait[]{gender,melee_weap,size,change_in_distance,temperature_preference,humidity_preference,energy,health,cash,armor,carnivorous};
    }
/*    
	public void collectMultiLinear() {
		for( int i = 0; i < color.length; i++)
			scalars.add(color[i]);
		for( int i = 0; i < signal.length; i++)
			scalars.add(signal[i]);
		for( int i = 0; i < resource_levels.length; i++)
			scalars.add(resource_levels[i]);
		scalars.add(gender);
		scalars.add(melee_weap);
		scalars.add(range_weap);
		scalars.add(size);
	}
	public void fromMultiLinear(Iterator<Trait> it) {
		gender = it.next();
		melee_weap = it.next();
		range_weap = it.next();
		size = it.next();
		for( int i = 0; i < color.length; i++)
			color[i] = it.next();
		for( int i = 0; i < signal.length; i++)
			signal[i] = it.next();
		for( int i = 0; i < resource_levels.length; i++)
			resource_levels[i] = it.next();
	}*/
    
    /*
    
	public VisibleTraits scale(float scale, VisibleTraits result) {
		if( result == null) result = new VisibleTraits();
		
		result.gender = this.gender * scale;
		result.melee_weap = this.melee_weap * scale;
		result.range_weap = this.range_weap * scale;
		result.size = this.size * scale;
		for( int i = 0; i < 3; i++) {
			result.color[i] = this.color[i] * scale;
			result.signal[i] = this.signal[i] * scale;
			result.resource_levels[i] = this.resource_levels[i] * scale;
		}
		return result;
	}
	public VisibleTraits multiply(VisibleTraits source, VisibleTraits result) {
		if( result == null) result = new VisibleTraits();
		
		result.gender = this.gender * source.gender;
		result.melee_weap = this.melee_weap * source.melee_weap;
		result.range_weap = this.range_weap * source.range_weap;
		result.size = this.size * source.size;
		for( int i = 0; i < 3; i++) {
			result.color[i] = this.color[i] * source.color[i];
			result.signal[i] = this.signal[i] * source.signal[i];
			result.resource_levels[i] = this.resource_levels[i] * source.resource_levels[i];
		}
		return result;
	}
	public VisibleTraits add(VisibleTraits source, VisibleTraits result) {
		if( result == null) result = new VisibleTraits();
		
		result.gender = this.gender + source.gender;
		result.melee_weap = this.melee_weap + source.melee_weap;
		result.range_weap = this.range_weap + source.range_weap;
		result.size = this.size + source.size;
		for( int i = 0; i < 3; i++) {
			result.color[i] = this.color[i] + source.color[i];
			result.signal[i] = this.signal[i] + source.signal[i];
			result.resource_levels[i] = this.resource_levels[i] + source.resource_levels[i];
		}
		return result;
	}
	public VisibleTraits multiply(VisibleTraits source) {
		return multiply(source,new VisibleTraits());
	}
	public VisibleTraits add(VisibleTraits source) {
		return add(source,new VisibleTraits());
	}
	public VisibleTraits scale(float source) {
		return scale(source,new VisibleTraits());
	}
	public float getTotal() {
		float total = 0;
		
		total += this.gender;
		total += this.melee_weap;
		total += this.range_weap;
		total += this.size;
		for( int i = 0; i < 3; i++) {
			total += this.color[i];
			total += this.signal[i];
			total += this.resource_levels[i];
		}
		return total;
	}*/
    
}
