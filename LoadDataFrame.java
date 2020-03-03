package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.*;

import java.io.*;
import java.net.*;


class LoadDataFrame extends JFrame
{
	ContinuousBeam cb = ContiBeamMain.cb;
	LoadDataFrame frameSelfRef;
	JPanel pane;
	LoadDataPanel panelSelfRef;
	Box box1, box2, box3;
	Box box4submitButtons;
	int qNo = 0;
	String question[] =
		{
			"Input joint loads, if any ...                         ",
			"Input uniformly distributed loads on members (kN/m)   ",
			"Input point loads on members, if any ...              ",
			"                                                      ",
			"Change the values, if necessary. Then click 'Submit'  ",
			"Moment of Inertia(I) of typical cross-section (m.^4)  ",
			"Change the values, if necessary. Then click 'Submit'  ",
			"Now, specify support details...                       "
		};
	JLabel questionLabel = new JLabel(question[qNo]);
	JTextField answerTF;  
	boolean jointLoadFlag = true;
	short supportIndex = 0;
	JButton previousBtn = new JButton("Previous Joint");
	JButton nextBtn = new JButton("Next Joint");

	ImageIcon up;
	ImageIcon dn;
	ImageIcon clk;
	ImageIcon antclk;
	JButton upJtLoad;
	JButton downJtLoad;
	JButton clockwiseJtMoment;
	JButton anticlockJtMoment;
	JButton jointLoadSubmit = new JButton("No more Joint Actions");

	boolean udlFlag = false;
	short memberIndex = 0;
	JButton preMemberBtn = new JButton("Previous Member");
	JButton nextMemberBtn = new JButton("  Next Member  ");
	JButton udlUp = new JButton(  " Upward udl ");
	JButton udlDown = new JButton("Downward udl");
	JButton udlSubmit = new JButton("No more U. D. Loads");

	boolean pointLoadFlag = false;
	JButton ptLoadUp = new JButton(  " Upward Pt. Load ");
	JButton ptLoadDown = new JButton("Downward Pt. Load");
	JButton pointLoadSubmit = new JButton("No more Point Loads");

	AnswerActionListener listen1 = new AnswerActionListener();
	JointLoadButtonListener listen2 = new JointLoadButtonListener();
	UdlActionListener listen3 = new UdlActionListener();
	PtLoadActionListener listen4 = new PtLoadActionListener();

