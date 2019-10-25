from keras.models import model_from_json
from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras import backend as K
import numpy as np
import os
import pickle

class TextClassify:
    def __init__(self, texts):
        self.texts = texts

    def classify(self):
        # clear session
        K.clear_session()
        # load json and create model
        json_file = open('data/model/model.json', 'r')
        loaded_model_json = json_file.read()
        json_file.close()
        loaded_model = model_from_json(loaded_model_json)
        # load weights into new model
        loaded_model.load_weights("data/model/model.h5")
        print("Loaded model from disk")
        # loading
        with open('data/model/tokenizer.pickle', 'rb') as handle:
            tokenizer = pickle.load(handle)

        labels = []    
        for text in self.texts:
            tokens = tokenizer.texts_to_sequences([text])
            tokens = pad_sequences(tokens, maxlen=5)
            prediction = loaded_model.predict(np.array(tokens))
            i,j = np.where(prediction == prediction.max()) #calculates the index of the maximum element of the array across all axis
            # i->rows, j->columns
            i = int(i)
            j = int(j)
            total_possible_outcomes = ['name','company','job']
            print("Result:",total_possible_outcomes[j])  
            labels.append(total_possible_outcomes[j])
        return labels    
