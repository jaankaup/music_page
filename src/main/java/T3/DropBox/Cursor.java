package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class Cursor {

  @Expose
  private String session_id;

  @Expose
  private int offset;

  public Cursor() {}

  public void setSession_id(String session_id) {this.session_id = session_id;}
  public String getSession_id() {return this.session_id;}

  public void setOffset(int offset) {this.offset = offset;}
  public int getOffset() {return this.offset;}
}
