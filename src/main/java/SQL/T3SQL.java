package SQL; 
import java.util.HashMap;
import java.lang.IllegalArgumentException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.sql.DriverManager;
import java.sql.Array;
import java.sql.Timestamp;
import java.sql.Driver;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import org.sqlite.JDBC;
import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
//import org.json.JSONObject;
import org.json.JSONArray;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.google.gson.Gson;
import T3.*;
import T3.DropBox.*;

public class T3SQL {

  private final static Logger LOGGER = Logger.getLogger(T3SQL.class.getName());

  @Context private ServletContext sc;

  public T3SQL(@Context ServletContext sc) { this.sc = sc;}

  private Connection connect() {
    String url = sc.getRealPath("/WEB-INF/classes/satama");

    Connection conn = null;

    try {
      Class.forName("org.sqlite.JDBC");
    }
    catch (Exception e)  {
      LOGGER.log(Level.INFO,e.toString());
    }

    try {
      conn = DriverManager.getConnection("jdbc:sqlite:" + url);
      conn.setAutoCommit(false);
    } catch (SQLException e) { throw new InternalException("Could'n connect to the database.");}

    return conn;
  }

  public List<Member> getMember(int[] id) {
    return getMembers(id, false); 
  }

  public List<Member> getAllMembers() {
    return getMembers(null, true); 
  }

  public List<Song> getSong(int[] id) {
    return getSongs(id, false, false, -1); 
  }

  public List<Song> getSongByConcert(int id) {
    return getSongs(null, false, true, id);
  }

  public List<Concert> getConcert(int id) {
    return getConcerts(id, false); 
  }

  public List<Song> getAllSongs() {
    return getSongs(null, true, false, -1); 
  }

  /* Call this tu start SQL command first time.
   * @param closeConnection: true closes the database connection. False leaves
   * database connection open.   
   * @param rollBack: set true to rollBack if an exception is thrown. */
//  public void PrepareSQLCommand(boolean closeConnection, boolean rollBack) {
//    
//  }
//
//  public void prepareStatement(String statement,  

