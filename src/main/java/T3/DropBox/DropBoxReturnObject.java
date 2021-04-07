package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class DropBoxReturnObject {

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
  private String size;

  @Expose
  private String hash;

  public DropBoxReturnObject() {}

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

  public void setSize(String size) {this.size = size;}
  public String getSize() {return this.size;}

  public void setHash(String hash) {this.hash = hash;}
  public String getHash() {return this.hash;}
}
