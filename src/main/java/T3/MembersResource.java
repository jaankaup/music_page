package T3;

import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import SQL.T3SQL;

@Path("members")
public class MembersResource {
  
  private @Context ServletContext sc;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Member> getAllMembers(@Context UriInfo uriInfo) {

      T3SQL jee = new T3SQL(sc);
      List<Member> members = jee.getAllMembers();
      URIFactory.addLinksToMembers(members,uriInfo);
      return members;
  }

  @GET
  @Path("/{memberId}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Member> getMember(@Context UriInfo uriInfo, @PathParam("memberId") int id) {
      T3SQL jee = new T3SQL(sc);
      List<Member> members = jee.getMember(new int[]{id});
      if (members.size() == 0) throw new DataNotFoundException("Member not found.");
      URIFactory.addLinksToMembers(members,uriInfo);
      return members;
  }
}
