package T3;

import org.apache.commons.codec.binary.Base64;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.ServletContext;
//import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.commons.io.FileUtils;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.HttpHeaders;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.io.FilenameUtils;
import com.google.gson.Gson;

import SQL.T3SQL;
import T3.DropBox.*;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("songs")
public class Songs {
  
  private final static Logger LOGGER = Logger.getLogger(Songs.class.getName());
  private static final String fileLocation = "/home/jaankaup/satamamusicTemp/";
//  private static final String fileLocation = "";
  private static final String removedLocation = "/home/jaankaup/satamamusicremoved/";
  private static final String documentation = "https://jaankaup1.ties478.website/Task3/api.html";
  @Context private ServletContext sc;
  @Context private HttpHeaders httpheaders;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Song> getAllSongs(@Context UriInfo uriInfo, @BeanParam SongFilterBean qParams) {

      T3SQL jee = new T3SQL(sc);
      List<Song> allSongs = jee.getAllSongs();
      List<Song> filteredSongs = new ArrayList<>(); 
      List<Concert> cs = jee.getConcerts(-1, true);  
      String qName = qParams.getName();
      if (qName != null) qName = qName.toLowerCase();
      int userId = qParams.getUserId();
      int concertId = qParams.getConcertId();
      HashSet<Integer> csTemp = new HashSet<>();  

      /* Find all concters by userId */
      if (userId > 0) {
        for (Concert c : cs) {
          List<PlayerInfo> pi = c.getPi();
          boolean playerFound = false;
          for (PlayerInfo pInfo : pi) {
            Member m = pInfo.getMember();
            if (m.getId() == userId) { playerFound = true; break; }
          }
          if (playerFound) csTemp.add(new Integer(c.getId()));  
        }
      }
      for (Song s : allSongs) {
        boolean matches = true;

        if (qName != null && !s.getSongName().toLowerCase().startsWith(qName)) {matches = false; continue;}
        if (userId > 0 && !csTemp.contains(s.getConcertId())) { matches = false; continue;}
        if (concertId > 0 && s.getConcertId() != concertId) {matches = false; continue;}
        filteredSongs.add(s);
      }
      //return csTemp;
      //return qParams;
      URIFactory.addLinksToSongs(filteredSongs,uriInfo);
      return filteredSongs;
  }
  
  @GET
  @Path("/{songId}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Song> getSong(@Context UriInfo uriInfo, @PathParam("songId") int id) {
      T3SQL jee = new T3SQL(sc);
      List<Song> song = jee.getSong(new int[]{id});
      if (song.size() == 0) throw new DataNotFoundException("Song not found.");
      /* TODO: check if concert exists. check if corcert has the song. */
      URIFactory.addLinksToSongs(song,uriInfo);
      return song;
  }

  @DELETE
  @Path("/{songId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  //@RolesAllowed("admin")
  public Response deleteSong(@Context UriInfo uriInfo, @PathParam("songId") int id) {
      T3SQL jee = new T3SQL(sc);
//      List<Song> song = jee.getSong(new int[]{id});
//      if (song.size() == 0) throw new DataNotFoundException("Song not found.");
      Songs.deleteSongFiles(new int[]{id}, false,sc);
      jee.deleteSong(new int[]{id});
      /* TODO: check if concert exists. check if corcert has the song. */
      ResponseBuilder response = Response.ok();
      return response.build();
  }

  @GET
  @Path("/{songId}/file")
//  @Produces({"audio/mpeg","application/json"})
  @Produces({"audio/mpeg",MediaType.APPLICATION_JSON})
  @PermitAll
  public Response getSongFile(@PathParam("songId") int id) {
      T3SQL jee = new T3SQL(sc);
      List<Song> song = jee.getSong(new int[]{id});
      if (song.size() == 0) {
        return Response.status(Response.Status.NOT_FOUND)
                                       .entity(new ErrorMessage("File not found.", 404, documentation))
                                       .build();
      }
      String fileName = song.get(0).getFileName();
      //String filename = String.join(fileLocation,file);
      File f = null;

//      BufferedWriter w = null;
//      File f = null;
//      byte[] buffer = null;
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        f = new File(fileLocation,fileName);
//        InputStream is = new FileInputStream(f);
//        buffer = new byte[(int) f.length()];
//        is.read(buffer);
//        is.close();
//
//        baos.write(buffer, 0, (int)f.length());
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                                       .entity(new ErrorMessage("File not found.", 404, documentation))
                                       .build();
      }
      String rangeParam = httpheaders.getHeaderString("Range");
      LOGGER.log(Level.INFO,"rangeParam");
      LOGGER.log(Level.INFO,rangeParam == null ? "null" : rangeParam);
      Response res = null;
      try {
        // Try to use a file from local file system.
        if (f.exists()) res = buildStream(f, rangeParam);
        // Else get it from dropbox.
        else {
          byte[] b = DropBox.getFile(fileName,sc); 
          File hardFile = addFileToHardDrive(b, fileName); 
          res = buildStream(hardFile, rangeParam);
        }
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.toString());
        throw new InternalException("An error occurred while creating file stream.");
      }
      return res;
  }

