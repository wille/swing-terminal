package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

@SuppressWarnings("serial")
public class JTerminal extends JComponent {
	
	public static final Font DEFAULT_FONT;
	public static final Color DEFAULT_FOREGROUND = Color.white;
	public static final Color DEFAULT_BACKGROUND = Color.black;
	public static final char NULL_CHAR = '\u0000';
	
	public static final char ESCAPE = 27;
	public static final String UNIX_CLEAR = ESCAPE + "[H" + ESCAPE + "[J";

	static {		
		DEFAULT_FONT = new Font("Lucida Console", Font.PLAIN, 14);
	}
	
	private List<InputListener> inputListeners = new ArrayList<InputListener>();
	private List<SizeChangeListener> sizeChangeListeners = new ArrayList<SizeChangeListener>();
	
	private List<String> lines = new ArrayList<String>();
	
	private Thread repaintThread;

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
	
	private JPopupMenu menu;
	
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
		
		menu = new JPopupMenu();
		
		JMenuItem miCopy = new JMenuItem("Copy");
		miCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				setClipboard(getSelection());
			}	
		});
		menu.add(miCopy);
		
		JMenuItem miCut = new JMenuItem("Cut");
		miCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				
			}	
		});
		menu.add(miCut);
		
		JMenuItem miPaste = new JMenuItem("Paste");
		miCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				
			}	
		});
		menu.add(miPaste);
		
		addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				if (command.equalsIgnoreCase("clear") || command.equalsIgnoreCase("cls")) {
					//terminal.clear();
					//return;
				}

				terminal.setBlockAtCurrentPos();
			}
		});
	}
	
	/**
	 * Gets currently marked text selection
	 * @return
	 */
	public String getSelection() {
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
	}
	
	/**
	 * Resets whole terminal
	 */
	public void clear() {
		this.lines.clear();
		
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
		
	}

	@Override
	public void paintComponent(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(DEFAULT_BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		FontMetrics fm = g.getFontMetrics();
		
		int x = 0;
		int y = fm.getHeight();
		
		for (int i = 0; i < lines.size(); i++) {
			String s = lines.get(i);
			
			List<String> ls = new ArrayList<String>();
			
			int td = s.length(); // to draw
			
			while (true) {
				String t = null;
				while (td > 0 && fm.stringWidth((t = s.substring(0, td--))) > getWidth());
				
				if (td <= 0) {
					break;
				}
				
				s = s.substring(td, s.length());
				td = s.length();
								
				if (s.length() == 0) {
					break;
				}
				
				ls.add(t);
			}
			
			Color foreground = Color.white;
			Color background = Color.black;
			
			for (String line : ls) {
				for (int cp = 0; cp < line.toCharArray().length; cp++) {
					char c = line.charAt(cp);
					
					if (c == ESCAPE) {
						char next = line.charAt(cp + 1);
						
						if (next == '[') {
							next = line.charAt(cp + 2);
							char next2 = line.charAt(cp + 3);
							
							if (next == '0' && next2 == 'm') {
								cp += 3;
								continue;
							} else {
								cp += 4;
								continue;
							}
						}
					} else if (c == '\n') {
						y += fm.getHeight();
						continue;
					}
					
					String charString = Character.toString(c);
					
					x += fm.stringWidth(line) / line.length();
					
					g.setColor(background);
					g.fillRect(x, y, fm.stringWidth(line) / line.length(), fm.getHeight());
					
					g.setColor(foreground);
					g.drawString(charString, x, y);
				}
				
				x = 0;
				y += fm.getHeight();
			}
		}
		
		if (blinking) {
			int i = cursorx + cursory;
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
		
	}
	
	/**
	 * Gets total size of lists
	 * @return
	 */
	public int getTotal() {
		return -1;
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
		//if (cursory > 0 && cursorx + (cursory - 1) * columns >= block) {
		//	cursory--;
		//}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor down
	 */
	public void moveDown() {	
		//if (cursory + 1 < rows) {
		//	cursory++;
		//}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor to the left
	 */
	public void moveLeft() {
		//if (cursorx - 1 >= 0 && cursorx - 1 + cursory * columns >= block) {
		//	cursorx--;
		//}
		
		repaintThread.interrupt();
	}
	
	/**
	 * Moves cursor to the right
	 */
	public void moveRight() {
		//if (cursorx + 1 < columns) {
		//	cursorx++;
		//}
		
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
			//cursorx = columns - 1;
			cursory--;
		} else if (cursorx == 0 && cursory == 0) {
			return;
		} else {
			moveLeft();
		}
		
		//int i = x + y * columns;
		
		//if (i + 1 == block) {
		//	return;
		//}

		//this.lines.remove(i);
		
		repaintThread.interrupt();
	}
	
	/**
	 * Append character
	 * @param c
	 */
	public void append(char c) {
		append(c, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_FONT);
	}
	
	/**
	 * Append character
	 * @param c
	 * @param foreground
	 * @param background
	 * @param font
	 */
	public void append(char c, Color foreground, Color background, Font font) {
		Point p = getTypedEnd();
		cursorx = p.x;
		cursory = p.y;
		String s = lines.get(lines.size() - 1);
		s += c;
	}
	
	/**
	 * Append string
	 * @param s
	 */
	public void append(String s) {		
		append(s, DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, DEFAULT_FONT);
	}
	
	/**
	 * Append string
	 * @param s
	 * @param foreground
	 * @param background
	 * @param font
	 */
	public void append(String s, Color foreground, Color background, Font font) {		
		if (strip(s).equals(UNIX_CLEAR)) {
			clear();
			return;
		}
		
		lines.add(s);
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
		
		/*int i = x + y * columns;

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
		fonts.add(i, font);*/
		
		if (y < lines.size()) {
			String temp = lines.get(y);
			
			String s1 = temp.substring(0, x);
			String s2 = temp.substring(x, temp.length());
			
			lines.set(y, s1 + c + s2);
		} else {
			lines.add(lines.size(), Character.toString(c));
		}
	

		repaintThread.interrupt();
		shouldScroll = true;
	}
	
	/**
	 * Get where current typed text ends
	 * @return
	 */
	public Point getTypedEnd() {
		if (lines.size() == 0) {
			return new Point(0, 0);
		}
		
		Point p = new Point();
				
		for (String s : lines) {
			for (char c : s.toCharArray()) {
				if (c == '\n') {
					p.y++;
				}
			}
			
			p.y++;
		}
		
		p.x = lines.get(lines.size() - 1).length();
		
		return p;
	}
	
	/**
	 * Called when enter is pressed (newline)
	 * @param press If enter actually is pressed or if just a new line should be added
	 */
	public void enter(boolean press) {
		/*int latestBlock = block;
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
		}*/
	}
	
	/**
	 * Called when key pressed, checks if character is valid and checks for combinations such as Ctrl+C
	 * @param e
	 */
	public void keyPressed(KeyEvent e) {
		char c = e.getKeyChar();
		
		if (ctrl && (int) e.getKeyChar() == 3) {
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
			} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {// && chars.get(cursorx + cursory * columns) != NULL_CHAR) {
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
			if (e.isPopupTrigger()) {
				showMenu(e);
			} else {
				int x = e.getX() / charwidth;
				int y = e.getY() / charheight;
				
				//int i = x + y * columns;

				//select1 = i;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showMenu(e);
			} else if (!e.isPopupTrigger() && !menu.isVisible()) {
				int x = e.getX() / charwidth;
				int y = e.getY() / charheight;
				
				//int i = x + y * columns;

				//select2 = i;
			}
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX() / charwidth;
			int y = e.getY() / charheight;
			
			//int i = x + y * columns;

			//select2 = i;
		}
		
		private void showMenu(MouseEvent e) {
			menu.show(e.getComponent(), e.getX(), e.getY());
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
		//this.block = cursorx + cursory * columns;
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
	
	private static void setClipboard(String s) {
		StringSelection selection = new StringSelection(s);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}
	
	public static String strip(String s) {
		while (s.startsWith("\n")) {
			s = s.substring(1, s.length());
		}
		
		while (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		
		return s;
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
