package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
//소켓 : 프로그램이 네트워크에서 송수신할 수 있도록, 네트워크 환경에 연결할 수 있게 만들어진 연결부
//두 프로그램이 네트워크를 통해 서로 통신을 수행할 수 있도록 양쪽에 생성되는 링크의 단자입니다.
//두 소켓이 연결되면 서로 다른 프로세스끼리 데이터를 전달할 수 있습니다.
//결국 소켓이 구현됨으로써 네트워크 및 전송 계층의 캡슐화가 가능해집니다. 
//소켓은 원래 캘리포니아 버클리 대학 분교에서 UNIX용으로 개발되었으며, 
//UNIX에서의 입출력 메소드의 표준인 개방/읽기/쓰기/닫기 메커니즘을 따릅니다. 
/*
Chatting 서버
1. 클라이언트의 접속 -> ServerWorker Thread 생성 및 start
2. ServerWorker -> 개별 client에 채팅 서비스
3. 공지 채팅 가능하도록 변경 **
4. 채팅방 인원 공지 
*/
//TCP 서버는 여러 클라이언트의 요청을 처리 할 수 있어야 하고, 동시에 각 클라이언트들과 통신을 할 수 있어야 합니다.
//그래서 필요한 것이 쓰레드 
//쓰레드를 통해 TCPServer의 메인 메서드는 무한루프를 돌면서 요청을 계속 받아들이고, 요청이 오면 새로운 쓰레드를 생성하여 각 클라이언트들과 통신을 할 수 있도록 하는 개선할 것입니다.

public class ClientThread implements Runnable{
	private Socket socket;
	private BufferedReader br;
	PrintWriter pw;
	private String user;
	
	public ClientThread(Socket socket) {
		super();
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(),true);
			 
			while (true) { 
				String packet = br.readLine();
				System.out.println("packet: "+packet);
				String[] msgArr = packet.split(";");
				String division = msgArr[0];				
				String content = "";
				for(int i=1; i<msgArr.length; i++) {
					content = content + msgArr[i];
				} 
				if (content.trim().equals("quit")) {
					break;
				}
				LocalDate today = LocalDate.now();
				LocalTime now = LocalTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
				String formatedNow = now.format(formatter);
				if(division.equals("00x0")) {
					user = content;
					SocketServer.sendMessage(today +" "+ formatedNow +" ["+user+"]님이 입장하셨습니다. (현재 접속 인원: "+SocketServer.list.size()+"명)");
				}else if(division.equals("00x1")) {  
					SocketServer.sendMessage(user+"님: "+content);
				}else { 
					break;
				}
//					Scanner scanner = new Scanner(System.in);
//					System.out.println("[전체에게] : ");
//					String notice = scanner.nextLine();
//					pw.println("[관리자] :"+notice);
			}  
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				closeAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
			SocketServer.sendMessage(user+"님이 나가셨습니다 (현재 접속 인원: " + SocketServer.list.size() + "명)");
			SocketServer.list.remove(this);
		}
	}
 
	public void closeAll() throws IOException {
		if (pw != null)
			pw.close();
		if (br != null)
			br.close();
		if (socket != null)
			socket.close();
	} 
 
}
