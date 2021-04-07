package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class Commit {

  @Expose
  private String path;

  @Expose
  private String mode;

  @Expose
  private boolean autorename;

  @Expose
  private boolean mute;

  @Expose
  private boolean strict_conflict;

  public Commit() {}

  public void setPath(String path) {this.path = path;}
  public String getPath() {return this.path;}

  public void setMode(String mode) {this.mode = mode;}
  public String getMode() {return this.mode;}

  public void setAutorename(boolean autorename) {this.autorename = autorename;}
  public boolean getAutorename() {return this.autorename;}

  public void setMute(boolean mute) {this.mute = mute;}
  public boolean getMute() {return this.mute;}

  public void setStrict_conflict(boolean strict_conflict) {this.strict_conflict = strict_conflict;}
  public boolean getStrict_conflict() {return this.strict_conflict;}
}
