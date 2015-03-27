package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.redpois0n.oslib.Shell;

public class DebugTerminal {
	
	private static Process p;
	private static JTerminal terminal;
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		terminal = new JTerminal();

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(terminal);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
		    public void adjustmentValueChanged(AdjustmentEvent e) {  
		    	if (terminal.scrollToBottom()) {
		    		boolean scrollUp = terminal.scrollUp();
		    		e.getAdjustable().setValue(scrollUp ? 0 : e.getAdjustable().getMaximum());  
		    	} 
		    }
		});

		terminal.addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				if (command.equalsIgnoreCase("clear") || command.equalsIgnoreCase("cls")) {
					terminal.clear();
					return;
				}

				append(command + "\n");
				terminal.setBlockAtCurrentPos();
			}
			
			@Override
			public void onTerminate(JTerminal terminal) {
				try {
					if (p != null) {
						p.destroy();
					}
					startShell();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		terminal.addSizeChangeListener(new SizeChangeListener() {
			@Override
			public void sizeChange(JTerminal terminal, boolean reset, int width, int height) {
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				scrollPane.revalidate();
				vertical.revalidate();
				vertical.setValue(reset ? 0 : vertical.getMaximum());
				terminal.revalidate();
			}
		});

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(terminal.getKeyListener());
		frame.add(scrollPane);
		frame.setSize(675, 300);
		frame.setVisible(true);

		terminal.append("JTerminal Test\n", Color.white, Color.red, JTerminal.DEFAULT_FONT);
		terminal.append("Debug and Example\n\n");
		terminal.setBlockAtCurrentPos();
		
		startShell();
	}
	
	public static void startShell() {
		try {
			String shell = Shell.getShell().getPath();
			
			ProcessBuilder builder = new ProcessBuilder(shell);
			builder.redirectErrorStream(true);
			p = builder.start();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {							
							BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
							
							String line;
							
							while ((line = reader.readLine()) != null) {
								terminal.append(line + "\n");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void append(String command) {		
		try {
			new PrintWriter(p.getOutputStream(), true).println(command);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
