
package Genetics;

import java.util.Iterator;

public interface iInheritable<Obj extends Trait> {
	public void initializeMembers();
	public void collectTraits();
	public void collectMultiLinear();
	public void fromMultiLinear(Iterator<Obj> it);
}
