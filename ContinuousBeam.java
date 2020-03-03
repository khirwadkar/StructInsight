package structinsight2020.contibeam;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.net.*;
import kitematrixutil.Matrix;

class ContinuousBeam
{
	private int nSpans; // No. of members
	private int nJoints; // = nSpans + 1
	private int nRestraints = 0; // Total number of support restraints
	                            // against translation and rotation
	private int nDOF; // No. of actual displacements 
	                 // or degrees of freedom = 2 * nJoints - nRestraints
	private int nrj; // No. of restrained joints
	private double jointPosX[]; // X position of joint from left-end in meters
	private int rL[] = new int[22]; // Joint Restraint List
	private int cRL[]; // = new int[22]; // Cumulative Restraint List
	
	private int nLoadedjts = 0;
	private int nLoadedmembers = 0;
	private double jtAction[]; // = new double[22]; // Actions (loads) applied at joints, in direction
                                      // of structure axes. Two per jt.
	private int nUDL = 0;
	private double S[][]; // Global or Overall Stiffness matrix
             //  = new double[22][22]   Assumption: Maximum 10 members
	private double inv_S[][]; // Inverse of nDOF x nDOF part
                              // of Global or Overall Stiffness matrix
	private double equi_jt_load[]; //= new double[22];  // Equivalent jt. loads (structure axes). Two per jt.
	private double combined_jt_load[]; //= new double[22];  // Combined jt. loads (structure axes). Two per jt.
	private double jt_displacement[]; //= new double[22];  // Jt. Displacements (Structure axes)
	private double support_reactions[]; //= new double[22];  // Support reactions (Structure axes).

	private Beam beamNum[];
	private double total_length = 0.0;
	
	void setNSpans(int n)
	{
		nSpans = n;
		beamNum = new Beam[nSpans];
		total_length = 0.0;
		for(int i=0; i<nSpans; i++)
		{
			beamNum[i] = new Beam();
		}
		initJoints();
		jointPosX = new double[nJoints];
		calcJointPosX();
	}
	
	int getNSpans()
	{
		return nSpans;
	}
	
	void setTypicalSpan(double len)
	{
		for(int i=0; i<nSpans; ++i)
			beamNum[i].setLength(len);
		calcJointPosX();
	}
	
	double getMemberLength(int beamIndex)
	{
		return beamNum[beamIndex].getLength();
	}
	double [] getAllSpans()
	{
		double len[] = new double[nSpans];
		for(int i=0; i<nSpans; i++)
		{	
			len[i] = beamNum[i].getLength();
		}
		return len;
	}
	
	void setAllSpans(double len[])
	{
		for(int i=0; i<nSpans; i++)
		{	
			beamNum[i].setLength(len[i]);	
		}
		calcJointPosX();
	}
	
	double getTotal_length()
	{
		return total_length;
	}
	
	void setTypicalModEla(double ModE_MPa)
	{
		for(int i=0; i<nSpans; ++i)
			beamNum[i].setE_MPa(ModE_MPa);
	}
	
	void setAllE_MPa(double E_MPa[])
	{
		for(int i=0; i<nSpans; i++)
		{
			beamNum[i].setE_MPa(E_MPa[i]);
		}
	}
	
	void setTypicalMomIner(double mi)
	{
		for(int i=0; i<nSpans; ++i)
			beamNum[i].setI(mi);
	}
	
	void setAllMomIner(double mi[])
	{
		for(int i=0; i<nSpans; i++)
		{
			beamNum[i].setI(mi[i]);	
		}
	}
	
	private void initJoints() // called from setNSpans(int)
	{
		nJoints = nSpans + 1;
		nrj = nJoints;
		nRestraints = 0;
		// The following loop is redundant, as the values are
		// getting set in setJointType()
		for(int k=0; k<nJoints; ++k)
		{
			rL[2*k] = 1;
			rL[2*k+1] = 0;
		}
	}
	
	int getNJoints()
	{
		return nJoints;
	}
	
	int getJointType(int jtIndex)
	{
		if(rL[2*jtIndex]==1 && rL[2*jtIndex+1]==1)
			return Beam.FIXED;
		else if(rL[2*jtIndex]==1 && rL[2*jtIndex+1]==0)
			return Beam.HINGE;
		else
			return Beam.FREE;
	}
	
