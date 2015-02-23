package com.redpois0n.terminal;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class DebugTerminal {

	public static void main(String[] args) {
		final JTerminal terminal = new JTerminal();

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(terminal);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
		    public void adjustmentValueChanged(AdjustmentEvent e) {  
		    	if (terminal.scrollToBottom()) {
		    		 e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
		    	}
		    }
		});

		terminal.addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				System.out.println(command);
				try {
					Process p = Runtime.getRuntime().exec(command);
					BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
					
					String line;
					
					while ((line = reader.readLine()) != null) {
						terminal.append(line + "\n");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				terminal.append('\n');
				terminal.append("root@master:~# ", Color.green, JTerminal.DEFAULT_BACKGROUND, JTerminal.DEFAULT_FONT);
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
		terminal.append("root@master:~# ", Color.green, JTerminal.DEFAULT_BACKGROUND, JTerminal.DEFAULT_FONT);
		terminal.setBlockAtCurrentPos();
	}

}
