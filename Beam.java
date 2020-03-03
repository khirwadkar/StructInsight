package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import kitematrixutil.Matrix;

class Beam
{
	static final int FIXED=1, HINGE=2, FREE=3;	
	int id;
	double x_coord_left;
	private double length, E_MPa, E, I;
	private double EI; // kN-sq.m.
	int thickness; // thickness in drawing, pixel unit
	int supportLeft, supportRight;
	private ArrayList <PointLoad> pLoads = new ArrayList<PointLoad>();  
	private ArrayList <UdLoad> udLoads = new ArrayList<UdLoad>();
	private double udl; // temporary modification 
                        // ultimately 'udLoads' to be considered
	double sm[][];
	double scm1, scm2, scm3;
	double aml[]; // Actions  at ends of restrained member,
                  // in direction of member axes, due to loads
	double jt_displacement[] = new double[4];
	private ArrayList <PointLoad> shearForces;
	private ArrayList <PointLoad> bendingMoments;

	Beam()
	{
		length = 3.5; // in meters
		E_MPa = 34500;  // in Mega Pascals, concrete
		E = E_MPa * 1000; // in KN per sq.m.
		I = 0.0005175; // in m^4, 230 x 300 beam
		thickness = 1; // pixel
	}
  
	void setLength(double l)
	{
		length = l;
	}
  
	double getLength()
	{
		return length;
	}
  
	void setE_MPa(double E_MPa)
	{
		this.E_MPa = E_MPa;
		E = E_MPa * 1000;
	}
  
	void setI(double mi)
	{
		I = mi;
		EI = E * I;
	}
  
	double getEI()
	{
		return EI;
	}
  
	void setThickness(int t)
	{
		thickness = t;
	}
  
	int getThickness()
	{
		return thickness;
	}
  
	void setUdl(double udlValue)
	{
		udl = udlValue;
	}
  
	double getUdl()
	{
		return udl; 
	}
  
	void addPointLoad(PointLoad P)
	{
		pLoads.add(P);
		Collections.sort(pLoads);
	}
  
	void removeAllPtLoads()
	{
		pLoads = new ArrayList<PointLoad>();
	}
  
	ArrayList<PointLoad> getPointLoads()
	{
		return pLoads;
	}
  
	void sortPointLoads()
	{
		Collections.sort(pLoads);
	}
  
	void calc_member_stiffness_matrix()
	{
		sm = new double[4][4];
		scm1 = 4 * E * I / length;
		scm2 = 1.5 * scm1 / length;
		scm3 = 2 * scm2 / length;
		sm[0][0] = sm[2][2] = scm3;
		sm[0][2] = sm[2][0] = -scm3;
		sm[0][1] = sm[1][0] = sm[0][3] = sm[3][0] = scm2;
		sm[1][2] = sm[2][1] = sm[2][3] = sm[3][2] = -scm2;
		sm[1][1] = sm[3][3] = scm1;
		sm[1][3] = sm[3][1] = scm1/2;
	}
  
	void calc_aml_matrix()
	{
		aml = new double[4]; 
		double w = udl;
		double L = length;
		if(Math.abs(w) > 0.0)
		{
			aml[0] += (w * L / 2);
			aml[1] += (w * L * L / 12);
			aml[2] += (w * L / 2);
			aml[3] += (-w * L * L / 12);
		}

		int nPtLoads = pLoads.size();
		for(int k=0; k<nPtLoads; k++)
		{
			double P = pLoads.get(k).getLoad();
			double a, b;
			a = pLoads.get(k).getDist();
			b = L - a;
			aml[0] += (P * b * b * (3*a + b) / (L * L * L));
			aml[1] += (P * a * b * b / (L * L));
			aml[2] += (P * a * a * (a + 3*b) / (L * L * L));
			aml[3] += (-P * a * a * b / (L * L));
		}     
	}
  
	void setJtDisplacements(double a[])
	{
		jt_displacement = a;
	}
  
	void calcFinalAml()
	{
		int j1=0, j2=1, k1=2, k2=3;
		aml[0] = aml[0] + scm3*(jt_displacement[j1] - jt_displacement[k1]) 
				+ scm2*(jt_displacement[j2] + jt_displacement[k2]);
		aml[1] = aml[1] + scm2*(jt_displacement[j1] - jt_displacement[k1]) 
				+ scm1*(jt_displacement[j2] + jt_displacement[k2]/2);
		aml[2] = aml[2] - scm3*(jt_displacement[j1] - jt_displacement[k1]) 
				- scm2*(jt_displacement[j2] + jt_displacement[k2]);
		aml[3] = aml[3] + scm2*(jt_displacement[j1] - jt_displacement[k1]) 
				+ scm1*(jt_displacement[j2]/2 + jt_displacement[k2]);
	}

