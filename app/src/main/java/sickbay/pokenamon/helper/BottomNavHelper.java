package sickbay.pokenamon.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;
import android.widget.Toast;

import sickbay.pokenamon.R;
import sickbay.pokenamon.core.Battle;
import sickbay.pokenamon.core.Collection;
import sickbay.pokenamon.core.Gacha;
import sickbay.pokenamon.core.Home;

public class BottomNavHelper {

    public static void setup(Activity activity) {
        LinearLayout navHome = activity.findViewById(R.id.nav_home);
        LinearLayout navBattle = activity.findViewById(R.id.nav_battle);
        LinearLayout navSummon = activity.findViewById(R.id.nav_summon);
        LinearLayout navCollection = activity.findViewById(R.id.nav_collection);

        if (navHome == null) return;

        navHome.setBackgroundResource(0);
        navBattle.setBackgroundResource(0);
        navSummon.setBackgroundResource(0);
        navCollection.setBackgroundResource(0);

        if (activity instanceof Home) {
            navHome.setBackgroundResource(R.drawable.rect);
        } else if (activity instanceof Gacha) {
            navSummon.setBackgroundResource(R.drawable.rect);
        } else if (activity instanceof Collection) {
            navCollection.setBackgroundResource(R.drawable.rect);
        } else if (activity instanceof Battle) {
            navBattle.setBackgroundResource(R.drawable.rect);
        }
        navHome.setOnClickListener(v -> {
            if (!(activity instanceof Home)) {
                Intent intent = new Intent(activity, Home.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }
        });

        navSummon.setOnClickListener(v -> {
            if (!(activity instanceof Gacha)) {
                Intent intent = new Intent(activity, Gacha.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }
        });

        navCollection.setOnClickListener(v -> {
            if (!(activity instanceof Collection)) {
                Intent intent = new Intent(activity, Collection.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }
        });

        //other buttons
    }
}