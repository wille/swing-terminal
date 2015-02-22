package com.redpois0n.terminal;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class DebugTerminal {

	public static void main(String[] args) {
		JTerminal console = new JTerminal();

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(console);

		console.addInputListener(new InputListener() {
			@Override
			public void processCommand(JTerminal terminal, String command) {
				System.out.println(command);
			}
		});

		console.addSizeChangeListener(new SizeChangeListener() {
			@Override
			public void sizeChange(int width, int height) {
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			}
		});

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(console.getKeyListener());
		frame.add(scrollPane);
		frame.setSize(675, 300);
		frame.setVisible(true);
	}

}
