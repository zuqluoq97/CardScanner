package com.vgu.dungluong.cardscannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vgu.dungluong.cardscannerapp.tools.RequestPermissionsToolImpl;
import com.vgu.dungluong.cardscannerapp.tools.RequestPermissonsTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends Activity  {
    
    private static final String TAG = MainActivity.class.getSimpleName();
    static final int PHOTO_REQUEST_CODE = 1;
    private TessBaseAPI tessBaseApi;
    TextView textView;
    Uri outputFileUri;
    private static final String lang = "vie";
    String result = "empty";

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    private static final String TESSDATA = "tessdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button captureImg = (Button) findViewById(R.id.action_btn);
        if (captureImg != null) {
            captureImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startCameraActivity();
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(MainActivity.this);
                }
            });
        }
        textView = (TextView) findViewById(R.id.textResult);

        testCoreNlp();
    }


    /**
     * to get high resolution image from camera
     */
    private void startCameraActivity() {
        try {
            String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
            prepareDirectory(IMGS_PATH);

            outputFileUri = FileProvider.getUriForFile(
                    MainActivity.this,
                    getApplicationContext().getPackageName() + ".my.package.name.provider",
                    new File(IMGS_PATH + "/ocr.jpg"));

            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                doOCR(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void doOCR(Uri imageUri) {
        prepareTesseract();
        startOCR(imageUri);
    }

    /**
     * Prepare directory on external storage
     *
     * @param path
     * @throws Exception
     */
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    /**
     * Copy tessdata files (located on assets/tessdata) to destination directory
     *
     * @param path - name of directory with .traineddata files
     */
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     *
     * @param imgUri
     */
    private void startOCR(Uri imgUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            options.inTargetDensity = 300;
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);
            result = extractText(bitmap);
            textView.setText(result);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "aAáÁàÀạẠãÃảẢăĂắẮằẰặẶẵẴẳẲâÂấẤầẦậẬẫẪẩẨbBcCdDđĐeEéÉèÈẹẸẽẼẻẺêÊếẾềỀệỆễỄểỂfFgGhHiIíÍìÌịỊĩĨỉỈjJkKlLmMnNoOóÓòÒọỌõÕỏỎôÔốỐồỒộỘỗỖổỔơƠớỚờỜợỢỡỠởỞpPqQrRsStTuUúÚùÙụỤũŨủỦưƯứỨừỪựỰữỮửỬvVxXyYýÝỳỲỵỴỹỸỷỶwWzZ123456789',.@-:/ ");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }

    private void testCoreNlp(){
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text from the file..
        String text = "This is the text used for testing Standford Core NLP library";

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println("parse tree:\n" + tree);

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println("dependency graph:\n" + dependencies);
        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
    }
}