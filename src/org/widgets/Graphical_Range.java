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
package org.widgets;

import java.util.Timer;
import java.util.TimerTask;

import org.connect.Rest_com;
import org.database.DomodroidDB;
import org.database.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.panel.Gradients_Manager;
import org.panel.Graphics_Manager;
import org.panel.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;



public class Graphical_Range extends FrameLayout implements SeekBar.OnSeekBarChangeListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private FrameLayout rightPan;
	private LinearLayout bodyPanHorizontal;
	private LinearLayout infoPan;
	private LinearLayout leftPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView state;
	private SeekBar seekBarVaria;
	private String address;
	private int state_progress;
	private String usage;
	private String url;
	private int dev_id;
	private Handler handler;
	private String state_key;
	private int range;
	private int scale;
	private int valueMin;
	private int valueMax;
	private String type; 
	private String command;
	public static int stateThread; 
	private int update;
	public boolean activate=false;
	private Animation animation;
	private boolean touching;
	private int updating=0;
	private DomodroidDB domodb;
	private String wname;


	public Graphical_Range(Activity context, String address, String name,int dev_id,String state_key, String url, String usage, String parameters, String model_id, int update, int widgetSize) throws JSONException {
		super(context);
		this.address = address;
		this.url = url;
		this.dev_id=dev_id;
		this.state_key=state_key;
		this.usage = usage;
		this.update = update;
		this.wname = name;
		stateThread = 1;

		//get parameters
		JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
		command = jparam.getString("command");
		valueMin = jparam.getInt("valueMin");
		valueMax = jparam.getInt("valueMax");
		range = valueMax-valueMin;
		scale = 100/range;

		String[] model = model_id.split("\\.");
		type = model[0];

		this.setPadding(5, 5, 5, 5);

		domodb = new DomodroidDB(context);

		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//linearlayout horizontal body		
		bodyPanHorizontal=new LinearLayout(context);
		bodyPanHorizontal.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.CENTER_VERTICAL));
		bodyPanHorizontal.setOrientation(LinearLayout.HORIZONTAL);

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 10, 10);

		//img
		img = new ImageView(context);
		img.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));

		//right panel with different info and seekbars		
		rightPan=new FrameLayout(context);
		rightPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		rightPan.setPadding(0, 0, 10, 0);

		// panel
		leftPan = new LinearLayout(context);
		leftPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.BOTTOM));
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setGravity(Gravity.CENTER_VERTICAL);
		leftPan.setPadding(4, 5, 0, 0);

		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		infoPan.setOrientation(LinearLayout.HORIZONTAL);
		infoPan.setPadding(4, 5, 0, 0);


		//name
		nameDevices=new TextView(context);
		nameDevices.setText(name);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);


		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setPadding(20, 0, 0, 0);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);

		//first seekbar variator
		seekBarVaria=new SeekBar(context);
		seekBarVaria.setProgress(0);
		seekBarVaria.setMax(range);
		seekBarVaria.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));
		seekBarVaria.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbarvaria));
		seekBarVaria.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
		seekBarVaria.setThumbOffset(-3);
		seekBarVaria.setOnSeekBarChangeListener(this);
		seekBarVaria.setPadding(0, 0, 15, 7);

		infoPan.addView(nameDevices);
		infoPan.addView(state);
		leftPan.addView(infoPan);
		leftPan.addView(seekBarVaria);
		rightPan.addView(leftPan);

		imgPan.addView(img);
		bodyPanHorizontal.addView(imgPan);
		bodyPanHorizontal.addView(rightPan);

		background.addView(bodyPanHorizontal);

		this.addView(background);
		updateTimer();	


		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					if(msg.what==valueMin){
						state.setText("State : "+0+"%");
					}else if(msg.what>valueMin && msg.what<valueMax){
						state.setText("State : "+(int)(msg.what*(100f/(float)valueMax))+"%");
					}else if(msg.what==valueMax){
						state.setText("State : "+100+"%");
					}
					state.setAnimation(animation);
					new SBAnim(seekBarVaria.getProgress(),msg.what).execute();
				} catch (Exception e) {
					Log.e("handler error", "device "+wname);
					e.printStackTrace();
				}
			}	
		};
	}


	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromTouch) {
		if(progress==valueMin){
			state.setText("State : "+0+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
		}else if(progress>valueMin && progress<valueMax){
			state.setText("State : "+(int)(progress*(100f/(float)valueMax))+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 1));
		}else if(progress==valueMax){
			state.setText("State : "+100+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		}
	}



	public void onStartTrackingTouch(SeekBar arg0) {
		touching=true;
		updating=3;
	}


	public void onStopTrackingTouch(SeekBar arg0) {
		state_progress = arg0.getProgress();
		new CommandeThread().execute();
		touching=false;
	}

	public void updateTimer() {
		TimerTask doAsynchronousTask;
		final Handler handler = new Handler();
		final Timer timer = new Timer();

		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							if(getWindowVisibility()==0 || !activate){
								new UpdateThread().execute();
							}else{
								Log.e("finalize","finalize");
								timer.cancel();
								this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}});
			}
		};
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if(updating<1){
				if(domodb.requestFeatureState(dev_id, state_key)==null){
					handler.sendEmptyMessage(0);
				}else{
					handler.sendEmptyMessage(Integer.parseInt(domodb.requestFeatureState(dev_id, state_key)));
				}
			}
			updating--;
			return null;
		}
	}

	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			updating=3;
			JSONObject json_Ack = Rest_com.connect(url+"command/"+type+"/"+address+"/"+command+"/"+state_progress);
			try {
				@SuppressWarnings("unused")
				Boolean ack = JSONParser.Ack(json_Ack);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public class SBAnim extends AsyncTask<Void, Integer, Void>{
		private int begin;
		private int end;

		public SBAnim(int begin, int end){
			this.begin=begin;
			this.end=end;
		}

		@Override
		protected Void doInBackground(Void... params) {
			final int steps = java.lang.Math.abs(end-begin);
			new Thread(new Runnable() {
				public synchronized void run() {
					for(int i=0;i<=steps;i++){
						try {
							this.wait(7*scale);
							if(!touching){
								if(end-begin>0)seekBarVaria.setProgress(begin+i);
								else seekBarVaria.setProgress(begin-i);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
			return null;
		}

	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			activate=true;
		}
	}
}