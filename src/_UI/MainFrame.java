package _UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import World.*;
import Entities.*;
import Genetics.*;

public class MainFrame extends JFrame {

	private JPanel jContentPane = null;
	
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	public static void createAndShowGUI() {
		MainFrame mf = new MainFrame();
		mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mf.setVisible(true);
	}
	
	public  MainFrame() {
		super();
		Time.init();
		Time.initWorld();
		
		initialize();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				super.keyPressed(ev);
				char c = ev.getKeyChar();
				if( c == 'g')
					Climate.changeDisplayMode();
				if( c == 's')
					Organism.changeDisplayMode();
				if( c == 'r') {
					Time.world.climate.soil.refresh();
					Time.world.climate.meat.refresh();
					Time.world.climate.meds.refresh();
				}
			}
			
		});
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(1200, 700);
		this.setContentPane(getJContentPane());
		this.setTitle("SWARMS");
	}
	public JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			Time.world.view.setBounds(5,5,1366,768);
			jContentPane.add(Time.world.view,null);
			Time t = new Time();
			t.start_time(120, 16);
			t.start_draw_timer(120);
			//jContentPane.add(jButton3, null);
			//jContentPane.add(jButton4, null);
		}
		return jContentPane;
	}
		
		//c.setPreferredSize(new Dimension(800,600));
		//c.setSize(new Dimension(800,600));
		//c.setLayout(null);
}
