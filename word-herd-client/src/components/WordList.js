import React, {Component} from 'react';
import {ListGroup, ListGroupItem} from 'react-bootstrap';
import './WordList.css';

class WordList extends Component {
  render() {
    return (<div className="WordList">
      <ListGroup>
        {this.props.words.map((word) => <ListGroupItem key={word}>{word}</ListGroupItem>)}
      </ListGroup>
    </div>);
  }
}

export default WordList;
