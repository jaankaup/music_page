package T3;

import java.text.ParseException;
import java.lang.reflect.Field;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.StringBuilder;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import java.lang.reflect.Method;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.Context;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import org.glassfish.jersey.internal.util.Base64;
import javax.servlet.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MediaType;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.json.JSONArray;
import SQL.T3SQL;

@Provider
public class SecurityFilter implements ContainerRequestFilter {
  private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
  private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
  private static final String SECURED_URL_PREFIX = "secured";
  private static final String DOCUMENTATION = "https://jaankaup1.ties478.website/Task3/api.html";
  private static final ErrorMessage FORBIDDEN_ErrMESSAGE = new ErrorMessage("Access blocked for all users !!!", 403, DOCUMENTATION);
  private static final ErrorMessage UNAUTHORIZED_ErrMESSAGE = new ErrorMessage("User cannot access the resource.", 401, DOCUMENTATION);
  private final static Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

  @Context private ResourceInfo resourceInfo;
  @Context private ServletContext sc;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    UriInfo ui = requestContext.getUriInfo();     
    String path = ui.getPath();
    String method = requestContext.getMethod();
    MediaType meditaType = requestContext.getMediaType();

    LOGGER.log(Level.INFO,"Security filter:");

    if (!checkRole(requestContext)) {
      unauthorized(requestContext); 
      return;
    };
  }

  /* Perform authentication. */
  private boolean authenticate(ContainerRequestContext requestContext, Map<String,String> up) {
    String username = up.get("username");
    String password = up.get("password");
    if ("user".equals(username) && "password".equals(password)) { return true; }

    ErrorMessage errorMessage = new ErrorMessage("User cannot access the resource.", 401,
        "http://myDocs.org");
    Response unauthorizedStatus = Response.status(Response.Status.UNAUTHORIZED)
      .entity(errorMessage)
      .build();
    requestContext.abortWith(unauthorizedStatus);

    return false;
  }

  private void unauthorized(ContainerRequestContext requestContext) {
    Response response = Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_ErrMESSAGE).build();
    requestContext.abortWith(response);
  }

  private boolean checkRole(ContainerRequestContext requestContext) {
    LOGGER.log(Level.INFO,"checking role");
    Method resMethod = resourceInfo.getResourceMethod();
    Class<?> resClass = resourceInfo.getResourceClass();

    if(resMethod.isAnnotationPresent(PermitAll.class)){ return true; }
    if(resMethod.isAnnotationPresent(DenyAll.class)){
      Response response = Response.status(Response.Status.FORBIDDEN)
        .entity(FORBIDDEN_ErrMESSAGE).build();
      requestContext.abortWith(response);
      return false;
    }
    if(resMethod.isAnnotationPresent(RolesAllowed.class)){
      boolean succeed = false;
      Map<String,String> parsedBasicData = null;
      try {
        parsedBasicData = parseBasic(requestContext);
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.toString());
        return false;
      }
      LOGGER.log(Level.INFO,parsedBasicData.toString());
      String username = parsedBasicData.get("username");
      String password = parsedBasicData.get("password");
      if (username == null || password == null) return false;
      T3SQL jee = new T3SQL(sc);
      List<Member> member = jee.getMemberByName(username);
      if (member.size() != 1)  return false;
      Member me = member.get(0);
      if (password.equals(me.getPassword()) && username.equals(me.getName())) {
        return rolesMatched(me,resMethod.getAnnotation(RolesAllowed.class).value());  
      }
    }
    // TODO: Refactor!!!
    if(resClass.isAnnotationPresent(PermitAll.class)){ return true; }
    if(resClass.isAnnotationPresent(DenyAll.class)){
      Response response = Response.status(Response.Status.FORBIDDEN)
        .entity(FORBIDDEN_ErrMESSAGE).build();
      requestContext.abortWith(response);
      return false;
    }
    if(resClass.isAnnotationPresent(RolesAllowed.class)){
      boolean succeed = false;
      Map<String,String> parsedBasicData = null;
      try {
        parsedBasicData = parseBasic(requestContext);
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.toString());
        return false;
      }
      String username = parsedBasicData.get("username");
      String password = parsedBasicData.get("password");
      if (username == null || password == null) { LOGGER.log(Level.INFO,"username == null || password == null)");  return false; }
      T3SQL jee = new T3SQL(sc);
      List<Member> member = jee.getMemberByName(username);
      if (member.size() != 1) { LOGGER.log(Level.INFO,"member.size() != 1"); return false; }
      Member me = member.get(0);
      if (password.equals(me.getPassword()) && username.equals(me.getName())) {
        try {
           String[] s = resClass.getAnnotation(RolesAllowed.class).value();
        }
        catch (Exception e) {
          LOGGER.log(Level.INFO,e.toString());
        }
        return rolesMatched(me,resClass.getAnnotation(RolesAllowed.class).value());  
      }
    }
    return false;
  }

  private boolean rolesMatched(Member user, String[] roles) {
    List<String> userRoles = user.getStatus();
    for (int i=0 ; i<userRoles.size() ; i++) {
      for (int j=0 ; j<roles.length ; j++) {
        LOGGER.log(Level.INFO,userRoles.get(i) + " == " + roles[j]);
        if (userRoles.get(i).equalsIgnoreCase(roles[j])) return true;
      }
    }
    return false;
  }

  private Map<String,String> parseBasic(ContainerRequestContext requestContext) {

    Map<String,String> hm = new HashMap<>(); 
    List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER_KEY);
    if (authHeader != null) LOGGER.log(Level.INFO,authHeader.toString());
    if (authHeader != null && authHeader.size() > 0) {
      String authToken = authHeader.get(0);
      authToken = authToken.replaceFirst(AUTHORIZATION_HEADER_PREFIX, "");
      String decodedString = Base64.decodeAsString(authToken);
      StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
      String username = tokenizer.nextToken();
      String password = tokenizer.nextToken();
      hm.put("username",username.trim());
      hm.put("password",password.trim());
      return hm;
    }
    return hm;
  }

  /* This is not needed. */
  private void unmarshallJSON(Class<?> classType, String jsonData) throws JAXBException, ParseException {
    // Create a JaxBContext
		JAXBContext jc = JAXBContext.newInstance(classType);

		// Create the Unmarshaller Object using the JaxB Context
		Unmarshaller unmarshaller = jc.createUnmarshaller();

		// Set the Unmarshaller media type to JSON or XML
		unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE,"application/json");

		// Set it to true if you need to include the JSON root element in the
		// JSON input
		unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
		//unmarshaller.setProperty(UnmarshallerProperties.BEAN_VALIDATION_MODE, true);
		unmarshaller.setProperty(UnmarshallerProperties.JSON_TYPE_COMPATIBILITY, true);
		unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
