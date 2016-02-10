package org.sunspotworld.tools;

import org.sunspotworld.Logger;

/**
 * @author Povilas Marcinkevicius
 * @version 1.1.0
 * 
 * Used to allow for constant sensor data updating with subscriptions
 */
public abstract class SensorListener
{
  private static final int MAX_ACTUATORS = 10;
  
  private class Entry
  {
    public String name;
    public ListenerActuator actuator;
    
    public Entry(String name, ListenerActuator actuator)
    {
      this.name = name;
      this.actuator = actuator;
    }
  }
  
  private final Entry[] entries = new Entry[MAX_ACTUATORS];
  private int entryNum = 0;
  private double lastValue = 1.0;
  private final double threshold;
  private byte type;
  double value = 0.0;
  
  public SensorListener(double threshold, byte type)
  {
    this.threshold = threshold;
    this.type = type;
  }
  
  public boolean subscribe(String name, ListenerActuator actuator)
  {
    if(entryNum == MAX_ACTUATORS)
    {
      Logger.log(Logger.ERROR, "Listener array full! Can't add " + name);
      return false;
    }
    else
    {
      entries[entryNum] = new Entry(name, actuator);
      entryNum++;
      System.out.println("Sensor Listener " + type + " started");
      return true;
    }
  }
  
  public boolean unsubscribe(String name)
  {
    int toRemove = -1;
    for(int i = 0; i < entryNum; i++)
      if(entries[i].name.equals(name))
        toRemove = i;
    
    if(toRemove == -1)
    {
      System.out.println("Trying to remove non-existing subscribtion!");
      return false;
    }
    else
    {
      for(int i = toRemove + 1; i < entryNum; i++)
        entries[i - 1] = entries[i];
      entryNum--;
      return true;
    }
  }
  
  boolean thresholdCrossed()
  {
    boolean crossed = (value + threshold < lastValue || value - threshold > lastValue);
    if(crossed)
      lastValue = value;
    return crossed;
  }
  
  void sendData()
  {
    value = Math.floor(value * 100) / 100;
    for(int i = 0; i < entryNum; i++)
      entries[i].actuator.onListenerTrigger(value, type);
  }
  
  public void heartBeat() {}
  public void disable() {}
  public void enable() {}
}
