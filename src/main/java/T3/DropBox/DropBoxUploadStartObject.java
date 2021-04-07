package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class DropBoxUploadStartObject {

  @Expose
  private String session_id;


  public DropBoxUploadStartObject() {}

  public void setSession_id(String session_id) {this.session_id = session_id;}
  public String getSession_id() {return this.session_id;}

}
