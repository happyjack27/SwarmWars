package Genetics;

import java.util.Iterator;

import World.Time;

public abstract class SuperGenome extends Genome {
	///FOR EACH VECTOR, ALSO ADD A SWITCH (ON/OFF)F
	public static final float log_mult = 3;
	int num_vectors = 8; 
	//public int[] resultDNA;
	//public int[] fullDNA;
	public Trait[] vectors;
	public Trait[] multipliers;
	public Trait[] switches;
	//public int[][] matrixDNA;
	public float[] vectorResult;
	//public MultiLinear<Trait>[] vectorGenomes;
	

	public SuperGenome() {
		super();
	}
	public SuperGenome(boolean is_template) {
		super();
	}
	public int[] newDNA(int length) {
		return super.newDNA(length*num_vectors+num_vectors*3);
	}
	public int[] toDNA() {
		return DNA;
	}
	public int fromDNA(int[] sourceDNA, int start) {
		int temp = start;
		if( vectors == null)
			vectors = new Trait[num_vectors];
		if( switches == null)
			switches = new Trait[num_vectors];
		if( multipliers == null)
			multipliers = new Trait[num_vectors];
		//if( matrixDNA == null)
			//matrixDNA = new int[num_vectors][];
		if( vectorResult == null)
			vectorResult = new float[scalars.size()];
		
			//vectorGenomes = new MultiLinear<Trait>[num_vectors];
		for( int i = 0; i < num_vectors; i++) {
			if( vectors[i] == null)
				vectors[i] = new Trait();
			vectors[i].fromDNA(sourceDNA[start++]);
			vectors[i].value = (vectors[i].value-0.5f)*2.0f*4.0f; //
		}

		for( int i = 0; i < num_vectors; i++) {
			if( switches[i] == null)
				switches[i] = new Trait();
			switches[i].fromDNA(sourceDNA[start++]);
			switches[i].value = switches[i].value  < 0.5 ? 0 : 1;
		}
		for( int i = 0; i < num_vectors; i++) {
			if( multipliers[i] == null)
				multipliers[i] = new Trait();
			multipliers[i].fromDNA(sourceDNA[start++]);
			multipliers[i].value = (float)Math.exp((multipliers[i].value - 0.5f)*4f);
		}

		for( int j = 0; j < scalars.size(); j++) {
			vectorResult[j] = 0;
		}
		for( int i = 0; i < num_vectors; i++) {
			//matrixDNA[i] = new int[scalars.size()];
			for( int j = 0; j < scalars.size(); j++) {
				//matrixDNA[i][j] = sourceDNA[start++];
				float f = Trait.floatFromDNA(sourceDNA[start++]);
				vectorResult[j] +=  (f-0.5f)*vectors[i].value*switches[i].value*multipliers[i].value; 
			}
		}
		for( int j = 0; j < scalars.size(); j++) {
			vectorResult[j] += 0.5;
		}

		
		DNA = new int[start-temp];
		int c = 0;
		for( ; temp < start; ) {
			DNA[c++] = sourceDNA[temp++];
		}
		//public int fromDNA(int[] sourceDNA, int start) {
		for( int j = 0; j < scalars.size(); j++) {
			scalars.get(j).value = vectorResult[j];
		}
		return start;
		//template.apply(this);
		//return ret;
	}
	/*
	public int[] newDNA(int length) {
		int[] mask = new int[length];
		for (int i = 0; i < mask.length; i++)
			mask[i] = Time.rand.nextInt();
		return mask;
	}
	*/
/*
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
	*/
}
