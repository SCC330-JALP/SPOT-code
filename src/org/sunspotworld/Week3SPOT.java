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
import com.sun.spot.util.Utils;
import java.io.DataOutputStream;
import java.io.IOException;

public class Week3SPOT extends MIDlet
{
  private final int SAMPLE_PERIOD = 5000; // TODO: Change to 3600000 (an hour)
  
  private final ILightSensor lightSensor = (ILightSensor) Resources.lookup(ILightSensor.class);
  private final ITemperatureInput temperatureSensor = (ITemperatureInput) Resources.lookup(ITemperatureInput.class);

  private DataOutputStream streamConn = ConnectionProtocolSPOT.getOutputStreamConn(ConnectionProtocolSPOT.getClosestConnection(127, ConnectionProtocolSPOT.PORT_BASE_SEARCH));
  
  protected void pauseApp() {}
  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}
  protected void startApp() throws MIDletStateChangeException
  {
    if(streamConn /*conn*/ != null)
    while(true)
    {
      long startTime = System.currentTimeMillis();

      try
      {
        streamConn.writeDouble(lightSensor.getAverageValue() * 2.0);
        streamConn.writeDouble(temperatureSensor.getCelsius());
        streamConn.writeLong(System.currentTimeMillis());
      }
      catch(IOException e)
      { ConnectionSPOT.throwError(e, 1, 1); }

      Blinker.blink(500, 0, 0, 255, 0, 128, 1); // Right green LED - transmitting
      Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - startTime));
    }
  }
}