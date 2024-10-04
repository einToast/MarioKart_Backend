from flask import Flask, request, jsonify, Response

from generate_gameplay_lists import generate_plan

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
    app.run(debug=False, port=8000, host='0.0.0.0')
