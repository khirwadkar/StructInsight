package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import kitematrixutil.Matrix;

public class ContiBeamMain
{
	static ContinuousBeam cb;
	static int nArgs = 0;

	public static void displayMenu(String[] items)
	{
		cb = new ContinuousBeam();
		cb.setNSpans(5);
		nArgs = items.length;
		JFrame mainFrame = new JFrame("KITE - Continuous Beam Analysis");
		JPanel mainPanel = new MainFramePanel(mainFrame);
		mainFrame.setContentPane(mainPanel);
		if(nArgs == 0)
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setBounds(50, 50, 700, 500);
		mainFrame.setVisible(true);
	}

	static void getBeamData()
	{
		JFrame beamDataFrame = new BeamDataFrame();
		beamDataFrame.setBounds(50, 50, 700, 500);
		beamDataFrame.setVisible(true);
	}

	static void getLoadData()
	{
		JFrame loadDataFrame = new LoadDataFrame();
		loadDataFrame.setBounds(50, 50, 700, 500);
		loadDataFrame.setVisible(true);		
	}

	static void analysis()
	{
		JFrame analysisFrame = new AnalysisFrame();
		analysisFrame.setBounds(50, 0, 700, 600);
		analysisFrame.setVisible(true);		
	}

	static void tellAbout()
	{
		JFrame aboutFrame = new AboutFrame();
		aboutFrame.setBounds(50, 50, 700, 500);
		aboutFrame.setVisible(true);
	}
}

class F2U
{
	public static URL codebase;
	public static InetAddress localAddress = null;
	public static Image up, dn, clk, antclk;
	public static URL getURL(String file)
	{
		URL file_url = null;
		try
		{
			file_url = new URL(codebase.getProtocol(),
						codebase.getHost(),
						codebase.getFile() + file);
		}
		catch(Exception e)
		{
			System.out.println("Something wrong with URL");
			System.out.println(e);
		}
		return file_url;
	}
}

class MainFramePanel extends JPanel
{
	JButton b1, b2, b3, b4, b5;
	Box box;
	JFrame mainFrame;
	// JPanel mainPanelSelfRef;

	public MainFramePanel(JFrame mFrame)
	{
		mainFrame = mFrame;
		setLayout(new GridLayout(5, 1, 50, 10));
		b1 = new JButton(" Input Beam Data");
		b2 = new JButton(" Input Load Data");
		b3 = new JButton("Analyse The Beam");
		b4 = new JButton("      About     ");
		b5 = new JButton("      Exit      ");
		MyActionListener listen = new MyActionListener();
		b1.addActionListener(listen);
		b2.addActionListener(listen);
		b3.addActionListener(listen);
		b4.addActionListener(listen);
		b5.addActionListener(listen);
		add(b1);
		add(b2);
		add(b3);
		add(b4);
		add(b5);
	}

	public Insets getInsets()
	{
		return new Insets(100, 200, 100, 200);
	}

	class MyActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			Object source = ae.getSource();
			if(source == b1)
				ContiBeamMain.getBeamData();
			else if(source == b2)
				ContiBeamMain.getLoadData();
			else if(source == b3)
				ContiBeamMain.analysis();
			else if(source == b4)
				ContiBeamMain.tellAbout();
			else if(source == b5)
			{
				mainFrame.setVisible(false);
				mainFrame.dispose();
				if(ContiBeamMain.nArgs == 0)
					System.exit(0);  // Otherwise return to opening frame.
			}
		}
	}
}