	void setJointType(int jtIndex, int jtType)
	{
		if(jtType == Beam.FIXED)
		{
			rL[2*jtIndex] = 1; 
			rL[2*jtIndex+1] = 1;
			nRestraints += 2;
		}
		if(jtType == Beam.HINGE)
		{
			rL[2*jtIndex] = 1; 
			rL[2*jtIndex+1] = 0;
			nRestraints++;
		}
		if(jtType == Beam.FREE)
		{
			rL[2*jtIndex] = 0; 
			rL[2*jtIndex+1] = 0;
			nrj--;
		}
	}
	
	private void calcJointPosX()
	{
		jointPosX[0] = 0;
		for(int i=1; i<nJoints; ++i)
		{
			jointPosX[i] = jointPosX[i-1] + beamNum[i-1].getLength(); 
		}
		total_length = jointPosX[nJoints-1];
	}
	
	double getJointPosX(int jtIndex)
	{
		return jointPosX[jtIndex];
	}
	
	void calcCumulRestraints()
	{
		nDOF = 2*nJoints - nRestraints;
		cRL = new int[2*nJoints];
		cRL[0] = rL[0];
		for(int k=1; k<2*nJoints; k++)
		{
			cRL[k] = cRL[k-1] + rL[k];
		}
	}
	
	void calcNumUDLs()
	{
		nUDL = 0;
		for(int k=0; k<nSpans; k++)
			if(Math.abs(beamNum[k].getUdl()) > 0.0)
				++nUDL;
	}
	
	void calcBeamThicknesses()
	{
		HashSet<Double> ei_set = new HashSet<Double>();
		Double ei[] = new Double[nSpans]; 
		int i, m, n;
		for(i=0; i<nSpans; ++i)
		{
			ei[i] = beamNum[i].getEI();
			ei_set.add(ei[i]);
		}
		Double ei_set_array[] = new Double[ei_set.size()];
		ei_set_array = ei_set.toArray(ei_set_array);
		Arrays.sort(ei_set_array);
		if(ei_set_array.length == 1)
			return;
		n = 10 / ei_set_array.length;
		for(i=0; i<nSpans; ++i)
		{
			m = Arrays.binarySearch(ei_set_array, ei[i]);
			beamNum[i].setThickness((m+1) * n);
		}
	}
	
	int getBeamThickness(int beamIndex)
	{
		return beamNum[beamIndex].getThickness();
	}
	
	void initJtActionArray()
	{
		jtAction = new double[2*nJoints];
	}
	
	void setJtAction(int jointIndex, double value)
	{
		jtAction[jointIndex] = value;
	}
	
	double getJtAction(int jointIndex)
	{
		return jtAction[jointIndex];
	}
	
	void setMemberUDL(int memberIndex, double value)
	{
		beamNum[memberIndex].setUdl(value);
	}
	
	double getMemberUDL(int memberIndex)
	{
		return beamNum[memberIndex].getUdl();
	}
	
	void addMemberPointLoad(int memberIndex, PointLoad p)
	{
		beamNum[memberIndex].addPointLoad(p);
	}
	
	void removeAllMemberPtLoads(int memberIndex)
	{
		beamNum[memberIndex].removeAllPtLoads();
	}
	
	PointLoad [] getMemberPtLoads(int memberIndex)
	{
		ArrayList<PointLoad> pLoad = beamNum[memberIndex].getPointLoads();
		int nPtLoads = pLoad.size();
		PointLoad b[] = new PointLoad[nPtLoads];
		pLoad.toArray(b);
		return b;
	}
	
	int getNumMemberPtLoads(int memberIndex)
	{
		ArrayList<PointLoad> pLoad = beamNum[memberIndex].getPointLoads();
		int nPtLoads = pLoad.size();
		return nPtLoads;		
	}
	
	void setAmlMatrices()
	{
		int i;
		for(i=0; i<nSpans; i++)
		{
			beamNum[i].calc_aml_matrix();
		}	   
	}
	
	void setStiffnessMatrices()
	{
		int i;
		for(i=0; i<nSpans; i++)
		{
			beamNum[i].calc_member_stiffness_matrix();
		}	   
	}
	
