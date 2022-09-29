package org.springframework.ide.eclipse.chromium.browser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.scalasbt.ipcsocket.UnixDomainServerSocket;
import org.scalasbt.ipcsocket.Win32NamedPipeServerSocket;
import org.scalasbt.ipcsocket.Win32SecurityLevel;

public class ElectronBrowserCanvas extends Canvas
{
   private Image image;
   private GC gc;

   private Image createImage(int width, int height)
   {
      ImageData data = new ImageData(width, height, 32, new PaletteData(0xff0000,0x00ff00, 0x0000ff));
//      data.setAlpha(0, 0, 0);
//      Arrays.fill(data.alphaData, (byte) -1);
//      Arrays.fill(data.data, (byte) -1);
      return new Image(getDisplay(), data);
   }

   public ElectronBrowserCanvas(Composite parent)
   {
      this(parent, true);
   }

   public ElectronBrowserCanvas(Composite parent, boolean startElectron)
   {
      super(parent, SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);

      Display display = getDisplay();
      image = createImage(display.getBounds().width, display.getBounds().height);
      gc = new GC(image);
      FPSText.initGC(gc);

      addControlListener(new ControlListener() {
         @Override
         public void controlResized(ControlEvent e)
         {
            Rectangle oldBounds = image.getBounds();
            Rectangle newBounds = getBounds();
            if (oldBounds.width < newBounds.width || oldBounds.height < newBounds.height)
            {
               image.dispose();
               image = createImage(newBounds.width, newBounds.height);
               gc = new GC(image);
               FPSText.initGC(gc);
            }
            sendResize(newBounds);
         }

         @Override
         public void controlMoved(ControlEvent e)
         {
         }
      });

      addPaintListener(new PaintListener() {
         @Override
         public void paintControl(PaintEvent event)
         {
            event.gc.drawImage(image, event.x, event.y, event.width, event.height, event.x, event.y, event.width, event.height);
         }
      });

      addMouseListener(new MouseListener() {
         @Override
         public void mouseUp(MouseEvent e)
         {
            handleEvent("mouseUp", e);

         }

         @Override
         public void mouseDown(MouseEvent e)
         {
            handleEvent("mouseDown", e);
         }

         @Override
         public void mouseDoubleClick(MouseEvent e)
         {
         }
      });

      addMouseMoveListener(new MouseMoveListener() {
         @Override
         public void mouseMove(MouseEvent e)
         {
            handleEvent("mouseMove", e);
         }
      });

      addKeyListener(new KeyListener() {
         @Override
         public void keyReleased(KeyEvent e)
         {
            handleEvent("keyUp", e);
         }

         @Override
         public void keyPressed(KeyEvent e)
         {
            handleEvent("keyDown", e);
            handleEvent("char", e);
         }
      });
      addMouseWheelListener(new MouseWheelListener() {

         @Override
         public void mouseScrolled(MouseEvent e)
         {
            if (e.count != 0)
            {
               handleEvent("mouseWheel", e); // TODO how to correctly calculate deltaY etc.
            }
         }
      });
      addDisposeListener(new DisposeListener() {
         @Override
         public void widgetDisposed(DisposeEvent e)
         {
            sendMessage("{\n" +
                  "   \"type\":\"quit\"\n" + "}");
            if (process != null) process.destroy();
         }
      });

      new Thread(() -> {
         listen();
      }).start();

      if (startElectron)
      {
         unzipAndStartElectron();
      }
   }

   private ServerSocket server;

