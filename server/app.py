from flask import Flask, request, json
from main.text_detect import TextDetect
from main.text_classify import TextClassify
app = Flask(__name__)

@app.route("/")
def main():
    return "Welcome to my Flask page!"

@app.route("/textDetection", methods = ['POST'])
def detect():
    file = request.files['image']
    td = TextDetect(file, app)
    res = {}
    res['rect'] = td.find().tolist()
    return json.dumps(res)

@app.route("/textClassification", methods = ['POST'])
def classify():
    content = request.json
    texts = content['texts']
    tc = TextClassify(texts)
    res = {}
    res['labels'] = tc.classify()
    return json.dumps(res)

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port='8080')