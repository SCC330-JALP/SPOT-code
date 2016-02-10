package org.sunspotworld.tools;

import com.sun.spot.util.Utils;
import com.sun.squawk.util.Arrays;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.1.0
 * 
 * Used to access all the listeners as well as provide them with heartBeat
 * (poller for the ones without actual listeners). Roughly equivalent to HelperClasses from 330.
 */
public class ToolSet
{
  public static final byte ID_COMPASS       = 0;
  public static final byte ID_TEMPERATURE   = 1;
  public static final byte ID_LIGHT         = 2;
  public static final byte ID_ACCELERATION  = 3;
  public static final byte ID_BUTTON_LEFT   = 4;
  public static final byte ID_BUTTON_RIGHT  = 5;
  public static final byte ID_SOUND         = 6;
  public static final byte ID_BATTERY       = 7;
  public static final byte ID_INFRARED      = 8;
  public static final byte ID_A2            = 9;
  public static final byte ID_A3            = 10;
  public static final byte ID_D2            = 11;
  public static final byte ID_D3            = 12;
  public static final byte ID_PING          = 13;

  private static final SensorListener[] listeners = new SensorListener[]
  {
    new ToolCompass     (3.0, ID_COMPASS),
    new ToolTemperature (0.8, ID_TEMPERATURE),
    new ToolLight       (1.0, ID_LIGHT),
    new ToolAcceleration(1.2, ID_ACCELERATION),
    new ToolButton      (100, ID_BUTTON_LEFT, 1),
    new ToolButton      (100, ID_BUTTON_RIGHT, 2),
    new ToolPinDigital  (100, ID_SOUND, 0), // Sound - noise sensor just listens on a pin
    new ToolBattery     (1.5, ID_BATTERY),
    new ToolInfrared    (0.5, ID_INFRARED),
    new ToolPinAnalog   (0.2, ID_A2, 2),
    new ToolPinAnalog   (0.2, ID_A3, 3),
    new ToolPinDigital  (100, ID_D2, 2),
    new ToolPinDigital  (100, ID_D3, 3),
    new ToolPing        (1.0, ID_PING)
  };
  
  private static final int usableListenerNum = listeners.length - 1;
  private static final boolean[] active = new boolean[usableListenerNum]; // all except ping
  static { Arrays.fill(active, false); } // fills it with false
  private static final String codeletters = "ctlabrseiwxyz";
  
  public static boolean subscribe(byte type, String subscriptionName, ListenerActuator listenerCode)
  { return listeners[type].subscribe(subscriptionName, listenerCode); }
  
  public static boolean unsubscribe(byte type, String subscriptionName)
  { return listeners[type].unsubscribe(subscriptionName); }
  
  public static void subscribeAll(String subscriptionName, ListenerActuator listenerCode)
  {
    for(byte b = 0; b <= usableListenerNum; b++)
      listeners[b].subscribe(subscriptionName, listenerCode);
  }
  
  public static void unsubscribeAll(byte type, String subscriptionName)
  {
    for(byte b = 0; b <= usableListenerNum; b++)
      listeners[b].unsubscribe(subscriptionName);
  }
  
  // Disable all and reenable correct ones. Ping excluded
  public static void resetListeners(String list)
  {
    for(int i = 0; i < usableListenerNum; i++)
    {
      active[i] = false;
      listeners[i].disable();
      Utils.sleep(20);
    }
    
    for(int i = 0; i < list.length(); i++)
    {
      char c = list.charAt(i);
      int index = codeletters.indexOf(c);
      if(index >= 0)
      {
        active[index] = true;
        listeners[index].enable();
        Utils.sleep(20);
      }
    }
  }
  
  public static void initiateHeartBeat(final int period)
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        while(true)
        {
          try
          {
            Thread.sleep(period);
            for(int i = 0; i < listeners.length; i++)
              if(i == ID_PING || active[i])
                listeners[i].heartBeat();
          }
          catch(InterruptedException e)
          { e.printStackTrace(); } // no need to even log this
        }
      }
    }, "HeartBeat").start();
  }
}
