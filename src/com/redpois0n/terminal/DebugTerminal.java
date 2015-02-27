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

public class DebugTerminal {
	
	private static Process p;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		final JTerminal terminal = new JTerminal();

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
				System.out.println(command);
				append(command);
				terminal.append('\n');
				terminal.setBlockAtCurrentPos();
			}
		});

		terminal.addSizeChangeListener(new SizeChangeListener() {
			@Override
			public void sizeChange(JTerminal terminal, int width, int height) {
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				scrollPane.revalidate();
				vertical.revalidate();
				vertical.setValue(vertical.getMaximum());
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
		
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						startShell();
						
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
	}
	
	public static void startShell() throws Exception {
		if (System.getProperty("os.name").contains("Win")) {
			ProcessBuilder builder = new ProcessBuilder("cmd");
			builder.redirectErrorStream(true);
			p = builder.start();
		} else {
			ProcessBuilder builder = new ProcessBuilder("/bin/bash");
			builder.redirectErrorStream(true);
			p = builder.start();
		}
	}
	
	public static void append(String command) {		
		try {
			PrintWriter input = new PrintWriter(p.getOutputStream(), true);
			input.println(command);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
