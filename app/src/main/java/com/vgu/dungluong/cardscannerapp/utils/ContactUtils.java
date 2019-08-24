package com.vgu.dungluong.cardscannerapp.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.vgu.dungluong.cardscannerapp.data.model.local.ContactDTO;
import com.vgu.dungluong.cardscannerapp.data.model.local.DataDTO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Dung Luong on 14/08/2019
 */
public class ContactUtils {

    private ContactUtils() {
    }

    // Create a new contact and add it to android contact address book.
    public static void addContact(ContentResolver contentResolver)
    {
        // Create a new fake contact first.
        ContactDTO contactDto = generateRandomContactDTO();

        String groupTitle = "Workmate";

        String groupNotes = "Company workmates contacts";

        long groupId = getExistGroup(contentResolver, groupTitle);

        // Group do not exist.
        if(groupId == -1) {
            // Create a new group
            groupId = insertGroup(contentResolver, groupTitle, groupNotes);
        }

        // Create a new contact.
        long rawContactId = insertContact(contentResolver, contactDto);
        AppLogger.i("rawcontactid:" + rawContactId);
        // Set group id.
        contactDto.setGroupId(groupId);
        // Contact id and raw contact id has same value.
        contactDto.setContactId(rawContactId);
        contactDto.setRawContactId(rawContactId);

        // Insert contact group membership data ( group id ).
        insertGroupId(contentResolver, contactDto.getGroupId(), contactDto.getRawContactId());

        // Insert contact address list data.
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDto.getRawContactId(),
                ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.SipAddress.TYPE,
                ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS,
                contactDto.getAddressList());

        // Insert organization data.
        insertOrganization(contentResolver, contactDto);

        // Insert contact display, given and family name.
        insertName(contentResolver, contactDto);

        /* Insert contact email list data, Content uri do not use ContactsContract.CommonDataKinds.Email.CONTENT_URI
         * Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/emails */
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDto.getRawContactId(),
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                contactDto.getEmailList());

        // Insert contact nickname.
        insertNickName(contentResolver, contactDto);

        // Insert contact note.
        insertNote(contentResolver, contactDto);

        /* Insert contact phone list data, Content uri do not use ContactsContract.CommonDataKinds.Phone.CONTENT_URI
         * Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones */
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDto.getRawContactId(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                contactDto.getPhoneList());

        // Insert contact website list.
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDto.getRawContactId(),
                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Website.TYPE,
                ContactsContract.CommonDataKinds.Website.URL,
                contactDto.getWebsiteList());

        // Insert contact im list.
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contactDto.getRawContactId(),
                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Im.PROTOCOL,
                ContactsContract.CommonDataKinds.Im.DATA,
                contactDto.getImList());

        // Insert contact post address
        insertPostalAddress(contentResolver, contactDto);

        // Insert identity
        insertIdentity(contentResolver, contactDto);

        // Insert photo
        insertPhoto(contentResolver, contactDto);
    }

    /* Generate a contact dto object.
        Each dto object field value has a uuid string .
    */
    private static ContactDTO generateRandomContactDTO()
    {
        ContactDTO contactDto = new ContactDTO();

        //**************************************************************
        // Create contact address list.
        List<DataDTO> addressList = new ArrayList<DataDTO>();

        // Create home address.
        DataDTO homeAddressDto = new DataDTO();
        homeAddressDto.setDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_HOME);
        homeAddressDto.setDataValue("3122 Camden Street");
        addressList.add(homeAddressDto);

        // Create work address.
        DataDTO workAddressDto = new DataDTO();
        workAddressDto.setDataType(ContactsContract.CommonDataKinds.SipAddress.TYPE_WORK);
        workAddressDto.setDataValue("3819 Watson Street");
        addressList.add(workAddressDto);

        // Add address list.
        contactDto.setAddressList(addressList);

        //***************************************************************
        // Below is contact organization related info.

        // Set company
        contactDto.setCompany("IBM");
        // Set department
        contactDto.setDepartment("Development Team");
        // Set title
        contactDto.setTitle("Senior Software Engineer");
        // Set job description
        contactDto.setJobDescription("Develop features use java.");
        // Set office location.
        contactDto.setOfficeLocation("Mountain View");

        //***************************************************************
        // Create email address list.
        List<DataDTO> emailList = new ArrayList<DataDTO>();

        // Create work email.
        DataDTO workEmailDto = new DataDTO();
        workEmailDto.setDataType(ContactsContract.CommonDataKinds.Email.TYPE_HOME);
        workEmailDto.setDataValue("jack@dev2qa.com");
        emailList.add(workEmailDto);

        // Create home email.
        DataDTO homeEmailDto = new DataDTO();
        homeEmailDto.setDataType(ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM);
        homeEmailDto.setDataValue("jack@gmail.com");
        emailList.add(homeEmailDto);

        // Add email list.
        contactDto.setEmailList(emailList);

        //***************************************************************
        // Below is structured name related info.

        contactDto.setDisplayName("Jack");

