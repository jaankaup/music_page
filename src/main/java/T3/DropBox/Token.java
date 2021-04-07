package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class Token {
  
  @Expose
  private String access_token;

  @Expose
  private String account_id;

  @Expose
  private String token_type;

  @Expose
  private String uid;

  public void setAccess_token(String access_token) {this.access_token = access_token;}
  public String getAccess_token() {return this.access_token;}

  public void setAccount_id(String account_id) {this.account_id = account_id;}
  public String getAccount_id() {return this.account_id;}

  public void setToken_type(String token_type) {this.token_type = token_type;}
  public String getToken_type() {return this.token_type;}

  public void setUid(String uid) {this.uid = uid;}
  public String getUid() {return this.uid;}
}
