//import React from 'react';
//import {ReactAudioPlayer} from 'react_audio_player.jsx';

var rDebug = true;

Date.prototype.yyyymmdd = function() {
  var mm = this.getMonth() + 1; // getMonth() is zero-based
  var dd = this.getDate();

  return [this.getFullYear(),
          (mm>9 ? '' : '0') + mm,
          (dd>9 ? '' : '0') + dd
         ].join('-');
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
    
function getCode() {
  if (window.location.href !== undefined) {
    if (window.location === undefined) return;
    var codeDP = window.location.href.split("?code=");
    if (codeDP.length === 2) {
      return codeDP[1];
    }
  }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////
  
// After compression: [{generalName: song1, songs: [...]}]
function compressSongs(songs) {
  if (!Array.isArray(songs)) {
    console.error(`compressSongs(${typeof songs}).`);
    return [];
  }
  var uniqueSongNames = Array.from(new Set(songs.map(x => x.songName)));
  uniqueSongNames = uniqueSongNames.map(x => Object.assign({},{"generalName": x, "songs": []})); 
  var result = [];
  for (let x of songs) {
    uniqueSongNames.find(z => z.generalName === x.songName).songs.push(x); 
  }
  return uniqueSongNames;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////
  
/* param @songs must be in form on the result of compressSongs-function. No
 * error checking! */
function deCompressSongs(songs) {
  var result = [];
  if (!Array.isArray(songs)) {
    console.error(`deCompressSongs(${typeof songs}).`);
    return [];
  }
  for (let x of songs) {
    for (let y of x.songs) {
      result.push(JSON.parse(JSON.stringify(y))); 
    }
  }
  return result;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

function createDefaultBand() {

  // Concert.
  var date = new Date(Date.now()).yyyymmdd();
  var id = null; 
  var links = [];
  var name = "";

  return {concert: [{date: date, id: id, links: links, name: name}], members: [], songs: []};
}

/////////////////////////////////////////////////////////////////////////////////////////////////////

/*   
 *
 * The main component of the react application.
 *
 * */

class App extends React.Component {
    constructor(props) {

      super(props);
//        this.actionType = Actions.RefreshSongs; 
        this.state = { 
	        "footer" : this.createFooter(), /* [
                       {tag:"Concerts",click:(e) => ajaxGetAllConcerts(this.updateState.bind(this))},
                       {tag:"Songs",click:(e) => ajaxGetAllSongs(this.updateState.bind(this))},
                       {tag:"Login",click:(e) => this.updateState({login: true})}*/
          "concerts" : [],
          "songs" : [],
          "band" : undefined,
          "playerURI" : "webapi/songs/1/file",
          "playerSongName" : "Song name",
          "login": false,
          "username": '',
          "password": '',
          "modifyBand": false, 
          "users": [], 
          "error": "",  
          "success": ""  
        };
        ajaxGetAllMembers(this.updateState.bind(this));
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    createFooter() {
      return [
        {tag:"Concerts",click:(e) => {e.preventDefault(); ajaxGetAllConcerts(this.updateState.bind(this));}},
        {tag:"Songs",click:(e) => {e.preventDefault(); ajaxGetAllSongs(this.updateState.bind(this));}},
        {tag:"Login",click:(e) => {e.preventDefault(); this.updateState({login: true});}}
      ];
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    updateState(data) {
      //this.getCode();
      this.setState((prevState) => { 
        var newState = {};
        // Lets copy the old data from the prevState.
        for (let x in prevState) {
          // kopsataan vanhasta statesta uuteen.
          if (!(x in data)) {
            switch (typeof(prevState[x])) {
              case 'undefined':
                newState[x] = undefined;
                break;
              case 'object':
                if (x === "footer") newState[x] = this.createFooter(); 
                else newState[x] = JSON.parse(JSON.stringify(prevState[x]));
                break;
              default:
                newState[x] = prevState[x];
            }
          }
        }
          // Kopsataan datasta uuteen stateen.
            for (let x in data) {
              if (x === "songs") {
                newState[x] = compressSongs(data[x].slice());
              }
              else if (x === "footer") newState[x] = this.createFooter(); 
              else if (typeof(data[x]) === "undefined") newState[x] = undefined; 
              else if (x === "band") {
                newState[x] = JSON.parse(JSON.stringify(data[x])); 
                newState[x].songs = compressSongs(newState[x].songs); 
              }
              else if (typeof(x) === "object") newState[x] = data[x]; // Object.assign({},data[x]);//JSON.parse(JSON.stringify(data[x]));
              else newState[x] = data[x]; 
            }

        if (newState.band === undefined && newState.modifyBand === true) {
          console.error("newState.band === undefined && modifyBand === true");
          //newState.modifyBand = false;
        }
        $.ajaxSetup({
          headers: {"Authorization": "Basic " + btoa(newState.username + ":" + newState.password)}
        });
        return newState;
      });
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    playSong(song) {
      for (let i of song.links) {
        if (i.rel === 'file') {
          this.updateState({playerURI: removePrefix(i.link), playerSongName: song.songName});
          return;
        }
      }
      console.error('playSong(): ongelmia.');
      console.error(song);
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    renderBand() {
      console.log("renderBand");
      if (this.state.band === undefined) return;
      return (<div><Band band={this.state.band}/><Songs songs={this.state.band.songs} playSong={this.playSong.bind(this)} /></div>);
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    authenticate(e) {
      var username = e.target[0].value;
      var password = e.target[1].value;
      this.updateState({username: username, password: password, login: false});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    closeLogin(e) {
      e.preventDefault();
      this.updateState({login: false});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    createNewConcert(e) {
      var that = this;
      this.updateState({modifyBand: true, songs: [], concerts: [], band: createDefaultBand()});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    onCloseBandForm(e) {
      this.updateState({band: undefined});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    onCloseError(e) {
      this.updateState({error : ""});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    onCloseSuccess(e) {
      this.updateState({success : ""});
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////
  
    sendConcert(e,concert) {
      console.log("sendConcert");
      var files = [];
      var functions = [];

      for (let x of concert.songs) {
        if (x.file !== undefined && x.file !== null) {
          files.push(new Promise((resolve, reject) => {
                             console.debug("HYYI  HAI");
                             console.debug(x.file);
                             const reader = new FileReader();
                             reader.readAsDataURL(x.file);
                             reader.onload = () => {
                                                    console.debug(x.file);
                                                    x.fileName = x.file.name;
                                                    x.file = reader.result.split("base64,")[1];
                                                    resolve(reader.result);};
                             reader.onerror = error => reject(error);
                           }));
        }
        else x.fileName = null;
      }
      if (files.length > 0) {
        console.debug(files);
        $.when.apply(null,files).then(data => { ajaxCreateConcert(concert, this.updateState.bind(this));}, (error) => console.log(error));
        return;
      }
      ajaxCreateConcert(concert, this.updateState.bind(this));
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    startDP(e) {
      console.log("startDP");
      ajaxDP(this.updateState.bind(this));
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    finishDP(code) {
      console.log("finishDP");
      // TODO updatefunction
      if (code === undefined) {
        this.updateState({error: "No code available."});
        return;
      }
      ajaxPostDP(code,this.updateState.bind(this));      
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////

    render () {
      return (
        <div id="page">
	      <Footer songsPressed={(e)=>this.songsPressed(e)}
                footer={this.state.footer}
                modifyChecked={this.state.modifyBand}
                modifyBandCheckboxClicked={(e) => this.updateState({modifyBand: !this.state.modifyBand})}
                createNewConcert={this.createNewConcert.bind(this)}
                startDP={this.startDP.bind(this)}
                finishDP={this.finishDP.bind(this)}
                />
        <div id="middle">
        {this.state.error.length > 0 ? <Error errorMsg={this.state.error} onCloseError={this.onCloseError.bind(this)}/> : null}
        {this.state.success.length > 0 ? <Success successMsg={this.state.success} onCloseSuccess={this.onCloseSuccess.bind(this)}/> : null}
        {this.state.login ? <LoginForm onSubmit={this.authenticate.bind(this)} onCloseLogin={this.closeLogin.bind(this)}/> : null}
        {this.state.modifyBand ? null : this.renderBand()}
	      <Concerts concerts={this.state.concerts}
                  modify={this.state.modifyBand}
                  clickConcert={(e) => {e.preventDefault(); ajaxGetConcert(removePrefix($(e.target).attr("href")),this.updateState.bind(this));}}
                  deleteConcert={(i) => ajaxDeleteConcert(i,this.updateState.bind(this))}
        /> 
        {this.state.modifyBand ? (this.state.band !== undefined ? <BandForm band={this.state.band} users={this.state.users}
                                                                  onCloseBandForm={this.onCloseBandForm.bind(this)} onSubmit={this.sendConcert.bind(this)} /> : null)  : null }
	      <Songs songs={this.state.songs} playSong={this.playSong.bind(this)} /> 
        </div>
        <AudioPlayer playerURI={this.state.playerURI} playerSongName={this.state.playerSongName} />
        </div>
      )
    }
}

/*****************************************************************************************************************************************/

class Error extends React.Component {
  constructor(props) {
    super(props);
  }
    render() {

      return (
        <div id="errorPanel">
          <textarea defaultValue={this.props.errorMsg}/>
          <button onClick={(e) => { e.preventDefault(); this.props.onCloseError(e)}}>
            <img alt="close" src="Button-Close-icon.png"/>
          </button>
        </div>
      )
    }
}


/*****************************************************************************************************************************************/

class Success extends React.Component {
  constructor(props) {
    super(props);
  }
    render() {

      return (
        <div id="successPanel">
          <textarea defaultValue={this.props.successMsg}/>
          <button onClick={(e) => { e.preventDefault(); this.props.onCloseSuccess(e)}}>
            <img alt="close" src="Button-Close-icon.png"/>
          </button>
        </div>
      )
    }
}

/*****************************************************************************************************************************************/

class Band extends React.Component {
  constructor(props) {
    super(props);
  }

  createMembers() {
    console.log("createMembers");
    console.log(this.props.band.members);
    var members = this.props.band.members.map((x,i) => <li key={i}><label>{x.member.name}</label><label>({x.instruments.join(", ")})</label></li>);
    return members;
  }

  render () {
    return (
    <div id="listaus">
    <h1>Band</h1>
      <ul>
      {this.createMembers()}
      </ul>
    </div>
    )
  }
}

/*****************************************************************************************************************************************/

class Footer extends React.Component {
    constructor(props) {
      super(props);
    }

    addLinks() {
      console.log("Footer: this.props.footer:");
      console.log(this.props.footer);
      var links = this.props.footer.map((x,i) =>
        <p key={x.tag.replace(/\s/g,'')+i.toString()}> <a href={""} onClick={x.click}>{x.tag}</a></p>);  
      return links;
    }

    render () {
      return (
    <footer>
        {this.addLinks()}
        <p className="modifyCheckBox">
          <label>Modify-mode:
            <input type="checkbox" checked={this.props.modifyChecked} onChange={this.props.modifyBandCheckboxClicked}/>
          </label>
        </p>
        {!this.props.modifyChecked ? null :
        <p className="newConcert">
          <button onClick={(e) => {e.preventDefault(); this.props.createNewConcert(e);}} >Create a new Concert</button>
        </p>}
        {!this.props.modifyChecked ? null :
        <p className="DP">
          <button onClick={(e) => {e.preventDefault(); this.props.startDP(e);}} >Start DP</button>
        </p>}
        {!this.props.modifyChecked ? null :
        <p className="DP">
          <button onClick={(e) => {e.preventDefault(); var code = getCode(); this.props.finishDP(code);}} >Finish DP</button>
        </p>}
    </footer>
      )
    }
}

/*****************************************************************************************************************************************/

class Songs extends React.Component {
    constructor(props) {
      super(props);
    }

    createMultipleSongs(songs) {
      var s = songs.map((song,ind) => <li key={ind} onClick={(e) => {e.stopPropagation();this.props.playSong(song)}}><label>{song.songName}</label></li>);
      return s;
    }

    // Closes the previous opened details-element if necessary.
    detailsClick(e) {
      //e.stopPropagation();
      console.log(e.target);
      var target = $(e.target).closest('details').get(0);
      if (e.target.tagName.toLowerCase() === 'details'){
        var detailit = $('summary').parent('details');
        // Jos ollaan sulkemassa detailssia, niin poistutaan.
        if (!target.open) return;
        // Jos ollaan aukaisemassa detailssia, niin suljetaan edellnine detailsi.
        detailit.each((ind, elem) => {
          if (elem.open === true && target !== elem) elem.open = false; 
        });
        return;
      }
      target.open = !target.open;
    }

    // Creates ul-elment for multiple songs if necessary.
    createUl(songs) {
      if (songs.length == 1) {
        return;
      }
      return (
        <ol className="olSong">
          {this.createMultipleSongs(songs)}
        </ol>
      );
    }

    createSongs() {
      var s = this.props.songs.map((x,i) =>
        <tr key={x.generalName.replace(/\s/g,'') + x.songs.map(a => a.id.toString()).join()} onClick={(e) => e.preventDefault()}>
          <td onClick={(e) => e.preventDefault()}>
            <details onToggle={this.detailsClick} onClick={x.songs.length == 1 ? (e) => this.props.playSong(x.songs[0]) : this.detailsClick} onSelect={console.log("selectedDetails")}>
              <summary>
                <a href={x.songs.length}>{x.generalName}  {x.songs.length > 1 ? x.songs.length.toString() : ""}</a>
              </summary>
              {this.createUl(x.songs)}
           </details>
          </td>
        </tr>);
      return s;
    }

    render () {
      if (this.props.songs.length === 0) 
        return <div id="songs" className="taulukko"></div>;
      return (
    <div id="songs" className="taulukko">
        <table>
        <tbody>
        <tr><th>Songs</th></tr>
        {this.createSongs()}
        </tbody>
        </table>
    </div>
      )
    }
}

/*****************************************************************************************************************************************/

class AudioPlayer extends React.Component {
    constructor(props) {
      super(props);
    }

    render () {
      return (
    <div id="player">
      <p id="playerSongName">{this.props.playerSongName}</p>
      <p><ReactAudioPlayer src={this.props.playerURI}/></p>
    </div>
      )
    }
}
/*****************************************************************************************************************************************/

class Concerts extends React.Component {
    constructor(props) {
      super(props);
    }

    createConcerts() {
      var concerts = [];
      for (let i=0; i<this.props.concerts.length; i++) {
	      var nimi = this.props.concerts[i]['name'];
	      var aika = this.props.concerts[i]['date'];
        var selfLink = "";
        for (let x of this.props.concerts[i].links) {
           if (x['rel'] === 'self') { selfLink = x['link']; break; }
        }
        concerts.push(
          <tr key={i}>
            <td>
              <p className="concertName">
                <a onClick={this.props.clickConcert} href={selfLink}>{nimi} {aika}</a>
              </p>
              {this.props.modify ? 
              <p>
                <button className="deleteMemberButton" onClick={(e) => { e.preventDefault(); this.props.deleteConcert(this.props.concerts[i].id)}} title="Remove this concert.">
                  <img alt="Remove" src="delete-button-png-delete-button-png-image-689.png"/>
                </button>
              </p>
                : <p></p>}
            </td>
          </tr>);
      }
      return concerts;
    }

    render () {
      if (this.props.concerts.length === 0) 
        return <div></div>;

      return (
        <div>
    <table>
      <tbody>
      <tr><th>Concerts</th></tr>
      {this.createConcerts()}
      </tbody>
    </table>
        </div>
      )
    }
}

/*****************************************************************************************************************************************/

class ReactAudioPlayer extends React.Component {
  componentDidMount() {
    const audio = this.audioEl;

    this.updateVolume(this.props.volume);

    audio.addEventListener('error', (e) => {
      this.props.onError(e);
    });

    // When enough of the file has downloaded to start playing
    audio.addEventListener('canplay', (e) => {
      this.props.onCanPlay(e);
    });

    // When enough of the file has downloaded to play the entire file
    audio.addEventListener('canplaythrough', (e) => {
      this.props.onCanPlayThrough(e);
    });

    // When audio play starts
    audio.addEventListener('play', (e) => {
      this.setListenTrack();
      this.props.onPlay(e);
    });

    // When unloading the audio player (switching to another src)
    audio.addEventListener('abort', (e) => {
      this.clearListenTrack();
      this.props.onAbort(e);
    });

    // When the file has finished playing to the end
    audio.addEventListener('ended', (e) => {
      this.clearListenTrack();
      this.props.onEnded(e);
    });

    // When the user pauses playback
    audio.addEventListener('pause', (e) => {
      this.clearListenTrack();
      this.props.onPause(e);
    });

    // When the user drags the time indicator to a new time
    audio.addEventListener('seeked', (e) => {
      this.props.onSeeked(e);
    });

    audio.addEventListener('loadedmetadata', (e) => {
      this.props.onLoadedMetadata(e);
    });

    audio.addEventListener('volumechange', (e) => {
      this.props.onVolumeChanged(e);
    });
  }

  componentWillReceiveProps(nextProps) {
    this.updateVolume(nextProps.volume);
  }

  /**
   * Set an interval to call props.onListen every props.listenInterval time period
   */
  setListenTrack() {
    if (!this.listenTracker) {
      const listenInterval = this.props.listenInterval;
      this.listenTracker = setInterval(() => {
        this.props.onListen(this.audioEl.currentTime);
      }, listenInterval);
    }
  }

  /**
   * Set the volume on the audio element from props
   * @param {Number} volume
   */
  updateVolume(volume) {
    if (typeof volume === 'number' && volume !== this.audioEl.volume) {
      this.audioEl.volume = volume;
    }
  }

  /**
   * Clear the onListen interval
   */
  clearListenTrack() {
    if (this.listenTracker) {
      clearInterval(this.listenTracker);
      this.listenTracker = null;
    }
  }

  render() {
//    const incompatibilityMessage = this.props.children || (
//      <p>Your browser does not support the <code>audio</code> element.</p>
//    );

    // Set controls to be true by default unless explicity stated otherwise
    const controls = !(this.props.controls === false);

    // Set lockscreen / process audio title on devices
    const title = this.props.title ? this.props.title : this.props.src;

    // Some props should only be added if specified
    const conditionalProps = {};
    if (this.props.controlsList) {
      conditionalProps.controlsList = this.props.controlsList;
    }

    return (
      <audio
        autoPlay={this.props.autoPlay}
        className={`react-audio-player ${this.props.className}`}
        controls={controls}
        crossOrigin={this.props.crossOrigin}
        id={this.props.id}
        loop={this.props.loop}
        muted={this.props.muted}
        onPlay={this.onPlay}
        preload={this.props.preload}
        ref={(ref) => { this.audioEl = ref; }}
        src={this.props.src}
        style={this.props.style}
        title={title}
        {...conditionalProps}
      >
      </audio>
    );
  }
}

       // {incompatibilityMessage}
ReactAudioPlayer.defaultProps = {
  autoPlay: false,
  children: null,
  className: '',
  controls: true,
  controlsList: '',
  crossOrigin: null,
  id: '',
  listenInterval: 10000,
  loop: false,
  muted: false,
  onAbort: () => {},
  onCanPlay: () => {},
  onCanPlayThrough: () => {},
  onEnded: () => {},
  onError: () => {},
  onListen: () => {},
  onPause: () => {},
  onPlay: () => {},
  onSeeked: (e) => {},
  onVolumeChanged: () => {},
  onLoadedMetadata: () => {},
  preload: 'auto',
  src: '',
  style: {},
  title: '',
  volume: 1.0,
};

/*****************************************************************************************************************************************/

class LoginForm extends React.Component {

    constructor(props) {
    super(props);
    //this.formEl = React.createRef();
  }

  componentDidMount() {
    const thisForm = this.formEl;

    thisForm.addEventListener('onSubmit', (e) => {
      alert("apua");
      e.preventDefault();
      this.props.onSubmit(e);
    });
  }

  render() {

    return (
      <div id="loginBorder">
      <div id="loginBackground">
      <form acceptCharset="UTF-8" method={this.props.method}
        className={this.props.className}
        id={this.props.id}
        ref={(ref) => { this.formEl = ref; }}
        onSubmit={(e) => {e.preventDefault(); this.props.onSubmit(e);}}
      >
        <p>
        <label>
          Username:
          <input name="username" type="text" ref={i => this.input = i} />
        </label>
        </p>
        <p>
        <label>
          Password:
          <input name="password" type="password" ref={i => this.input = i} />
        </label>
        </p>
        <p>
          <input name="submit" type="submit" value="Login" id="loginButton"/>
        </p>
        <p>
          <input name="closeLogin" type="image" alt="close" src="Button-Close-icon.png" value="Login" id="closeButton" onClick={this.props.onCloseLogin}/>
        </p>
      </form>
      </div>
      </div>
    );
  }
}

LoginForm.defaultProps = {
  className: '',
  action: '',
  id: 'loginForm',
  method: 'GET',
  onSubmit: (e) => {alert("kake!")},
};

/*****************************************************************************************************************************************/

class BandForm extends React.Component {

    constructor(props) {
    super(props);
      var initialState = JSON.parse(JSON.stringify(this.props));
      initialState.band.songs = deCompressSongs(initialState.band.songs);

      for (let x of initialState.band.songs) {
        x.file = null;
      }
      this.state = initialState;
  }

   componentWillReceiveProps(props) {
     this.props = props;
     this.setState((prevState) => { 
       var newBand = JSON.parse(JSON.stringify(this.props.band))
       newBand.songs = deCompressSongs(newBand.songs);
       for (let x of newBand.songs) {
         if (x.file === undefined) x.file = null;
       }
       var newUsers = JSON.parse(JSON.stringify(this.props.users))
       return {band: newBand, users: newUsers};
     });
   }

  cloneState(prevState) {
    console.log("cloneState");
    var files = prevState.band.songs.map(x=> x.file);
    for (let x of prevState.band.songs) {
      x.file = "";
    }
    var newState = JSON.parse(JSON.stringify(prevState));
    for (let x=0; x<files.length ; x++) {
      newState.band.songs[x].file = files[x]; 
    }
    console.log("newState");
    console.log(newState);
    return newState;
  }

  createOptions(memberId,index) {
    var options = [];
    for (let j=0 ; j<this.state.users.length ; j++) {
      var value = this.state.users[j].name;
      options.push(<option key={`${j}`} value={value}>{value}</option>);
    }
    return options;
  }

  deleteMember(index) {
    console.log(`Deleting member from index ${index}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState); // JSON.parse(JSON.stringify(prevState));
      newState.band.members.splice(index,1);
      return newState;
    });
  }

  addNewMember() {
    console.log(`Adding member.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      //var newState = JSON.parse(JSON.stringify(prevState));
      newState.band.members.unshift({instruments:[],member:JSON.parse(JSON.stringify(newState.users[0]))});
      return newState;
    });
  }

  addNewSong() {
    console.log(`Adding a new song.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      //var newState = JSON.parse(JSON.stringify(prevState));
      var concertId = newState.band.concert.id;
      var songId = null;
      var links = [];
      var name = "";
      newState.band.songs.unshift({concertId: concertId, id: songId, links: links, songName: name, file:null,fileElement:null/*,newSong: true*/});
      return newState;
    });
  }

  changeInstrumentText(e, memberIndex, instrumentIndex) {
    console.log(`changeText ${memberIndex} ${instrumentIndex}`);
    e.persist();

    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.members[memberIndex].instruments[instrumentIndex] = e.target.value;
      return newState;
    });
  }

  addInstrument(index) {
    console.log(`Adding instrument to index ${index}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.members[index].instruments.unshift("");
      return newState;
    });
  }

  deleteInstrument(memberIndex,instrumentIndex) {
    console.log(`Deleting instrument ${memberIndex} ${instrumentIndex}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.members[memberIndex].instruments.splice(instrumentIndex,1);
      return newState;
    });
  }

  deleteSong(songIndex) {
    console.log(`Deleting song ${songIndex}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      var deletedSongArray = newState.band.songs.splice(songIndex,1);
      return newState;
    });
  }

  changeMember(e,memberIndex,newMember) {
    console.log(`changeMember ${memberIndex}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.members[memberIndex].member = JSON.parse(JSON.stringify(newMember));
      return newState;
    });
  }

  changeSongName(e,songIndex) {
    console.log(`changeSongName ${songIndex}.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.songs[songIndex].songName = e.target.value;
      return newState;
    });
  }

  changeDate(e) {
    console.log(`changeDate.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.concert[0].date = e.target.value;
      return newState;
    });
  }

  changeName(e) {
    console.log(`changeName.`);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      newState.band.concert[0].name = e.target.value;
      return newState;
    });
  }

  addFile(e,i) {
    console.log(`addFile.`);
    console.log(e.target);
    this.setState((prevState) => { 
      var newState = this.cloneState(prevState);
      if (e.target.files[0] !== undefined) newState.band.songs[i].file = e.target.files[0];
      return newState;
    });
  }

  createMembers() {
    var members = this.state.band.members.map((x,i) =>
      <div className="memberBlock" key={`${i}`}>
            <label className="memberLabel">Member:
              <select value={x.member.name} onChange={(e) => {e.persist(); this.changeMember(e,i,this.state.users[e.target.selectedIndex]);}}>
                {this.createOptions(x.member.id,i)}
              </select>
              <button className="deleteMemberButton" title="Remove this member."
                      onClick={(e) => {e.preventDefault(); this.deleteMember(i);}}>
                <img alt="close" src="Button-Close-icon.png"/>
              </button>
            </label>
            <label className="instumentsLabel">Instruments:
              <button className="addInstrumentButton" title="Add a new instrument."
                      onClick={(e) => {e.preventDefault(); this.addInstrument(i);}}>
                <img alt="add" src="Fairytale_button_add.svg"/>
              </button>
              {x.instruments.map((inst,ind) =>
                <p key={`${ind}`} className="intrumentLabel">
                  <label>
                    <input onChange={(e) => this.changeInstrumentText(e,i,ind)} type="text" value={`${inst}`}/>
                  </label>
                    <button className="deleteMemberButton" onClick={(e) => {e.preventDefault(); this.deleteInstrument(i,ind);}} title="Remove this instrument." >
                       <img alt="close" src="delete-button-png-delete-button-png-image-689.png"/>
                  </button>
                </p>)} 
            </label>
      </div>
    );
    return members;
  }

  onInvalid_fileinput(e) {
    var target = e.target;
    //e.checkValidity();
  }

  createSongs() {
    console.log("createSongs");
    console.log(this.state);
    
    var songs = this.state.band.songs.map((x,i) => 
      <p key={i} className="modifySongBlock">
      <label>
        <input placeholder="Add song name here." type="text" value={x.songName}
               required={true} pattern=".*\S+.*" 
               onChange={(e) => { e.persist(); this.changeSongName(e,i);}}/>
        <label>{x.file === null ? "Add file." : x.file.name}
          {this.state.band.concert[0].id === null ?
            <input id={`file${i}`} required={true}
                   className="hidden"
                   type="file"
                   onChange={e => {e.persist(); this.addFile(e,i); }}
                   onInvalid={e => console.log("INVALIIDI!")}
                   /> :
            <input id={`file${i}`}
                   className="hidden"
                   type="file"
                   onChange={e => {e.persist(); this.addFile(e,i); }}
                   onInvalid={e => console.log("INVALIIDI!")}
                   /> 
          }
        </label>
        <button className="deleteMemberButton" onClick={(e) => { e.preventDefault(); this.deleteSong(i)}} title="Remove this song.">
          <img alt="close" src="delete-button-png-delete-button-png-image-689.png"/>
        </button>
      </label>
      </p>
    );
    return songs;
  }

  onNameChange(e) {
    console.log("onNameChange");
    var value = e.target;
    console.log(value);

    if (value.validity.valueMissing === true) {
      value.setCustomValidity("The value is missing.");
      return;
    }

    if (value.validity.patternMismatch === true) {
      value.setCustomValidity("The value is missing.");
      return;
    }
    value.setCustomValidity("");
  }

  checkSongs(e) {
    var value = e.target;
    for (let s of this.state.band.songs) {
      
    }
  }

  onTimeChange(e) {
    console.log("onTimeChange");
    var value = e.target;
    console.log(value);

    if (value.validity.valueMissing === true) {
      value.setCustomValidity("The value is missing.");
      return;
    }

    if (value.validity.patternMismatch === true) {
      value.setCustomValidity("yyyy-mm-dd");
      return;
    }
    value.setCustomValidity("");
  }

  getBand() {
    var result = JSON.parse(JSON.stringify(this.state.band));
    return result;
  }

  buildConcert() {
    var concert = this.state.band.concert[0];
    var members = this.state.band.members;
    var songs = this.state.band.songs;

    concert.pi = members;
    concert.songs = songs;
    return concert;
  }

  render() {

    return (
      <div id="memberBorder">
      <div id="memberBackground">
      <h1>Band</h1>
      <form method={this.props.method}
            className={this.props.className}
            id={this.props.id}
            ref={(ref) => { this.formEl = ref; }}
            onSubmit={(e) => {console.log("SUUUUUBMITTT");e.preventDefault(); this.buildConcert(); this.props.onSubmit(e,this.getBand());}}
            noValidate
      >
        <p> 
        <label>
          Name:
          <input name="name" type="text" value={this.state.band.concert[0].name}
                 required={true} pattern=".*\S+.*" /* onInput={(e) => {e.persist();e.preventDefault(); this.changeName(e);}}*/ 
                 onChange={(e) => { e.persist(); this.onNameChange(e); this.changeName(e); }}/>
        </label>
        </p>
        <p>
        <label>
          Date:
          <input placeholder="yyyy-mm-dd" name="date" type="text"
                 required={true} pattern="[0-9]{4}-[0-9]{2}-[0-9]{2}"
                 value={this.state.band.concert[0].date} onChange={(e) => { e.persist(); this.onTimeChange(e);this.changeDate(e); }} />
        </label>
        </p>
        <h2>Members:
          <button onClick={(e) => {e.preventDefault(); this.addNewMember();}} className="addInstrumentButton" title="Add a new member.">
            <img alt="add" src="Fairytale_button_add.svg"/>
          </button>
        </h2>
        {this.createMembers()}
        <h2>Songs:
          <button onClick={(e) => {e.preventDefault(); this.addNewSong();}} className="addInstrumentButton" title="Add a new song.">
            <img alt="add" src="Fairytale_button_add.svg" />
          </button>
        </h2>
        {this.createSongs()}
        <p>
          <input name="submit" type="submit" value="Save" id="bandFormSubmit"
                 onClick={(e) => {
                   e.preventDefault();
                   var form = $("#bandForm").get(0);
                   console.log("form:");
                   console.log(form);
                   console.log(form.checkValidity());
                   if (!form.reportValidity()) { return; }
                   this.props.onSubmit(e,this.buildConcert());}}/>
        </p>
        <p>
          <input name="submit" type="image" alt="close" src="Button-Close-icon.png" value="Login" id="closeButton" onClick={(e) => {e.preventDefault(); this.props.onCloseBandForm(e);}}/>
        </p>
      </form>
      </div>
      </div>
    );
  }
}

/*****************************************************************************************************************************************/

BandForm.defaultProps = {
  className: '',
  action: '',
  id: 'bandForm',
  method: 'POST',
  onSubmit: (e) => {alert("blah!")},
};

/*****************************************************************************************************************************************/

ReactDOM.render(
    <App />,
  document.getElementById('root')
);
