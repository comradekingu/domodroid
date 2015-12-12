package widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import misc.Color_Progress;
import misc.Color_RGBField;
import misc.Color_Result;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;
import org.json.JSONObject;

import rinor.Rest_com;
import widgets.Graphical_Binary.SBAnim;
import database.DmdContentProvider;
import database.JSONParser;
import database.WidgetUpdate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Graphical_Color extends Basic_Graphical_widget implements OnSeekBarChangeListener,  OnClickListener{


	private int mInitialColor, mDefaultColor;
	private String mKey;

	private final LinearLayout color_LeftPan;
	private final LinearLayout color_RightPan;
	private final LinearLayout featurePan2;
	private final int dev_id;
	private final int id;
	private final Handler handler;
	private final String state_key;
	private final Color_Progress seekBarHueBar;
	private final Color_Progress seekBarPowerBar;
	private final Color_Progress seekBarRGBXBar;
	private final Color_Progress seekBarRGBYBar;
	private final Color_RGBField rgbView;
	private final Color_Result resultView;
	private final TextView state_key_view;
	private final String url;
	private final int update;
	private Animation animation;
	private boolean touching;
	private int updating=0;
	private int state_progress;

	private int argb = 0;
	private String argbS = "";
	private Message msg;
	private static String mytag;
	private final String name;
	private final String wname;
	private final String type;
	private final String address;
	private Activity mycontext;
	public static FrameLayout container = null;
	public static FrameLayout myself = null;
	private Boolean switch_state = false;
	private TimerTask doAsynchronousTask;


	private final int widgetSize;
	private Color currentColor;
	private final SeekBar seekBarOnOff;
	private int[] mMainColors = new int[65536];
	public float mCurrentHue = 0;
	public int rgbHue = 0;
	public int rgbX = 0;
	public int rgbY = 0;

	private final TextView title7;
	private final TextView title8;
	private final TextView title9;
	private final String t7s;
	private final String t8s;
	private String t9s = "";
	private final SharedPreferences params;
	private tracerengine Tracer = null;
	private Entity_client session = null; 
	private Boolean realtime = false;
	private final int session_type;
	private final String place_type;
	private final int place_id;
	private final String login;
	private final String password;
	private final float api_version;
	private final String usage;
	private final Context context;

	@SuppressLint("HandlerLeak")
	public Graphical_Color(tracerengine Trac, Activity context, 
			SharedPreferences params, 
			int id,int dev_id, 
			String name,
			String model_id,
			String address,
			final String state_key, 
			String url,
			String usage, 
			int update, 
			int widgetSize,
			int session_type,int place_id,String place_type) {
		super(context,Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.context=context;
		this.Tracer = Trac;
		this.dev_id = dev_id;
		this.id = id;
		this.usage=usage;
		this.state_key = state_key;
		this.name=name;
		this.wname=name;
		this.address=address;
		this.url = url;
		this.update=update;
		this.widgetSize=widgetSize;
		this.params = params;
		this.myself = this;
		this.session_type = session_type;
		this.place_id= place_id;
		this.place_type= place_type;
		mytag="Graphical_Color("+dev_id+")";
		setOnClickListener(this);

		login = params.getString("http_auth_username",null);
		password = params.getString("http_auth_password",null);
		api_version=params.getFloat("API_VERSION", 0);

		String[] model = model_id.split("\\.");
		type = model[0];
		Tracer.d(mytag,"model_id = <"+model_id+"> type = <"+type+">" );

		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(state_key);
		state_key_view.setTextColor(Color.parseColor("#333333"));

		//first seekbar on/off
		seekBarOnOff=new SeekBar(context);
		seekBarOnOff.setProgress(0);
		seekBarOnOff.setMax(100);
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bgseekbaronoff);
		seekBarOnOff.setLayoutParams(new LayoutParams(bMap.getWidth(),bMap.getHeight()));
		seekBarOnOff.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbaronoff));
		seekBarOnOff.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
		seekBarOnOff.setThumbOffset(0);
		seekBarOnOff.setOnSeekBarChangeListener(this);
		seekBarOnOff.setTag("onoff");

		//feature panel 2
		featurePan2=new LinearLayout(context);
		featurePan2.setOrientation(LinearLayout.HORIZONTAL);
		//featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		featurePan2.setGravity(Gravity.CENTER_VERTICAL);
		featurePan2.setPadding(20, 0, 0, 10);

		//left panel
		color_LeftPan = new LinearLayout(context);
		color_LeftPan.setOrientation(LinearLayout.VERTICAL);
		//color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1));
		color_LeftPan.setPadding(0, 0, 0, 10);

		TextView title1 = new TextView(context);
		title1.setText(context.getString(R.string.Hue));
		title1.setTextSize(10);
		title1.setTextColor(Color.parseColor("#333333"));
		TextView title2 = new TextView(context);
		title2.setText(context.getString(R.string.Sat));
		title2.setTextSize(10);
		title2.setTextColor(Color.parseColor("#333333"));
		TextView title3 = new TextView(context);
		title3.setText(context.getString(R.string.Bright));
		title3.setTextSize(10);
		title3.setTextColor(Color.parseColor("#333333"));
		//TextView title4 = new TextView(context);
		//title4.setText("Luminosity");
		//title4.setTextSize(10);
		//title4.setTextColor(Color.parseColor("#333333"));
		TextView title5 = new TextView(context);
		title5.setText(context.getString(R.string.Field));
		title5.setTextSize(10);
		title5.setTextColor(Color.parseColor("#333333"));
		TextView title6 = new TextView(context);
		title6.setText(context.getString(R.string.Curcolor));
		title6.setTextSize(10);
		title6.setTextColor(Color.parseColor("#333333"));
		title7 = new TextView(context);
		t7s = context.getString(R.string.Red);
		title7.setText(t7s+" : 255");
		title7.setTextSize(10);
		title7.setTextColor(Color.parseColor("#333333"));
		title8 = new TextView(context);
		t8s = context.getString(R.string.Green);
		title8.setText(t8s+" : 0");
		title8.setTextSize(10);
		title8.setTextColor(Color.parseColor("#333333"));
		title9 = new TextView(context);
		t9s = context.getString(R.string.Blue);
		title9.setText(t9s+" : 0");
		title9.setTextSize(10);
		title9.setTextColor(Color.parseColor("#333333"));


		//seekbar huebar
		seekBarHueBar=new Color_Progress(Tracer, context,0,0);
		seekBarHueBar.setProgress(0);
		seekBarHueBar.setMax(255);
		seekBarHueBar.setProgressDrawable(null);
		seekBarHueBar.setOnSeekBarChangeListener(this);
		seekBarHueBar.setTag("hue");

		//seekbar rgbbarX
		seekBarRGBXBar=new Color_Progress(Tracer, context,1,0);
		seekBarRGBXBar.setProgress(0);
		seekBarRGBXBar.setMax(255);
		seekBarRGBXBar.setProgressDrawable(null);
		seekBarRGBXBar.setOnSeekBarChangeListener(this);
		seekBarRGBXBar.setTag("rgbx");

		//seekbar rgbbarY
		seekBarRGBYBar=new Color_Progress(Tracer, context,2,0);
		seekBarRGBYBar.setProgress(0);
		seekBarRGBYBar.setMax(255);
		seekBarRGBYBar.setProgressDrawable(null);
		seekBarRGBYBar.setOnSeekBarChangeListener(this);
		seekBarRGBYBar.setTag("rgby");

		//seekbar powerbar
		seekBarPowerBar=new Color_Progress(Tracer, context,3,0);
		seekBarPowerBar.setProgress(0);
		seekBarPowerBar.setMax(255);
		seekBarPowerBar.setProgressDrawable(null);
		seekBarPowerBar.setOnSeekBarChangeListener(this);
		seekBarPowerBar.setTag("power");


		//RGBField
		rgbView = new Color_RGBField(getContext(), Color.RED, Color.RED);
		//rgbView.drawRGBField();

		//right panel
		color_RightPan = new LinearLayout(context);
		color_RightPan.setOrientation(LinearLayout.VERTICAL);
		color_RightPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.RIGHT));
		color_RightPan.setPadding(20, 0, 0, 10);

		//Color result
		resultView = new Color_Result(context);

		LL_featurePan.addView(seekBarOnOff);
		LL_infoPan.addView(state_key_view);

		color_LeftPan.addView(title1);
		color_LeftPan.addView(seekBarHueBar);
		color_LeftPan.addView(title2);
		color_LeftPan.addView(seekBarRGBXBar);
		color_LeftPan.addView(title3);
		color_LeftPan.addView(seekBarRGBYBar);
		//color_LeftPan.addView(title4);
		//color_LeftPan.addView(seekBarPowerBar);
		color_LeftPan.addView(title5);
		color_LeftPan.addView(rgbView);

		color_RightPan.addView(title6);
		color_RightPan.addView(resultView);
		color_RightPan.addView(title7);
		color_RightPan.addView(title8);
		color_RightPan.addView(title9);


		featurePan2.addView(color_LeftPan);
		featurePan2.addView(color_RightPan);
		featurePan2.setVisibility(INVISIBLE);

		//LoadSelections();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				argbS = "?";
				if(msg.what == 2) {
					Toast.makeText(getContext(), "Command to server rejected", Toast.LENGTH_SHORT).show();

				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(LL_background);

					myself.setVisibility(GONE);

					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself	

				} else 	if(msg.what == 9999) {
					if(session != null)
						argbS = session.getValue();
					else
						return;
					Tracer.d(mytag,"Handler receives a new value from state engine <"+argbS+">" );

				}
				if(argbS.equals("off")) {
					switch_state=false;
					argbS="000000";
					argb=0;
				} else if(argbS.equals("on")) {
					seekBarOnOff.setProgress(100);
					switch_state=true;
					LoadSelections();	//Recall last values known from shared preferences
					// argb and argbS will be set when seekBars will be changed
					return;

				} else {
					try {
						argbS = argbS.substring(1);	//It's the form #RRGGBB : ignore the #
						//Tracer.d(mytag,"Handler ==> argbS after extraction = <"+argbS+">" );
						argb = Integer.parseInt(argbS,16);
						//Tracer.d(mytag,"Handler ==> argb after parsing = <"+argb+">" );
					} catch (Exception e) {
						argb = 1;
					}
				}
				int r, g , b;
				int value_save = argb;
				r=((argb>>16)&0xFF);
				g=((argb>>8)&0xFF);
				b=((argb)&0xFF);


				if (argb == 0){
					seekBarOnOff.setProgress(0);
					switch_state=false;
				} else {
					seekBarOnOff.setProgress(100);
				}
				//Convert RGB to HSV color, and set sliders
				float hsv[] = new float[3];

				Color.colorToHSV(value_save, hsv);
				//Tracer.d(mytag,"Handler ==> RGB ("+value_save+") values after process = <"+r+"> <"+g+"> <"+b+">" );
				//Tracer.d(mytag,"Handler ==> HSV values after process = <"+hsv[0]+"> <"+hsv[1]+"> <"+hsv[2]+">" );

				//Seekbars are in range 0-255 : convert HSV values
				//Hue is an angle : convert it to linear
				seekBarHueBar.setProgress((int) ( 255f -(hsv[0]*255f/360)) );
				seekBarRGBXBar.setProgress((int) (hsv[1]*255f));
				seekBarRGBYBar.setProgress((int) (hsv[2]*255f));

				title7.setText(t7s+" : "+r);
				title8.setText(t8s+" : "+g);
				title9.setText(t9s+" : "+b);
				if( (r != 0) || (g != 0) || (b != 0)) {
					SaveSelections();
				}

			}

		};	
		updating = 0;
		//================================================================================
		/*
		 * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
		WidgetUpdate cache_engine = WidgetUpdate.getInstance();
		if(cache_engine != null) {
			if (api_version<=0.6f){
				session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
			}else if (api_version>=0.7f){
				session = new Entity_client(id, "", mytag, handler, session_type);
			}
			if(Tracer.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
				//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}

		}
		//================================================================================
		//updateTimer();	//Don't use anymore cyclic refresh....		
	}



	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		if(arg0.getTag().equals("onoff")) {
			//User is moving on/off object....
		} else	if(arg0.getTag().equals("hue")){				
			//rgb view
			mCurrentHue = (255-arg0.getProgress())*360/255;
			rgbView.mCurrentHue = mCurrentHue;
			rgbView.invalidate();

			//rgb X
			float[] hsv0 = {0,0,(float)rgbY/255f};
			float[] hsv1 = {mCurrentHue,1,(float)rgbY/255f};
			seekBarRGBXBar.hsv0 = hsv0;
			seekBarRGBXBar.hsv1 = hsv1;
			seekBarRGBXBar.invalidate();

			//rgb Y
			float[] hsv2 = {0,0,0};
			float[] hsv3 = {mCurrentHue,(float)rgbX/255f,1};
			seekBarRGBYBar.hsv2 = hsv2;
			seekBarRGBYBar.hsv3 = hsv3;
			seekBarRGBYBar.invalidate();

		} else if(arg0.getTag().equals("rgbx")){
			rgbX = arg0.getProgress();
			float[] hsv2 = {0,0,0};
			float[] hsv3 = {mCurrentHue,(float)rgbX/255f,1};
			seekBarRGBYBar.hsv2 = hsv2;
			seekBarRGBYBar.hsv3 = hsv3;
			seekBarRGBYBar.invalidate();

			rgbView.mCurrentX = arg0.getProgress();
			seekBarRGBYBar.invalidate();
			rgbView.invalidate();

		}else if(arg0.getTag().equals("rgby")){
			rgbY = arg0.getProgress();
			float[] hsv0 = {0,0,(float)rgbY/255f};
			float[] hsv1 = {mCurrentHue,1,(float)rgbY/255f};
			seekBarRGBXBar.hsv0 = hsv0;
			seekBarRGBXBar.hsv1 = hsv1;
			rgbView.mCurrentY = 255-arg0.getProgress();
			seekBarRGBXBar.invalidate();
			rgbView.invalidate();
		}

		float[] hsvCurrent = {mCurrentHue,(float)rgbX/255f,(float)rgbY/255f};
		argb = Color.HSVToColor(hsvCurrent);
		resultView.hsvCurrent = hsvCurrent;
		argbS = Integer.toHexString((argb>>16)&0xFF)+Integer.toHexString((argb>>8)&0xFF)+Integer.toHexString((argb)&0xFF);
		title7.setText(t7s+" : "+((argb>>16)&0xFF));
		title8.setText(t8s+" : "+((argb>>8)&0xFF));
		title9.setText(t9s+" : "+((argb)&0xFF));
		resultView.invalidate();
	}



	public void onStartTrackingTouch(SeekBar seekBar) {
		touching=true;
		updating=3;
	}



	public void onStopTrackingTouch(SeekBar seekBar) {
		String tag = (String )seekBar.getTag();
		if(tag.equals("onoff")) {
			if(seekBar.getProgress()<20){
				seekBar.setProgress(0);
				switch_state=false;
				Tracer.i(mytag,"Change switch to OFF" );
				// Force color picker to black....
				seekBarRGBYBar.setProgress(0);		//No brightness => black !
			}else{
				seekBar.setProgress(100);
				switch_state=true;
				LoadSelections();			//Recall last known value, till state engine refresh...
				Tracer.i(mytag,"Change switch to ON" );
			}
			new CommandeThread().execute();		//And send switch_state to Domogik

		} else {
			state_progress = seekBar.getProgress();
			SaveSelections();
			Tracer.i(mytag,"End of change : new rgb value =  #"+argbS );
			if(seekBarOnOff.getProgress() > 50)
				if(switch_state)
					new CommandeThread().execute();		//send new color
		}
		touching=false;

	}

	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			String Url2send = url+"command/"+type+"/"+address+"/setcolor/";

			if( ( argb != 0) && switch_state) {

				String srgb = Integer.toHexString(argb);
				if(srgb.length() > 6)
					srgb = srgb.substring(2);
				Url2send+="#"+srgb;
			} else {
				String State="";
				if(switch_state) {
					State="on";

				} else {
					State="off";
					seekBarHueBar.setProgress(255);
					seekBarRGBXBar.setProgress(0);
					seekBarRGBYBar.setProgress(0);
				}
				Url2send+=State;
			}

			updating=1;

			Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
			JSONObject json_Ack = null;
			try {
				json_Ack = Rest_com.connect_jsonobject(Url2send,login,password);
			} catch (Exception e) {
				Tracer.e(mytag, "Rest exception sending command : <"+e.getMessage()+">");
			}
			Boolean ack = false;
			if(json_Ack != null) {
				try {
					//@SuppressWarnings("unused")
					ack = JSONParser.Ack(json_Ack);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(ack==false){
					Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
					handler.sendEmptyMessage(2);
				} 
			}
			updating=0;
			return null;
		}
	}
	/*
	 * Saving HSV parameters allow to restore them when Domogik server 
	 * only notify 'on' state (without any RGB parameters)
	 * We've to know which was the last one, kept by Domogik
	 */

	private void SaveSelections() {
		SharedPreferences.Editor prefEditor=params.edit();
		prefEditor.putInt("COLORHUE",seekBarHueBar.getProgress());
		prefEditor.putInt("COLORSATURATION",seekBarRGBXBar.getProgress());
		prefEditor.putInt("COLORBRIGHTNESS",seekBarRGBYBar.getProgress());
		prefEditor.putString("COLORRGB","#"+argbS);
		prefEditor.commit();
		/*
		Tracer.i(mytag, "SaveSelections()");
		Tracer.i(mytag,"Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i(mytag,"Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i(mytag,"Bright = "+params.getInt("COLORBRIGHTNESS",0));
		 */
	}

	private void LoadSelections() {
		seekBarHueBar.setProgress(params.getInt("COLORHUE",0));
		seekBarRGBXBar.setProgress(params.getInt("COLORSATURATION",255));
		seekBarRGBYBar.setProgress(params.getInt("COLORBRIGHTNESS",255));
		/*
		Tracer.i(mytag, "LoadSelections()");
		Tracer.i(mytag,"Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i(mytag,"Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i(mytag,"Bright = "+params.getInt("COLORBRIGHTNESS",0));
		 */
	}

	public void onClick(View arg0) {
		Tracer.i(mytag, "Touch....");
		if(featurePan2.getVisibility()== INVISIBLE){
			LL_background.addView(featurePan2);
			featurePan2.setVisibility(VISIBLE);
			Tracer.i(mytag, "FeaturePan2 set to VISIBLE");
		}
		else{
			LL_background.removeView(featurePan2);
			featurePan2.setVisibility(INVISIBLE);
			Tracer.i(mytag, "FeaturePan2 set to INVISIBLE");
		}
		return;
	}
}