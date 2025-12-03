package at.tgm.server;

import at.tgm.network.core.SocketClient;
import at.tgm.network.packets.S2CPOSTQuiz;
import at.tgm.objects.Distro;
import at.tgm.objects.Quiz;

import java.io.IOException;
import java.net.Socket;

public class ServerClient extends SocketClient {
    private Quiz quiz;

    public ServerClient(Socket socket) throws IOException {
        super(socket, Distro.SERVER);
    }

    public void startQuiz() throws IOException {

        if (quiz == null){
            System.out.println("Quiz started for: " + this.getNutzer().getUsername());

            quiz = new Quiz(10, System.currentTimeMillis());

            this.send(new S2CPOSTQuiz(quiz.getCensoredItems()));

        }

    }
}
