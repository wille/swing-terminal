package com.redpois0n.terminal;

import javax.swing.JFrame;

public class DebugTerminal {

	public static void main(String[] args) {
		JTerminal console = new JTerminal();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(console);
		frame.setSize(650, 400);
		frame.setVisible(true);
	}

}
