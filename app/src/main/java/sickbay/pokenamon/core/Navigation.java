package sickbay.pokenamon.core;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

import sickbay.pokenamon.R;

public class Navigation {

    public static void setup(Activity activity) {
        ImageView navHome = activity.findViewById(R.id.nav_home);
        ImageView navBattle = activity.findViewById(R.id.nav_battle);
        ImageView navSummon = activity.findViewById(R.id.nav_summon);
        ImageView navCollection = activity.findViewById(R.id.nav_collection);

        if (navHome == null) return;

        navHome.setBackgroundResource(0);
        navBattle.setBackgroundResource(0);
        navSummon.setBackgroundResource(0);
        navCollection.setBackgroundResource(0);

        if (activity instanceof Home) {
            navHome.setBackgroundResource(R.drawable.rect);
        }  else if (activity instanceof Battle) {
            navBattle.setBackgroundResource(R.drawable.rect);
        } else if (activity instanceof Gacha) {
            navSummon.setBackgroundResource(R.drawable.rect);
        } else {
            navCollection.setBackgroundResource(R.drawable.rect);
        }

        navHome.setOnClickListener(v -> {
            if (!(activity instanceof Home)) {
                Intent intent = new Intent(activity, Home.class);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish();
            }
        });

        navBattle.setOnClickListener(v -> {
            if (!(activity instanceof Battle)) {
                Intent intent = new Intent(activity, Battle.class);
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
    }
}