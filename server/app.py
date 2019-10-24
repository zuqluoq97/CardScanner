from flask import Flask, request, json
from keras.models import model_from_json
from main.text_detect import TextDetect
from main.text_classify import TextClassify
import tensorflow as tf
import pickle

app = Flask(__name__)
model = None
tokenizer = None
graph = tf.get_default_graph()

def load_model():
    global model
    global tokenizer
     # load json and create model
    json_file = open('data/model/model.json', 'r')
    loaded_model_json = json_file.read()
    json_file.close()
    model = model_from_json(loaded_model_json)
    # load weights into new model
    model.load_weights("data/model/model.h5")
    print("Loaded model from disk")
    # loading token
    with open('data/model/tokenizer.pickle', 'rb') as handle:
        tokenizer = pickle.load(handle)

def get_global_step(self):
    with tf.variable_scope(tf.get_variable_scope(), reuse=tf.compat.v1.AUTO_REUSE) as scope:
        global_step = tf.get_variable('global_step', [], initializer=tf.constant_initializer(0), trainable=False)
    return global_step

@app.route("/")
def main():
    return "Welcome to my Flask page!"

@app.route("/textDetection", methods = ['POST'])
def detect():
    global graph
    with graph.as_default():
        print("detect")
        file = request.files['image']
        td = TextDetect(file, app)
        res = {}
        res['rect'] = td.find().tolist()
        return json.dumps(res)


@app.route("/textClassification", methods = ['POST'])
def classify():
    global graph
    with graph.as_default():
        content = request.json
        texts = content['texts']
        tc = TextClassify(texts)
        res = {}
        res['labels'] = tc.classify(model, tokenizer)
        return json.dumps(res)


if __name__ == "__main__":
    print(("* Loading model and Flask starting server..."
		"please wait until server has fully started"))
    load_model()
    app.run(debug=True, host='0.0.0.0', port='8080')