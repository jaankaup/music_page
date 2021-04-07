package T3;

import java.util.List;
import java.util.ArrayList;
import java.security.Principal;

public class User implements Principal{
  private String firstName, lastName,
          login, password;
  private List<String> role;

  public User(String firstName, String lastName, String login, String password){
    this.firstName = firstName;
    this.lastName = lastName;
    this.login = login;
    this.password = password;
    this.role = new ArrayList<String>();
  };
   
    // getters and setters
    @Override
    public String getName() {
      return this.firstName + " " + this.lastName;
    }
    
    public List<String> getRole() { return role; }
}
