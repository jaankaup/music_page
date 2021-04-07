package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class AppendEntry {

  @Expose
  private Cursor cursor;

  @Expose
  private Commit commit;


  public AppendEntry() {}

  public void setCursor(Cursor cursor) {this.cursor = cursor;}
  public Cursor getCursor() {return this.cursor;}

  public void setCommit(Commit commit) {this.commit = commit;}
  public Commit getCommit() {return this.commit;}
}
