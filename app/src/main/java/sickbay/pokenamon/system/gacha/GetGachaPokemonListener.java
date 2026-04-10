package sickbay.pokenamon.system.gacha;

import sickbay.pokenamon.model.Pokemon;

public interface GetGachaPokemonListener {
    void onComplete(Pokemon pokemon);
    void onError(String message);
}
