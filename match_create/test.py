import random


def generate_schedule(num_teams=25, num_fields=4, num_rounds=8, teams_per_game=4):
    teams = list(range(1, num_teams + 1))
    schedule = {
        f"Round {i + 1}": {f"Field {chr(65 + j)}": [] for j in range(num_fields)}
        for i in range(num_rounds)
    }
    team_appearances = {team: 0 for team in teams}
    field_1_appearances = {team: 0 for team in teams}

    for rnd in range(num_rounds):
        round_teams = teams[:]
        random.shuffle(round_teams)
        for fld in range(num_fields):
            game_teams = round_teams[
                fld * teams_per_game : fld * teams_per_game + teams_per_game
            ]
            for team in game_teams:
                team_appearances[team] += 1
                if fld == 0:
                    field_1_appearances[team] += 1
            schedule[f"Round {rnd + 1}"][f"Field {chr(65 + fld)}"] = game_teams

    # Ensure each team plays at least once on Field 1
    for team in teams:
        if field_1_appearances[team] == 0:
            for rnd in range(num_rounds):
                if len(schedule[f"Round {rnd + 1}"]["Field A"]) < teams_per_game:
                    schedule[f"Round {rnd + 1}"]["Field A"].append(team)
                    field_1_appearances[team] += 1
                    break

    return schedule


# Generate and print the schedule
schedule = generate_schedule()
for rnd, fields in schedule.items():
    print(rnd)
    for field, teams in fields.items():
        print(f"  {field}: {teams}")
