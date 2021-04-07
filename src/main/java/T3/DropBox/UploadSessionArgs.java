package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class UploadSessionArgs {

  @Expose
  private Cursor cursor;

  @Expose
  private boolean close;

  public UploadSessionArgs() {}

  public void setCursor(Cursor cursor) {this.cursor = cursor;}
  public Cursor getCursor() {return this.cursor;}

  public void setClose(boolean close) {this.close = close;}
  public boolean getClose() {return this.close;}
}
