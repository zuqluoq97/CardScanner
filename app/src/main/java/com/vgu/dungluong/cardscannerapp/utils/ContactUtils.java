package com.vgu.dungluong.cardscannerapp.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.vgu.dungluong.cardscannerapp.data.model.local.Contact;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactDTO;
import com.vgu.dungluong.cardscannerapp.data.model.local.ContactField;
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

    public static long getGroupId(ContentResolver contentResolver){
        long groupId = getExistGroup(contentResolver);

        // Group do not exist.
        if(groupId == -1) {
            // Create a new group
            groupId = insertGroup(contentResolver);
        }
        return groupId;
    }

    // Create a new contact and add it to android contact address book.
    public static void addContact(ContentResolver contentResolver, Contact contact) {
        // Insert contact group membership data ( group id ).
        insertGroupId(contentResolver, contact.groupId(), contact.rawContactId());

        // Insert contact display, given and family name.
        insertName(contentResolver, contact);

        /* Insert contact phone list data, Content uri do not use ContactsContract.CommonDataKinds.Phone.CONTENT_URI
         * Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones */
        AppLogger.i(contact.phoneList().toString());
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contact.rawContactId(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                contact.phoneList());

        /* Insert contact email list data, Content uri do not use ContactsContract.CommonDataKinds.Email.CONTENT_URI
         * Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/emails */
        insertListData(contentResolver, ContactsContract.Data.CONTENT_URI,
                contact.rawContactId(),
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.LABEL,
                contact.emailList());


        // Insert organization data.
        insertOrganization(contentResolver, contact);

        // Insert contact website list.
        insertWebs(contentResolver,
                contact.rawContactId(),
                contact.websiteList());

        // Insert contact post address
        insertPostalAddress(contentResolver, contact);

        // Insert photo
        insertPhoto(contentResolver, contact);
    }

    /*
     *  Check whether the group exist of not.
     *  Return exist group id or -1 if group is not exist.
     * */
    public static long getExistGroup(ContentResolver contentResolver)
    {
        long ret = -1;

        String queryColumnArr[] = {ContactsContract.Groups._ID};

        StringBuffer whereClauseBuf = new StringBuffer();
        whereClauseBuf.append(ContactsContract.Groups.TITLE);
        whereClauseBuf.append("='");
        whereClauseBuf.append(AppConstants.GROUP_PHONE_TITLE);
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
    private static long insertGroup(ContentResolver contentResolver)
    {
        // Insert a group in group table.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.Groups.TITLE, AppConstants.GROUP_PHONE_TITLE);
        Uri groupUri = contentResolver.insert(ContactsContract.Groups.CONTENT_URI, contentValues);
        // Get the newly created raw contact id.
        long groupId = ContentUris.parseId(groupUri);

        return groupId;
    }

    /*
     *  Insert a new empty contact.
     *  Return newly created contact id.
     * */
    public static long insertContact(ContentResolver contentResolver, String displayName)
    {
        // Insert an empty contact in both contacts and raw_contacts table.
        // Return the system generated new contact and raw_contact id.
        // The id in contacts and raw_contacts table has same value.
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, displayName);
        contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_ALTERNATIVE, displayName);
        Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created raw contact id.
        long rawContactId = ContentUris.parseId(rawContactUri);
        return rawContactId;
    }

    /* Insert contact belongs group id in data table. */
    public static void insertGroupId(ContentResolver contentResolver, long groupRowId, long rawContactId)
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
                                       String dataValueColumnLabel,
                                       List<ContactField> contactFields) {

        ContentValues contentValues = new ContentValues();

        int size = contactFields.size();

        for(int i=0;i<size;i++) {

            ContactField contactField = contactFields.get(i);

            contentValues.clear();

            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

            contentValues.put(ContactsContract.Data.MIMETYPE, mimeType);

            contentValues.put(dataTypeColumnName, contactField.dataType());

            if(contactField.dataType() == 0) {
                contentValues.put(dataValueColumnLabel, contactField.dataLabel());
            }

            contentValues.put(dataValueColumnName, contactField.dataValue());

            contentResolver.insert(contentUri, contentValues);
        }

    }

    private static void insertWebs(ContentResolver contentResolver,
                                   long rawContactId,
                                   List<String> webs){
        ContentValues contentValues = new ContentValues();

        int size = webs.size();

        for(int i=0;i<size;i++) {

            contentValues.clear();

            contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

            contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);

            contentValues.put(ContactsContract.CommonDataKinds.Website.URL, webs.get(i));

            contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
        }

    }

    /*  Insert organization info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertOrganization(ContentResolver contentResolver, Contact contact) {
        ContentValues contentValues = new ContentValues();

        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contact.rawContactId());
        // Set data mimetype.
        contentValues.put(ContactsContract.CommonDataKinds.Organization.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        // Set company name.
        contentValues.put(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.company());
        // Set department.
        contentValues.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, contact.department());
        // Set title.
        contentValues.put(ContactsContract.CommonDataKinds.Organization.TITLE, contact.title());

        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);

    }

    /*  Insert name info into data table.
     *  ContentResolver contentResolver : The content resolver object.
     *  ContactDTO contactDto : Contact dto contains all the insert data.
     * */
    private static void insertName(ContentResolver contentResolver, Contact contact) {
        ContentValues contentValues = new ContentValues();

        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contact.rawContactId());
        // Set data mimetype.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // Set display name.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName());
        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    /* Insert contact postal address info in data table. */
    private static void insertPostalAddress(ContentResolver contentResolver, Contact contact)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contact.rawContactId());
        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
        contentValues.put(ContactsContract.CommonDataKinds.SipAddress.TYPE, contact.address().dataType());

        if(contact.address().dataType() == 0) {
            contentValues.put(ContactsContract.CommonDataKinds.SipAddress.LABEL, contact.address().dataLabel());
        }
        contentValues.put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, contact.address().dataValue());


        /* Insert to data table. Do not use uri ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
       Otherwise it will throw error java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/postals*/
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }

    /* Insert contact photo info in data table. */
    private static void insertPhoto(ContentResolver contentResolver, Contact contact)
    {
        ContentValues contentValues = new ContentValues();
        // Set raw contact id. Data table only has raw_contact_id.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, contact.rawContactId());
        // Set mimetype first.
        contentValues.put(ContactsContract.CommonDataKinds.Photo.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        // Set photo
        contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO, contact.contactPhoto());
        // Set photo file id.
        //contentValues.put(ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID, contactDto.getPhotoFieldId());

        // Insert to data table.
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues);
    }
}
