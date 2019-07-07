import React from 'react';
import { withStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import './Search.css';

import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import SearchIcon from '@material-ui/icons/Search';
import IconButton from '@material-ui/core/IconButton';
import Fab from '@material-ui/core/Fab';
import ArrowDownwardIcon from '@material-ui/icons/ArrowDownward';

interface SearchResult {
  documents: Array<SearchResultItem>;
  numFound: bigint;
}

interface SearchResultItem {
  id: string;
  url: string;
  name: string;
  body: string;
}

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
  searchTitle: {
    fontSize: "18px",
    lineHeight: 1.33,
    textDecoration: "none",
    cursor: "pointer",
    color: "rgba(0, 0, 0, 0.87)",
    textDecorationColor: "rgba(0, 0, 0, 0.87)",
    '&:hover': {
      textDecoration: 'underline',
    },
  },
  searchUrl: {
    color: theme.palette.primary.dark,
    textDecoration: "none",
    cursor: "pointer",
    '&:hover': {
      textDecoration: 'underline',
    },
  },
  searchText: {
    color: "#545454",
    '& em': {
      backgroundColor: "#ffffcc",
    },
  },
  searchItem: {
    paddingTop: "30px",
  },
  searchMessage: {
    fontSize: "12px",
    paddingTop: "20px",
    color: "#545454",
  },
});

interface SearchState {
  searchResultItems : Array<Array<SearchResultItem>>;
  searchMessage: string;
  showMore: boolean;
  currentPage: number;
  searchQuery: string;
  apiUrl: string;
}

class Search extends React.Component<any, SearchState> {

  private searchInput?: HTMLInputElement = undefined;

  constructor(props: any){
    super(props);
    this.state = { 
      searchResultItems : new Array<Array<SearchResultItem>>(),
      searchMessage: "",
      showMore: false,
      currentPage: 0,
      searchQuery: "",
      apiUrl: "",
    };
    
  }

  componentDidMount(){
    this.searchInput!.focus(); 

    var configUrl = "/config.json";

    fetch(configUrl)
        .then(res => res.json())
        .then(
          (result) => {
            this.setState({
              apiUrl : result.apiUrl,
            });
          },
          // Note: it's important to handle errors here
          // instead of a catch() block so that we don't swallow
          // exceptions from actual bugs in components.
          (error) => {
            console.error(error);
          }
        )
  }

  render() {
    return (
      <div>
        <Paper className={this.props.classes.root}>
          <InputBase
            className={this.props.classes.input}
            placeholder="Recherche"
            inputProps={{ 'aria-label': 'Recherche' }}
            onKeyDown={ (event) => this.handleSearch(event) }
            inputRef={(input:HTMLInputElement ) => { this.searchInput = input; }} 
          />
          <IconButton className={this.props.classes.iconButton} aria-label="Search">
            <SearchIcon />
          </IconButton>
        </Paper>
        <div className={this.props.classes.searchMessage}>{this.state.searchMessage}</div>
        {this.state.searchResultItems.map((page: Array<SearchResultItem>, pageKey: any) =>
          <div key={pageKey}>
          {page.map((item: SearchResultItem, key: any) =>
              <div className={this.props.classes.searchItem} key={key}>
                <div><a className={this.props.classes.searchTitle} href={ "file://" + item.url} target="_blank">{item.name}</a></div>
                <div><a className={this.props.classes.searchUrl} href={ "file://" + item.url} target="_blank">{item.url}</a></div>
                <div className={this.props.classes.searchText} dangerouslySetInnerHTML={{ __html: item.body }}></div>
              </div>
          )}
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

    var url = this.state.apiUrl + 
      "/v1/documents/?query=" + encodeURIComponent(this.state.searchQuery) + 
      "&page=" + nextPage;

    fetch(url)
        .then(res => res.json())
        .then(
          (result) => {
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
  }

  private handleSearch(e: React.KeyboardEvent<HTMLElement | HTMLTextAreaElement>) {
    if( e.key === 'Enter' ){
      console.info("Search !");
      var query:string = "";
      if(e.target instanceof HTMLInputElement) {
        query = e.target.value;
      }
      else if(e.target instanceof HTMLTextAreaElement) {
        query = e.target.value;
      }

      this.setState({
        searchQuery: query,
        currentPage: 0,
      })


      var url = this.state.apiUrl + "/v1/documents/?query=" + encodeURIComponent(query);

      fetch(url)
        .then(res => res.json())
        .then(
          (result) => {
            console.info("Found !");
            
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
    }
  }
}

export default withStyles(styles)(Search);
