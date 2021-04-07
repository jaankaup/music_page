package T3;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Provider // the annotation preregisters our Mapper for JAX-RS to be used
public class InternalExceptionMapper implements ExceptionMapper<InternalException>{
  @Override
  public Response toResponse(InternalException ex) {
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(),500,"https://jaankaup1.ties478.website/Task3/api.html");
    return Response.status(Status.NOT_FOUND)
      .entity(errorMessage)
      .build();
  }
}
