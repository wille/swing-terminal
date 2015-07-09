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
	
	public static void main(String[] args) throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		terminal = new JTerminal();

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(terminal);

		terminal.addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				append(command + "\n");
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

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(terminal.getKeyListener());
		frame.add(scrollPane);
		frame.setSize(675, 300);
		frame.setVisible(true);

		terminal.append("JTerminal Test\n");
		terminal.append("Debug and Example\n\n");
		
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
