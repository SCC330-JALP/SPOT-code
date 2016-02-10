package org.sunspotworld;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Povilas marcinkevicius
 * @veraion 1.0.0
 */
public class Logger
{
  public static final byte ID_LOG = 13;
  
  public static final byte INFO     = 0;
  public static final byte WARN     = 1;
  public static final byte ERROR    = 2;
  public static final byte CRITICAL = 3;
  
  public static void log(byte level, String message)
  {
    if(Spot331.streamOut == null)
      return;
    
    if(level > CRITICAL)
      throw new IllegalArgumentException();

    DataOutputStream conn = Spot331.streamOut.getConn();
    try
    {
      conn.writeByte(ID_LOG);
      conn.writeByte(level);
      conn.writeUTF(message);
      conn.flush();
    }
    catch (IOException e)
    { Blinker.blink(1000, 1000, 255, 0, 0, 255, 50); }
    
    Spot331.streamOut.done();
  }
}
