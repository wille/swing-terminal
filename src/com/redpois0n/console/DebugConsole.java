package com.redpois0n.console;

import javax.swing.JFrame;

public class DebugConsole {

	public static void main(String[] args) {
		JConsole console = new JConsole();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(console);
		frame.setSize(650, 400);
		frame.setVisible(true);
	}

}
