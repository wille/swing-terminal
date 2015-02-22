package com.redpois0n.console;

import java.awt.Graphics;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JConsole extends JComponent {
	
	private int columns;
	private int rows;
	
	private int charwidth;
	private int charheight;
	
	private boolean blinking;
	private boolean blinkCursor;
		
	public JConsole() {
		this.columns = 80;
		this.rows = 30;
		
		this.charwidth = 8;
		this.charheight = 12;
		
		this.blinkCursor = true;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));
	}
	
	public int getRealX(int x) {
		return x * charwidth;
	}
	
	public int getRealY(int y) {
		return y * charheight;
	}
	
	public void toggleBlink() {
		blinkCursor = !blinkCursor;
		
		if (blinkCursor) {
			new Thread(new BlinkRunnable()).start();
		}
	}
	
	public class BlinkRunnable implements Runnable {
		@Override
		public void run() {
			try {
				while (blinkCursor) {
					blinking = !blinking;
					JConsole.this.repaint();
					Thread.sleep(300L);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}
	

}
