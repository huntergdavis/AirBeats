package com.misomedia.cameratestapp;

public enum UiThreadMessages {
	SCRATCH ( 11),
	CLOSETITLESCREEN (12),
	AUDIOPROCESSED (13);
	
	
	private final int messageNum;
	UiThreadMessages(int messageNum) {
		this.messageNum = messageNum;
	}
	
	public int value() {
		return this.messageNum;
	}
}
