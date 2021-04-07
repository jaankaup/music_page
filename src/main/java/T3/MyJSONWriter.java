package T3;

import java.lang.annotation.Annotation;
import javax.ws.rs.ext.Provider;
import java.lang.Override;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.MessageBodyWriter;
import java.lang.reflect.Type;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;

//@Produces("application/json; charset=UTF-8")
//@Produces({"application/json", "text/plain; charset=UTF-8"})
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class MyJSONWriter implements MessageBodyWriter<Object>{

  private final static Logger LOGGER = Logger.getLogger(MyJSONWriter.class.getName());

  @Override
  public long getSize(Object arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
      return -1;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type arg1, Annotation[] arg2, MediaType arg3) {
      return true;
//      return T.class.isAssignableFrom(type);
  }

  @Override
  public void writeTo(Object type, Class<?> genericType, Type type1, Annotation[] annot, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out)
    throws IOException, WebApplicationException {
//        if (String.class == genericType)
//          return out.write(type.cast(json));
        //OutputStreamWriter writer = new OutputStreamWriter(out, "UTF_8");
        OutputStreamWriter writer = new OutputStreamWriter(out,StandardCharsets.UTF_8);
        //OutputStreamWriter writer = new OutputStreamWriter(out, "UTF_8");

        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            //String jsonString = gson.toJson(type, genericType);
            //LOGGER.log(Level.INFO,"jsonString:");
            //LOGGER.log(Level.INFO,jsonString);
            //byte[] jsonStringUTF8 = jsonString.getBytes("UTF8");
            //writer.write(jsonStringUTF8);
            //LOGGER.log(Level.INFO,"jsonStringUTF8:");
            //LOGGER.log(Level.INFO,jsonStringUTF8.toString());
            //out.write(jsonStringUTF8, 0, jsonStringUTF8.length);
            gson.toJson(type, genericType, writer);
        } finally {
            writer.close();
        }
    }
}
