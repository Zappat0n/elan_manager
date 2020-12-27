package drive;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;

import java.io.IOException;

public class CustomProgressListener implements MediaHttpUploaderProgressListener {
    public void progressChanged(MediaHttpUploader uploader) throws IOException {
        switch (uploader.getUploadState()) {
            case INITIATION_STARTED:
                System.out.println("Initiation has started!");
                break;
            case INITIATION_COMPLETE:
                System.out.println("Initiation is complete!");
                break;
            case MEDIA_IN_PROGRESS:
                System.out.println(uploader.getProgress());
                break;
            case MEDIA_COMPLETE:
                System.out.println("Upload is complete!");
        }
    }
}
