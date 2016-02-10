package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IToneGenerator;

/**
 * @author Povilas Marcinkevicius
 * @version 2.0.0
 */
public class Beeper
{
  private static final IToneGenerator toneGen = (IToneGenerator) Resources.lookup(IToneGenerator.class);
  
  public static void beep(final int playTime, final int pauseTime, final int frequency, final int repetitions)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        for(int i = 0; i < repetitions; i++)
        {
          toneGen.startTone(frequency, playTime);
          
          try
          { Thread.sleep(playTime + pauseTime); }
          catch(InterruptedException e)
          { /* will hear a sound glitch; not fatal */ }
        }
      }
    }).start();
  }
  
  public static void beep(final int playTime, final int frequency)
  { toneGen.startTone(frequency, playTime); }
}