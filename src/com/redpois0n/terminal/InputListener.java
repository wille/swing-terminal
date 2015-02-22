package com.redpois0n.terminal;

public abstract interface InputListener {
	
	public abstract void processCommand(JTerminal terminal, String command);

}
