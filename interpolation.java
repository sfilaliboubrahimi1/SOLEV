
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;
import org.joda.time.DateTime;
import org.joda.time.Period;

import densify.Densifier;

public class interpolation {
	
	public static void main (String []args) throws SQLException, ParseException, IOException{
		
		ArrayList<ArrayList<Polygon>> TrackPols =new ArrayList<ArrayList<Polygon>>();
		ArrayList<ArrayList<DateTime>> TrackTime =new ArrayList<ArrayList<DateTime>>();
		ArrayList<DateTime> tempDate =new ArrayList<>();
		ArrayList<Polygon> temp =new ArrayList<>();
		Polygon polygons[] = new Polygon[2];
		Polygon[] Densified_polygons= new Polygon[2];
		ArrayList<double[]> TS= new ArrayList<>();
		
		 Connection c = null;
	      try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5433/spoca",
	            "postgres", "root");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");

	      String sql = "select * from ss";
	      Statement stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery(sql);
	      	      
	      int i=0;
	      int old_trnum=0;
	      int count=0;
	      
	      //STEP 5: Extract data from result set
	      while(rs.next()){
	         //Retrieve by column name
	         int arid  = rs.getInt("chid");
	         int trnum = rs.getInt("trnum");
	         String dt = rs.getString("datetime");
	         DateTime datetime= DateTime.parse(dt.substring(0,29));
	    //     System.out.println("THE TIME IS"+datetime);
	      //   String first = rs.getString("first");
	         String wkbString = rs.getString("pol");
	         byte[] aux = WKBReader.hexToBytes(wkbString);
	         Geometry geom = new WKBReader().read(aux);	
	         
	         
	         String wktString = geom.toText();
	         // geometry in WKT format
	       //  System.out.printf("WKB = %s\n", wkbString);
	        // System.out.printf("trnum = "+trnum);
	      //   System.out.printf("WKT = %s\n", wktString);
	         GeometryFactory gf= new GeometryFactory();
	         WKTReader reader = new WKTReader(gf);
	        // System.out.println("trnum is"+trnum+"old trnum"+old_trnum);
	         if(old_trnum==trnum){
	    //    System.out.println("trnum is"+trnum+"old trnum"+old_trnum);
	         temp.add((Polygon) reader.read(wktString));
	         tempDate.add(datetime);
	         if(rs.isLast()){  
	         		TrackPols.add(temp);
	         		TrackTime.add(tempDate);         		
	         	}
	         	
	         }
	         else{
	        	 TrackPols.add(temp);
	        	 TrackTime.add(tempDate);
	        	 
	        	 temp= new ArrayList<>();
	        	 tempDate= new ArrayList<>();
	        	 
	        	 temp.add((Polygon) reader.read(wktString));
	        	 tempDate.add(datetime);
	         }
	      //   System.out.println("\n\nSIZE INSDE LOOP"+TrackPols.size());
	         count++;
	         old_trnum=trnum;
	         ////////////////////////////////////////////////////////////////////////
	            }//end sql scan
	      
//	System.out.println("Number of tracks is "+TrackPols.size()); 
//	System.out.println("Number of pols in track 1 is "+TrackPols.get(1).size());
//	System.out.println("Number of times in track 1 is "+TrackTime.get(1).size());
	
//	System.out.println("POLYGON 1 TRACK 1"+TrackPols.get(1).get(0).toString());
//	System.out.println("TIME 1 TRACK 1"+TrackTime.get(1).get(0).toString());

	
	
// IN TE RP OL AT ION START HEEEEEEEEEEEEEEEEEEEEEEEERRRRRRRRRRRREEEEEEEEEEEEEEEE	
		    FileWriter fstream = new FileWriter("C:\\Users\\spouki\\Documents\\GSU\\SPOCA\\ssin.txt", true); //true tells to append data.
		    BufferedWriter out = new BufferedWriter(fstream);	
int filecount=0;
for(int high=0;high<TrackPols.size();high++){
//	System.out.println("\n\nTRACK NUMBER "+high);
	for(int gr=0;gr<TrackPols.get(high).size()-1;/*gr++,*/ filecount++){
	//	System.out.println("\n\nPOLS"+gr+" and "+(gr+1)+" in TRACK NUM"+high);
	
	/*	try  
		{
		    FileWriter fstream = new FileWriter("C:\\Users\\spouki\\Documents\\GSU\\SPOCA\\EF"+filecount+".txt", true); //true tells to append data.
		    BufferedWriter out = new BufferedWriter(fstream);
		*/    
	try{	
	      polygons[0] = TrackPols.get(high).get(gr);
	      DateTime t1=TrackTime.get(high).get(gr);
	  	System.out.println("\n Polygon0="+polygons[0].toString());
	      

	      
	      Densifier densifier = new Densifier(polygons[0]);
          double distanceTolerance = 1.18;
          Densified_polygons[0] = (Polygon) densifier.densify(polygons[0], distanceTolerance);
          
          densifier = new Densifier(polygons[1]);
	      polygons[1] = TrackPols.get(high).get(++gr);
	      DateTime t2=TrackTime.get(high).get(gr);
		  	System.out.println("\n Polygon1="+polygons[1].toString());
	       
	      	String TrajectoryLabel=t1.toString()+"/"+t2.toString();
	      //	System.out.println("\n\n\\n\\n\\n\\n\\n\\n\n\n"+TrajectoryLabel);
	      Densified_polygons[1] = (Polygon) densifier.densify(polygons[1], distanceTolerance);
      /*    
          System.out.println("DENIFIED POL 1="+Densified_polygons[0].toString()+"\n");
          System.out.println("NUM POINTS POL1 ="+Densified_polygons[0].getNumPoints()+"\n");
          
          System.out.println("DENIFIED POL 2="+Densified_polygons[1].toString()+"\n");
          System.out.println("NUM POINTS POL2 ="+Densified_polygons[1].getNumPoints()+"\n");
      */   			

	      TS=TimeSeriesTransform(Densified_polygons);
	      DTW dtw= new DTW();
	      double warpingdistance;
	     
	      double [] TS2_replica= new double[TS.get(1).length*2];
	      
	      // Copy first part
        for(int y=0;y<TS.get(1).length;y++){  	 
	    	//  System.out.println(TS.get(1).length-1);
	    	  TS2_replica[y]=TS.get(1)[y];	    	  
	      }
	      
	      int num=TS.get(1).length;
	      // ducplicate first part
	      for(int y=0;y<TS.get(1).length;y++){  	 
	    	//  System.out.println(TS.get(1).length-1);
	    	  TS2_replica[num++]=TS.get(1)[y];	    	  
	      }
	      // TIME TO SECTION MY SECOND TIME SERIES....
	      ArrayList<double[]> TSS2=(ArrayList<double[]>)SectionDup(TS2_replica)[0]; 
	      double[][] indexpair = (double[][])SectionDup(TS2_replica)[1]; 
	  /*    
	      System.out.print("The pair of indeces are:\n");
	      for(int tst=0;tst<6;tst++){
		   		 System.out.print(indexpair[tst][0]+" ,");
		   		System.out.println(indexpair[tst][1]+" ,");
				 	}*/

// Start the sliding window...
	    	 double[] warps= new double[6];
	    	 int b=0;
	//    System.out.println();
	    		 for(int N=0;N<6;N++){	    	    	 
	    			 warps[b]=dtw.warpingdistance(TS.get(0),TSS2.get(N));
	    		 //    System.out.println(b+". The warping Distance is = "+warps[b]+" TS1"+"TS2_sub"+N);
	    		        b++;
	    		 }// end for
	    		 
	    	// Keep notes of the indices of the separation  
	    
	    	 
	    	int index= getMin(warps);
	       	/*System.out.println("The min Distance happened in index = "+index);
	       	System.out.println("The corresponding pairs are = "+indexpair[index][0]+" and "+indexpair[index][1]);
		
	    	 System.out.println("\n\nTHE BEST CORRESPONDING TIME SERIES IS : ");
			 for(int tst=(int)indexpair[index][0];tst<(int)indexpair[index][1];tst++){
		   		 System.out.print(TS2_replica[tst]+" ,");
				 	}*/
			 int in= (int)(indexpair[index][0]%TS.get(1).length);
			 Coordinate [] RearangedDensifiedPol2=Polygon_Reorganizer(Densified_polygons[1],index);
	//		 System.out.println("THE SECOND MATCHED POINT IS"+Densified_polygons[1].getCoordinates()[index]);
		//	 out.write("POINT("+Densified_polygons[0].getCoordinates()[0].x+" "+Densified_polygons[0].getCoordinates()[0].y+")");
		//	 out.write(" "+Densified_polygons[0].toString());
		//	 out.write("\n POINT("+Densified_polygons[1].getCoordinates()[index].x+" "+Densified_polygons[1].getCoordinates()[index].y+")");
		//	 out.write(" "+Densified_polygons[1].toString());
			 
			 // Translate Coordinates into a string
			 String str="POLYGON((";
			 for(int k=0;k<RearangedDensifiedPol2.length;k++){
				if(k==RearangedDensifiedPol2.length-1)
				 str+=RearangedDensifiedPol2[k].x+" "+RearangedDensifiedPol2[k].y+"))";
				else str+=RearangedDensifiedPol2[k].x+" "+RearangedDensifiedPol2[k].y+" ,";;
				 
			 		}
			 GeometryFactory gf=new GeometryFactory();
			 WKTReader reader = new WKTReader(gf);
			 Polygon ReorganizedPol= (Polygon) reader.read(str);
			
		       // DTW STARTS HEREEE
		        Polygon inp[]=new Polygon[2];
		        	inp[0]=Densified_polygons[0];
		        	inp[1]=ReorganizedPol;
		        		
		        double[] time1=TimeSeriesTransform(inp).get(0);
		        double[] time2=TimeSeriesTransform(inp).get(1);
		        
		        int[][] warpPath = dtw.warp(time1, time2);
		        // Transform pol ro coordnate
		        Coordinate[] input1=PolytoCoordi(Densified_polygons[0]);
		        Coordinate[] input2= PolytoCoordi(ReorganizedPol);
		        
		        Coordinate[] Interpolated;
////////////////////////////////////////////

			    
Period p = new Period(t1, t2);
double hours= 0;
double stable= p.getHours();
	if(hours<stable){	

		
		//System.out.println("\n PERIOD= "+hours);
		//System.out.println("\n"+t2.getHourOfDay());

        System.out.print("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( "+TrajectoryLabel+","+high+",999,");
        out.write("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( '"+TrajectoryLabel+"',"+high+",999,");
        System.out.print("'"+t1+"',");
        out.write("'"+t1+"',");
           System.out.print("ST_GeomFromText('POLYGON((");
           out.write("ST_GeomFromText('POLYGON((");
           for(int h=0;h<input1.length;h++){
        	   if(h==input1.length-1){ 
        		   System.out.print(input1[h].x +" "+input1[h].y+"))',4326));");
        		   out.write(input1[h].x +" "+input1[h].y+"))',4326));");
        	   
        	   }
        	   else {System.out.print(input1[h].x +" "+input1[h].y+",");
        	   	out.write(input1[h].x +" "+input1[h].y+",");
        	   }
           }
		
		
		
		while(hours<stable){
		//System.out.println("\n\t"+t1.plusHours(cout--).getHourOfDay());
		//t1.mi
		hours++;
		               Interpolated = Linechinterpolation(warpPath,input1,input2, 0,hours,stable);
		               
		            //   System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIINNTTTTTTTTTTTTTTTTTTTT4EEEEEEEEEEEEERRRRRRPPP");
		            //   System.out.println("hours"+hours);
		            System.out.print("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( "+TrajectoryLabel+","+high+",0,");
		            out.write("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( '"+TrajectoryLabel+"',"+high+",0,");
		            System.out.print("'"+t1.plusHours((int) hours)+"',");
		            out.write("'"+t1.plusHours((int) hours)+"',");
		               System.out.print("ST_GeomFromText('POLYGON((");
		               out.write("ST_GeomFromText('POLYGON((");
		               for(int h=0;h<Interpolated.length;h++){
		            	   if(h==Interpolated.length-1){ System.out.print(Interpolated[h].x +" "+Interpolated[h].y+"))',4326));");
		            	   out.write(Interpolated[h].x +" "+Interpolated[h].y+"))',4326));");		            	   
		            	   }
		            	   else {System.out.print(Interpolated[h].x +" "+Interpolated[h].y+",");
		            	   		out.write(Interpolated[h].x +" "+Interpolated[h].y+",");
		            	   }
		               }
		}
		
        System.out.print("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( "+TrajectoryLabel+","+high+",999,");
        out.write("\nINSERT INTO \"ssin\"(trajectorylabel,trnum,duration,datetime,pol) VALUES ( '"+TrajectoryLabel+"',"+high+",999,");
        System.out.print("'"+t2+"',");
        out.write("'"+t2+"',");
           System.out.print("ST_GeomFromText('POLYGON((");
           out.write("ST_GeomFromText('POLYGON((");
           for(int h=0;h<input2.length;h++){
        	   if(h==input2.length-1) {
        		   System.out.print(input2[h].x +" "+input2[h].y+"))',4326));");
        		   out.write(input2[h].x +" "+input2[h].y+"))',4326));");
        	   }
        	   else{ System.out.print(input2[h].x +" "+input2[h].y+",");
        	   		out.write(input2[h].x +" "+input2[h].y+",");
        	   }
           }
		
		
	}
//System.out.println("\n"+t1.getHourOfDay());

 /////////////////////////////////////////            	   
            	   
            	   
    /*           System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIINNTTTTTTTTTTTTTTTTTTTT4EEEEEEEEEEEEERRRRRRPPP");
               System.out.print("POLYGON((");
               for(int h=0;h<input1.length;h++){
            	   if(h==input1.length-1) System.out.println(input1[h].x +" "+input1[h].y+"))");
            	   else System.out.print(input1[h].x +" "+input1[h].y+",");
            	   }
               System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIINNTTTTTTTTTTTTTTTTTTTT4EEEEEEEEEEEEERRRRRRPPP");
               System.out.print("POLYGON((");
               for(int h=0;h<input2.length;h++){
            	   if(h==input2.length-1) System.out.println(input2[h].x +" "+input2[h].y+"))");
            	   else System.out.print(input2[h].x +" "+input2[h].y+",");
            	   }
               */
			 
			 
			 /* 
			 System.out.println("\n\n\n REORGANIZED POLYGON IS =");
			 for(int k=0;k<ReorganizedPol.getNumPoints();k++){
				 System.out.print(ReorganizedPol.getCoordinates()[k].x+" "+ReorganizedPol.getCoordinates()[k].y+" ,");
				 		}*/
			
	//		 System.out.println("\n\n"+str);
	/*		 out.close();
		}
		catch (IOException e)
		{
		    System.err.println("Error: " + e.getMessage());
		}
		
		
		
		
		*/
	
	
	}catch(Exception e){
		
		e.printStackTrace();
	}
	
	}// end pol in track loop
		
		}// end Track loop

out.close();
//System.out.println("\n\nNumber of tracks is "+TrackPols.size());  

	   	       	}
	
	static ArrayList<double[]> TimeSeriesTransform(Polygon[] input){
		
		ArrayList<double[]> TS= new ArrayList<>();
		double[] CTS;
		// go through each polygon of the sequence
		for(int i=0;i<input.length;i++){
			// got through each point of the polygon
			CTS= new double[input[i].getNumPoints()];
			for(int j=0;j<input[i].getNumPoints();j++){
				CTS[j]=input[i].getCoordinates()[j].distance(input[i].getCentroid().getCoordinate());				
			///	System.out.print(", "+CTS[j]);
			}
			TS.add(CTS);
			//System.out.print(", "+TS.get(i));
			
			System.out.println();
		}	
		return TS;
	}
	
	static Object[] SectionDup(double[] input){
		
		Object[] obj = new Object[2];
		ArrayList<double[]> TSS= new ArrayList<>();
		double[][] indexpairs= new double[6][2];
		double leftratio=0;
	    double rightratio=0.5;
	      
	   /* 	  System.out.println("\n\n");
		    	 for(int pp=0;pp<input.length;pp++){
		    		 System.out.print(pp+". "+input[pp]+" ,");	 
		    		 
		    	 }*/
	    	//  System.out.println("\n\nTOTAL NUMBER OF POINTS"+input.length);
	    	  for(int p=0;rightratio<=1;p++){
	    	  double ratio=0.1;
	    	  // FIND FIRST THE CORRECT INDEX
	    	  int li=(int)(input.length*leftratio);
	    	  int ri=(int)(input.length*rightratio);
	    	  indexpairs[p][0]=li;
	    	  indexpairs[p][1]=ri;
	    	//  System.out.println("LEFT INDEX = "+li); 
	    	//  System.out.println("RIGHT INDEX = "+ri);
  	  
	    	 double [] db= Arrays.copyOfRange(input, li,ri);
	    	 TSS.add(db);
	    	/* for(int pp=0;pp<db.length;pp++){
	    		 System.out.print(db[pp]+" ,");	 
	    		 
	    	 } */
	    	  leftratio+=ratio;
	    	  rightratio+=ratio;
	    	  
	    	 obj[0]=TSS;
	    	 obj[1]=indexpairs;
	    //	 System.out.println();
	    	  }

	      	
		return obj;
	}
	
	static ArrayList<double[]> TimeSeriesSection(double[] input){
		
		ArrayList<double[]> TSS= new ArrayList<>();
		double leftratio=0;
	      double rightratio=0.5;
	      
	    	  System.out.println("\n\n");
		    	/* for(int pp=0;pp<input.length;pp++){
		    		 System.out.print(pp+". "+input[pp]+" ,");	 
		    		 
		    	 }*/
	    	//  System.out.println("\n\nTOTAL NUMBER OF POINTS"+input.length);
	    	  for(int p=0;rightratio<=1;p++){
	    	  double ratio=0.01;
	    	  // FIND FIRST THE CORRECT INDEX
	    	  int li=(int)(input.length*leftratio)+1;
	    	  int ri=(int)(input.length*rightratio)+1;
	    	  
	    	//  System.out.println("LEFT INDEX = "+li); 
	    	//  System.out.println("RIGHT INDEX = "+ri);
  	  
	    	 double [] db= Arrays.copyOfRange(input, li,ri);
	    	 TSS.add(db);
	    	/* for(int pp=0;pp<db.length;pp++){
	    		 System.out.print(db[pp]+" ,");	 
	    		 
	    	 } */
	    	  leftratio+=0.1;
	    	  rightratio+=0.1;
	    	 
	    	// System.out.println();
	    	  }

	      	
		return TSS;
	}
		static void PrintTS(ArrayList<double[]>TSS1){
			 //Printing the TS 
			 for(int tst=0;tst<TSS1.size();tst++){
				 System.out.println("\nTIME SERIES NUMBER= "+tst);
				 for(int j=0;j<TSS1.get(tst).length;j++){
					 	Locale locale = Locale.US;
					    System.out.print(Double.parseDouble(NumberFormat.getNumberInstance(locale).format(TSS1.get(tst)[j]))+", ");
					 
		   		// System.out.print(" ,");
				 	}
				 
			 }
	}// end method
		public static int getMin(double[] array){  
		      double min = array[0];  
		      int index=0;
		      for(int i=1;i < array.length;i++){  
		      if(array[i] < min){  
		      min = array[i];
		      index=i;

		         }  
		     }  
		             return index;  
		}  // end min mehtod
		
		 public static Coordinate[] Polygon_Reorganizer(Polygon polygon_densified, int count){

		      //  System.out.println("Found the endpoint in index"+count);
		        // Re-arrange a new clean polygon that starts with the endpoint
		        Coordinate[] clean= new Coordinate[polygon_densified.getNumPoints()];
		        int counter=0;
		        for(int i=count;i<polygon_densified.getNumPoints();i++){
		            clean[counter]=polygon_densified.getCoordinates()[i];
		            //      System.out.println(counter+". "+clean[counter].toString());
		            counter++;
		        }
		        if(counter<polygon_densified.getNumPoints()){
		            for(int i=1;i<count;i++){
		                clean[counter]=polygon_densified.getCoordinates()[i];
		                //      System.out.println(counter+". "+clean[counter].toString());
		                counter++;
		            }
		            clean[polygon_densified.getNumPoints()-1]=polygon_densified.getCoordinates()[count];
		        }
		        // System.out.println("The Coordinates are as follow");
		     /*   for(int i=0;i<polygon_densified.getNumPoints();i++){
		                System.out.println(clean[i]);
		        }*/
		        return clean;
		    }
	public static Coordinate[] PolytoCoordi(Polygon pol){
	//	System.out.println("umber of points"+pol.getNumPoints());
		Coordinate[] ret= new Coordinate[pol.getNumPoints()];
		
		for(int i=0;i<pol.getNumPoints();i++){
			
			Coordinate cor=new Coordinate(pol.getCoordinates()[i].x,pol.getCoordinates()[i].y);
			ret[i]=cor;
		//	System.out.println("Points"+ret[i].x);
			}				
		return ret;
	}	 
		 
	public static Coordinate[]  Linechinterpolation(int[][]warpPath,Coordinate[]clean, Coordinate[]clean2, double t1,double t, double t2){
		      Coordinate[] Interp=new Coordinate[warpPath.length];
		      double g1=t1;
		      double g=t;
		      double g2=t2;


		      for(int i=0;i<warpPath.length;i++){
		          Coordinate d1= clean[warpPath[i][0]];
		          Coordinate d;
		          Coordinate d2= clean2[warpPath[i][1]];

		          double dx=d1.x+(g-g1)/(g2-g1)*(d2.x-d1.x);
		          double dy=d1.y+(g-g1)/(g2-g1)*(d2.y-d1.y);

		          d= new Coordinate(dx,dy);
		          Interp[i]=d;

		      }
		  /*    System.out.print("\n\nThe interpolated polygon points are as follows:");
		      for(int i=0;i<warpPath.length;i++){
		          System.out.print(Interp[i].x+" "+Interp[i].y+",");
		      }

		      System.out.println("\n\nclean num of points"+clean.length+"clean2 num of points"+clean2.length);
*/
		return Interp;

		    }
		
}





