package edu.umd.lib.staffdir.google;

import java.io.FileInputStream;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;

public class DriveUploader {
  public static final Logger log = LoggerFactory.getLogger(DriveUploader.class);

  private final String appName;
  private final String serviceAccountCredentialsFile;

  /**
   * Class constructor for setting up Drive access
   *
   * @param appName
   * @param serviceAccountCredentialsFile
   */
  public DriveUploader(String appName, String serviceAccountCredentialsFile) {
    this.appName = appName;
    this.serviceAccountCredentialsFile = serviceAccountCredentialsFile;
  }

  /**
   * Attempt an upload to Google Drive.
   *
   * @param outputFileName
   *          path to file from params
   * @param serviceAccountCredentialsFile
   *          the Google ID of the file to be updated
   */
  public String UpdateFile(String outputFileName, String uploadId) throws IOException {
    FileInputStream keyFile = new FileInputStream(this.serviceAccountCredentialsFile);
    GoogleCredentials credentials = GoogleCredentials.fromStream(keyFile)
        .createScoped(Collections.singleton(DriveScopes.DRIVE));

    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
        credentials);

    Drive service = new Drive.Builder(new NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        requestInitializer)
        .setApplicationName(this.appName)
        .build();

    // Upload file to drive.
    File fileMetadata = new File();
    fileMetadata.setName("All Staff List.xlsx");

    // Set file's content.
    java.io.File filePath = new java.io.File(outputFileName);

    // Specify media type and file-path for file.
    FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filePath);
    try {
      Drive.Files.Update request = service.files().update(uploadId, new File(), mediaContent)
          .setFields("id");
      request.setSupportsAllDrives(true);
      request.getMediaHttpUploader().setDirectUploadEnabled(true);

      File file = request.execute();
      return file.getId();
    } catch (GoogleJsonResponseException e) {
      log.error("Unable to upload file: " + e.getDetails());
      throw e;
    }
  }

}
