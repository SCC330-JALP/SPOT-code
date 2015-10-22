/**
* @author Povilas Marcinkevicius
* 
* @version 1.0.0
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

  private /*ConnectionSPOT conn*/ DataOutputStream streamConn = ConnectionSPOT.getClosestConnection(127, ConnectionSPOT.PORT_BASE_SEARCH).getOutputStreamConn();
  
  protected void pauseApp() {}
  protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {}
  protected void startApp() throws MIDletStateChangeException
  {
    if(streamConn /*conn*/ != null)
    while(true)
    {
      long startTime = System.currentTimeMillis();
      /*Radiogram radiogram = conn.getNewRadiogram();
      radiogram.writeDouble(lightSensor.getAverageValue() * 2.0);
      radiogram.writeDouble(temperatureSensor.getCelsius());
      conn.send();*/

      try
      {
        streamConn.writeDouble(lightSensor.getAverageValue() * 2.0);
        streamConn.writeDouble(temperatureSensor.getCelsius());
        streamConn.writeLong(System.currentTimeMillis());
      }
      catch(IOException e)
      { ConnectionSPOT.throwError(e, 17); }

      Blinker.blink(500, 0, 0, 255, 0, 128, 1); // Right green LED - transmitting
      Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - startTime));
    }
  }
}