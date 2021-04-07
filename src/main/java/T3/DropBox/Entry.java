package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class Entry {

  @Expose
  private String path;

  public Entry() {}

  public String getPath() {return this.path;}
  public void setPath(String p) { this.path = p; }
}
