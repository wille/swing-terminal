package com.redpois0n.console;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Arrays;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JConsole extends JComponent {
	
	private Font[] fonts;
	private Color[] foregrounds;
	private Color[] backgrounds;
	private char[] chars;
	
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
		this.foregrounds = new Color[getTotal()];
		this.backgrounds = new Color[getTotal()];
		this.chars = new char[getTotal()];
		
		Arrays.fill(backgrounds, Color.black);
		Arrays.fill(foregrounds, Color.white);
		Font f = new Font("Lucida Console", Font.PLAIN, 14);
		Arrays.fill(fonts, f);
		Arrays.fill(chars, 'a');

		this.charwidth = 8;
		this.charheight = 15;
		
		toggleBlink();
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * y;
						
				Color background = backgrounds[i];
				Color foreground = foregrounds[i];
				char c = chars[i];
				Font font = fonts[i];
				
				int rx = getRealX(y);
				int ry = getRealY(x);
				
				g.setColor(background);
				g.fillRect(rx, ry, charwidth, charheight);
				
				g.setColor(foreground);
				g.setFont(font);
				g.drawString(Character.toString(c), rx, ry + charheight);
			}
		}
		
		
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
