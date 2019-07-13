import React from 'react';
import { Link } from 'react-router-dom'
import { SearchResultItem, SearchResult } from './Model'
import './Preview.css';
import { withStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import Fab from '@material-ui/core/Fab';
import SaveAltIcon from '@material-ui/icons/SaveAlt';
import TextField from '@material-ui/core/TextField';
import Chip from '@material-ui/core/Chip';

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
  chip: {
    margin: theme.spacing(0.5),
  },
});

interface PreviewState {
  searchResultItem : SearchResultItem;
  sameItems : SearchResult;
  id: string;
}

class Preview extends React.Component<any, PreviewState> {

  private newTagInput?: HTMLInputElement = undefined;

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

  private handleDelete(tag: string){
    this.setState(state => {
      var searchResultItem = state.searchResultItem;
      searchResultItem.tags = searchResultItem.tags.filter(t => t !== tag);
      
      return {
        searchResultItem
      }
    }, () => {
      fetch("/api/v1/documents/" + this.state.id + "/tags", {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.state.searchResultItem.tags)
      });
    });
  }

  private handleSearchAddTag(e: React.KeyboardEvent<HTMLElement | HTMLTextAreaElement>) {
    if( e.key === 'Enter' ){
      var newTag = this.newTagInput!.value;
      this.newTagInput!.value = "";
      this.setState(state => {
        var searchResultItem = state.searchResultItem;
        searchResultItem.tags.push(newTag);
  
        return {
          searchResultItem
        }
      }, () => {
        fetch("/api/v1/documents/" + this.state.id + "/tags", {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.state.searchResultItem.tags)
        });
      });
    }
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
        <div>
          <TextField
            id="standard-name"
            label="Ajouter un tag"
            margin="normal"
            onKeyDown={ (event) => this.handleSearchAddTag(event) }
            inputRef={(input:HTMLInputElement ) => { this.newTagInput = input; }} 
          />
        </div>
        <div>
        {this.state.searchResultItem.tags.map((item: string, key: any) => 
          <Chip
          key={item}
          label={item}
          className={this.props.classes.chip}
          onDelete={() => this.handleDelete(item)}
          />
        )}
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
