package T3;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.JsonAdapter;
//import javax.json.bind.annotation.*;

@XmlRootElement
public class Song {

  @Expose
  @SerializedName("id")
  private int id;

  @Expose
  @SerializedName("songName")
  private String songName;

  @SerializedName("fileName")
  private String fileName;

  private String info;

  @Expose
  @SerializedName("concertId")
  private int concertId;

  @Expose
  @SerializedName("links")
  private List<Link> links = new ArrayList<>();

  @SerializedName("file")
  private String file;

  public Song() {}

  public void setId(int id) {this.id = id;}
  public int getId() {return this.id;}

  public void setConcertId(int concertId) {this.concertId = concertId;}
  public int getConcertId() {return this.concertId;}

  public void setSongName(String songName) {this.songName = songName;}
  public String getSongName() {return this.songName;}

  public void setInfo(String info) {this.info = info;}
  public String getInfo() {return this.info;}

  // This seems to work only with xml.
  //@XmlTransient
//  @JsonbTransient
  public String getFileName() {return fileName;}
  public void setFileName(String fileName) {this.fileName = fileName;}

  public List<Link> getLinks() {return this.links;}

  public void addLink(String url, String rel) { links.add(new Link(url,rel)); } 

  public void setFile(String file) {this.file = file;}
  public String getFile() {return this.file;}
}
