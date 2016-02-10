package org.sunspotworld.tools;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 * 
 * Used to send ping messages to detect when the spot disconnects
 */
public class ToolPing extends SensorListener
{
  public ToolPing(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  public void heartBeat()
  {
    value = 1.0;
    sendData();
  }
}
