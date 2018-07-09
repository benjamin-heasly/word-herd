import React, {Component} from 'react';
import {Button} from 'react-bootstrap';
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
      words: []
    }

    this.handleMessage = this.handleMessage.bind(this);
    this.handleWordSubmitted = this.handleWordSubmitted.bind(this);
  }

  handleMessage(msg, topic) {
    console.log('handle topic: ' + topic + " message: " + msg);
    switch (topic) {
      case '/app/all':
        this.setState({words: msg});
        break;
      case '/user/topic/new':
        this.setState({words: this.state.words.concat(msg)});
        break;
      default:
        console.log('unexpected topic');
    }
  }

  handleWordSubmitted(word) {
    this.sockJsClient.sendMessage('/app/add', word)
  }

  componentDidMount() {
    const checkAuthUrl = `${process.env.REACT_APP_WORD_HERD_API_ROOT}/checkAuth`;
    fetch(checkAuthUrl, {
      redirect: "manual",
      credentials: "include"
    }).then(response => {
      this.setState({authenticated: response.ok})
    });
  }

  render() {
    const loginUrl = `${process.env.REACT_APP_WORD_HERD_API_ROOT}/oauth2/authorization/github`;
    const authenticated = this.state.authenticated;
    if (!authenticated) {
      return (<div className="Words">
        <Button bsStyle="primary" bsSize="large" href={loginUrl}>Login with GitHub</Button>
      </div>);
    }

    const connected = this.state.connected;
    let controls;
    if (connected) {
      controls = <div>
        <p>This is your Word Herd.</p>
        <hr/>
        <WordList words={this.state.words}/>
        <WordInput submitWord={(word) => this.handleWordSubmitted(word)}/>
        <hr/>
      </div>;
    } else {
      controls = <div>
        <p>Connecting...</p>
        <hr/>
      </div>;
    }

    const websocketUrl = `${process.env.REACT_APP_WORD_HERD_API_ROOT}/words`;
    const logoutUrl = `${process.env.REACT_APP_WORD_HERD_API_ROOT}/logout`;

    return (<div className="Words">
      <SockJsClient url={websocketUrl} topics={['/app/all', '/user/topic/new']} onMessage={this.handleMessage} onConnect={() => {
          this.setState({connected: true})
        }} onDisconnect={() => {
          this.setState({connected: false})
        }} ref={(client) => {
          this.sockJsClient = client
        }}/> {controls}

      <form action={logoutUrl} method="post">
        <Button type="submit">Log Out</Button>
      </form>
    </div>);

  }
}

export default Words;
