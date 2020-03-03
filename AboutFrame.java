package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class AboutFrame extends JFrame
{
	AboutFrame frameSelfRef;
	JPanel pane;
	AboutPanel panelSelfRef;
	JButton quitButton = new JButton("OK");
	JTextArea ta;
	URL page;
	JScrollPane scroll;
	Box box1;

	AboutFrame()
	{
		super("KITE - About Continuous Beam Analysis Software");
		frameSelfRef = this;
		pane = new AboutPanel();
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

	class AboutPanel extends JPanel // inner class
	{
		JPanel p1 = new CopyrightPanel();
		JPanel p2 = new JPanel();

		AboutPanel()
		{
			setLayout(new GridLayout(2,1));
			panelSelfRef = this;

			add(p1);

			ActionListener quitListener = new QuitListener();
			quitButton.addActionListener(quitListener);

			ta = new JTextArea(10, 60);
			ta.setText("Loading ...");
			BufferedReader data = null;
			String line;
			StringBuffer buff = new StringBuffer();
			try
			{
				InputStream txt_file = getClass().getResourceAsStream("/TnC.txt");
				InputStreamReader tnc_file = new InputStreamReader(txt_file);
				data = new BufferedReader(tnc_file);
				while((line = data.readLine()) != null)
					buff.append(line + "\n");
				ta.setText(buff.toString());
				ta.setCaretPosition(0);
				ta.setEditable(false);
			}
			catch(IOException e)
			{
				System.out.println("Error while reading the file TnC.txt");
			}	  
			scroll = new JScrollPane(ta);
			p2.add(scroll);
			quitButton.setPreferredSize(new Dimension(100, 35));
			p2.add(quitButton);
			add(p2);
		}

		class QuitListener implements ActionListener
		{
			public void actionPerformed(ActionEvent ae)
			{
				frameSelfRef.setVisible(false);
				frameSelfRef.dispose();
			}
		}
	} // Inner class AboutPanel ends
} // class AboutFrame ends

class CopyrightPanel extends JPanel
{
	Font f1, f2;
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		f1 = new Font("Serif", Font.BOLD, 12);
		g.setFont(f1);
		g.drawString("StructInsight", 10, 25);
		g.drawString("(K.I.T.E.'s Continuous Beam Analysis Software)", 10, 45);
		g.drawString("(c) Copyright Sanjay G. Khirwadkar 2020", 10, 65);
		g.drawString("All rights reserved", 10, 85);
		f2 = new Font("Serif", Font.BOLD, 16);
		g.setFont(f2);
		g.drawString("Software User Agreement:", 25, 225);
	}
} // class CopyrightPanel ends
