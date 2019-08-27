package com.vgu.dungluong.cardscannerapp.ui.result;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.view.View;

import com.androidnetworking.utils.ParseUtil;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Rotate;
import com.vgu.dungluong.cardscannerapp.BR;
import com.vgu.dungluong.cardscannerapp.data.DataManager;
import com.vgu.dungluong.cardscannerapp.data.model.local.Contact;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
import com.vgu.dungluong.cardscannerapp.data.model.local.Corners;
import com.vgu.dungluong.cardscannerapp.data.model.local.OnTouchZone;
import com.vgu.dungluong.cardscannerapp.ui.base.BaseViewModel;
import com.vgu.dungluong.cardscannerapp.utils.AppConstants;
import com.vgu.dungluong.cardscannerapp.utils.AppLogger;
import com.vgu.dungluong.cardscannerapp.utils.CardExtract;
import com.vgu.dungluong.cardscannerapp.utils.CardProcessor;
import com.vgu.dungluong.cardscannerapp.utils.CommonUtils;
import com.vgu.dungluong.cardscannerapp.utils.ContactUtils;
import com.vgu.dungluong.cardscannerapp.utils.ParserUtils;
import com.vgu.dungluong.cardscannerapp.utils.SourceManager;
import com.vgu.dungluong.cardscannerapp.utils.rx.SchedulerProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.databinding.Bindable;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import static com.vgu.dungluong.cardscannerapp.utils.AppConstants.GROUP_PHONE_TITLE;

/**
 * Created by Dung Luong on 02/07/2019
 */
public class ResultViewModel extends BaseViewModel<ResultNavigator> {

    private Mat mCardPicture;

    private ObservableBoolean mIsOCRSucceed;

    private ObservableBoolean mIsTextDetectSucceed;

    private ObservableField<String> mResultString;

    private Bitmap mBitmap;

    private List<Bitmap> bms;

    private int idx;

    private int idx2;

    private List<String> mOCRs;

    private List<Corners> mRects;

    private int mTimes;

    private MutableLiveData<List<OnTouchZone>> mOnTouchZonesMutableLiveData;

    private Contact mContact;

    private ObservableArrayList<String> mTypeObservableArrayList;

    private MutableLiveData<List<ContactField>> mPhoneContactFieldMutableLiveData;

    private MutableLiveData<List<ContactField>> mEmailContactFieldMutableLiveData;

    private MutableLiveData<List<String>> mWebContactFieldMutableLiveData;

    private ObservableField<String> mDepartmentObservableField;

    private ObservableField<String> mFullAddressObservableField;

    private ObservableField<String> mAddressDataTypeObservableField;

    private ObservableField<String> mNameObservableField;

    private ObservableField<String> mCompanyObservableField;

    private ObservableField<String> mTitleObservableField;

    private ContactField mAddressContactField;

    public ResultViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        mContact = Contact.create(0,
                0,
                0,
                "",
                "",
                "",
                "",
                new ArrayList<>(),
                new ArrayList<>(),
                ContactField.create(-1, "", ""),
                new ArrayList<>(),
                new byte[]{});
        mTypeObservableArrayList = new ObservableArrayList<>();
        mTypeObservableArrayList.addAll(AppConstants.DATA_TYPE1_TYPE_TITLE);
        notifyPropertyChanged(BR.typeObservableArrayList);

        mPhoneContactFieldMutableLiveData = new MutableLiveData<>();
        mEmailContactFieldMutableLiveData = new MutableLiveData<>();
        mWebContactFieldMutableLiveData = new MutableLiveData<>();

        mDepartmentObservableField = new ObservableField<>("");
        mFullAddressObservableField = new ObservableField<>("");
        mNameObservableField = new ObservableField<>("");
        mCompanyObservableField = new ObservableField<>("");
        mTitleObservableField = new ObservableField<>("");
        mAddressDataTypeObservableField = new ObservableField<>(mTypeObservableArrayList.get(1));
        mAddressContactField = ContactField.create(-1, "", "");

