from helper import create_team_liste, count_in_list, create_duells, flatten, check_game_plan, create_filled_df
import random
import pandas as pd


def create_plan(team_list, number_fields=4, number_rounds=8, max_duell_repeats=100, min_searches=0, max_searches=20,
                seed_counter=0):
    def get_best_combination(teams, df):
        """gets the best combinations of given teams
        """
        combinations = []
        pairs = []
        i = 0
        while i < len(teams):
            t_1 = teams[i]
            j = i + 1
            i += 1
            while j < len(teams):
                t_2 = teams[j]
                j += 1
                row = df[((df['team_1'] == t_1) & (df['team_2'] == t_2)) |
                         ((df['team_1'] == t_2) & (df['team_2'] == t_1))]
                if not row.empty:
                    n_g = row['num_games'].values[0]
                    if n_g == 0:
                        teams.remove(t_1)
                        teams.remove(t_2)
                        pairs.append({t_1, t_2})
                        i = 0
                        break
        if teams:
            while len(teams) >= 2:
                t_1 = teams[0]
                t_2 = teams[1]
                teams.remove(t_1)
                teams.remove(t_2)
                pairs.append({t_1, t_2})
        while len(pairs) > 1:
            min_num = (-1, -1)
            p_1 = pairs[0]
            for j in range(1, len(pairs) - 1):
                p_2 = pairs[j]
                temp_sum = 0
                for t_1 in p_1:
                    for t_2 in p_2:
                        row = df[((df['team_1'] == t_1) & (df['team_2'] == t_2)) |
                                 ((df['team_1'] == t_2) & (df['team_2'] == t_1))]
                        temp_sum += row['num_games'].values[0]
                if temp_sum < min_num[0] or min_num[0] == -1:
                    min_num = (temp_sum, j)
            combinations.append(list(p_1.union(pairs[min_num[1]])))
            pairs = [p for p in pairs if p not in [p_1, pairs[min_num[1]]]]
        return combinations

    def sync_df(r_plan, df):
        for teams_on_field in r_plan:
            teams_on_field.sort()
            for t_1_idx in range(len(teams_on_field)):
                for t_2 in teams_on_field[t_1_idx:]:
                    bedingung = (df['team_1'] == teams_on_field[t_1_idx]) & (df['team_2'] == t_2)
                    df.loc[bedingung, 'num_games'] += 1
        return df

    def sort_game_plan(g_plan, teams):
        new_g_plan = []
        t_f_s = {t: 0 for t in teams}  # key: team, value: times on switch 1
        for r_plan_idx in range(len(g_plan)):
            temp_sums = []
            for f_plan in g_plan[r_plan_idx]:
                temp_sum = 0
                for t in f_plan:
                    s = t_f_s[t]
                    if s:
                        temp_sum += s
                    else:
                        temp_sum -= 100
                temp_sums.append(temp_sum)
            temp_zip = zip(g_plan[r_plan_idx], temp_sums)
            sorted_zip = sorted(temp_zip, key=lambda x: x[1])
            new_r_plan, _ = zip(*sorted_zip)
            for t in new_r_plan[0]:
                t_f_s[t] += 1
            new_g_plan.append(new_r_plan)
        zero_teams = []
        for key, value in t_f_s.items():
            if value == 0:
                zero_teams.append(key)
        if len(zero_teams) >= 2:
            return new_g_plan, False
        elif len(zero_teams) == 1:
            zero_team = zero_teams[0]
            if count_in_list(new_g_plan[-1], zero_team):
                temp = -1
                for t_idx in range(len(new_g_plan[-1][0])):
                    if t_f_s[new_g_plan[-1][0][t_idx]] > 1:
                        temp = t_idx
                        t_f_s[new_g_plan[-1][0][t_idx]] -= 1
                        break
                if temp != -1:
                    for f_idx in range(1, len(new_g_plan[-1][1:])):
                        for t_idx in range(len(new_g_plan[-1][f_idx])):
                            if new_g_plan[-1][f_idx][t_idx] == zero_team:
                                new_g_plan[-1][f_idx][t_idx] = new_g_plan[-1][0][temp]
                                new_g_plan[-1][0][temp] = zero_team
                                t_f_s[zero_team] += 1
            else:
                return new_g_plan, False
        return new_g_plan, True

    if seed_counter == 0:
        if len(team_list) == 15:
            seed_counter = 17
        elif len(team_list) == 16:
            seed_counter = 15
        elif len(team_list) == 17:
            seed_counter = 4
        elif len(team_list) == 18:
            seed_counter = 3955
        elif len(team_list) == 19:
            seed_counter = 3823
        elif len(team_list) == 20:
            seed_counter = 452
        elif len(team_list) == 21:
            seed_counter = 18
        elif len(team_list) == 22:
            seed_counter = 28
        elif len(team_list) == 23:
            seed_counter = 60
        elif len(team_list) == 24:
            seed_counter = 33
        elif len(team_list) == 25:
            seed_counter = 62

    i = 0
    curr_best = (None, 1234, 1234, 0)
    while True:
        skip = False
        random.seed(seed_counter + i)
        i += 1
        game_plan = []
        t_l = team_list[:]
        df_duells = create_duells(t_l)
        idx_last = len(t_l) - 1  # all teams after this index have more games played then the ones before

        for number_round in range(number_rounds):
            selection = t_l[0:number_fields * 4]
            round_plan = get_best_combination(selection, df_duells)
            df_duells = sync_df(round_plan, df_duells)
            if df_duells['num_games'].max() > max_duell_repeats:
                skip = True
                break
            game_plan.append(round_plan)
            if idx_last < number_fields * 4:
                new_beginning = t_l[number_fields * 4:]
                new_mid = t_l[0:idx_last + 1]
                new_end = t_l[idx_last + 1:number_fields * 4]
                extend_beginning = new_beginning + new_mid
                random.shuffle(extend_beginning)
                idx_last += len(t_l[number_fields * 4:])
                t_l = extend_beginning + new_end
            else:
                new_beginning = t_l[number_fields * 4:]
                new_end = t_l[0:number_fields * 4]
                random.shuffle(new_end)
                idx_last -= number_fields * 4
                t_l = new_beginning + new_end

        print(i)
        if not skip:
            temp_game_plan, valid = sort_game_plan(game_plan[:], team_list)
            df_duells_new = create_filled_df(team_list, temp_game_plan)

            if valid and df_duells['num_games'].max() >= df_duells_new['num_games'].max() and df_duells_new[
                'num_games'].max() <= max_duell_repeats:
                if curr_best[1] > df_duells_new['num_games'].max() or (
                        curr_best[1] == df_duells_new['num_games'].max() and curr_best[2] > len(
                    df_duells_new[df_duells['num_games'] == df_duells_new['num_games'].max()])):
                    curr_best = (temp_game_plan, df_duells_new['num_games'].max(),
                                 len(df_duells_new[df_duells['num_games'] == df_duells_new['num_games'].max()]),
                                 seed_counter + i - 1)

            if valid and i >= min_searches and curr_best[1] <= max_duell_repeats:
                print("SEED:", curr_best[3])
                return curr_best[0]
        if i >= max_searches:
            return curr_best[0]


