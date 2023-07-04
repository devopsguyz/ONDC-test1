package com.nsdl.beckn.common.filter.wrapper;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class DecompressServletInputStream extends ServletInputStream {
	private InputStream inputStream;

	public DecompressServletInputStream(InputStream input) {
		this.inputStream = input;

	}

	@Override
	public int read() throws IOException {
		return this.inputStream.read();
	}

	@Override
	public boolean isFinished() { // TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() { // TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadListener(ReadListener listener) {
		// TODO Auto-generated method stub

	}

}