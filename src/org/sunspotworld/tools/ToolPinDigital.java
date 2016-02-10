package org.sunspotworld.tools;

import com.sun.spot.resources.transducers.IIOPin;
import com.sun.spot.resources.transducers.IInputPinListener;
import com.sun.spot.resources.transducers.InputPinEvent;
import com.sun.spot.sensorboard.EDemoBoard;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.1.0
 */
public class ToolPinDigital extends SensorListener implements IInputPinListener
{
  private IIOPin inputPin;
  
  public ToolPinDigital(double triggerThreshold, byte type, int pinNum)
  {
    super(triggerThreshold, type);
    inputPin = EDemoBoard.getInstance().getIOPins()[pinNum];
  }

  public void pinSetHigh(InputPinEvent evt)
  {
    value = 1.0;
    sendData();
  }

  public void pinSetLow(InputPinEvent evt)
  {
    value = 0.0;
    sendData();
  }
  
  public void enable()
  { inputPin.addIInputPinListener(this); }
  
  public void disable()
  {
    while(inputPin.getIInputPinListeners().length > 0)
        inputPin.removeIInputPinListener(inputPin.getIInputPinListeners()[0]);
  }
}
