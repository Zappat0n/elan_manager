package drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.DriveList;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.File;
import utils.MyLogger;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleDriveManager {
    private static final String TAG = GoogleDriveManager.class.getSimpleName();
    private static final String APPLICATION_NAME = " Elan-photo-manager";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "client_secret_1037714381902-5ba2p0i1n5ba6q42uuoaahlikdfbjuhv.apps.googleusercontent.com.json";
    private static final String CLIENTID = "1037714381902-5ba2p0i1n5ba6q42uuoaahlikdfbjuhv.apps.googleusercontent.com";
    Drive service;

    public GoogleDriveManager() throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(CLIENTID);
    }

    public String createFolder(String parentsId, String name) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(parentsId));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File file = service.files().create(fileMetadata)
                .setSupportsAllDrives(true)
                .setFields("id")
                .execute();
        System.out.println("Folder ID: " + file.getId());
        return file.getId();
    }

    public FileList getFiles() throws IOException {
        return service.files().list() //name contains '"+contains+"' and
                .setQ("mimeType = 'application/vnd.google-apps.file'")
                .execute();
    }

    public DriveList getDrives() throws IOException {
        return service.drives().list() //name contains '"+contains+"' and .setQ("mimeType = 'application/vnd.google-apps.folder'")
                .execute();
    }

    public FileList getDriveContent(String driveId) throws IOException {
        return service.files().list() //name contains '"+contains+"' and
                .setQ("mimeType='application/vnd.google-apps.folder'")
                //.setQ("parents in '"+folderId+"'")
                .setSupportsAllDrives(true)
                .setTeamDriveId(driveId)
                .setCorpora("drive")
                .setIncludeItemsFromAllDrives(true)
                .execute();
    }

    public FileList getFolderContent(String folderId, String contains) throws IOException {
        return service.files().list() //name contains '"+contains+"' and
                .setQ("mimeType='application/vnd.google-apps.folder' and name contains '" + contains
                        + "' and '" + folderId + "' in parents")
                //.setQ("parents in '"+folderId+"'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();
    }
/*
    public FileList getFolderContent(String folderId, String contains) throws IOException {
        return service.files().list() //name contains '"+contains+"' and
                .setQ("name contains '"+contains+"' and parents in '"+folderId+"'")
                .execute();
    }
*/
    public String uploadFile(java.io.File file, String folderId, String name) throws IOException {
        String mimeType = new MimetypesFileTypeMap().getContentType(file);
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(folderId));
        FileContent mediaContent = new FileContent(mimeType, file);
        File googleFile = service.files().create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id, parents")
                .execute();
        System.out.println("File: " + name + " uploaded");
        return  googleFile.getId();
    }

    public String uploadMediaFile(java.io.File mediaFile, String folderId, String name) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(folderId));
        FileContent mediaContent = new FileContent("image/jpeg", mediaFile);
        File file = service.files().create(fileMetadata, mediaContent)
                .setSupportsAllDrives(true)
                .setFields("id, parents")
                .execute();
        System.out.println("File: " + name + " uploaded");
        return  file.getId();
    }

    public BufferedImage download(String fileId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //BufferedInputStream in = new BufferedInputStream(new URL("https://drive.google.com/thumbnail?authuser=0&sz=w320&id="+fileId).openStream());
            //byte dataBuffer[] = new byte[1024];
            //int bytesRead;
            //while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            //    outputStream.write(dataBuffer, 0, bytesRead);
            //}
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);

            ByteArrayInputStream input = new ByteArrayInputStream(outputStream.toByteArray());
            return ImageIO.read(input);
        } catch (IOException e) {
            if (!(e instanceof HttpResponseException && ((HttpResponseException)e).getStatusCode() == 404 ))
                MyLogger.e(TAG, e);
            return null;
        }
    }

    public String uploadMediaFile(String folderId, Date date, String name, BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "image/jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AbstractInputStreamContent isc = new InputStreamContent("image/jpeg", is);

        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setParents(Collections.singletonList(folderId));
        //if (fileId != null) fileMetadata.setId(fileId);
        fileMetadata.setModifiedTime(new DateTime(date));
        File file = service.files().create(fileMetadata, isc)
                .setSupportsAllDrives(true)
                .setFields("id, parents")
                .execute();
        System.out.println("File: " + name + " uploaded");
        return  file.getId();
    }

    public String uploadMediaFile(String folderId, Date date, String fileId, MediaPicture picture) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(picture.image, "image/jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AbstractInputStreamContent isc = new InputStreamContent("image/jpeg", is);

        File fileMetadata = new File();
        fileMetadata.setName(picture.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));
        if (fileId != null) fileMetadata.setId(fileId);
        fileMetadata.setModifiedTime(new DateTime(date));
        File file = service.files().create(fileMetadata, isc)
                .setSupportsAllDrives(true)
                .setFields("id, parents")
                .execute();
        System.out.println("File: " + picture.getName() + " uploaded");
        return  file.getId();
    }


    public void updateFile(String fileId, Date date, MediaPicture picture) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(picture.image, "jpg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            AbstractInputStreamContent isc = new InputStreamContent("image/jpeg", is);
            DateTime dateTime = new DateTime(date);

            File fileMetadata = new File();
            fileMetadata.setName(picture.getName());
            fileMetadata.setModifiedTime(dateTime);
            //fileMetadata.setParents(Collections.singletonList(folderId));

            service.files().update(fileId, fileMetadata, isc)
                    .setSupportsAllDrives(true)
                    .setSupportsTeamDrives(true)
                    .execute();
        } catch (IOException e) {
            MyLogger.e(TAG, e);
        }
    }


    public Integer delete(String fileId) {
        try {
            service.files().delete(fileId)
                    .setSupportsAllDrives(true)
                    .setSupportsTeamDrives(true)
                    .execute();
            return 1;
        } catch (IOException e) {
            if (e instanceof HttpResponseException) return ((HttpResponseException)e).getStatusCode();
            else return 0;
        }
    }
}