	void calcShearForces(double drawing_scale)
	{
		shearForces  = new ArrayList<PointLoad>();  
		double w = udl;
		double x = 0, L = length;
		int i, j;
		int n = pLoads.size();
		int beam_length_in_pixels = (int)(L * drawing_scale);
		int nGaps = beam_length_in_pixels / 5;
		double dx_in_meters = L / nGaps;
		PointLoad firstSF = new PointLoad(aml[0], x);
		shearForces.add(firstSF);
		// Changes due to udl & point loads -->
		for(i=1; i<nGaps; ++i)
		{
			x += dx_in_meters;
			double sf_at_x = aml[0] - w * x;
			for(j=0; j<n; j++)
			{
				PointLoad jthPtLoad = pLoads.get(j);
				if(jthPtLoad.getDist() <= x)
				{
					sf_at_x -= jthPtLoad.getLoad();
				}
			}
			PointLoad nextSF = new PointLoad(sf_at_x, x);
			shearForces.add(nextSF);
		}
		double temp = aml[0]-w*L;
		for(j=0; j<n; j++)
		{
			PointLoad jthPtLoad = pLoads.get(j);
			temp -= jthPtLoad.getLoad();
		}
		PointLoad lastSF = new PointLoad(temp, L);
		shearForces.add(lastSF);
	} // End of calcShearForces(double drawing_scale) method

	ArrayList<PointLoad> getShearForces()
	{
		return shearForces;
	}

	double getMaxSF()
	{
		double maxSF = 0;
		int i, n = shearForces.size();
		for(i=0; i<n; ++i)
		{
			if(Math.abs(shearForces.get(i).getLoad()) > maxSF)
				maxSF = Math.abs(shearForces.get(i).getLoad()); 
		}
		return maxSF;
	}

	void calcBendingMoments(double drawing_scale)
	{
		bendingMoments  = new ArrayList<PointLoad>();  
		double w = udl;
		double x = 0, L = length;
		int i, j;
		int n = pLoads.size();
		int beam_length_in_pixels = (int)(L * drawing_scale);
		int nGaps = beam_length_in_pixels / 5;
		double dx_in_meters = L / nGaps;
		PointLoad firstBM = new PointLoad(aml[1], x);
		bendingMoments.add(firstBM);
		// Changes due to udl & point loads -->
		for(i=1; i<nGaps; ++i)
		{
			x += dx_in_meters;
			double bm_at_x = aml[1] - aml[0] * x + w * x * x / 2;
			for(j=0; j<n; j++)
			{
				PointLoad jthPtLoad = pLoads.get(j);
				double px = jthPtLoad.getDist(); 
				if(px <= x)
				{
					bm_at_x += jthPtLoad.getLoad() * (x - px);
				}
			}
			PointLoad nextBM = new PointLoad(bm_at_x, x);
			bendingMoments.add(nextBM);
		}
		double temp = aml[1] - aml[0]*L + w*L*L/2;
		for(j=0; j<n; j++)
		{
			PointLoad jthPtLoad = pLoads.get(j);
			temp += jthPtLoad.getLoad() * (L - jthPtLoad.getDist());
		}
		PointLoad lastBM = new PointLoad(temp, L);
		bendingMoments.add(lastBM);
	} // End of calcBendingMoments(double drawing_scale) method

	ArrayList<PointLoad> getBendingMoments()
	{
		return bendingMoments;
	}
  
	double getMaxBM()
	{
		double maxBM = 0;
		int i, n = bendingMoments.size();
		for(i=0; i<n; ++i)
		{
			if(Math.abs(bendingMoments.get(i).getLoad()) > maxBM)
				maxBM = Math.abs(bendingMoments.get(i).getLoad()); 
		}
		return maxBM;
	}

	void printResults(PrintWriter out, int k)
	{
		// 'k' is member index
		out.println(""+ (k+1) 
					+ "\t" + String.format("%10.3f", aml[0])
					+ "\t" + String.format("%10.3f", aml[1])
					+ "\t" + String.format("%10.3f", aml[2])
					+ "\t" + String.format("%10.3f", aml[3])
				   );
	}
} // class Beam ends



