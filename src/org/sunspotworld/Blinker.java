package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.util.Utils;

/**
 * @author Povilas Marcinkevicius
 * @version 1.0.1
 */
public class Blinker
{
  private final static ITriColorLEDArray leds = ((ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class));
  
  /**
   * Used to blink on a new thread; calling code keeps running;
   */
  public static void blink(final long onMs, final long offMs, final int r, final int g, final int b, final int ledMap, final int repeat)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        blinkAndWait(onMs, offMs, r, g, b, ledMap, repeat);
        Thread.currentThread().interrupt();
      }
    }).start();
  }
  
  /**
   * Used to blink on the same thread. Code will resume once the blinking has stopped 
   */
  public static void blinkAndWait(final long onMs, final long offMs, final int r, final int g, final int b, final int ledMap, final int repeat)
  {
    leds.setRGB(r, g, b);
    for(int i = 0; i < repeat; i++)
    {
      leds.setOn(ledMap);
      Utils.sleep(onMs);
      leds.setOff();
      Utils.sleep(offMs);
    }
  }
  
  public static void blinkAndWaitError(Exception e, int classNo, int exceptionNo)
  {
    e.printStackTrace();
    blinkAndWait(1000, 1000, 255, 0, 0, classNo + exceptionNo * 16, 1000);
  }
}
