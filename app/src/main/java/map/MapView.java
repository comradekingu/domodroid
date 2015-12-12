package map;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.domogik.domodroid13.R;

import rinor.Rest_com;

import database.Cache_management;
import database.DmdContentProvider;
import database.DomodroidDB;
import database.JSONParser;
import database.WidgetUpdate;
import activities.Activity_Map;
import activities.Graphics_Manager;
import activities.Sliding_Drawer;
import widgets.Entity_Map;
import widgets.Entity_client;
import widgets.Graphical_Binary;
import widgets.Graphical_Binary_New;
import widgets.Graphical_Boolean;
import widgets.Graphical_Cam;
import widgets.Graphical_Color;
import widgets.Graphical_History;
import widgets.Graphical_Info;
import widgets.Graphical_Info_commands;
import widgets.Graphical_Info_with_achartengine;
import widgets.Graphical_List;
import widgets.Graphical_Range;
import widgets.Graphical_Trigger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MapView extends View {
	private Builder list_type_choice;
	private Bitmap map;
	private Bitmap widget;
	private Bitmap drawable;
	public int width;
	public int height;
	private Canvas canvasMap;
	private Canvas canvasWidget;
	private TransformManager mat;
	private Matrix origin;
	private SVG svg;
	float currentScale = 1;
	float currentScalewidth = 1;
	float currentScaleheight = 1;
	private int screenwidth;
	private int screenheight;
	private int widgetSize;
	private boolean addMode=false;
	private boolean removeMode=false;
	private boolean moveMode=false;
	private int update;
	private static int text_Offset_X;
	private static int text_Offset_Y;
	private int moves;
	private SharedPreferences.Editor prefEditor;
	private boolean map_autozoom=false;
	public int temp_id;
	public int map_id;
	public final String map_name = "";

	private Paint paint_map;
	private Paint paint_text;
	private ViewGroup panel_widget;
	private ViewGroup panel_button;
	private final Activity context;
	private Sliding_Drawer top_drawer;
	private Sliding_Drawer bottom_drawer;

	private Graphical_Trigger trigger;
	private Graphical_Range variator;
	private Graphical_Binary onoff;
	private Graphical_Binary_New onoff_New;
	private Graphical_Info info;
	private Graphical_Info_commands info_commands;
	private Graphical_History info_with_history;
	private Graphical_Info_with_achartengine info1;
	private Graphical_Boolean bool;
	private Graphical_Color colorw;
	private Graphical_List list;
	private Graphical_Cam cam;
	private Graphical_Color color;

	private Vector<String> files;
	private Entity_Map[] listFeatureMap;
	private Entity_Map[] listMapSwitches;
	private int mode;
	private int formatMode;
	private String svg_string;
	private int currentFile = 0;
	private String type;
	private SharedPreferences params;
	private final float api_version;

	private float pos_X0=0;
	private float pos_X1=0;

	private final int screen_width;
	//private Boolean activated; 
	private final String mytag="MapView";
	private Boolean locked = false;
	private String parameters;
	private int valueMin;
	private int valueMax;
	private String value0;
	private String value1;
	private String Value_0;
	private String Value_1;
	private static Handler handler = null;
	final Handler handler_longclic = new Handler(); 
	private tracerengine Tracer = null;
	private final int mytype = 2;
	private WidgetUpdate cache_engine = null;
	private final float texsize = 14;
	private final float scale;
	private final String login;
	private final String password;
	private String Address;
	private String URL;
	private String state_progress;
	//Declare this flag globally
	boolean longclic = false;
	private MotionEvent event1;
	private float[] valuelongclic = new float[9];
	private String command_id = null;
	private String command_type = null;
	private final float dip20;

	public MapView(tracerengine Trac, Activity context, SharedPreferences params) {
		super(context);
		this.Tracer = Trac;
		this.context=context;
		this.params=params;
		api_version=params.getFloat("API_VERSION", 0);
		login = params.getString("http_auth_username",null);
		password = params.getString("http_auth_password",null);

		//activated=true;
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		screen_width = metrics.widthPixels;
		scale = getContext().getResources().getDisplayMetrics().density;
		Resources r = getResources();
		dip20 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, r.getDisplayMetrics());
		text_Offset_X= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());

		startCacheEngine();
		/*
		 * This view has only one handler for all mini widgets displayed on map
		 * It'll receive a unique notification from WidgetUpdate when one or more values have changed
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 9997) {
					//state_engine send us a signal to notify at least one value changed
					Tracer.d(mytag,"state engine notify change for mini widget(s) : refresh all of them !" );
					for (Entity_Map featureMap : listFeatureMap) {
						// if a miniwidget was connected to engine, session's value could have changed....
						if(featureMap.getSession() != null) {
							featureMap.setCurrentState(featureMap.getSession().getValue());
						} 

					}
					refreshMap();

				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );

					try {
						finalize();
					} catch (Throwable t) {
						Tracer.d(mytag,"Could not finished" );
					}


				}
			}	
		};


		//End of create method ///////////////////////

	}

	private void startCacheEngine() {

		if(cache_engine == null) {
			Tracer.w(mytag, "Starting WidgetUpdate engine !");
			cache_engine = WidgetUpdate.getInstance();
			//MapView is'nt the first caller, so init is'nt required (already done by View)
			cache_engine.set_handler(handler, mytype);	//Put our main handler to cache engine (as MapView)
		}  
		Tracer.set_engine(cache_engine);
		Tracer.w(mytag, "WidgetUpdate engine connected !");
	}

	public void purge() {
		// TODO We've to unsubscribe all connected mini widgets from cache engine
	}

	public void  onWindowVisibilityChanged (int visibility) {
		Tracer.i(mytag,"Visibility changed to : "+visibility);
		/*
		if(visibility == View.VISIBLE)
			//this.activated = true;
		else
			//activated=false;
		 */
	}

	public void clear_Widgets(){
		String map_name=files.elementAt(currentFile);
		Tracer.i(mytag,"Request to clear all widgets from : "+map_name);
		Tracer.get_engine().cleanFeatureMap(map_name);
		//All device as been delete re-check the cache URL
		Cache_management.checkcache(Tracer,context);
		initMap();
	}

	public void removefile(){
		//remove the current file
		File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile));
		Tracer.i(mytag,"Request to remove "+currentFile);
		f.delete();
		//remove feature of this map in table_feature_map
		Tracer.get_engine().cleanFeatureMap(map_name);
		//All device on this map as been delete re-check the cache URL
		Cache_management.checkcache(Tracer,context);
		initMap();		
	}

	public void initMap(){
		Toast.makeText(context, files.elementAt(currentFile).substring(0,files.elementAt(currentFile).lastIndexOf('.')), Toast.LENGTH_SHORT).show();

		//listFeatureMap = domodb.requestFeatures(files.elementAt(currentFile));
		listFeatureMap = Tracer.get_engine().getMapFeaturesList(files.elementAt(currentFile));
		listMapSwitches = Tracer.get_engine().getMapSwitchesList(files.elementAt(currentFile));

		//Each real mini widget must be connected to cache engine, to receive notifications
		for (Entity_Map featureMap : listFeatureMap) {
			Entity_client cursession=null;
			if (api_version<=0.6f){
				cursession = new Entity_client(
						featureMap.getDevId(),
						featureMap.getState_key(), 
						"mini widget",
						handler,
						mytype);
				cursession.setType(true);	//It's a mini widget !
			} else if (api_version>=0.7f){
				cursession = new Entity_client(
						featureMap.getId(),
						"", 
						"mini widget",
						handler,
						mytype);
				cursession.setType(true);	//It's a mini widget !
			}

			if(Tracer.get_engine().subscribe(cursession) ) {
				//This widget is connected to state_engine
				featureMap.setSession(cursession);
				featureMap.setCurrentState(cursession.getValue());
			} else {
				// cannot connect it ????
				Tracer.i(mytag,"Cannot connect mini widget to state engine : ("+cursession.getDevId()+") ("+cursession.getskey()+") => it'll not be updated !");
				featureMap.setCurrentState("????");
			}

		}
		//get file extension
		String extension = files.elementAt(currentFile).substring(files.elementAt(currentFile).lastIndexOf('.'));
		//put extension in lowercase
		extension=extension.toLowerCase();

		if(extension.equals(".svg")){
			formatMode=1;
			//Try to allow PNG and png extension to solve #1707 on irc tracker.
			//Could also try to put all in lowercase: files.elementAt(currentFile).substring(files.elementAt(currentFile).toLowerCase()......
		}else if(extension.equals(".png")||extension.equals(".jpg")||extension.equals(".jepg")){ 
			formatMode=2;
		}else{
			formatMode=0;
		}

		//Load current scale if it exists.
		if(params.getFloat("Mapscale", 1)!=1){
			currentScale=params.getFloat("Mapscale", 1);
		}
		map_autozoom=params.getBoolean("map_autozoom", false);
		origin = new Matrix();
		mat = new TransformManager();
		//mat.setZoom(params.getBoolean("ZOOM", false));
		//mat.setDrag(params.getBoolean("DRAG", false));
		//TODO try to solve drag and zoom problem.
		mat.setZoom(false);
		mat.setDrag(false);

		mat.setScreenConfigScaling();
		paint_text = new Paint();
		paint_text.setPathEffect(null);
		paint_text.setAntiAlias(true);
		paint_text.setStyle(Paint.Style.FILL_AND_STROKE);
		paint_text.setColor(Color.WHITE);
		paint_text.setShadowLayer(1, 0, 0, Color.BLACK);

		//Get screen size
		DisplayMetrics metrics = new DisplayMetrics();
		//context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		metrics = getContext().getResources().getDisplayMetrics();
		screenwidth= metrics.widthPixels;
		screenheight= metrics.heightPixels;

		//Case using a svg file as map
		if(formatMode==1){	
			try{

				File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
				svg_string = getFileAsString(f);
				svg = SVGParser.getSVGFromString(svg_string);
				//adjust to scale
				if (map_autozoom==true){
					currentScale=autoscale((int)svg.getSurfaceWidth(),(int)svg.getSurfaceHeight());
				}
				svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
				Picture picture = svg.getPicture();
				map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
				canvasMap = new Canvas(map);
				canvasMap.drawPicture(picture);
				widget = Bitmap.createBitmap((int)((svg.getSurfaceWidth()+screen_width)*currentScale), (int)((svg.getSurfaceHeight()+screenheight)*currentScale), Bitmap.Config.ARGB_8888);
				canvasWidget = new Canvas(widget);

			}catch (Exception e) {
				Tracer.e(mytag+" initmap()","formatMode=1 "+e.getStackTrace().toString());
				return;
			}
			//Case using a png file as map
		}else if(formatMode==2){
			try{

				File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
				Bitmap bitmap = decodeFile(f);
				//adjust to scale
				if (map_autozoom==true){
					currentScale=autoscale(bitmap.getWidth(),bitmap.getHeight());
				}
				map = Bitmap.createBitmap((int)(bitmap.getWidth()*currentScale), (int)(bitmap.getHeight()*currentScale), Bitmap.Config.ARGB_4444);
				canvasMap = new Canvas(map);
				Matrix matScale = new Matrix();
				matScale.postScale(currentScale, currentScale);
				origin = canvasMap.getMatrix();
				origin.postConcat(mat.matrix);
				canvasMap.setMatrix(matScale);
				canvasMap.drawBitmap(bitmap, 0, 0, paint_map );
				widget = Bitmap.createBitmap((int)((bitmap.getWidth()+screen_width)*currentScale), (int)((bitmap.getHeight()+screenheight)*currentScale), Bitmap.Config.ARGB_8888);
				canvasWidget = new Canvas(widget);

			}catch (Exception e) {
				Tracer.e(mytag+" initmap()","formatMode=2 "+e.getStackTrace().toString());
				return;
			}
		}
		drawWidgets();

		postInvalidate();
	}

	public void refreshMap(){
		canvasMap=null;
		canvasWidget=null;
		System.gc();	//Run garbage collector to free maximum of memory

		//Case using a svg file as map
		if(formatMode==1){
			try{

				File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
				svg_string = getFileAsString(f);
				svg = SVGParser.getSVGFromString(svg_string);
				//adjust to scale
				if (map_autozoom==true){
					currentScale=autoscale((int)svg.getSurfaceWidth(),(int)svg.getSurfaceHeight());
				}
				svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
				Picture picture = svg.getPicture();
				map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
				canvasMap = new Canvas(map);
				canvasMap.drawPicture(picture);	
				widget = Bitmap.createBitmap((int)((svg.getSurfaceWidth()+screen_width)*currentScale), (int)((svg.getSurfaceHeight()+screenheight)*currentScale), Bitmap.Config.ARGB_8888);
				canvasWidget = new Canvas(widget);

			}catch (Exception e) {
				Tracer.e(mytag+" refreshmap()","formatMode=1 "+e.getStackTrace().toString());
				return;
			}
			//Case using a png file as map
		}else if(formatMode==2){
			try{

				File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
				Bitmap bitmap = decodeFile(f);
				//adjust to scale
				if (map_autozoom==true){
					currentScale=autoscale(bitmap.getWidth(),bitmap.getHeight());
				}
				map = Bitmap.createBitmap((int)(bitmap.getWidth()*currentScale), (int)(bitmap.getHeight()*currentScale), Bitmap.Config.ARGB_4444);
				canvasMap = new Canvas(map);
				Matrix matScale = new Matrix();
				matScale.postScale(currentScale, currentScale);
				canvasMap.setMatrix(matScale);
				canvasMap.drawBitmap(bitmap, 0, 0, paint_map );
				Tracer.e(mytag, "Trying to create widget at scale : "+currentScale);
				widget = Bitmap.createBitmap((int)((bitmap.getWidth()+screen_width)*currentScale), (int)((bitmap.getHeight()+screenheight)*currentScale), Bitmap.Config.ARGB_8888);
				canvasWidget = new Canvas(widget);

			}catch (Exception e) {
				Tracer.e(mytag+" refreshmap()","formatMode=2 "+e.getStackTrace().toString());
				return;

			}
		}

		drawWidgets();
		postInvalidate();
	}

	public void drawWidgets(){
		if(locked) {
			return;
		}
		locked=true;
		int id = 0;
		// first try to process map switches, if any present in this map
		Tracer.e(mytag, "Processing map switches widgets list");

		if(listMapSwitches != null) {

			for (Entity_Map switchesMap : listMapSwitches) {
				id=switchesMap.getId();

				//Its a map switch widget
				id = id - 99999;
				if( (id >= 0 ) && (id < files.size()) ) {
					String mapname = files.elementAt(id);
					Tracer.e(mytag, "Processing switch to map <"+mapname+">");
					// Draw symbol of 'map_next'
					try {

						drawable = BitmapFactory.decodeResource(getResources(), R.drawable.map_next);
						if(drawable != null) {
							canvasWidget.drawBitmap(drawable, 
									(switchesMap.getPosx()*currentScale)-drawable.getWidth()/2, 
									(switchesMap.getPosy()*currentScale)-drawable.getWidth()/2, 
									paint_map);
						} else {
							Tracer.e(mytag,"No drawable available for map switch");
							return;
						}

					} catch (Exception e) {
						Tracer.e(mytag,"cannot draw map switch icon ! ! ! !");
						return;
					}

					//Draw the map name text
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f);
						canvasWidget.drawText(mapname, 
								(switchesMap.getPosx()*currentScale)+text_Offset_X, 
								(switchesMap.getPosy()*currentScale)+text_Offset_Y, 
								paint_text);
					}
				}
			}
		}
		// And now process real widgets
		Tracer.e(mytag, "Processing normal widgets list");

		for (Entity_Map featureMap : listFeatureMap) {

			String states = "";
			JSONObject jparam;

			if(featureMap != null) {
				states = featureMap.getCurrentState();
			} else {
				Tracer.e(mytag,"Wrong feature in featureMap list ! ! ! Abort processing !");
				return;
			}

			if(featureMap.isalive()) {
				//set intstate to select correct icon color
				int intstate = 0;
				if(! (featureMap.getState_key().equals("color"))) {
					//get parameters valuemin,max, 0 and 1
					parameters=featureMap.getParameters();
					try {
						jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
						value1 = jparam.getString("value1");
						value0 = jparam.getString("value0");
					} catch (Exception e) {
						Tracer.i(mytag, "No value for parameters 0/1");
						value0 = "0";
						value1 = "1";
					}		
					try {
						jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
						valueMin = jparam.getInt("valueMin");
						valueMax = jparam.getInt("valueMax");
					} catch (JSONException e1) {
						//e1.printStackTrace();
						//Tracer.e(mytag,"DrawWidget No parameters ! ");
						//Tracer.e(mytag,"DrawWidget   for mini widget type <"+featureMap.getValue_type()+">");

						//TODO : what to put into value0, 1, min & max ?
					}
					//Tracer.e(mytag,"DrawWidget value0  <"+value0+"> value1 <"+value1+"> valueMin <"+valueMin+"> valueMax <"+valueMax+">");
				}
				if (states != "????"){
					if ((states.equals(value1))||(states.equals("1")) ||((featureMap.getValue_type().equals("range") && (Integer.parseInt(states)>valueMin)))){
						//if ((states.equals("high")) || (states.equals("on") || ((featureMap.getValue_type().equals("range") && (Integer.parseInt(states)>0)))))
						intstate=1;
					}
					//set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
					featureMap.setState(intstate);
				}

				try {
					// Draw symbol of feature
					drawable = BitmapFactory.decodeResource(getResources(), featureMap.getRessources());
					if(drawable != null) {
						canvasWidget.drawBitmap(drawable, 
								(featureMap.getPosx()*currentScale)-drawable.getWidth()/2, 
								(featureMap.getPosy()*currentScale)-drawable.getWidth()/2, 
								paint_map);
						Tracer.i(mytag,"Draw symbol of feature X="+((featureMap.getPosx()*currentScale)-drawable.getWidth()/2)+
								" Y="+((featureMap.getPosy()*currentScale)-drawable.getWidth()/2)+" MAP "+paint_map);
					} else {
						Tracer.e(mytag,"No drawable available for object");
						return;
					}
				} catch (Exception e) {
					Tracer.e(mytag,"cannot draw object ! ! ! !");
					return;
				}

				// Draw state and description
				//TODO add missing datatype from 0.4
				//String but carreful
				//datetime done
				//ColorCII
				//Char
				//DayOfWeek
				//HVACVent
				//HVACFan
				//HVACMode
				//HVACHeat
				//UPSEvent
				//UPSState
				//TODO #48 grab label from diverse place:
				String label = featureMap.getDescription();
				if(label.length() < 1||label.equalsIgnoreCase("null"))
					label = featureMap.getName();
				//TODO grab value from State and translate it
				String value=featureMap.getCurrentState();
				if(parameters.contains("command"))
					value="command";
				if (value.equals("0"))
					value=value0;
				if (value.equals("1"))
					value=value1;
				try {
					Tracer.d(mytag,"Try to get value translate from R.STRING" );
					value=context.getString((Graphics_Manager.getStringIdentifier(getContext(),
							value.toLowerCase())));
				}catch (Exception e1) {
					Tracer.d(mytag,"Nothing in R.STRING for "+value );
				}
				if (value=="????")
					value="";

				if((featureMap.getValue_type().equals("string") && (! featureMap.getState_key().equals("color")))
						||featureMap.getValue_type().equals("datetime")){
					if(! featureMap.getDevice_feature_model_id().contains("camera")) {
						for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(texsize* scale + 0.5f-2);
							canvasWidget.drawText(value, 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y, 
									paint_text);
							if (params.getBoolean("HIDE",false)==false)
								canvasWidget.drawText(label,
										(featureMap.getPosx()*currentScale)+text_Offset_X,
										(featureMap.getPosy()*currentScale)+text_Offset_Y+(15*(int)scale),
										paint_text);
						}
					}
					else if(featureMap.getDevice_feature_model_id().contains("camera")) {
						for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(texsize* scale + 0.5f-2);
							if (params.getBoolean("HIDE",false)==false)
								canvasWidget.drawText(label, 
										(featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y+(15*(int)scale),
										paint_text);

						}
					}


				} else if(featureMap.getValue_type().equals("binary") || featureMap.getValue_type().equals("boolean")
						|| featureMap.getValue_type().equals("bool")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f-2);
						if (params.getBoolean("HIDE",false)==false){ 
							canvasWidget.drawText(value,
									(featureMap.getPosx()*currentScale)+text_Offset_X,
									(featureMap.getPosy()*currentScale)+text_Offset_Y,
									paint_text);
							canvasWidget.drawText(label,
									(featureMap.getPosx()*currentScale)+text_Offset_X,
									(featureMap.getPosy()*currentScale)+text_Offset_Y+(15*(int)scale),
									paint_text);
						}
					}

				} else if(featureMap.getValue_type().equals("number")){
					float formatedValue = 0;
					if(value != null && value != ""){
						formatedValue = Round(Float.parseFloat(value),2);
						Tracer.v(mytag, " Round the value"+value+" to "+formatedValue);

						try {
							//Basilic add, number feature has a unit parameter
							jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
							String test_unite = jparam.getString("unit");
							//# 30 convert byte unit.
							if (test_unite.equals("b") == true){
								value=android.text.format.Formatter.formatFileSize(context,Long.parseLong(value));
							}else if (test_unite.equals("ko") == true){
								value=android.text.format.Formatter.formatFileSize(context,Long.parseLong(value)*1024);
							}else{
								value=formatedValue+ " "+test_unite;
							}
						} catch (JSONException e) {				
							//Basilic : no sure that the key state was the better way to find unit
							if(featureMap.getState_key().equals("temperature"))value=featureMap.getCurrentState()+" °C";
							else if(featureMap.getState_key().equals("pressure"))value=featureMap.getCurrentState()+" hPa";
							else if(featureMap.getState_key().equals("humidity"))value=featureMap.getCurrentState()+" %";
							else if(featureMap.getState_key().equals("percent"))value=featureMap.getCurrentState()+" %";
							else if(featureMap.getState_key().equals("visibility"))value=featureMap.getCurrentState()+" km";
							else if(featureMap.getState_key().equals("chill"))value=featureMap.getCurrentState()+" °C";
							else if(featureMap.getState_key().equals("speed"))value=featureMap.getCurrentState()+" km/h";
							else if(featureMap.getState_key().equals("drewpoint"))value=featureMap.getCurrentState()+" °C";
							else if( (featureMap.getState_key().equals("condition-code")))
								//Add try catch to avoid other case that make #1794
								try {
									value=context.getString(Graphics_Manager.Names_conditioncodes(getContext(),Integer.parseInt(featureMap.getCurrentState())));
								}catch (Exception e1) {

								}

						}
					}
					if(value == null)
						value = "";

					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f+4);
						if(featureMap != null) {
							//Tracer.e(mytag,"Drawing value for "+label+"Value = "+value+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
							canvasWidget.drawText(value,
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y-(10*(int)scale), 
									paint_text);
							paint_text.setTextSize(texsize* scale + 0.5f-1);
							//Tracer.e(mytag,"Drawing label "+label+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
							if (params.getBoolean("HIDE",false)==false) 
								canvasWidget.drawText(label,
										(featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y+(6*(int)scale), 
										paint_text);
						}
					}
				} else if(featureMap.getValue_type().equals("range")||((parameters.contains("command"))&&(featureMap.getDevice_feature_model_id().startsWith("DT_Scaling")))){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f-2);
						if (params.getBoolean("HIDE",false)==false){ 
							//TODO see if we should not use label instead of featureMap.getDevice_usage_id()
							//It is not the same text displayed for this type of device
							canvasWidget.drawText(featureMap.getCurrentState(), 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y, 
									paint_text);
							canvasWidget.drawText(label, 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y+(15*(int)scale), 
									paint_text);
							//Tracer.e(mytag,"Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						}else{
							if  (featureMap.getState_key().equals("light")){
								if (Integer.parseInt(featureMap.getCurrentState()) > valueMin){
									canvasWidget.drawText(featureMap.getCurrentState(),
											(featureMap.getPosx()*currentScale)+text_Offset_X,
											(featureMap.getPosy()*currentScale)+text_Offset_Y,
											paint_text);
								}
							}else{
								canvasWidget.drawText(featureMap.getCurrentState(),
										(featureMap.getPosx()*currentScale)+text_Offset_X,
										(featureMap.getPosy()*currentScale)+text_Offset_Y,
										paint_text);
							}
						}
					}

				}else if(featureMap.getValue_type().equals("trigger")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f-2);
						if (params.getBoolean("HIDE",false)==false){
							if (parameters.contains("command"))
								canvasWidget.drawText(value,
										(featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y, 
										paint_text);
							canvasWidget.drawText(label, 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y+(15*(int)scale), 
									paint_text);
						}
					}
				}else if(featureMap.getState_key().equals("color")){
					Tracer.e(mytag,"Drawing color for "+featureMap.getName()+" Value = "+states);
					Paint paint_color = new Paint();
					paint_color.setPathEffect(null);
					paint_color.setAntiAlias(true);
					//paint_color.setStyle(Paint.Style.FILL_AND_STROKE);
					paint_color.setStyle(Paint.Style.FILL);
					String argbS = states;
					//Process RGB value
					if(states.equals("off")) {
						argbS="#000000";
					} else if(argbS.equals("on")) {
						argbS=params.getString("COLORRGB", "#FFFFFF");	//Restore last known color, White by default
					} else{
						//To avoid http://tracker.domogik.org/issues/1972 here
						argbS="#FFFFFF";
					}
					//Tracer.e(mytag,"Drawing color for "+featureMap.getName()+" RGB Value = "+Integer.toHexString(loc_argb));
					//Draw first a black background...
					paint_color.setColor(Color.BLACK);
					paint_color.setShadowLayer(1, 0, 0, Color.BLACK);
					int left = (int)(featureMap.getPosx()*currentScale)+text_Offset_X-(10*(int)scale);
					int top  = (int)(featureMap.getPosy()*currentScale)+text_Offset_Y-(15*(int)scale);
					int right= (int)(featureMap.getPosx()*currentScale)+text_Offset_X+(85*(int)scale);
					int bottom=(int)(featureMap.getPosy()*currentScale)+text_Offset_Y+(10*(int)scale);
					Rect r = new Rect(left,top,right,bottom);
					canvasWidget.drawRect(r, paint_color);

					//And draw real color inside the 1st one

					paint_color.setColor(Color.parseColor(argbS));
					left+=3;
					top+=3;
					right-=3;
					bottom-=3;
					r = new Rect(left,top,right,bottom);
					canvasWidget.drawRect(r, paint_color);
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(texsize* scale + 0.5f-2);
						if (params.getBoolean("HIDE",false)==false)
							canvasWidget.drawText(label, 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y+(25*(int)scale), 
									paint_text);
					}
				}
			} else {
				// This widget is'nt alive anymore...
				Tracer.e(mytag,"Could not draw "+featureMap.getId());
				canvasWidget = null; //?????
			}

		}
		locked=false;

	}	

	public void showTopWidget(Entity_Map feature) throws JSONException{
		Tracer.d(mytag, "Show top Widget");
		DomodroidDB domodb = new DomodroidDB(Tracer, context);
		domodb.owner="MapView.showTopWidgets";
		if(panel_widget.getChildCount()!=0){
			panel_widget.removeAllViews();
		}
		String label = feature.getDescription();
		if(label.length() < 1||label.equalsIgnoreCase("null"))
			label = feature.getName();


		String URL = params.getString("URL","1.1.1.1");
		String parameters = feature.getParameters();
		String device_type_id = feature.getDevice_type_id();
		String State_key = feature.getState_key();
		String Address = feature.getAddress();
		String zone = "";
		int Graph = params.getInt("GRAPH",3);
		int update_timer = params.getInt("UPDATE_TIMER",300);
		int DevId = feature.getDevId();
		int Id = feature.getId();

		String iconName = "unknow";
		try {
			iconName = domodb.requestIcons(Id, "feature").getValue().toString();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if (iconName=="unknow")
			iconName=feature.getDevice_usage_id();

		//add debug option to change label adding its Id
		if (params.getBoolean("DEV",false)==true)
			label = label+" ("+DevId+")";

		String[] model = device_type_id.split("\\.");
		String type ="";
		try{
			type = model[1];
		}catch (Exception e){
			type= model[0];
		}


		if (feature.getValue_type().equals("binary")) {
			if(type.equals("rgb_leds") && (State_key.equals("command"))) {
				//ignore it : it'll have another device for Color, displaying the switch !)
			} else {
				if (params.getBoolean("WIDGET_CHOICE",false)==false) {
					onoff = new Graphical_Binary(Tracer, context,Address,
							label,Id,DevId,State_key,URL,iconName,
							parameters,device_type_id,update_timer,widgetSize, mytype,0,zone,params);
					onoff.container=(FrameLayout) panel_widget;
					panel_widget.addView(onoff);
				}
				else{
					onoff_New = new Graphical_Binary_New(Tracer, context,Address,
							label,Id,DevId,State_key,URL,iconName,
							parameters,device_type_id,update_timer,widgetSize, mytype,0,zone,params);
					onoff_New.container=(FrameLayout) panel_widget;
					panel_widget.addView(onoff_New);
				}

			}
		}
		else if (feature.getValue_type().equals("boolean")||feature.getValue_type().equals("bool")) {
			if (feature.getParameters().contains("command")){
				onoff_New = new Graphical_Binary_New(Tracer, context,Address,
						label,Id,DevId,State_key,URL,iconName,
						parameters,device_type_id,update_timer,widgetSize, mytype,0,zone,params);
				onoff_New.container=(FrameLayout) panel_widget;
				panel_widget.addView(onoff_New);
			}else{
				bool = new Graphical_Boolean(Tracer, context,Address,
						label,Id,DevId,State_key,iconName,
						parameters,device_type_id,update_timer,widgetSize, mytype,0,zone,params);
				bool.container=(FrameLayout) panel_widget;
				panel_widget.addView(bool);
			}
		}
		else if (feature.getValue_type().equals("range")) {
			variator = new Graphical_Range(Tracer, context,Address,
					label,Id,DevId,State_key,URL,iconName,
					parameters,device_type_id,update_timer,widgetSize, mytype,0,zone,params);
			variator.container=(FrameLayout) panel_widget;
			panel_widget.addView(variator);
		}
		else if (feature.getValue_type().equals("trigger")) {
			//#51 change widget for 0.4 if it's not a command
			if (parameters.contains("command")){
				trigger = new Graphical_Trigger(Tracer, context,Address,
						State_key,label,Id,DevId,State_key,URL,iconName,
						parameters,device_type_id,widgetSize, mytype,0,zone,params);
				trigger.container=(FrameLayout) panel_widget;
				panel_widget.addView(trigger);
				Tracer.i(mytag,"   ==> Graphical_Trigger");
			}else {
				info = new Graphical_Info(Tracer, context,Id,DevId,label,
						State_key,"",iconName,update_timer,
						widgetSize, mytype, parameters,0,zone,params);
				info.container=(FrameLayout) panel_widget;
				info.with_graph=false;
				panel_widget.addView(info);
				Tracer.i(mytag,"   ==> Graphical_Info");
			}
		}
		else if(State_key.equals("color")){
			color = new Graphical_Color(Tracer, context, 
					params,Id,DevId,label,
					device_type_id,	//Added by Doume to know the 'techno'
					Address,//  idem to know the address
					State_key,URL,iconName,update_timer,
					widgetSize, mytype,0,zone);
			panel_widget.addView(color);}
		else if (feature.getValue_type().equals("number")) {
			Tracer.i(mytag, "Parameters for number:"+feature.getParameters());
			if(feature.getParameters().contains("command_type")) {
				info_commands = new Graphical_Info_commands(Tracer, context,
						Id,DevId,label,State_key,URL,iconName,update_timer,
						widgetSize, mytype, parameters,0,zone,params,feature.getValue_type());
				info_commands.container=(FrameLayout) panel_widget;;
				panel_widget.addView(info_commands);
			}
			else if (params.getBoolean("Graph_CHOICE",false)==true) {
				Tracer.i(mytag, "Graphical_Info_with_achartengine created");
				info1= new Graphical_Info_with_achartengine(Tracer, context,
						Id,DevId,label,State_key,URL,iconName,Graph,
						update_timer,widgetSize, mytype, parameters,0,zone,params);
				info1.container=(FrameLayout) panel_widget;
				panel_widget.addView(info1);
			}else{
				Tracer.i(mytag, "Graphical_Info created");
				info = new Graphical_Info(Tracer, context,Id,DevId,
						label,State_key,URL,iconName,
						update_timer,widgetSize, mytype, parameters,0,zone,params);
				info.container=(FrameLayout) panel_widget;
				panel_widget.addView(info);
			}
		}
		else if (feature.getValue_type().equals("list")) {
			list = new Graphical_List(Tracer, context,Id,DevId, label,
					device_type_id,	//Added by Doume to know the 'techno'
					Address,			//  idem to know the address
					State_key,URL,iconName,Graph,
					update_timer,widgetSize, mytype, parameters,device_type_id,0,zone,params);
			list.container=(FrameLayout) panel_widget;
			panel_widget.addView(list);
		}
		else if (State_key.equals("color")) {
			colorw = new Graphical_Color(Tracer, context,
					params,Id,DevId,label,feature.getDevice_feature_model_id(),
					Address,State_key,URL,iconName,
					update_timer,
					widgetSize, mytype,0,zone
					);
			colorw.container=(FrameLayout) panel_widget;
			panel_widget.addView(colorw);
		}
		else if(feature.getValue_type().equals("string")){
			Tracer.i(mytag, "parameters="+parameters);
			if(feature.getDevice_feature_model_id().contains("call")) {
				info_with_history = new Graphical_History(Tracer, context,Id,DevId,label,
						State_key,URL,iconName,update_timer,
						widgetSize, mytype, parameters,0,zone,params);
				panel_widget.addView(info_with_history);
			}else if(feature.getDevice_feature_model_id().contains("camera")) {
				cam = new Graphical_Cam(Tracer, context,
						Id,DevId,label,
						State_key,Address,iconName,widgetSize, mytype,0,zone);
				panel_widget.addView(cam);
			}else if(parameters.contains("command")) {
				info_commands = new Graphical_Info_commands(Tracer, context,Id,DevId,label,
						State_key,URL,iconName,update_timer,
						widgetSize, mytype, parameters,0,zone,params,feature.getValue_type());
				panel_widget.addView(info_commands);
			}
			else {
				info = new Graphical_Info(Tracer, context,Id,DevId,label,
						State_key,"",iconName,update_timer,
						widgetSize, mytype, parameters,0,zone,params);
				info.container=(FrameLayout) panel_widget;
				info.with_graph=false;
				panel_widget.addView(info);
			}
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		origin = canvas.getMatrix();
		origin.postConcat(mat.matrix);
		canvas.setMatrix(origin);
		//35 is the height of the top border in dip.
		canvas.drawBitmap(map, 0, 35*getResources().getDisplayMetrics().density, paint_map);
		canvas.drawBitmap(widget, 0, 0, paint_map);
	}

	public Bitmap decodeFile(File f){
		Bitmap b = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			//			
			//			FileInputStream fis = new FileInputStream(f);
			//			BitmapFactory.decodeStream(fis, null, options);
			//			fis.close();
			//
			int scale = 1;
			if (options.outHeight > params.getInt("SIZE", 600) || options.outWidth > params.getInt("SIZE", 600)) {
				scale = (int)Math.pow(2, (int) Math.round(Math.log(params.getInt("SIZE", 600) / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
			}

			//Decode with inSampleSize
			BitmapFactory.Options options2 = new BitmapFactory.Options();
			options2.inSampleSize = scale;
			FileInputStream fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, options2);
			fis.close();
		} catch (IOException e) {
			Tracer.e(mytag, "Error decoding file");
		}
		return b;
	}

	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		int nbPointers = event.getPointerCount();
		float[] value = new float[9];
		float[] saved_value = new float[9];
		//TODO save value at the good time
		if(longclic == false){
			event1=MotionEvent.obtain(event);
			valuelongclic=value;
			Tracer.d(mytag, "Saving this event X="+event1.getX()+" Y="+event1.getY());
		}
		mat.matrix.getValues(value);
		//switch (event.getAction() & MotionEvent.ACTION_MASK) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Tracer.d(mytag, "ACTION_DOWN");
			longclic = false;
			Tracer.d(mytag, "longclic=false");
			handler_longclic.postDelayed(mLongPressed, 800);
			moves=0;
			mat.matrix.getValues(saved_value);
			mat.actionDown(event.getX(), event.getY());
			//save to pos_XO where was release the press
			pos_X0 = event.getX();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			Tracer.d(mytag, "ACTION_POINTER_DOWN");
			mat.actionPointerDown(event);
			break;
			//when stop pressing
		case MotionEvent.ACTION_UP:
			Tracer.d(mytag, "ACTION_UP");
			handler_longclic.removeCallbacks(mLongPressed);
			mat.actionUp(event.getX(), event.getY());
			//save to pos_X1 where was release the press
			pos_X1 = event.getX();
			//Select what action to do
			if (addMode==true){
				do_action("Add", event, value);
			}else if(removeMode==true){
				do_action("Delete", event, value);
			}else if(moveMode==true){
				do_action("Move", event, value);
			}else{
				//Move to left
				if(pos_X1 - pos_X0 > screen_width/2){
					if(currentFile +1 < files.size()) currentFile++;
					else currentFile=0;
					canvasMap=null;
					canvasWidget=null;
					System.gc();
					//refresh the map
					initMap();
					//Re-init last save position
					pos_X0=0;
					pos_X1=0;
					//Move to right
				}else if(pos_X0 - pos_X1 > screen_width/2){
					if(currentFile != 0) 
						currentFile--;
					else 
						currentFile=files.size()-1;
					canvasMap=null;
					canvasWidget=null;
					System.gc();
					//refresh the map
					initMap();
					//Re-init last save position
					pos_X0=0;
					pos_X1=0;
					//Display widget
				}else if(longclic == false){
					boolean widgetActiv=false;
					//Switch if it's a map
					for (Entity_Map switchesMap : listMapSwitches) {
						//TODO correct +20 by dip
						if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+dip20 && 
								(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+dip20){
							//That seems to be this switch map widget clicked !
							int new_map = switchesMap.getId() - 99999;
							if(new_map < files.size() && new_map >= 0) {
								currentFile=new_map;
							}
							canvasMap=null;
							canvasWidget=null;
							System.gc();
							initMap();

							panel_button.setVisibility(View.GONE);
							panel_widget.setVisibility(View.VISIBLE);
							widgetActiv=true;
							postInvalidate();
							return true;
						}
					}
					widgetActiv=false;
					//get widgets
					for (Entity_Map featureMap : listFeatureMap) {
						//TODO correct +20 by dip
						if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+dip20 && 
								(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+dip20){
							//Launch directly the command
							try {
								// #2009 action directly if binary or trigger
								// via the asynctask new CommandeThread()
								//TODO 0.4 should add bool because it could also be add here
								// problem is to know is real state before sending it
								if (featureMap.getValue_type().equals("trigger")){
									//#51 change widget for 0.4 if it's not a command
									if (featureMap.getParameters().contains("command")){
										Tracer.d(mytag, "This is a Trigger launching it");
										this.URL = params.getString("URL","1.1.1.1");
										this.Address = featureMap.getAddress();
										if (api_version>=0.7f){
											try {
												JSONObject jparam= new JSONObject(featureMap.getParameters());
												command_id = jparam.getString("command_id");
												command_type= jparam.getString("command_type");
												state_progress="1";
												new CommandeThread().execute();	
											} catch (JSONException e) {
												Tracer.d(mytag, "No command_id for this device");
											}
										}else{
											new CommandeThread().execute();	
										}
									}
								}else if (featureMap.getValue_type().equals("binary")){
									Tracer.d(mytag, "This is a binary try to change is state");
									Tracer.d(mytag, "State is "+featureMap.getCurrentState().toString());
									if (featureMap.getCurrentState().equals("true")){
										featureMap.setCurrentState("false");
									}else if (featureMap.getCurrentState().equals("false")){
										featureMap.setCurrentState("true");
									}else if (featureMap.getCurrentState().equals("on")){
										featureMap.setCurrentState("off");
									}else if (featureMap.getCurrentState().equals("off")){
										featureMap.setCurrentState("on");
									}else if (featureMap.getCurrentState().equals("1")){
										featureMap.setCurrentState("0");
									}else if (featureMap.getCurrentState().equals("0")){
										featureMap.setCurrentState("1");
									}

									this.URL = params.getString("URL","1.1.1.1");
									this.Address = featureMap.getAddress();
									String[] model = featureMap.getDevice_type_id().split("\\.");
									this.type = model[0];
									this.state_progress=featureMap.getCurrentState();
									if (api_version>=0.7f){
										try {
											JSONObject jparam= new JSONObject(parameters);
											command_id = jparam.getString("command_id");
											command_type= jparam.getString("command_type");
											new CommandeThread().execute();
										} catch (JSONException e) {
											Tracer.d(mytag, "No command_id for this device");
										}	
									}else{
										new CommandeThread().execute();
									}
								}else{
									//Show the top widgets
									Tracer.d(mytag, "Launch showtopwidgets");
									showTopWidget(featureMap);
									panel_button.setVisibility(View.GONE);
									panel_widget.setVisibility(View.VISIBLE);
									if(!top_drawer.isOpen())top_drawer.setOpen(true, true);
									if(bottom_drawer.isOpen())bottom_drawer.setOpen(false, true);
									widgetActiv=true;
								}
							} catch (Exception e) {
								Tracer.d(mytag, "on action up crash "+ e.toString());
							}

						}

					}
					//hide it
					if(!widgetActiv && moves < 5){
						Tracer.d(mytag, "Launch HIDE top widgets");
						top_drawer.setOpen(false, true);
						bottom_drawer.setOpen(false, true);
					}
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Tracer.d(mytag, "ACTION_POINTER_UP");
			handler_longclic.removeCallbacks(mLongPressed);
			mat.matrix.getValues(value);
			currentScale*=value[0];
			//Save current zoom scale
			prefEditor=params.edit();
			prefEditor.putFloat("Mapscale", currentScale);
			prefEditor.commit();	//To save it really !
			value[0]=1;
			value[4]=1;
			mat.matrix.setValues(value);
			refreshMap();
			break;
		case MotionEvent.ACTION_MOVE:
			Tracer.d(mytag, "ACTION_MOVE");
			moves++;
			mat.currentScale = currentScale;
			mat.actionMove(nbPointers, event);
			break;
		}
		postInvalidate();
		return true;
	}

	final Runnable mLongPressed = new Runnable() {
		public void run() { 
			if(params.getBoolean("map_menu_disable",false)==false){
				longclic = true;
				//Code for long click
				Tracer.v(mytag, "Long press :)");
				list_type_choice = new AlertDialog.Builder(getContext());
				List<String> list_choice = new ArrayList<String>();
				//TODO translate his menu
				list_choice.add("Add");
				//TODO remove entry if not on a widget
				list_choice.add("Move");
				list_choice.add("Delete");
				list_choice.add("Change icon");
				final CharSequence[] char_list =list_choice.toArray(new String[list_choice.size()]);
				//list_type_choice.setTitle(R.string.What_to_do_message);
				list_type_choice.setSingleChoiceItems(char_list, -1,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						if (checkedItem.toString()=="Add"){
							Activity_Map.dialog_feature.show();
						}	    					
						do_action(checkedItem.toString(), event1,valuelongclic);
						Tracer.d(mytag, "do_action "+checkedItem.toString()+" at X="+event1.getX()+"at Y="+event1.getY());
						dialog.dismiss();
					}
				}
						);
				list_type_choice.show();
			}
		}   
	};

	private void do_action(String action,MotionEvent event, float[] value) {
		if(action.equals("Change icon")) {
			Tracer.d(mytag, "Change icon");
			for (final Entity_Map featureMap : listFeatureMap) {
				if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+dip20 && 
						(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+dip20){
					Tracer.d(mytag, "Change ison of a feature");
					final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
					List<String> list_icon = new ArrayList<String>();
					String[] fiilliste;
					fiilliste = context.getResources().getStringArray(R.array.icon_area_array); 
					for (int i=0; i < fiilliste.length ; i++){
						list_icon.add(fiilliste[i].toString());
					}
					final CharSequence[] char_list_icon =list_icon.toArray(new String[list_icon.size()]);
					list_icon_choice.setTitle(context.getString(R.string.Wich_ICON_message)+" "+featureMap.getName()+"-"+featureMap.getState_key());
					List_Icon_Adapter adapter=new List_Icon_Adapter(getContext(), fiilliste);
					list_icon_choice.setAdapter(adapter,null );
					list_icon_choice.setSingleChoiceItems(char_list_icon, -1,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							ListView lw = ((AlertDialog)dialog).getListView();
							Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
							String icon = checkedItem.toString();
							ContentValues values = new ContentValues();
							//type = area, room, feature
							values.put("name", "feature");
							//icon is the name of the icon wich will be select 
							values.put("value", icon);
							//reference is the id of the area, room, or feature
							int reference = 0;
							reference=featureMap.getId();
							values.put("reference", reference);
							context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
							initMap();
							dialog.cancel();
						}
					}
							);	
					AlertDialog alert_list_icon = list_icon_choice.create();
					alert_list_icon.show();

				}
			}
		} else if(action.equals("Move")) {
			Tracer.d(mytag, "Move");
			for (Entity_Map featureMap : listFeatureMap) {
				if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+dip20 && 
						(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+dip20){
					Tracer.d(mytag, "Move find a feature");
					//remove entry
					Tracer.get_engine().remove_one_FeatureMap(featureMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
					moveMode=false;
					//new UpdateThread().execute();
					//return to add mode on next click
					//refresh the map
					initMap();
					temp_id = featureMap.getId();
					addMode=true;
				}
			}
			for (Entity_Map switchesMap : listMapSwitches) {
				if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+dip20 && 
						(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+dip20){
					//remove entry
					Tracer.get_engine().remove_one_FeatureMap(switchesMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
					moveMode=false;
					//new UpdateThread().execute();
					//return to add mode on next click
					//refresh the map
					initMap();
					temp_id = switchesMap.getId();
					addMode=true;
				}
			}
		}else if (action.equals("Delete")){
			Tracer.d(mytag, "Delete");
			for (Entity_Map featureMap : listFeatureMap) {
				if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-dip20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+dip20 && 
						(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-dip20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+dip20){
					//remove entry
					Tracer.d(mytag, "Delete a feature");
					Tracer.get_engine().remove_one_FeatureMap(featureMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
					//A device on this map as been delete re-check the cache URL
					Cache_management.checkcache(Tracer,context);
					removeMode=false;
					//new UpdateThread().execute();
					//refresh the map
					initMap();
				}
			}
			for (Entity_Map switchesMap : listMapSwitches) {
				if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+20 && 
						(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+20){
					//remove entry
					Tracer.get_engine().remove_one_FeatureMap(switchesMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
					removeMode=false;
					//new UpdateThread().execute();
					//refresh the map
					initMap();
				}
			}		
		}else if (action.equals("Add")){
			int db_id = 0;
			if(temp_id != -1) {
				//insert in the database feature map the device id, its position and map name. 
				db_id = temp_id;
			} else {
				if (map_id != -1) {
					db_id = map_id;
					// a map switch has been selected from list of widgets
				}
			}
			if (db_id != 0) {
				Tracer.get_engine().insertFeatureMap(db_id, 
						(int)((event.getX()-value[2])/currentScale), 
						(int)((event.getY()-value[5])/currentScale),
						files.elementAt(currentFile));
				//Re-check the cache URL
				Cache_management.checkcache(Tracer,context);
			}
			map_id = -1;
			temp_id = -1;
			addMode=false;
			//refresh the map
			initMap();
		}
	}

	public String getFileAsString(File file){ 
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				sb.append( dis.readLine() +"\n");
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public float autoscale(int image_width,int image_height) {
		currentScalewidth=(float)screenwidth/(float)image_width;
		currentScaleheight=(float)screenheight/(float)image_height;
		//select witch scale is the best
		if (currentScaleheight<currentScalewidth){
			currentScale=currentScaleheight;	
		} else{
			currentScale=currentScalewidth;	
		}
		//Save current zoom scale
		prefEditor=params.edit();
		prefEditor.putFloat("Mapscale", currentScale);
		prefEditor.commit();	//To save it really !
		return currentScale;
	}

	public boolean isAddMode() {
		return addMode;
	}

	public void setAddMode(boolean addMode) {
		this.addMode = addMode;
	}

	public boolean isRemoveMode() {
		return removeMode;
	}

	public void setRemoveMode(boolean removeMode) {
		this.removeMode = removeMode;
	}

	public int getUpdate() {
		return update;
	}

	public void setUpdate(int update) {
		this.update = update;
	}

	public void setParams(SharedPreferences params) {
		this.params = params;
	}

	public void setPanel_widget(ViewGroup panel_widget) {
		this.panel_widget = panel_widget;
	}

	public void setPanel_button(ViewGroup panel_button) {
		this.panel_button = panel_button;
	}

	public void setTopDrawer(Sliding_Drawer top_drawer) {
		this.top_drawer = top_drawer;
	}

	public void setBottomDrawer(Sliding_Drawer bottom_drawer) {
		this.bottom_drawer = bottom_drawer;
	}

	public void setFiles(Vector<String> files) {
		this.files = files;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(int currentFile) {
		this.currentFile = currentFile;
	}

	public boolean isMoveMode() {
		return moveMode;
	}

	public void setMoveMode(boolean moveMode) {
		this.moveMode = moveMode;	
	}

	public class CommandeThread extends AsyncTask<Void, Integer, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			Handler temphandler =  new Handler(context.getMainLooper());
			temphandler.post( new Runnable(){
				public void run(){
					String Url2send;
					if(api_version>=0.7f){
						Url2send = URL+"cmd/id/"+command_id+"?"+command_type+"="+state_progress;
					}else{
						Url2send = URL+"command/"+type+"/"+Address+"/"+state_progress;
					}
					Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
					JSONObject json_Ack = null;
					try {
						json_Ack = Rest_com.connect_jsonobject(Url2send,login,password);
					} catch (Exception e) {
						Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
						Toast.makeText(context, "Rinor exception sending command",Toast.LENGTH_LONG).show();
					}
					try {
						Boolean ack = JSONParser.Ack(json_Ack);
						if(ack==false){
							Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
							Toast.makeText(context, "Received error from Rinor",Toast.LENGTH_LONG).show();
							handler.sendEmptyMessage(2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
					);
			return null;
		}
	}

	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}

}