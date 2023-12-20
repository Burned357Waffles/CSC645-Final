import java.io.*;
import java.net.*;

class TextClient {
    static BufferedReader inFromUser;
    static Socket clientSocket;
    static DataOutputStream outToServer;
    static BufferedReader inFromServer;
    static boolean loggedIn = false;
    
    
    // Method to establish a connection with the server
    private static void connectToServer() throws IOException
    {
        // Create a socket connection to the server
        clientSocket = new Socket("127.0.0.1", 6789);
    
        // Create output stream to send data to the server
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
    
        // Create input stream to receive data from the server
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    
    
    // Method to log into the server
    private static void login() throws IOException
    {
        // Perform login process until successfully logged in
        while (!loggedIn) {
            System.out.print("Enter username: ");
            String username = inFromUser.readLine();
            outToServer.writeBytes(username + "\n"); // Send the username to the server
            
            System.out.print("Enter password: ");
            String password = inFromUser.readLine();
            outToServer.writeBytes(password + "\n"); // Send the password to the server
            
            if (inFromServer.read() == 1) {
                loggedIn = true;
                System.out.println("\nAccess Granted");
            }
            else
                System.out.println("\nAccess Denied – Username/Password Incorrect");
        }
    }
    
    
    // Method to retrieve and display the list of users
    private static void getUsers() throws IOException {
        System.out.println("Getting list of users...\n==================================================");
        int userCount = Integer.parseInt(inFromServer.readLine());
        for (int i = 0; i < userCount; i++) {
            System.out.println(inFromServer.readLine());
        }
    }
    
    
    // Method to send a message to another user
    private static void sendMessage() throws IOException {
        System.out.print("\nEnter a username you want to send a message to: ");
        outToServer.writeBytes(inFromUser.readLine() + '\n'); // Send recipient's username to the server
        
        if (inFromServer.read() == 0) {
            System.out.println("Invalid user");
            return;
        }
        
        System.out.print("Enter the message you want to send: ");
        outToServer.writeBytes(inFromUser.readLine() + '\n'); // Send the message to the server
        System.out.println("\n Status: Message sent successful");
    }
    
    
    // Method to read and display messages for the user
    private static void readMessage() throws IOException {
        System.out.println("\nHere are your messages:\n==================================================");
        int messageCount = Integer.parseInt(inFromServer.readLine());
        
        if (messageCount == 0) {
            System.out.println("*** You have no messages ***");
        }
        
        for (int i = 0; i < messageCount; i++) {
            System.out.println(inFromServer.readLine());
        }
    }
    
    
    public static void main(String[] argv) throws Exception {
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            System.out.println("""
                    0. Connect to the server
                    1. Get the user list
                    2. Send a message
                    3. Get my messages
                    4. Exit""");
            
            System.out.print("\nPlease enter a choice: ");
            String option = inFromUser.readLine();
            
            // Check if the user is logged in. This prevents options 1, 2, and 3 from being executed
            if (!option.equals("0") && !option.equals("4") && !loggedIn) {
                System.out.println("\nError: Not logged in. Please choose option 0 and login to use commands.\n");
                continue;
            }
            
            // Process user-selected options
            switch (option) {
                case "0" -> {
                    if (loggedIn)
                        System.out.println("Already logged in.");
                    
                    connectToServer();
                    outToServer.writeBytes(option + "\n");
                    login();
                }
                case "1" -> {
                    outToServer.writeBytes(option + "\n");
                    getUsers();
                }
                case "2" -> {
                    outToServer.writeBytes(option + "\n");
                    sendMessage();
                }
                case "3" -> {
                    outToServer.writeBytes(option + "\n");
                    readMessage();
                }
                case "4" -> {
                    if (loggedIn) {
                        outToServer.writeBytes(option + "\n");
                        clientSocket.close();
                    }
                    System.exit(0);
                }
            }
            
            System.out.println("==================================================");
        }
    }
}