	void  setGlobalStiffMatrix()
	{
		int i, j, k, j1, j2, k1, k2;
		S = new double[2*nJoints][2*nJoints];
		for(i=0; i<nSpans; ++i)
		{
			j1 = 2*i; j2 = 2*i+1; k1 = 2*i+2; k2 = 2*i+3;

			if(rL[j1] == 0)  // The joint is not restrained in Y dir.
				j1 = j1 - cRL[j1]; // Push the row in Overall stiff. mat. towards beginning
			else
				j1 = nDOF + cRL[j1] - 1; // Push it after 'n' rows

			if(rL[j2] == 0)  // The joint is not restrained in Z dir.
				j2 = j2 - cRL[j2]; 
			else
				j2 = nDOF + cRL[j2] - 1;// 1 is subtracted because CRL[0] is 1.

			// The same thing for right end of the member
			if(rL[k1] == 0)  // The joint is not restrained in Y dir.
				k1 = k1 - cRL[k1]; // Push the row in Overall stiff. mat. towards beginning
			else
				k1 = nDOF + cRL[k1] - 1; // Push it after 'n' rows

			if(rL[k2] == 0)  // The joint is not restrained in Z dir.
				k2 = k2 - cRL[k2]; 
			else
				k2 = nDOF + cRL[k2] - 1;// 1 is subtracted because CRL[0] is 1.

			// Building overall (global) stiffness matrix
			if(rL[2*i] == 0)
			{
				S[j1][j1] += beamNum[i].sm[0][0];  // First Column
				S[j2][j1] += beamNum[i].sm[1][0];  // of member stiffness
				S[k1][j1] +=  beamNum[i].sm[2][0];
				S[k2][j1] +=  beamNum[i].sm[3][0];
			}
			if(rL[2*i+1] == 0)
			{
				S[j1][j2] += beamNum[i].sm[0][1];  // Second Column
				S[j2][j2] += beamNum[i].sm[1][1];
				S[k1][j2] +=  beamNum[i].sm[2][1];
				S[k2][j2] +=  beamNum[i].sm[3][1];
			}
			if(rL[2*i+2] == 0)
			{
				S[j1][k1] +=  beamNum[i].sm[0][2];  // Third Column
				S[j2][k1] +=  beamNum[i].sm[1][2];
				S[k1][k1] += beamNum[i].sm[2][2];
				S[k2][k1] += beamNum[i].sm[3][2];
			}
			if(rL[2*i+3] == 0)
			{
				S[j1][k2] +=  beamNum[i].sm[0][3];  // Fourth Column
				S[j2][k2] +=  beamNum[i].sm[1][3];
				S[k1][k2] += beamNum[i].sm[2][3];
				S[k2][k2] += beamNum[i].sm[3][3];
			}
		}
	}
	
	void invertGlobalStiffMatrix()
	{
		// Inverting the nXn part of the overall stiff. mat
		// where n means cb.nDOF in our program
		inv_S = Matrix.inverse_matrix(S, nDOF);
	}
	
	void setEquiJointLoads()
	{
		int i, j, k;
		int n = 2*nJoints; // 2*nj = n+nr
		// Member loads to Equivalent joint loads
		equi_jt_load = new double[n];
		for(i=0; i<nSpans; i++)
		{
			// beamNum[i].calc_aml_matrix();
			equi_jt_load[2*i] -= beamNum[i].aml[0];
			equi_jt_load[2*i+1] -= beamNum[i].aml[1];
			equi_jt_load[2*i+2] -= beamNum[i].aml[2];
			equi_jt_load[2*i+3] -= beamNum[i].aml[3];
		}	   

		// Combined joint loads (arranged like Overall stiff. mat.)
		combined_jt_load = new double[n];
		for(j=0; j<n; j++) // 2*nj = n+nr
		{
			if(rL[j] == 0) 
				k = j - cRL[j];
			else
				k = nDOF + cRL[j] - 1; // As CRL[0] is 1.
			combined_jt_load[k] = jtAction[j] + equi_jt_load[j];
		}      
	}
	
	void calcJtDisplacements()
	{
		// Joint Displacements Calculation
		int n = 2*nJoints; // 2*nj = n+nr	
		jt_displacement = new double[n];
		for(int j=0; j<nDOF; ++j)
		{
			for(int k=0; k<nDOF; ++k)
				jt_displacement[j] = jt_displacement[j] 
	                           + inv_S[j][k] * combined_jt_load[k];
		}
	}
	
	void calcSupportReactions()
	{
		// Calculation of support reactions
		int n = 2*nJoints; // 2*nj = n+nr	
		support_reactions = new double[n];
		for(int k=nDOF; k<nDOF+nRestraints; k++)
		{
			support_reactions[k] = -combined_jt_load[k];
			for(int j=0; j<nDOF; ++j)
			{
				support_reactions[k] = support_reactions[k] 
	                               + S[k][j] * jt_displacement[j];
			}
		}
	}
	
