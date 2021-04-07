package T3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import com.google.gson.Gson;
import SQL.T3SQL;
import T3.DropBox.*;

@Path("concerts")
@PermitAll
public class ConcertsResource {

  private final static Logger LOGGER = Logger.getLogger(ConcertsResource.class.getName());
  private @Context ServletContext sc;
      //LOGGER.log(Level.INFO,e.toString());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Concert> getConcerts(@Context UriInfo uriInfo) {
    LOGGER.log(Level.INFO,"getConcerts:");
    T3SQL jee = new T3SQL(sc);
    List<Concert> allConcerts = jee.getConcerts(0,true);
    URIFactory.addLinksToConcerts(allConcerts,uriInfo);
    return allConcerts;
  }
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public List<Concert> postConcert(@Context UriInfo uriInfo, Concert concert) {

      LOGGER.log(Level.INFO,"post: concerts");

      for (Song s : concert.getSongs()) {
        if (s.getFileName() == null ||s.getFile() == null) {
          throw new BadRequestException("A song name and file must be specifed."); 
        }
      }
      List<Concert> cons = new ArrayList<>();
      T3SQL jee = new T3SQL(sc);
      int newConcertId = jee.createNewConcert(concert);
      if (newConcertId == -1) throw new InternalException("Failed to create a new Concert."); 
//      jee.removeMembersFromConcert(conc);
      LOGGER.log(Level.INFO,"Adding members now.");
      addMembers(newConcertId, concert.getPi()); 

      List<Concert> c = jee.getConcert(newConcertId);
      ConcertsResource.addSongs(concert.getSongs(), newConcertId, sc);

      URIFactory.addLinksToConcerts(c,uriInfo);
      return c;
  }

  @GET
  @Path("/{concertId}")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<Concert> getConcert(@Context UriInfo uriInfo, @PathParam("concertId") int id) {
      T3SQL jee = new T3SQL(sc);
      List<Concert> c = jee.getConcert(id);
      URIFactory.addLinksToConcerts(c,uriInfo);
      return c;
  }

  @PUT
  @Path("/{concertId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public List<Concert> putConcert(@Context UriInfo uriInfo, @PathParam("concertId") int id, Concert concert) {
     // LOGGER.log(Level.INFO,"put: concerts/" + Integer.toString(id));
      
      List<Concert> cons = new ArrayList<>();
      T3SQL jee = new T3SQL(sc);

      List<Concert> c = jee.getConcert(id);
      if (c.size() == 0) throw new DataNotFoundException("Concert not found."); 

      jee.updateConcert(concert);

      removeMembers(id);

      if (concert.getPi() != null) {
        addMembers(id, concert.getPi()); 
      }

      List<Song> songs = concert.getSongs();
      List<Song> oldSongs = jee.getSongByConcert(id); 

//      List<Song> toBeRemoved = new ArrayList<>(); 
      List<Integer> oldIds = oldSongs.stream().map(s -> s.getId()).collect(Collectors.toList());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");
//      LOGGER.log(Level.INFO,"oldIds" + oldIds.toString());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");

      List<Integer> newIds = songs.stream().map(s -> s.getId()).collect(Collectors.toList());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");
//      LOGGER.log(Level.INFO,"newIds" + newIds.toString());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");

      // Songs ids to be removed.
      List<Integer> removeIds = oldIds.stream().filter(oldId -> !newIds.contains(oldId)).collect(Collectors.toList());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");
//      LOGGER.log(Level.INFO,"removeIds" + removeIds.toString());
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");
//      for (Song s : removeSongs) {
//        LOGGER.log(Level.INFO,new Gson().toJson(s));
//      }
//      LOGGER.log(Level.INFO,"\n------------------------------------\n");

      // Songs to be modified.
 //     LOGGER.log(Level.INFO,"songsModify:");
      //List<Song> songsModify = oldSongs.stream().filter(s -> newIds.contains(s.getId()) && !removeIds.contains(s.getId()) && s.getFile() == null).collect(Collectors.toList());
      List<Song> songsModify = songs.stream().filter(s -> oldIds.contains(s.getId()) && s.getFile() == null).collect(Collectors.toList());
      for (int i=0 ;i<songsModify.size() ; i++) {
        Song mSong = songsModify.get(i);
        for (int j=0 ;j<oldSongs.size() ; j++) {
          Song oSong = oldSongs.get(j);
          if (mSong.getId() == oSong.getId()) {
            // Copy the filename from old.
            mSong.setFileName(oSong.getFileName());
            break;
          }
        }
      }


      // Brand new songs.
      List<Song> newSongs = songs.stream().filter(s -> s.getId() == 0 || s.getFile() != null).collect(Collectors.toList());

      // Delete songs. 
      int[] idsToRemove = removeIds.stream().mapToInt(i->i).toArray();
      if (idsToRemove.length > 0) {
        Songs.deleteSongFiles(idsToRemove,true,sc);
        jee.deleteSong(idsToRemove);
      }

      // Modify songs
      for (Song s : songsModify) {
        ConcertsResource.updateSong(s,sc);
      }

      // Add new songs.
      if (newSongs.size() > 0) {
        ConcertsResource.addSongs(newSongs, id, sc);
      }

      URIFactory.addLinksToConcerts(c,uriInfo);
      return c;
  }

  @DELETE
  @Path("/{concertId}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public Response deleteConcert(@Context UriInfo uriInfo, @PathParam("concertId") int id) {
      T3SQL jee = new T3SQL(sc);
      List<Concert> c = jee.getConcert(id);
      if (c.size() == 0) throw new DataNotFoundException("Resource not found.");

      /* This should be uncommitted. */
      jee.deleteMembersFromConcert(id);
      List<Song> songs = jee.getSongByConcert(id); 
      List<Integer> songIdsToRemove = new ArrayList<>(); 

      for (Song s : songs) {
        songIdsToRemove.add(s.getId());
      }
      int [] songIds_remove = songIdsToRemove.stream().mapToInt(i->i).toArray();
      try {
        Songs.deleteSongFiles(songIds_remove,true,sc);
        jee.deleteSong(songIds_remove);
        jee.deleteConcert(id);
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.toString());
        LOGGER.log(Level.INFO,e.getStackTrace().toString());
      }
      ResponseBuilder response = Response.ok("{}");
      return response.build();
  }

  @GET
  @Path("/{concertId}/members")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public List<PlayerInfo> getMembers(@Context UriInfo uriInfo, @PathParam("concertId") int id) {
      T3SQL jee = new T3SQL(sc);
      Concert c = jee.getConcert(id).get(0);
      List<Member> members = new ArrayList<>();
      List<PlayerInfo> pis = c.getPi();
      for (PlayerInfo pInfo : pis) {
        Member member = pInfo.getMember();
        members.add(member);
      }
      URIFactory.addLinksToMembers(members, uriInfo);
      return pis;
  }

  @DELETE
  @Path("/{concertId}/members")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public Response deleteMembers(@Context UriInfo uriInfo, @PathParam("concertId") int id) {
      removeMembers(id);
      ResponseBuilder response = Response.ok();
      return response.build();
  }

  @POST
  @Path("/{concertId}/members")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("admin")
  public List<PlayerInfo> postMembers(@Context UriInfo uriInfo, @PathParam("concertId") int id, List<PlayerInfo> pInfos) {
      LOGGER.log(Level.INFO,"Post: concert/" + Integer.toString(id) + "/members");
      T3SQL jee = new T3SQL(sc);
      Concert c = jee.getConcert(id).get(0);
      jee.deleteMembersFromConcert(c.getId());
      return addMembers(id,pInfos);
  }

  private void removeMembers(int concertId) {
    T3SQL jee = new T3SQL(sc);
    Concert c = jee.getConcert(concertId).get(0);
    jee.deleteMembersFromConcert(c.getId());
  }

  private List<PlayerInfo> addMembers(int concertId, List<PlayerInfo> pInfos) {
    T3SQL jee = new T3SQL(sc);
    jee.deleteMembersFromConcert(concertId);
    for (PlayerInfo pi : pInfos) {
      jee.addMember(concertId, pi);
    }
    return pInfos;
  }

  private static int addNewSong(Song s, int concertId, @Context ServletContext sc) {
    T3SQL jee = new T3SQL(sc);
    return jee.createNewSongBatch(s,concertId);
//    return -5;
  }

  public static void addSongs(List<Song> newSongs, int concertId, final @Context ServletContext sc) { 
      List<UploadSessionArgs> uploadArgs = new ArrayList<>();
      FinishBatchArgs fba = new FinishBatchArgs();
      List<byte[]> files = new ArrayList<>();
      List<AppendCallData> appendData = new ArrayList<>();

      int offset = 0;

      List<String> sessionIds = new ArrayList<>();

      ExecutorService executorService = new ThreadPoolExecutor(newSongs.size(), newSongs.size(), 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
      List<Callable<String>> callables = new ArrayList<>();
      List<Future<String>> futures = null;

      for (Song s : newSongs) {
        Callable<String> c = () -> {
        DropBoxUploadStartObject start = DropBox.uploadStart(new byte[0], "blaaaah", sc);
        LOGGER.log(Level.INFO,start.getSession_id());
        return start.getSession_id();
        };
        callables.add(c);
      }
      try {
        futures = executorService.invokeAll(callables);
        for (Future<String> f : futures) {
          String result = null;
          try {
            result = f.get();
            sessionIds.add(result);
          } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.INFO,ex.getMessage());
          }
        }
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.getMessage());
      }
      finally {
        executorService.shutdown();
      }

      for (int i=0 ; i<newSongs.size() ; i++) {

         byte[] data = Base64.decodeBase64(newSongs.get(i).getFile());
         files.add(data);

         UploadSessionArgs uArg = new UploadSessionArgs();       
         AppendEntry ae = new AppendEntry();

         Cursor cursor = new Cursor();
         cursor.setSession_id(sessionIds.get(i));
         cursor.setOffset(offset);

         Commit commit = new Commit();
         commit.setPath(DropBox.folder+newSongs.get(i).getFileName().replace(" ","_"));
         commit.setMode("add");
         commit.setAutorename(true);
         commit.setMute(false);
         commit.setStrict_conflict(false);

         uArg.setClose(true);
         uArg.setCursor(cursor);

         uploadArgs.add(uArg);

         ae.setCursor(cursor);
         ae.setCommit(commit);

         AppendCallData callData = new AppendCallData();
         callData.setData(data);
         callData.setUsa(uArg);
         appendData.add(callData);

         fba.addEntry(ae);
//         offset += data.length;
      }

      // Appending files concurrently.;
      ExecutorService executorService2 = new ThreadPoolExecutor(newSongs.size(), newSongs.size(), 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
      List<Callable<Object>> runnables2 = new ArrayList<>();
      for (AppendCallData aData : appendData) {
        Runnable r = () -> {DropBox.uploadAppend(aData.getData(),aData.getUsa(),sc); };
        Callable<Object> c = Executors.callable(r);
        runnables2.add(c);
      }
      try {
        executorService2.invokeAll(runnables2);
      }
      catch (Exception e) {
        LOGGER.log(Level.INFO,e.getMessage());
      }
      finally {
        executorService2.shutdown();
      }

      for (int i=0 ; i<appendData.size() ; i++) {
        fba.getEntries().get(i).getCursor().setOffset(appendData.get(i).getData().length);
      }

      FinishBatchReturnObject uploadResult = DropBox.finishBatch(fba,sc);
      AsyncJob aj = null;
      if (uploadResult.getTag().equals("async_job_id")) {
        aj = new AsyncJob();
        aj.setAsync_job_id(uploadResult.getAsync_job_id());
        uploadResult = DropBox.finishBatchCheck(aj,sc);
      }
      while (uploadResult.getTag().equals("in_progress")) {
        uploadResult = DropBox.finishBatchCheck(aj,sc);
      }

      List<FinishBatchReturnEntry> entries = uploadResult.getEntries();

      for (int i=0; i<newSongs.size() ; i++) {
        FinishBatchReturnEntry e = entries.get(i);
        Song s = newSongs.get(i);
//        List<Integer> oldRemovableFiles = new ArrayList<>();
        if (e.getTag().equals("success")) {
          Song old = null;
          if (s.getId() != 0) {
            // DropBox. 
            
            T3SQL jee = new T3SQL(sc);
            try {
              old = jee.getSong(new int[]{s.getId()}).get(0);
            }
            catch (Exception ex) {
              // Someone have propably deleted the song.
              LOGGER.log(Level.INFO,ex.getMessage());
              continue;
            }
            if (!old.getFileName().equals(e.getName())) {
              LOGGER.log(Level.INFO,"removing files id = " + Integer.toString(old.getId()));
              try {
                Songs.deleteSongFiles(new int[]{old.getId()}, true,sc);
              }
              catch (Exception ex2) {
                LOGGER.log(Level.INFO,ex2.getMessage());
              }
            }
            String newName = e.getName().replace("'","");
            s.setFileName(newName); 
            ConcertsResource.updateSong(s,sc);
          }
          else {
            s.setFileName(e.getName()); 
            ConcertsResource.addNewSong(s,concertId,sc);
          }
        }
        else {
          LOGGER.log(Level.INFO,"Failed to add file to DropBox");
          LOGGER.log(Level.INFO,new Gson().toJson(e));
        }
      }
  }

  public static void updateSong(Song s, @Context ServletContext sc) {
    T3SQL jee = new T3SQL(sc);
    jee.updateSong(s);
  }

  private FinishBatchReturnObject checkBatch(String id) {
    LOGGER.log(Level.INFO,"checkBatch.");
    AsyncJob aj = new AsyncJob();
    aj.setAsync_job_id(id);
    return DropBox.finishBatchCheck(aj,sc);
  }
}