//        contactDto.setGivenName("Bill"+uuidStr);
//
//        contactDto.setFamilyName("Trump"+uuidStr);

        //**************************************************************
        // Contact nick name related info.

        contactDto.setNickName("FlashMan");

        //**************************************************************
        // Contact note related info.
        contactDto.setNote("dev2qa.com senior engineer");

        //**************************************************************
        // Im related info
        List<DataDTO> imList = new ArrayList<DataDTO>();

        DataDTO qqDto = new DataDTO();
        qqDto.setDataType(ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ);
        qqDto.setDataValue("QQ_888" );
        imList.add(qqDto);

        DataDTO icqDto = new DataDTO();
        icqDto.setDataType(ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ);
        icqDto.setDataValue("ICQ_666" );
        imList.add(icqDto);

        DataDTO skypeDto = new DataDTO();
        skypeDto.setDataType(ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE);
        skypeDto.setDataValue("SKYPE_968" );
        imList.add(skypeDto);

        contactDto.setImList(imList);

        //***************************************************************
        // Create phone list.
        List<DataDTO> phoneList = new ArrayList<DataDTO>();

        // Create mobile phone.
        DataDTO mobilePhone = new DataDTO();
        mobilePhone.setDataType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        mobilePhone.setDataValue("139011111181");
        phoneList.add(mobilePhone);

        // Create work mobile phone.
        DataDTO workMobilePhone = new DataDTO();
        workMobilePhone.setDataType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);
        workMobilePhone.setDataValue("13801234567");
        phoneList.add(workMobilePhone);

        // Create home phone.
        DataDTO homePhone = new DataDTO();
        homePhone.setDataType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
        homePhone.setDataValue("010-123456789");
        phoneList.add(homePhone);

        contactDto.setPhoneList(phoneList);

        //***************************************************************
        // Create website list
        List<DataDTO> websiteList = new ArrayList<DataDTO>();

        // Create work website dto.
        DataDTO workWebsiteDto = new DataDTO();
        workWebsiteDto.setDataType(ContactsContract.CommonDataKinds.Website.TYPE_WORK);
        workWebsiteDto.setDataValue(".dev2qa.com");
        websiteList.add(workWebsiteDto);

        // Create blog website dto.
        DataDTO blogWebsiteDto = new DataDTO();
        blogWebsiteDto.setDataType(ContactsContract.CommonDataKinds.Website.TYPE_BLOG);
        blogWebsiteDto.setDataValue(".blog.dev2qa.com");
        websiteList.add(blogWebsiteDto);

        contactDto.setWebsiteList(websiteList);

        //**************************************************************
        // Set postal related info.
        //contactDto.setCountry("USA"+uuidStr);
        //contactDto.setCity("Chicago"+uuidStr);
        //contactDto.setRegion("Washington DC"+uuidStr);
        contactDto.setStreet("No.9 Street 12, Song Than Industrial Zone II, Di An District, Binh Duong Province, Vietnam ");
        //contactDto.setPostCode("60606"+uuidStr);
        contactDto.setPostType(ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM);

        //**************************************************************

        // Set identity info.
        //contactDto.setIdentity("347-80-XXXX"+uuidStr);
        //contactDto.setNamespace("SSN");

        //**************************************************************
        // Set photo info.
        contactDto.setPhoto(".png");
        contactDto.setPhotoFieldId("id");

        return contactDto;
    }

    /*
     *  Check whether the group exist of not.
     *  Return exist group id or -1 if group is not exist.
     * */
    private static long getExistGroup(ContentResolver contentResolver, String groupTitle)
    {
        long ret = -1;

        String queryColumnArr[] = {ContactsContract.Groups._ID};

        StringBuffer whereClauseBuf = new StringBuffer();
        whereClauseBuf.append(ContactsContract.Groups.TITLE);
        whereClauseBuf.append("='");
        whereClauseBuf.append(groupTitle);
        whereClauseBuf.append("'");

        Cursor cursor = contentResolver.query(ContactsContract.Groups.CONTENT_URI, queryColumnArr, whereClauseBuf.toString(), null, null);
        if(cursor!=null)
        {
            if(cursor.getCount()>0)
            {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(ContactsContract.Groups._ID);
                ret = cursor.getLong(columnIndex);
            }
        }
        return ret;
    }

    /*
     *  Insert a new contact group.
     *  Return newly created group id.
     * */
    private static long insertGroup(ContentResolver contentResolver, String groupTitle, String groupNotes)
    {
        // Insert a group in group table.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Groups.TITLE, groupTitle);
        contentValues.put(ContactsContract.Groups.NOTES, groupNotes);
        Uri groupUri = contentResolver.insert(ContactsContract.Groups.CONTENT_URI, contentValues);
        // Get the newly created raw contact id.
        long groupId = ContentUris.parseId(groupUri);

        return groupId;
    }

    /*
     *  Insert a new empty contact.
     *  Return newly created contact id.
     * */
    private static long insertContact(ContentResolver contentResolver, ContactDTO contactDto)
    {
        // Insert an empty contact in both contacts and raw_contacts table.
        // Return the system generated new contact and raw_contact id.
        // The id in contacts and raw_contacts table has same value.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, contactDto.getDisplayName());
        contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE, contactDto.getDisplayName());
        Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created raw contact id.
        long rawContactId = ContentUris.parseId(rawContactUri);
        return rawContactId;
    }

    /* Insert contact belongs group id in data table. */
    private static void insertGroupId(ContentResolver contentResolver, long groupRowId, long rawContactId)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);
        // Set contact belongs group id.
        contentValues.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupRowId);

        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    /*  Insert list DataDTO into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  Uri contentUri : Insert data uri. ( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, Email.CONTENT_URI etc.)
     *  long contactId : contacts table id.
     *  long rawContactId : raw_contacts table id. Same value with contacts table id.
     *  String mimeType : The inserted data mime type. ( ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE etc. )
     *  String dataTypeColumnName : Data type column name.( ContactsContract.CommonDataKinds.Email.TYPE, Phone.TYPE etc.)
     *  String dataValueColumnName : Data value column name. ( ContactsContract.CommonDataKinds.Phone.NUMBER, Email.ADDRESS, Nickname.NAME etc)
     *   List<DataDTO> dataList : Data list, such as phone list, address list and email list etc.
     * */
    private static void insertListData(ContentResolver contentResolver,
                                       Uri contentUri,
                                       long rawContactId,
                                       String mimeType,
                                       String dataTypeColumnName,
                                       String dataValueColumnName,
                                       List<DataDTO> dataList)
    {
        if(dataList!=null) {

            ContentValues contentValues = new ContentValues();

            int size = dataList.size();

            for(int i=0;i<size;i++) {

                DataDTO dataDto = dataList.get(i);

                contentValues.clear();

                // Set raw contact id. Data table only has raw_contact_id.
                contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                // Set data mimetype.
                contentValues.put(ContactsContract.Data.MIMETYPE, mimeType);
                // Set data type.
//                if(dataDto.getDataType() == 0)
//                    contentValues.put(dataTypeColumnName, "abc");
//                else


//                if(dataDto.getDataType() == 0){
//                    contentValues.put("data3", "abc");
//                }else{
                    contentValues.put(dataTypeColumnName, dataDto.getDataType());
               // }
                if(dataDto.getDataType() == 0) {
                    AppLogger.i("label");
                    contentValues.put(ContactsContract.CommonDataKinds.Email.LABEL, "Green Bot");
                }
                // Set data value.
                contentValues.put(dataValueColumnName, dataDto.getDataValue());

                contentResolver.insert(contentUri, contentValues);
            }
        }

    }

    /*  Insert organization info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertOrganization(ContentResolver contentResolver, ContactDTO contactDto)
    {
        if(contactDto!=null) {

            ContentValues contentValues = new ContentValues();

            // Set raw contact id. Data table only has raw_contact_id.
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
            // Set data mimetype.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            // Set company name.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, contactDto.getCompany());
            // Set department.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, contactDto.getDepartment());
            // Set title.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, contactDto.getTitle());
            // Set job description.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION, contactDto.getJobDescription());
            // Set office location.
            contentValues.put(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, contactDto.getOfficeLocation());

            // Insert to data table.
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
    }

    /*  Insert name info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertName(ContentResolver contentResolver, ContactDTO contactDto)
    {
        if(contactDto!=null) {

            ContentValues contentValues = new ContentValues();

            // Set raw contact id. Data table only has raw_contact_id.
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
            // Set data mimetype.
            contentValues.put(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            // Set display name.
            contentValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactDto.getDisplayName());
            // Set given name.
            contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contactDto.getGivenName());
            // Set family name.
            contentValues.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contactDto.getFamilyName());
            // Insert to data table.
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
    }

    /*  Insert nick name info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertNickName(ContentResolver contentResolver, ContactDTO contactDto)
    {
        if(contactDto!=null) {

            ContentValues contentValues = new ContentValues();

            // Set raw contact id. Data table only has raw_contact_id.
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
            // Set data mimetype.
            contentValues.put(ContactsContract.CommonDataKinds.Nickname.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE);
            // Set display name.
            contentValues.put(ContactsContract.CommonDataKinds.Nickname.NAME, contactDto.getNickName());
            // Insert to data table.
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
    }

    /*  Insert note info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertNote(ContentResolver contentResolver, ContactDTO contactDto)
    {
        if(contactDto!=null) {

            ContentValues contentValues = new ContentValues();

            // Set raw contact id. Data table only has raw_contact_id.
            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
            // Set data mimetype.
            contentValues.put(ContactsContract.CommonDataKinds.Note.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            // Set display name.
            contentValues.put(ContactsContract.CommonDataKinds.Note.NOTE, contactDto.getNote());
            // Insert to data table.
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }
    }

    /* Insert contact postal address info in data table. */
    private static void insertPostalAddress(ContentResolver contentResolver, ContactDTO contactDto)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
        // Set country
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, contactDto.getCountry());
        // Set city
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, contactDto.getCity());
        // Set region
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION, contactDto.getRegion());
        // Set street
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, contactDto.getStreet());
        // Set postcode
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, contactDto.getPostCode());
        // Set postcode
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, contactDto.getPostType());

    /* Insert to data table. Do not use uri ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
       Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/postals*/
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    /* Insert contact identity info in data table. */
    private static void insertIdentity(ContentResolver contentResolver, ContactDTO contactDto)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.Identity.MIMETYPE, ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE);
        // Set identity
        contentValues.put(ContactsContract.CommonDataKinds.Identity.IDENTITY, contactDto.getIdentity());
        // Set namespace
        contentValues.put(ContactsContract.CommonDataKinds.Identity.NAMESPACE, contactDto.getNamespace());

        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    /* Insert contact photo info in data table. */
    private static void insertPhoto(ContentResolver contentResolver, ContactDTO contactDto)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contactDto.getRawContactId());
        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.Photo.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        // Set photo
        contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO, contactDto.getPhoto());
        // Set photo file id.
        //contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID, contactDto.getPhotoFieldId());

        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }
}
