package T3;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.lang.Throwable;
import javax.ws.rs.WebApplicationException;
import java.util.logging.Logger;
import java.util.logging.Level;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable>{
  private final static Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());
  @Override
  public Response toResponse(Throwable ex) {
//    LOGGER.log(Level.INFO,ex.getStackTrace().toString());
//    Response response;
//    if (ex instanceof WebApplicationException) {
//        WebApplicationException webEx = (WebApplicationException)ex;
//        response = webEx.getResponse();
//        return response;
//    }

    //ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(),500,"http://myDocs.org");
    //ErrorMessage errorMessage = new ErrorMessage(ex.getClass().getName(),500,"http://myDocs.org");
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(),500,"http://myDocs.org");
    return Response.status(Status.INTERNAL_SERVER_ERROR)
      .entity(errorMessage)
      .build();
  }
}
