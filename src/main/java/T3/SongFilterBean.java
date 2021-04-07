package T3;
import javax.ws.rs.QueryParam;

public class SongFilterBean {

  private @QueryParam("name") String name;
  private @QueryParam("userId") int userId;
  private @QueryParam("concertId") int concertId;

  public String getName() {return name;}
  public void setName(int year) {this.name = name;}
  public int getUserId() {return userId;}
  public void setUserId(int start) {this.userId = userId;}
  public int getConcertId() {return concertId;}
  public void setConcertId(int size) {this.concertId = concertId;}

}
