# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

People in Brazilian (tropical) cities deciding when to do an outdoor activity — run, walk, bike, picnic,
garden — in the next one to three days. They use it on the phone while planning their day and on a laptop;
both matter equally.

A second, decisive audience for now: judges and readers of the DEV "Hacktoberfest 2026 Open-Source AI
Challenge — Week 1: Touch Grass" post, who meet the product through the demo, screenshots and README on a
desktop.

## Product Purpose

Janela (Portuguese for *window*) finds the best time windows to go outside, based on what matters in a
tropical climate: feels-like heat, UV index, rain chance (afternoon storms) and wind. It then explains the
choice in a short, human recommendation with a tiny "touch grass" challenge.

Success in this phase: win the challenge's **Best Use of Gemma** category — the product must make it evident
that an open-weight model running locally explains decisions that deterministic code computed — while being
genuinely useful.

## Positioning

**Java computes, Gemma explains.** Scoring is plain, unit-tested Java; the local model (Gemma via Ollama)
never picks times or does math — it receives ranked windows and writes about them, so it cannot hallucinate
the forecast. No API keys, no per-query cost, nothing about the person's plans sent to an AI cloud, and it
still works with the model off (template text, clearly labelled).

## Operating Context

- Runs locally: Spring Boot API on :8080, React app on :5173, Ollama with `gemma3:4b` by default.
- A recommendation takes about 15 s with Gemma, a few seconds with the template.
- Forecast and geocoding come from Open-Meteo's open API (the only call that leaves the machine);
  attribution (CC BY 4.0) must stay visible in the UI.
- Searches are shareable as URLs (`?city=&cityId=&activity=&duration=&days=&lang=`).

## Capabilities and Constraints

- Inputs: city (with suggestions and an exact-place pick), activity (RUN, WALK, BIKE, PICNIC, GARDENING),
  duration (15–480 min, rounded up to whole hours), next 1–3 days that still have daylight, language pt/en.
- Output: up to 3 non-overlapping daytime windows spread across the days, each with a 0–100 score,
  feels-like temperature, max UV (with WHO category), max rain chance and max wind; plus the narrative,
  whether it is AI-generated, and the model name.
- No windows is a real outcome; the template answers it, never the model.
- API: `GET /api/v1/windows`, `GET /api/v1/cities`; errors are RFC 9457 problem details.

## Brand Commitments

- Name: **janela**, lowercase.
- Mark: a window of four panes with one lit by the sun (the favicon).
- Bilingual pt-BR / en with a visible switch.
- A visible badge stating whether the text was written locally by Gemma (with the model name) or by the
  offline template.

## Evidence on Hand

- Real live output from the running app (no fixtures needed); screenshots in `docs/`.
- No testimonials, users, metrics or press exist — do not invent any.

## Product Principles

1. The answer first: when to go, then why.
2. Honest about the machine: say what the model wrote, what the code computed, and what left the computer.
3. Tropical reality over generic weather: heat, UV and storms are the story.
4. Works without AI; better with it.
