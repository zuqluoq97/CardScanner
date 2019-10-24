from keras.preprocessing.text import Tokenizer
from keras.preprocessing.sequence import pad_sequences
from keras import backend as K
import numpy as np
import os


class TextClassify:
    def __init__(self, texts):
        self.texts = texts

    def classify(self, model, tokenizer):
        labels = []    
        for text in self.texts:
            tokens = tokenizer.texts_to_sequences([text])
            tokens = pad_sequences(tokens, maxlen=5)
            prediction = model.predict(np.array(tokens))
            i,j = np.where(prediction == prediction.max()) #calculates the index of the maximum element of the array across all axis
            # i->rows, j->columns
            i = int(i)
            j = int(j)
            total_possible_outcomes = ['name','company','job']
            print("Result:",total_possible_outcomes[j])  
            labels.append(total_possible_outcomes[j])
    
        return labels    