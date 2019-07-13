
import React from 'react';
import { withStyles, WithStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import { SearchResultItem } from './Model'
import { Link } from 'react-router-dom'
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
  searchText: {
    color: "#545454",
    '& em': {
      backgroundColor: "#ffffcc",
    },
  },
  chip: {
    margin: theme.spacing(0.5),
  },
});

interface SearchResultListProps extends WithStyles<typeof styles> {
  items: Array<SearchResultItem>;
}

interface SearchResultListState {
  items: Array<SearchResultItem>;
}

class SearchResultList extends React.Component<SearchResultListProps, SearchResultListState> {
  constructor(props: SearchResultListProps){
    super(props);
    this.state = { 
      items: props.items,
    };
  }

  componentDidUpdate(prevProps: SearchResultListProps, prevState: SearchResultListState) {
    if (this.props.items !== prevProps.items || this.state.items !== prevState.items) {
      this.setState(state => {
        return {
          items: this.props.items,
        }
      });
    }
  }

  render() {
    return (
      <div>
        {this.state.items.map((item: SearchResultItem, key: any) =>
            <div className={this.props.classes.searchItem} key={key}>
              <div><Link 
                to={"/preview?id="+item.id} 
                className={this.props.classes.searchTitle} 
                dangerouslySetInnerHTML={{ __html: item.name }} /></div>
              <div><Link 
                to={"/preview?id="+item.id} 
                className={this.props.classes.searchUrl} 
                dangerouslySetInnerHTML={{ __html: item.urlTxt }} /></div>
              <div className={this.props.classes.searchText} dangerouslySetInnerHTML={{ __html: item.body }}></div>
              <div>
                {item.tags.map((item: string, key: any) => 
                  <Chip
                  size="small"
                  key={item}
                  label={item}
                  className={this.props.classes.chip}
                  />
                )}
              </div>
            </div>
        )}  
      </div>
    )
  }
}

export default withStyles(styles)(SearchResultList);