package org.sunspotworld.tools;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ISwitchListener;
import com.sun.spot.resources.transducers.SwitchEvent;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.2.0
 */
public class ToolButton extends SensorListener implements ISwitchListener
{
  private ISwitch sw;
  
  public ToolButton(double triggerThreshold, byte type, int buttonNum)
  {
    super(triggerThreshold, type);
    if(buttonNum != 1 && buttonNum != 2)
      throw new IllegalArgumentException("switches can only be 1 or 2");
    sw = (ISwitch) Resources.lookup(ISwitch.class, "SW" + buttonNum);
  }

  public void switchPressed(SwitchEvent event)
  {
    value = 1;
    sendData();
  }
  
  public void switchReleased(SwitchEvent event)
  {
    value = 0;
    sendData();
  }
  
  public void disable()
  {
    while(sw.getISwitchListeners().length > 0)
      sw.removeISwitchListener(sw.getISwitchListeners()[0]);
  }
  
  public void enable()
  { sw.addISwitchListener(this); }
}
