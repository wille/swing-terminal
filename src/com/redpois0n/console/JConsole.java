package com.redpois0n.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JConsole extends JComponent {
	
	private final Font[] fonts;
	private final Color[] foreground;
	private final Color[] background;
	private final char[] chars;
	
	private int columns;
	private int rows;
	
	private int charwidth;
	private int charheight;
	
	private boolean blinking;
	private boolean blinkcursor;
	
	private int cursorrow;
	private int cursorcolumn;
		
	public JConsole() {
		this.columns = 80;
		this.rows = 24;
		
		this.fonts = new Font[getTotal()];
		this.foreground = new Color[getTotal()];
		this.background = new Color[getTotal()];
		this.chars = new char[getTotal()];

		this.charwidth = 8;
		this.charheight = 15;
		
		toggleBlink();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));
		
		if (blinking) {
			g.setColor(Color.white);
			g.fillRect(getRealX(cursorrow), getRealY(cursorcolumn), charwidth, charheight);
		}
	}
	
	public int getTotal() {
		return columns + rows * columns;
	}
	
	public int getRealX(int x) {
		return x * charwidth;
	}
	
	public int getRealY(int y) {
		return y * charheight;
	}
	
	public void toggleBlink() {
		blinkcursor = !blinkcursor;
		
		if (blinkcursor) {
			new Thread(new BlinkRunnable()).start();
		}
	}
	
	public class BlinkRunnable implements Runnable {
		@Override
		public void run() {
			try {
				while (blinkcursor) {
					blinking = !blinking;
					JConsole.this.repaint();
					Thread.sleep(600L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}
	

}
