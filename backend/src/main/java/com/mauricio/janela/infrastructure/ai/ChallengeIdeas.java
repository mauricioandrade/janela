package com.mauricio.janela.infrastructure.ai;

import com.mauricio.janela.domain.model.Activity;
import com.mauricio.janela.domain.model.Language;
import com.mauricio.janela.domain.model.Location;
import com.mauricio.janela.domain.model.OutdoorWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Small, concrete "touch grass" challenges, by activity and by the part of the day a window falls in. Picking the idea
 * in code keeps the model from repeating the same breeze-on-your-face line; the model only adapts it to the place.
 * The pick is stable for a given place, window and activity, so the same search reads the same.
 */
final class ChallengeIdeas {

    private record Idea(String pt, String en) {
    }

    private static final Map<Activity, List<Idea>> BY_ACTIVITY = Map.of(
            Activity.WORKOUT, List.of(
                    new Idea("termine com dois minutos de alongamento na grama",
                            "finish with two minutes of stretching on the grass"),
                    new Idea("faça a última série descalço, sentindo o chão",
                            "do your last set barefoot, feeling the ground"),
                    new Idea("entre as séries, ache três tons de verde diferentes à sua volta",
                            "between sets, find three different shades of green around you"),
                    new Idea("descanse um minuto à sombra de uma árvore antes de ir embora",
                            "rest for a minute in a tree's shade before you leave")),
            Activity.RUN, List.of(
                    new Idea("no último trecho, conte três cantos de pássaros diferentes",
                            "on the last stretch, count three different bird calls"),
                    new Idea("corra um trecho sem fone, só ouvindo seus passos",
                            "run one stretch without headphones, just your footsteps"),
                    new Idea("ache a sombra mais fresca do percurso e pare nela por trinta segundos",
                            "find the coolest shade on your route and stand in it for thirty seconds"),
                    new Idea("termine descalço na grama por um minuto",
                            "finish barefoot on the grass for one minute")),
            Activity.WALK, List.of(
                    new Idea("toque no tronco de três árvores diferentes e compare a casca",
                            "touch the bark of three different trees and compare them"),
                    new Idea("entre numa rua que você nunca percorreu",
                            "turn into a street you have never walked"),
                    new Idea("ache uma folha caída bonita e leve para casa",
                            "pick up the best fallen leaf you can find and take it home"),
                    new Idea("caminhe cinco minutos com o celular no bolso, olhando para cima",
                            "walk five minutes with your phone in your pocket, looking up")),
            Activity.BIKE, List.of(
                    new Idea("pare uma vez e olhe o céu por trinta segundos",
                            "stop once and watch the sky for thirty seconds"),
                    new Idea("pedale um trecho devagar e sinta o ar mudar entre o sol e a sombra",
                            "ride one stretch slowly and feel the air change between sun and shade"),
                    new Idea("conte as árvores floridas que você passar",
                            "count the flowering trees you pass"),
                    new Idea("termine numa praça e fique cinco minutos parado",
                            "end at a square and sit still for five minutes")),
            Activity.PICNIC, List.of(
                    new Idea("tire os sapatos e pise na grama por um minuto",
                            "take your shoes off and stand on the grass for one minute"),
                    new Idea("deite e encontre formas em três nuvens",
                            "lie back and find shapes in three clouds"),
                    new Idea("leve uma fruta da estação e coma à sombra",
                            "bring a fruit in season and eat it in the shade"),
                    new Idea("fique cinco minutos em silêncio ouvindo o lugar",
                            "spend five minutes in silence, just listening to the place")),
            Activity.GARDENING, List.of(
                    new Idea("afunde as mãos na terra e sinta a temperatura dela antes de começar",
                            "push your hands into the soil and feel its temperature before you start"),
                    new Idea("cheire uma folha de cada planta que você cuidar",
                            "smell a leaf of every plant you tend"),
                    new Idea("procure uma minhoca ou uma formiga trabalhando",
                            "look for a worm or an ant at work"),
                    new Idea("regue uma planta que não é sua, de um vizinho ou da calçada",
                            "water a plant that is not yours, a neighbour's or one on the sidewalk")));

    private static final List<Idea> DAWN = List.of(
            new Idea("veja o céu mudar de cor por dois minutos", "watch the sky change colour for two minutes"),
            new Idea("repare no orvalho na grama antes que ele seque", "notice the dew on the grass before it dries"),
            new Idea("escute o primeiro coro de pássaros do dia", "listen to the first bird chorus of the day"));

    private static final List<Idea> DAYTIME = List.of(
            new Idea("ache a árvore com a sombra mais larga e fique um minuto embaixo dela",
                    "find the tree with the widest shade and stand under it for a minute"),
            new Idea("observe as nuvens se moverem por um minuto", "watch the clouds move for one minute"));

    private static final List<Idea> DUSK = List.of(
            new Idea("assista o sol baixar por dois minutos, sem foto", "watch the sun go down for two minutes, no photo"),
            new Idea("procure a primeira estrela ou planeta", "look for the first star or planet"),
            new Idea("repare no ar esfriando quando o sol some", "notice the air cooling as the sun disappears"));

    private ChallengeIdeas() {
    }

    /** One idea for this window, in the reader's language. */
    static String pick(Activity activity, OutdoorWindow window, Location location, Language language) {
        int hour = window.start().getHour();
        List<Idea> pool = new ArrayList<>(BY_ACTIVITY.get(activity));
        pool.addAll(hour < 9 ? DAWN : hour >= 16 ? DUSK : DAYTIME);
        int index = Math.floorMod(Objects.hash(location.name(), window.start(), activity), pool.size());
        Idea idea = pool.get(index);
        return language == Language.EN ? idea.en() : idea.pt();
    }

    /** Every idea in the pool for an activity and hour, for tests. */
    static List<String> poolFor(Activity activity, int hour, Language language) {
        List<Idea> pool = new ArrayList<>(BY_ACTIVITY.get(activity));
        pool.addAll(hour < 9 ? DAWN : hour >= 16 ? DUSK : DAYTIME);
        return pool.stream().map(idea -> language == Language.EN ? idea.en() : idea.pt()).toList();
    }
}
