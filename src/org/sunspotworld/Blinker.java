/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.util.Utils;

/**
 * @author Povilas Marcinkevicius
 * 
 * @version 1.0.0
 */
public class Blinker
{
  private final static ITriColorLEDArray leds = ((ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class));
  
  public static void blink(final long onMs, final long offMs, final int r, final int g, final int b, final int ledMap, final int repeat)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        leds.setRGB(r, g, b);
        for(int i = 0; i < repeat; i++)
        {
          leds.setOn(ledMap);
          Utils.sleep(onMs);
          leds.setOff();
          Utils.sleep(offMs);
        }
        Thread.currentThread().interrupt();
      }
    }).start();
  }
  
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
}
