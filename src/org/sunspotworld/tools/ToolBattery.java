package org.sunspotworld.tools;

import com.sun.spot.peripheral.IPowerController;
import com.sun.spot.peripheral.Spot;
import org.sunspotworld.Beeper;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolBattery extends SensorListener
{
  private static final int VOLTAGE_MIN = 3300;
  private static final int VOLTAGE_MAX = 4400;
  private static final int ALERT_THRESHOLD = 30;
  private static final int BEEPING_PERIOD = 30000;
  private static final IPowerController powerController = Spot.getInstance().getPowerController();
  private static long lastBeeped = System.currentTimeMillis();
  
  public ToolBattery(double triggerThreshold, byte type)
  { super(triggerThreshold, type); }
  
  private static int getBattery()
  {
    double voltageRange = VOLTAGE_MAX - VOLTAGE_MIN;
    int percentage = (int)(((powerController.getVbatt() - VOLTAGE_MIN) / voltageRange) * 100.0);
    return Math.max(0, Math.min(100, percentage));
  }

  public void heartBeat()
  {
    value = getBattery();
    long now = System.currentTimeMillis();
    if(value <= ALERT_THRESHOLD && lastBeeped + BEEPING_PERIOD < now)
    {
      lastBeeped = now;
      Beeper.beep(2000, 1000, 1000, 3);
    }

    if(thresholdCrossed())
      sendData();
  }
}
