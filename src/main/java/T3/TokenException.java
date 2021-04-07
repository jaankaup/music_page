package T3;

import java.lang.RuntimeException;

public class TokenException extends RuntimeException{
  private static final long serialVersionUID = -6672553621676928681L;
  public TokenException(String message){
    super(message);
  } 
}
