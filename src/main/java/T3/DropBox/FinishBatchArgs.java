package T3.DropBox;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class FinishBatchArgs {

  @Expose
  private List<AppendEntry> entries = new ArrayList<>();

  public FinishBatchArgs() {}

  public List<AppendEntry> getEntries() {return this.entries;}
  public void addEntry(AppendEntry a) {this.entries.add(a);}
}