	LoadDataFrame()
	{
		super("KITE - Get Load Data");
		frameSelfRef = this;

		cb.initJtActionArray();

		URL up_url = getClass().getResource("/MyIcons/up-arrow.jpg"); 
		Image image_up = Toolkit.getDefaultToolkit().getImage(up_url); 
		up = new ImageIcon(image_up);   

		URL dn_url = getClass().getResource("/MyIcons/down-arrow.jpg"); 
		Image image_dn = Toolkit.getDefaultToolkit().getImage(dn_url); 
		dn = new ImageIcon(image_dn);  

		URL clk_url = getClass().getResource("/MyIcons/clockwise-moment.jpg"); 
		Image image_clk = Toolkit.getDefaultToolkit().getImage(clk_url);
		clk = new ImageIcon(image_clk);

		URL antclk_url = getClass().getResource("/MyIcons/anticlockwise-moment.jpg");
		Image image_antclk = Toolkit.getDefaultToolkit().getImage(antclk_url); 
		antclk = new ImageIcon(image_antclk); 

		upJtLoad = new JButton("Load", up);
		downJtLoad = new JButton("Load", dn);
		clockwiseJtMoment = new JButton("Moment", clk);
		anticlockJtMoment = new JButton("Moment", antclk);

		pane = new LoadDataPanel();
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

	class LoadDataPanel extends JPanel // inner class
   {
       String str;
	 
       LoadDataPanel()
       {
           setLayout(null);
	   panelSelfRef = this;
	   box1 = Box.createHorizontalBox();
	   box1.setBounds(50, 5, 480, 30);
	   box1.add(questionLabel);
	   answerTF = new JTextField("" + cb.getNSpans());
	   answerTF.setColumns(10);

	   answerTF.addActionListener(listen1);
	   box1.add(answerTF);
	   answerTF.setVisible(false);

	   box4submitButtons = Box.createHorizontalBox();
	   box4submitButtons.setBounds(100, 360, 500, 40);
	   jointLoadSubmit.addActionListener(listen2);
	   box4submitButtons.add(Box.createGlue());
	   box4submitButtons.add(jointLoadSubmit);
	   box4submitButtons.add(Box.createGlue());

	   add(box1);
	   add(box4submitButtons);

	   box2 = Box.createHorizontalBox();
	   box2.setBounds(150, 35, 400, 35);
	   previousBtn.addActionListener(listen1);
	   nextBtn.addActionListener(listen1);
	   box2.add(Box.createGlue());
	   box2.add(previousBtn);
	   previousBtn.setEnabled(false);
	   box2.add(Box.createHorizontalStrut(10));
	   box2.add(nextBtn);
	   box2.add(Box.createGlue());
	   add(box2);		 

	   box3 = Box.createHorizontalBox();
	   box3.setBounds(50, 80, 600, 60);
	   upJtLoad.addActionListener(listen1);
	   downJtLoad.addActionListener(listen1);
	   clockwiseJtMoment.addActionListener(listen1);
	   anticlockJtMoment.addActionListener(listen1);
	   box3.add(Box.createGlue());
	   box3.add(upJtLoad);
	   box3.add(Box.createHorizontalStrut(10));
	   box3.add(downJtLoad);
	   box3.add(Box.createHorizontalStrut(10));
	   box3.add(clockwiseJtMoment);
	   box3.add(Box.createHorizontalStrut(10));
	   box3.add(anticlockJtMoment);
	   box3.add(Box.createGlue());
	   add(box3);
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
	   if(jointLoadFlag)
	   {
	       if(supportIndex == 0)
	       {
	           g.setColor(Color.GREEN);
		   g.drawLine(45, 227, 45, 215);
		   g.drawLine(45, 227, 45-4, 223);
		   g.drawLine(45, 227, 45+4, 223);
		   g.drawLine(45, 215, 45+10, 190);
		   g.setColor(Color.BLACK);
		   g.drawString("Specify loads at this joint!", 45, 185);
               }
	       else if(supportIndex == (short)(nJoints-1))
	       {
	           g.setColor(Color.GREEN);
		   g.drawLine(645, 227, 645, 215);
		   g.drawLine(645, 227, 645-4, 223);
		   g.drawLine(645, 227, 645+4, 223);
		   g.drawLine(645, 215, 645+10, 190);
		   g.setColor(Color.BLACK);
		   g.drawString("at this joint!", 605, 185);
               }
	       else
	       {
	           int x = 45 + (int)(cb.getJointPosX(supportIndex) * drawing_scale);
		   g.setColor(Color.GREEN);
		   g.drawLine(x, 232, x, 220);
		   g.drawLine(x, 232, x-4, 228);
		   g.drawLine(x, 232, x+4, 228);
		   g.drawLine(x, 220, x+10, 195);
		   g.setColor(Color.BLACK);
		   g.drawString("at this joint!", x, 188); 
               }
	       g.setColor(Color.RED);
	       for(int i=0; i<nJoints; ++i)
	       {
	           double temp = cb.getJtAction(2*i);
		   if(temp != 0.0)
		       drawJtLoad(g, i, temp, 240, drawing_scale);
               }
	       g.setColor(Color.MAGENTA);
	       for(int i=0; i<nJoints; ++i)
	       {
	           double temp = cb.getJtAction(2*i+1); 
		   if(temp != 0.0)
		       drawJtMoment(g, i, temp, 250, drawing_scale);
               }
	       g.setColor(Color.BLACK);
           } // if(jointLoadFlag)

           if(udlFlag)
	   {
	       short leftJtindex = memberIndex;	
	       int x = 45 + (int)(cb.getJointPosX(leftJtindex) * drawing_scale);
	       x = x + (int)(cb.getMemberLength(memberIndex) * drawing_scale / 2);
	       g.setColor(Color.GREEN);
	       g.drawLine(x, 232, x, 220);
	       g.drawLine(x, 232, x-4, 228);
	       g.drawLine(x, 232, x+4, 228);
	       g.drawLine(x, 220, x+10, 195);
	       g.setColor(Color.BLACK);
	       g.drawString("on member " + (memberIndex+1), x, 188);
		  
               g.setColor(Color.RED);
	       for(int i=0; i<nSpans; ++i)
	       {
	           double temp = cb.getMemberUDL(i); 
		   if(temp != 0.0)
		       drawMemberUDL(g, i, temp, 250, drawing_scale);	
               }

	       g.setColor(Color.BLACK);	
           } // if(udlFlag)		

           if(pointLoadFlag)
	   {
               short leftJtindex = memberIndex;	
	       int x = 45 + (int)(cb.getJointPosX(leftJtindex) * drawing_scale);
	       x = x + (int)(cb.getMemberLength(memberIndex) * drawing_scale / 2);
	       g.setColor(Color.GREEN);
	       g.drawLine(x, 265, x, 290);
	       g.drawLine(x, 265, x-4, 269);
	       g.drawLine(x, 265, x+4, 269);
	       g.drawLine(x, 290, x-10, 310);
	       g.setColor(Color.BLACK);
	       g.drawString("on member " + (memberIndex+1), x-25, 325);

	       g.setColor(Color.ORANGE);
	       for(int i=0; i<nSpans; ++i)
	       {
	           PointLoad temp[] = cb.getMemberPtLoads(i); 
		   if(temp.length > 0)
		       drawMemberPtLoads(g, i, temp, 240, drawing_scale);	
               }

	       g.setColor(Color.BLACK);	
           } // if(pointLoadFlag)		
       } // end of paintComponent method
	 
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
	 
       void drawJtLoad(Graphics g, int jtIndex, double value, int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(jtIndex) * scale);
	   g.drawLine(x, y, x, y-50);
	   if(value < 0)
	   {
	       g.drawLine(x, y, x-5, y-6);
	       g.drawLine(x, y, x+5, y-6);
	   }
	   else
	   {
               g.drawLine(x, y-50, x-5, y-44);
	       g.drawLine(x, y-50, x+5, y-44);
	   }
	   value = Math.abs(value);
	   if(jtIndex % 2 == 0)
	       g.drawString(""+value+" kN", x-20, y-70);
	   else
	       g.drawString(""+value+" kN", x-20, y-60);
       }
	 
       void drawJtMoment(Graphics g, int jtIndex, double value, int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(jtIndex) * scale);
	   g.drawArc(x-15, y-20, 40, 40, 70, 220);
	   if(value < 0)
	   {
	       g.drawLine(x+13, y-19, x+7, y-24);
	       g.drawLine(x+13, y-19, x+7, y-16);
	   }
	   else
	   {
	       g.drawLine(x+13, y+19, x+7, y+16);
	       g.drawLine(x+13, y+19, x+7, y+24);
	   }
	   value = Math.abs(value);
	   if(jtIndex % 2 == 0)
	       g.drawString(""+value+"kN-m", x+15, y-25);
	   else
	       g.drawString(""+value+"kN-m", x+15, y-15);
       } // End of drawJtMoment

