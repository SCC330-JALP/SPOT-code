package org.sunspotworld.tools;

import com.sun.spot.resources.transducers.IIOPin;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.util.Utils;
import java.io.IOException;

/**
 * @author @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public class ToolCompass extends SensorListener
{
  private static final EDemoBoard pinBoard = EDemoBoard.getInstance();
  private static final IIOPin compassSensor0 = pinBoard.getIOPins()[EDemoBoard.D0];
  private static final IIOPin compassSensor1 = pinBoard.getIOPins()[EDemoBoard.D1];
  public static boolean hasHardware = true;
  
  public ToolCompass(double triggerThreshold, byte type)
  {
    super(triggerThreshold, type);
    compassSensor0.setAsOutput(false);
    compassSensor1.setAsOutput(true);
    pinBoard.initUART(EDemoBoard.SERIAL_SPEED_9600, EDemoBoard.SERIAL_DATABITS_8, EDemoBoard.SERIAL_PARITY_NONE, EDemoBoard.SERIAL_STOPBITS_2);
    Utils.sleep(30);
    hasHardware = checkHardware();
    System.out.println("Compass hardware " + (hasHardware ? "detected" : "not found"));
  }
  
  private boolean checkHardware()
  {
    final boolean ok[] = new boolean[]{false};
    Thread check = new Thread(new Runnable()
    {
      public void run()
      {
        heartBeat();
        ok[0] = true;
      }
    }, "Compass hardware check");
    check.start();
    
    Utils.sleep(200);
    check.interrupt();
    check.interrupt();
    return ok[0];
  }
  
  public void heartBeat()
  {
    if(hasHardware)
    {
      try
      {
        pinBoard.writeUART((byte)0x13);
        Utils.sleep(5);
        int msb = pinBoard.readUART();
        int lsb = pinBoard.readUART();
        value = (int)(((msb << 8) + lsb) / 10);
        if(thresholdCrossed())
          sendData();
      }
      catch(IOException e)
      { e.printStackTrace(); }
      }
    }
  }
