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

/**
* @author Povilas Marcinkevicius
* @version 1.2.3
**/
public class Week4SPOT extends MIDlet
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

  private ConnectionSPOT radiogramConn;// = ConnectionProtocolSPOT.getClosestBaseMac(); /*127, ConnectionProtocolSPOT.PORT_BASE_SEARCH*/
  private DataOutputStream streamConn; // not final because changes on command
  private final ZoneChecker zoneChecker = new ZoneChecker(3);
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
    String closestAddress = ConnectionProtocolSPOT.getClosestBaseMac();
    if(closestAddress == null)
      Blinker.blinkAndWait(600, 600, 0, 0, 255, 195, 3); // 1100 0011 3x blue
    else
    {
      Blinker.blinkAndWait(600, 600, 0, 255, 0, 195, 3); // 1100 0011 3x green
      radiogramConn = new ConnectionSPOT(closestAddress, ConnectionProtocolSPOT.PORT_BASE_SEARCH, 127);
      streamConn = ConnectionProtocolSPOT.getOutputStreamConn(radiogramConn, "");
    
      final ConnectionSPOT commandListener = new ConnectionSPOT(ConnectionSPOT.LISTEN, PORT_COMMAND_RELAY, 10); // Never closed
      final Thread activeActionThread[] = {new Thread(new Runnable(){public void run(){ Utils.sleep(Long.MAX_VALUE); }})}; // Just so it's not null and pointer
      
      Thread commandListenerThread = new Thread(new Runnable()
      { public void run() { listenForCommands(commandListener, activeActionThread); }});
      commandListenerThread.start();
      
      while(true)
      {
        String closestBase = ConnectionProtocolSPOT.getClosestBaseMac();
        boolean changeConn = zoneChecker.shouldIConnectToThis(closestBase);
        ConnectionSPOT.printRemote("Closest Base: " + closestBase + "; Connect: " + changeConn);
        
        if(changeConn)
        {
          Blinker.blink(500, 500, 0, 0, 255, 54, 5); // 0110 0110

          try
          {
            radiogramConn.getNewRadiogram().writeUTF(ConnectionProtocolSPOT.STREAM_KILL);
            radiogramConn.send();
            Utils.sleep(1000); // To make sure message is delivered
            radiogramConn.close();
          }
          catch(Exception e) { /* We expect exceptions here; ignore */ }
          
          radiogramConn = new ConnectionSPOT(closestBase, ConnectionProtocolSPOT.PORT_BASE_SEARCH, 127);
          reconnect(activeActionThread); // Reconnects with the new radiogramConn
        }
        //Utils.sleep(10000); Listening is in-line with the thread so no need for artificial delay
      }
    }
  }
  
  private void reconnect(Thread currentListener[])
  {
    Blinker.blinkAndWait(500, 500, 0, 255, 0, 129, 1); // Blink 1000 0001 GREEN x5
    try
    {
      streamConn.close();
      currentListener[0].interrupt();
      currentListener[0].interrupt();
    }
    catch(Exception e)
    { /* We expect exceptions here; ignore */ }

    currentListener[0] = new Thread(new Runnable()
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
          { Blinker.blinkAndWaitError(1, 4); }

          Blinker.blink(500, 500, 0, 255, 0, tool.getLEDs(), 3);
          sendSensorChangeTimes(tool, streamConn);
        }
      }
    });
    currentListener[0].start();
  }
  
  private void listenForCommands(ConnectionSPOT commandListener, Thread activeActionThread[])
  {
    while(true)
    {
      try
      {
        command[0] = commandListener.receive().readUTF();
        reconnect(activeActionThread);
      }
      catch(IOException e)
      { return; }
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
        {
          streamConn.writeLong(System.currentTimeMillis());
          streamConn.flush();
        }
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
        streamConn.flush();
      }
      catch(Exception e)
      { return; } // ConnectionSPOT.throwError(e, 1, 1);

      Blinker.blink(500, 0, 0, 255, 0, 128, 1); // Right green LED - transmitting
      Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - startTime));
    }
  }
}




/*Ok, final thing I think (just to sum up and double-check):

For travel it seems the best way of doing it is getting off-peak return to Patricroft train station, get the 6:36 train (yes, yes, it's really early, but it starts at 9:15) and take a taxi from there (10 mins).

As for people coming, I'll confirm 12 people next Saturaday if nothing comes up:

1) Povilas Marcinkevi?ius
2) Anna Guo
3) Andrei Sbarcea
4) John P. Salliaris
5) Nikolas Papakosta
6) Samantha Joanne Luton
7) David Buxton
8) Kristen Keating
9) Lucy Shen
10) WeiMin Zhou
11) Robert Tiberiu Novac
12) Luke Luya

Please bring your own food (they sell it at the place but it's expensive and bad) and have money to buy paintballs. The free 100 will be provided for the last few matches, so you'll need your own for the beginning. 8 pounds per 100. I'd imagine you'll need 300ish (not sure though), but, just in case, have somemoney for extra :P especially if you spray and pray* you'll need it :D

*Spray and pray: A term commonly used amongst FPS players, which refers to the practice of unloading your clip in the general direction of another player in the hopes that you'll hit them. Generally considered a good indicator that someone is a noob.*/