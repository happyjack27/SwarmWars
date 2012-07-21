package Genetics;
import java.util.*;

import World.Time;

/*
 * do genome templates by having 3 genomes to make template:
 * *scale
 * *bias
 * *function (linear, logarithmic)
 * all start out as 0-1, then.
 * 
 */

public abstract class Genome extends Inheritable implements iGenome {
	public static final float log_mult = 3;
	
	public Genome() {
		super();
	}
	public Genome(boolean is_template) {
		super();
	}
	public int fromDNA(int[] sourceDNA, int start) {
		int ret = super.fromDNA(sourceDNA, start);
		//template.apply(this);
		return ret;
	}
	public int[] newDNA(int length) {
		int[] mask = new int[length];
		for (int i = 0; i < mask.length; i++)
			mask[i] = Time.rand.nextInt();
		return mask;
	}

	public int[] offspringDNA(int[] mDNA, int[] fDNA, int mutations) {
		int[] mask = new int[mDNA.length];
		for (int i = 0; i < mask.length; i++)
			mask[i] = Time.rand.nextInt();

		int[] newDNA = new int[mDNA.length];
		for (int i = 0; i < mask.length; i++)
			newDNA[i] = (mask[i] & mDNA[i]) | (~mask[i] & fDNA[i]);

		for (int i = 0; i < mutations; i++) {
			int index = Time.rand.nextInt(newDNA.length);
			int bit = 0x01 << Time.rand.nextInt(32);
			newDNA[index] ^= bit;
		}

		return newDNA;
	}
	public static int findHammingDistance(int[] mDNA, int[] fDNA) {
		int num = 0;
		for (int i = 0; i < mDNA.length; i++)
			num += Trait.numberOfSetBits(mDNA[i] ^ fDNA[i]);
		return num;
	}
	
	public void initAsBiasTemplate() {
		Trait[] signed = getSignedTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = -0.5f;
		signed = getSignedLogarithmicTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = -0.5f;
		
		Inheritable[] ih;
		ih = getSignedInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> ith = ih[i].scalars.iterator();
			while( ith.hasNext())
				ith.next().value = -0.5f;
		}
		ih = getSignedLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> ith = ih[i].scalars.iterator();
			while( ith.hasNext())
				ith.next().value = -0.5f;
		}
	}
	public void initAsScaleTemplate() {
		Iterator<Trait> it = scalars.iterator();
		while( it.hasNext())
			it.next().value = 1.0f;

		Trait[] signed;
		signed = getNegativeLogarithmicTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = -1.0f;
		signed = getSignedTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = 2.0f;
		signed = getSignedLogarithmicTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = 2.0f;
		
		Inheritable[] ih;
		ih = getNegativeLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> ith = ih[i].scalars.iterator();
			while( ith.hasNext())
				ith.next().value = -1.0f*log_mult;
		}
		ih = getSignedInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> ith = ih[i].scalars.iterator();
			while( ith.hasNext())
				ith.next().value = 2.0f;
		}
		ih = getSignedLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> ith = ih[i].scalars.iterator();
			while( ith.hasNext())
				ith.next().value = 2.0f*log_mult;
		}
	}
	
	public void initAsFunctionTemplate() {
		Trait[] signed = getSignedLogarithmicTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = 1.0f;
		signed = getNegativeLogarithmicTraits();
		for( int i = 0; i < signed.length; i++)
			signed[i].value = 1.0f;
		
		Inheritable[] ih;
		ih = getSignedLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> it = ih[i].scalars.iterator();
			while( it.hasNext())
				it.next().value = 1.0f;
		}
		ih = getNegativeLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> it = ih[i].scalars.iterator();
			while( it.hasNext())
				it.next().value = 1.0f;
		}
		ih = getPositiveLogarithmicInheritables();
		for( int i = 0; i < ih.length; i++) {
			Iterator<Trait> it = ih[i].scalars.iterator();
			while( it.hasNext())
				it.next().value = 1.0f;
		}
	}
}