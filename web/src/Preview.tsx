import React from 'react';
import { Link } from 'react-router-dom'
import { SearchResultItem, SearchResult } from './Model'
import './Preview.css';
import { withStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import Fab from '@material-ui/core/Fab';
import SaveAltIcon from '@material-ui/icons/SaveAlt';

const styles = (theme: Theme) => ({
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
    '& em': {
      backgroundColor: "#ffffcc",
    },
  },
  searchUrl: {
    color: theme.palette.primary.dark,
    textDecoration: "none",
    cursor: "pointer",
    '&:hover': {
      textDecoration: 'underline',
    },
    '& em': {
      backgroundColor: "#ffffcc",
    },
  },
  searchItem: {
    paddingTop: "30px",
  },
});

interface PreviewState {
  searchResultItem : SearchResultItem;
  sameItems : SearchResult;
  id: string;
}

class Preview extends React.Component<any, PreviewState> {

  constructor(props: any){
    super(props);
    this.state = { 
      searchResultItem : new SearchResultItem(),
      sameItems: new SearchResult(),
      id: "",
    };
    
  }

  componentDidMount(){
    const search = this.props.location.search;
    const params = new URLSearchParams(search);
    const id = params.get('id');

    this.refresh(id!);
  }

  refresh(id: string) {
    var url =  "/api/v1/documents/" + id;

    fetch(url)
      .then(res => res.json())
      .then(
        (result: SearchResultItem) => {
          console.info("Found !");
          
          this.setState({
            searchResultItem: result,
            id: id!
          });

          fetch(url+"/same")
            .then(res => res.json())
            .then(
              (result: SearchResult) => {
                console.info("Found !");
                
                this.setState({
                  sameItems: result
                });
              },
              // Note: it's important to handle errors here
              // instead of a catch() block so that we don't swallow
              // exceptions from actual bugs in components.
              (error) => {
                this.setState({
                  searchResultItem : new SearchResultItem(),
                  sameItems: new SearchResult(),
                  id: "",
                });
              }
            )
        },
        // Note: it's important to handle errors here
        // instead of a catch() block so that we don't swallow
        // exceptions from actual bugs in components.
        (error) => {
          this.setState({
            searchResultItem : new SearchResultItem(),
            sameItems: new SearchResult(),
            id: "",
          });
        }
      )
  }

  render() {
    return (
      <div>
        <div className="field"><b>Nom du fichier: </b>{this.state.searchResultItem.name}</div>
        <div className="field"><b>Chemin: </b>{this.state.searchResultItem.url}</div>
        <div style={{ textAlign: "center", margin: "auto", marginTop: "20px"}}>
          <Fab
            variant="extended"
            size="medium"
            color="primary"
            aria-label="More"
          >
            <SaveAltIcon/>
            Télécharger
          </Fab>
        </div>
        <div className="field similar"><b>Documents avec le même nom: </b></div>
        <div>
          {this.state.sameItems.documents.map((item: SearchResultItem, key: any) =>
              <div className={this.props.classes.searchItem} key={key}>
                <div><Link 
                  onClick={ (event) => this.refresh(item.id) }
                  to={"/preview?id="+item.id} 
                  className={this.props.classes.searchTitle} 
                  dangerouslySetInnerHTML={{ __html: item.name }} /></div>
                <div><Link 
                  onClick={ (event) => this.refresh(item.id) }
                  to={"/preview?id="+item.id} 
                  className={this.props.classes.searchUrl} 
                  dangerouslySetInnerHTML={{ __html: item.urlTxt }} /></div>
              </div>
          )}  
        </div>
      </div>
    )
  }

}

export default withStyles(styles)(Preview);