        mIsOCRSucceed = new ObservableBoolean(false);
        mIsTextDetectSucceed = new ObservableBoolean(false);
        mResultString = new ObservableField<>("");
        bms = new ArrayList<>();
        mOCRs = new ArrayList<>();
        mCardPicture = SourceManager.getInstance().getPic();
        AppLogger.i(mCardPicture.height() + " " + mCardPicture.width());
        if(Objects.requireNonNull(mCardPicture).height() > mCardPicture.width())
            Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_90_CLOCKWISE);
        mRects = new ArrayList<>();
        mOnTouchZonesMutableLiveData = new MutableLiveData<>();
    }

    public void displayCardImage(){
        Mat img = CardProcessor.updateCropRectsOnImage(mCardPicture, getRects());
        int cardHeight = (int) (getScaleRatioWidth() * img.height());
        mBitmap = Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, mBitmap, true);
        getNavigator().getCardImageView().setImageBitmap(Bitmap.createScaledBitmap(mBitmap,
                getNavigator().getCardImageView().getWidth(), cardHeight, false));
    }

    public void next(){
        getNavigator().getCardImageView().setImageBitmap(bms.get(idx));
        idx ++;
    }

    public void rotate(View view){
        Core.rotate(mCardPicture, mCardPicture, Core.ROTATE_180);
        getNavigator().animateButton(view, mTimes);
        mTimes ++;
        displayCardImage();
    }

    public void onAddMorePhoneButtonClick(){
        getNavigator().addOnePhoneContactField();
    }

    public void onAddMoreEmailButtonClick(){
        getNavigator().addOneEmailContactField();
    }

    public void onAddMoreWebButtonClick(){
        getNavigator().addOneWebContactField();
    }

    public void onSaveContactButtonClick(){
        setIsLoading(true);
        mContact = mContact.withGroupId(ContactUtils.getGroupId(getNavigator().getContentResolver()));
        mContact = mContact.withContactId(ContactUtils.insertContact(getNavigator().getContentResolver(), getNameObservableField()));
        mContact = mContact.withrawContactId(mContact.contactId());
        mContact = mContact.withDisplayName(getNameObservableField().trim());
        mContact = mContact.withCompany(getCompanyObservableField().trim());
        mContact = mContact.withDepartment(getDepartmentObservableField().trim());
        mContact = mContact.withTitle(getTitleObservableField().trim());
        mContact = mContact.withPhoneList(getNavigator().getPhoneContactFields());
        mContact = mContact.withEmailList(getNavigator().getEmailContactFields());
        mContact = mContact.withWebsiteList(getNavigator().getWebs());
        mContact = mContact.withAddress(mAddressContactField);

        int maxPhotoWidth = CommonUtils.getMaxContactPhotoSize(getNavigator().getActivityContext());
        Imgproc.resize(mCardPicture, mCardPicture, new Size(maxPhotoWidth, (double) (mCardPicture.height() * maxPhotoWidth/ mCardPicture.width())));
        Bitmap bitmap = Bitmap.createBitmap(mCardPicture.width(), mCardPicture.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCardPicture, bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        mContact = mContact.withContactPhoto(byteArray);
        ContactUtils.addContact(getNavigator().getContentResolver(), mContact);
        setIsLoading(false);
        getNavigator().openMainActivity();
    }

    public void textDetect(){
        setIsLoading(true);
        File imgFile = getNavigator().getFileForCropImage();
        saveBitmapToJpg(mBitmap, imgFile, 300);
        Bitmap bm = get300DPIBitmap(imgFile);
        Utils.bitmapToMat(bm, mCardPicture);
        getCompositeDisposable().add(getDataManager()
                .doServerTextDetection(imgFile)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(rects -> {
                            setIsLoading(false);
                            updateCropAreas(rects.getCorners());
                            displayCardImage();
                            setIsTextDetectSucceed(true);
                        },
                        throwable -> {
                            setIsLoading(false);
                            AppLogger.e(throwable.getLocalizedMessage());
                        }));
    }

    public void updateCropAreas(List<Corners> corners){
        mRects = corners;
        List<OnTouchZone> onTouchZones = new ArrayList<>();
        corners.forEach(rect -> {
            onTouchZones.add(CommonUtils.getOnTouchZone(rect));
        });
        mOnTouchZonesMutableLiveData.setValue(onTouchZones);
        displayCardImage();
    }

    public void cropTextArea(){
        setIsLoading(true);
        getCompositeDisposable().add(CardProcessor
                .cropTextArea(mCardPicture, getRects())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(bitmaps -> {
                    bms = bitmaps;
                    tesseract(bitmaps);
                }, throwable -> {
                    setIsLoading(false);
                    AppLogger.e(throwable.getLocalizedMessage());
                }));
    }

    public void tesseract(List<Bitmap> bitmap){
        for(int i = 0; i < bitmap.size(); i++){
            Bitmap bm = bitmap.get(i);
            getCompositeDisposable().add(getDataManager()
                    .doTesseract(bm, getNavigator().getTesseractApi())
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(ocr -> {
                            mOCRs.add(ocr.first);
                            idx2++;
                            if(idx2 == bitmap.size()){
                                displayOCR();
                            }
                    }, throwable -> {
                        getNavigator().handleError(throwable.getLocalizedMessage());
                        setIsLoading(false);
                    }));
        }
    }

    private void displayOCR(){
        final String[] result = {""};
        mOCRs.forEach(ocr -> {
            result[0] += ocr + "\n";
        });
        mResultString.set(result[0]);
        ParserUtils parserUtils = new ParserUtils(mOCRs);
        parserUtils.run();
        doTextClassification(parserUtils.getTexts());

        mPhoneContactFieldMutableLiveData.postValue(parserUtils.getPhones()
                .stream()
                .map(phone ->{
                    if(phone.first.trim().isEmpty()){
                        return ContactField.create(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, "", phone.second);
                    }else{
                        return ContactField.create(ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, phone.first, phone.second);
                    }
                }).collect(Collectors.toList()));

        mEmailContactFieldMutableLiveData.postValue(parserUtils.getEmails()
                .stream()
                .map(email -> ContactField.create(ContactsContract.CommonDataKinds.Email.TYPE_WORK, "", email))
                .collect(Collectors.toList()));

        if(!parserUtils.getDepartment().isEmpty()) setDepartmentObservableField(parserUtils.getDepartment());

        mWebContactFieldMutableLiveData.postValue(parserUtils.getWebs());

        String fullAddress = "";
        List<String> addresses = parserUtils.getAddresses();
        for(int i =0; i < addresses.size(); i++){
            fullAddress += addresses.get(i);
            if(i != addresses.size() - 1) fullAddress += ", ";
        }

        setFullAddressObservableField(fullAddress);

        AppLogger.i(parserUtils.getEmails().toString());
        AppLogger.i(parserUtils.getWebs().toString());
        AppLogger.i(parserUtils.getAddresses().toString());
        AppLogger.i(parserUtils.getPhones().toString());
        AppLogger.i(parserUtils.getTexts().toString());

    }

    public void doTextClassification(List<String> unLabeledTexts){
        try {
            setIsLoading(true);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("texts", new JSONArray(unLabeledTexts));
            AppLogger.i(jsonObject.toString());
            getCompositeDisposable().add(getDataManager()
                    .doServerTextClassification(jsonObject)
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(labeledList -> {
                        setNameObservableField(labeledList.getName(unLabeledTexts));
                        setCompanyObservableField(labeledList.getCompany(unLabeledTexts));
                        setTitleObservableField(labeledList.getJob(unLabeledTexts));
                        setIsLoading(false);
                        setIsOCRSucceed(true);
                        AppLogger.i(labeledList.toString());
                    }, throwable -> getNavigator().handleError(throwable.getLocalizedMessage())));
        }catch (JSONException e){
            setIsLoading(false);
            AppLogger.e(e.getLocalizedMessage());
        }
    }


    private Bitmap get300DPIBitmap(File file){
        Bitmap bm = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            options.inTargetDensity = 300;
            bm = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception e) {
            AppLogger.e(e.getLocalizedMessage());
        }
        return bm;
    }

    public void saveBitmapToJpg(Bitmap bitmap, File file, int dpi){
        try {
            ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageByteArray);
            byte[] imageData = imageByteArray.toByteArray();

            setDpi(imageData, dpi);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(imageData);
            fileOutputStream.close();
        }catch (IOException e){
            AppLogger.e(e.getLocalizedMessage());
        }
    }

    private void setDpi(byte[] imageData, int dpi) {
        imageData[13] = 1;
        imageData[14] = (byte) (dpi >> 8);
        imageData[15] = (byte) (dpi & 0xff);
        imageData[16] = (byte) (dpi >> 8);
        imageData[17] = (byte) (dpi & 0xff);
    }

    public double getScaleRatioWidth(){
        return (double) getNavigator().getCardImageView().getWidth() / mCardPicture.width();
    }

    public double getScaleRatioHeight(){
        return (double) getNavigator().getCardImageView().getHeight() / mCardPicture.height();
    }

    public ObservableBoolean getIsOCRSucceed() {
        return mIsOCRSucceed;
    }

    public ObservableBoolean getIsTextDetectSucceed() {
        return mIsTextDetectSucceed;
    }

    public void setIsOCRSucceed(boolean isOCRSucceed) {
        mIsOCRSucceed.set(isOCRSucceed);
    }

    public void setIsTextDetectSucceed(boolean isTextDetectSucceed) {
        mIsTextDetectSucceed.set(isTextDetectSucceed);
    }

    public ObservableField<String> getResultString() {
        return mResultString;
    }

    public List<Corners> getRects() {
        return mRects;
    }

    public MutableLiveData<List<OnTouchZone>> getOnTouchZones() {
        return mOnTouchZonesMutableLiveData;
    }

    @Bindable
    public List<String> getTypeObservableArrayList(){
        return mTypeObservableArrayList;
    }

    @Bindable
    public String getDepartmentObservableField(){
        return mDepartmentObservableField.get();
    }

    public void setDepartmentObservableField(String department){
        if(!Objects.equals(getDepartmentObservableField(), department)){
            mDepartmentObservableField.set(department);
            notifyPropertyChanged(BR.departmentObservableField);
        }
    }

    @Bindable
    public String getFullAddressObservableField(){
        return mFullAddressObservableField.get();
    }

    public void setFullAddressObservableField(String fullAddress){
        if(!Objects.equals(getFullAddressObservableField(), fullAddress)){
            mFullAddressObservableField.set(fullAddress);
            notifyPropertyChanged(BR.fullAddressObservableField);
            mAddressContactField = mAddressContactField.withDataValue(fullAddress);
        }
    }

    @Bindable
    public String getAddressDataTypeObservableField(){
        return mAddressDataTypeObservableField.get();
    }

    public void setAddressDataTypeObservableField(String addressType){
        if(!Objects.equals(getAddressDataTypeObservableField(), addressType)){
            mAddressDataTypeObservableField.set(addressType);
            notifyPropertyChanged(BR.addressDataTypeObservableField);
            switch (mTypeObservableArrayList.stream()
                    .map(String::toLowerCase).collect(Collectors.toList())
                    .indexOf(addressType.toLowerCase().trim())){
                case 0:
                    mAddressContactField = mAddressContactField.withDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME);
                    break;
                case 1:
                    mAddressContactField = mAddressContactField.withDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK);
                    break;
                case 2:
                    mAddressContactField = mAddressContactField.withDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_OTHER);
                    break;
                default:
                    if(addressType.isEmpty()){
                        mAddressContactField = mAddressContactField.withDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK);
                    }else {
                        mAddressContactField = mAddressContactField.withDataLabel(addressType);
                        mAddressContactField = mAddressContactField.withDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_CUSTOM);
                    }
            }
        }
    }

    @Bindable
    public String getNameObservableField(){
        return mNameObservableField.get();
    }

    public void setNameObservableField(String name){
        if(!Objects.equals(getNameObservableField(), name)){
            mNameObservableField.set(name);
            notifyPropertyChanged(BR.nameObservableField);
        }
    }

    @Bindable
    public String getCompanyObservableField(){
        return mCompanyObservableField.get();
    }

    public void setCompanyObservableField(String company){
        if(!Objects.equals(getCompanyObservableField(), company)){
            mCompanyObservableField.set(company);
            notifyPropertyChanged(BR.companyObservableField);
        }
    }

    @Bindable
    public String getTitleObservableField(){
        return mTitleObservableField.get();
    }

    public void setTitleObservableField(String title){
        if(!Objects.equals(getTitleObservableField(), title)){
            mTitleObservableField.set(title);
            notifyPropertyChanged(BR.titleObservableField);
        }
    }

    public MutableLiveData<List<ContactField>> getPhoneContactFieldMutableLiveData() {
        return mPhoneContactFieldMutableLiveData;
    }

    public MutableLiveData<List<ContactField>> getEmailContactFieldMutableLiveData() {
        return mEmailContactFieldMutableLiveData;
    }

    public MutableLiveData<List<String>> getWebContactFieldMutableLiveData() {
        return mWebContactFieldMutableLiveData;
    }
}
