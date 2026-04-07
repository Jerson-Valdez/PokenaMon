package sickbay.pokenamon.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import sickbay.pokenamon.R;
import sickbay.pokenamon.helper.PokemonDetail;
import sickbay.pokenamon.model.Pokemon;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {

    private List<Pokemon> pokemonList;

    public PokemonAdapter(List<Pokemon> pokemonList) {
        this.pokemonList = pokemonList;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokemon_card, parent, false);
            return new PokemonViewHolder(view);
        } catch (Exception e) {
            Toast.makeText(parent.getContext(), "CRASH IN ONCREATEVIEW: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        Context ctx = holder.itemView.getContext();

        try {
            Pokemon pokemon = pokemonList.get(position);

            if (pokemon == null) {
                Toast.makeText(ctx, "Pokemon at position " + position + " is null!", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> types = pokemon.getTypes();

            if (holder.txtName != null) holder.txtName.setText(pokemon.getName());
            if (holder.txtLevel != null) holder.txtLevel.setText("LVL. " + pokemon.getLevel());

            if (holder.txtStars != null) {
                StringBuilder starBuilder = new StringBuilder();
                for (int i = 0; i < pokemon.getStars(); i++) {
                    starBuilder.append("★");
                }
                holder.txtStars.setText(starBuilder.toString());
            }

            if (types != null && !types.isEmpty()) {
                try {
                    int primaryColor = ContextCompat.getColor(ctx, getColorRes(ctx, types.get(0), false));

                    if (holder.txtLevel != null) holder.txtLevel.setTextColor(primaryColor);
                    if (holder.txtStars != null) holder.txtStars.setTextColor(primaryColor);

                    if (holder.txtType1 != null) {
                        holder.txtType1.setVisibility(View.VISIBLE);
                        setupTypeBadge(holder.txtType1, types.get(0));
                    }

                    if (types.size() > 1 && holder.txtType2 != null) {
                        holder.txtType2.setVisibility(View.VISIBLE);
                        setupTypeBadge(holder.txtType2, types.get(1));
                    } else if (holder.txtType2 != null) {
                        holder.txtType2.setVisibility(View.GONE);
                    }
                } catch (Exception colorEx) {
                    Toast.makeText(ctx, "Color/Type Crash: " + colorEx.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            if (holder.imgPokemon != null && pokemon.getImageUrl() != null) {
                try {
                    Glide.with(ctx)
                            .load(pokemon.getImageUrl())
                            .placeholder(R.mipmap.full_logo_foreground)
                            .into(holder.imgPokemon);
                } catch (Exception glideEx) {
                    Toast.makeText(ctx, "Glide Image Crash: " + glideEx.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, PokemonDetail.class);
                intent.putExtra("selected_pokemon", pokemon); // Passing the whole object!
                context.startActivity(intent);
            });

        } catch (Exception e) {
            Toast.makeText(ctx, "General Bind Crash pos " + position + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupTypeBadge(TextView textView, String type) {
        if (type == null || textView == null) return;

        Context context = textView.getContext();
        try {
            textView.setText(type.toUpperCase());

            int mainColor = ContextCompat.getColor(context, getColorRes(context, type, false));
            int bgColor = ContextCompat.getColor(context, getColorRes(context, type, true));

            textView.setTextColor(mainColor);

            if (textView.getBackground() != null) {
                GradientDrawable drawable = (GradientDrawable) textView.getBackground().mutate();
                drawable.setColor(bgColor);
                drawable.setStroke(3, mainColor);
            } else {
                Toast.makeText(context, "XML Missing Background for Type!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Badge Crash (" + type + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int getColorRes(Context context, String type, boolean isBackground) {
        try {
            String suffix = isBackground ? "1" : "";
            String colorName = type.toLowerCase() + suffix;

            int resId = context.getResources().getIdentifier(
                    colorName,
                    "color",
                    context.getPackageName()
            );

            return (resId != 0) ? resId : R.color.normal;
        } catch (Exception e) {
            return R.color.normal;
        }
    }

    @Override
    public int getItemCount() {
        if (pokemonList == null) {
            return 0;
        }
        return pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtType1, txtType2, txtLevel, txtStars;
        ImageView imgPokemon;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                txtName = itemView.findViewById(R.id.pokemon_name);
                txtType1 = itemView.findViewById(R.id.pokemon_type);
                txtType2 = itemView.findViewById(R.id.pokemon_type2);
                txtLevel = itemView.findViewById(R.id.pokemon_level);
                txtStars = itemView.findViewById(R.id.pokemon_stars);
                imgPokemon = itemView.findViewById(R.id.pokemon_image);
            } catch (Exception e) {
                // If this fails, your XML IDs do not match these R.id variables
                Log.e("ViewHolderCrash", e.getMessage());
            }
        }
    }
}