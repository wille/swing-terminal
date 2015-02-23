package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JTerminal extends JComponent {
	
	public static final Font DEFAULT_FONT = new Font("Lucida Console", Font.PLAIN, 14);
	public static final Color DEFAULT_FOREGROUND = Color.white;
	public static final Color DEFAULT_BACKGROUND = Color.black;
	
	private List<InputListener> inputListeners = new ArrayList<InputListener>();
	private List<SizeChangeListener> sizeChangeListeners = new ArrayList<SizeChangeListener>();
	
	private Font[] fonts;
	private Color[] foregrounds;
	private Color[] backgrounds;
	private char[] chars;
	
	private Thread repaintThread;
	
	private int columns;
	private int rows;
	
	private int charwidth;
	private int charheight;
	
	private boolean blinking;
	private boolean blinkcursor;
	
	private int cursorx;
	private int cursory;
	
	private boolean shouldScroll;
	
	private int block;
		
	public JTerminal() {
		this.repaintThread = new Thread(new RepaintRunnable());
		
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
		
		setSize();
	}
	
	public KeyListener getKeyListener() {
		return super.getKeyListeners()[0];
	}
	
	public void setSize() {
		int width = getRealX(columns);
		int height = getRealY(rows);
		
		super.setPreferredSize(new Dimension(width, height));
		
		for (SizeChangeListener l : sizeChangeListeners) {
			l.sizeChange(this, width, height);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(DEFAULT_BACKGROUND);
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
				
				if (i >= backgrounds.length) {
					break;
				}
				
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
						
				if (i >= foregrounds.length) {
					break;
				}
				
				Color foreground = foregrounds[i];
				char c = chars[i];
				Font font = fonts[i];
				
				int rx = getRealX(y);
				int ry = getRealY(x);	
				
				g.setColor(foreground);
				g.setFont(font);
				g.drawString(Character.toString(c), rx, ry + charheight - 2);
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
		
		System.arraycopy(fonts, 0, nfonts, 0, fonts.length);
		System.arraycopy(foregrounds, 0, nforegrounds, 0, foregrounds.length);
		System.arraycopy(backgrounds, 0, nbackgrounds, 0, backgrounds.length);
		System.arraycopy(chars, 0, nchars, 0, chars.length);		
		
		this.fonts = nfonts;
		this.foregrounds = nforegrounds;
		this.backgrounds = nbackgrounds;
		this.chars = nchars;
		
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == '\u0000') {
				chars[i] = ' ';
			}
		}
		
		cursorx = 0;
		cursory++;
		
		setSize();			
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
			repaintThread.start();
		} else {
			repaintThread.interrupt();
		}
	}
	
	public void moveUp() {
		if (cursory > 0 && cursorx + (cursory - 1) * columns >= block) {
			cursory--;
		}
		
		repaintThread.interrupt();
	}
	
	public void moveDown() {	
		if (cursory + 1 < rows) {
			cursory++;
		}
		
		repaintThread.interrupt();
	}
	
	public void moveLeft() {
		if (cursorx - 1 >= 0 && cursorx - 1 + cursory * columns >= block) {
			cursorx--;
		}
		
		repaintThread.interrupt();
	}
	
	public void moveRight() {
		if (cursorx + 1 < columns) {
			cursorx++;
		}
		
		repaintThread.interrupt();
	}
	
	public void delete() {
		delete(cursorx, cursory);
	}
	
	public void delete(int x, int y) {		
		boolean b = x == cursorx && y == cursory;
		
		if (cursorx == 0 && cursory > 0) {
			cursorx = columns - 1;
			cursory--;
		} else if (cursorx == 0 && cursory == 0) {
			return;
		} else {
			moveLeft();
		}
		
		int i;
		
		if (b) {
			i = cursorx + cursory * columns;
		} else {
			i = x + y * columns;
		}
		
		if (i + 1 == block) {
			return;
		}
		
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
		
		repaintThread.interrupt();
	}
	
	public void append(char c) {
		insert(c, cursorx, cursory, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_FONT);
	}
	
	public void append(char c, Color foreground, Color background, Font font) {
		insert(c, cursorx, cursory, foreground, background, font);
	}
	
	public void append(String s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
	}
	
	public void append(String s, Color foreground, Color background, Font font) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i), foreground, background, font);
		}
	}
	
	public void insert(char c, int x, int y, Color foreground, Color background, Font font) {
		if (Character.toString(c).equals(System.getProperty("line.separator")) || c == '\n') {
			enter(false);
			return;
		}
		int i =  x + y * columns;

		if (x + 1 >= columns && y + 1 >= rows) {
			expand();
		} else if (x + 1 >= columns) {
			cursory++;
			cursorx = 0;
		} else {
			moveRight();
		}
		
		List<Character> tchars = new ArrayList<Character>();
		for (char cc : chars) {
			tchars.add(cc);
		}
		
		List<Color> tforegrounds = new ArrayList<Color>(Arrays.asList(foregrounds));
		List<Color> tbackgrounds = new ArrayList<Color>(Arrays.asList(backgrounds));
		List<Font> tfonts = new ArrayList<Font>(Arrays.asList(fonts));
		
		tchars.add(i, c);
		tforegrounds.add(i, foreground);
		tbackgrounds.add(i, background);
		tfonts.add(i, font);
		
		chars = new char[tchars.size()];
		int s = 0;
		for (Character cc : tchars) {
			chars[s++] = cc;
		}
		
		foregrounds = tforegrounds.toArray(new Color[0]);
		backgrounds = tbackgrounds.toArray(new Color[0]);
		fonts = tfonts.toArray(new Font[0]);

		repaintThread.interrupt();
		shouldScroll = true;
	}
	
	public void enter(boolean press) {
		int latestBlock = block;
		block = cursorx + cursory * columns;
		
		StringBuilder sb = new StringBuilder();
		
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
				
				if (i < latestBlock) {
					continue;
				}
				
				sb.append(chars[i]);
			}
		}
		
		if (cursory + 1 >= rows) {
			expand();
		} else {
			cursory++;
			cursorx = 0;
		}
		
		repaintThread.interrupt();
		shouldScroll = true;
		
		if (press) {
			for (InputListener l : inputListeners) {
				l.processCommand(this, sb.toString());
			}
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
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter(true);
			} else if (Character.isAlphabetic(e.getKeyChar()) || Character.isDigit(e.getKeyChar()) || Character.isSpaceChar(e.getKeyChar())) {
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
	
	public void addInputListener(InputListener listener) {
		inputListeners.add(listener);
	}
	
	public void removeInputListener(InputListener listener) {
		inputListeners.remove(listener);
	}
	
	public void addSizeChangeListener(SizeChangeListener listener) {
		sizeChangeListeners.add(listener);
	}
	
	public void removeSizeChangeListener(SizeChangeListener listener) {
		sizeChangeListeners.remove(listener);
	}
	
	public void setBlockAtCurrentPos() {
		this.block = cursorx + cursory * columns;
	}

	public boolean scrollToBottom() {
		boolean b = shouldScroll;
		
		if (b) {
			shouldScroll = !shouldScroll;
		}
		return b;
	}
	
	public class RepaintRunnable implements Runnable {
		@Override
		public void run() {
			while (true) {
				JTerminal.this.repaint();

				if (blinkcursor) {
					blinking = !blinking;			
				}
				
				try {
					Thread.sleep(600L);
				} catch (InterruptedException e) {
					
				}
			}
			
		}		
	}

}
