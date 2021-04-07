package T3.DropBox;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class FinishBatchReturnEntry {

  @SerializedName(".tag")
  @Expose
  private String tag;

  @Expose
  private String name;

  @Expose
  private String path_lower;

  @Expose
  private String path_display;

  @Expose
  private String id;

  @Expose
  private String client_modified;

  @Expose
  private String server_modified;

  @Expose
  private String rev;

  @Expose
  private int size;

  @Expose
  private String content_hash;

  @Expose
  private boolean has_explicit_shared_members;

  @Expose
  private SharingInfo sharing_info;

  @Expose
  private List<PropertyGroup> property_groups = new ArrayList<>();

  public FinishBatchReturnEntry() {}

  public void setTag(String tag) {this.tag = tag;}
  public String getTag() {return this.tag;}

  public void setName(String name) {this.name = name;}
  public String getName() {return this.name;}

  public void setPath_lower(String path_lower) {this.path_lower = path_lower;}
  public String getPath_lower() {return this.path_lower;}

  public void setPath_display(String path_display) {this.path_display = path_display;}
  public String getPath_display() {return this.path_display;}

  public void setId(String id) {this.id = id;}
  public String getId() {return this.id;}

  public void setClient_modified(String client_modified) {this.client_modified = client_modified;}
  public String getClient_modified() {return this.client_modified;}

  public void setServer_modified(String server_modified) {this.server_modified = server_modified;}
  public String getServer_modified() {return this.server_modified;}

  public void setRev(String rev) {this.rev = rev;}
  public String getRev() {return this.rev;}

  public void setSize(int size) {this.size = size;}
  public int getSize() {return this.size;}

  public void setContent_hash(String hash) {this.content_hash = hash;}
  public String getContent_hash() {return this.content_hash;}

  public void setHas_explicit_shared_members(boolean value) {this.has_explicit_shared_members = value;}
  public boolean getHas_explicit_shared_members() {return this.has_explicit_shared_members;}

  public void setSharing_info(SharingInfo sharing_info) {this.sharing_info = sharing_info;}
  public SharingInfo getSharing_info() {return this.sharing_info;}

  public List<PropertyGroup> getProperty_groups() {return this.property_groups;}
}

