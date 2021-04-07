
package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class SharingInfo {

  @SerializedName(".tag")
  @Expose
  private boolean read_only;

  @Expose
  private String parent_shared_folder_id;

  @Expose
  private String modified_by;

  public SharingInfo() {}

  public void setRead_only(boolean readOnly) {this.read_only = read_only;}
  public boolean getRead_only() {return this.read_only;}

  public void setParent_shared_folder_id(String parent_shared_folder_id) {this.parent_shared_folder_id = parent_shared_folder_id;}
  public String getParent_shared_folder_id() {return this.parent_shared_folder_id;}

  public void setModified_by(String modified_by) {this.modified_by = modified_by;}
  public String getModified_by() {return this.modified_by;}

}