	void rearrangeVectors()
	{
		// Rearranging displacement vector according to original jt. numbering system
		int j = nDOF;
		int last = nDOF + nRestraints - 1; 
		for(int JE=last; JE>=0; JE--) // JE & KE are indices for expanded vectors
		{
			if(rL[JE] == 0)
			{
				j--;
				jt_displacement[JE] = jt_displacement[j];
			}
			else
				jt_displacement[JE] = 0;
		}                                                                                                         
		// Rearranging AR vector according to ori. jt. numbering system
		int k = nDOF - 1;
		last = nDOF + nRestraints;
		for(int KE=0; KE<last;  KE++)
		{
			if(rL[KE] == 1)
			{
				k++;
				support_reactions[KE] = support_reactions[k];
			}
			else
				support_reactions[KE] = 0;
		}

		// Copy displacements to members
		for(k=0; k<nSpans; ++k)
		{
			double a[] = new double[4];
			a[0] = jt_displacement[2*k];
			a[1] = jt_displacement[2*k+1];
			a[2] = jt_displacement[2*k+2];
			a[3] = jt_displacement[2*k+3];
			beamNum[k].setJtDisplacements(a);
		}
	}
	
    void calcFinalMemberEndActions()
    {
		for(int k=0; k<nSpans; ++k)
		{
			beamNum[k].calcFinalAml();
		}    	
    }
 
    double getSupportReaction(int reactionIndex)
    {
		return support_reactions[reactionIndex];
    }
    
    void printResults(PrintWriter out)
    {
		out.println("Joint    Y-Displ      Z-displ      Y-react     Z-react");
		for(int k=0; k<2*nJoints; k+=2)
		{
			out.println(""+ (k/2+1) 
		   		      + "\t" + String.format("%11.4f", jt_displacement[k]*1000)
		   		      + "\t" + String.format("%11.4f", jt_displacement[k+1]*1000)
		   		      + "\t" + String.format("%10.3f", support_reactions[k])
		   		      + "\t" + String.format("%10.3f", support_reactions[k+1])
		   		   );
			   
		}
		out.println();

		out.println("Member     SF-Left      BM-Left      SF-Right      BM-Right");
		for(int k=0; k<nSpans; ++k)
		{
			beamNum[k].printResults(out, k);
		}
		out.println();
    }
    
    void calcShearForces()
    {
		double drawing_scale = 600 / total_length;
		for(int k=0; k<nSpans; ++k)
		{
			beamNum[k].calcShearForces(drawing_scale);
		} 
    }
    
    ArrayList<PointLoad> getMemberShearForces(int memberIndex)
    {
		return beamNum[memberIndex].getShearForces();	
    }
    
    double getMaxSF()
    {
		double maxSF = 0;
		for(int k=0; k<nSpans; ++k)
		{
			if(beamNum[k].getMaxSF() > maxSF)
				maxSF = beamNum[k].getMaxSF();
		}
		return maxSF;
    }
    
    void calcBendingMoments()
    {
		double drawing_scale = 600 / total_length;
		for(int k=0; k<nSpans; ++k)
		{
			beamNum[k].calcBendingMoments(drawing_scale);
		} 
    }
    
    ArrayList<PointLoad> getMemberBendingMoments(int memberIndex)
    {
		return beamNum[memberIndex].getBendingMoments();	
    }
    
    double getMaxBM()
    {
		double maxBM = 0;
		for(int k=0; k<nSpans; ++k)
		{
			if(beamNum[k].getMaxBM() > maxBM)
				maxBM = beamNum[k].getMaxBM();
		}
		return maxBM;
    } 
} // class ContinuousBeam ends

class PointLoad implements Comparable<PointLoad>
{
	private double p, x;
	
	PointLoad()
	{
	   p = 0; x = 0;
	}

	PointLoad(double p, double x)
	{
	   this.p = p; 
	   this.x = x;
	}
		
	public int compareTo(PointLoad pt)  // for drawing pt loads properly
	{
	  if(this.x < pt.x)
		return -1;
	  else if(this.x > pt.x)
		return 1;
	  else
		return 0;
	}
	
	double getLoad()
	{
	  return p;
	}
	
	double getDist()
	{
	  return x;
	}	
	
	void setLoad(double value)
	{
	  p = value;
	}
}

class UdLoad
{
	double p, x1, x2;
}



