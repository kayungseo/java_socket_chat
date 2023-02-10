package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
 
import core.SocketServer;
//작업흐름
//서버 쪽에서 SeverSocket을 생성
//클라이언트 쪽에서 해당 IP에 Socket을 생성
//서버쪽에서 접속하려는 클라이언트를 accept()하고 리스트에 클라이언트 저장
//클라이언트가 작업 요청 (채팅 프로그램이니 작업은 메세지 전송)
//서버 쪽 스레드에서 요청을 처리
//응답을 클라이언트에게 반환
//받은 메세지를 리스트에 있는 모든 클라이언트들에게 전달
//이유는 단체 채팅방에 있는 모든 유저는 채팅을 보낸 유저의 채팅을 볼수 있어야 하기 때문
//https://gegenpress.tistory.com/39
//https://ssons.tistory.com/49
// 소켓 : 프로그램이 네트워크에서 송수신할 수 있도록, 네트워크 환경에 연결할 수 있게 만들어진 연결부
// 두 프로그램이 네트워크를 통해 서로 통신을 수행할 수 있도록 양쪽에 생성되는 링크의 단자입니다.
// 두 소켓이 연결되면 서로 다른 프로세스끼리 데이터를 전달할 수 있습니다.
// 결국 소켓이 구현됨으로써 네트워크 및 전송 계층의 캡슐화가 가능해집니다. 
// 소켓은 원래 캘리포니아 버클리 대학 분교에서 UNIX용으로 개발되었으며, 
// UNIX에서의 입출력 메소드의 표준인 개방/읽기/쓰기/닫기 메커니즘을 따릅니다. 
/*
Chatting 서버
1. 클라이언트의 접속 -> ServerWorker Thread 생성 및 start
2. ServerWorker -> 개별 client에 채팅 서비스
3. 공지 채팅 가능하도록 변경 **
4. 채팅방 인원 공지 
*/
public class SocketServer{
	static ArrayList<ClientThread> list;//쓰레드 리스트 
	static String SERVER_IP = "192.168.7.36";
	static int SERVER_PORT = 1225; 
	 
	public static ServerSocket serverSocket = null;
	public static Socket socket = null;
	public static Queue<String> Packet = new LinkedList<>();
	
	public SocketServer() {
		list = new ArrayList<ClientThread>();
		System.out.println("서버 시작");
	}
	
	public void go() throws IOException {
		ServerSocket serverSocket = null;
		serverSocket = new ServerSocket();
		try {
			// 채팅 서버 시작
			serverSocket.bind(new InetSocketAddress(SERVER_IP, SERVER_PORT));//포트를 설정하고 서버 소켓을 만듦
			System.out.println("** Server binding 완료 **");
			
			// 다수의 클라이언트에게 지속적으로 서비스하기 위해 while 이용
			while (true) { // while 무한반복문 안에서 클라이언트가 접속 시 Socket 정보를 받음
				Socket socket = serverSocket.accept();//연결 요청을 받아들여 소켓 간 연결을 수립
				ClientThread sw = new ClientThread(socket);//Runnable 구현 쓰레드에 클라이언트의 Socket 정보를 넣어주고 start 
				list.add(sw);
				Thread thread = new Thread(sw);//Runnable로 쓰레드 생성한 경우 Thread 생성후 start() 호출 
				thread.start();
			}

		} finally {
			if (serverSocket != null)
				serverSocket.close();
			System.out.println("**ChatServer 종료합니다**");
		}
	}
	
	//구분자 없애기
	public static void sendMessage(String message) {  
		System.out.println(message); //서버에게
		for (int i=0;i<list.size();i++) { list.get(i).pw.println(message); }//클라이언트 모두에게 전달 
	}
	public static void main(String[] args) {
		try {
			new SocketServer().go();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}