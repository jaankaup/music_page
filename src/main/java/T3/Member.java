package T3;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class Member {
  // transient
  @Expose private int id;
  @Expose private String name;
  private String password;
  private List<String> status = new ArrayList<>();
  @Expose private List<Link> links = new ArrayList<>();

  public void setId(int id) { this.id = id;}
  public int getId() { return id; }

  public void setName(String name) { this.name = name; }
  public String getName() { return name; }

//  @JsonbTransient
  public List<String> getStatus() { return status; }
  public void setStatus(List<String> status) { this.status = status; }

//  @JsonbTransient
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }

  public List<Link> getLinks() {return this.links;}
  public void addLink(String url, String rel) { links.add(new Link(url,rel)); } 
}
