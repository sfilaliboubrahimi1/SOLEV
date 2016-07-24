

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import snaq.db.DBPoolDataSource;

import edu.gsu.dmlab.databases.ImageDBConnection;
import edu.gsu.dmlab.databases.SingleFeatureDBConnection;
import edu.gsu.dmlab.databases.interfaces.IFeatureDBConnection;
import edu.gsu.dmlab.databases.interfaces.IImageDBConnection;
import edu.gsu.dmlab.databases.interfaces.ITrackDBConnection;
import edu.gsu.dmlab.databases.interfaces.ImageDBWaveParamPair;
import edu.gsu.dmlab.datatypes.EventType;
import edu.gsu.dmlab.datatypes.GenericEvent;
import edu.gsu.dmlab.datatypes.Track;
import edu.gsu.dmlab.datatypes.interfaces.IEvent;
import edu.gsu.dmlab.datatypes.interfaces.ITrack;
import edu.gsu.dmlab.exceptions.InvalidConfigException;
import edu.gsu.dmlab.factory.BLASDataTypeFactory;
import edu.gsu.dmlab.factory.interfaces.IBLASDataTypeFactory;
import edu.gsu.dmlab.geometry.Point2D;
import edu.gsu.dmlab.imageproc.ImgParamNormalizer;
import edu.gsu.dmlab.imageproc.interfaces.IImgParamNormalizer;

public class Sandbox {

