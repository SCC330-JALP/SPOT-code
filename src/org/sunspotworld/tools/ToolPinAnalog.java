package org.sunspotworld.tools;

import com.sun.spot.resources.transducers.IAnalogInput;
import com.sun.spot.sensorboard.EDemoBoard;
import java.io.IOException;
import org.sunspotworld.Logger;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolPinAnalog extends SensorListener
{
  private IAnalogInput inputPin;
  
  public ToolPinAnalog(double triggerThreshold, byte type, int pinNum)
  {
    super(triggerThreshold, type);
    inputPin = EDemoBoard.getInstance().getAnalogInputs()[pinNum];
  }
  
  public void heartBeat()
  {
    try
    {
      if(thresholdCrossed())
      {
        value = inputPin.getVoltage();
        sendData();
      }
    }
    catch(IOException e)
    {
      Logger.log(Logger.ERROR, "Analog pin failed to retrieve voltage");
      e.printStackTrace();
    }
  }
}
