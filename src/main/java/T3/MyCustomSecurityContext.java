package T3;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class MyCustomSecurityContext implements SecurityContext{
  private User user;
  private String scheme;

  public MyCustomSecurityContext(User user, String scheme) {
    this.user = user;
    this.scheme = scheme;
  }
  @Override
  public Principal getUserPrincipal() {
    return this.user;
  }
  @Override
  public boolean isUserInRole(String role) {
    if (user.getRole() != null) {
      return user.getRole().contains(role);
    } return false;
  }
  @Override
  public boolean isSecure() {
    return "https".equals(this.scheme);
  }
  @Override
  public String getAuthenticationScheme() {
    return SecurityContext.BASIC_AUTH;
  }
}
