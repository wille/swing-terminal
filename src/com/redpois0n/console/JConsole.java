package com.redpois0n.console;

import java.awt.Graphics;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class JConsole extends JComponent {
	
	private int width;
	private int height;
	
	private int charwidth;
	private int charheight;
		
	public JConsole() {
		this.width = 80;
		this.height = 30;
		
		this.charwidth = 8;
		this.charheight = 12;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.fillRect(0, 0, getRealX(width), getRealY(height));
	}
	
	public int getRealX(int x) {
		return x * charwidth;
	}
	
	public int getRealY(int y) {
		return y * charheight;
	}

}
