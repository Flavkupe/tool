package edu.bowdoin.robocup.TOOL.Calibrate;

import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JButton;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;


import javax.swing.InputMap;
import javax.swing.ActionMap;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import edu.bowdoin.robocup.TOOL.Data.DataListener;
import edu.bowdoin.robocup.TOOL.Data.Frame;
import edu.bowdoin.robocup.TOOL.Data.DataSet;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.text.*;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Cursor;

import edu.bowdoin.robocup.TOOL.Image.ImageOverlay;

import edu.bowdoin.robocup.TOOL.Calibrate.ColorSwatchParent;
import edu.bowdoin.robocup.TOOL.TOOL;


public class CalibratePanel extends JPanel implements DataListener, KeyListener,
MouseWheelListener{

    private ColorSwatchPanel colorSwatches;
    private JCheckBox undefineSpecific, smallTableMode;
    private ThreshSlider thresh;
    private JButton fillHoles, undo, redo, prevImage, nextImage, jumpToButton;
    private JTextField jumpToFrame;
    private JTextPane feedback;
    private InputMap im;
    private ActionMap am;


    private Calibrate calibrate;

    private static final int NUM_COLUMNS = 20;
    private static final int NUM_ROWS = 2;
    private static final int DEFAULT_COLOR_SWATCH_WIDTH = 40;

    public CalibratePanel(Calibrate aCalibrate) {
	super();
	calibrate = aCalibrate;
        setupWindow();
	setupListeners();
    }

    public ColorSwatchPanel getSwatches() {
        return colorSwatches;
    }

    private void setupWindow() {
        
        colorSwatches = new ColorSwatchPanel(calibrate, 
					     DEFAULT_COLOR_SWATCH_WIDTH);

        // centering text from http://forum.java.sun.com/thread.jspa?threadID=166685&messageID=504493
        feedback = new JTextPane();
        // Make the text centered
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyledDocument doc = feedback.getStyledDocument();
        StyleConstants.setAlignment(set,StyleConstants.ALIGN_CENTER);
        feedback.setParagraphAttributes(set,true);

	feedback.setEditable(false);
	feedback.setText("Welcome to TOOL 1.0");
        // Make the background match in color
        feedback.setBackground(this.getBackground());

	prevImage = new JButton("Previous (S)");
	prevImage.setFocusable(false);
	
	nextImage = new JButton("Next (D)");
	nextImage.setFocusable(false);

	fillHoles = new JButton("Fill Holes (H)");
	fillHoles.setFocusable(false);

        smallTableMode = new JCheckBox("Small Table Mode");
	

	undo = new JButton("Undo");
	undo.setFocusable(false);

	redo = new JButton("Redo");
	redo.setFocusable(false);
	
	jumpToFrame = new JTextField("0", 4);
	
	jumpToButton = new JButton("Jump");
	jumpToButton.setFocusable(false);
       
	undefineSpecific = new JCheckBox("Undefine color");
        undefineSpecific.setAlignmentX(Component.CENTER_ALIGNMENT);
	undefineSpecific.setFocusable(false);

	thresh = new ThreshSlider(calibrate);
	thresh.setFocusable(false);
        
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

       
        JPanel navigation = new JPanel();
        navigation.setLayout(new GridLayout(4,2));
        navigation.add(prevImage);
        navigation.add(nextImage);
        navigation.add(undo);
        navigation.add(redo);
        navigation.add(jumpToFrame);
        navigation.add(jumpToButton);
        navigation.add(fillHoles);
        navigation.add(smallTableMode);
        
        // Size the navigation panel to only take up as much room as needed
        Dimension navigationSize = new Dimension(2 * (int) smallTableMode.getPreferredSize().getWidth(), 4 * (int) fillHoles.getPreferredSize().getHeight());
        navigation.setMinimumSize(navigationSize);
        navigation.setPreferredSize(navigationSize);
        navigation.setMaximumSize(navigationSize);
        
       
        JPanel textAndSwatches = new JPanel();
        textAndSwatches.setLayout(new BoxLayout(textAndSwatches, 
                                                BoxLayout.Y_AXIS));
        textAndSwatches.add(feedback);
        textAndSwatches.add(undefineSpecific);
        textAndSwatches.add(colorSwatches);
        
        add(textAndSwatches);
        add(navigation);
        add(thresh);
        
        // Since the main panel of the calibration panel has a different
        // cursor, we want java to switch back to the default upon entering
        // this panel.
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        setupShortcuts();
        
    }
    



    private void setupShortcuts() {
        im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke metaJ = KeyStroke.getKeyStroke(KeyEvent.VK_J,
                InputEvent.META_MASK);
        im.put(metaJ, "jumpToField");

        int numColorSwatchShortcuts = 10;
        for (int i = 0; i < numColorSwatchShortcuts; i++) {
            // Put the standard num pad into input map
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, 0),
                   "color" + (i+1));
            // .. and the number pad
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1 + i, 0),
                   "color" + (i+1));
        }
       

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_MASK), "redo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "undefineSpecific");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "edgeThresh");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "fillHoles");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "nextImage");
	im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "lastImage");
        

        am = this.getActionMap();
        am.put("jumpToField", new AbstractAction("jumpToField") {
            public void actionPerformed(ActionEvent e) {
                 jumpToFrame.requestFocus();
                 jumpToFrame.setSelectionStart(0);
                 jumpToFrame.setSelectionEnd(jumpToFrame.getText().length());
            }
        });
       
        // Now register the action for the number pad stuff
        for (int i = 0; i <numColorSwatchShortcuts; i++) {
            am.put("color" + (i+1), new SetColorSwatchAction(i));
        }


        am.put("undo", new AbstractAction("undo") {
                public void actionPerformed(ActionEvent e) {
                    undo.doClick();
                }
            });
        am.put("redo", new AbstractAction("redo") {
                public void actionPerformed(ActionEvent e) {
                    redo.doClick();
                }
            });
        am.put("undefineSpecific", new AbstractAction("undefine") {
                public void actionPerformed(ActionEvent e) {
                    undefineSpecific.doClick();
                }
            });
        am.put("edgeThresh", new AbstractAction("edgeThresh") {
                public void actionPerformed(ActionEvent e) {
                    thresh.clickEnabled();
                }
            });
        am.put("fillHoles", new AbstractAction("fillHoles") {
                public void actionPerformed(ActionEvent e) {
                    fillHoles.doClick();
                }
            });
	am.put("nextImage", new AbstractAction("nextImage") {
                public void actionPerformed(ActionEvent e) {
                    calibrate.getTool().getDataManager().next();
                }
            });
	am.put("lastImage", new AbstractAction("lastImage") {
                public void actionPerformed(ActionEvent e) {
                    calibrate.getTool().getDataManager().last();
                }
            });
    }
    

    private void setupListeners() {
	// Let the slider listen to mouse wheel movements
        addMouseWheelListener(this);

	calibrate.getTool().getDataManager().addDataListener(this);


        smallTableMode.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
		    calibrate.setSmallTableMode(smallTableMode.isSelected());
		}
	    });



	undefineSpecific.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    calibrate.setUndefine(undefineSpecific.isSelected());
		    colorSwatches.setCrossedOut(undefineSpecific.isSelected());
		    colorSwatches.repaint();
		}
	    });

	prevImage.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
                    calibrate.getLastImage();
                }
	});
	
	nextImage.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
                    calibrate.getNextImage();
                }
	});
	
	fillHoles.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    calibrate.fillHoles();
		}
	    });	
	
	undo.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
                    calibrate.undo();
		}
	    });

	redo.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    calibrate.redo();
		}
	    });
	
        jumpToFrame.addKeyListener(new KeyListener(){
                public void keyPressed(KeyEvent e){
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        jumpToFrame.transferFocus();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        jumpToButton.doClick();
                    }
                }
                public void keyTyped(KeyEvent e) {}
                public void keyReleased(KeyEvent e){}
            });
	
        jumpToButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int newIndex = Integer.parseInt(jumpToFrame.getText());
                        calibrate.setImage(newIndex);
                        jumpToFrame.transferFocus();
                    }
                    // If they didn't have a legit number in the field, this
                    // catches the problem to avoid a crash.
                    catch (NumberFormatException f) { 
                        jumpToFrame.setText("Invalid");
                        jumpToFrame.setSelectionStart(0);
                        jumpToFrame.setSelectionEnd("Invalid".length());
                        return; 
                    }
                }
            });
	

    }

    
    /**
     * Greys out buttons depending on whether we can actually use them at this
     * moment; e.g. undo button is initially grey because you cannot undo until
     * there is something on the undo stack.  Similarly for the previous image
     * and next image buttons.  Finally, fill holes and the jump button and
     * text area are only accessible if we have an image loaded.
     */
    public void fixButtons() {
        undo.setEnabled(calibrate.canUndo());
        redo.setEnabled(calibrate.canRedo());
        prevImage.setEnabled(calibrate.canGoBackward());
        nextImage.setEnabled(calibrate.canGoForward());
        fillHoles.setEnabled(calibrate.hasImage());
        jumpToFrame.setEnabled(calibrate.hasImage());
        jumpToButton.setEnabled(calibrate.hasImage());
        smallTableMode.setSelected(calibrate.isSmallTableMode());
    }

    public void setText(String text) {
	feedback.setText(text);
    }

    
    public void setColorSelected(byte color) {
	colorSwatches.setSelected(color);
    }
   
    public void notifyDataSet(DataSet s, Frame f) {
		
    }
	
    /** Set the text in the box to update the frame number. */
    public void notifyFrame(Frame f) {
        jumpToFrame.setText((new Integer(f.index())).toString());
    }
    
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
            thresh.pressPlus();
        }
        else{
            thresh.pressMinus();
        }
    }


    class SetColorSwatchAction extends AbstractAction {
        private int i;
        public SetColorSwatchAction(int i) {
            this.i = i;
        }
        public void actionPerformed(ActionEvent e) {
            if (!jumpToFrame.hasFocus()) {
                    colorSwatches.setColor(i);
            }
        }

    }
    
}

