/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;

import database.DmdContentProvider;
import database.WidgetUpdate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class Graphical_Boolean extends Basic_Graphical_widget{

	private final TextView state;
	private String value0;
	private String value1;
	private final String Value_0;
	private final String Value_1;
	private final ImageView bool;
	private final Handler handler;
	private final String state_key;
	private final int update;
	private static String mytag;
	private Message msg;
	private String stateS = "";
	private final float api_version;
	private final SharedPreferences params;

	public static FrameLayout container = null;
	private static FrameLayout myself = null;
	private tracerengine Tracer = null;

	private Entity_client session = null; 
	private Boolean realtime = false;
	private final int session_type;
	private final String place_type;
	private final int place_id;
	private final Activity context;
	private final String usage;

	@SuppressLint("HandlerLeak")
	public Graphical_Boolean(tracerengine Trac, Activity context, 
			String address, final String name, 
			int id,int dev_id, 
			String state_key, final String usage,
			String parameters, 
			String model_id, int update, 
			int widgetSize,
			int session_type,int place_id,String place_type, SharedPreferences params) {
		super(context,Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.myself=this;
		this.context = context;
		this.Tracer = Trac;
		this.state_key = state_key;
		this.usage=usage;
		this.update = update;
		this.session_type = session_type;
		try{
			this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase())).toString();
		}catch (Exception e){
			Tracer.d(mytag, "no translation for this state:"+state_key);
			this.stateS= state_key;
		}
		this.place_id= place_id;
		this.place_type= place_type;
		this.params = params;
		api_version=params.getFloat("API_VERSION", 0);

		try {
			JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
			value0 = jparam.getString("value0");
			value1 = jparam.getString("value1");
		} catch (Exception e) {
			value0 = "0";
			value1 = "1";
		}		

		if (usage.equals("light")){
			this.Value_0 =  getResources().getText(R.string.light_stat_0).toString();
			this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
		}else if (usage.equals("shutter")){
			this.Value_0 =  getResources().getText(R.string.shutter_stat_0).toString();
			this.Value_1 =  getResources().getText(R.string.shutter_stat_1).toString();
		}else{
			this.Value_0 = value0;
			this.Value_1 = value1;		
		}

		mytag="Graphical_Boolean("+dev_id+")";

		//state
		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setText(stateS+" : "+Value_0);

		//boolean on/off
		bool = new ImageView(context);
		bool.setImageResource(R.drawable.boolean_off);

		super.LL_infoPan.addView(state);
		super.LL_featurePan.addView(bool);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {	
				String status;
				if(msg.what == 9999) {
					if(session == null)
						return;
					status = session.getValue();
					if(status != null)   {
						Tracer.d(mytag,"Handler receives a new status <"+status+">" );

						try {
							if(status.equals(value0)||status.equals("0")){
								bool.setImageResource(R.drawable.boolean_off);
								//change color if statue=low to (usage, o) means off
								//note sure if it must be kept as set previously as default color.
								IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
								state.setText(stateS+" : "+Value_0);
							}else if(status.equals(value1)||status.equals("1")){
								bool.setImageResource(R.drawable.boolean_on);
								//change color if statue=high to (usage, 2) means on
								IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
								state.setText(stateS+" : "+Value_1);
							}
						} catch (Exception e) {
							Tracer.e(mytag, "handler error device "+name);
							e.printStackTrace();
						}
					}
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(LL_background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself
				}

			}

		};
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

}


