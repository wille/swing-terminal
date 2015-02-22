package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JTerminal extends JComponent {
	
	private Font[] fonts;
	private Color[] foregrounds;
	private Color[] backgrounds;
	private char[] chars;
	
	private Thread blinkThread;
	
	private int columns;
	private int rows;
	
	private int charwidth;
	private int charheight;
	
	private boolean blinking;
	private boolean blinkcursor;
	
	private int cursorx;
	private int cursory;
		
	public JTerminal() {
		this.blinkThread = new Thread(new BlinkRunnable());
		
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
		
		super.addKeyListener(new KeyEventListener());
	}
	
	public KeyListener getKeyListener() {
		return super.getKeyListeners()[0];
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
			g.fillRect(getRealX(cursorx), getRealY(cursory), charwidth, charheight);
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
			blinkThread.start();
		} else {
			blinkThread.interrupt();
		}
	}
	
	public void moveUp() {
		if (cursory - 1 > 0) {
			cursory--;
		}
		
		blinkThread.interrupt();
	}
	
	public void moveDown() {	
		if (cursory + 1 < rows) {
			cursory++;
		}
		
		blinkThread.interrupt();
	}
	
	public void moveLeft() {
		if (cursorx - 1 > 0) {
			cursorx--;
		}
		
		blinkThread.interrupt();
	}
	
	public void moveRight() {
		if (cursorx + 1 < columns) {
			cursorx++;
		}
		
		blinkThread.interrupt();
	}
	
	public class KeyEventListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				moveUp();
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				moveDown();
			} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				moveLeft();
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				moveRight();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void keyTyped(KeyEvent e) {
			
		}
		
	}
	
	public class BlinkRunnable implements Runnable {
		@Override
		public void run() {
			while (blinkcursor) {
				blinking = !blinking;
				JTerminal.this.repaint();
				try {
					Thread.sleep(600L);
				} catch (InterruptedException e) {
					
				}
			}
			
		}		
	}
	

}
