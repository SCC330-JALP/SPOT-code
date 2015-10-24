package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import java.io.IOException;
import javax.microedition.io.Connector;

/**
 * @author Povilas Marcinkevicius
 * 
 * @version 1.0.1
 */
public class ConnectionSPOT
{
  public static final String BROADCAST = "broadcast";
  public static final String LISTEN = "";
  
  private RadiogramConnImpl conn;
  private Radiogram radiogram;
  
  public String address;
  
  public ConnectionSPOT(String addr, int port, int radiogramSize)
  {
    try
    {
      address = addr;
      conn = (RadiogramConnImpl) Connector.open("radiogram://" + addr + ":" + port);
      radiogram = new Radiogram(radiogramSize, conn);
    }
    catch(IOException e)
    { throwError(e, 2, 1); }
  }
  
  public Radiogram receive() throws IOException
  {
    conn.receive(radiogram);
    return radiogram;
  }
  
  public void send()
  {
    try
    { conn.send(radiogram); }
    catch (IOException e)
    { throwError(e, 2, 2); }
  }
  
  public void close()
  {
    try
    { conn.close(); }
    catch(IOException e)
    { throwError(e, 2, 3); }
  }
  
  public Radiogram getNewRadiogram()
  {
    radiogram.reset();
    return radiogram;
  }
  
  public static final void throwError(Exception e, int classNo, int exceptionNo)
  {
    ConnectionSPOT singleConn = new ConnectionSPOT(BROADCAST, ConnectionProtocolSPOT.PORT_BASE_SEARCH, 127);
    try
    { singleConn.getNewRadiogram().writeUTF(e.toString()); }
    catch(IOException e2)
    { /* If this does not work calling itself is pointless */ }
    singleConn.send();
    singleConn.close();

    Blinker.blinkAndWait(1000, 1000, 255, 0, 0, classNo | (exceptionNo << 4), 1000);
  }
}