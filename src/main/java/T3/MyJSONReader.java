package T3;

import java.lang.annotation.Annotation;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyReader;
import java.lang.reflect.Type;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.GsonBuilder;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class MyJSONReader<T> implements MessageBodyReader<T> {
  private final static Logger LOGGER = Logger.getLogger(MyJSONReader.class.getName());
  @Override
  public boolean isReadable(Class<?> type, Type genericType,
      Annotation[] annotations,MediaType mediaType) {
    return true;
  }

  @Override
  public T readFrom(Class<T> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
      InputStream entityStream) throws IOException, WebApplicationException {

    /* Copy the input stream to String. Do this however you like.
     * Here I use Commons IOUtils.
     */

//    byte[] buf = IOUtils.toByteArray(entityStream);
//    LOGGER.log(Level.INFO,buf.toString());
//    IOUtils.copy(entityStream,buf);
//    LOGGER.log(Level.INFO,Integer.toString(buf.length));
//    JsonReader reader = new JsonReader(new InputStreamReader(entityStream, "UTF-8"));
//    Gson gson = new GsonBuilder().create();
//    T data = null;

    // Read file in stream mode
//    reader.beginName();
//    while (reader.hasNext()) {
//      // Read data into object model
//      try {
//      data = gson.fromJson(reader, genericType);
//      
////      if (person.getId() == 0 ) {
////        System.out.println("Stream mode: " + person);
////      }
////      break;
//    
//  } catch (Exception ex) {
//    LOGGER.log(Level.INFO,ex.getMessage());
//  }  
//    }
//    reader.close();
//    return data;
//    StringWriter writer = new StringWriter();
//    IOUtils.copy(entityStream, writer, "UTF-8");
//    String json = writer.toString();
//    LOGGER.log(Level.INFO,"json to string:");
//    LOGGER.log(Level.INFO,json);

//    InputStream is = con.getInputStream();
    InputStreamReader buff_reader = new InputStreamReader(entityStream);
    //use GSON to create a POJO directly from the input stream
//    MyType instance = new Gson().fromJson(buff_reader, MyType.class);

    /* if the input stream is expected to be deserialized into a String,
     * then just cast it
     */
//    if (String.class == genericType)
//      return type.cast(json);

    /* Just for checking. */
    try {
      return (new Gson().fromJson(buff_reader, genericType));
    }

    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new BadRequestException("Failed to parse JSON."); 
    }
 //   return new Gson().fromJson(json, genericType);
  }
}

