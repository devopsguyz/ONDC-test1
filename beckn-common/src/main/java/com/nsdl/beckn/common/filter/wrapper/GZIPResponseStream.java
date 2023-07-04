package com.nsdl.beckn.common.filter.wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

public class GZIPResponseStream extends ServletOutputStream {

	protected ByteArrayOutputStream baos = null;
	protected GZIPOutputStream gzipstream = null;
	protected boolean closed = false;
	protected HttpServletResponse response = null;
	protected ServletOutputStream output = null;

	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		this.closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		this.baos = new ByteArrayOutputStream();
		this.gzipstream = new GZIPOutputStream(this.baos);
	}

	@Override
	public void close() throws IOException {
		if (this.closed) {
			throw new IOException("This output stream has already been closed");
		}
		this.gzipstream.finish();

		byte[] bytes = this.baos.toByteArray();

		this.response.addHeader("Content-Length", Integer.toString(bytes.length));
		this.response.addHeader("Content-Encoding", "gzip");
		this.output.write(bytes);
		this.output.flush();
		this.output.close();
		this.closed = true;
	}

	@Override
	public void flush() throws IOException {
		if (this.closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		this.gzipstream.flush();
	}

	@Override
	public void write(int b) throws IOException {
		if (this.closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.gzipstream.write((byte) b);
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if (this.closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		this.gzipstream.write(b, off, len);
	}

	public boolean closed() {
		return this.closed;
	}

	public void reset() {
		// noop
	}

	@Override
	public boolean isReady() { return false; }

	@Override
	public void setWriteListener(WriteListener listener) {
	}
}
