import itertools

import pandas as pd

from team import Team


def create_team_liste(length=10):
    return [i for i in range(length)]


def count_in_list(temp_list, x):
    counter = 0
    for element in temp_list:
        if isinstance(element, list) or isinstance(element, tuple):
            counter += count_in_list(element, x)
        else:
            if element == x:
                counter += 1
    return counter


def flatten(liste):
    ergebnis = []
    for element in liste:
        if isinstance(element, list) or isinstance(element, tuple):
            ergebnis.extend(flatten(element))
        elif isinstance(element, (int, float, Team)):
            ergebnis.append(element)
    return ergebnis


def create_duells(t_l):
    combs = itertools.combinations(t_l, 2)
    df = pd.DataFrame(combs, columns=['team_1', 'team_2']).assign(num_games=0)
    return df


def create_filled_df(_t_l, plan_games):
    combs = itertools.combinations(_t_l, 2)
    df = pd.DataFrame(combs, columns=['team_1', 'team_2']).assign(num_games=0)
    for _round in plan_games:
        for _field in _round:
            for _t_1_idx in range(len(_field)):
                for _t_2 in _field[_t_1_idx:]:
                    _bed = (df['team_1'] == _field[_t_1_idx]) & (df['team_2'] == _t_2)

                    df.loc[_bed, 'num_games'] += 1
    return df


def check_game_plan(plan, eval_plan=None):
    output_string = ''

    output_string += str(plan)
    max_games_count = set(iter([0]))

    if plan and isinstance(plan[0][0][0], Team):
        teams = sorted(list(set(flatten(plan))), key=lambda team: team.name)
    else:
        teams = sorted(list(set(flatten(plan))))
    df = create_filled_df(teams, plan)
    for num in range(df['num_games'].max(), -1, -1):
        output_string += '\n' + str(len(df[df['num_games'] == num])) + ' duells repeat ' + str(num) + ' times'
    num_games = set()
    for t in teams:
        num_games.add(count_in_list(plan, t))
    output_string += '\n' + 'Every team plays between ' + str(num_games)
    if eval_plan:
        dic = dict()
        for r_idx in range(len(plan)):
            for f_idx in range(len(plan[r_idx])):
                for t_idx in range(len(plan[r_idx][f_idx])):
                    if eval_plan[r_idx][f_idx][t_idx]:
                        if dic.get(plan[r_idx][f_idx][t_idx]):
                            dic[plan[r_idx][f_idx][t_idx]] += 1
                        else:
                            dic[plan[r_idx][f_idx][t_idx]] = 1
        output_string += '\n' + 'Every team has same amount of rating games: ' + str(
            len(set(dic.values())) == 1) + ' = ' + str(set(dic.values()))
        max_games_count = set(dic.values())
    switch_1 = set()
    for r in plan:
        for t in r[0]:
            switch_1.add(t)
    output_string += '\n' + 'Every team at least once at switch 1: ' + str(len(switch_1) == len(teams))
    print(output_string)

    return max_games_count