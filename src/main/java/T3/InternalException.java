package T3;
import java.lang.RuntimeException;

public class InternalException extends RuntimeException{
  private static final long serialVersionUID = -6672553621676928689L;
  public InternalException(String message){
    super(message);
  } 
}
