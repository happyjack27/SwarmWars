package Player;
import java.util.*;
import java.awt.*;

import Entities.*;
import _UI.*;

public class Player {
	
	public void draw(Graphics g, int x, int y) {
		g.setColor(Color.black);
		g.drawString(""+id+": "+num_organisms, x, y);
		if( id < 3)
			b.draw(g);
	}
	public int num_organisms = 0;
	public int id = 0;
	public String name = "";
	public Vector<Player> allies;
	public Vector<Player> neutral;
	public Vector<Organism> organisms;
	public BugBar b;
	
	public Player() {
		super();
		organisms = new Vector<Organism>();
		allies = new Vector<Player>();
		neutral = new Vector<Player>();
		b = new BugBar();
		b.owner = this;
	}
	
	public static float getAlignment(Organism org1, Organism org2) {
		Player o1 = org1.owner;
		Player o2 = org2.owner;
		if( o1 == null || o2 == null)
			return 0;
		if( o1 == o2)
			return 1;
		if( o1.allies != null) {
			Iterator<Player> allies = o1.allies.iterator();
			while( allies.hasNext())
				if( o2 == allies.next())
					return 1;
		}
		if( o1.neutral != null) {
			Iterator<Player> neutral = o1.neutral.iterator();
			while( neutral.hasNext())
				if( o2 == neutral.next())
					return 0;
		}
		return -1;
	}
	
}