//  @PUT
//  @Path("/{songId}/file")
//  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
//  @Produces(MediaType.APPLICATION_JSON)
//  @PermitAll
//  public Response putSongFile(@PathParam("songId") byte[] song) {
//      LOGGER.log(Level.INFO,"Adding a new file." + Integer.toString(id));
//      T3SQL jee = new T3SQL(sc);
//      List<Song> song = null;
//      song = jee.getSong(new int[]{id});
//      if (song.size() == 0) 
//        throw new DataNotFoundException("Resource not found.");   
//      String file = song.get(0).getFileName();
//
//      try {
//        File src = new File(fileLocation,file);
//        File dest = new File(removedLocation,file);
//        FileUtils.moveFile(src,dest);
//      }
//      catch (Exception e) {
//        LOGGER.log(Level.INFO,e.toString());
//        throw new InternalException("Failed to remove file.");
//      }
//      ResponseBuilder response = Response.ok();
//      return response.build();
//  }

  @DELETE
  @Path("/{songId}/file")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public Response deleteSongFile(@PathParam("songId") int id) {
      LOGGER.log(Level.INFO,"Removing file from song id " + Integer.toString(id));
      Songs.deleteSongFiles(new int[]{id}, false, sc);
      ResponseBuilder response = Response.ok();
      return response.build();
  }

