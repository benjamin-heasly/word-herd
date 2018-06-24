import React, { Component } from 'react';
import { FormGroup, FormControl } from 'react-bootstrap';
import './WordInput.css';

class WordInput extends Component {

  constructor(props) {
    super(props);

    this.state = {word: "",}

    this.handleInput = this.handleInput.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleInput(event) {
    const word = event.target.value;
    this.setState({word: word})
  }

  handleSubmit(event) {
    const word = this.state.word;
    this.props.submitWord(word);
    this.setState({word: "",});

    event.preventDefault();
  }

  render() {
    return (
      <div className="WordInput">
      <form onSubmit={this.handleSubmit} >
        <FormGroup >
          <FormControl
            type="text"
            placeholder="add a word"
            value={this.state.word}
            onChange={this.handleInput} />
        </FormGroup>
      </form>
      </div>
    );
  }
}

export default WordInput;
