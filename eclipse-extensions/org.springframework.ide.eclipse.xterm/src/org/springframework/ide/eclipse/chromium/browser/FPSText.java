package org.springframework.ide.eclipse.chromium.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class FPSText
{
   private long start = System.currentTimeMillis();
   private long frames = 0;
   private String fps = "";

   public void calcFPS()
   {
      long end = System.currentTimeMillis();
      frames++;
      if (end - start > 1000)
      {
         fps = "FPS: " + String.format("%.2f", frames / ((end - start) / 1000.0));
         frames = 0;
         start = end;
      }
   }
   
   public void drawFPS(GC gc, Composite comp, int x, int y)
   {
      Point textSize = gc.textExtent(fps);
      int width = comp.getBounds().width;
      int posX = width - textSize.x;
      if ((x > posX && y < textSize.y) || frames == 0);
      {
         gc.drawText(fps, posX, 0, false);
         comp.redraw(posX, 0, width, textSize.y, false);
      }
   }
   
   public static void initGC(GC gc)
   {
      gc.setFont(new Font(null,"Arial", 16, SWT.BOLD ));
      gc.setForeground(new Color(null, 255, 0, 0));
   }
}