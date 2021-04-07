"use strict"

const url = "webapi/";
const srv = `https://jaankaup1.ties478.website/Task3/`;
const regexSrv = /https:\/\/jaankaup1.ties478.website\/Task3\//gi;

/////////////////////////////////////////////////////////////////////////////////////////////////////
  //
function progressIndicatorStart() {
  $("*").css("cursor","progress");
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function progressIndicatorStop() {
  $("*").css("cursor", "");
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function removePrefix(str) {
  if (str.startsWith(srv)) {
    console.log("yeah");
    console.log(str.replace(regexSrv,''));
    return str.replace(regexSrv,'');
  }
  return str;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

/* Function to get all songs from Task3. */
function ajaxGetAllSongs(updateFunction) {
  var data = {concerts: [], songs: [], band:undefined, error: ""};

  $.when(
    // Get the HTML
    $.get(url + "songs", (songs) => data.songs = songs)
  ).then(function() {updateFunction(data);},
         function(x,t,e) {console.error("ajaxGetAllSongs:");printAjaxError(x,t,e);});
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxGetAllConcerts(updateFunction) {

  var data = {concerts: [], songs: [], band:undefined, error: ""};

  $.when(
    // Get the HTML
    $.get(url + "concerts", (concerts) => data.concerts = concerts)
  ).then(function() {updateFunction(data);},
         function(x,t,e) {console.error("ajaxGetAllConcerts:");printAjaxError(x,t,e);});
}


/////////////////////////////////////////////////////////////////////////////////////////////////////

/* Get all concerts. */
function ajaxGetConcert(concertURI,updateFunction) {
  var data = {concerts: [], songs: [], error: ""/*, band:undefined*/};
//  var concertURI = removePrefix($(e.target).attr("href"));
  var songsURI = undefined;
  var membersURI = undefined;
  var concert = undefined;
  progressIndicatorStart(); 
  console.log(concertURI);
  
  $.when(
  $.get(concertURI, (c) => concert = c)
  ).then(() => {
    if (concert === undefined || concert === []) { data.error = "Problems with gettin the concert."; progressIndicatorStop();return; } 

    data.band = {concert:concert};
    for (let x of concert[0].links) {
      if (x.rel === "songs") songsURI = removePrefix(x.link);  
      if (x.rel === "members") membersURI = removePrefix(x.link);  
    }

    console.debug(songsURI);
    console.debug(membersURI);
    
    if (songsURI === undefined) { data.error = "Problems with getting songs."; progressIndicatorStop();return; } 
    if (membersURI === undefined) { data.error = "Problems with getting members."; progressIndicatorStop();return; } 

    $.when(
      // Get the HTML
      $.get(songsURI, (songs) => {data.band.songs = songs;}),
      $.get(membersURI, (members) => {data.band.members = members;}),
    ).then(function() {/*data.modifyBand=true*/;updateFunction(data);progressIndicatorStop();},
           function(x,t,e) {console.error("ajaxGetConcert:");printAjaxError(x,t,e);progressIndicatorStop();});
  });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxCreateSong(concertId, song) {

}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxPostMembers(concertId, members) {
    var error = "";
  
    $.when($.ajax({ async: true,
                    url: "webapi/concerts/"+concertId+"/members",
                    type: "post",
                    dataType:"json",
                    data: JSON.stringify(members),
                    contentType: 'application/json'}))
      .then(() => {}, (x,t,e) => error = e.responseText);
    return error;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////


function ajaxGetAllMembers(updateFunction) {

  var data = {};

  $.when(
    $.get("webapi/members", (m) => data.users = m)
  ).then(() => {() => console.log(data);updateFunction(data)}, (x,t,e) => { console.error("ajaxGetAllMembers");printAjaxError(x,t,e);});
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxCreateConcert(concert,updateFunction) {
  console.log("ajaxCreateConcert");
  console.log("concert.id");
  console.log(concert.id);
  var d = {error: ""};
  var method = concert.id == null ? "post" : "put";
  var resourceUrl = concert.id == null ? "webapi/concerts" : `webapi/concerts/${concert.id}`;
  var sData = JSON.stringify(concert);

  var error = "";

  progressIndicatorStart();

  $.when($.ajax({ async: true,
                  url: resourceUrl,
                  type: method,
                  dataType:false,//"json",
                  data: sData,
                  headers: {"Content-Type":"application/json"}
                })).then((data) => {var concertURI = undefined;
                                    var c = data[0];
                                    console.log("SAATIIN VASTAUKSEKSI");
                                    console.log(c);
                                    for (let x of c.links) {
                                      if (x.rel === "self") {
                                        concertURI = x.link;
                                        break;
                                      }
                                    }
                                    if (concertURI !== undefined) { 
                                      console.log("method == " + method);
                                      var successText = method === "put" ? "Concert modified succesfully." : "Concert created succesfully.";
                                      updateFunction({error:"",modifyBand:false,band:undefined, success: successText});
                                      ajaxGetConcert(concertURI,updateFunction);
                                    }
                                    else progressIndicatorStop();
                                    console.log(concertURI === undefined);},
                         (x,t,e) => {progressIndicatorStop();
                                     handleError(x,t,e,updateFunction);});
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxDP(updateFunction) {
  console.log("ajaxDP");
  var data = {error: ""};
  $.when($.ajax({ async: true,
                  url: url+"dropBox",
                  type: "get",
                  dataType:"json",
                  data: {},
                })).then((data)  => { window.location = data;},
                         (x,t,e) => {handleError(x,t,e,updateFunction);} );
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxPostDP(code,updateFunction) {
  console.log("ajaxDP");
  $.when($.ajax({ async: true,
                  url: url+"dropBox",
                  type: "post",
                  contentType: "application/json",
                  dataType:"json",
                  data: JSON.stringify({code:code}),
                })).then((data)  => { console.log(data) ;},
                         (x,t,e) => { handleError(x,t,e,updateFunction);/* data.error = e; updateFunction(data);*/} );
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function handleError(xhr,statusStr,errorStr,updateFunction) {
  console.debug("handleError");
  console.debug("xhr");
  if (xhr !== undefined) console.debug(xhr);
  console.debug("strStatus");
  if (statusStr !== undefined) console.debug(statusStr);
  console.debug("errorStr");
  if (errorStr !== undefined) console.debug(errorStr);
  switch (xhr.responseJSON.errorCode) {
    case 401:
      updateFunction({error: "Authorization required.", login: true});
      break;
    case 403:
      updateFunction({error: "Forbidden."});
      break;
    case 404:
      updateFunction({error: "Resource not found."});
      break;
    case 500:
      updateFunction({error: "Server error."});
      break;
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function ajaxDeleteConcert(concertId,updateFunction) {
  var data = {error: ""};
  $.when($.ajax({ async: true,
	         url: "webapi/concerts/" + concertId,
	         type: "delete",
           dataType:"json",
           contentType: 'application/json'
	     })).then((data,status,xhr) => {ajaxGetAllConcerts(updateFunction);},
              (xhr,statur,error) => {handleError(x,t,e,updateFunction);});
}

function printAjaxError(xhr,textStatus,error) {

}
