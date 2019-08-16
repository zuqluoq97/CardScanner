# coding=utf-8
import os
import shutil
import sys
import time
import numpy as np
import cv2
import tensorflow as tf
import math
# Disable deprecation warning of tensorflow
# import tensorflow.python.util.deprecation as deprecation
# deprecation._PRINT_DEPRECATION_WARNINGS = False

sys.path.append(os.getcwd())
from nets import model_train as model
from utils.rpn_msr.proposal_layer import proposal_layer
from utils.text_connector.detectors import TextDetector
from werkzeug import secure_filename

tf.app.flags.DEFINE_string('test_data_path', 'data/demo/', '')
tf.app.flags.DEFINE_string('output_path', 'data/res/', '')
tf.app.flags.DEFINE_string('gpu', '0', '')
tf.app.flags.DEFINE_string('checkpoint_path', 'main/checkpoints_mlt/', '')
tf.app.flags.DEFINE_string('bind', '', 'Server address')
tf.app.flags.DEFINE_integer('timeout', 30, 'Server timeout')
FLAGS = tf.app.flags.FLAGS

class TextDetect:

    def __init__(self, img_file, app):
        self.img_path = os.path.join(app.root_path, "crop.jpg")
        img_file.save(self.img_path)

    def get_global_step(self):
        with tf.variable_scope(tf.get_variable_scope(), reuse=tf.compat.v1.AUTO_REUSE) as scope:
            global_step = tf.get_variable('global_step', [], initializer=tf.constant_initializer(0), trainable=False)
        return global_step

    def resize_image(self, img) :
        img_size = img.shape
        im_size_min = np.min(img_size[0:2])
        im_size_max = np.max(img_size[0:2])
        im_scale = float(600) / float(im_size_min)

        if np.round(im_scale * im_size_max) > 1200:
            im_scale = float(1200) / float(im_size_max)

        new_h = int(img_size[0] * im_scale)
        new_w = int(img_size[1] * im_scale)
        new_h = new_h if new_h // 16 == 0 else (new_h // 16 + 1) * 16
        new_w = new_w if new_w // 16 == 0 else (new_w // 16 + 1) * 16
        re_im = cv2.resize(img, (new_w, new_h), interpolation=cv2.INTER_LINEAR)
       
        return re_im, (new_h / img_size[0], new_w / img_size[1])

    def find(self):
        os.environ['CUDA_VISIBLE_DEVICES'] = FLAGS.gpu
        tf.reset_default_graph()
        with tf.get_default_graph().as_default():
            input_image = tf.placeholder(tf.float32, shape=[None, None, None, 3], name='input_image')
            input_im_info = tf.placeholder(tf.float32, shape=[None, 3], name='input_im_info')

            global_step = self.get_global_step()
            bbox_pred, cls_pred, cls_prob = model.model(input_image)
            variable_averages = tf.train.ExponentialMovingAverage(0.997, global_step)
            saver = tf.train.Saver(variable_averages.variables_to_restore())
            
            with tf.Session(config=tf.ConfigProto(allow_soft_placement=True)) as sess:
                ckpt_state = tf.train.get_checkpoint_state(FLAGS.checkpoint_path)
                model_path = os.path.join(FLAGS.checkpoint_path, os.path.basename(ckpt_state.model_checkpoint_path))
                print('Restore from {}'.format(model_path))
                saver.restore(sess, model_path)

                print('===============')
        
                try:
                    im = cv2.imread(self.img_path)[:, :, ::-1]
                    
                except:
                    print("Error reading image {}!".format(self.img_path))

                img, (rh, rw) = self.resize_image(im)
                h, w, c = img.shape
                im_info = np.array([h, w, c]).reshape([1, 3])
                bbox_pred_val, cls_prob_val = sess.run([bbox_pred, cls_prob],
                                                       feed_dict={input_image: [img],
                                                                  input_im_info: im_info})

                textsegs, _ = proposal_layer(cls_prob_val, bbox_pred_val, im_info)
                scores = textsegs[:, 0]
                textsegs = textsegs[:, 1:5]
                
                textdetector = TextDetector(DETECT_MODE='O')
                boxes = textdetector.detect(textsegs, scores[:, np.newaxis], img.shape[:2])
               
                for box in boxes:
                    box_idx = 0
                    while box_idx < 8:
                        if box_idx % 2 == 0:
                            witdth_scale = box[box_idx] / rw
                            box[box_idx] = self.round_half_up(witdth_scale)
                        else:
                            height_scale = box[box_idx] / rh
                            box[box_idx] = self.round_half_up(height_scale)
                        box_idx +=1

                boxes = np.array([box[:-1] for box in boxes], dtype=np.int)    
                return boxes
    
    def round_half_up(self, n):
        return math.floor(n + 0.5)
