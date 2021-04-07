package T3;

import com.google.gson.annotations.Expose;

public class Link {
  @Expose private String link;
  @Expose private String rel;

  public Link() {}
  public Link(String link, String rel) { this.link = link; this.rel = rel; }

  public void setLink(String s) { link = s; }
  public void setRel(String s) { rel = s; }
  public String getLink() { return link; }
  public String getRel() { return rel; }
}
