from flask import Flask, request, json
from text_detect import TextDetect

app = Flask(__name__)

@app.route("/")
def main():
    return "Welcome to my Flask page!"

@app.route("/upload", methods = ['POST'])
def upload():
    file = request.files['image']
    td = TextDetect(file, app)
    res = {}
    res['rect'] = td.find().tolist()
    return json.dumps(res)

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port='8080')