  public List<Member> getMemberByName(String memberName) {
    
    String s = String.join("",
        "SELECT id, nimi, status, salasana ",
        "FROM jasenet ",
        "WHERE nimi = ?");

    List<Member> members = null;// = new ArrayList<>();
    StringBuilder error = new StringBuilder();

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 

    try {
        conn = this.connect();

        pstmt = conn.prepareStatement(s);
        pstmt.setString(1, memberName);
        rs = pstmt.executeQuery();

        members = parseMembers(rs);
    }
    catch (Exception e)
      {
      LOGGER.log(Level.INFO,e.toString());
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
    if (error.length() > 0) throw new DataNotFoundException(error.toString());  
    return members;
  }

  public List<Member> getMembers(int[] ids, boolean getAll) {

    String sAll = String.join("",
        "SELECT id, nimi, status, salasana ",
        "FROM jasenet ",
        "ORDER BY nimi");

    String sSingle = String.join("",
        "SELECT id, nimi, status, salasana ",
        "FROM jasenet ",
        "WHERE id in ("); 

    List<Member> members = null;
    StringBuilder error = new StringBuilder();

    Connection conn = null; 
    Statement stmt  = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = this.connect();

      if (getAll) {
        stmt  = conn.createStatement();
        rs = stmt.executeQuery(sAll);
      }
      else {
        StringBuilder builder = new StringBuilder();

        for (int i=0 ; i < ids.length; i++ ) {
          builder.append("?,");
        }

        String stmt2 = sSingle + builder.deleteCharAt(builder.length()-1).toString()+")";

        pstmt = conn.prepareStatement(stmt2);

        int index = 1;
        for( Object o : ids ) {
          pstmt.setObject( index++, o );
        }

        rs = pstmt.executeQuery();
      }

      members = parseMembers(rs);

    }
    catch (Exception e)
      {
        LOGGER.log(Level.INFO,e.toString());
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
        LOGGER.log(Level.INFO,ex.toString());
      }
    }
    if (error.length() > 0) throw new DataNotFoundException(error.toString());  
    return members;
  }

  public List<Song> getSongs(int[] ids, boolean getAll, boolean byConcertId, int cId){

    String sAll = String.join("",
        "SELECT id, nimi, keikka, tiedosto ",
        "FROM biisit ",
        "ORDER BY nimi");

    String sSingle = String.join("",
        "SELECT id, nimi, keikka, tiedosto ",
        "FROM biisit ",
        "WHERE id in ("); 

    String byConcert = String.join("",
        "SELECT id, nimi, keikka, tiedosto ",
        "FROM biisit ",
        "WHERE keikka = ?");

    List<Song> songs = new ArrayList<>();
    StringBuilder error = new StringBuilder();

    Connection conn = null; 
    Statement stmt  = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      conn = this.connect();

      if (byConcertId) {
        pstmt = conn.prepareStatement(byConcert);
        pstmt.setInt(1, cId);
        rs = pstmt.executeQuery();
      }

      else if (getAll) {
        stmt  = conn.createStatement();
        rs = stmt.executeQuery(sAll);
      }
      else {
        StringBuilder builder = new StringBuilder();

        for (int i=0 ; i < ids.length; i++ ) {
          builder.append("?,");
        }

        String stmt2 = sSingle + builder.deleteCharAt(builder.length()-1).toString()+")";

        pstmt = conn.prepareStatement(stmt2);

        int index = 1;
        for( Object o : ids ) {
          pstmt.setObject( index++, o );
        }

        rs = pstmt.executeQuery();
      }
      LOGGER.log(Level.INFO,"rs == null: " + Boolean.toString(rs == null));

      while (rs.next()) {

        int sid = rs.getInt("id");
        int concertId = rs.getInt("keikka");
        String songName = rs.getString("nimi");
        String fileName = rs.getString("tiedosto");
        String thisLink = String.join("","/concerts/", Integer.toString(sid));  
        String fileLink = String.join("",thisLink,"/file");  
        String concertLink = String.join("","/concerts/",Integer.toString(concertId));  

//        LOGGER.log(Level.INFO,"creating songs:");
        Song song = new Song();
//        LOGGER.log(Level.INFO,"sid: " + Integer.toString(sid));
        song.setId(sid);
//        LOGGER.log(Level.INFO,"songname: " + songName);
        song.setSongName(songName);
//        LOGGER.log(Level.INFO,"concertId: " + Integer.toString(concertId));
        song.setConcertId(concertId);
//        LOGGER.log(Level.INFO,"fileName: " + fileName);
        song.setFileName(fileName);
        songs.add(song);
      }
    }
    catch (SQLException e)
      {
      error.append(e.toString());
      }
    catch (NullPointerException e)
      {
      error.append(e.toString());
      }
    catch (Exception e)
      {
      error.append(e.toString());
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      error.append(ex.toString());
      }
    }
    if (error.length() > 0) {
      LOGGER.log(Level.INFO,error.toString());
      throw new InternalException(error.toString());
    }  
      LOGGER.log(Level.INFO,"returning from getSongs()");
    return songs;
  } 

  //public List<Concert> getConcerts(int id, boolean getAll){
  public List<Concert> getConcerts(int id, boolean getAll){

    String sAll = String.join("", "SELECT k.id AS concertId, k.nimi AS concertName, k.aika AS date, j.id AS playerId, j.nimi AS playerName, o.soitin AS instruments ",
                    "FROM keikat k LEFT OUTER JOIN osallistunut o ON o.keikka = k.id LEFT OUTER JOIN jasenet j ON j.id = o.jasen ORDER BY k.id");
    String sSingle = String.join("", "SELECT k.id AS concertId, k.nimi AS concertName, k.aika AS date, j.id AS playerId, j.nimi AS playerName, o.soitin AS instruments ",
                    "FROM keikat k LEFT OUTER JOIN osallistunut o ON o.keikka = k.id LEFT OUTER JOIN jasenet j ON j.id = o.jasen WHERE k.id = ?");

    List<Concert> concerts = new ArrayList<>();

    Connection conn = null; 
    Statement stmt  = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List<String> result = new ArrayList<>();
    HashMap<Integer,Concert> dConcerts = new HashMap<>();

    try {
      conn = this.connect();

      if (getAll) {
        stmt  = conn.createStatement();
        rs = stmt.executeQuery(sAll);
      }
      else {
        pstmt = conn.prepareStatement(sSingle);
        pstmt.setInt(1, id);
        rs = pstmt.executeQuery();
      }

      while (rs.next()) {
         
        int concertId = rs.getInt("concertId");
        String concertName = rs.getString("concertName");
        String date =  rs.getString("date");
        int playerId = rs.getInt("playerId");
        String playerName = rs.getString("playerName");
        String instruments = rs.getString("instruments");

        if (concertName == null || date == null) continue;

        Concert c = dConcerts.get(concertId);
        if (c == null) {
          c = new Concert();
          c.setId(concertId);
          c.setName(concertName);
          c.setDate(date);
          dConcerts.put(concertId,c);
        }
        if (playerName == null ||instruments == null) continue;
        PlayerInfo pi = new PlayerInfo();
        Member member = new Member();
        member.setName(playerName);
        member.setId(playerId);
        pi.setMember(member);
        JSONArray array = new JSONArray(instruments);
        for (int i=0; i<array.length(); i++) {
          pi.addInstrument(array.getString(i));
        }
        c.addPi(pi);
      }
    }

    catch (SQLException e)
      {
        LOGGER.log(Level.INFO,e.toString());
      }
    catch (NullPointerException e)
      {
        LOGGER.log(Level.INFO,e.toString());
      }
    catch (Exception e)
      {
        LOGGER.log(Level.INFO,e.toString());
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
        LOGGER.log(Level.INFO,ex.toString());
      }
    }
    return new ArrayList<Concert>(dConcerts.values());
  } 

    /* Get members from rs. */
    private List<Member> parseMembers(ResultSet rs) {

      List<Member> result = new ArrayList<>();
      try {
      while (rs.next()) {
        int id = rs.getInt("id");
        String name = rs.getString("nimi");
        String status = rs.getString("status");
        String password = rs.getString("salasana");

        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setPassword(password);

        JSONArray array = new JSONArray(status);
        List<String> roles = new ArrayList<>();
        for (int i=0; i<array.length(); i++) {
          roles.add(array.getString(i));
        }
        member.setStatus(roles);
        result.add(member);
      }
      }
      catch (Exception ex) {
        LOGGER.log(Level.INFO,ex.toString());
      }
      return result;
    }
    
    public int createNewConcert(Concert concert) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    int generatedId = -1;

    try {
        conn = this.connect();
        if (concert.getName().trim().length() == 0) throw new BadRequestException("Concert name can not be empty.");

        pstmt = conn.prepareStatement("INSERT into keikat (nimi,kuvaus,aika) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, concert.getName());
        pstmt.setString(2, "");
        //pstmt.setTimestamp(3, (new Timestamp((new SimpleDateFormat("yyyy-MM-dd").parse(date)).getTime())));
        //long time = 0;
//        if (!concert.getDate().matches("\\d{4}-\\d{2}-\\d{2}"))
//          throw new BadRequestException("Error occured when parsing JSON. " + "Date must be in format dddd-mm-dd"); 
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        df.setLenient(false);
//        Date temp = null;
//        try {
//          temp = df.parse(concert.getDate());
//        }
//        catch (ParseException e) {
//          throw new BadRequestException("Date must be in format dddd-mm-dd and the date must be valid."); 
//        }
//        catch (IllegalArgumentException e) {
//          throw new BadRequestException("Date is not an actual date."); 
//        }
        //try {
        //  time = (new SimpleDateFormat("yyyy-MM-dd").parse(date)).getTime();
        //}
        //catch (Exception e) {
        //  LOGGER.log(Level.INFO,e.toString());
        //  throw new BadRequestException("The date must be in from of yyyy-mm-dd.");
        //}
        pstmt.setString(3, concert.getDate());

        pstmt.executeUpdate();

        ResultSet tableKeys = pstmt.getGeneratedKeys();
        tableKeys.next();
        generatedId = tableKeys.getInt(1);
        conn.commit();
        LOGGER.log(Level.INFO,"Created a new concert with id: " + Integer.toString(generatedId));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
    return generatedId;
  }

    /* Returns a new concert id if succeeded, else returns -1. */
    public int addMember(int concertId, PlayerInfo pi) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    int generatedId = -1;

    try {
        conn = this.connect();

        pstmt = conn.prepareStatement("INSERT into osallistunut (soitin,keikka,jasen) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
        JSONArray jsonArray = new JSONArray(pi.getInstruments()); 
        int memberId = pi.getMember().getId(); 
        pstmt.setString(1, jsonArray.toString());
        pstmt.setInt(2, concertId);
        pstmt.setInt(3, memberId);

        pstmt.executeUpdate(); 
        conn.commit();
        LOGGER.log(Level.INFO,"Created a new playerInfo to concertId " + Integer.toString(concertId));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
    return generatedId;
  }


  public void deleteSong(int[] songIds) {
    
    String deleteSongs = String.join("",
        "DELETE ",
        "FROM biisit ",
        "WHERE id in ("); 

    Connection conn = null; 
    PreparedStatement pstmt = null;

    try {
      conn = this.connect();

        StringBuilder builder = new StringBuilder();

        for (int i=0 ; i < songIds.length; i++ ) {
          builder.append("?,");
        }

        String stmt2 = deleteSongs + builder.deleteCharAt(builder.length()-1).toString()+")";

        pstmt = conn.prepareStatement(stmt2);

        int index = 1;
        for( Object o : songIds ) {
          pstmt.setObject( index++, o );
        }
        pstmt.executeUpdate(); 
        conn.commit();
      }
    
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database error.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database error.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      throw new InternalException("Database error.");
      }
    }
      LOGGER.log(Level.INFO,"returning from deleteSongs()");
  }

  public void deleteMembersFromConcert(int concertId) {
      
    String deleteMembers = String.join("",
        "DELETE ",
        "FROM osallistunut ",
        "WHERE keikka = ?"); 

    PreparedStatement pstmt = null;
    Connection conn = null; 

    try {
        conn = this.connect();

        pstmt = conn.prepareStatement(deleteMembers);
        pstmt.setInt(1, concertId);
        pstmt.executeUpdate(); 
        conn.commit();
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
  }

  public void deleteConcert(int concertId) {
      
    String deleteConcert = String.join("",
        "DELETE ",
        "FROM keikat ",
        "WHERE id = ?"); 

    PreparedStatement pstmt = null;
    Connection conn = null; 

    try {
        conn = this.connect();

        pstmt = conn.prepareStatement(deleteConcert);
        pstmt.setInt(1, concertId);
        pstmt.executeUpdate(); 
        conn.commit();
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
  }

    public int createNewSong(Song newSong,int concertId) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    int generatedId = -1;
    try {
        if (newSong.getFileName() != null && newSong.getFile() != null && newSong.getFileName().length() > 0 && newSong.getFile().length() > 0) {
          DropBoxReturnObject dbo = Songs.addSongDP(newSong,concertId,sc);
          newSong.setFileName(dbo.getName());
          //Songs.addSong(newSong,concertId,sc);
        }
        conn = this.connect();
        if (newSong.getSongName().trim().length() == 0) throw new BadRequestException("Song name can not be empty.");

        pstmt = conn.prepareStatement("INSERT into biisit (keikka,nimi,info,tiedosto) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, concertId);
        pstmt.setString(2, newSong.getSongName().trim());
        pstmt.setString(3, "");
        pstmt.setString(4, newSong.getFileName());

        pstmt.executeUpdate();

        ResultSet tableKeys = pstmt.getGeneratedKeys();
        tableKeys.next();
        generatedId = tableKeys.getInt(1);
        conn.commit();
        LOGGER.log(Level.INFO,"Created a new song with id: " + Integer.toString(generatedId));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
    return generatedId;
  }

    public int createNewSongBatch(Song newSong,int concertId) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    int generatedId = -1;
    try {
        conn = this.connect();
        if (newSong.getSongName().trim().length() == 0) throw new BadRequestException("Song name can not be empty.");

        pstmt = conn.prepareStatement("INSERT into biisit (keikka,nimi,info,tiedosto) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, concertId);
        pstmt.setString(2, newSong.getSongName().trim());
        pstmt.setString(3, "");
        pstmt.setString(4, newSong.getFileName());

        pstmt.executeUpdate();

        ResultSet tableKeys = pstmt.getGeneratedKeys();
        tableKeys.next();
        generatedId = tableKeys.getInt(1);
        conn.commit();
        LOGGER.log(Level.INFO,"Created a new song with id: " + Integer.toString(generatedId));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
    return generatedId;
  }
    
    public void updateSong(Song song) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    try {
        conn = this.connect();
        // Do not test this here!
        if (song.getSongName().trim().length() == 0) throw new BadRequestException("Song name can not be empty.");

        pstmt = conn.prepareStatement("UPDATE biisit SET keikka = ? , " + 
                                      "nimi = ? , info = ? , tiedosto = ? WHERE id = ?");
        pstmt.setInt(1, song.getConcertId());
        pstmt.setString(2, song.getSongName());
        pstmt.setString(3, song.getInfo() == null ? "" : song.getInfo());
        pstmt.setString(4, song.getFileName());
        pstmt.setInt(5, song.getId());

        pstmt.executeUpdate();

        conn.commit();
        LOGGER.log(Level.INFO,"Updatet a song with id: " + Integer.toString(song.getId()));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      LOGGER.log(Level.INFO,"Song:");
      LOGGER.log(Level.INFO,new Gson().toJson(song));
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
  }

    public void updateConcert(Concert concert) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Connection conn = null; 
    try {
        conn = this.connect();

        pstmt = conn.prepareStatement("UPDATE keikat SET nimi = ? , " + 
                                      "kuvaus = ? , aika = ? WHERE id = ?");
        pstmt.setString(1, concert.getName());
        pstmt.setString(2, "");
        pstmt.setString(3, concert.getDate());
        pstmt.setInt(4, concert.getId());

        pstmt.executeUpdate();

        conn.commit();
        LOGGER.log(Level.INFO,"Updated a concert with id: " + Integer.toString(concert.getId()));
    }
    catch (SQLException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    catch (NullPointerException e)
      {
      LOGGER.log(Level.INFO,e.toString());
      throw new InternalException("Database problems.");
      }
    finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
      LOGGER.log(Level.INFO,ex.toString());
      }
    }
  }
}
