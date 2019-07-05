import React from 'react';

import Paper from '@material-ui/core/Paper';
import { makeStyles } from '@material-ui/core/styles';

const useStyles = makeStyles(theme => ({
  paper: {
    padding: theme.spacing(2),
    display: 'flex',
    overflow: 'auto',
    flexDirection: 'column',
  },
}));

function Settings() {
  const classes = useStyles();

  return (
  <div>
    <Paper className={classes.paper}>
      <h2>Paramétrage</h2>    
    </Paper>
  </div>
  );
}

export default Settings;