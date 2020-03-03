package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class BeamDataFrame extends JFrame
{
   BeamDataFrame frameSelfRef;
   JPanel pane;
   BeamDataPanel panelSelfRef;
   Box box1, box2;
   int qNo = 0;
   String question[] =
   {
       "Input number of spans (beam members) (1 to 10):      ",
       "Input typical span (beam length) in meters:          ",
       "Change the spans, if necessary. Then click 'Submit'. ",
       "Typical Mod. of Elasticity(E) of beam material (MPa) ",
       "Change the values, if necessary. Then click 'Submit' ",
       "Moment of Inertia(I) of typical cross-section (m.^4) ",
       "Change the values, if necessary. Then click 'Submit' ",
       "Now, specify support details...                      "
   };
   JLabel questionLabel = new JLabel(question[qNo]);
   JTextField answerTF;
   JTextField spanTF[];
   JButton spanSubmit = new JButton("Submit");
   JTextField modElaTF[];
   JButton modElaSubmit = new JButton("Submit");
   boolean eflag = false;
   JTextField momInerTF[];
   JButton momInerSubmit = new JButton("Submit");
   boolean miflag = false;
   
   ContinuousBeam cb = ContiBeamMain.cb;
   double typicalSpan, typicalE, typicalMI;
   
   JButton fixedBt = new JButton("Fixed Support");
   JButton freeBt = new JButton("Free Joint");
   JButton simpleSupBt = new JButton("Simple Support");
   boolean supportFlag = false;
   short supportIndex = 0;
   
   BeamDataFrame()
   {
       super("KITE - Get Beam Data");
       frameSelfRef = this;
       pane = new BeamDataPanel();

       setContentPane(pane);

       WindowListener listenWin = 
           new WindowAdapter()
	   {
	       public void windowClosing(WindowEvent we)
	       {
	           setVisible(false);
		   dispose();
	       }
	   };

       addWindowListener(listenWin);
   }
   
   class BeamDataPanel extends JPanel // inner class
   {
       String str;
	 
       BeamDataPanel()
       {
           setLayout(null);
	   panelSelfRef = this;
	   box1 = Box.createHorizontalBox();
	   box1.add(questionLabel);
	   answerTF = new JTextField("" + cb.getNSpans());
	   answerTF.setColumns(10);
	   AnswerActionListener listen1 = new AnswerActionListener();
	   answerTF.addActionListener(listen1);
	   spanSubmit.addActionListener(listen1);
	   modElaSubmit.addActionListener(listen1);
	   momInerSubmit.addActionListener(listen1);
	   box1.add(answerTF);
	   box1.setBounds(50, 10, 480, 40);
	   add(box1);

	   SupportActionListener listen2 = new SupportActionListener();
	   fixedBt.addActionListener(listen2);
	   freeBt.addActionListener(listen2);
	   simpleSupBt.addActionListener(listen2);
	   box2 = Box.createHorizontalBox();
	   box2.setBounds(50, 70, 600, 40);
	   box2.add(Box.createGlue());
	   box2.add(fixedBt);
	   box2.add(Box.createHorizontalStrut(20));
	   box2.add(freeBt);
	   box2.add(Box.createHorizontalStrut(20));
	   box2.add(simpleSupBt);
	   box2.add(Box.createGlue());
	   //add(box2);
       }
	 
       public void paintComponent(Graphics g)
       {
           double drawing_scale = 600 / cb.getTotal_length();
		
	   int nSpans = cb.getNSpans();
	   for(int i=0; i<nSpans; ++i)
	       drawBeam(g, i, 250, drawing_scale);
	   g.setColor(Color.BLUE);
	   g.drawLine(45, 240, 645, 240);
	   g.drawLine(45, 250, 645, 250);
	   g.setColor(Color.BLACK);

	   int nJoints = cb.getNJoints();
	   for(int i=0; i<nJoints; ++i)
	   {
               int jtType = cb.getJointType(i);
	       switch(jtType)
	       {
                   case Beam.FIXED:
		       drawFixedSupport(g, i, 250, drawing_scale);	
		       break;
                   case Beam.HINGE:
		       drawVertArrow(g, i, 250, drawing_scale);
		       break;
               }
           }
           if(eflag)   // Modulus of Elasticity
           {
               g.setColor(Color.RED);
	       int w1 = (int)(600/cb.getNSpans());
	       for(int i=0; i<nSpans; i++)
	       {
                   int x1 = 45 + (int)((cb.getJointPosX(i+1) + cb.getJointPosX(i)) / 2 
		                                  * drawing_scale);
		   int x2 = 45 + w1/2 + w1*i;
		   g.drawLine(x1, 265, x1, 275);
		   g.drawLine(x1, 275, x2, 290);
		   g.drawLine(x2, 290, x2, 300);
               }
           }
           if(miflag)   // Moment of Inertia
	   {
               g.setColor(Color.RED);
	       int w1 = (int)(600/cb.getNSpans());
	       for(int i=0; i<nSpans; i++)
	       {
                   int x1 = 45 + (int)((cb.getJointPosX(i+1) + cb.getJointPosX(i)) / 2 * drawing_scale);
		   int x2 = 45 + w1/2 + w1*i;
		   g.drawLine(x1, 265, x1, 275);
		   g.drawLine(x1, 275, x2, 290);
		   g.drawLine(x2, 290, x2, 300);
               }
           }
           if(supportFlag)
	   {
	       if(supportIndex == 0)
	       {
	           g.setColor(Color.RED);
		   g.drawOval(35, 230, 20, 70);
		   g.drawLine(45, 230, 55, 200);
		   g.setColor(Color.BLACK);
		   g.drawString("Click a button!", 45, 195);
               }
	       else if(supportIndex == (short)(nJoints-1))
	       {
	           g.setColor(Color.RED);
		   g.drawOval(635, 230, 20, 70);
		   g.drawLine(645, 230, 635, 200);
		   g.setColor(Color.BLACK);
		   g.drawString("Click a button!", 605, 195);
               }
	       else
	       {
	           int x = 45 + (int)(cb.getJointPosX(supportIndex) * drawing_scale);
		   g.setColor(Color.RED);
		   g.drawLine(x, 237, x, 225);
		   g.drawLine(x, 237, x-4, 233);
		   g.drawLine(x, 237, x+4, 233);
		   g.drawLine(x, 225, x+10, 200);
		   g.setColor(Color.BLACK);
		   g.drawString("Specify this joint!", x, 193); 
               }
           }
       }

       void drawBeam(Graphics g, int beamIndex, int y, double scale)
       {
           int x = 45 + (int)(cb.getJointPosX(beamIndex) * scale);
	   int L = (int)(cb.getMemberLength(beamIndex) * scale);
	   int t = cb.getBeamThickness(beamIndex);
	   if(t==1)
	       return;
	   g.fillRect(x, y-t, L, t);
       }

       void drawVertArrow(Graphics g, int jtIndex, int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(jtIndex) * scale);
	   g.drawLine(x, y, x, y+40);
	   g.drawLine(x, y, x-5, y+6);
	   g.drawLine(x, y, x+5, y+6);
       }

       void drawFixedSupport(Graphics g, int jtIndex, int y, double scale)
       {
	   if((int)cb.getJointPosX(jtIndex) == 0) // left-most end
	   {
	       int x = 45;
	       g.drawLine(x, y-25, x, y+25);
	       for(int i=0; i<45; i+=5)
	       {
	           g.drawLine(x, y-23+i, x-5, y-18+i);
               }
	   }
	   else  // right-most end
	   {
	       int x = 45 + (int)(cb.getJointPosX(jtIndex) * scale);
	       g.drawLine(x, y-25, x, y+25);
	       for(int i=0; i<50; i+=5)
	       {
	           g.drawLine(x, y-23+i, x+5, y-28+i);
	       }
	   }
       }
   } // BeamDataPanel class ends
   
   class AnswerActionListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           Object source = ae.getSource();
	   String str;
	   if(source == spanSubmit)
	   {
	       int j=0;
	       double beamLengths[] = new double[cb.getNSpans()];
	       try
	       {
	           for(j=0; j<cb.getNSpans(); j++)
		   {
		       str = spanTF[j].getText();
		       beamLengths[j] = Math.abs(Double.parseDouble(str.trim()));  
                   }
               }
	       catch(NumberFormatException e) 
	       {
	           JOptionPane.showMessageDialog(null, 
		    	"Beam " + (j+1) 
		    	+ " length must be a positive real number!",
				"Error", JOptionPane.ERROR_MESSAGE);
		   return;
               }
	       cb.setAllSpans(beamLengths);
	       for(j=0; j<cb.getNSpans(); j++)
	       {  
	           panelSelfRef.remove(spanTF[j]);
	       }
	       qNo++;
	       questionLabel.setText(question[qNo]);
	       answerTF.setText("34500");
	       answerTF.setVisible(true);
	       panelSelfRef.remove(spanSubmit);	
	       repaint();
	       return;
           }
		
           if(source == modElaSubmit)
	   {
	       int j = 0;
	       double E[] = new double[cb.getNSpans()];
	       try
	       {
	           for(j=0; j<cb.getNSpans(); j++)
		   {
		       str = modElaTF[j].getText();
		       E[j] = Math.abs(Double.parseDouble(str.trim())); 
		   }
               }
	       catch(NumberFormatException e) 
	       {
	           JOptionPane.showMessageDialog(null, 
		    	"Modulus of Elasticity of member " + (j+1) 
		    	+ " is erroneous!",
			"Error", JOptionPane.ERROR_MESSAGE);
                   return;
               }
	       cb.setAllE_MPa(E);
	       // To Do - Add relevant statements here
	       for(j=0; j<cb.getNSpans(); j++)
	       {
	           panelSelfRef.remove(modElaTF[j]);
	       }
	       qNo++;
	       questionLabel.setText(question[qNo]);
	       answerTF.setText("0.0005175");
	       answerTF.setVisible(true);
	       panelSelfRef.remove(modElaSubmit);
	       eflag = false;
	       repaint();
	       return;
           }

	   if(source == momInerSubmit)
	   {
	       int j = 0;
	       double mi[] = new double[cb.getNSpans()];
	       try
	       {
	           for(j=0; j<cb.getNSpans(); j++)
		   {
		       str = momInerTF[j].getText();
		       mi[j] = Math.abs(Double.parseDouble(str.trim())); 
                   }
               }
	       catch(NumberFormatException e) 
	       {
	           JOptionPane.showMessageDialog(null, 
		    	"Moment of Inertia of member " + (j+1) 
		    	+ " is erroneous!",
			"Error", JOptionPane.ERROR_MESSAGE);
                   return;
               }
	       cb.setAllMomIner(mi);
	       // To Do - Add relevant statements here
	       for(j=0; j<cb.getNSpans(); j++)
	       {
	           panelSelfRef.remove(momInerTF[j]);
	       }
	       qNo++;
	       questionLabel.setText(question[qNo]);
	       panelSelfRef.remove(momInerSubmit);
	       miflag = false;
	       panelSelfRef.add(box2);
	       supportFlag = true;
	       cb.calcBeamThicknesses();
	       repaint();
	       return;
           }

	   if(source == answerTF)
	   {
	       switch(qNo)
	       {
	           case 0:
		       try
		       {			  
		           str = answerTF.getText();
			   int temp1 = Integer.parseInt(str.trim());
			   if(temp1 > 0 && temp1 < 11)
			   {
			       cb.setNSpans(temp1);
			       qNo++;
			       questionLabel.setText(question[qNo]);
			       answerTF.setText("3.5");
			       repaint();
                           }
			   else
			   {
			       JOptionPane.showMessageDialog(null, 
			           "Number of beam members can have value from 1 through 10",
				   "Error", JOptionPane.WARNING_MESSAGE);  
                           }
			   return;
                       }
		       catch(NumberFormatException e) 
		       {
		           JOptionPane.showMessageDialog(null, 
			           "Number of beam members must be an integer!",
				   "Error", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
			  
                   case 1:
                       try
		       {
		           str = answerTF.getText();  
			   typicalSpan = Double.parseDouble(str.trim());
			   typicalSpan = Math.abs(typicalSpan);
                       }
		       catch(NumberFormatException e) 
		       {
		           JOptionPane.showMessageDialog(null, 
			          "Beam length must be a positive real number!",
				  "Error", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
		       cb.setTypicalSpan(typicalSpan);
		       spanTF = new JTextField[cb.getNSpans()];
		       int w = (int)(600/cb.getNSpans()) - 20;
		       for(int j=0; j<cb.getNSpans(); j++)
		       {
		           spanTF[j] = new JTextField("" + typicalSpan);
			   int x = 56 + (w+20)*j;
			   spanTF[j].setBounds(x, 270, w, 27);
			   panelSelfRef.add(spanTF[j]);
                       }
		       qNo++;
		       questionLabel.setText(question[qNo]);
		       answerTF.setVisible(false);
		       spanSubmit.setBounds(285, 310, 120, 30);
		       panelSelfRef.add(spanSubmit);
		       repaint();
		       return;
			  
                   case 3:
                       try
		       {
		           str = answerTF.getText();  
			   typicalE = Double.parseDouble(str.trim());
			   typicalE = Math.abs(typicalE);
                       }
		       catch(NumberFormatException e) 
		       {
		           JOptionPane.showMessageDialog(null, 
				"Modulus of Elasticity must be a positive real number!",
				"Error", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
		       modElaTF = new JTextField[cb.getNSpans()];
		       cb.setTypicalModEla(typicalE);
		       eflag = true;
		       int w1 = (int)(600/cb.getNSpans()) - 20;
		       for(int j=0; j<cb.getNSpans(); j++)
		       {
		           modElaTF[j] = new JTextField("" + typicalE);
			   int x = 56 + (w1+20)*j;
			   modElaTF[j].setBounds(x, 300, w1, 27);
			   panelSelfRef.add(modElaTF[j]);
                       }
		       qNo++;
		       questionLabel.setText(question[qNo]);
		       answerTF.setVisible(false);
		       modElaSubmit.setBounds(285, 340, 120, 30);
		       panelSelfRef.add(modElaSubmit);
		       repaint();
		       return;
			  
                   case 5:
                       try
		       {
		           str = answerTF.getText();  
			   typicalMI = Double.parseDouble(str.trim());
			   typicalMI = Math.abs(typicalMI);
                       }
		       catch(NumberFormatException e) 
		       {
		           JOptionPane.showMessageDialog(null, 
				"Moment of Inertia must be a positive real number!",
				"Error", JOptionPane.ERROR_MESSAGE);
                           return;
                       }
		       momInerTF = new JTextField[cb.getNSpans()];
		       cb.setTypicalMomIner(typicalMI);
		       miflag = true;
		       int w2 = (int)(600/cb.getNSpans()) - 20;
		       for(int j=0; j<cb.getNSpans(); j++)
		       {
		           momInerTF[j] = new JTextField("" + typicalMI);
			   int x = 56 + (w2+20)*j;
			   momInerTF[j].setBounds(x, 300, w2, 27);
			   panelSelfRef.add(momInerTF[j]);
                       }
		       qNo++;
		       questionLabel.setText(question[qNo]);
		       answerTF.setVisible(false);
		       momInerSubmit.setBounds(285, 340, 120, 30);
		       panelSelfRef.add(momInerSubmit);
		       repaint();
		       return;			  
               } // switch
           } // if
       } // actionPerformed() ends
   } // AnswerActionListener class ends
   
   class SupportActionListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           if(supportIndex == (short)(cb.getNJoints()))
	   {
	       supportFlag = false ;
	       panelSelfRef.remove(box2);
	       repaint();
	       frameSelfRef.setVisible(false);
	       frameSelfRef.dispose();
	       return;
           }
	   Object source = ae.getSource();
	   if(source == fixedBt)
	   {
	       cb.setJointType(supportIndex, Beam.FIXED);
	   }
	   if(source == simpleSupBt)
	   {
	       cb.setJointType(supportIndex, Beam.HINGE);
	   }
	   if(source == freeBt)
	   {
	       cb.setJointType(supportIndex, Beam.FREE);
	   }
	   if(supportIndex == 0)
	   {
	       supportIndex = (short)(cb.getNJoints() - 1);
	   }
	   else if(supportIndex == (short)(cb.getNJoints() - 1))
	   {
	       supportIndex = 1;
	       fixedBt.setEnabled(false);
	       if(cb.getNJoints() == 2)
	       {
	           supportIndex += 1;
		   supportFlag = false ;
		   simpleSupBt.setEnabled(false);
		   freeBt.setText("Exit");
               }
           }
	   else if(supportIndex == (short)(cb.getNJoints() - 2))
	   {
	       supportIndex += 2;
	       supportFlag = false ;
	       simpleSupBt.setEnabled(false);
	       freeBt.setText("Exit");
           }
	   else
	       supportIndex++;
	   repaint();
       } // actionPerformed() ends
   } // SupportActionListener class ends			
} // BeamDataFrame class ends