//		unmarshaller.setProperty(UnmarshallerProperties.JSON_VALUE_WRAPPER, true);

		// Create the StreamSource by creating StringReader using the JSON input
		StreamSource json = new StreamSource(new StringReader(jsonData));

		// Getting the employee pojo again from the json
		//Concert c = unmarshaller.unmarshal(json, Concert.class).getValue();    
    if (classType == Concert.class) {
		  Concert c = unmarshaller.unmarshal(json, Concert.class).getValue();    
      
      StringBuilder errors = new StringBuilder();
      String date = c.getDate();
//      if (c.getId() == 0) { errors.append("Couldn't parse id."); }
      if (c.getName() == null) { errors.append("Couldn't parse name."); }
      if (date == null) { errors.append("Couldn't parse date."); }
      if (errors.length() > 0)
        throw new BadRequestException("Error occured when parsing JSON. " + errors.toString()); 
      Date temp = new SimpleDateFormat("yyyy-MM-dd").parse(date);
      if (date == null) 
        throw new BadRequestException("Error occured when parsing JSON. " + "Date must be in format dddd-mm-dd"); 
      if (c.getName().trim().length() == 0) 
        throw new BadRequestException("Error occured when parsing JSON. " + "Name can't be an empty string."); 
      }
    if (classType == PlayerInfo.class ) {
      LOGGER.log(Level.INFO,"PlayerIfno");
		  PlayerInfo pi = unmarshaller.unmarshal(json, PlayerInfo.class).getValue();    
    }
  }
    //if (c.getId() == 0) { errors.add("Couldn't parse id."); }
//        throw new BadRequestException("Error occured when parsing JSON. Couldn't parse the id."); 

  /* Get the data from request. */
  private byte[] getRequestContent(ContainerRequestContext requestContext) throws IOException {
    int length = requestContext.getLength();
    byte[] bytes = new byte[length];
    InputStream is = requestContext.getEntityStream();
//    LOGGER.log(Level.INFO,is.getClass().getName());
//    LOGGER.log(Level.INFO,Boolean.toString(is.markSupported()));
//    is.mark(length);
    is.read(bytes, 0, length);
//    is.reset();
//    String str = new String(bytes, "UTF-8");
//    LOGGER.log(Level.INFO,str);
    InputStream newStream = new ByteArrayInputStream(bytes);
    requestContext.setEntityStream(newStream);
    return bytes;
  }
}
