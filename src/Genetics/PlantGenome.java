package Genetics;

public class PlantGenome extends Genome {
	public static GenomeTemplate template;

	public Trait breed_distance;
	public Trait growth_rate; //this also deterimes soil needs and reproduction rate
	public Trait breed_cycles_per_life;
	public Trait seed_distance;
	public Trait temperature_preference;
	public Trait humidity_preference;
	public Trait hue; //+=blue, -=yellow
	public Trait aspect_ratio;
	
	public PlantGenome() {
		super();
		if( template == null) {
			template = new GenomeTemplate(new PlantGenome(true),new PlantGenome(true),new PlantGenome(true));
		}
	}
	public PlantGenome(boolean is_template) {
		super();
	}
	public int fromDNA(int[] sourceDNA, int start) {
		int ret = super.fromDNA(sourceDNA, start);
		template.apply(this);
		return ret;
	}
	
	public void initializeMembers() {
		breed_distance = new Trait();
		growth_rate = new Trait();
		breed_cycles_per_life = new Trait();
		seed_distance = new Trait();
		temperature_preference = new Trait();
		humidity_preference = new Trait();
		hue = new Trait();
		aspect_ratio = new Trait();
	}
	
    public void collectTraits() {
    	traits = new Trait[]{
    			breed_distance,
    			growth_rate,
    			breed_cycles_per_life,
    			seed_distance,
    			temperature_preference,
    			humidity_preference,
    			hue
    	};
    }

	public Trait[] getSignedTraits() {
		return new Trait[]{
    			temperature_preference,
    			humidity_preference,
    			hue
		};
	}
	
	public Trait[] getNegativeLogarithmicTraits() {
		return new Trait[]{
		};
	}
	
	public Trait[] getSignedLogarithmicTraits() {
		return new Trait[]{
    			breed_distance,
    			growth_rate,
    			breed_cycles_per_life,
    			seed_distance,
    			aspect_ratio,
		};
	}
	
	public Inheritable[] getSignedInheritables() {
		return new Inheritable[]{
		};
	}
	
	public Inheritable[] getSignedLogarithmicInheritables() {
		return new Inheritable[]{
		};
	}
	
	public Inheritable[] getPositiveLogarithmicInheritables() {
		return new Inheritable[]{
		};
	}
	
	public Inheritable[] getNegativeLogarithmicInheritables() {
		return new Inheritable[]{
		};
	}
}