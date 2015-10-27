/**
* @author Povilas Marcinkevicius
* 
* @version 1.0.1
**/

package org.sunspotworld;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ILightSensor; 
import com.sun.spot.resources.transducers.ITemperatureInput; 
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.util.Utils;
import java.io.DataOutputStream;
import java.io.IOException;

public class Week3SPOT extends MIDlet
{
  private static final String COMMAND_IDLE = "idle";
  private static final String COMMAND_ZONE_DATA = "zone";
  private static final char COMMAND_SENSOR_EVENT = 's';
  private static final char SENSOR_LIGHT = 'l';
  private static final char SENSOR_MOTION = 'm';
  private static final char SENSOR_TEMPERATURE = 't';
  private static final int PORT_COMMAND_RELAY = 37;
  
  private final int SAMPLE_PERIOD = 5000;
  
  private final ILightSensor lightSensor = (ILightSensor) Resources.lookup(ILightSensor.class);
  private final ITemperatureInput temperatureSensor = (ITemperatureInput) Resources.lookup(ITemperatureInput.class);
  private final IAccelerometer3D motionSensor = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);

  private final ConnectionSPOT radiogramConn = ConnectionProtocolSPOT.getClosestConnection(127, ConnectionProtocolSPOT.PORT_BASE_SEARCH);
  private DataOutputStream streamConn; // not final because changes on command
  private final String command[] = {COMMAND_IDLE};

  private double lastSensorVal;
  private interface MeasuringTool
  {
    public double getValue() throws IOException;
    public double getTriggerDelta();
    public long getWaitTime();
    public int getLEDs();
  }
  
  private class ToolTemp implements MeasuringTool
  {
    public double getValue() throws IOException { return temperatureSensor.getCelsius(); }
    public double getTriggerDelta() { return 3.0; }
    public long getWaitTime() { return 20000; }
    public int getLEDs() { return 3; }
  }
  
  private class ToolLight implements MeasuringTool
  {
    public double getValue() throws IOException { return lightSensor.getAverageValue(); }
    public double getTriggerDelta() { return 30.0; }
    public long getWaitTime() { return 2000; }
    public int getLEDs() { return 6; }
  }
  
  private class ToolMotion implements MeasuringTool
  {
    public double getValue() throws IOException { return motionSensor.getAccel(); }
    public double getTriggerDelta() { return 0.2; }
    public long getWaitTime() { return 1000; }
    public int getLEDs() { return 12; }
  }
  
  protected void pauseApp() {}
  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}
  protected void startApp() throws MIDletStateChangeException
  {
    if(radiogramConn != null)
    {
      streamConn = ConnectionProtocolSPOT.getOutputStreamConn(radiogramConn, "");
    
      ConnectionSPOT commandListener = new ConnectionSPOT(ConnectionSPOT.LISTEN, PORT_COMMAND_RELAY, 10); // Never closed
      Thread activeAction = new Thread(new Runnable(){public void run(){ Utils.sleep(Long.MAX_VALUE); }}); // Just so it's not null

      while(true)
      {
        try
        {
          command[0] = commandListener.receive().readUTF();        
          Blinker.blinkAndWait(500, 500, 0, 255, 0, 129, 1); // Blink 1000 0001 GREEN x5
          try
          {
            streamConn.close();
            activeAction.interrupt();
            activeAction.interrupt();
          }
          catch(Exception e){}

          activeAction = new Thread(new Runnable()
          {
            public void run()
            {
              streamConn = ConnectionProtocolSPOT.getOutputStreamConn(radiogramConn, command[0]);
              if(command[0].equals(COMMAND_ZONE_DATA))
              {
                Blinker.blink(500, 500, 0, 255, 0, 24, 3);
                sendZoneData(streamConn);
              }
              else if(command[0].equals(COMMAND_IDLE))
              {
                Blinker.blink(500, 500, 0, 255, 0, 48, 3);
                Utils.sleep(Long.MAX_VALUE); // keeping it open for consistency
              }
              else if(command[0].charAt(0) == COMMAND_SENSOR_EVENT)
              {
                MeasuringTool tool;
                char sensorType = command[0].charAt(1);
                if(sensorType == SENSOR_LIGHT)
                  tool = new ToolLight();
                else if(sensorType == SENSOR_MOTION)
                  tool = new ToolMotion();
                else //if(sensorType == SENSOR_TEMPERATURE) // An assumption sX where X is anything unforseen won't come in here...
                  tool = new ToolTemp();

                try
                { lastSensorVal = tool.getValue(); }
                catch(IOException e)
                { ConnectionSPOT.throwError(e, 1, 4); }

                Blinker.blink(500, 500, 0, 255, 0, tool.getLEDs(), 3);
                sendSensorChangeTimes(tool, streamConn);
              }
            }
          });
        }
        catch(IOException e)
        { ConnectionSPOT.throwError(e, 1, 2); }
        activeAction.start();
      }
    }
  }
  
  private void sendSensorChangeTimes(MeasuringTool tool, DataOutputStream streamConn)
  {
    while(true)
    {
      try
      {
        long startTime = System.currentTimeMillis();
        double curVal = tool.getValue();
        if(curVal + tool.getTriggerDelta() < lastSensorVal || curVal - tool.getTriggerDelta() > lastSensorVal)
          streamConn.writeLong(System.currentTimeMillis());
        lastSensorVal = curVal;
        Utils.sleep(tool.getWaitTime() - (System.currentTimeMillis() - startTime));
      }
      catch(Exception e)
      { return; } // ConnectionSPOT.throwError(e, 1, 3);
    }
  }
  
  private void sendZoneData(DataOutputStream streamConn)
  {
    while(true)
    {
      long startTime = System.currentTimeMillis();

      try
      {
        streamConn.writeDouble(lightSensor.getAverageValue() * 2.0);
        streamConn.writeDouble(temperatureSensor.getCelsius());
        streamConn.writeLong(System.currentTimeMillis());
      }
      catch(Exception e)
      { return; } // ConnectionSPOT.throwError(e, 1, 1);

      Blinker.blink(500, 0, 0, 255, 0, 128, 1); // Right green LED - transmitting
      Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - startTime));
    }
  }
}