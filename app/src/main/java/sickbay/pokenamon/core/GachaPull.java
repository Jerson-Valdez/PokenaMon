package sickbay.pokenamon.core;

import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;
import java.util.ArrayList;

import sickbay.pokenamon.R;
import sickbay.pokenamon.system.home.UserManager;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.system.home.BackgroundMusicManager;
import sickbay.pokenamon.system.home.PokemonListAdapter;
import sickbay.pokenamon.util.Localizer;

public class GachaPull extends AppCompatActivity {
    private final String STAR = "★";
    Button nextButton;
    TextView skipButton, sellButton;
    ImageView pokeballAnim, sprite;
    TextView name, type1, type2, rarity;
    ArrayList<PokemonDTO> summonedPokemon;
    int index = 0;
    PokemonDTO currentPokemon;
    MediaPlayer cryPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout_gacha_pull);

        BackgroundMusicManager.getInstance(this).play(R.raw.gacha_theme);

        if (getIntent().getExtras() != null) {
            summonedPokemon = getIntent().getExtras().getParcelableArrayList("pulls");

            if (summonedPokemon != null) {
                currentPokemon = summonedPokemon.get(index);
            }
        }

        skipButton = findViewById(R.id.gacha_skip_button);
        sellButton = findViewById(R.id.gacha_sell_button);
        nextButton = findViewById(R.id.gacha_next_button);
        pokeballAnim = findViewById(R.id.gacha_pokeball);
        sprite = findViewById(R.id.pokemon_sprite);
        name = findViewById(R.id.gacha_pokemon_name);
        type1 = findViewById(R.id.gacha_pokemon_type1);
        type2 = findViewById(R.id.gacha_pokemon_type2);
        rarity = findViewById(R.id.gacha_pokemon_rarity);
        cryPlayer = new MediaPlayer();
        cryPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (currentPokemon != null) {
            nextButton.setOnClickListener(v -> nextShowcase());
            skipButton.setOnClickListener((v) -> finishShowcase());

            sellButton.setOnClickListener((v) -> {
                UserManager.getInstance().sellPokemon(this, currentPokemon, this::nextShowcase);
            });

            refreshUI();
        }
    }

    private void refreshUI() {
        pokeballAnim.setVisibility(ImageView.VISIBLE);
        sprite.setVisibility(ImageView.GONE);
        name.setVisibility(TextView.GONE);
        type1.setVisibility(TextView.GONE);
        type2.setVisibility(TextView.GONE);
        rarity.setVisibility(TextView.GONE);
        skipButton.setVisibility(TextView.GONE);
        sellButton.setVisibility(TextView.GONE);
        nextButton.setVisibility(Button.GONE);

        name.setText("");
        rarity.setText("");

        if (index + 1 == summonedPokemon.size() || summonedPokemon.size() == 1) {
            nextButton.setText("Close");
        }

        spriteScale(currentPokemon.getHeight());

        try {
            setPokeballAnimation();
        } catch (Exception error) {
            Log.e("GachaPull", error.getMessage(), error);
            throw error;
        }
    }

    private void setPokeballAnimation() {
        int r = currentPokemon.getRarity();

        Glide.with(this)
                .asGif()
                .load(r == 1 ? R.raw.pokeball : r == 3 ? R.raw.greatball : r == 4 ? R.raw.ultraball : R.raw.masterball)
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull GifDrawable resource, @NonNull Object model, Target<GifDrawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        resource.setLoopCount(1);
                        resource.startFromFirstFrame();
                        resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                pokeballAnim.post(GachaPull.this::setPokemonSprite);
                            }
                        });
                        return false;
                    }
                })
                .into(pokeballAnim);
    }

    private void setPokemonSprite() {
        Glide.with(this)
                .load(currentPokemon.getSprite().getFront())
                .error(currentPokemon.getSprite().getFrontFallback())
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        try {
                            cryPlayer.reset();
                            cryPlayer.setDataSource(currentPokemon.getCry());
                            cryPlayer.prepare();
                            cryPlayer.start();
                        } catch (IOException e) {
                            Log.e("GachaPull", e.getMessage(), e);
                        }

                        pokeballAnim.setVisibility(ImageView.GONE);
                        sprite.setVisibility(ImageView.VISIBLE);
                        name.setVisibility(TextView.VISIBLE);
                        type1.setVisibility(TextView.VISIBLE);
                        rarity.setVisibility(TextView.VISIBLE);
                        sellButton.setVisibility(TextView.VISIBLE);
                        nextButton.setVisibility(Button.VISIBLE);

                        if (summonedPokemon.size() > 1) {
                            skipButton.setVisibility(TextView.VISIBLE);
                        }

                        updateTextData();
                        return false;
                    }
                })
                .fitCenter()
                .into(sprite);
    }

    private void updateTextData() {
        name.setText(Localizer.formatPokemonName(currentPokemon.getName()));
        type1.setText(Localizer.toTitleCase(currentPokemon.getTypes().get(0)));
        rarity.setText(STAR.repeat(currentPokemon.getRarity()));
        PokemonListAdapter.setTypeStrokeColor(type1, currentPokemon.getTypes().get(0));

        if (currentPokemon.getTypes().size() > 1) {
            type2.setText(Localizer.toTitleCase(currentPokemon.getTypes().get(1)));
            PokemonListAdapter.setTypeStrokeColor(type2, currentPokemon.getTypes().get(1));
            type2.setVisibility(TextView.VISIBLE);
        } else {
            type2.setVisibility(TextView.GONE);
        }
    }

    private void spriteScale(double speciesHeightInMeters) {
        int basePadding = 10;
        int dynamicPadding;

        if (speciesHeightInMeters < 1.0) {
            dynamicPadding = 100;
        } else if (speciesHeightInMeters < 3.0) {
            dynamicPadding = 40;
        } else {
            dynamicPadding = basePadding;
        }

        sprite.setPadding(dynamicPadding, dynamicPadding, dynamicPadding, dynamicPadding);
        sprite.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void finishShowcase() {
        if (cryPlayer != null) {
                cryPlayer.stop();
                cryPlayer.release();
                cryPlayer = null;
            }

            finish();
            overridePendingTransition(0, 0);
    }

    private void nextShowcase() {
        index++;
        if (index >= summonedPokemon.size()) {
            finishShowcase();
            return;
        }

        currentPokemon = summonedPokemon.get(index);
        refreshUI();
    }
}
