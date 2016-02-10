package org.sunspotworld.tools;

import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.sensorboard.EDemoBoard;
import java.io.IOException;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolInfrared extends SensorListener
{
  private static final IAnalogInput inputPin = EDemoBoard.getInstance().getAnalogInputs()[EDemoBoard.A1];
  private static boolean noCompass = !ToolCompass.hasHardware;
  
  public ToolInfrared(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  public void heartBeat()
  {
    if(noCompass)
    {
      try
      {
        value = inputPin.getVoltage() > 0.5 ? 1 : 0;
        if(thresholdCrossed())
          sendData();
      }
      catch(IOException e)
      { e.printStackTrace(); }
    }
  }
}
