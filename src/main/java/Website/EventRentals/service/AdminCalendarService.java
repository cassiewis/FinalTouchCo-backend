// package Website.EventRentals.service;

// import com.google.api.client.auth.oauth2.Credential;
// import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
// import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
// import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
// import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
// import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.json.JsonFactory;
// import com.google.api.client.json.gson.GsonFactory;
// import com.google.api.client.util.store.FileDataStoreFactory;
// import com.google.api.services.calendar.Calendar;
// import com.google.api.services.calendar.CalendarScopes;
// import com.google.api.services.calendar.model.*;

// import org.springframework.stereotype.Service;

// import java.io.File;
// import java.io.IOException;
// import java.security.GeneralSecurityException;
// import java.time.LocalDate;
// import java.util.Collections;
// import java.util.List;

// @Service
// public class AdminCalendarService {

//     private static final String APPLICATION_NAME = "Final Touch Decor Calendar";
//     private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//     private static final String TOKENS_DIRECTORY_PATH = "tokens";

//     private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
//     private static final String CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID");
//     private static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");

//     private static final String CALENDAR_NAME = "Final Touch Decor Reservations";

//     private Calendar calendarService;

//     public AdminCalendarService() throws Exception {
//         // Only initialize if environment variables are set
//         if (CLIENT_ID != null && CLIENT_SECRET != null) {
//             this.calendarService = getCalendarService();
//         } else {
//             System.out.println("WARNING: Google Calendar integration disabled - GOOGLE_CLIENT_ID or GOOGLE_CLIENT_SECRET not set");
//             this.calendarService = null;
//         }
//     }

//     private Calendar getCalendarService() throws Exception {
//         var clientSecrets = new GoogleClientSecrets()
//                 .setInstalled(new GoogleClientSecrets.Details()
//                         .setClientId(CLIENT_ID)
//                         .setClientSecret(CLIENT_SECRET)
//                         .setAuthUri("https://accounts.google.com/o/oauth2/auth")
//                         .setTokenUri("https://oauth2.googleapis.com/token"));

//         var flow = new GoogleAuthorizationCodeFlow.Builder(
//                 GoogleNetHttpTransport.newTrustedTransport(),
//                 JSON_FACTORY,
//                 clientSecrets,
//                 SCOPES
//         ).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
//          .setAccessType("offline")
//          .build();

//         var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//         Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

//         return new Calendar.Builder(
//                 GoogleNetHttpTransport.newTrustedTransport(),
//                 JSON_FACTORY,
//                 credential
//         ).setApplicationName(APPLICATION_NAME).build();
//     }

//     public String createEvent(String summary, String description, LocalDate date) throws IOException {
//         Event event = new Event()
//                 .setSummary(summary)
//                 .setDescription(description);

//         EventDateTime startDate = new EventDateTime()
//                 .setDateTime(new com.google.api.client.util.DateTime(date.toString() + "T00:00:00Z"))
//                 .setTimeZone("America/Boise");

//         event.setStart(startDate);
//         event.setEnd(startDate); // all day event, so end time is the same as start time

//         event = calendarService.events().insert(CALENDAR_NAME, event).execute();
//         return event.getId();
//     }

//     public void updateEvent(String eventId, String newSummary) throws IOException {
//         Event event = calendarService.events().get(CALENDAR_NAME, eventId).execute();
//         event.setSummary(newSummary);
//         calendarService.events().update(CALENDAR_NAME, eventId, event).execute();
//     }

//     public void deleteEvent(String eventId) throws IOException {
//         calendarService.events().delete(CALENDAR_NAME, eventId).execute();
//     }

// }