import React, { Component } from 'react';
import { Button } from 'react-bootstrap';
import SockJsClient from 'react-stomp';
import './WordSocket.css';

class WordSocket extends Component {

  constructor(props) {
    super(props);
    this.state = {authenticated: false, connected: false,}

    this.handleMessage = this.handleMessage.bind(this);
  }

  componentDidMount() {
    fetch("http://lvh.me:8080/checkAuth", { redirect: "error", credentials: "same-origin" })
      .then(response => { this.setState({ authenticated: response.ok })});

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
    const authenticated = this.state.authenticated;
    if (!authenticated) {
      return (
        <div className="WordSocket">
          <p>To access your Word Herd please</p>
          <Button bsStyle="primary" bsSize="large" href="/oauth2/authorization/github">Login with GitHub</Button>
        </div>
      );
    }

    const connected = this.state.connected;
    const statusMessage = connected? "This is your Word Herd." : "Connecting...";
    const websocketUrl = "http://lvh.me:8080/words";

    return (
      <div className="WordSocket">
        <p>{statusMessage}</p>
        <SockJsClient
          url={websocketUrl}
          topics={['/app/all' , '/topic/new']}
          onMessage={ this.handleMessage }
          onConnect={ () => {this.setState({connected: true})} }
          onDisconnect={ () => {this.setState({connected: false})} }
          ref={ (client) => { this.clientRef = client }} />
        <hr />
        <form action="/logout" method="post" >
          <Button type="submit">Log Out</Button>
        </form>
      </div>
    );
  }
}

export default WordSocket;
