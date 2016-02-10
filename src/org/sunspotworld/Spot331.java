package org.sunspotworld;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import java.io.DataOutputStream;
import java.io.IOException;
import org.sunspotworld.tools.ListenerActuator;
import org.sunspotworld.tools.ToolSet;

/**
* @author Povilas Marcinkevicius
* @version 2.1.2
* 
**/
public class Spot331 extends MIDlet implements ListenerActuator
{  
  private static final int HEARTBEAT_DELAY = 750;

  public static StreamConnectionOut streamOut;
  
  protected void pauseApp() {}
  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}
  protected void startApp() throws MIDletStateChangeException
  {    
    streamOut = ConnectionProtocolSPOT.connectToClosestBase();

    ToolSet.initiateHeartBeat(HEARTBEAT_DELAY);
    ToolSet.subscribeAll("main", this);
    CommandProcessor.startProcessor();
  }
  
  public void onListenerTrigger(double value, byte type)
  {
    DataOutputStream stream = streamOut.getConn();
    
    try
    {
      stream.writeByte(type);
      stream.writeDouble(value);
      stream.flush();
    }
    catch(IOException e)
    {
      Logger.log(Logger.ERROR, "Failed to write or flush listener data");
      e.printStackTrace();
    }
    
    streamOut.done();
  }
}