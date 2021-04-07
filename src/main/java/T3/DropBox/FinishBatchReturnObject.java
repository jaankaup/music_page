package T3.DropBox;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class FinishBatchReturnObject {

  @SerializedName(".tag")
  @Expose
  private String tag;

  @Expose
  private String async_job_id;

  @Expose
  private List<FinishBatchReturnEntry> entries = new ArrayList<>();

  public FinishBatchReturnObject() {}

  public void setTag(String tag) {this.tag = tag;}
  public String getTag() {return this.tag;}

  public void setAsync_job_id(String value) {this.async_job_id = value;}
  public String getAsync_job_id() {return this.async_job_id;}

  public List<FinishBatchReturnEntry> getEntries() {return this.entries;}

}
