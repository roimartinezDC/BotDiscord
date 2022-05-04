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
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

import java.io.*;

import reactor.core.publisher.Mono;

import javax.swing.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class MyBot {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /** Directory to store authorization tokens for this application. */
    private static final String TOKENS_DIRECTORY_PATH = "resources"; //importante que sea resources

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    //permisos aquí (solo DRIVE)
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MyBot.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("264294599420-qbjj4p75858o1ve5pvgk5riktuiphkol.apps.googleusercontent.com");
        //returns an authorized Credential object.
        return credential;
    }

    public static void main(String[] args) {
        //Utilizamos el DiscordClient, que es la clase necesaria para interacutar con Discord
        //creamos uno con el token de nuestro bot, el cual obtuve en el portal para desarrolladores de Discord, al crear el bot en mi aplicación
        final String token = "OTUzNjMzMDMyMDg5MjcyMzQw.YjHZ-A.COFNVxvsyfl7kGN2GPNFA5AA608";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            //constructor del embed
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .color(Color.CINNABAR)
                    .title("Bobby")
                    .image("https://i.ytimg.com/vi/M6W9dPBthAU/hqdefault.jpg")
                    .build();

            //creacion del evento escuchador de mensajes
            gateway.on(MessageCreateEvent.class).subscribe(event -> {
                final Message message = event.getMessage();
                //declaracion de los comandos
                if ("!ping".equals(message.getContent())) {
                    final MessageChannel channel = message.getChannel().block();
                    //con esta línea el bot muestra un mensaje con los que sele indica
                    channel.createMessage("Pong!").block();
                }
                if ("!embed".equals(message.getContent())) {
                    final MessageChannel channel = message.getChannel().block();
                    //un mensaje del bot con un parámetro diferente (embed)
                    channel.createMessage(MessageCreateSpec.builder()
                            .content("Sigo?")
                            .addEmbed(embed)
                            .build()).subscribe();
                }
                if ("/list".equals((message.getContent()))) {
                    final MessageChannel channel = message.getChannel().block();

                    String ruta = "src/main/images";
                    File carpeta = new File(ruta);
                    //con File.list() se listan los contenidos del directorio
                    String[] imagenes = carpeta.list();
                    String res = "";
                    for (int i = 0; i < imagenes.length; i++) {
                        res += imagenes[i]+"\n";
                    }

                    channel.createMessage(res).block();
                }
                if (message.getContent().startsWith("/get")) {
                    final MessageChannel channel = message.getChannel().block();
                    //con un substring recojo solo el texto que haya después del comando /get
                    String archivo = message.getContent().substring(5, message.getContent().length());
                    //declaracion de un nuevo embed
                    EmbedCreateSpec embed2 = EmbedCreateSpec.builder()
                            .color(Color.BISMARK)
                            .title(archivo.split("\\.")[0])
                            .image("attachment://src/main/images/"+archivo)
                            .build();
                    //registramos el archivo que vamos a mostrar en el nuevo embed
                    InputStream fileAsInputStream = null;
                    boolean exists = true;
                    try {
                        fileAsInputStream = new FileInputStream("src/main/images/"+archivo);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        exists = false;
                    }
                    if (exists == true) {
                        channel.createMessage(MessageCreateSpec.builder()
                                .addFile("src/main/images/"+archivo, fileAsInputStream)
                                .addEmbed(embed2)
                                .build()).subscribe();
                    } else {
                        channel.createMessage("El archivo no existe").block();
                    }

                }
            });

            gateway.onDisconnect().block();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}