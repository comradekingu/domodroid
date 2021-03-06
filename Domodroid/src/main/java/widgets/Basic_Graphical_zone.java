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

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import Abstract.translate;
import Event.Event_to_navigate_house;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import misc.tracerengine;

public class Basic_Graphical_zone extends FrameLayout implements OnClickListener {

    private final ImageView IV_img;
    final TextView TV_name;
    private final int id;
    //private int session_type;
    final String name;
    private final String type;

    //public Graphical_Feature(Context context,int id,String name_room, String description_room, String icon, int widgetSize, int session_type) {
    public Basic_Graphical_zone(tracerengine Trac, Context context, int id, String name, String description, String icon, int widgetSize, String type) {
        super(context);
        this.id = id;
        this.name = name;
        this.type = type;
        //this.session_type = session_type;
        this.setPadding(5, 5, 5, 5);
        setOnClickListener(this);

        //panel with border
        LinearLayout LL_background = new LinearLayout(context);
        if (widgetSize == 0)
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        else
            LL_background.setLayoutParams(new LayoutParams(widgetSize, LayoutParams.WRAP_CONTENT));

        LL_background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("black", LL_background.getHeight()));

        //panel to set img with padding left
        FrameLayout FL_imgPan = new FrameLayout(context);
        FL_imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        FL_imgPan.setPadding(5, 8, 10, 10);
        //img
        IV_img = new ImageView(context);
        IV_img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        change_this_icon(icon);

        //info panel
        LinearLayout LL_infoPan = new LinearLayout(context);
        LL_infoPan.setOrientation(LinearLayout.VERTICAL);
        LL_infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        LL_infoPan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        LL_infoPan.setPadding(0, 0, 10, 0);


        //name of zone
        TV_name = new TextView(context);
        TV_name.setText(name);
        TV_name.setTextSize(18);
        TV_name.setTextColor(Color.WHITE);
        TV_name.setGravity(Gravity.RIGHT);

        //description
        TextView TV_description = new TextView(context);
        try {
            TV_description.setText(context.getResources().getString(translate.do_translate(getContext(), Trac, description)));
        } catch (Exception e) {
            String mytag = "Basic_Graphical_zone";
            TV_description.setText(description);
        }
        TV_description.setTextSize(17);
        TV_description.setGravity(Gravity.RIGHT);

        LL_infoPan.addView(TV_name);
        LL_infoPan.addView(TV_description);
        FL_imgPan.addView(IV_img);

        LL_background.addView(FL_imgPan);
        LL_background.addView(LL_infoPan);

        this.addView(LL_background);
    }

    public void onClick(View v) {
        EventBus.getDefault().post(new Event_to_navigate_house(id, type, name));
    }

    void change_this_icon(String icon) {
        IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));
    }
}

