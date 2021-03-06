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

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.send_command;


public class Graphical_Range extends Basic_Graphical_widget implements SeekBar.OnSeekBarChangeListener {

    private TextView state;
    private SeekBar seekBarVaria;
    private int scale;
    private int valueMin = 0;
    private int valueMax = 100;
    private int CustomMax;
    private final boolean activate = false;
    private Animation animation;
    private boolean touching;
    public FrameLayout container = null;
    private final FrameLayout myself = null;
    private static String mytag;

    private String stateS = "";
    private String test_unite;
    private String command_id = null;
    private String command_type = null;
    private final Entity_Feature feature;
    private final int session_type;
    private JSONObject jparam;
    private int dev_id;
    private String state_key;
    private int status;
    private String Value_timestamp;

    public Graphical_Range(tracerengine Trac,
                           final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                           final Entity_Feature feature) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Range(tracerengine Trac,
                           final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                           final Entity_Map feature_map) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        String state_key = feature.getState_key();
        String parameters = feature.getParameters();
        String address = feature.getAddress();
        try {
            this.stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            this.stateS = state_key;
        }
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }
        mytag = "Graphical_Range(" + dev_id + ")";
        int stateThread = 1;

        //get parameters
        try {
            jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
        } catch (JSONException e) {
            Tracer.i(mytag, "No parameters");
            seekBarVaria.setEnabled(false);
        }
        if (api_version >= 0.7f) {
            try {
                int number_of_command_parameters = jparam.getInt("number_of_command_parameters");
                if (number_of_command_parameters == 1) {
                    command_id = jparam.getString("command_id");
                    command_type = jparam.getString("command_type1");
                }
            } catch (JSONException e) {
                Tracer.i(mytag, "No parameters for command");
                seekBarVaria.setEnabled(false);
            }
        } else {
            try {
                String command = jparam.getString("command");
                valueMin = jparam.getInt("valueMin");
                valueMax = jparam.getInt("valueMax");
            } catch (JSONException e) {
                Tracer.i(mytag, "No parameters for command");
                seekBarVaria.setEnabled(false);
            }
            int range = valueMax - valueMin;
            scale = 100 / range;
        }
        try {
            test_unite = jparam.getString("unit");
        } catch (JSONException e) {
            test_unite = "%";
        }
        if (test_unite == null || test_unite.length() == 0) {
            test_unite = "%";
        }
        String[] model = feature.getDevice_type_id().split("\\.");
        String type = model[0];

        //linearlayout horizontal body
        LinearLayout bodyPanHorizontal = new LinearLayout(activity);
        bodyPanHorizontal.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER_VERTICAL));
        bodyPanHorizontal.setOrientation(LinearLayout.HORIZONTAL);

        //right panel with different info and seekbars
        FrameLayout rightPan = new FrameLayout(activity);
        rightPan.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        rightPan.setPadding(0, 0, 10, 0);

        // panel
        LinearLayout leftPan = new LinearLayout(activity);
        leftPan.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.BOTTOM));
        leftPan.setOrientation(LinearLayout.VERTICAL);
        leftPan.setGravity(Gravity.CENTER_VERTICAL);
        leftPan.setPadding(4, 5, 0, 0);

        state = new TextView(activity);
        state.setTextColor(Color.BLACK);
        state.setPadding(20, 0, 0, 0);
        state.setText(stateS);

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        //first seekbar variator
        seekBarVaria = new SeekBar(activity);
        seekBarVaria.setProgress(0);
        if (api_version < 0.7f)
            seekBarVaria.setMax(valueMax - valueMin);
        seekBarVaria.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
        seekBarVaria.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbarvaria));
        seekBarVaria.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
        seekBarVaria.setThumbOffset(-3);
        seekBarVaria.setOnSeekBarChangeListener(this);
        seekBarVaria.setPadding(0, 0, 15, 7);

        leftPan.addView(state);
        leftPan.addView(seekBarVaria);
        rightPan.addView(leftPan);
        bodyPanHorizontal.addView(rightPan);
        super.LL_topPan.removeView(super.LL_featurePan);
        super.LL_infoPan.addView(bodyPanHorizontal);


        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    try {
                        status = Integer.parseInt(session.getValue());
                    } catch (Exception e) {
                        status = 0;
                    }
                    Value_timestamp = session.getTimestamp();
                    update_display();
                    //register eventbus for new value
                    EventBus.getDefault().register(this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
    }


    /**
     * @param event an Entity_client_event_value from EventBus when a new value is received from widgetupdate.
     */
    @Subscribe
    public void onEvent(Entity_client_event_value event) {
        // your implementation
        Tracer.d(mytag, "Receive event from Eventbus" + event.Entity_client_event_get_id() + " With value" + event.Entity_client_event_get_val());
        if (event.Entity_client_event_get_id() == dev_id) {
            status = Integer.parseInt(event.Entity_client_event_get_val());
            Value_timestamp = event.Entity_client_event_get_timestamp();
            update_display();
        }
    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);
                //#1649
                //Value min and max should be the limit of the widget
                if (status <= valueMin) {
                    state.setText(stateS + " : " + valueMin + " " + test_unite);
                } else if (status > valueMin && status < valueMax) {
                    state.setText(stateS + " : " + status + " " + test_unite);
                } else if (status >= valueMax) {
                    state.setText(stateS + " : " + valueMax + " " + test_unite);
                }
                state.setAnimation(animation);
                new SBAnim(seekBarVaria.getProgress(), status - valueMin).execute();
            }
        });
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        //#1649
        //Value min and max should be the limit of the widget
        int realprogress = (progress + valueMin);
        if (realprogress <= valueMin) {
            state.setText(stateS + " : " + valueMin + " " + test_unite);
            change_this_icon(0);
        } else if (realprogress > valueMin && realprogress < valueMax) {
            state.setText(stateS + " : " + realprogress + " " + test_unite);
            change_this_icon(1);
        } else if (realprogress >= valueMax) {
            state.setText(stateS + " : " + valueMax + " " + test_unite);
            change_this_icon(2);
        }
    }


    public void onStartTrackingTouch(SeekBar arg0) {
        touching = true;
        int updating = 3;
    }


    public void onStopTrackingTouch(SeekBar arg0) {
        //send the correct value by replacing it with a converted one.
        int state_progress = arg0.getProgress() + valueMin;
        send_command.send_it(activity, Tracer, command_id, command_type, String.valueOf(state_progress), api_version);
        touching = false;
    }

    public class SBAnim extends AsyncTask<Void, Integer, Void> {
        private final int begin;
        private final int end;

        public SBAnim(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final int steps = java.lang.Math.abs(end - begin);
            new Thread(new Runnable() {
                public synchronized void run() {
                    for (int i = 0; i <= steps; i++) {
                        try {
                            this.wait(7 * scale);
                            if (!touching) {
                                if (end - begin > 0) seekBarVaria.setProgress(begin + i);
                                else seekBarVaria.setProgress(begin - i);
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
        if (visibility == View.VISIBLE) {
            //activate=true;
        }
    }

}