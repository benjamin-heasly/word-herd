import React, { Component } from 'react';
import SockJsClient from 'react-stomp';
import './WordSocket.css';

class WordSocket extends Component {

  constructor(props) {
    super(props);
    this.state = {connected: false,}

    this.handleMessage = this.handleMessage.bind(this);
  }

  componentDidMount() {
    this.props.setSubmit((word) => { this.clientRef.sendMessage('/app/add', word) });
  }

  componentWillUnmount() {
    this.props.setSubmit(undefined);
  }

  handleMessage(msg, topic) {
    console.log('handle message: topic: ' + topic + " message: " + msg);
    switch (topic) {
      case '/app/all':
        this.props.setWords(msg);
        break;
      case '/topic/new':
        this.props.addWord(msg);
        break;
      default:
        console.log('unexpected topic');
    }
  }

  render() {
    const connected = this.state.connected;
    const statusLabel = connected ? "This is your Word Herd." : "Connecting..."

    return (
      <div className="WordSocket">
        <p>{statusLabel}</p>

        <SockJsClient
          url='http://localhost:8080/words'
          topics={['/app/all' , '/topic/new']}
          onMessage={ this.handleMessage }
          onConnect={ () => {this.setState({connected: true})} }
          onDisconnect={ () => {this.setState({connected: false})} }
          ref={ (client) => { this.clientRef = client }} />
      </div>
    );
  }
}

export default WordSocket;
