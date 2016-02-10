package org.sunspotworld.tools;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import java.io.IOException;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolAcceleration extends SensorListener
{
  private static final IAccelerometer3D motionSensor = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
  
  public ToolAcceleration(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  public void heartBeat()
  {
    try
    {
      value = Math.max(0.0, Math.min(100.0, motionSensor.getAccel() * 20.0));
      if(thresholdCrossed())
        sendData();
    }
    catch(IOException e)
    { e.printStackTrace(); }
  }
}
