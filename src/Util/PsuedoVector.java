package Util;

public class PsuedoVector {
	public float[] values = new float[10];
	public int last = 0;
	public int increment = 50;
	
	public int size() { return last; }
	
	public float get(int i) {
		return values[i];
	}
	
	public void add(float f) {
		if( values == null || last >= values.length)
			expand(increment);
		values[last++] = f;
	}
	public void expand() { expand(increment); }
	public void expand(int amt) {
		if( amt < 0)
			amt = -amt;
		if( values == null) {
			values = new float[amt];
			return;
		}
		float[] nv = new float[amt+values.length];
		for( int i = 0; i < last; i++)
			nv[i] = values[i];
		values = nv;
	}

}