//      PrintTS(TSS2);

// PPPPPPPPPPPPPPPPPPPPPPPPRRRRRRRRRRRRRRRRRRRRRRRRRIIIIIIIIIIIIIIIIIIIIIIIIIINNNNNNNNNNNNNNNNNNNNNNNNNNNTTTTTTTTTT   
/*	    	 System.out.println("\nTIME SERIES NUMBER 1");
	 for(int tst=0;tst<TS.get(0).length;tst++){
	 		Locale locale = Locale.US;

 		 System.out.print(Double.parseDouble(NumberFormat.getNumberInstance(locale).format(TS.get(0)[tst]))+" ,");
		 	}
/*    	 System.out.println("\n\nTIME SERIES NUMBER 2");
	 for(int tst=0;tst<TS.get(1).length;tst++){
		// System.out.println("\nTIME SERIES NUMBER= "+tst);
	//	 for(int j=0;j<TSS1.get(tst).length;j++){
 		 System.out.print(TS.get(1)[tst]+" ,");
		 	}
	 System.out.println("\n");
	 System.out.println("\n\nTIME SERIES NUMBER 2 DUPLICA");
	 for(int tst=0;tst<TS2_replica.length;tst++){
		// System.out.println("\nTIME SERIES NUMBER= "+tst);
	//	 for(int j=0;j<TSS1.get(tst).length;j++){
 		 System.out.print(TS2_replica[tst]+" ,");
		 	}
	 System.out.println("\n");			 
*/
// PPPPPPPPPPPPPPPPPPPPPPPPRRRRRRRRRRRRRRRRRRRRRRRRRIIIIIIIIIIIIIIIIIIIIIIIIIINNNNNNNNNNNNNNNNNNNNNNNNNNNTTTTTTTTTT    	 

