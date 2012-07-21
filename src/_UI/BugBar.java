package _UI;
import Entities.*;
import Player.*;

import java.awt.*;


public class BugBar {
	public Player owner;
	public void draw(Graphics g) {
		if( owner == null)
			return;
		Color bar = new Color(192,192,192,192);
		g.setColor(bar);
		g.fillRect(200, owner.id*75, 940/*owner.organisms.size()*40+40*/, 75);
		for( int i = 0; i < owner.organisms.size() && i < 25; i++) {
			Organism o = owner.organisms.get(i);
			float x = o.x; 
			float y = o.y; 
			float size = o.body.current_size.value;
			//o.body.setSize(o.body.adult_size.value);
			o.x = i * 50+240;
			o.y = 50+owner.id*75;
			o.draw(g);
			//o.body.setSize(size);
			o.x = x;
			o.y = y;
		}
	}

}
