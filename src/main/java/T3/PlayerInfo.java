package T3;
import java.util.List;
import java.util.ArrayList;
import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class PlayerInfo {
//  private int playerId;
//  private String name;
  @Expose private Member member;
  @Expose private List<String> instruments = new ArrayList<>();

  public PlayerInfo() {}

  public Member getMember() { return member; }
  public void setMember(Member member) { this.member = member ; }
  public List<String> getInstruments() { return instruments; }

  public void addInstrument(String instrument) {
     instruments.add(instrument);
  }
}
