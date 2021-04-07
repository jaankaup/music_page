package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class Field {

  @Expose
  private String name;

  @Expose
  private String value;

  public Field() {}

  public void setName(String name) {this.name = name;}
  public String getName() {return this.name;}

  public void setValue(String value) {this.value = value;}
  public String getValue() {return this.value;}
}
