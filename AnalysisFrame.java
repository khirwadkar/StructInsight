package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;


class AnalysisFrame extends JFrame
{
   ContinuousBeam cb = ContiBeamMain.cb;
   AnalysisFrame frameSelfRef;
   JPanel pane;
   AnalysisPanel panelSelfRef;
   JButton sfdButton = new JButton("S.F. Diagram");
   JButton bmdButton = new JButton("B.M. Diagram");
   JButton quitButton = new JButton("Back");
   Box box1;
   int whichDiagram = 0; // 0 = No diagram, 1 = SFD, 2 = BMD
   
   AnalysisFrame()
   {
       super("KITE - Analysis");
       frameSelfRef = this;

       cb.setAmlMatrices();
       cb.setStiffnessMatrices();
       cb.calcCumulRestraints();
       cb.setGlobalStiffMatrix();
       cb.invertGlobalStiffMatrix();
       cb.setEquiJointLoads();
       cb.calcJtDisplacements();
       cb.calcSupportReactions();
       cb.rearrangeVectors();
       cb.calcFinalMemberEndActions();
       cb.calcShearForces();
       cb.calcBendingMoments();

       if(F2U.localAddress == null)
       {
           try
	   {
	       FileWriter file1 = new FileWriter("result.data");
	       BufferedWriter buff = new BufferedWriter(file1);
	       PrintWriter out = new PrintWriter(buff);
	       cb.printResults(out);  		   
	       out.close();
	   }
	   catch(IOException e)
	   {
	       System.out.println(e);
	   }
       }
       pane = new AnalysisPanel();
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
   
   class AnalysisPanel extends JPanel // inner class
   {
       AnalysisPanel()
       {
	   setLayout(null);
	   panelSelfRef = this;

	   ActionListener quitListener =
		   new QuitListener();
	     
	   ActionListener whichDiagListener =
		   new DiagramActionListener();
	   
	   quitButton.addActionListener(quitListener);
	   sfdButton.addActionListener(whichDiagListener);
	   bmdButton.addActionListener(whichDiagListener);
	   box1 = Box.createHorizontalBox();
	   box1.setBounds(50, 520, 600, 50);
	   box1.add(Box.createGlue());
	   box1.add(sfdButton);
	   box1.add(Box.createHorizontalStrut(20));
	   box1.add(quitButton);
	   box1.add(Box.createHorizontalStrut(20));
	   box1.add(bmdButton);
	   box1.add(Box.createGlue());
	   add(box1);
       }
     
       class QuitListener implements ActionListener
       {
           public void actionPerformed(ActionEvent ae)
  	   {
	       frameSelfRef.setVisible(false);
	       frameSelfRef.dispose();
  	   }
       }
     
       class DiagramActionListener implements ActionListener
       {
           public void actionPerformed(ActionEvent ae)
	   {
	       Object source = ae.getSource();
	       if(source == sfdButton)
	           whichDiagram = 1;
	       if(source == bmdButton)
	           whichDiagram = 2;
	       frameSelfRef.repaint();
	   }
       }
     
       public void paintComponent(Graphics g)
       {
	   g.setColor(Color.BLUE);
	   g.drawLine(0, 199, 699, 199);
	   g.drawLine(0, 200, 699, 200);
	   g.drawLine(0, 201, 699, 201);
	   g.drawLine(0, 349, 699, 349);
	   g.drawLine(0, 350, 699, 350);
	   g.drawLine(0, 351, 699, 351);
		
	   double drawing_scale = 600 / cb.getTotal_length();
		
	   int nSpans = cb.getNSpans();
	   
	   // Load diagram
	   for(int i=0; i<nSpans; ++i)
               drawBeam(g, i, 100, drawing_scale);
	   g.setColor(Color.BLUE);
	   g.drawLine(45, 90, 645, 90);
	   g.drawLine(45, 100, 645, 100);
	   g.setColor(Color.BLACK);
		
	   int nJoints = cb.getNJoints();
	   for(int i=0; i<nJoints; ++i)
	   {
	       int jtType = cb.getJointType(i);
	       switch(jtType)
	       {
                   case Beam.FIXED:
                       drawFixedSupport(g, i, 100, drawing_scale);
		       break;
                   case Beam.HINGE:
                       drawVertArrow(g, i, 100, drawing_scale);
		       break;
               }
	   }
	   g.setColor(Color.RED);
	   for(int i=0; i<nJoints; ++i)
	   {
	       double temp = cb.getJtAction(2*i); 
	       if(temp != 0.0)
		   drawJtLoad(g, i, temp, 90, drawing_scale);
	   }
	   g.setColor(Color.MAGENTA);
	   for(int i=0; i<nJoints; ++i)
	   {
               double temp = cb.getJtAction(2*i+1); 
	       if(temp != 0.0)
	           drawJtMoment(g, i, temp, 90, drawing_scale);
	   }
	   g.setColor(Color.RED);
	   for(int i=0; i<nSpans; ++i)
	   {
	       double temp = cb.getMemberUDL(i); 
	       if(temp != 0.0)
	           drawMemberUDL(g, i, temp, 100, drawing_scale);	
	   }
	   g.setColor(Color.ORANGE);
	   for(int i=0; i<nSpans; ++i)
	   {
	       PointLoad temp[] = cb.getMemberPtLoads(i); 
	       if(temp.length > 0)
	           drawMemberPtLoads(g, i, temp, 90, drawing_scale);	
	   }

	   // Reaction diagram
	   for(int i=0; i<nSpans; ++i)
	       drawBeam(g, i, 265, drawing_scale);
	   g.setColor(Color.BLUE);
	   g.drawLine(45, 255, 645, 255);
	   g.drawLine(45, 265, 645, 265);
	   g.setColor(Color.RED);
	   for(int i=0; i<nJoints; ++i)
	   {
	       double temp = cb.getSupportReaction(2*i); 
	       if(temp != 0.0)
	           drawLoadReaction(g, i, temp, 265, drawing_scale);
	   }
	   g.setColor(Color.MAGENTA);
	   for(int i=0; i<nJoints; ++i)
	   {
	       double temp = cb.getSupportReaction(2*i+1); 
	       if(temp != 0.0)
	           drawMomentReaction(g, i, temp, 260, drawing_scale);
	   }		
	   
	   // SFD
	   if(whichDiagram == 1)
	   {
	       g.setColor(Color.BLUE);
	       g.drawLine(45, 430, 645, 430);
	       double maxSF = cb.getMaxSF();
	       double sfScale = 60 / maxSF;
	       for(int i=0; i<nSpans; ++i)
	           drawSFD(g, i, 430, drawing_scale, sfScale);		   
	   }
	   
	   // BMD
	   if(whichDiagram == 2)
	   {
	       g.setColor(Color.BLUE);
	       g.drawLine(45, 430, 645, 430);
	       double maxBM = cb.getMaxBM();
	       double bmScale = 60 / maxBM;
	       for(int i=0; i<nSpans; ++i)
	           drawBMD(g, i, 430, drawing_scale, bmScale);		   
	   }
	   
	   g.setColor(Color.BLACK);	  
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
       }

       void drawLoadReaction(Graphics g, int jtIndex, double value, int y, double scale)
       {
	   int x = 45 + (int)(cb.getJointPosX(jtIndex) * scale);
	   g.drawLine(x, y, x, y+50);
	   if(value < 0)
	   {
	       g.drawLine(x, y+50, x-5, y+44);
	       g.drawLine(x, y+50, x+5, y+44);
	   }
	   else
	   {
	       g.drawLine(x, y, x-5, y+6);
	       g.drawLine(x, y, x+5, y+6);
	   }
	   value = Math.abs(value);
	   String vStr;
	   if(value < 10)
	       vStr = String.format("%7.3f kN", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN", value);
	   else
	       vStr = String.format("%d kN", (int)value);
	   g.drawString(vStr, x-20, y+70);
       } // End of drawLoadReaction()
	 
       void drawMomentReaction(Graphics g, int jtIndex, double value, int y, double scale)
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
	   String vStr;
	   if(value < 10)
	       vStr = String.format("%7.3f kN-m", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN-m", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN-m", value);
	   else
	       vStr = String.format("%d kN-m", (int)value);
	   g.drawString(vStr, x-10, y-27);
       } // End of drawMomentReaction()
	 
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
	   g.setColor(Color.BLUE);
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
	   FontMetrics fm = g.getFontMetrics();
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
	 
       void drawSFD(Graphics g, int beamIndex, int y, double drScale, double sfScale)
       {
	   int xStart = 45 + (int)(cb.getJointPosX(beamIndex) * drScale);
	   int L = (int)(cb.getMemberLength(beamIndex) * drScale);
	   ArrayList <PointLoad> sf = cb.getMemberShearForces(beamIndex);
	   double p;
	   int i, n = sf.size();
	   int xPrev = xStart, yPrev = y;
	   g.setColor(Color.GREEN);
	   for(i=0; i<n; i++)
	   {
	       p = sf.get(i).getLoad();
	       double x = sf.get(i).getDist();
	       int pPixels = (int)(p * sfScale);
	       int xPos = xStart + (int)(x * drScale);
	       int yPos = y - pPixels;
	       g.drawLine(xPos, y, xPos, yPos);
	       g.drawLine(xPrev, yPrev, xPos, yPos);
	       xPrev = xPos;
	       yPrev = yPos;
	   }
	   
	   g.setColor(Color.BLACK);
	   p = sf.get(0).getLoad();
	   int pPixels = (int)(p * sfScale);
	   double value = Math.abs(p);
	   String vStr;
	   if(value < 0.001)
	       vStr = " ";
	   else if(value < 10)
	       vStr = String.format("%7.3f kN", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN", value);
	   else
	       vStr = String.format("%d kN", (int)value);
	   if(p > 0)
	       g.drawString(vStr, xStart, y-pPixels-2);
	   else
	       g.drawString(vStr, xStart, y-pPixels+15);
	   
	   p = sf.get(n-1).getLoad();
	   pPixels = (int)(p * sfScale);
	   value = Math.abs(p);
	   if(value < 0.001)
	       vStr = " ";
	   else if(value < 10)
	       vStr = String.format("%7.3f kN", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN", value);
	   else
	       vStr = String.format("%d kN", (int)value);
	   FontMetrics fm = g.getFontMetrics();
	   int xPos = xStart + L - 5 - fm.stringWidth(vStr);
	   if(p > 0)
	       g.drawString(vStr, xPos, y-pPixels-2);
	   else
	       g.drawString(vStr, xPos, y-pPixels+15);
       } // End of drawSFD()
	 
       void drawBMD(Graphics g, int beamIndex, int y, double drScale, double bmScale)
       {
	   int xStart = 45 + (int)(cb.getJointPosX(beamIndex) * drScale);
	   int L = (int)(cb.getMemberLength(beamIndex) * drScale);
	   ArrayList <PointLoad> bm = cb.getMemberBendingMoments(beamIndex);
	   double p;
	   int i, n = bm.size();
	   int xPrev = xStart, yPrev = y;
	   g.setColor(Color.GREEN);
	   for(i=0; i<n; i++)
	   {
	       p = bm.get(i).getLoad();
	       double x = bm.get(i).getDist();
	       int pPixels = (int)(p * bmScale);
	       int xPos = xStart + (int)(x * drScale);
	       int yPos = y - pPixels;
	       g.drawLine(xPos, y, xPos, yPos);
	       g.drawLine(xPrev, yPrev, xPos, yPos);
	       xPrev = xPos;
	       yPrev = yPos;
	   }
	   
	   g.setColor(Color.BLACK);
	   p = bm.get(0).getLoad();
	   int pPixels = (int)(p * bmScale);
	   double value = Math.abs(p);
	   String vStr;
	   if(value < 0.001)
	       vStr = " ";
	   else if(value < 10)
	       vStr = String.format("%7.3f kN-m", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN-m", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN-m", value);
	   else
	       vStr = String.format("%d kN-m", (int)value);
	   if(p > 0)
	       g.drawString(vStr, xStart, y-pPixels-2);
	   else
	       g.drawString(vStr, xStart, y-pPixels+15);
	   
	   p = bm.get(n-1).getLoad();
	   pPixels = (int)(p * bmScale);
	   value = Math.abs(p);
	   if(value < 0.001)
	       vStr = " ";
	   else if(value < 10)
	       vStr = String.format("%7.3f kN-m", value);
	   else if(value < 100)
	       vStr = String.format("%7.2f kN-m", value);
	   else if(value < 1000)
	       vStr = String.format("%7.1f kN-m", value);
	   else
	       vStr = String.format("%d kN-m", (int)value);
	   FontMetrics fm = g.getFontMetrics();
	   int xPos = xStart + L - 5 - fm.stringWidth(vStr);
	   if(p > 0)
	       g.drawString(vStr, xPos, y-pPixels-2);
	   else
	       g.drawString(vStr, xPos, y-pPixels+15);
       } // End of drawBMD()
	 
	 
   } // inner class AnalysisPanel ends

} // AnalysisFrame class ends



