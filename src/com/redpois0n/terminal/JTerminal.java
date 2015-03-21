package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JTerminal extends JComponent {
	
	public static final Font DEFAULT_FONT;
	public static final Color DEFAULT_FOREGROUND = Color.white;
	public static final Color DEFAULT_BACKGROUND = Color.black;
	public static final char NULL_CHAR = '\u0000';

	static {		
		DEFAULT_FONT = new Font("Lucida Console", Font.PLAIN, 14);
	}
	
	private List<InputListener> inputListeners = new ArrayList<InputListener>();
	private List<SizeChangeListener> sizeChangeListeners = new ArrayList<SizeChangeListener>();
	
	private List<Font> fonts;
	private List<Color> foregrounds;
	private List<Color> backgrounds;
	private List<Character> chars;
	
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
	private boolean shouldScrollUp;
	private boolean ctrl;
	
	private int block;
	
	private int select1;
	private int select2;
	
	public JTerminal() {
		this.repaintThread = new Thread(new RepaintRunnable());
		
		clear();
		
		this.charwidth = 8;
		this.charheight = 15;
				
		toggleBlink();
		
		super.addKeyListener(new KeyEventListener());
		super.addMouseListener(new MouseEventListener());
		super.addMouseMotionListener(new MouseEventListener());
		
		setSize();
	}
	
	/**
	 * Resets whole terminal
	 */
	public void clear() {
		this.columns = 80;
		this.rows = 24;
		
		this.fonts = new ArrayList<Font>();
		this.foregrounds = new ArrayList<Color>();
		this.backgrounds = new ArrayList<Color>();
		this.chars = new ArrayList<Character>();
		
		fill(chars, getTotal(), NULL_CHAR);
		fill(backgrounds, getTotal(), DEFAULT_BACKGROUND);
		fill(foregrounds, getTotal(), DEFAULT_FOREGROUND);
		fill(fonts, getTotal(), DEFAULT_FONT);
		
		this.cursorx = 0;
		this.cursory = 0;
		setBlockAtCurrentPos();
		
		setSize();
		shouldScroll = true;
		shouldScrollUp = true;
	}
	
	/**
	 * Gets main key listener
	 * @return
	 */
	public KeyListener getKeyListener() {
		return super.getKeyListeners()[0];
	}
	
	/**
	 * Called when size changed, notifies change listeners and sets preferred size
	 */
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
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(DEFAULT_BACKGROUND);
		g.fillRect(0, 0, getRealX(columns), getRealY(rows));

		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
				
				if (i >= backgrounds.size()) {
					break;
				}
				
				Color background = backgrounds.get(i);

				int rx = getRealX(y);
				int ry = getRealY(x);	
				
				if (i > select1 && i < select2) {
					g.setColor(Color.white);
				} else {
					g.setColor(background);
				}
				
				g.fillRect(rx, ry, charwidth, charheight);
			}
		}
		
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
						
				if (i >= foregrounds.size()) {
					break;
				}
				
				Color foreground = foregrounds.get(i);
				char c = chars.get(i);
				Font font = fonts.get(i);
				
				int rx = getRealX(y);
				int ry = getRealY(x);	
				
				if (c == NULL_CHAR) {
					continue;
				}
				
				if (i > select1 && i < select2) {
					g.setColor(Color.black);
				} else {
					g.setColor(foreground);
				}

				if (font != null) {
					g.setFont(font);
				}
				
				g.drawString(Character.toString(c), rx, ry + charheight - 2);
			}
		}
		
		
		if (blinking) {
			int i = cursorx + cursory * columns;
			if (i > select1 && i < select2) {
				g.setColor(Color.black);
			} else {
				g.setColor(Color.white);
			}
			g.fillRect(getRealX(cursorx), getRealY(cursory), charwidth, charheight);
		}
		
	}
	
	/**
	 * Adds a row to bottom
	 */
	public void expand() {
		int total = getTotal();
		
		this.rows++;
		
		for (int i = total; i <= getTotal(); i++) {
			fonts.add(DEFAULT_FONT);
			foregrounds.add(DEFAULT_FOREGROUND);
			backgrounds.add(DEFAULT_BACKGROUND);
			chars.add(NULL_CHAR);
		}
		
		cursorx = 0;
		cursory++;
		
		setSize();			
	}
	
	/**
	 * Gets total size of lists
	 * @return
	 */
	public int getTotal() {
		return columns + rows * columns;
	}
	
	/**
	 * Gets on screen position for specified x
	 * @param x
	 * @return x * character width
	 */
	public int getRealX(int x) {
		return x * charwidth;
	}
	
	/**
	 * Gets on screen position for specified y
	 * @param y
	 * @return y * character height
	 */
	public int getRealY(int y) {
		return y * charheight;
	}
	
	/**
	 * Toggles cursor blinking
	 */
	public void toggleBlink() {
		blinkcursor = !blinkcursor;
		
		if (blinkcursor) {
			repaintThread.start();
		} else {
			repaintThread.interrupt();
		}
	}
	
	/**
	 * Moves cursor up
	 */
	public void moveUp() {
		if (cursory > 0 && cursorx + (cursory - 1) * columns >= block) {
			cursory--;
		}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor down
	 */
	public void moveDown() {	
		if (cursory + 1 < rows) {
			cursory++;
		}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor to the left
	 */
	public void moveLeft() {
		if (cursorx - 1 >= 0 && cursorx - 1 + cursory * columns >= block) {
			cursorx--;
		}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor to the right
	 */
	public void moveRight() {
		if (cursorx + 1 < columns) {
			cursorx++;
		}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Delete character at current cursor position
	 */
	public void delete() {
		delete(cursorx, cursory);
	}
	
	/**
	 * Deletes character at specified position
	 * @param x
	 * @param y
	 */
	public void delete(int x, int y) {				
		if (cursorx == 0 && cursory > 0) {
			cursorx = columns - 1;
			cursory--;
		} else if (cursorx == 0 && cursory == 0) {
			return;
		} else {
			moveLeft();
		}
		
		int i = x + y * columns;
		
		if (i + 1 == block) {
			return;
		}

		fonts.remove(i);
		foregrounds.remove(i);
		backgrounds.remove(i);
		chars.remove(i);
		
		repaintThread.interrupt();
	}
	
	/**
	 * Append character
	 * @param c
	 */
	public void append(char c) {
		insert(c, cursorx, cursory, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_FONT);
	}
	
	/**
	 * Append character
	 * @param c
	 * @param foreground
	 * @param background
	 * @param font
	 */
	public void append(char c, Color foreground, Color background, Font font) {
		insert(c, cursorx, cursory, foreground, background, font);
	}
	
	/**
	 * Append string
	 * @param s
	 */
	public void append(String s) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i));
		}
	}
	
	/**
	 * Append string
	 * @param s
	 * @param foreground
	 * @param background
	 * @param font
	 */
	public void append(String s, Color foreground, Color background, Font font) {
		for (int i = 0; i < s.length(); i++) {
			append(s.charAt(i), foreground, background, font);
		}
	}
	
	/**
	 * Inserts character at x+y*columns
	 * @param c
	 * @param x
	 * @param y
	 * @param foreground
	 * @param background
	 * @param font
	 */
	public void insert(char c, int x, int y, Color foreground, Color background, Font font) {
		if (Character.toString(c).equals(System.getProperty("line.separator")) || c == '\n') {
			enter(false);
			return;
		}
		int i = x + y * columns;

		if (x + 1 >= columns && y + 1 >= rows) {
			expand();
		} else if (x + 1 >= columns) {
			cursory++;
			cursorx = 0;
		} else {
			moveRight();
		}
				
		chars.add(i, c);
		foregrounds.add(i, foreground);
		backgrounds.add(i, background);
		fonts.add(i, font);		

		repaintThread.interrupt();
		shouldScroll = true;
	}
	
	/**
	 * Get where current typed text ends
	 * @return
	 */
	public int getTypedEnd() {				
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				int i = y + x * columns;
				
				if (chars.get(i) == NULL_CHAR) {
					return i;
				} 
				
				if (i < block) {
					continue;
				}	
			}
		}
		
		return -1;
	}
	
	/**
	 * Called when enter is pressed (newline)
	 * @param press If enter actually is pressed or if just a new line should be added
	 */
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
				
				sb.append(chars.get(i));
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
				l.processCommand(this, sb.toString().trim());
			}
		}
	}
	
	/**
	 * Called when key pressed, checks if character is valid and checks for combinations such as Ctrl+C
	 * @param e
	 */
	public void keyPressed(KeyEvent e) {
		char c = e.getKeyChar();
		
		if (ctrl && e.getKeyCode() == KeyEvent.VK_C) {
			for (InputListener l : inputListeners) {
				l.onTerminate(this);
			}
			return;
		}
		
		append(c);
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
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT && chars.get(cursorx + cursory * columns) != NULL_CHAR) {
				moveRight();
			} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				delete();
			} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter(true);
			} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				ctrl = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				ctrl = false;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {		
			if ((int) e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
				JTerminal.this.keyPressed(e);
			}
		}
	}
	
	public class MouseEventListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX() / charwidth;
			int y = e.getY() / charheight;
			
			int i = x + y * columns;

			select1 = i;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			int x = e.getX() / charwidth;
			int y = e.getY() / charheight;
			
			int i = x + y * columns;

			select2 = i;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX() / charwidth;
			int y = e.getY() / charheight;
			
			int i = x + y * columns;

			select2 = i;
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
	
	/**
	 * Sets block at current position (good after user pressed enter and new line text has been appended)
	 */
	public void setBlockAtCurrentPos() {
		this.block = cursorx + cursory * columns;
	}

	/**
	 * Checks if should scroll to bottom, and automatically changes the variable to the opposite
	 * @return
	 */
	public boolean scrollToBottom() {
		boolean b = shouldScroll;
		
		if (b) {
			shouldScroll = !shouldScroll;
		}
		return b;
	}
	
	/**
	 * Checks if should scroll to top, and automaticalyl changes the variable to the opposite
	 * @return
	 */
	public boolean scrollUp() {
		boolean b = shouldScrollUp;
		
		if (b) {
			shouldScrollUp = !shouldScrollUp;
		}
		
		return b;
	}
	
	/**
	 * Fills list with specified value
	 * @param list
	 * @param size Fills from index 0 to size - 1
	 * @param t Value to fill with
	 */
	public <T> void fill(List<T> list, int size, T t) {
		for (int i = 0; i < size; i++) {
			list.add(t);
		}
	}
	
	/**
	 * Repaint thread
	 */
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
