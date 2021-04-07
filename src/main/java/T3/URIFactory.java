package T3;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.UriInfo;

/* A class for generating URI:s. */
public class URIFactory {

  public enum SongLinkType { SELF, MEMBER, FILE };

  public static void addSelfConcertLink(Concert concert, UriInfo uriInfo) {
    String uri = createConcertUri(concert.getId(),uriInfo);
    concert.addLink(uri,"self");
  }

  public static void addSongsLink(Concert concert, UriInfo uriInfo) {
    String uri = createSongsUri(concert.getId(), uriInfo);
    concert.addLink(uri,"songs");
  }

  public static void addMembersLink(Concert concert, UriInfo uriInfo) {
    String uri = createMemberUriForConcert(concert.getId(), uriInfo);
    concert.addLink(uri,"members");
  }
  
  public static void addLinksToConcerts(List<Concert> concerts, UriInfo uriInfo) {
    for (Concert c : concerts) {
      addSelfConcertLink(c,uriInfo);
      addSongsLink(c,uriInfo);
      addMembersLink(c,uriInfo);
    }
  }

  public static void addLinksToMembers(List<Member> members, UriInfo uriInfo) {
    for (Member m : members) {
      m.addLink("self",createMemberUri(m.getId(), uriInfo));
    }
  }

  // TODO: change ConcersResource method to return uri.
  public static void addConcertLink(Song song, UriInfo uriInfo) {
    String uri = createConcertUri(song.getConcertId(), uriInfo);
    song.addLink(uri,"concert");
  }

  public static void addLinksToSongs(List<Song> songs, UriInfo uriInfo) {
      for (Song s : songs) {
        String songUri = createSongUri(s.getId(), uriInfo);
        s.addLink(songUri,"self");
        String concertUri = createConcertUri(s.getConcertId(), uriInfo);
        s.addLink(concertUri,"concert");
        //String membersUri = createMembersUri(s.getConcertId(), uriInfo);
        //s.addLink(membersUri,"members");
        String fileUri = createFileUri(s.getId(), uriInfo);
        s.addLink(fileUri,"file");
      }
  }

  public static String createMemberUri(int memberId, UriInfo uriInfo) {
    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(MembersResource.class)
                        .path(MembersResource.class,"getMember")
                        .resolveTemplate("memberId",memberId)
                        .build()
                        .toString();
    return uri;
  }

  public static String createMemberUriForConcert(int concertId, UriInfo uriInfo) {
    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(ConcertsResource.class)
                        .path(ConcertsResource.class,"getMembers")
                        .resolveTemplate("concertId",concertId)
                        .build()
                        .toString();
    return uri;
  }
  /* Creates an uri URI for concert. */
  public static String createConcertUri(int concertId, UriInfo uriInfo) {

    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(ConcertsResource.class)
                        .path(ConcertsResource.class,"getConcert")
                        .resolveTemplate("concertId",concertId)
                        .build()
                        .toString();
    return uri;
  }
  
  public static String createSongsUri(int concertId, UriInfo uriInfo) {
    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(Songs.class)
                        .queryParam("concertId", new Integer(concertId))
                        .build()
                        .toString();
    return uri;
  }

  public static String createSongUri(int songId, UriInfo uriInfo) {
//    HashMap<String,Object> templateValues = new HashMap<>();
//    templateValues.put("concertId",concertId);
//    templateValues.put("songId",songId);
    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(Songs.class)
                        .path(Songs.class,"getSong")
                        .resolveTemplate("songId",songId)
                        .build()
                        .toString();
    return uri;
  }

  public static String createFileUri(int songId, UriInfo uriInfo) {
//    HashMap<String,Object> templateValues = new HashMap<>();
//    templateValues.put("concertId",concertId);
//    templateValues.put("songId",songId);

    String uri = uriInfo.getBaseUriBuilder()
                        .scheme("https")
                        .path(Songs.class)
                        .path(Songs.class,"getSongFile")
                        .resolveTemplate("songId",songId)
                        .build()
                        .toString();
    return uri;
  }

  /* Creates an URI for songs. */
  public static String createSongUri(int concertId, int songId, UriInfo uriInfo, SongLinkType type) {

    HashMap<String,Object> templateValues = new HashMap<>();
    templateValues.put("concertId",concertId);
    templateValues.put("songId",songId);

    String sType = "";

    switch (type) {
      case SELF:
        sType = "getSong";
        break;
      case MEMBER:
        sType = "getMembers";
        break;
      case FILE:
        sType = "getSong";
        break;
      default:
        //throw new Exception("createSongUri: 500. No such type.");
    }

    String uri = uriInfo.getBaseUriBuilder()
      .scheme("https")
      .path(ConcertsResource.class)
      .path(ConcertsResource.class,sType)
      .resolveTemplates(templateValues)
      .build()
      .toString();
    return uri;
  }

  /* Creates an URI for song file. */
//  public static String createSongFileUri(int songId, UriInfo uriInfo) {
//
//    String uri = uriInfo.getBaseUriBuilder()
//      .scheme("https")
//      .path(Songs.class)
//      .path(Songs.class,"getSongFile")
//      .resolveTemplate("songId",songId)
//      .build()
//      .toString();
//    return uri;
//  }
}
