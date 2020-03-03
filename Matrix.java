package kitematrixutil;

public class Matrix
{
	public static double[][] inverse_matrix(double a[][], int nColumns)
	{
		/* This function computes the inverse of the  
		* nColumns x nColumns part of the argument matrix
		* and returns it. If that part of the argument matrix is singular, i.e.,
		* its inverse is not possible, then this function returns a
		* 1x1 singular matrix.
		*/
		double singular[][] = {{0}};
		double inv[][];
		int i, j, k, ij, n, npn;
		double col, row;

		n = nColumns;
		inv = new double[n][n];   // All elements are zeroes by default

		// Storing identity matrix in inv 
		for(i=0; i<n; ++i)
		{
			inv[i][i] = 1.0;
		}

		// Doing pivotal and other transformations
		for(k=0; k<n; ++k)
		{
			/* Checking if any diagonal element is zero.
			*  That indicates, determinant is zero;
			*  and that means the given matrix is singular.
			*/
			if(Math.abs(a[k][k]) < 0.0000001)  // nearly zero
				return singular;
		  
			for(i=0; i<n; ++i)
			{
				if(i == k)  // diagonal element
					continue;
				else
				{
					col = a[i][k] / a[k][k]; 
					for(j=0; j<n; ++j)
					{
						a[i][j] = a[i][j] - col * a[k][j];
						inv[i][j] = inv[i][j] - col * inv[k][j]; // same transform
					}
				}
			}
		}

		// Dividing by pivotal element
		for(i=0; i<n; ++i)
		{
			row = a[i][i];
			for(j=0; j<n; ++j)
				inv[i][j] /= row;
		}

		return inv;
	}

	public static void display_matrix(double a[][])
	{
		int nRows = a.length;
		int nCols =a[0].length;
		int i, j;
		for(i=0; i<nRows; i++)
		{
			for(j=0; j<nCols; ++j)
			{
				System.out.print(String.format("%10.3f", a[i][j]) + "    ");
			}
			System.out.println();
		}
	}

  
  /* Test cases for matrix inversion
   * [ 1  3  3 ]          [ 7  -3  -3 ]
   * [ 1  4  3 ]          [-1   1   0 ]
   * [ 1  3  4 ]          [-1   0   1 ]
   * -----------------------------------
   * 
   * [ 1  2  3 ]          [-24  18  5 ]
   * [ 0  1  4 ]          [ 20 -15 -4 ]
   * [ 5  6  0 ]          [ -5   4  1 ]
   * -----------------------------------
   * 
   * [ 1  0 -2 ]          [  7  -2   2 ]
   * [ 4  1  0 ]          [-28   9  -8 ]
   * [ 1  1  7 ]          [  3  -1   1 ]
   * ------------------------------------
   */
  
  /*  Second example-
   *  [ 1  2  3   |  1  0  0 ]
   *  [ 0  1  4   |  0  1  0 ]
   *  [ 5  6  0   |  0  0  1 ]
   *  
   *  Transformation on R2 not necessary
   *  
   *  Transform R3 - 5 x R1 produces:
   *  [ 1  2   3   |  1  0  0 ]
   *  [ 0  1   4   |  0  1  0 ]
   *  [ 0 -4 -15   | -5  0  1 ]
   * 
   *  Likewise, apply the following transforms-
   *  R1 - 2 * R2
   *  R3 - 4 * R2
   *  R1 + 5 * R3
   *  R2 - 4 * R1
   *  
   */

   public static void main(String[] args)
   {
       double test1[][] = {{5}};
       double inv_test1[][];
       System.out.println("Original matrix 1:"); 
       display_matrix(test1);
       inv_test1 = inverse_matrix(test1, 1);
       System.out.println("Inverse matrix 1:"); 
       display_matrix(inv_test1);
       System.out.println("------------------------------------------------------------");
       
       double test2[][] = {{1, 3, 3}, {1, 4, 3}, {1, 4, 3}};
       double inv_test2[][];
       System.out.println("Original matrix 2:"); 
       display_matrix(test2);
       inv_test2 = inverse_matrix(test2, 3);
       System.out.println("Inverse matrix 2:"); 
       display_matrix(inv_test2);
       System.out.println("------------------------------------------------------------");
       
       double test3[][] = {{1, 2, 3}, {0, 1, 4}, {5, 6, 0}};
       double inv_test3[][];
       System.out.println("Original matrix 3:"); 
       display_matrix(test3);
       inv_test3 = inverse_matrix(test3, 3);
       System.out.println("Inverse matrix 3:"); 
       display_matrix(inv_test3);
       System.out.println("------------------------------------------------------------");
       
       double test4[][] = {{1, 0, 0, 0}, 
                           {0, 1, 0, 0},
                           {0, 0, 1, 0},
                           {0, 0, 0, 1}};
       double inv_test4[][];
       System.out.println("Original matrix 4:"); 
       display_matrix(test4);
       inv_test4 = inverse_matrix(test4, 4);
       System.out.println("Inverse matrix 4:"); 
       display_matrix(inv_test4);
       System.out.println("------------------------------------------------------------");
       
   }
}

