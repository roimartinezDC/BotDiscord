import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import java.io.File;
import reactor.core.publisher.Mono;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MyBot {
    public static void main(String[] args) {
        //Utilizamos el DiscordClient, que es la clase necesaria para interacutar con Discord
        //creamos uno con el token de nuestro bot, el cual obtuve en el portal para desarrolladores de Discord, al crear el bot en mi aplicación
        final String token = "OTUzNjMzMDMyMDg5MjcyMzQw.YjHZ-A.gDuIN9vehSI6VOqYgyKa7tlv5E8";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();

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

                String ruta = "C:\\03.COD\\BotDiscord\\src\\main\\images";
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
                        .image("attachment://C:\\03.COD\\BotDiscord\\src\\main\\images\\"+archivo)
                        .build();
                //registramos el archivo que vamos a mostrar en el nuevo embed
                InputStream fileAsInputStream = null;
                boolean exists = true;
                try {
                    fileAsInputStream = new FileInputStream("C:\\03.COD\\BotDiscord\\src\\main\\images\\"+archivo);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    exists = false;
                }
                if (exists == true) {
                    channel.createMessage(MessageCreateSpec.builder()
                            .addFile("C:\\03.COD\\BotDiscord\\src\\main\\images\\"+archivo, fileAsInputStream)
                            .addEmbed(embed2)
                            .build()).subscribe();
                } else {
                    channel.createMessage("El archivo no existe").block();
                }

            }
        });

        gateway.onDisconnect().block();
    }
}