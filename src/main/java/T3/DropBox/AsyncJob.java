package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class AsyncJob {

  @Expose
  private String async_job_id;

  public AsyncJob() {}

  public void setAsync_job_id(String id) {this.async_job_id = id;}
  public String geAsync_job_id() {return this.async_job_id;}

}
