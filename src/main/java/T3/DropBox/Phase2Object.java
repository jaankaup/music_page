package T3.DropBox;

import javax.xml.bind.annotation.*;
import com.google.gson.annotations.Expose;

@XmlRootElement
public class Phase2Object {

  @Expose
  private String code;

  @Expose
  private String token;

  @Expose
  private String grant_type;

  @Expose
  private String client_id;

  @Expose
  private String client_secret;

  @Expose
  private String redirect_uri;
  

  public String getCode() { return this.code; }
  public void setCode(String code) { this.code = code; }

  public String getToken() { return this.token; }
  public void setToken(String token) { this.token = token; }

  public String getGrant_type() { return this.grant_type; }
  public void setGrant_type(String grant_type) { this.grant_type = grant_type; }

  public String getClient_id() { return this.client_id; }
  public void setClient_id(String client_id) { this.client_id = client_id; }

  public String getClient_secret() { return this.client_secret; }
  public void setClient_secret(String client_secret) { this.client_secret = client_secret; }

  public String getRedirect_uri() { return this.redirect_uri; }
  public void setRedirect_uri(String redirect_uri) { this.redirect_uri = redirect_uri; }
}
