package org.sunspotworld;

import java.io.DataInputStream;
import org.sunspotworld.tools.ToolSet;

/**
 * @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public final class CommandProcessor
{
  private static final byte CMD_LISTENERS = 0;
  private static final byte CMD_SCRIPT = 1;

  private static StreamConnectionIn streamIn = ConnectionProtocolSPOT.connectToClosestBaseIn();
  
  public static void startProcessor()
  {
    DataInputStream inputStream = streamIn.getConn();
    while(true)
    {
      try
      {
        byte type = inputStream.readByte();
        
        if(type == CMD_LISTENERS)
          ToolSet.resetListeners(inputStream.readUTF());
        else if(type == CMD_SCRIPT)
          ScriptProcessor.process(inputStream.readUTF());
      }
      catch(Exception e)
      { Logger.log(Logger.CRITICAL, "Command listener failed"); }
    }
  }
}
