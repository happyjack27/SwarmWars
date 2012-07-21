package Genetics;

import java.util.*;

public class GenomeTemplate {
	Genome bias;
	Genome scale;
	Genome function; //linear or logarithmic 0 = linear, 1 = logarithmic
	
	public GenomeTemplate(Genome bias, Genome scale, Genome function) {
		super();
		this.bias = bias;
		this.scale = scale;
		this.function = function;
		bias.initAsBiasTemplate();
		scale.initAsScaleTemplate();
		function.initAsFunctionTemplate();
	}
	
	public void apply(Genome g) {
		
		g.add(bias);
		g.multiply_inner(scale);
		double base2 = Math.log(2.0);
		
		//now apply function.
		Iterator<Trait> it = g.scalars.iterator();
		Iterator<Trait> itt = function.scalars.iterator();
		while( it.hasNext() && itt.hasNext()) {
			Trait t = it.next();
			Trait t_funct = itt.next();
			if( t_funct.value == 1) {
				t.value = (float)Math.exp(t.value*base2);
			}
		}
		//.. now apply function
		
	}

}
