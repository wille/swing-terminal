package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class JTerminal extends JTextPane {
	
	public static final Font DEFAULT_FONT;
	public static final Color DEFAULT_FOREGROUND = Color.white;
	public static final Color DEFAULT_BACKGROUND = Color.black;
	public static final char NULL_CHAR = '\u0000';
	
	public static final char ESCAPE = 27;
	public static final String UNIX_CLEAR = ESCAPE + "[H" + ESCAPE + "[J";

	static {		
		DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 14);
	}
	
	private List<InputListener> inputListeners = new ArrayList<InputListener>();
	
	private StyledDocument doc;
	
	public JTerminal() {
		this.doc = getStyledDocument();
		setFont(DEFAULT_FONT);
		setForeground(DEFAULT_FOREGROUND);
		setBackground(DEFAULT_BACKGROUND);
		setCaret(new TerminalCaret());
		
		addKeyListener(new KeyEventListener());
		addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				
			}
		});
	}

	/**
	 * Gets main key listener
	 * @return
	 */
	public KeyListener getKeyListener() {
		return super.getKeyListeners()[0];
	}
	
	/**
	 * Called when key pressed, checks if character is valid and checks for combinations such as Ctrl+C
	 * @param e
	 */
	public void keyPressed(KeyEvent e) {
		char c = e.getKeyChar();
		
		/*if (ctrl && (int) e.getKeyChar() == 3) {
			for (InputListener l : inputListeners) {
				l.onTerminate(this);
			}
			return;
		}*/
		
		//append(c);
	}
	
	public synchronized void append(String s) {
		setText(super.getText() + s);
	}
	
	public class KeyEventListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				enter();
			} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				//ctrl = true;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
				//ctrl = false;
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {		
			if ((int) e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
				JTerminal.this.keyPressed(e);
			}
		}
	}
	
	public void enter() {
		System.out.println("eeter");
	}
	
	public void addInputListener(InputListener listener) {
		inputListeners.add(listener);
	}
	
	public void removeInputListener(InputListener listener) {
		inputListeners.remove(listener);
	}

}
