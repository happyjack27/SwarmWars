package Genetics;

/*
 * need to make trait instantiation templates:
 * float scale;
 * float bias;
 * boolean isLogarithmic;
 */
public class Trait {
	public float value = 0;
	public Trait() {
		super();
	}
	public Trait(float f) {
		super();
		value = f;
	}
	
	public float getFloat() {
		return value;
	}
	
	public Trait copy() {
		Trait c = new Trait();
		c.value = value;
		return c;
	}
	
	public static float squash(float s) {
		return (float)(2.0/(1+Math.exp(-s)) - 1.0);
	}
	public static float rbf(float s) {
		return (float)Math.exp(-s*s);
	}

	//returns a float of range 0-1 from the number of bits set in the int.
	public void fromDNA(int source) {
		//DNA = source;
		value = floatFromDNA(source);//(float)bitcount / (float)32;
	}
	public static float floatFromDNA(int source) {
		//DNA = source;
		int bitcount = numberOfSetBits(source);
		return (float)bitcount / (float)32;
	}

	public static int numberOfSetBits(int i) {
	    i = i - ((i >> 1) & 0x55555555);
	    i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
	    return (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
	}
	
}