	public static void main(String[] args) throws SQLException, IOException, InvalidConfigException, ParseException {

		DBPoolDataSource imngDBPoolSourc = null;
		imngDBPoolSourc = new DBPoolDataSource();
		imngDBPoolSourc.setName("img-pool-ds");
		imngDBPoolSourc.setIdleTimeout(Integer.parseInt("650"));
		imngDBPoolSourc.setMinPool(Integer.parseInt("1"));
		imngDBPoolSourc.setMaxPool(Integer.parseInt("3"));
		imngDBPoolSourc.setMaxSize(Integer.parseInt("5"));
/*		imngDBPoolSourc.setUser("remoteUser");

*			SOLEV DATABASE AUTHENTIFICATION
*
*/
		DBPoolDataSource trkDBPoolSourc = null;
		trkDBPoolSourc = new DBPoolDataSource();
		trkDBPoolSourc.setName("tracking-pool-ds");


		int cacheSize = 2;
		HashMap<Integer, double[]> histoBoundaryMap = new HashMap<Integer, double[]>();
		histoBoundaryMap.put(Integer.valueOf(0), new double[] { 0.75, 8.3 });
		histoBoundaryMap.put(Integer.valueOf(1), new double[] { 0, 256 });
		histoBoundaryMap.put(Integer.valueOf(2), new double[] { 0, 110 });
		histoBoundaryMap.put(Integer.valueOf(3), new double[] { 0.75, 2 });
		histoBoundaryMap.put(Integer.valueOf(4), new double[] { 0, 14 });
		histoBoundaryMap.put(Integer.valueOf(5), new double[] { 0, 255 });
		histoBoundaryMap.put(Integer.valueOf(6), new double[] { -0.06, 1 });
		histoBoundaryMap.put(Integer.valueOf(7), new double[] { -0.001, 0.005 });
		histoBoundaryMap.put(Integer.valueOf(8), new double[] { 0, 40 });
		histoBoundaryMap.put(Integer.valueOf(9), new double[] { -0.001, 0.06 });

		IBLASDataTypeFactory blasFactory = new BLASDataTypeFactory();
		IImgParamNormalizer normalizer = new ImgParamNormalizer(histoBoundaryMap);

		//database connection for image parameters
		IImageDBConnection imageDB = new ImageDBConnection(imngDBPoolSourc, blasFactory, normalizer, cacheSize);

		//database connection for tracks
	//	ITrackDBConnection  trkDb = new TrackDBConnection_V2(trkDBPoolSourc);

		//Connection for best image features to select
	//	IFeatureDBConnection featDbConnect = new SingleFeatureDBConnection(trkDBPoolSourc);
	//	int numFeatures = 5;
	//	ImageDBWaveParamPair[] bestFeatList = featDbConnect.getBestFeatures(EventType.FLARE, numFeatures);

		// Store Data in a file....
		/*	PrintWriter writer = new PrintWriter("C:\\Users\\spouki\\Documents\\GSU\\SPOCA\\FL_SQLScript.txt", "UTF-8");

				//Displays images
				SeqDisp disp = new SeqDisp(imageDB, trkDb);
				ArrayList<ITrack> trks =disp.trks;
				 System.out.print("Number of Tracks\n"+trks.size());
				for(int i = 0; i < trks.size(); i++) {   
					ArrayList<IEvent> events= trks.get(i).getEvents();

					if(events.size()==1); 
					else{
					for(int k = 0; k < events.size(); k++) {  
						System.out.print("INSERT INTO \"fl\"(trnum,duration,datetime,pol) VALUES (");
						writer.print("INSERT INTO \"fl\"(trnum,duration,datetime,pol) VALUES (");
						System.out.print(" "+i+", ");
						writer.print(" "+i+", ");
						System.out.print(events.get(k).getTimePeriod().toDurationMillis()+", ");
						writer.print(events.get(k).getTimePeriod().toDurationMillis()+", ");
					/*	System.out.print("START"+events.get(k).getTimePeriod().getStart()+" END "+events.get(k).getTimePeriod().getEnd());

						DateTime t1=events.get(k).getTimePeriod().getStart();
						DateTime t2=events.get(k).getTimePeriod().getEnd();
						Period p = new Period(t1, t2);
						int hours = p.getHours();

						System.out.println("\n PERIOD= "+hours);
						System.out.println("\n"+t1.getHourOfDay());

						int count=1;
						while(hours!=0){
						System.out.println("\n\t"+t1.plusHours(count++).getHourOfDay());
						hours--;
						}
						System.out.println("\n"+t2.getHourOfDay());

						//	writer.print(events.get(k).getTimePeriod().toDurationMillis()+", ");						
						System.out.print("\'"+events.get(k).getTimePeriod().toString()+"\',ST_GeomFromText('POLYGON(( ");
						writer.print("\'"+events.get(k).getTimePeriod().toString()+"\',ST_GeomFromText('POLYGON(( ");


						for(int j = 0; j<events.get(k).getShape().xpoints.length; j++) {
							if (j== events.get(k).getShape().xpoints.length-1){	
						System.out.print(events.get(k).getShape().xpoints[j]+" "+events.get(k).getShape().ypoints[j]+"))',4326));");
						writer.print(events.get(k).getShape().xpoints[j]+" "+events.get(k).getShape().ypoints[j]+"))',4326));");

							}
							else{
								System.out.print(events.get(k).getShape().xpoints[j]+" "+events.get(k).getShape().ypoints[j]+", ");
								writer.print(events.get(k).getShape().xpoints[j]+" "+events.get(k).getShape().ypoints[j]+", ");

							}


						}		
						    System.out.println();
						    writer.println();
						}
					}
				}  

				writer.close();
				disp.start();

			}

		}
		 */
		//////////////////        HARDCODED 3 INSTANCES          \\\\\\\\\\
		
/*				GeometryFactory gf= new GeometryFactory();
		WKTReader reader = new WKTReader(gf);
		com.vividsolutions.jts.geom.Polygon p= (com.vividsolutions.jts.geom.Polygon) reader.read("POLYGON((3360.0 2704.0,3360.657894736842 2704.9473684210525,3361.315789473684 2705.8947368421054,3361.9736842105262 2706.842105263158,3362.6315789473683 2707.7894736842104,3363.2894736842104 2708.7368421052633,3363.9473684210525 2709.684210526316,3364.6052631578946 2710.6315789473683,3365.2631578947367 2711.5789473684213,3365.9210526315787 2712.5263157894738,3366.5789473684213 2713.4736842105262,3367.2368421052633 2714.4210526315787,3367.8947368421054 2715.3684210526317,3368.5526315789475 2716.315789473684,3369.2105263157896 2717.2631578947367,3369.8684210526317 2718.2105263157896,3370.5263157894738 2719.157894736842,3371.184210526316 2720.1052631578946,3371.842105263158 2721.0526315789475,3372.5 2722.0,3373.157894736842 2722.9473684210525,3373.815789473684 2723.8947368421054,3374.4736842105262 2724.842105263158,3375.1315789473683 2725.7894736842104,3375.7894736842104 2726.7368421052633,3376.4473684210525 2727.684210526316,3377.1052631578946 2728.6315789473683,3377.7631578947367 2729.5789473684213,3378.4210526315787 2730.5263157894738,3379.0789473684213 2731.4736842105262,3379.7368421052633 2732.4210526315787,3380.3947368421054 2733.3684210526317,3381.0526315789475 2734.315789473684,3381.7105263157896 2735.2631578947367,3382.3684210526317 2736.2105263157896,3383.0263157894738 2737.157894736842,3383.684210526316 2738.1052631578946,3384.342105263158 2739.0526315789475,3385.0 2740.0,3386.0337078651687 2739.449438202247,3387.067415730337 2738.8988764044943,3388.1011235955057 2738.3483146067415,3389.134831460674 2737.7977528089887,3390.1685393258426 2737.247191011236,3391.2022471910113 2736.696629213483,3392.2359550561796 2736.14606741573,3393.2696629213483 2735.5955056179773,3394.303370786517 2735.044943820225,3395.3370786516853 2734.494382022472,3396.370786516854 2733.9438202247193,3397.4044943820227 2733.3932584269664,3398.438202247191 2732.8426966292136,3399.4719101123596 2732.2921348314608,3400.505617977528 2731.741573033708,3401.5393258426966 2731.191011235955,3402.5730337078653 2730.6404494382023,3403.6067415730336 2730.0898876404494,3404.6404494382023 2729.5393258426966,3405.674157303371 2728.9887640449438,3406.7078651685392 2728.438202247191,3407.741573033708 2727.887640449438,3408.775280898876 2727.3370786516853,3409.808988764045 2726.7865168539324,3410.8426966292136 2726.2359550561796,3411.876404494382 2725.6853932584268,3412.9101123595506 2725.1348314606744,3413.9438202247193 2724.5842696629215,3414.9775280898875 2724.0337078651687,3416.0112359550562 2723.483146067416,3417.044943820225 2722.932584269663,3418.078651685393 2722.38202247191,3419.112359550562 2721.8314606741574,3420.14606741573 2721.2808988764045,3421.179775280899 2720.7303370786517,3422.2134831460676 2720.179775280899,3423.247191011236 2719.629213483146,3424.2808988764045 2719.078651685393,3425.3146067415732 2718.5280898876404,3426.3483146067415 2717.9775280898875,3427.38202247191 2717.4269662921347,3428.4157303370785 2716.876404494382,3429.449438202247 2716.325842696629,3430.483146067416 2715.775280898876,3431.516853932584 2715.224719101124,3432.550561797753 2714.674157303371,3433.5842696629215 2714.123595505618,3434.61797752809 2713.5730337078653,3435.6516853932585 2713.0224719101125,3436.6853932584268 2712.4719101123596,3437.7191011235955 2711.921348314607,3438.752808988764 2711.370786516854,3439.7865168539324 2710.820224719101,3440.820224719101 2710.2696629213483,3441.85393258427 2709.7191011235955,3442.887640449438 2709.1685393258426,3443.921348314607 2708.61797752809,3444.955056179775 2708.067415730337,3445.9887640449438 2707.516853932584,3447.0224719101125 2706.9662921348313,3448.0561797752807 2706.4157303370785,3449.0898876404494 2705.8651685393256,3450.123595505618 2705.3146067415732,3451.1573033707864 2704.7640449438204,3452.191011235955 2704.2134831460676,3453.224719101124 2703.6629213483147,3454.258426966292 2703.112359550562,3455.2921348314608 2702.561797752809,3456.325842696629 2702.0112359550562,3457.3595505617977 2701.4606741573034,3458.3932584269664 2700.9101123595506,3459.4269662921347 2700.3595505617977,3460.4606741573034 2699.808988764045,3461.494382022472 2699.258426966292,3462.5280898876404 2698.7078651685392,3463.561797752809 2698.1573033707864,3464.5955056179773 2697.6067415730336,3465.629213483146 2697.0561797752807,3466.6629213483147 2696.505617977528,3467.696629213483 2695.955056179775,3468.7303370786517 2695.4044943820227,3469.7640449438204 2694.85393258427,3470.7977528089887 2694.303370786517,3471.8314606741574 2693.752808988764,3472.8651685393256 2693.2022471910113,3473.8988764044943 2692.6516853932585,3474.932584269663 2692.1011235955057,3475.9662921348313 2691.550561797753,3477.0 2691.0,3477.277777777778 2689.8703703703704,3477.5555555555557 2688.740740740741,3477.8333333333335 2687.6111111111113,3478.1111111111113 2686.4814814814813,3478.3888888888887 2685.3518518518517,3478.6666666666665 2684.222222222222,3478.9444444444443 2683.0925925925926,3479.222222222222 2681.962962962963,3479.5 2680.8333333333335,3479.777777777778 2679.703703703704,3480.0555555555557 2678.574074074074,3480.3333333333335 2677.4444444444443,3480.6111111111113 2676.314814814815,3480.8888888888887 2675.185185185185,3481.1666666666665 2674.0555555555557,3481.4444444444443 2672.925925925926,3481.722222222222 2671.796296296296,3482.0 2670.6666666666665,3482.277777777778 2669.537037037037,3482.5555555555557 2668.4074074074074,3482.8333333333335 2667.277777777778,3483.1111111111113 2666.1481481481483,3483.3888888888887 2665.0185185185187,3483.6666666666665 2663.8888888888887,3483.9444444444443 2662.759259259259,3484.222222222222 2661.6296296296296,3484.5 2660.5,3484.777777777778 2659.3703703703704,3485.0555555555557 2658.240740740741,3485.3333333333335 2657.1111111111113,3485.6111111111113 2655.9814814814813,3485.8888888888887 2654.8518518518517,3486.1666666666665 2653.722222222222,3486.4444444444443 2652.5925925925926,3486.722222222222 2651.462962962963,3487.0 2650.3333333333335,3487.277777777778 2649.203703703704,3487.5555555555557 2648.074074074074,3487.8333333333335 2646.9444444444443,3488.1111111111113 2645.814814814815,3488.3888888888887 2644.685185185185,3488.6666666666665 2643.5555555555557,3488.9444444444443 2642.425925925926,3489.222222222222 2641.296296296296,3489.5 2640.1666666666665,3489.777777777778 2639.037037037037,3490.0555555555557 2637.9074074074074,3490.3333333333335 2636.777777777778,3490.6111111111113 2635.6481481481483,3490.8888888888887 2634.5185185185187,3491.1666666666665 2633.3888888888887,3491.4444444444443 2632.259259259259,3491.722222222222 2631.1296296296296,3492.0 2630.0,3491.0833333333335 2629.2638888888887,3490.1666666666665 2628.527777777778,3489.25 2627.7916666666665,3488.3333333333335 2627.0555555555557,3487.4166666666665 2626.3194444444443,3486.5 2625.5833333333335,3485.5833333333335 2624.847222222222,3484.6666666666665 2624.1111111111113,3483.75 2623.375,3482.8333333333335 2622.6388888888887,3481.9166666666665 2621.902777777778,3481.0 2621.1666666666665,3480.0833333333335 2620.4305555555557,3479.1666666666665 2619.6944444444443,3478.25 2618.9583333333335,3477.3333333333335 2618.222222222222,3476.4166666666665 2617.4861111111113,3475.5 2616.75,3474.5833333333335 2616.0138888888887,3473.6666666666665 2615.277777777778,3472.75 2614.5416666666665,3471.8333333333335 2613.8055555555557,3470.9166666666665 2613.0694444444443,3470.0 2612.3333333333335,3469.0833333333335 2611.597222222222,3468.1666666666665 2610.8611111111113,3467.25 2610.125,3466.3333333333335 2609.3888888888887,3465.4166666666665 2608.652777777778,3464.5 2607.9166666666665,3463.5833333333335 2607.1805555555557,3462.6666666666665 2606.4444444444443,3461.75 2605.7083333333335,3460.8333333333335 2604.972222222222,3459.9166666666665 2604.2361111111113,3459.0 2603.5,3458.0833333333335 2602.7638888888887,3457.1666666666665 2602.027777777778,3456.25 2601.2916666666665,3455.3333333333335 2600.5555555555557,3454.4166666666665 2599.8194444444443,3453.5 2599.0833333333335,3452.5833333333335 2598.347222222222,3451.6666666666665 2597.6111111111113,3450.75 2596.875,3449.8333333333335 2596.1388888888887,3448.9166666666665 2595.402777777778,3448.0 2594.6666666666665,3447.0833333333335 2593.9305555555557,3446.1666666666665 2593.1944444444443,3445.25 2592.4583333333335,3444.3333333333335 2591.722222222222,3443.4166666666665 2590.9861111111113,3442.5 2590.25,3441.5833333333335 2589.5138888888887,3440.6666666666665 2588.777777777778,3439.75 2588.0416666666665,3438.8333333333335 2587.3055555555557,3437.9166666666665 2586.5694444444443,3437.0 2585.8333333333335,3436.0833333333335 2585.097222222222,3435.1666666666665 2584.3611111111113,3434.25 2583.625,3433.3333333333335 2582.8888888888887,3432.4166666666665 2582.152777777778,3431.5 2581.4166666666665,3430.5833333333335 2580.6805555555557,3429.6666666666665 2579.9444444444443,3428.75 2579.2083333333335,3427.8333333333335 2578.472222222222,3426.9166666666665 2577.7361111111113,3426.0 2577.0,3425.0 2577.59375,3424.0 2578.1875,3423.0 2578.78125,3422.0 2579.375,3421.0 2579.96875,3420.0 2580.5625,3419.0 2581.15625,3418.0 2581.75,3417.0 2582.34375,3416.0 2582.9375,3415.0 2583.53125,3414.0 2584.125,3413.0 2584.71875,3412.0 2585.3125,3411.0 2585.90625,3410.0 2586.5,3409.0 2587.09375,3408.0 2587.6875,3407.0 2588.28125,3406.0 2588.875,3405.0 2589.46875,3404.0 2590.0625,3403.0 2590.65625,3402.0 2591.25,3401.0 2591.84375,3400.0 2592.4375,3399.0 2593.03125,3398.0 2593.625,3397.0 2594.21875,3396.0 2594.8125,3395.0 2595.40625,3394.0 2596.0,3393.0 2596.59375,3392.0 2597.1875,3391.0 2597.78125,3390.0 2598.375,3389.0 2598.96875,3388.0 2599.5625,3387.0 2600.15625,3386.0 2600.75,3385.0 2601.34375,3384.0 2601.9375,3383.0 2602.53125,3382.0 2603.125,3381.0 2603.71875,3380.0 2604.3125,3379.0 2604.90625,3378.0 2605.5,3377.0 2606.09375,3376.0 2606.6875,3375.0 2607.28125,3374.0 2607.875,3373.0 2608.46875,3372.0 2609.0625,3371.0 2609.65625,3370.0 2610.25,3369.0 2610.84375,3368.0 2611.4375,3367.0 2612.03125,3366.0 2612.625,3365.0 2613.21875,3364.0 2613.8125,3363.0 2614.40625,3362.0 2615.0,3361.9866666666667 2616.173333333333,3361.9733333333334 2617.346666666667,3361.96 2618.52,3361.9466666666667 2619.693333333333,3361.9333333333334 2620.866666666667,3361.92 2622.04,3361.9066666666668 2623.213333333333,3361.8933333333334 2624.3866666666668,3361.88 2625.56,3361.866666666667 2626.733333333333,3361.8533333333335 2627.9066666666668,3361.84 2629.08,3361.826666666667 2630.2533333333336,3361.8133333333335 2631.4266666666667,3361.8 2632.6,3361.786666666667 2633.7733333333335,3361.7733333333335 2634.9466666666667,3361.76 2636.12,3361.7466666666664 2637.2933333333335,3361.733333333333 2638.4666666666667,3361.72 2639.64,3361.7066666666665 2640.8133333333335,3361.693333333333 2641.9866666666667,3361.68 2643.16,3361.6666666666665 2644.3333333333335,3361.653333333333 2645.5066666666667,3361.64 2646.68,3361.6266666666666 2647.8533333333335,3361.6133333333332 2649.0266666666666,3361.6 2650.2,3361.5866666666666 2651.3733333333334,3361.5733333333333 2652.5466666666666,3361.56 2653.72,3361.5466666666666 2654.8933333333334,3361.5333333333333 2656.0666666666666,3361.52 2657.24,3361.5066666666667 2658.4133333333334,3361.4933333333333 2659.5866666666666,3361.48 2660.76,3361.4666666666667 2661.9333333333334,3361.4533333333334 2663.1066666666666,3361.44 2664.28,3361.4266666666667 2665.4533333333334,3361.4133333333334 2666.6266666666666,3361.4 2667.8,3361.3866666666668 2668.9733333333334,3361.3733333333334 2670.1466666666665,3361.36 2671.32,3361.346666666667 2672.4933333333333,3361.3333333333335 2673.6666666666665,3361.32 2674.84,3361.306666666667 2676.0133333333333,3361.2933333333335 2677.1866666666665,3361.28 2678.36,3361.266666666667 2679.5333333333333,3361.2533333333336 2680.7066666666665,3361.24 2681.88,3361.2266666666665 2683.0533333333333,3361.213333333333 2684.2266666666665,3361.2 2685.4,3361.1866666666665 2686.5733333333333,3361.173333333333 2687.7466666666664,3361.16 2688.92,3361.1466666666665 2690.0933333333332,3361.133333333333 2691.266666666667,3361.12 2692.44,3361.1066666666666 2693.6133333333332,3361.0933333333332 2694.786666666667,3361.08 2695.96,3361.0666666666666 2697.133333333333,3361.0533333333333 2698.306666666667,3361.04 2699.48,3361.0266666666666 2700.653333333333,3361.0133333333333 2701.826666666667,3361.0 2703.0,3360.5 2703.5,3360.0 2704.0))");
		System.out.println(p);
		Polygon poly= new Polygon();

		for(int i=0;i<p.getNumPoints();i++){
			poly.addPoint((int)p.getCoordinates()[i].x, (int)p.getCoordinates()[i].y);
		}

		DateTime startDate = new DateTime();
		startDate=DateTime.parse("2013-10-15T00:01:36.000-04:00");
		DateTime endDate=startDate.plusHours(1);
		Interval inter= new Interval(startDate, endDate) ;

		Point2D pt=new Point2D(p.getCentroid().getX(),p.getCentroid().getY());
		Rectangle bbox=poly.getBoundingBox();

		//p.getEnvelope().getCoordinates()[0].distance(p.getEnvelope().getCoordinates()[1]);
		IEvent ev= new  GenericEvent(0, inter, pt, bbox, poly, EventType.ACTIVE_REGION);
		//////////////////////////////////////////////////////////////////////////////////			
		com.vividsolutions.jts.geom.Polygon p1= (com.vividsolutions.jts.geom.Polygon) reader.read("POLYGON(( 2028 1512, 2327 1516, 2314 1284, 2029 1281, 2028 1512))");
		Polygon poly1= new Polygon();

		for(int i=0;i<p1.getNumPoints();i++){
			poly1.addPoint((int)p1.getCoordinates()[i].x, (int)p1.getCoordinates()[i].y);
		}

		DateTime startDate1 = new DateTime();
		startDate1=DateTime.parse("2013-10-15T08:01:36.000-04:00");
		endDate= startDate1.plusHours(1);
		Interval inter1= new Interval(startDate1, endDate) ;

		Point2D pt1=new Point2D(p1.getCentroid().getX(),p1.getCentroid().getY());
		Rectangle bbox1=poly1.getBoundingBox();


		IEvent ev1= new  GenericEvent(1, inter1, pt1, bbox1, poly1, EventType.ACTIVE_REGION);
		////////////////////////////////////////////////////////////////
		com.vividsolutions.jts.geom.Polygon p5= (com.vividsolutions.jts.geom.Polygon) reader.read("POLYGON(( 1436 1766, 1535 1762, 1537 1695, 1438 1699, 1436 1766))");
		Polygon poly5= new Polygon();
		
		for(int i=0;i<p5.getNumPoints();i++){
			if(i==p5.getNumPoints()-1) poly5.addPoint((int)(p5.getCoordinates()[i].x), (int)(p5.getCoordinates()[i].y));
			else poly5.addPoint((int)(p5.getCoordinates()[i].x), (int)(p5.getCoordinates()[i].y));			
         }
		
		for(int i=0;i<poly5.npoints;i++){
			System.out.println(poly5.xpoints[i]+" "+poly5.ypoints[i]+" ,");			
         }
		
		
		DateTime startDate5 = new DateTime();
		startDate5=DateTime.parse("2014-05-20T18:56:55.000-04:00");
		DateTime endate= startDate5.plusHours(1);
	    Interval inter5= new Interval(startDate5, endate) ;

		Point2D pt5=new Point2D(p5.getCentroid().getX(),p5.getCentroid().getY());
		Rectangle bbox5=poly5.getBoundingBox();


		IEvent ev2= new  GenericEvent(2, inter5, pt5, bbox5, poly5, EventType.ACTIVE_REGION);

		ArrayList<IEvent> trr= new ArrayList<IEvent>();
		ArrayList<IEvent> trr2= new ArrayList<IEvent>();

		trr.add(ev);
		trr.add(ev1);
		trr2.add(ev2);

		ITrack tr = new Track(trr);
		ITrack tr2 = new Track(trr2);

		ArrayList<ITrack> TRArray = new ArrayList<ITrack>();
		TRArray.add(tr);
		TRArray.add(tr2);
	///////////////////////////////////////////////////////////////////////////

				ArrayList<ArrayList<com.vividsolutions.jts.geom.Polygon>> TrackPols =new ArrayList<ArrayList<com.vividsolutions.jts.geom.Polygon>>();
				ArrayList<ArrayList<DateTime>> TrackTime =new ArrayList<ArrayList<DateTime>>();
				ArrayList<DateTime> tempDate =new ArrayList<>();
				ArrayList<com.vividsolutions.jts.geom.Polygon> temp =new ArrayList<>();
				com.vividsolutions.jts.geom.Polygon polygons[] = new  com.vividsolutions.jts.geom.Polygon[2];
				com.vividsolutions.jts.geom.Polygon[] Densified_polygons= new  com.vividsolutions.jts.geom.Polygon[2];
				
				ArrayList<double[]> TS= new ArrayList<>();
				
				 Connection c = null;
			      try {
			       //  Class.forName("org.postgresql.Driver");
			         c = DriverManager
			            .getConnection("jdbc:postgresql://localhost:5433/spoca",
			            "postgres", "root");
			      } catch (Exception e) {
			         e.printStackTrace();
			         System.err.println(e.getClass().getName()+": "+e.getMessage());
			         System.exit(0);
			      }
			      System.out.println("Opened database successfully");

			     int choice=10;
			      String sql = "select * from arin limit"+choice;
			      Statement stmt = c.createStatement();
			      ResultSet rs = stmt.executeQuery(sql);
			      	      
			      int i=0;
			      int old_trnum=0;
			      int count=0;
			      
			      //STEP 5: Extract data from result set
			      while(rs.next()){
			         //Retrieve by column name
			       //  int arid  = rs.getInt("arid");
			         int trnum = rs.getInt("trnum");
			         String dt = rs.getString("datetime");
			         DateTime datetime= DateTime.parse(dt.substring(0,29));
			         System.out.println("THE TIME IS"+datetime);
			         String wkbString = rs.getString("pol");
			         byte[] aux = WKBReader.hexToBytes(wkbString);
			         Geometry geom = new WKBReader().read(aux);	
			         
			         
			         String wktString = geom.toText();
			        // geometry in WKT format
			         System.out.printf("trnum = "+trnum);
			         System.out.printf("WKT = %s\n", wktString);
			          gf= new GeometryFactory();
			          reader = new WKTReader(gf);
			        // System.out.println("trnum is"+trnum+"old trnum"+old_trnum);
			         if(old_trnum==trnum){
			         temp.add((com.vividsolutions.jts.geom.Polygon) reader.read(wktString));
			         tempDate.add(datetime);
			         	if(count==(choice-1)){ TrackPols.add(temp);
			         	TrackTime.add(tempDate);         		
			         	}
			         	
			         }
			         else{
			        	 TrackPols.add(temp);
			        	 TrackTime.add(tempDate);
			        	 
			        	 temp= new ArrayList<>();
			        	 tempDate= new ArrayList<>();
			        	 
			        	 temp.add((com.vividsolutions.jts.geom.Polygon) reader.read(wktString));
			        	 tempDate.add(datetime);
			         }
			         count++;
			         old_trnum=trnum;
			            }//end sql sca
		*/
		///////////////////////////////////////////////////////////////////////////
	//Displays images
		
		ArrayList<ITrack> Tracks=new ArrayList<ITrack>();
		ArrayList<IEvent> ArratEvents=new ArrayList<IEvent>();		
		IEvent temp = null;

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

	//     int choice=10;
	      String sql = "Select * from ssin order by id asc limit 500";
	      Statement stmt = c.createStatement();
	      ResultSet rs = stmt.executeQuery(sql);
//		  int rowcounts=getRowCount(rs);
	//	  System.out.println("THE NUM OF ROWS IS "+rowcounts);

      	      
	      int i=0;
	      int old_trnum=0;
	      int count=0;
	      
	      //STEP 5: Extract data from result set
	      while(rs.next()){
	         //Retrieve by column name
	         int id  = rs.getInt("id");
	         int trnum = rs.getInt("trnum");
	        // System.out.println(trnum);
	         int original = rs.getInt("duration");
	         String dt = rs.getString("datetime");
	         DateTime datetime= DateTime.parse(dt.substring(0,29));
	         DateTime endtime=datetime.plusHours(1);
	         Interval timeperiod= new Interval(datetime,endtime);
	         
	         
	     //    System.out.println("THE TIME IS"+datetime);
	         String wkbString = rs.getString("pol");
	         String TrajectoryLabel = rs.getString("trajectorylabel");
	       //  String dt = rs.getString("datetime");
	         byte[] aux = WKBReader.hexToBytes(wkbString);
	         Geometry geom = new WKBReader().read(aux);	
         
	         String wktString = geom.toText();
	        // geometry in WKT format
	    //     System.out.printf("trnum = "+trnum);
	    //     System.out.printf("WKT = %s\n", wktString);
	         GeometryFactory gf= new GeometryFactory();
	         WKTReader reader = new WKTReader(gf);
	         com.vividsolutions.jts.geom.Polygon p=(com.vividsolutions.jts.geom.Polygon) reader.read(wktString);
	         Point2D pt= new Point2D(p.getCentroid().getX(),p.getCentroid().getY());
	 		 java.awt.Polygon poly= new java.awt.Polygon();
			for(int j=0;j<p.getNumPoints();j++){
				poly.addPoint((int)p.getCoordinates()[j].x, (int)p.getCoordinates()[j].y);
			}
			
			Rectangle bbox=poly.getBoundingBox();
	         
	        // System.out.println("trnum is"+trnum+"old trnum"+old_trnum);
	         if(old_trnum==trnum||rs.isFirst()){
	         temp= new GenericEvent(id,timeperiod,pt,bbox,poly,EventType.SUNSPOT,original,TrajectoryLabel);
	         ArratEvents.add(temp);
	         count++;	
	      //   System.out.println("\nIF ORIGINAL"+original);
	         	if(rs.isLast()){ 
	                ITrack track= new Track(ArratEvents);
		            Tracks.add(track);
	            	}	         	
	         }
	         else{
	         //    System.out.println("\nORIGINAL"+original);
	             count++;
	             if(count!=0){
	             ITrack track= new Track(ArratEvents);
	             Tracks.add(track);
	             
	             ArratEvents=new ArrayList<IEvent>();
	             }
		         temp= new GenericEvent(id,timeperiod,pt,bbox,poly,EventType.SUNSPOT,original,TrajectoryLabel);
		         ArratEvents.add(temp);
	         }
	        // count++;
	         old_trnum=trnum;
	         ////////////////////////////////////////////////////////////////////////
	            }
	      System.out.println(count);
	       System.out.println(Tracks.size());
	       System.out.println("track length"+Tracks.get(0).size());
		
		int wavelength=171;
		SeqDisp disp = new SeqDisp(imageDB, Tracks,wavelength);
		disp.start();

	}
}
