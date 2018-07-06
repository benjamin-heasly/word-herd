import React, { Component } from 'react';
import { Button } from 'react-bootstrap';
import SockJsClient from 'react-stomp';
import './Words.css';
import WordList from './WordList';
import WordInput from './WordInput';

class Words extends Component {

  constructor(props) {
    super(props);
    this.state = {
      authenticated: false,
      connected: false,
      words: []}

    this.handleMessage = this.handleMessage.bind(this);
    this.handleWordSubmitted = this.handleWordSubmitted.bind(this);
  }

  handleMessage(msg, topic) {
    console.log('handle topic: ' + topic + " message: " + msg);
    switch (topic) {
      case '/app/all':
        this.setState({ words: msg });
        break;
      case '/topic/new':
        this.setState({ words: this.state.words.concat(msg) });
        break;
      default:
        console.log('unexpected topic');
    }
  }

  handleWordSubmitted(word) {
    this.sockJsClient.sendMessage('/app/add', word)
  }

  componentDidMount() {
    fetch("http://lvh.me:8080/checkAuth", { redirect: "error", credentials: "same-origin" })
      .then(response => { this.setState({ authenticated: response.ok }) });
  }

  render() {
      const authenticated = this.state.authenticated;
      if (!authenticated) {
        return (
          <div className="Words">
            <Button bsStyle="primary" bsSize="large" href="/oauth2/authorization/github">Login with GitHub</Button>
          </div>
        );
      }

    const connected = this.state.connected;
    let controls;
    if (connected) {
      controls =
        <div>
          <p>This is your Word Herd.</p>
          <hr />
          <WordList
            words={this.state.words} />
          <WordInput
            submitWord={(word) => this.handleWordSubmitted(word)} />
          <hr />
        </div>;
    } else {
      controls =
        <div>
          <p>Connecting...</p>
          <hr />
        </div>;
    }

    const websocketUrl = "http://lvh.me:8080/words";

    return (
      <div className="Words">
        <SockJsClient
          url={websocketUrl}
          topics={['/app/all' , '/topic/new']}
          onMessage={ this.handleMessage }
          onConnect={ () => { this.setState({ connected: true }) } }
          onDisconnect={ () => { this.setState({ connected: false }) } }
          ref={ (client) => { this.sockJsClient = client } } />

        {controls}

        <form action="/logout" method="post" >
          <Button type="submit">Log Out</Button>
        </form>
      </div>
    );

  }
}

export default Words;
