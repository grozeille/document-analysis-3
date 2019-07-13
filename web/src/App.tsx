import React from 'react';
import clsx from 'clsx';
import { withStyles, createMuiTheme } from '@material-ui/core/styles';
import { WithStyles, ThemeProvider } from '@material-ui/styles';
import { Theme } from "@material-ui/core";

import blue from '@material-ui/core/colors/blue';

import { BrowserRouter as Router, Route, Link } from 'react-router-dom';

import Drawer from '@material-ui/core/Drawer';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import List from '@material-ui/core/List';
import CssBaseline from '@material-ui/core/CssBaseline';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import InboxIcon from '@material-ui/icons/MoveToInbox';
import MailIcon from '@material-ui/icons/Mail';
import Container from '@material-ui/core/Container';
import Grid from '@material-ui/core/Grid';
import SearchIcon from '@material-ui/icons/Search';
import SettingsIcon from '@material-ui/icons/Settings';

import Search from './Search';
import Settings from './Settings';
import Preview from './Preview';
import { secondaryListItems } from './SecondaryMenu';

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
  hide: {
    display: 'none',
  },
  drawer: {
    width: drawerWidth,
    flexShrink: 0,
    whiteSpace: 'nowrap' as 'nowrap',
  },
  drawerOpen: {
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerClose: {
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    overflowX: 'hidden' as 'hidden',
    width: theme.spacing(7) + 1,
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing(9) + 1,
    },
  },
  content: {
    flexGrow: 1,
    paddingTop: theme.spacing(3),
    paddingBottom: theme.spacing(3),
    //padding: theme.spacing(3),
  },
});


interface AppState {
  open: boolean;
}

interface AppProps extends WithStyles<typeof styles> {

}

class App extends React.Component<AppProps, AppState> {
  
  constructor(props: AppProps) {
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
        <AppBar
          position="fixed"
          className={clsx(this.props.classes.appBar, {
            [this.props.classes.appBarShift]: this.state.open,
          })}
        >
          <Toolbar>
            <IconButton
              color="inherit"
              aria-label="Open drawer"
              onClick={() => this.handleDrawerOpen() }
              edge="start"
              className={clsx(this.props.classes.menuButton, {
                [this.props.classes.hide]: this.state.open,
              })}
            >
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" noWrap>
              Documents
            </Typography>
          </Toolbar>
        </AppBar>
        <Drawer
          variant="permanent"
          className={clsx(this.props.classes.drawer, {
            [this.props.classes.drawerOpen]: this.state.open,
            [this.props.classes.drawerClose]: !this.state.open,
          })}
          classes={{
            paper: clsx({
              [this.props.classes.drawerOpen]: this.state.open,
              [this.props.classes.drawerClose]: !this.state.open,
            }),
          }}
          open={this.state.open}
        >
          <div className={this.props.classes.toolbar}>
            <IconButton onClick={() => this.handleDrawerClose() }>
              {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
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
          <div className={this.props.classes.toolbar} />
          <Container maxWidth="lg">
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
