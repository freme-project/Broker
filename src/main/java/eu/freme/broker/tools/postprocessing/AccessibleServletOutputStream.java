package eu.freme.broker.tools.postprocessing;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.*;

/**
 * Created by Arne Binder (arne.b.binder@gmail.com) on 08.12.2015.
 */
public class AccessibleServletOutputStream extends ServletOutputStream {

    private DataOutputStream buffer;

    public AccessibleServletOutputStream(OutputStream output) {
        this.buffer = new DataOutputStream (output);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener listener) {
    }

    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
    }

    @Override
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        buffer.write(arg0, arg1, arg2);
    }

    @Override
    public void write(byte[] arg0) throws IOException {
        buffer.write(arg0);
    }
}