package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Publisher {
    private static List<String> topics = new ArrayList<>();
    private static SocketChannel socketChannel = null;
    private static Charset charset = Charset.forName("UTF-8");
    public Publisher(){
       new PublisherGui(this);
    }
    public static void main(String[] args) {
        new Publisher();

        String server = "localhost";
        int serverPort = 50000;

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(server, serverPort));

            while (!socketChannel.finishConnect()) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void closingPublisher(){
        try {
            socketChannel.close();
            socketChannel.socket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<String> getTopics(){
        return topics;
    }
    public boolean addTopic(String topic) throws IOException {
        if(topics.contains(topic)){
            return false;
        }else {
            topics.add(topic);
            String topicJson = "{\"addTopic\":\"" + topic + "\"}\n";
            socketChannel.write(charset.encode(topicJson));
            return true;
        }
    }

    public void removeTopic(String topic) throws IOException {
        topics.remove(topic);
        String topicJson = "{\"removeTopic\":\"" + topic + "\"}\n";
        socketChannel.write(charset.encode(topicJson));
    }
    public void removeAllTopics() throws IOException {
        String topicJson = "{\"removeAllTopics\":\"true\"}\n";
        socketChannel.write(charset.encode(topicJson));
    }

    public void topicNews(String topic, String news) throws IOException {
        String newsJson = "{\"topicNews\":\"" + topic + "\",\"news\":\""+news+"\"}\n";
        socketChannel.write(charset.encode(newsJson));
    }
}
