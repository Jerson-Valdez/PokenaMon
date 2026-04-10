package sickbay.pokenamon.system.gacha;

import java.util.ArrayList;

import sickbay.pokenamon.db.dto.PokemonDTO;

public interface FetchGachaListener {
    void onComplete(ArrayList<PokemonDTO> results);
    void onError(String message);
}
