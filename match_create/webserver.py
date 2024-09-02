from http.cookiejar import debug

from flask import Flask, request, jsonify, Response

from match_create.generate_gameplay_lists import create_plan, get_unrated_games, generate_plan
from match_create.helper import create_team_liste, check_game_plan

app = Flask(__name__)

@app.route('/healthcheck', methods=['GET'])
def healthcheck():
    return Response('OK', status=200)

@app.route('/match_plan', methods=['POST'])
def match_plan():
    data = request.get_json()
    plan = generate_plan(data['num_teams'], 4, 8)
    return jsonify(plan)

if __name__ == '__main__':
    app.run(debug=True, port=8000)