   private void listen()
   {
      try
      {
         boolean win = System.getProperty("os.name", "").toLowerCase().startsWith("win");
         String socketName;
         if (win)
         {
            socketName = "\\\\.\\pipe\\electron_pipe";
            server = new Win32NamedPipeServerSocket(socketName, false, Win32SecurityLevel.LOGON_DACL);
         }
         else
         {
            //Path pipe = Files.createTempDirectory("electron_pipe").resolve("electron.pipe");
            Path pipe = Files.createDirectories(Paths.get("/tmp/electron_pipe")).resolve("electron.pipe");
            Files.deleteIfExists(pipe);
            socketName = pipe.toString();
            server = new UnixDomainServerSocket(socketName, false);
         }
         //server = new ServerSocket(9090);
      }
      catch (IOException e)
      {
         System.err.println("Could not open socket.");
         System.exit(-1);
      }

      new Thread(() -> {
         while (!isDisposed())
         {
            run();
            try
            {
               Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
         }
      }).start();
   }

   private PrintWriter out;

   private void run()
   {
      try
      {
         System.out.println("Open connection");
         final Socket socket = server.accept();
         final InputStream inputStream = socket.getInputStream();
         final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
         
         out = new PrintWriter(socket.getOutputStream());

         Display display = getDisplay();
         display.asyncExec(() -> sendResize(getBounds()));
         browse("");

         FPSText fps = new FPSText();

         String command = null;
         while (!isDisposed() && (command = new String(readBytesFromInputStream(bufferedInputStream, 32), "UTF-8")) != null)
         {
            if (command.startsWith("cursor:"))
            {
               String type = (command.substring("cursor:".length()).split(","))[0];
               display.asyncExec(() -> setCursor(type));
            }
            else if (command.startsWith("paint:"))
            {
               String[] split = command.substring("paint:".length()).split(",");
               Point point = new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
               Image newImage = new Image(null, new PartialInputStream(bufferedInputStream, Integer.parseInt(split[2])));
               Rectangle bounds = newImage.getBounds();
               if (isDisposed()) break;
               display.syncExec(() -> { // TODO sync or async?
                  if (isDisposed()) return;
                  gc.drawImage(newImage, point.x, point.y);
                  fps.drawFPS(gc, this, point.x, point.y);
                  //gc.drawRectangle(point.x, point.y, bounds.width - 1, bounds.height - 1);
                  redraw(point.x, point.y, bounds.width, bounds.height, false);
                  newImage.dispose();
               });
               fps.calcFPS();
               sendMessage("accept", Collections.singletonMap("imageCount", split[3]));
            }
            else 
            {
               throw new IOException("Invalid command");
            }
         }
      }
      catch (IOException e)
      {
         out = null;
         System.err.println("Error: " + e.getMessage());
         e.printStackTrace();
      }
   }
   
   private byte[] readBytesFromInputStream(final InputStream inputStream, final int numberOfBytes) throws IOException
   {
      int bytesToRead = numberOfBytes;
      int bytesRead = 0;
      byte[] bytes = new byte[numberOfBytes];
      while (bytesToRead > 0 && (bytesRead = inputStream.read(bytes, numberOfBytes - bytesToRead, bytesToRead)) > -1)
      {
         bytesToRead -= bytesRead;
      }
      return bytes;
   }
   
   private void sendMessage(String type, Map<String, String> parameters)
   {
      StringBuilder builder = new StringBuilder();
      parameters.forEach((key, value) -> builder.append(",\n" + "   \"" + key + "\":" + value));
      sendMessage("{\n" + "   \"type\":\"" + type + "\"" + builder.toString() + "\n" + "}");
   }

   private void sendMessage(String message)
   {
      if (out != null)
      {
         out.write(message + "\n");
         out.flush();
      }
   }
   
   public void browse(String url)
   {
      sendMessage("browse", Collections.singletonMap("url", "\"" + url + "\""));
   }

   private void sendResize(Rectangle bounds)
   {
      Map<String, String> params = new LinkedHashMap<>();
      params.put("width", Integer.toString(bounds.width));
      params.put("height", Integer.toString(bounds.height));
      sendMessage("resize", params);
   }

   private void handleEvent(String type, MouseEvent e)
   {
      Map<String, String> params = new LinkedHashMap<>();

      if ((e.stateMask & SWT.BUTTON_MASK) != 0)
      {
         List<String> modifiers = new ArrayList<>();
         if ((e.stateMask & SWT.BUTTON1) != 0)
         {
            modifiers.add("\"leftButtonDown\"");
         }
         if ((e.stateMask & SWT.BUTTON2) != 0)
         {
            modifiers.add("\"middleButtonDown\"");
         }
         if ((e.stateMask & SWT.BUTTON3) != 0)
         {
            modifiers.add("\"rightButtonDown\"");
         }
         params.put("modifiers", "[" + String.join(",", modifiers) + "]");
      }
      String button;
      switch (e.button)
      {
         case 1:
            button = "left";
            break;
         case 2:
            button = "middle";
            break;
         case 3:
            button = "right";
            break;
         default:
            button = null;
            break;
      }
      
      params.put("x", Integer.toString(e.x));
      params.put("y", Integer.toString(e.y));

      /*if (oldMouseEvent != null && e.time - oldMouseEvent.time < 1000)
      {
         params.put("movementX", Integer.toString(oldMouseEvent.x - e.x));
         params.put("movementY", Integer.toString(oldMouseEvent.y - e.y));
      }
      oldMouseEvent = e;*/

      if (button != null) params.put("button", "\"" + button + "\"");
      if (!"mouseWheel".equals(type))
      {
         params.put("clickCount", Integer.toString(e.count));
      }
      else
      {
         params.put("deltaY", Integer.toString(e.count * 100 / 3));
      }
      sendMessage(type, params);
   }

   private void handleEvent(String type, KeyEvent e)
   {
      String keyCode = null;
      if (Character.isLetterOrDigit(e.character))
      {
         keyCode = Character.toString(e.character);
      }
      else
      {
         String c = null;
         switch (e.keyCode)
         {
            case 8:
               keyCode = "Backspace";
               break;
            case 9:
               keyCode = "Tab";
               break;
            case 13:
               keyCode = "Return";
               c = "Return";
               break;
            case 27:
               keyCode = "Escape";
               break;
            case 32:
               keyCode = "Space";
               c = " ";
               break;
            case 50:
               keyCode = "\\\"";
               c = keyCode;
               break;
            case SWT.ARROW_UP:
               keyCode = "Up";
               break;
            case SWT.ARROW_DOWN:
               keyCode = "Down";
               break;
            case SWT.ARROW_LEFT:
               keyCode = "Left";
               break;
            case SWT.ARROW_RIGHT:
               keyCode = "Right";
               break;
            case SWT.PAGE_UP:
               keyCode = "PageUp";
               break;
            case SWT.PAGE_DOWN:
               keyCode = "PageDown";
               break;
            case SWT.HOME:
               keyCode = "Home";
               break;
            case SWT.END:
               keyCode = "End";
               break;
            case SWT.KEYPAD_ADD:
               keyCode = "numadd";
               c = "+";
               break;
            case SWT.KEYPAD_SUBTRACT:
               keyCode = "numsub";
               c = "-";
               break;
            case SWT.KEYPAD_MULTIPLY:
               keyCode = "nummult";
               c = "*";
               break;
            case SWT.KEYPAD_DIVIDE:
               keyCode = "numdiv";
               c = "/";
               break;
            case SWT.KEYPAD_DECIMAL:
               keyCode = "numdec";
               c = ",";
               break;
            case SWT.KEYPAD_CR:
               keyCode = "Enter";
               break;
            case SWT.KEYPAD_0:
            case SWT.KEYPAD_1:
            case SWT.KEYPAD_2:
            case SWT.KEYPAD_3:
            case SWT.KEYPAD_4:
            case SWT.KEYPAD_5:
            case SWT.KEYPAD_6:
            case SWT.KEYPAD_7:
            case SWT.KEYPAD_8:
            case SWT.KEYPAD_9:
               keyCode = "num" + (e.keyCode - SWT.KEYPAD_0);
               break;
            case SWT.F1:
            case SWT.F2:
            case SWT.F3:
            case SWT.F4:
            case SWT.F5:
            case SWT.F6:
            case SWT.F7:
            case SWT.F8:
            case SWT.F9:
            case SWT.F10:
            case SWT.F11:
            case SWT.F12:
               keyCode = "F" + (e.keyCode - SWT.F1 + 1);
               break;
            default:
               break;
         }
         if (keyCode == null)
         {
            if (e.character == 0)
               return;
            keyCode = Character.toString(e.character);
         }
         else if ("char".equals(type))
         {
            if (c == null)
               return;
            keyCode = c;
         }
      }

      sendMessage(type, Collections.singletonMap("keyCode", "\"" + keyCode + "\""));
   }

   private Map<String, Integer> cursorMapping;

   private void setCursor(String type)
   {
      if (cursorMapping == null)
      {
         cursorMapping = new HashMap<>();
         // TODO col-resize, row-resize, m-panning, e-panning, n-panning, ne-panning, nw-panning, s-panning, se-panning, sw-panning, w-panning, move, vertical-text, cell, context-menu, alias, progress, nodrop, copy, not-allowed, zoom-in, zoom-out, grab, grabbing
         cursorMapping.put("default", SWT.CURSOR_ARROW);
         cursorMapping.put("crosshair", SWT.CURSOR_CROSS);
         cursorMapping.put("pointer", SWT.CURSOR_HAND);
         cursorMapping.put("text", SWT.CURSOR_IBEAM);
         cursorMapping.put("wait", SWT.CURSOR_WAIT);
         cursorMapping.put("help", SWT.CURSOR_HELP);
         cursorMapping.put("e-resize", SWT.CURSOR_SIZEE);
         cursorMapping.put("n-resize", SWT.CURSOR_SIZEN);
         cursorMapping.put("ne-resize", SWT.CURSOR_SIZENE);
         cursorMapping.put("nw-resize", SWT.CURSOR_SIZENW);
         cursorMapping.put("s-resize", SWT.CURSOR_SIZES);
         cursorMapping.put("se-resize", SWT.CURSOR_SIZESE);
         cursorMapping.put("sw-resize", SWT.CURSOR_SIZESW);
         cursorMapping.put("w-resize", SWT.CURSOR_SIZEW);
         cursorMapping.put("ns-resize", SWT.CURSOR_SIZENS);
         cursorMapping.put("ew-resize", SWT.CURSOR_SIZEWE);
         cursorMapping.put("nesw-resize", SWT.CURSOR_SIZENESW);
         cursorMapping.put("nwse-resize", SWT.CURSOR_SIZENWSE);
         cursorMapping.put("none", SWT.CURSOR_NO);
         //cursorMapping.put("", SWT.CURSOR_SIZEALL);
      }
      
      Integer swtType = cursorMapping.get(type);
      if (swtType == null)
      {
         System.out.println("Cursor not found:" + type);
         swtType = SWT.CURSOR_ARROW;
      }
      
      Cursor cursor = new Cursor(getDisplay(), swtType);
      setCursor(cursor);
   }

   private Process process;
   
   private void unzipAndStartElectron()
   {
      File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "headless-electron"); // TODO unzip to AppData
      if(!tempDir.exists())
      {
         tempDir.mkdirs();
      }
      try
      {
         // TODO add Linux and Mac
//         unzip(getClass().getResourceAsStream("/headless-electron-darwin-x64-0.0.1.zip"), tempDir);
    	 unzip(new FileInputStream(new File(System.getProperty("user.home") + "/headless-electron-win32-x64-0.0.1.zip")), tempDir); 
         ProcessBuilder processBuilder = new ProcessBuilder(tempDir + File.separator + "headless-electron.exe");
         process = processBuilder.start();
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   private void unzip(InputStream is, File dest) throws IOException
   {
      try (ZipInputStream zip = new ZipInputStream(is))
      {
         ZipEntry entry = zip.getNextEntry();
         while (entry != null)
         {
            File out = new File(dest, entry.getName());
            File parentFile = out.getParentFile();
            if (!parentFile.exists())
            {
               parentFile.mkdirs();
            }
            if (!entry.isDirectory())
            {
               byte[] buffer = new byte[4096];
               try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out)))
               {
                  int count = 0;
                  while ((count = zip.read(buffer)) != -1)
                  {
                     bos.write(buffer, 0, count);
                  }
               }
            }
            else
            {
               out.mkdirs();
            }
            zip.closeEntry();
            entry = zip.getNextEntry();
         }
      }
   }
}