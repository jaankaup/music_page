package T3;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

public class Code {
  
  @Expose
  private String code;

  public void setCode(String code) {this.code = code;}
  public String getCode() {return this.code;}
}
