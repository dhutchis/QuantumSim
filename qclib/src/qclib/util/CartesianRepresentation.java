package qclib.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class CartesianRepresentation extends JFrame {
	private Cartesian cartesian;
	public Vector vector;
	public InitialStateLine initialStateLine;
	
    public CartesianRepresentation() {
        setTitle("Simulation visualization");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setSize(new Dimension(750, 750));
        
        this.vector = new Vector(0, 0);
        this.vector.setSize(700, 700);
        vector.setOpaque(false);
        add(this.vector);
        
        this.initialStateLine = new InitialStateLine(0, 0);
        this.initialStateLine.setSize(700, 700);
        initialStateLine.setOpaque(false);
        add(this.initialStateLine);
        
        this.cartesian = new Cartesian();
        this.cartesian.setSize(700, 700);
        cartesian.setOpaque(false);
        add(this.cartesian);
        
        setVisible(true);
    }

    public class Cartesian extends JPanel {
    	
    	@Override
        protected void paintComponent(Graphics g) {
    		super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setColor(Color.black);
            
            Line2D xCoordinateLine = new Line2D.Float(0, 350, 699, 350);
            Line2D yCoordinateLine = new Line2D.Float(350, 0, 350, 699);
            g2d.draw(xCoordinateLine);
            g2d.draw(yCoordinateLine);
        }
    }

    public class Vector extends JPanel{
        protected double x, y;
        
        public Vector(double x, double y){
        	this.setComponents(x, y);
        }
        
        public void setComponents(double x, double y) {
            assert y <= 1;
            assert x <= 1;
            assert y >= -1;
            assert x >= -1;
            this.x = x;
            this.y = y;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.red);
            Line2D vector = new Line2D.Float(350, 350, Math.round(350*(1+this.x)), Math.round(350*(1-this.y)));
            g2d.draw(vector);
        }
    }
    
    public class InitialStateLine extends Vector{
    	
    	public InitialStateLine(double x, double y) {
			super(x, y);
		}

		@Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.blue);
            Line2D vector = new Line2D.Float(Math.round(350*(1-this.x)), Math.round(350*(1+this.y)), Math.round(350*(1+this.x)), Math.round(350*(1-this.y)));
            g2d.draw(vector);
        }
    }
}
