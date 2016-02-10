package org.sunspotworld;

/**
 * @author Povilas Marcinkevicius
 * @version 1.0.0
 */
public final class ScriptProcessor
{
  public static void process(String script)
  {
    String[] words = splitString(script, " ");
    
    if(words[0].equals("BEEP"))
      Beeper.beep(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]), Integer.parseInt(words[4]));
    else if(words[0].equals("BLINK"))
      Blinker.blink(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]), Integer.parseInt(words[4]),
          Integer.parseInt(words[5]), Integer.parseInt(words[6]), Integer.parseInt(words[7]));
  }
  
  public static String[] splitString(String string, String splitter)
  {
    String[] result = new String[20]; // For our purposes will be enough. Still bad though.
    
    int i = 0;
    while(i < 20)
    {
      int index = string.indexOf(splitter);
      if(index < 0)
        break;
      
      result[i] = string.substring(0, index); // Everything up to the splitter
      string = string.substring(index + splitter.length(), string.length()); // Everything after the splitter
      i++;
    }
    result[i] = string; // string after the last splitter
    
    String[] trimmedResult = new String[i + 1];
    for(int j = 0; j < i + 1; j++)
      trimmedResult[j] = result[j];
    
    return trimmedResult;
  }
}