       void drawMemberUDL(Graphics g, int memberIndex, double value, int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(memberIndex) * scale);
	   int L = (int)(cb.getMemberLength(memberIndex) * scale);
	   int n = L/6;
	   int m = (L - 6*n)/2;
	   x += m;
	   double temp = Math.abs(value);
	   String vStr;
	   if(temp < 10)
	       vStr = String.format("%7.3f kN/m", temp);
	   else if(temp < 100)
	       vStr = String.format("%7.2f kN/m", temp);
	   else if(temp < 1000)
	       vStr = String.format("%7.1f kN/m", temp);
	   else
	       vStr = String.format("%d kN/m", (int)temp);
	   if(value > 0)
	   {
	       for(int i=0; i<n; ++i)
	           g.drawArc(x+i*6, y-10-4, 6, 8, 180, -180);
	       int xc = x + L/2;
	       int yc = y - 20;
		 
               g.drawLine(xc, yc, xc-5, yc-6);
	       g.drawLine(xc, yc, xc+5, yc-6);
	       g.drawLine(xc, yc, xc, yc-50);
	       g.drawString(vStr, xc-35, yc-60);
	   }
	   if(value < 0)
	   {
               for(int i=0; i<n; ++i)
	           g.drawArc(x+i*6, y, 6, 8, 180, 180);
	       int xc = x + L/2;
	       int yc = y + 15;

	       g.drawLine(xc, yc, xc-5, yc+6);
	       g.drawLine(xc, yc, xc+5, yc+6);
	       g.drawLine(xc, yc, xc, yc+50);
	       g.drawString(vStr, xc-35, yc+65);
	   }
       } // end of drawMemberUDL()

       void drawMemberPtLoads(Graphics g, int memberIndex, PointLoad values[], int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(memberIndex) * scale);
	   int L = (int)(cb.getMemberLength(memberIndex) * scale);
	   int n = values.length;
	   g.setColor(Color.RED);
           for(int i=0; i<n; ++i)
	   {
	       int posX = x + (int)(values[i].getDist() * scale);
	       g.drawLine(posX, y, posX, y-25);
	       if(values[i].getLoad() > 0)
	       {
	           g.drawLine(posX, y, posX-4, y-5);
		   g.drawLine(posX, y, posX+4, y-5);
               }
	       else
	       {
	           g.drawLine(posX, y-25, posX-4, y-20);
		   g.drawLine(posX, y-25, posX+4, y-20);
               }
	       if(i%2 == 0)
	       {
	           g.drawString(""+values[i].getLoad(), posX-15, y-30);
	       }
               else
               {
	           g.drawString(""+values[i].getLoad(), posX-15, y-45);
	       }        	 
           }
           String s1 = "Point loads in 'kN' and their distances in 'meters' units.";
	   FontMetrics fm = g.getFontMetrics();
	   int x1 = (panelSelfRef.getWidth() - fm.stringWidth(s1)) / 2;
	   g.drawString(s1, x1, 340);
	   g.setColor(Color.BLUE);
	   for(int i=0; i<n; ++i)
	   {
               int posX = x + (int)(values[i].getDist() * scale);
	       g.drawLine(x, y+14+12*i, x, y+24+12*i);
	       g.drawLine(posX, y+14+12*i, posX, y+24+12*i);
	       int midx = (x + posX) / 2;
	       String s2 = "" + values[i].getDist();
	       int x2 = midx - fm.stringWidth(s2) / 2;
	       g.drawString(s2, x2, y+22+12*i);
           }       
       } // end of drawMemberPtLoads()	 	 
   } // inner class 'LoadDataPanel' ends
   
   class AnswerActionListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           Object source = ae.getSource();
	   String str;
	   if(source == previousBtn)
	   { 
	       supportIndex--;
	   }
	   if(source == nextBtn)
	   { 
	       supportIndex++;
	   }
	   if(source == upJtLoad)
	   {
               String str1 = "Upward load on joint # " + (supportIndex +1) + " in kN:";
	       while(true)
	       {
                   try
		   {
                       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       cb.setJtAction(2*supportIndex, tempValue);
		       break;
                   }
		   catch(NumberFormatException e)
		   {
                       JOptionPane.showMessageDialog(null, "Joint load must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
           }
           if(source == downJtLoad)
           { 
               String str1 = "Downward load on joint # " + (supportIndex +1) + " in kN:";
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       tempValue = -tempValue;
		       cb.setJtAction(2*supportIndex, tempValue);
		           break;
		   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "Joint load must be numeric!",
		               "Error", JOptionPane.ERROR_MESSAGE);
                   }
               }	
           }
	   if(source == anticlockJtMoment)
	   {
               String str1 = "Moment on joint # " + (supportIndex+1) + " in kN-m:";
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       cb.setJtAction(2*supportIndex+1, tempValue);
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "Joint moment must be numeric!",
		        	"Error", JOptionPane.ERROR_MESSAGE);
		   }
               }
           }
           if(source == clockwiseJtMoment)
	   { 
	       String str1 = "Moment on joint # " + (supportIndex+1) + " in kN-m:";
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       tempValue = -tempValue;
		       cb.setJtAction(2*supportIndex+1, tempValue);
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "Joint moment must be numeric!",
		              "Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
           }
	   if(supportIndex <= 0)
	   {
	       previousBtn.setEnabled(false);
	       supportIndex = 0;
           }
	   else if(!previousBtn.isEnabled())
	       previousBtn.setEnabled(true);
	   short nJoints = (short)cb.getNJoints();
	   if(supportIndex >= nJoints - 1)
	   {
	       nextBtn.setEnabled(false);
	       supportIndex = (short)(nJoints - 1);
           }
	   else if(!nextBtn.isEnabled())
	       nextBtn.setEnabled(true);	
	   repaint();
       }
   } // class AnswerActionListener ends
   
   class JointLoadButtonListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           Object source = ae.getSource();
	   String str;
	   if(source == jointLoadSubmit)
	   { 
	       qNo++;
	       questionLabel.setText(question[qNo]);
	       panelSelfRef.remove(box4submitButtons);
	       panelSelfRef.remove(box2);
	       panelSelfRef.remove(box3);
	       box2.removeAll();
	       box3.removeAll();
	       box4submitButtons.removeAll();
	       jointLoadFlag = false;

	       udlFlag = true;

	       preMemberBtn.addActionListener(listen3);
	       nextMemberBtn.addActionListener(listen3);
	       box2.setBounds(100, 35, 500, 35);
	       box2.add(Box.createGlue());
	       box2.add(preMemberBtn);
	       preMemberBtn.setEnabled(false);
	       box2.add(Box.createHorizontalStrut(10));
	       box2.add(nextMemberBtn);
	       box2.add(Box.createGlue());
	       panelSelfRef.add(box2);		 

	       udlUp.addActionListener(listen3);
	       udlDown.addActionListener(listen3);
	       box3.setBounds(100, 80, 500, 35);
	       box3.add(Box.createGlue());
	       box3.add(udlUp);
	       box3.add(Box.createHorizontalStrut(10));
	       box3.add(udlDown);
	       box3.add(Box.createGlue());
	       panelSelfRef.add(box3);

	       udlSubmit.addActionListener(listen3);
	       box4submitButtons.setBounds(100, 360, 500, 40);
	       box4submitButtons.add(Box.createGlue());
	       box4submitButtons.add(udlSubmit);
	       box4submitButtons.add(Box.createGlue());
	       panelSelfRef.add(box4submitButtons);

	       repaint();
	       return;
           }
       }
   } // class JointLoadButtonListener ends
   
   class UdlActionListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           Object source = ae.getSource();
	   String str;
	   if(source == udlSubmit)
	   {
	       cb.calcNumUDLs();
	       qNo++;
	       questionLabel.setText(question[qNo]);

	       panelSelfRef.remove(box4submitButtons);
	       panelSelfRef.remove(box2);
	       panelSelfRef.remove(box3);
	       box2.removeAll();
	       box3.removeAll();
	       box4submitButtons.removeAll();
	       udlFlag = false;

	       pointLoadFlag = true;

	       preMemberBtn.removeActionListener(listen3);
	       nextMemberBtn.removeActionListener(listen3);
	       preMemberBtn.addActionListener(listen4);
	       nextMemberBtn.addActionListener(listen4);
	       box2.add(Box.createGlue());
	       box2.add(preMemberBtn);
	       preMemberBtn.setEnabled(false);
	       nextMemberBtn.setEnabled(true);
	       box2.add(Box.createHorizontalStrut(10));
	       box2.add(nextMemberBtn);
	       box2.add(Box.createGlue());
	       panelSelfRef.add(box2);		 

	       ptLoadUp.addActionListener(listen4);
	       ptLoadDown.addActionListener(listen4);
	       box3.add(Box.createGlue());
	       box3.add(ptLoadUp);
	       box3.add(Box.createHorizontalStrut(10));
	       box3.add(ptLoadDown);
	       box3.add(Box.createGlue());
	       panelSelfRef.add(box3);

	       pointLoadSubmit.addActionListener(listen4);
	       box4submitButtons.setBounds(100, 360, 500, 40);
	       box4submitButtons.add(Box.createGlue());
	       box4submitButtons.add(pointLoadSubmit);
	       box4submitButtons.add(Box.createGlue());
	       panelSelfRef.add(box4submitButtons);

	       memberIndex = 0;
	       repaint();
	       return;
           } // end (source == udlSubmit)

	   if(source == preMemberBtn)
	   { 
	       memberIndex--;
	   }
	   if(source == nextMemberBtn)
	   { 
	       memberIndex++;
	   }
	   if(memberIndex <= 0)
	   {
	       preMemberBtn.setEnabled(false);
	       memberIndex = 0;
           }
	   else if(!preMemberBtn.isEnabled())
	       preMemberBtn.setEnabled(true);
		
           short nSpans = (short)cb.getNSpans();
	   if(memberIndex >= nSpans - 1)
	   {
	       nextMemberBtn.setEnabled(false);
	       memberIndex = (short)(nSpans - 1);
	   }
	   else if(!nextMemberBtn.isEnabled())
	       nextMemberBtn.setEnabled(true);

	   if(source == udlUp)
	   { 
	       String str1 = "Upward uniformly distributed load on member # " + (memberIndex + 1) + " in kN/m:";
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       if(tempValue > 0)
		           tempValue = -tempValue; // Note: upward udl is considered negative
		       cb.setMemberUDL(memberIndex, tempValue);
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "The U.D.L. must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
           } // end of (source == udlUp) 
	   if(source == udlDown)
	   {
	       String str1 = "Downward uniformly distributed load on member # " + (memberIndex + 1) + " in kN/m:";
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       double tempValue = Math.abs(Double.parseDouble(str.trim()));
		       cb.setMemberUDL(memberIndex, tempValue);
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "The U.D.L. must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
           } // end of (source == udlUp) 
		
           repaint();
       }
   } // class UdlActionListener ends
   		
   class PtLoadActionListener implements ActionListener
   {
       public void actionPerformed(ActionEvent ae)
       {
           Object source = ae.getSource();
	   String str;
	   if(source == pointLoadSubmit)
	   {
	       frameSelfRef.setVisible(false);
	       frameSelfRef.dispose();
	       repaint();
	       return;
           } // end (source == pointLoadSubmit)
		
           if(source == preMemberBtn)
	   { 
	       memberIndex--;
	   }
	   if(source == nextMemberBtn)
	   { 
	       memberIndex++;
	   }
	   if((source == preMemberBtn || source == nextMemberBtn) &&
				cb.getNumMemberPtLoads(memberIndex) > 0)
           {
               int confirmation =
	       JOptionPane.showConfirmDialog(null, 
	             "Remove all point loads on this member?",
		     "Click 'Yes' or 'No'", JOptionPane.YES_NO_OPTION);
               if(confirmation == JOptionPane.YES_OPTION)
	       cb.removeAllMemberPtLoads(memberIndex);
           }
	   if(memberIndex <= 0)
	   {
	       preMemberBtn.setEnabled(false);
	       memberIndex = 0;
	   }
	   else if(!preMemberBtn.isEnabled())
	       preMemberBtn.setEnabled(true);

	   short nSpans = (short)cb.getNSpans();
	   if(memberIndex >= nSpans - 1)
	   {
	       nextMemberBtn.setEnabled(false);
	       memberIndex = (short)(nSpans - 1);
           }
	   else if(!nextMemberBtn.isEnabled())
               nextMemberBtn.setEnabled(true);

           if(cb.getNumMemberPtLoads(memberIndex) >= 5)
	   {
               JOptionPane.showMessageDialog(null, 
			"At present, maximum 5 point loads are allowed per member!"
	        	+ "\nMove to another member.",
			"Attention", JOptionPane.ERROR_MESSAGE);
               return;
           }		
           if(source == ptLoadUp)
	   { 
	       String str1 = "Upward point load on member # " + (memberIndex + 1) + " in kN:";
	       double tempP=0, tempX=0;
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       tempP = Math.abs(Double.parseDouble(str.trim()));
		       tempP = -tempP; // Note: upward point load is considered negative
		       break;
                   }
		   catch(NumberFormatException e)
		   {
                       JOptionPane.showMessageDialog(null, "The point load must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
	       str1 = "Distance in meters, of " + tempP + " kN load from left end of member # " + (memberIndex + 1);
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       tempX = Math.abs(Double.parseDouble(str.trim()));
		       if(tempX >= cb.getMemberLength(memberIndex))
		       {
     			   JOptionPane.showMessageDialog(null, 
     				"The distance can not be more than length of this member ("
     				+ cb.getMemberLength(memberIndex) + " m.)!",
     				"Error", JOptionPane.ERROR_MESSAGE);
     			   continue;
     		       }
                       if(tempX <= 0.000001)
                       {
     			   JOptionPane.showMessageDialog(null, 
     				"The position can not be on the left end of this member.",
     				"Error", JOptionPane.ERROR_MESSAGE);
     			   continue;
     		       }
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "The distance must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
	       if(Math.abs(tempP) > 0.0)
	       {
	           PointLoad temp = new PointLoad(tempP, tempX);
		   cb.addMemberPointLoad(memberIndex, temp);
               }
           } // end of (source == ptLoadUp)		

           if(source == ptLoadDown)
	   { 
	       String str1 = "Downward point load on member # " + (memberIndex + 1) + " in kN:";
	       double tempP=0, tempX=0;
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
                       tempP = Math.abs(Double.parseDouble(str.trim()));
		       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "The point load must be numeric!",
		            "Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
               str1 = "Distance in meters, of " + tempP + " kN load from left end of member # " + (memberIndex + 1);
	       while(true)
	       {
	           try
		   {
		       str = JOptionPane.showInputDialog(str1, "0.0");
		       if(str==null)
		           str = "0.0";
		       tempX = Math.abs(Double.parseDouble(str.trim()));
		       if(tempX >= cb.getMemberLength(memberIndex))
		       {
		           JOptionPane.showMessageDialog(null, 
     				"The distance can not be more than length of this member ("
     				+ cb.getMemberLength(memberIndex) + " m.)!",
     				"Error", JOptionPane.ERROR_MESSAGE);
     			   continue;
                       }
		       if(tempX <= 0.000001)
		       {
		           JOptionPane.showMessageDialog(null, 
     				"The position can not be on the left end of this member.",
     				"Error", JOptionPane.ERROR_MESSAGE);
     			   continue;
                       }
                       break;
                   }
		   catch(NumberFormatException e)
		   {
		       JOptionPane.showMessageDialog(null, "The distance must be numeric!",
				"Error", JOptionPane.ERROR_MESSAGE);
                   }
               }
	       if(Math.abs(tempP) > 0.0)
	       {
	           PointLoad temp = new PointLoad(tempP, tempX);
		   cb.addMemberPointLoad(memberIndex, temp);
               }
           } // end of (source == ptLoadDown)
	   repaint();
       }
   } // class PtLoadActionListener ends
} // LoadDataFrame class ends
