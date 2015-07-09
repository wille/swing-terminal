package com.redpois0n.terminal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class DebugTerminal {
	
	private static Process p;
	private static PrintWriter writer;
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
			public void processCommand(JTerminal terminal, char c) {
				append(c);
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
		
		append("bash test.sh\n");
	}
	
	public static void startShell() {
		try {
			String shell = "bash";
			
			ProcessBuilder builder = new ProcessBuilder(shell);
			builder.redirectErrorStream(true);
			p = builder.start();
			writer = new PrintWriter(p.getOutputStream(), true);
			
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
	
	public static void append(char c) {		
		try {
			if (c == '\n') {
				writer.println();
			} else {
				writer.print(c);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void append(String command) {		
		try {
			writer.println(command);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
