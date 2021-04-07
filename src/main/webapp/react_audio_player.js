var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

import React, { Component } from 'react';
import PropTypes from 'prop-types';

var ReactAudioPlayer = function (_React$Component) {
  _inherits(ReactAudioPlayer, _React$Component);

  function ReactAudioPlayer() {
    _classCallCheck(this, ReactAudioPlayer);

    return _possibleConstructorReturn(this, (ReactAudioPlayer.__proto__ || Object.getPrototypeOf(ReactAudioPlayer)).apply(this, arguments));
  }

  _createClass(ReactAudioPlayer, [{
    key: 'componentDidMount',
    value: function componentDidMount() {
      var _this2 = this;

      var audio = this.audioEl;

      this.updateVolume(this.props.volume);

      audio.addEventListener('error', function (e) {
        _this2.props.onError(e);
      });

      // When enough of the file has downloaded to start playing
      audio.addEventListener('canplay', function (e) {
        _this2.props.onCanPlay(e);
      });

      // When enough of the file has downloaded to play the entire file
      audio.addEventListener('canplaythrough', function (e) {
        _this2.props.onCanPlayThrough(e);
      });

      // When audio play starts
      audio.addEventListener('play', function (e) {
        _this2.setListenTrack();
        _this2.props.onPlay(e);
      });

      // When unloading the audio player (switching to another src)
      audio.addEventListener('abort', function (e) {
        _this2.clearListenTrack();
        _this2.props.onAbort(e);
      });

      // When the file has finished playing to the end
      audio.addEventListener('ended', function (e) {
        _this2.clearListenTrack();
        _this2.props.onEnded(e);
      });

      // When the user pauses playback
      audio.addEventListener('pause', function (e) {
        _this2.clearListenTrack();
        _this2.props.onPause(e);
      });

      // When the user drags the time indicator to a new time
      audio.addEventListener('seeked', function (e) {
        _this2.props.onSeeked(e);
      });

      audio.addEventListener('loadedmetadata', function (e) {
        _this2.props.onLoadedMetadata(e);
      });

      audio.addEventListener('volumechange', function (e) {
        _this2.props.onVolumeChanged(e);
      });
    }
  }, {
    key: 'componentWillReceiveProps',
    value: function componentWillReceiveProps(nextProps) {
      this.updateVolume(nextProps.volume);
    }

    /**
     * Set an interval to call props.onListen every props.listenInterval time period
     */

  }, {
    key: 'setListenTrack',
    value: function setListenTrack() {
      var _this3 = this;

      if (!this.listenTracker) {
        var listenInterval = this.props.listenInterval;
        this.listenTracker = setInterval(function () {
          _this3.props.onListen(_this3.audioEl.currentTime);
        }, listenInterval);
      }
    }

    /**
     * Set the volume on the audio element from props
     * @param {Number} volume
     */

  }, {
    key: 'updateVolume',
    value: function updateVolume(volume) {
      if (typeof volume === 'number' && volume !== this.audioEl.volume) {
        this.audioEl.volume = volume;
      }
    }

    /**
     * Clear the onListen interval
     */

  }, {
    key: 'clearListenTrack',
    value: function clearListenTrack() {
      if (this.listenTracker) {
        clearInterval(this.listenTracker);
        this.listenTracker = null;
      }
    }
  }, {
    key: 'render',
    value: function render() {
      var _this4 = this;

      var incompatibilityMessage = this.props.children || React.createElement(
        'p',
        null,
        'Your browser does not support the ',
        React.createElement(
          'code',
          null,
          'audio'
        ),
        ' element.'
      );

      // Set controls to be true by default unless explicity stated otherwise
      var controls = !(this.props.controls === false);

      // Set lockscreen / process audio title on devices
      var title = this.props.title ? this.props.title : this.props.src;

      // Some props should only be added if specified
      var conditionalProps = {};
      if (this.props.controlsList) {
        conditionalProps.controlsList = this.props.controlsList;
      }

      return React.createElement(
        'audio',
        Object.assign({
          autoPlay: this.props.autoPlay,
          className: 'react-audio-player ' + this.props.className,
          controls: controls,
          crossOrigin: this.props.crossOrigin,
          id: this.props.id,
          loop: this.props.loop,
          muted: this.props.muted,
          onPlay: this.onPlay,
          preload: this.props.preload,
          ref: function ref(_ref) {
            _this4.audioEl = _ref;
          },
          src: this.props.src,
          style: this.props.style,
          title: title
        }, conditionalProps),
        incompatibilityMessage
      );
    }
  }]);

  return ReactAudioPlayer;
}(React.Component);

ReactAudioPlayer.defaultProps = {
  autoPlay: false,
  children: null,
  className: '',
  controls: false,
  controlsList: '',
  crossOrigin: null,
  id: '',
  listenInterval: 10000,
  loop: false,
  muted: false,
  onAbort: function onAbort() {},
  onCanPlay: function onCanPlay() {},
  onCanPlayThrough: function onCanPlayThrough() {},
  onEnded: function onEnded() {},
  onError: function onError() {},
  onListen: function onListen() {},
  onPause: function onPause() {},
  onPlay: function onPlay() {},
  onSeeked: function onSeeked() {},
  onVolumeChanged: function onVolumeChanged() {},
  onLoadedMetadata: function onLoadedMetadata() {},
  preload: 'metadata',
  src: null,
  style: {},
  title: '',
  volume: 1.0
};

ReactAudioPlayer.propTypes = {
  autoPlay: PropTypes.bool,
  children: PropTypes.element,
  className: PropTypes.string,
  controls: PropTypes.bool,
  controlsList: PropTypes.string,
  crossOrigin: PropTypes.string,
  id: PropTypes.string,
  listenInterval: PropTypes.number,
  loop: PropTypes.bool,
  muted: PropTypes.bool,
  onAbort: PropTypes.func,
  onCanPlay: PropTypes.func,
  onCanPlayThrough: PropTypes.func,
  onEnded: PropTypes.func,
  onError: PropTypes.func,
  onListen: PropTypes.func,
  onLoadedMetadata: PropTypes.func,
  onPause: PropTypes.func,
  onPlay: PropTypes.func,
  onSeeked: PropTypes.func,
  onVolumeChanged: PropTypes.func,
  preload: PropTypes.oneOf(['', 'none', 'metadata', 'auto']),
  src: PropTypes.string, // Not required b/c can use <source>
  style: PropTypes.objectOf(PropTypes.string),
  title: PropTypes.string,
  volume: PropTypes.number
};

export default ReactAudioPlayer;