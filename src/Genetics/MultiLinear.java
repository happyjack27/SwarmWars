package Genetics;
import java.util.*;

public class MultiLinear<Obj extends Trait> {
	public Vector<Obj> scalars = new Vector<Obj>();
	public void printout() { printout(scalars.size()); }
	public void printout(int num) {
		//num = 0;
		int m = 0;
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext()) {
			Trait t = it.next();
			if( Float.isInfinite(t.value)) {
				if( m < num)
					System.out.println(m+": infinite: "+t.value);
				t.value = 0;
			} else if( Float.isNaN(t.value)) {
				if( m < num)
					System.out.println(m+": nan: "+t.value);
				t.value = 0;
			} else if( t.value != t.value) {
				if( m < num)
					System.out.println(m+": "+t.value);
				t.value = 0;
			} else
				if( m < num)
					System.out.println(m+": "+t.value);
			m++;
		}
		
	}
	
	public void reset() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			it.next().value = 0;
	}

	public void set() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			it.next().value = 1;
	}

	
	public void multiply_inner(MultiLinear<Obj> source) {
		Iterator<Obj> it = scalars.iterator();
		Iterator<Obj> it2 = source.scalars.iterator();
		while( it.hasNext() && it2.hasNext())
			it.next().value *= it2.next().value;
	}
	public void add(MultiLinear<Obj> source) {
		Iterator<Obj> it = scalars.iterator();
		Iterator<Obj> it2 = source.scalars.iterator();
		while( it.hasNext() && it2.hasNext())
			it.next().value += it2.next().value;
	}
	public void addScaled(MultiLinear<Obj> source, float scale) {
		Iterator<Obj> it = scalars.iterator();
		Iterator<Obj> it2 = source.scalars.iterator();
		while( it.hasNext() && it2.hasNext())
			it.next().value += it2.next().value*scale;
	}
	public void recipricol() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext()) {
			Obj t = it.next(); 
			t.value = (float)(1.0/t.value);
		}
	}
	public void sqrt() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext()) {
			Obj t = it.next(); 
			t.value = (float)(Math.sqrt(t.value));
		}
	}
	public void rsqrt() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext()) {
			Obj t = it.next(); 
			t.value = (float)(1.0/Math.sqrt(t.value));
		}
	}
	public void scale(float scale) {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			it.next().value *= scale;
	}
	public void add(float scale) {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			it.next().value += scale;
	}
	public float total() {
		float total = 0;
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			total += it.next().value;
		return total;
	}
	public void copyFrom(MultiLinear<Obj> source) {
		Iterator<Obj> it = scalars.iterator();
		Iterator<Obj> it2 = source.scalars.iterator();
		while( it.hasNext() && it2.hasNext()) {
			it.next().value = it2.next().value;
		}
	}
	public void squash() {
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext()) {
			Obj obj = it.next();
			obj.value = Obj.squash(obj.value);
		}
		
	}
	
	/*
	public MultiLinear<Scalar> copy() {
		MultiLinear<Scalar> copy = new MultiLinear<Scalar>();
		Iterator<Obj> it = scalars.iterator();
		while( it.hasNext())
			copy.scalars.add(it.next().copy());
		return copy;
	}*/

}
