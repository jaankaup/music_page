package T3.DropBox;

//import java.io.UnsupportedEncodingException;
//import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.lang.StringBuilder;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.google.gson.Gson;
//import com.google.gson.stream.JsonReader;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import T3.*;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.DbxException;

@Path("dropBox")
public class DropBox {

  @Context private UriInfo uInfo;
  @Context private ServletContext sc;
  private final String appKey = "put_your_appkey_here";
  private final String secret = "put_your_secret_here";
  public static final String folder = "/Task3Music/";
  private final static Logger LOGGER = Logger.getLogger(DropBox.class.getName());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("superadmin")
  public Response getDropBox(@Context UriInfo uriInfo, String msg) {
    LOGGER.log(Level.INFO,"Calling DropBox.getDropBox.");
    String redirectURI = createRedirectURI(); 
    URI uri = null;
    try {
      uri = new URI("https://www.dropbox.com/oauth2/authorize");
    } catch (Exception e) {LOGGER.log(Level.INFO,e.getMessage()); throw new InternalException("Server error.");}
    StringBuilder requestUri = new StringBuilder(uri.toString());
    requestUri.append("?client_id=");
    try {
      requestUri.append(URLEncoder.encode(appKey,"UTF-8"));
    } catch (Exception e) { LOGGER.log(Level.INFO,e.getMessage()); throw new InternalException("Server error.");}
    requestUri.append("&response_type=code");
    requestUri.append("&redirect_uri="+redirectURI.toString());
    return Response.ok(requestUri).build();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("superadmin")
  public Response postDropBox(Code c) {
    LOGGER.log(Level.INFO,"Calling DropBox.postDropBox.");
    
      boolean error = false;
      String queryResult = "";
      StringBuilder tokenUri=new StringBuilder("code=");
      String redirectURI = createRedirectURI();
      try {
        tokenUri.append(URLEncoder.encode(c.getCode(),"UTF-8"));
        tokenUri.append("&grant_type=");
        tokenUri.append(URLEncoder.encode("authorization_code","UTF-8"));
        tokenUri.append("&client_id=");
        tokenUri.append(URLEncoder.encode(appKey,"UTF-8"));
        tokenUri.append("&client_secret=");
        tokenUri.append(URLEncoder.encode(secret,"UTF-8"));
        tokenUri.append("&redirect_uri="+redirectURI);
      }
      catch (Exception e) {
         LOGGER.log(Level.INFO,e.getMessage());
         throw new InternalException("DP failure.");
      }
      URL url = null;
      HttpURLConnection connection = null;
      BufferedReader in = null;
      try {
        url=new URL("https://api.dropbox.com/oauth2/token");
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" + tokenUri.toString().length());
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
        outputStreamWriter.write(tokenUri.toString());
        outputStreamWriter.flush();
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        queryResult = response.toString();
      }
      catch (Exception e) {
         LOGGER.log(Level.INFO,e.getMessage());
         error = true;
      }
      
      finally {
        try {
          if (in != null) in.close();
        } catch (Exception e) { LOGGER.log(Level.INFO,e.getMessage());}
        connection.disconnect();
        if (error) throw new InternalException("DP failure.");
        Gson gson = new GsonBuilder().create();
        Token token = gson.fromJson(queryResult, Token.class);
        sc.setAttribute("token", token);
//        try {
//          //LOGGER.log(Level.INFO,"NO OHOOOOOOOOO:");
//          //LOGGER.log(Level.INFO,DropBox.getInfo(sc));
//        }
//        catch (Exception e) { LOGGER.log(Level.INFO,e.getMessage()); throw new InternalException("DP error."); }
        return Response.ok().build();
      }
    }

  private String createRedirectURI() {
    String redirectURI = uInfo.getBaseUri().toString().replace("http://","https://").replace("webapi/","");
    return redirectURI;
  }

  public static Token getToken(@Context ServletContext sc) {
    Token token = (Token)sc.getAttribute("token");
    if (token == null) {
      LOGGER.log(Level.INFO,"Token not found.");
      throw new TokenException("No token found.");
    }
    return token;
  }

  public static String getInfo(@Context ServletContext sc) throws MalformedURLException, IOException, TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.getInfo.");
    Token token = DropBox.getToken(sc);
    String content = "{\"account_id\": \"" + token.getAccount_id() + "\"}";
    URL url = new URL("https://api.dropboxapi.com/" + "2" + /* token.getUid() + */ "/users/get_account");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    String queryResult = "";
    boolean error = false;
    BufferedReader in = null;
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length", "" + content.length());
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
      outputStreamWriter.write(content);
      outputStreamWriter.flush();
      in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      queryResult = response.toString();
    } catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      error = true;
    }
    
    finally {
      if (in != null) in.close();
      connection.disconnect();
      if (error) throw new InternalException("DP error.");
      LOGGER.log(Level.INFO,queryResult);
      return queryResult;
    } 
  }

  public static byte[] getFile(String fileName, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.getFile: '" + fileName + "'.");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    try {
      url = new URL("https://content.dropboxapi.com/2/files/download");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    String content = "";

    byte[] bytes = null;
    InputStream raw = null;
    try {
      connection.setDoOutput(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Dropbox-API-Arg", "{\"path\": \"" + DropBox.folder + fileName + "\"}");
//      LOGGER.log(Level.INFO,"RESPONSE CODE (download)");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));

      raw = connection.getInputStream();
      bytes = IOUtils.toByteArray(new BufferedInputStream(raw));
//      LOGGER.log(Level.INFO,"bytes.length()");
//      LOGGER.log(Level.INFO,Integer.toString(bytes.length));

    } catch (IOException e) {
      error = true;
      LOGGER.log(Level.INFO,"kosahtti!");
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally {
        try {
          if (raw != null) raw.close();
        } catch (Exception e) { LOGGER.log(Level.INFO,e.getMessage());}
      connection.disconnect();
      if (error ) throw new InternalException("Fileload error.");
      return bytes;
    } 
  }

  public static DropBoxReturnObject uploadFile(byte[] data, String fileName, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.uploadFile: '" + fileName + "'.");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    DropBoxReturnObject dpObject = null;
    try { 
      url = new URL("https://content.dropboxapi.com/2/files/upload");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/octet-stream");
      connection.setRequestProperty("Dropbox-API-Arg", "{\"path\": \"" + DropBox.folder + fileName + "\", \"autorename\": true}");
      connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data);
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (upload):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(upload):");
//      LOGGER.log(Level.INFO,queryResult);

      Gson gson = new GsonBuilder().create();
      dpObject = gson.fromJson(queryResult, DropBoxReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to save file '" + fileName + "'."); }
    return dpObject;
}

  // TODO: remove unnecessary String fileName and Servletcontext 
  public static DropBoxUploadStartObject uploadStart(byte[] data, String fileName, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.uploadStart: '" + fileName + "'.");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    DropBoxUploadStartObject  dpusObject = null;
    try { 
      url = new URL("https://content.dropboxapi.com/2/files/upload_session/start");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/octet-stream");
      connection.setRequestProperty("Dropbox-API-Arg", "{\"close\":false}");
      connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data);
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (upload start):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(upload):");
//      LOGGER.log(Level.INFO,queryResult);
      Gson gson = new GsonBuilder().create();
      dpusObject = gson.fromJson(queryResult, DropBoxUploadStartObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to upload file on phase1."); }
    return dpusObject;
}

  public static DropBoxReturnObject uploadFinish(byte[] data, String fileName, DropBoxUploadStartObject dbObject, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.uploadFinish: '" + fileName + "'.");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    DropBoxReturnObject dpObject = null;
    try { 
      url = new URL("https://content.dropboxapi.com/2/files/upload_session/finish");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/octet-stream");
      connection.setRequestProperty("Dropbox-API-Arg", "{\"cursor\":{\"session_id\":\"" + dbObject.getSession_id() + "\",\"offset\":" + data.length +"},\"commit\":{\"path\":\"" + folder + fileName + "\",\"autorename\":true}}");
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(upload):");
//      LOGGER.log(Level.INFO,queryResult);
      //Pattern pattern = Pattern.compile(".*\"session_id\": \"(.*)\".*");
      Gson gson = new GsonBuilder().create();
      dpObject = gson.fromJson(queryResult, DropBoxReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to upload file on phase2."); }
    return dpObject;
}

  public static void deleteFile(String fileName, @Context ServletContext sc) {
    LOGGER.log(Level.INFO,"Calling DropBox.deleteFile: '" + fileName + "'.");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    int returnCode = -1;
    URL url = null;
    try { 
      url = new URL("https://api.dropboxapi.com/2/files/delete_v2");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    String content = "{\"path\": \"" + DropBox.folder + fileName + "\"}";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-Length", "" + content.length());

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(content.getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (delete):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      returnCode = connection.getResponseCode();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(delete):");
//      LOGGER.log(Level.INFO,queryResult);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error && returnCode != 404) throw new InternalException("Failed to delete file '" + fileName + "'."); }
  }

  public static void uploadAppend(byte[] data, UploadSessionArgs api_args, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.uploadAppend:");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    DropBoxUploadStartObject dpusObject = null;
    try { 
      url = new URL("https://content.dropboxapi.com/2/files/upload_session/append_v2");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setUseCaches(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/octet-stream");
      connection.setRequestProperty("Dropbox-API-Arg", new Gson().toJson(api_args));
      connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data);
      outputStream.flush();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(append):");
//      LOGGER.log(Level.INFO,queryResult);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to append file."); }
}

  public static FinishBatchReturnObject finishBatch(FinishBatchArgs api_args, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.finishBatch:");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    FinishBatchReturnObject returnObject = null;
    try { 
      url = new URL("https://api.dropboxapi.com/2/files/upload_session/finish_batch");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
//      connection.setRequestProperty("Dropbox-API-Arg", new Gson().toJson(api_args));
 //     connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write((new Gson().toJson(api_args)).getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (finish batch):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(finish batch):");
//      LOGGER.log(Level.INFO,queryResult);
      Gson gson = new GsonBuilder().create();
      returnObject = gson.fromJson(queryResult, FinishBatchReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to finish batch."); }
    return returnObject;
}

  public static FinishBatchReturnObject finishBatchCheck(AsyncJob api_args, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.finishBatchCheck:");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    FinishBatchReturnObject returnObject = null;
    try { 
      url = new URL("https://api.dropboxapi.com/2/files/upload_session/finish_batch/check");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    LOGGER.log(Level.INFO,new Gson().toJson(api_args));

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
//      connection.setRequestProperty("Dropbox-API-Arg", new Gson().toJson(api_args));
 //     connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write((new Gson().toJson(api_args)).getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (finish batch check):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(finish batch check):");
//      LOGGER.log(Level.INFO,queryResult);
      Gson gson = new GsonBuilder().create();
      returnObject = gson.fromJson(queryResult, FinishBatchReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to finish batch."); }
    return returnObject;
}

  public static FinishBatchReturnObject deleteBatch(DeleteBatch api_args, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.deleteBatch:");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    FinishBatchReturnObject returnObject = null;
    try { 
      url = new URL("https://api.dropboxapi.com/2/files/delete_batch");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    LOGGER.log(Level.INFO,new Gson().toJson(api_args));

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
//      connection.setRequestProperty("Dropbox-API-Arg", new Gson().toJson(api_args));
 //     connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write((new Gson().toJson(api_args)).getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (finish batch check):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(finish batch check):");
//      LOGGER.log(Level.INFO,queryResult);
      Gson gson = new GsonBuilder().create();
      returnObject = gson.fromJson(queryResult, FinishBatchReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to finish batch."); }
    return returnObject;
}

  public static FinishBatchReturnObject delete_batchCheck(AsyncJob api_args, @Context ServletContext sc) throws TokenException {
    LOGGER.log(Level.INFO,"Calling DropBox.delete_batchCheck:");
    Token token = DropBox.getToken(sc);
    boolean error = false;
    URL url = null;
    FinishBatchReturnObject returnObject = null;
    try { 
      url = new URL("https://api.dropboxapi.com/2/files/delete_batch/check");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
//    LOGGER.log(Level.INFO,new Gson().toJson(api_args));

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("DP error.");
    }
    String queryResult = "";
    try {
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", "Bearer "+token.getAccess_token());
      connection.setRequestProperty("Content-Type", "application/json");
//      connection.setRequestProperty("Dropbox-API-Arg", new Gson().toJson(api_args));
 //     connection.setRequestProperty("Content-Length", "" + data.length);

      OutputStream outputStream = connection.getOutputStream();
      outputStream.write((new Gson().toJson(api_args)).getBytes(StandardCharsets.UTF_8));
      outputStream.flush();
//      LOGGER.log(Level.INFO,"RESPONSE CODE (finish batch check):");
//      LOGGER.log(Level.INFO,Integer.toString(connection.getResponseCode()));
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();
      while ((inputLine = in.readLine()) != null) { response.append(inputLine); }
      in.close();
      queryResult = response.toString();
//      LOGGER.log(Level.INFO,"QUERY RESULT(finish batch check):");
//      LOGGER.log(Level.INFO,queryResult);
      Gson gson = new GsonBuilder().create();
      returnObject = gson.fromJson(queryResult, FinishBatchReturnObject.class);
    }
    catch (Exception e) {
      error = true;
      LOGGER.log(Level.INFO,e.getMessage());
    }
    
    finally { connection.disconnect(); if (error) throw new InternalException("Failed to finish batch."); }
    return returnObject;
}
  /* Here are dropbox java api version. */
  public static FileMetadata uploadDPJavaApi(byte[] data, String fileName, @Context ServletContext sc) throws IOException, DbxException{

    LOGGER.log(Level.INFO,"uploadDPJavaApi");
  
    DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
    DbxClientV2 client = new DbxClientV2(config, DropBox.getToken(sc).getAccess_token());

    FileMetadata metadata = null;

    // Upload "test.txt" to Dropbox
    try (InputStream in = new ByteArrayInputStream(data)) {
      metadata = client.files().uploadBuilder(folder + fileName)
        .uploadAndFinish(in);
    }

    LOGGER.log(Level.INFO,metadata.toString());
  
    return metadata;
  }
}
