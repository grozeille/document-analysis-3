import React from 'react';
import { withStyles, WithStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import './Search.css';
import {RouteComponentProps, withRouter} from "react-router";

import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import SearchIcon from '@material-ui/icons/Search';
import IconButton from '@material-ui/core/IconButton';
import Fab from '@material-ui/core/Fab';
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward';
import { createBrowserHistory } from 'history';

import { SearchResultItem, SearchResult } from './Model'
import SearchResultList from './SearchResultList';

const history = createBrowserHistory();

const styles = (theme: Theme) => ({
  root: {
    padding: '2px 4px',
    display: 'flex',
    alignItems: 'center',
    width: '100%',
  },
  input: {
    marginLeft: 8,
    flex: 1,
  },
  iconButton: {
    padding: 10,
  },
  divider: {
    width: 1,
    height: 28,
    margin: 4,
  },
  searchMessage: {
    fontSize: "12px",
    paddingTop: "20px",
    color: "#545454",
  },
});

export interface SearchState {
  searchResultItems : Array<Array<SearchResultItem>>;
  searchMessage: string;
  showMore: boolean;
  currentPage: number;
  searchQuery: string;
}

interface SearchProps extends RouteComponentProps, WithStyles<typeof styles> {
  
}


class Search extends React.Component<SearchProps, SearchState> {

  private searchInput?: HTMLInputElement = undefined;

  constructor(props: SearchProps){
    super(props);
    this.state = { 
      searchResultItems : new Array<Array<SearchResultItem>>(),
      searchMessage: "",
      showMore: false,
      currentPage: 0,
      searchQuery: ""
    };
  }

  componentDidMount(){
    this.searchInput!.focus();
    
    const search = this.props.location.search;
    const params = new URLSearchParams(search);
    const query = params.get('query');
    if(query) {
      this.searchInput!.value = query;
      this.handleSearch();
    }
  }

  render() {
    return (
      <div>
        <Paper className={this.props.classes.root}>
          <InputBase
            className={this.props.classes.input}
            placeholder="Recherche"
            inputProps={{ 'aria-label': 'Recherche' }}
            onKeyDown={ (event) => this.handleSearchKeyboard(event) }
            inputRef={(input:HTMLInputElement ) => { this.searchInput = input; }} 
          />
          <IconButton 
            className={this.props.classes.iconButton} 
            aria-label="Search" 
            onClick={ (event) => this.handleSearch() }>
            <SearchIcon />
          </IconButton>
        </Paper>
        <div className={this.props.classes.searchMessage}>{this.state.searchMessage}</div>
        {this.state.searchResultItems.map((page: Array<SearchResultItem>, pageKey: any) =>
          <div key={pageKey}>
            <SearchResultList items={page} />
          </div>
        )}
        { this.state.showMore && 
          <div style={{ textAlign: "center", margin: "auto", marginTop: "20px"}}>
            <Fab
              variant="extended"
              size="medium"
              color="primary"
              aria-label="More"
              onClick={ (event) => this.handleSearchNextPage(event) }
            >
              <ArrowDownwardIcon/>
              Voir plus
            </Fab>
          </div>
        }
        
      </div>
    )
  }

  private handleSearchNextPage(e: React.MouseEvent<HTMLButtonElement>) {
    console.info("Search !");

    var nextPage = this.state.currentPage + 1;

    var url = "/api/v1/documents/?query=" + encodeURIComponent(this.state.searchQuery) + 
      "&page=" + nextPage;

    fetch(url)
      .then(this.handleErrors)
      .then(res => res.json())
      .then(
        (result: SearchResult) => {
          console.info("Found !");
          
          this.setState(state => {
            const searchResultItems = state.searchResultItems;
            searchResultItems.push(result.documents);

            return {
              currentPage: nextPage,
              searchResultItems: searchResultItems,
              showMore: (nextPage + 1) * 20 < result.numFound,
            }
          });
        },
        // Note: it's important to handle errors here
        // instead of a catch() block so that we don't swallow
        // exceptions from actual bugs in components.
        (error) => {
          this.setState({
            searchResultItems : new Array<Array<SearchResultItem>>(),
            searchMessage: "",
            showMore: false,
            currentPage: 0,
          });
        }
      )
      .catch((error: any) => {
        this.setState({
          searchResultItems : new Array<Array<SearchResultItem>>(),
          searchMessage: error,
          showMore: false,
          currentPage: 0,
        });
      });
  }

  private handleErrors(response: any) {
      if (!response.ok) {
          throw Error(response.statusText);
      }
      return response;
  }

  private handleSearch() {
    console.info("Search !");
    var query = this.searchInput!.value;

    this.setState({
      searchQuery: query,
      currentPage: 0,
    })

    history.push({
      pathname: '/',
      search: '?query=' + encodeURIComponent(query)
    })

    var url =  "/api/v1/documents/?query=" + encodeURIComponent(query);

    fetch(url)
      .then(this.handleErrors)
      .then(res => res.json())
      .then(
        (result: SearchResult) => {
          console.info("Found !");
          
          console.log(result);

          this.setState(state => {
            const searchResultItems = new Array<Array<SearchResultItem>>();
            searchResultItems.push(result.documents);

            return {
              searchResultItems: searchResultItems,
              searchMessage: result.numFound + " résultats",
              showMore: (state.currentPage + 1) * 20 < result.numFound,
            }
          });
        },
        // Note: it's important to handle errors here
        // instead of a catch() block so that we don't swallow
        // exceptions from actual bugs in components.
        (error) => {
          this.setState({
            searchResultItems : new Array<Array<SearchResultItem>>(),
            searchMessage: "",
            showMore: false,
            currentPage: 0,
          });
        }
      )
      .catch((error: any) => {
        this.setState({
          searchResultItems : new Array<Array<SearchResultItem>>(),
          searchMessage: error,
          showMore: false,
          currentPage: 0,
        });
      });
  }

  private handleSearchKeyboard(e: React.KeyboardEvent<HTMLElement | HTMLTextAreaElement>) {
    if( e.key === 'Enter' ){
      this.handleSearch();
    }
  }
}

export default withRouter(withStyles(styles)(Search));
