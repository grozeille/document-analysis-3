
import React from 'react';
import CopyToClipboard from 'react-copy-to-clipboard';
import Tooltip from '@material-ui/core/Tooltip';
import ClickAwayListener from '@material-ui/core/ClickAwayListener';
import { withStyles, WithStyles } from '@material-ui/styles';
import { Theme } from "@material-ui/core";

const styles = (theme: Theme) => ({
  copyLink: {
    color: theme.palette.text.primary,
    textDecoration: "none",
    cursor: "pointer",
    '&:hover': {
      borderBottom: "2px dotted rgba(0, 0, 0, 0.50)",
    }
  }
});

interface TextClipboardProps extends WithStyles<typeof styles> {
  text: string;
}

interface TextClipboardState {
  showCopyTooltip: boolean;
}

class TextClipboard extends React.Component<TextClipboardProps, TextClipboardState> {
  constructor(props: TextClipboardProps){
    super(props);
    this.state = { 
      showCopyTooltip: false,
    };
  }

  private handleTooltipClose() {
    this.setState({ showCopyTooltip: false });
  }

  private handleTooltipOpen() {
    this.setState({ showCopyTooltip: true });
  }

  render() {
    return (
      <ClickAwayListener onClickAway={() => this.handleTooltipClose()}>
            <CopyToClipboard onCopy={() => this.handleTooltipOpen()} text={this.props.text}>
              <Tooltip 
                title="Copié!" 
                placement="top"
                PopperProps={{
                  disablePortal: true,
                }}
                onClose={() => this.handleTooltipClose() }
                open={this.state.showCopyTooltip}
                disableFocusListener
                disableHoverListener
                disableTouchListener>
                <span className={this.props.classes.copyLink}>{this.props.text}</span>
              </Tooltip>
            </CopyToClipboard>
          </ClickAwayListener>
    )
  }
}

export default withStyles(styles)(TextClipboard);