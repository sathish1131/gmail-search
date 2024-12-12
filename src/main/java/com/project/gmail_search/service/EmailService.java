package com.project.gmail_search.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.MessagePart;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.GeneralSecurityException;

import java.util.*;

import org.json.JSONObject;


@Service
public class EmailService{
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "TestGmailAPI";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "src/main/resources/tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    // Load application.properties file
    FileInputStream input = new FileInputStream("src/main/resources/application.properties");
    Properties properties = new Properties();
    properties.load(input);
    //Update the credentials.json file
    String content = new String(Files.readAllBytes(Paths.get("src/main/resources/credentials.json")));
    JSONObject credentials = new JSONObject(content);
    JSONObject installed = credentials.getJSONObject("installed");
    installed.put("client_id", properties.getProperty("id"));
    installed.put("client_secret", properties.getProperty("sec"));
    // Load client secrets.
    InputStream in = new ByteArrayInputStream(credentials.toString(4).getBytes());
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

  public static Map<String, Object> searchEmails(String query) throws IOException, GeneralSecurityException {
  	Gmail service = initialiseGmailService();
  	String user = "me";
  	ListMessagesResponse messageResponse = service.users().messages().list(user).setQ(query).execute();
  	List<Message> messages = messageResponse.getMessages();
    Map<String, Object> map = new HashMap<>();
    List<Map<String, String>> list = new ArrayList<>();
    if(messages != null){
      for(Message message : messages){
        Message fullMessage = service.users().messages().get(user, message.getId()).setFormat("full").execute();
        String subject = "No subject";
        for(MessagePartHeader header : fullMessage.getPayload().getHeaders()){
          if("Subject".equalsIgnoreCase(header.getName())){
            subject = header.getValue();
            break;
          }
        }
        String body = "No content available";
        if(fullMessage.getPayload().getParts() != null){
          for(MessagePart part : fullMessage.getPayload().getParts()){
            if("text/plain".equals(part.getMimeType())){
              body = new String(part.getBody().decodeData());
              break;
            }
          }
        }
        list.add(Map.of("id", message.getId(), "subject", subject, "body", body));
        map.put("messages", list);
      }
    }
    return map;
  }

  private static Gmail initialiseGmailService() throws IOException,GeneralSecurityException {
  	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();
    return service;
  }
  

}