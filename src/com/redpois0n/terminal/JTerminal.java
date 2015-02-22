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
	
	public static final Font DEFAULT_FONT = new Font("Lucida Console", Font.PLAIN, 14);
	public static final Color DEFAULT_FOREGROUND = Color.white;
	public static final Color DEFAULT_BACKGROUND = Color.black;
	
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
		
		Arrays.fill(backgrounds, DEFAULT_BACKGROUND);
		Arrays.fill(foregrounds, DEFAULT_FOREGROUND);
		Arrays.fill(fonts, DEFAULT_FONT);
		Arrays.fill(chars, ' ');
		
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
		g.setColor(DEFAULT_BACKGROUND);
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
				
				Color background = backgrounds[i];

				int rx = getRealX(y);
				int ry = getRealY(x);	
				
				g.setColor(background);
				g.fillRect(rx, ry, charwidth, charheight);
			}
		}
		
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
						
				Color foreground = foregrounds[i];
				char c = chars[i];
				Font font = fonts[i];
				
				int rx = getRealX(y);
				int ry = getRealY(x);	
				
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
	
	public void expand() {
		this.rows++;
		
		Font[] nfonts = new Font[getTotal()];
		Color[] nforegrounds = new Color[getTotal()];
		Color[] nbackgrounds = new Color[getTotal()];
		char[] nchars = new char[getTotal()];
		
		System.arraycopy(fonts, 0, nfonts, fonts.length - 1, nfonts.length - fonts.length);
		System.arraycopy(foregrounds, 0, nforegrounds, foregrounds.length - 1, nforegrounds.length - foregrounds.length);
		System.arraycopy(backgrounds, 0, nbackgrounds, backgrounds.length - 1, nbackgrounds.length - backgrounds.length);
		System.arraycopy(chars, 0, nchars, chars.length - 1, nchars.length - chars.length);
		
		this.fonts = nfonts;
		this.foregrounds = nforegrounds;
		this.backgrounds = nbackgrounds;
		this.chars = nchars;
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
		if (cursory > 0) {
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
		if (cursorx - 1 >= 0) {
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
	
	public void delete() {				
		if (cursorx == 0 && cursory > 0) {
			cursorx = columns;
			cursory--;
		} else if (cursorx == 0 && cursory == 0) {
			return;
		} else {
			moveLeft();
		}
		
		int i = cursorx + cursory * columns;
		
		chars[i] = ' ';
		foregrounds[i] = DEFAULT_FOREGROUND;
		backgrounds[i] = DEFAULT_BACKGROUND;
		fonts[i] = DEFAULT_FONT;
		
		Font[] tfonts = new Font[getTotal()];
		Color[] tforegrounds = new Color[getTotal()];
		Color[] tbackgrounds = new Color[getTotal()];
		char[] tchars = new char[getTotal()];
		
		tfonts = fonts;
		tforegrounds = foregrounds;
		tbackgrounds = backgrounds;
		tchars = chars;
				
		for (int s = i; s < getTotal(); s++) {
			if (s + 1 >= fonts.length) {
				return;
			}
			tfonts[s] = fonts[s + 1];
			tforegrounds[s] = foregrounds[s + 1];
			tbackgrounds[s] = backgrounds[s + 1];
			tchars[s] = chars[s + 1];

		}
		
		fonts = tfonts;
		foregrounds = tforegrounds;
		backgrounds = tbackgrounds;
		chars = tchars;
		
		blinkThread.interrupt();
	}
	
	public void append(char c) {
		int i =  cursorx + cursory * columns;
		chars[i] = c;
				
		if (cursorx + 1 >= columns && cursory + 1 >= rows) {
			expand();
		} else if (cursorx + 1 >= columns) {
			cursory++;
			cursorx = 0;
		} else {
			moveRight();
		}
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
			} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				delete();
			} else {
				append(e.getKeyChar());
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
