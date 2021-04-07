package T3;
import java.lang.RuntimeException;

public class BadRequestException extends RuntimeException{
  private static final long serialVersionUID = -6672553621676928688L;
  public BadRequestException(String message){
    super(message);
  }
}
