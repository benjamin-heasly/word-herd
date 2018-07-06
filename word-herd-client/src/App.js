import React, { Component } from 'react';
import { PageHeader } from 'react-bootstrap';
import logo from './logo.png';
import './App.css';
import WordInput from './components/WordInput';
import WordList from './components/WordList';
import WordSocket from './components/WordSocket';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = { words: [] }

    this.handleWordSubmitted = this.handleWordSubmitted.bind(this);
  }

  handleWordSubmitted(word) {
    this.submit(word);
  }

  render() {
    return (
      <div className="App">
        <PageHeader className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
        </PageHeader>

        <WordSocket
          setWords={(allWords) => this.setState({ words: allWords })}
          addWord={(word) => this.setState({words: this.state.words.concat([word])})}
          setSubmit={ (submit) => this.submit=submit }/>

        <WordList
          words={this.state.words} />

        <WordInput
          submitWord={(word) => this.handleWordSubmitted(word)} />
      </div>
    );
  }
}

export default App;
