package sickbay.pokenamon.system.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.core.PokemonView;
import sickbay.pokenamon.db.dto.PokemonDTO;
import sickbay.pokenamon.util.Localizer;

public class PokemonListAdapter extends RecyclerView.Adapter<PokemonListAdapter.ViewHolder> {
    private final String STAR = "★";
    private final Context context;
    private final List<PokemonDTO> pokemons;

    public PokemonListAdapter(Context context, List<PokemonDTO> pokemons) {
        this.context = context;
        this.pokemons = pokemons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_pokemon_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PokemonDTO pokemon = pokemons.get(position);

        Glide.with(context)
            .load(pokemon.getSprite().getFrontFallback())
            .into(holder.sprite);

        holder.name.setText(Localizer.formatPokemonName(pokemon.getName()));
        holder.type1.setText(Localizer.toTitleCase(pokemon.getTypes().get(0)));
        PokemonListAdapter.setTypeStrokeColor(holder.type1, pokemon.getTypes().get(0));

        if (pokemon.getTypes().size() > 1) {
            holder.type2.setText(Localizer.toTitleCase(pokemon.getTypes().get(1)));
            PokemonListAdapter.setTypeStrokeColor(holder.type2, pokemon.getTypes().get(1));
            holder.type2.setVisibility(TextView.VISIBLE);
        } else {
            holder.type2.setVisibility(TextView.GONE);
        }

        holder.rarity.setText(STAR.repeat(pokemon.getRarity()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PokemonView.class);
            intent.putExtra("pokemon", pokemon);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView sprite;
        TextView name, type1, type2;
        TextView rarity;

        public ViewHolder(View view) {
            super(view);

            sprite = view.findViewById(R.id.pokemon_sprite);
            name = view.findViewById(R.id.pokemon_name);
            type1 = view.findViewById(R.id.pokemon_type1);
            type2 = view.findViewById(R.id.pokemon_type2);
            rarity = view.findViewById(R.id.pokemon_rarity);
        }
    }

    public static void setTypeStrokeColor(TextView view, String type) {
        GradientDrawable bg = (GradientDrawable) view.getBackground();
        Context ctx = view.getContext().getApplicationContext();
        String packageName = ctx.getPackageName();
        String color = type.toLowerCase();

        bg.mutate();
        bg.setStroke(2, ctx.getColor(ctx.getResources().getIdentifier(color, "color", packageName)));
    }
}
