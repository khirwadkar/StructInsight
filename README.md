# StructInsight

Structural analysis software, dealing with continuous beams; facilitates quick input of data.

------



## Aim of the Project

This project was conceived to act as a teaching tool. It's primary aim is to develop and enhance among the students, the "feel" of the behaviour of structures. Although there are quite a few popular structural analysis and design software programs available, this program differs by being simple to use, intuitive for data input, and quite light weight. In the present state of its development, it allows the user to quickly define the geometry of a continuous beam, and then see its behaviour under different load combinations.  The behaviour, in the form of reactions, shear forces, and bending moments, is shown graphically as well as numerically, while deformations are given numerically. An undergraduate student can solve many exercise problems in very less amount of time using this software, and better internalise the understanding of the structural behaviour. Aided with this software, a professor/teacher can present more illustrative examples during their lecture. A working professional may use this software for creating the first rough design by quickly sifting through different alternative combinations of individual beams and loads.

------



## Description of Modules

This software was developed using programming features available in the Tiger version of Java programming language (OpenJDK 1.5). The purpose of each of the three packages in this software and the classes within those packages is briefly described below. This should be helpful for the developers who want to improve/modify the program and adapt it to some specific needs. 

### Package structinsight2020

This package comprises only one file, viz., "StructInsightApp.java". Only one outer class has been defined in this file;  according to the class-naming rules of Java, it's name is *StructInsightApp*. It contains *main()* method, the starting point of the application. When executed, this class displays a window with one button in its client area. On the face of the button, a figure representing a continuous beam, is displayed. The window acts as a single choice menu; that single choice of invoking the continuous beam analysis program, is made by clicking the button. It is expected/proposed that, in the future, capability to analyse more types of structures like *plane frame, space frame, plane truss, space truss*, etc., would be added to this software and menu buttons for each of those types would be provided in this window. 



### Package structinsight2020.contibeam

This sub-package represents the continuous beam analysis program. It consists of the following files.

- ContiBeamMain.java: This file contains classes to display a window acting as a sub-menu with five choices. 
  1. Get the beam data.
  2. Get the load data.
  3. Analyse the beam and display the results.
  4. Give the software's  '*About*' information.
  5. Exit the program.
- BeamDataFrame.java: This file contains the definitions of the classes that allow the user to input the data about the geometry of the continuous beam, such as the number of component beam members, their respective lengths, cross-sectional properties, support conditions, etc.
- LoadDataFrame.java: This file contains the classes that enable the user to input the data about the various loads on the continuous beam.
- AnalysisFrame.java: The classes in this file get done the job of analysing the beam and then they display the graphical results in the form of reaction diagram, shear force diagram, and bending moment diagram. A text file with name 'result.data' also gets created here. It stores the numerical results of the analysis for further perusal.
- AboutFrame.java: The classes responsible for displaying the typical 'About' information are defined here. An external text file with name 'TnC.txt' containing the terms and conditions, is used by these classes for fulfilling their objective.
- ContinuousBeam.java: The actual analysis of the continuous beam, using the stiffness matrix method, is carried out here. A static object of the *ContinuousBeam* class from this file, is created in the *ContiBeamMain* class and is then shared by the other classes to store and access the data.
- Beam.java: The storage of data about the individual beam members of the continuous beam, is done in the objects of the *Beam* class defined in this file.



### Package kitematrixutil

Only one file with name "Matrix.java" is part of this package, at present. Our software uses the *inverse_matrix( )* method of the *Matrix* class defined in this file. It is used to invert the global stiffness matrix assembled for the given continuous beam.

------



