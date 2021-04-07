package T3;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class ErrorMessage {
  @Expose private String errorMessage;
  @Expose private int errorCode;
  @Expose private String documentation;
  public ErrorMessage(){}
  public ErrorMessage(String errorMessage, int errorCode, String documentation) {
    super();
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
    this.documentation = documentation;
  }

  public String getErrorMessage() { return this.errorMessage; }
  public void setErrorMessage(String message) { this.errorMessage = message; }

  public int getErrorCode() { return this.errorCode; }
  public void setErrorCode(int code) { this.errorCode = code; }

  public String getDocumentation() { return this.documentation; }
  public void setDocumentation(String documentation) { this.documentation = documentation; }
}
