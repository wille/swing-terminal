package com.redpois0n.terminal;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class DebugTerminal {

	public static void main(String[] args) {
		JTerminal console = new JTerminal();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(console);
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(console.getKeyListener());
		frame.add(scrollPane);
		frame.setSize(650, 400);
		frame.setVisible(true);	
	}

}
