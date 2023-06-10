import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebSocketServer {
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;

    // Множество для отслеживания подключенных клиентов
    private final Set<String> connectedClients = new HashSet<>();

    // Множество для хранения сгенерированных чисел
    private final Set<BigInteger> generatedNumbers = new HashSet<>();

    // Пул потоков для обработки клиентских запросов
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                // Ожидание подключения клиента
                Socket socketClient = serverSocket.accept();

                // Вывод информации о новом подключении
                System.out.println("New client connection: " + socketClient);

                // Обработка подключения в отдельном потоке
                threadPool.execute(() -> handleConnection(socketClient));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Завершение работы пула потоков
            threadPool.shutdown();
        }
    }

    private void handleConnection(Socket socketClient) {
        try {
            // Получение IP-адреса клиента
            String clientAddress = socketClient.getInetAddress().getHostAddress();

            // Проверка, подключен ли клиент уже
            if (isClientConnected(clientAddress)) {
                // Отклонение подключения, если клиент уже подключен
                System.out.println("Connection rejected. Already connected from " + clientAddress);
                socketClient.close();
                return;
            }

            // Добавление IP-адреса клиента в множество подключенных клиентов
            connectedClients.add(clientAddress);
            System.out.println("New connection from " + clientAddress);

            // Отправка ответа клиенту
            sendResponse(socketClient);

            // Вывод информации о разрыве соединения с клиентом
            System.out.println("Client disconnected: " + clientAddress);

            // Удаление IP-адреса клиента из множества подключенных клиентов
            connectedClients.remove(clientAddress);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Закрытие сокета
            closeSocket(socketClient);
        }
    }

    private boolean isClientConnected(String ipAddress) {
        synchronized (connectedClients) {
            // Проверка, содержится ли IP-адрес клиента в множестве подключенных клиентов
            return connectedClients.contains(ipAddress);
        }
    }

    private void sendResponse(Socket socketClient) throws IOException {
        BigInteger randomBigInteger;
        do {
            // Генерация случайного числа типа BigInteger
            randomBigInteger = generateRandomBigInteger();
        } while (generatedNumbers.contains(randomBigInteger));

        // Добавление сгенерированного числа в множество сгенерированных чисел
        generatedNumbers.add(randomBigInteger);
        System.out.println(randomBigInteger);

        // Формирование JSON-строки с сгенерированным числом
        String json = "{\"number\":" + randomBigInteger + "}";

        try (OutputStream output = socketClient.getOutputStream()) {
            // Отправка HTTP-ответа клиенту
            output.write("HTTP/1.1 200 OK\r\n".getBytes());
            output.write("Content-Type: application/json\r\n".getBytes());
            output.write(("Content-Length: " + json.length() + "\r\n").getBytes());
            output.write("\r\n".getBytes());
            output.write(json.getBytes());

            // Чтение данных из сокета (для очистки буфера)
            try (InputStream inputStream = socketClient.getInputStream()) {
                byte[] buffer = new byte[1024];
                while (inputStream.read(buffer) != -1) ;
            }
        }
    }

    private BigInteger generateRandomBigInteger() {
        Random random = new Random();
        // Генерация случайного числа типа BigInteger с заданной длиной
        return new BigInteger(128, random);
    }

    private void closeSocket(Socket socket) {
        try {
            // Закрытие сокета
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Создание экземпляра сервера и запуск сервера
        WebSocketServer server = new WebSocketServer();
        server.start();
    }
}