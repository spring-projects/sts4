package org.springframework.ide.eclipse.chromium.browser;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class PartialInputStream extends FilterInputStream
{

   private int bytesToRead;

   protected PartialInputStream(InputStream in, int size)
   {
      super(in);
      bytesToRead = size;
   }

   @Override
   public int available() throws IOException
   {
      return Math.min(super.available(), bytesToRead);
   }

   @Override
   public void close() throws IOException
   {
      bytesToRead = 0;
   }

   @Override
   public synchronized void mark(int readlimit)
   {
      throw new UnsupportedOperationException("mark not supported");
   }

   @Override
   public boolean markSupported()
   {
      return false;
   }

   @Override
   public int read() throws IOException
   {
      if (bytesToRead == 0) return -1;
      int read = super.read();
      if (read != -1) bytesToRead--;
      return read;
   }

   @Override
   public int read(byte[] b, int off, int len) throws IOException
   {
      if (bytesToRead == 0) return -1;
      int read = super.read(b, off, Math.min(len, bytesToRead));
      bytesToRead -= read;
      return read;
   }

   @Override
   public synchronized void reset() throws IOException
   {
      throw new UnsupportedOperationException("reset not supported");
   }

   @Override
   public long skip(long n) throws IOException
   {
      long skip = super.skip(Math.min(n, bytesToRead));
      bytesToRead -= skip;
      return skip;
   }
}