//  @PUT
//  @Path("/{songId}/file")
//  @Produces(MediaType.APPLICATION_JSON)
//  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
//  @PermitAll
//  public Response putSongFile(@PathParam("songId") int id) {
//      LOGGER.log(Level.INFO,"Putting file to song id " + Integer.toString(id));
//       
//      ResponseBuilder response = Response.ok();
//      return response.build();
//  }
//
  public static void deleteSongFiles(int[] songIds, boolean proceedWithErrors, @Context ServletContext sc) {
    
    List<Integer> notRemoved = new ArrayList<>();
    List<Integer> removed = new ArrayList<>();
    List<String> filesToBeRemoved = new ArrayList<>();

    for (int id : songIds) {
      T3SQL jee = new T3SQL(sc);
      List<Song> song = null;
      try {
        song = jee.getSong(new int[]{id});
      }
      catch (Exception ex) {
        LOGGER.log(Level.INFO,ex.toString());
        LOGGER.log(Level.INFO,"Song id == " + Integer.toString(id));
      }
      if (song.size() == 0) { 
        notRemoved.add(id);
        continue;
      }
      removed.add(id);
      filesToBeRemoved.add(song.get(0).getFileName());
    }
    
    if (!proceedWithErrors && notRemoved.size() > 0) {
      String s = notRemoved.toString();
      throw new DataNotFoundException("Unable to find songs: " + s.substring(1, s.length()-1)); 
    }

    List<String> filesThatFailed = new ArrayList<>();
    DeleteBatch db = new DeleteBatch();
    for (String file : filesToBeRemoved) {
      Entry e = new Entry();
      e.setPath(DropBox.folder + file);
      db.addEntry(e);
    }

    FinishBatchReturnObject deleteBatchResult = DropBox.deleteBatch(db,sc);

    AsyncJob aj = null;
    if (deleteBatchResult.getTag().equals("async_job_id")) {
      aj = new AsyncJob();
      aj.setAsync_job_id(deleteBatchResult.getAsync_job_id());
      deleteBatchResult = DropBox.delete_batchCheck(aj,sc);
    }
    while (deleteBatchResult.getTag().equals("in_progress")) {
      deleteBatchResult = DropBox.delete_batchCheck(aj,sc);
    }

    List<FinishBatchReturnEntry> entries = deleteBatchResult.getEntries();

    for (int i=0 ; i<filesToBeRemoved.size() ; i++) {
//    for (FinishBatchReturnEntry entry : deleteBatchResult.getEntries()) {

      String file = null;
      if (entries.get(i).getTag().equals("success")) {
        try {
          file = filesToBeRemoved.get(i);
          LOGGER.log(Level.INFO,"file:");
          LOGGER.log(Level.INFO,file);
          File src = new File(fileLocation,file);
          File dest = new File(removedLocation,file);
          FileUtils.moveFile(src,dest);
        }
        catch (IOException e) {
          LOGGER.log(Level.INFO,e.toString());
          filesThatFailed.add(file);
        }
        catch (NullPointerException e) {
          LOGGER.log(Level.INFO,e.toString());
          filesThatFailed.add(file);
        }
      }
      else {
        LOGGER.log(Level.INFO,"failed to delete a file.");
      }
    }
    if (!proceedWithErrors && filesThatFailed.size() > 0) {
        String s = filesThatFailed.toString();
        LOGGER.log(Level.INFO,s.substring(1, s.length()-1));
        throw new DataNotFoundException("Unable to find delete files: " + s.substring(1, s.length()-1)); 
    }
  }

  // TODO: remove concertId.
  public static DropBoxReturnObject addSongDP(Song song, int concertId, @Context ServletContext sc) {
    byte[] data = Base64.decodeBase64(song.getFile());
    try {
      DropBoxUploadStartObject dbObj = DropBox.uploadStart(data, song.getFileName(), sc);
      DropBoxReturnObject dbo = DropBox.uploadFinish(data, song.getFileName(), dbObj, sc);
      //
      
      /* Thiw works but its slow. */
      //DropBoxReturnObject dbo = DropBox.uploadFile(data, song.getFileName(), sc);
      //addFileToHardDrive(data,dbo.getName());
      //return DropBox.uploadFile(data, song.getFileName(), sc);
      return dbo;
    }
    catch (TokenException e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("Failed to access the token.");
    }
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("Failed to create file '" + song.getFileName() + "'.");
    }
    // TODO: return the new name of the file.
  }

  public static File addFileToHardDrive(byte[] data, String fileName) {
    File newFile = new File(fileLocation, fileName.replace("'","")); 
    //if (newFile.exists()) return newFile;
    try (OutputStream stream = new FileOutputStream(newFile)) {
      stream.write(data);
    }   
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("Failed to create file '" + fileName + "' to the local system.");
    }
    return newFile;
  }

  // TODO: remove concertId. This method is only for local storage
  // implementation. Don't use it on dropbox iplementation.
  public static void addSong(Song song, int concertId, @Context ServletContext sc) {
     
    final String fileName = song.getFileName();
    final String thisFName = FilenameUtils.getBaseName(fileName);
    final String thisExtension = FilenameUtils.getExtension(fileName);

    int counter = 0;
    String newName = fileName;
    LOGGER.log(Level.INFO,"newName:");
    LOGGER.log(Level.INFO,newName);
    File newFile = new File(fileLocation, fileName); 
    while (newFile.exists()) {
      LOGGER.log(Level.INFO,newFile + " exists!");
      newName = String.join("",thisFName,"_",Integer.toString(counter),".",thisExtension);
      newFile = new File(fileLocation, newName); 
      counter++;
    }

    byte[] data = Base64.decodeBase64(song.getFile());
    try (OutputStream stream = new FileOutputStream(fileLocation+newName)) {
      stream.write(data);
      song.setFileName(newName);
    }   
    catch (Exception e) {
      LOGGER.log(Level.INFO,e.getMessage());
      throw new InternalException("Failed to create a file.");
    }
    LOGGER.log(Level.INFO,"Created a new file " + newFile.getAbsolutePath());
  }

      //private Response buildStream(final File asset, final String range, final byte[] assetBuf, String assetBufFileName) {
      private Response buildStream(final File asset, final String range) {

          if (range == null) {
              StreamingOutput streamer = new StreamingOutput() {
                  @Override
                  public void write(final OutputStream output) throws IOException, WebApplicationException {

                      final FileChannel inputChannel = new FileInputStream(asset).getChannel();
                      final WritableByteChannel outputChannel = Channels.newChannel(output);
                      try {
                          inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                      } finally {
                          // closing the channels
                          inputChannel.close();
                          outputChannel.close();
             //             if (tempFile != null) tempFile.delete();
                      }
                  }
              };
              return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
          }

          // For real partial concent.
          String[] ranges = range.split("=")[1].split("-");
          final int from = Integer.parseInt(ranges[0]);
          //final int chunk_size = 1024 * 1024; // 1MB chunks
          //final int chunk_size = tempFile != null ? (int)tempFile.length() : 1024 * 1024; 
          final int chunk_size = (int)asset.length(); 
          /**
           * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
           */
          int to = chunk_size + from;
          if (to >= asset.length()) {
              to = (int) (asset.length() - 1);
          }
          if (ranges.length == 2) {
              to = Integer.parseInt(ranges[1]);
          }

          try {
            LOGGER.log(Level.INFO,"chunkkailee");
            //final String responseRange = String.format("bytes %d-%d/%d", from, to, tempFile == null ? asset.length() : tempFile.length());
            //final RandomAccessFile raf = tempFile == null ? new RandomAccessFile(asset, "r") : new RandomAccessFile(tempFile, "r");
            final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
            final RandomAccessFile raf = new RandomAccessFile(asset, "r");
            raf.seek(from);

            final int len = to - from + 1;
            final MediaStreamer streamer = new MediaStreamer(len, raf);
            ResponseBuilder res = Response.status(Response.Status.PARTIAL_CONTENT).entity(streamer)
                                          .header("Accept-Ranges", "bytes")
                                          .header("Content-Range", responseRange)
                                          .header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
                                          .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
            //if (tempFile != null) tempFile.delete();
            return res.build();
          }
          catch (Exception e) {
            LOGGER.log(Level.INFO,e.getMessage());
            throw new InternalException("DP exception.");
          }
      }
}
