package Genetics;

import java.util.*;

/*
 * apparently the constructor is called before member variables are initialized. :P
 * 
 */

public abstract class Inheritable extends MultiLinear<Trait> implements iInheritable<Trait> {//,iMultiLinearCollection<Trait> {
	int[] DNA;
	public Inheritable[][] inheritable_arrays;
	public Inheritable[] inheritables;
	public Trait[][] trait_arrays;
	public Trait[] traits;
	
	public Inheritable() {
		try {
			initializeMembers();
	    	collectTraits();
	    	initTriatArrays();
	    	collectMultiLinear();
		} catch (Exception ex) {
			System.out.println("inheritable constructor ex: "+ex);
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	public void initTriatArrays() {
		if( trait_arrays != null) {
			for( int i = 0; i < trait_arrays.length; i++) {
				if( trait_arrays[i] == null) {
					System.out.println("fatal error: trait array["+i+"] null "+this);
					System.exit(0);
					//continue;
				}
				for( int j = 0; j < trait_arrays[i].length; j++) {
					trait_arrays[i][j] = new Trait();
				}
			}
		}
	}
	
	public void collectMultiLinear() {
		scalars = new Vector<Trait>();
		if(inheritable_arrays != null) {
			for( int i = 0; i < inheritable_arrays.length; i++) {
				if( inheritable_arrays[i] != null) {
					for( int j = 0; j < inheritable_arrays.length; j++)
						scalars.addAll(inheritable_arrays[i][j].scalars);
				}
			}
		}
		if( inheritables != null)
			for( int i = 0; i < inheritables.length; i++) {
				scalars.addAll(inheritables[i].scalars);
			}
		if( trait_arrays != null)
			for( int i = 0; i < trait_arrays.length; i++)
				for( int j = 0; j < trait_arrays[i].length; j++)
					scalars.add(trait_arrays[i][j]);
		if( traits != null)
			for( int i = 0; i < traits.length; i++) {
				scalars.add(traits[i]);
			}
	}
	public void fromMultiLinear(Iterator<Trait> it) {
		if(inheritable_arrays != null) {
			for( int i = 0; i < inheritable_arrays.length; i++) {
				if( inheritable_arrays[i] != null) {
					for( int j = 0; j < inheritable_arrays.length; j++)
						inheritable_arrays[i][j].fromMultiLinear(it);
				}
			}
		}
		if( inheritables != null)
			for( int i = 0; i < inheritables.length; i++)
				inheritables[i].fromMultiLinear(it);
		if( trait_arrays != null)
			for( int i = 0; i < trait_arrays.length; i++)
				for( int j = 0; j < trait_arrays.length; j++)
					trait_arrays[i][j] = it.next();
		if( traits != null)
			for( int i = 0; i < traits.length; i++)
				traits[i] = it.next();
	}
    	
	public int[] toDNA() {
		return DNA;
	}
	
	public int fromDNA(int[] sourceDNA, int start) {
		Iterator<Trait> it = scalars.iterator();
		DNA = new int[scalars.size()];
		int i = 0;
		while( it.hasNext() && start < sourceDNA.length) {
			DNA[i] = sourceDNA[start++];
			it.next().fromDNA(DNA[i++]);
		}
		return start;
	}
	
}
