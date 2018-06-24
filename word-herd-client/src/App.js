import React, { Component } from 'react';
import logo from './logo.png';
import './App.css';
import WordInput from './components/WordInput';
import WordList from './components/WordList';
import { PageHeader } from 'react-bootstrap';

class App extends Component {
  constructor(props) {
    super(props);

    this.state = {
      words: ["aardvark", "zoop", "frg"]
    }
  }

  addWord(word) {
    const words = this.state.words;

    if ( words.indexOf(word) === -1) {
      this.setState({words: this.state.words.concat([word])});
      return true;
    }
    return false;
  }

  render() {
    return (
      <div className="App">
        <PageHeader className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
        </PageHeader>
        <p className="App-intro"> This is your Word Herd. </p>
        <WordList
          words={this.state.words}
        />
        <WordInput
          addWord={(word) => this.addWord(word)}
        />
      </div>
    );
  }
}

export default App;
