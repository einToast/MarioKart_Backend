from flask import Flask, request, jsonify, Response

from generate_gameplay_lists import generate_plan

app = Flask(__name__)

@app.route('/healthcheck', methods=['GET'])
def healthcheck():
    return Response('OK', status=200)

@app.route('/match_plan', methods=['POST'])
def match_plan():
    print('Request received')
    data = request.get_json()
    plan, max_games_count = generate_plan(data['num_teams'], 4, 8)

    response_dict = {
        'plan': plan,
        'max_games_count': next(iter(max_games_count))
    }
    return jsonify(response_dict)
    # return jsonify(plan)
if __name__ == '__main__':
    app.run(debug=False, port=8000, host='0.0.0.0')
