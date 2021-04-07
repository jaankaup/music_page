package T3;

import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;
//import javax.json.bind.annotation.*;

@XmlRootElement
public class Concert {

  @Expose private int id;
  @Expose private String name;
  @Expose private String date;
  private List<PlayerInfo> pi = new ArrayList<>();
  private List<Song> songs = new ArrayList<>();
  @Expose private List<Link> links = new ArrayList<>();

  public Concert() {}

  public void setId(int id) {this.id = id;}
  public int getId() {return this.id;}

  public void setName(String name) {this.name = name;}
  public String getName() {return this.name;}

  public void setDate(String date) {this.date = date;}
  public String getDate() {return this.date;}

  //@JsonbTransient
  //@XmlTransient
  public List<PlayerInfo> getPi() {return pi;}
  public void addPi(PlayerInfo pi) {this.pi.add(pi);}

  //@JsonbTransient
  //@XmlTransient
  public List<Song> getSongs() {return songs;}
  public void addSong(Song songs) {this.songs.add(songs);}

  public List<Link> getLinks() {return this.links;}

  public void addLink(String url, String rel) { links.add(new Link(url,rel)); } 
}
