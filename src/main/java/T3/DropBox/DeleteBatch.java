package T3.DropBox;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@XmlRootElement
public class DeleteBatch {

  @Expose
  private List<Entry> entries = new ArrayList<>();

  public DeleteBatch() {}

  public List<Entry> getEntries() {return entries;}
  public void addEntry(Entry e) { this.entries.add(e); }
}
