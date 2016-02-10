package org.sunspotworld.tools;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ILightSensor;
import java.io.IOException;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolLight extends SensorListener
{
  private static final ILightSensor lightSensor = (ILightSensor) Resources.lookup(ILightSensor.class);
  
  public ToolLight(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  public void heartBeat()
  {
    try
    {
      value = Math.min(100.0, Math.max(0.0, lightSensor.getAverageValue() / 10.0));
      if(thresholdCrossed())
        sendData();
    }
    catch(IOException e)
    { e.printStackTrace(); }
  }
}
