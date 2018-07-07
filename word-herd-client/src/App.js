import React, {Component} from 'react';
import {PageHeader} from 'react-bootstrap';
import logo from './logo.png';
import './App.css';
import Words from './components/Words';

class App extends Component {
  render() {
    return (<div className="App">
      <PageHeader className="App-header">
        <img src={logo} className="App-logo" alt="logo"/>
      </PageHeader>
      <Words/>
    </div>);
  }
}

export default App;
