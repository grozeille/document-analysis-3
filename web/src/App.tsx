import React from 'react';
import clsx from 'clsx';

import './App.css';

import { BrowserRouter as Router, Route, Link } from 'react-router-dom';
import CssBaseline from '@material-ui/core/CssBaseline';
import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import Container from '@material-ui/core/Container';
import Grid from '@material-ui/core/Grid';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import SearchIcon from '@material-ui/icons/Search';
import SettingsIcon from '@material-ui/icons/Settings';
import { ThemeProvider } from '@material-ui/styles';
import { Theme } from "@material-ui/core";
import { makeStyles, createMuiTheme, withStyles } from '@material-ui/core/styles';
import blue from '@material-ui/core/colors/blue';
import { createBrowserHistory } from 'history';
import { secondaryListItems } from './SecondaryMenu';
import Search from './Search';
import { SearchState } from './Search';
import { SearchResultItem, SearchResult } from './Model'
import Settings from './Settings';
import Preview from './Preview';

const theme = createMuiTheme({
  palette: {
    primary: blue,
  },
});

const drawerWidth = 240;

const styles = (theme: Theme) => ({
  root: {
    display: 'flex',
  },
  toolbar: {
    paddingRight: 24, // keep right padding when drawer closed
  },
  toolbarIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
    ...theme.mixins.toolbar,
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  appBarShift: {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  menuButton: {
    marginRight: 36,
  },
  menuButtonHidden: {
    display: 'none',
  },
  title: {
    flexGrow: 1,
  },
  appBarSpacer: theme.mixins.toolbar,
  content: {
    flexGrow: 1,
    height: '100vh',
    overflow: 'auto',
  },
  fixedHeight: {
    height: 240,
  },
  drawerPaper: {
    position: "relative" as 'relative',
    whiteSpace: 'nowrap' as 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerPaperClose: {
    overflowX: 'hidden' as 'hidden',
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    width: theme.spacing(7),
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing(9),
    },
  },
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
    overflowScrolling: "touch" as 'touch',
    WebkitOverflowScrolling: "touch" as 'touch',
  },
  paper: {
    padding: theme.spacing(2),
    display: 'flex',
    overflow: 'auto',
    flexDirection: 'column' as 'column',
  },
});


interface AppState {
  open: boolean;
}

class App extends React.Component<any, AppState> {

  constructor(props: any) {
    super(props);
    this.state = { 
      open : false,
    };
  }

  handleDrawerClose() {
    this.setState({ open : false});
  }

  handleDrawerOpen() {
    this.setState({ open : true});
  }

  render() {
    
    return (
      <Router>
        <ThemeProvider theme={theme}>
      <div className={this.props.classes.root}>
        <CssBaseline />
        <AppBar position="absolute" className={clsx(this.props.classes.appBar, this.state.open && this.props.classes.appBarShift)}>
          <Toolbar className={this.props.classes.toolbar}>
            <IconButton
              edge="start"
              color="inherit"
              aria-label="Open drawer"
              onClick={this.handleDrawerOpen}
              className={clsx(this.props.classes.menuButton, this.state.open && this.props.classes.menuButtonHidden)}
            >
              <MenuIcon />
            </IconButton>
            <Typography component="h1" variant="h6" color="inherit" noWrap className={this.props.classes.title}>
              Documents
            </Typography>
          </Toolbar>
        </AppBar>
        <Drawer
          variant="permanent"
          classes={{
            paper: clsx(this.props.classes.drawerPaper, !this.state.open && this.props.classes.drawerPaperClose),
          }}
          open={this.state.open}
        >
          <div className={this.props.classes.toolbarIcon}>
            <IconButton onClick={this.handleDrawerClose}>
              <ChevronLeftIcon />
            </IconButton>
          </div>
          <Divider />
          <List>
            <ListItem button component={Link} to="/">
              <ListItemIcon>
                <SearchIcon />
              </ListItemIcon>
              <ListItemText primary="Recherche" />
            </ListItem>
            <ListItem button component={Link} to="/settings">
              <ListItemIcon>
                <SettingsIcon />
              </ListItemIcon>
              <ListItemText primary="Paramétrage" />
            </ListItem>
          </List>
          <Divider />
          <List>{secondaryListItems}</List>
        </Drawer>
        <main className={this.props.classes.content}>
          <div className={this.props.classes.appBarSpacer} />
          <Container maxWidth="lg" className={this.props.classes.container}>
            <Grid container spacing={3}>
              {/* Recent Orders */}
              <Grid item xs={12}>
                  <Route path="/" exact 
                    component={() => <Search />} />
                  <Route path="/settings/" component={Settings} />
                  <Route path="/preview" component={Preview} />
              </Grid>
            </Grid>
          </Container>
        </main>
      </div>
      </ThemeProvider>
      </Router>
    );
  }
}

export default withStyles(styles)(App);
