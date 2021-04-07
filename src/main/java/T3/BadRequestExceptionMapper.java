package T3;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Provider // the annotation preregisters our Mapper for JAX-RS to be used
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException>{
  @Override
  public Response toResponse(BadRequestException ex) {
    ErrorMessage errorMessage = new ErrorMessage(ex.getMessage(),400,"https://jaankaup1.ties478.website/Task3/api.html");
    return Response.status(Status.BAD_REQUEST)
      .entity(errorMessage)
      .build();
  }
}
