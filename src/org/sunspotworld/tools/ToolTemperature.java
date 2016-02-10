package org.sunspotworld.tools;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITemperatureInput;
import java.io.IOException;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolTemperature extends SensorListener
{
  private static final ITemperatureInput temperatureSensor = (ITemperatureInput) Resources.lookup(ITemperatureInput.class);
  
  public ToolTemperature(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  public void heartBeat()
  {
    try
    {
      value = temperatureSensor.getCelsius();
      if(thresholdCrossed())
        sendData();
    }
    catch(IOException e)
    { e.printStackTrace(); }
  }
}
