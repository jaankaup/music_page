package T3.DropBox;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class PropertyGroup {

  @Expose
  private String template_id;

  @Expose
  private List<Field> fields = new ArrayList<>();

  public PropertyGroup() {}

  public void setTemplate_id(String template_id) {this.template_id = template_id;}
  public String getTemplate_id() {return this.template_id;}

  public List<Field> getFields() {return this.fields;}
}