def get_unrated_games(game_plan):
    teams = list(set(flatten(game_plan)))
    df = pd.DataFrame({'team': teams}).assign(num_games=0)
    min_games = -1
    for t in teams:
        c = count_in_list(game_plan, t)
        if min_games > c or min_games == -1:
            min_games = c
    rate_game_plan = []
    for r in game_plan:
        rate_game_plan.append([])
        for f in r:
            rate_game_plan[-1].append([])
            for t in f:
                if df[df['team'] == t]['num_games'].values.tolist()[0] >= min_games:
                    rate_game_plan[-1][-1].append(False)
                else:
                    df.loc[(df['team'] == t), 'num_games'] += 1
                    rate_game_plan[-1][-1].append(True)
    return rate_game_plan

def generate_plan(num_teams=25, num_fields=4, num_rounds=8):
    team_list = create_team_liste(num_teams)
    plan = create_plan(team_list, num_fields, num_rounds)
    print(plan)
    rate_plan = get_unrated_games(plan)
    print(rate_plan)
    max_games_count = check_game_plan(plan, rate_plan)
    return plan, max_games_count

# if __name__ == '__main__':
#     plan = create_plan(create_team_liste(25), 4, 8)
#     print(plan)
#     rate_plan = get_unrated_games(plan)
#     print(rate_plan)
#     check_game_plan(plan, rate_plan)
