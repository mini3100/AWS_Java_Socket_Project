package socket_project_client;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import lombok.Getter;
import socket_project_client.dto.RequestBodyDto;
import socket_project_client.dto.SendMessage;

@Getter // 모든 멤버 변수들에 Getter가 생성
public class ClientGUI extends JFrame {

	// 싱글톤
	private static ClientGUI instance;

	public static ClientGUI getInstance() {
		if (instance == null) {
			instance = new ClientGUI(); // Client 생성(최초의 한 번)
		}
		return instance;
	}

	// 변수
	private String username; // 채팅하는 사람 이름
	private Socket socket;

	// mainCard
	private CardLayout mainCardLayout;
	private JPanel mainCardPanel;

	// chattingRoomList
	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private JLabel userNameLabel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;

	// chattingRoom
	private JPanel chattingRoomPanel;
	private JLabel roomNameLabel;
	private JTextField messageTextField;
	private JTextArea chattingTextArea;
	private JScrollPane userListScrollPanel;
	private DefaultListModel<String> userListModel;
	private JList userList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI frame = ClientGUI.getInstance();
					frame.setVisible(true);
					
					ClientReceiver clientReceiver = new ClientReceiver();
					clientReceiver.start(); // Thread 작동

					// 접속 이벤트
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("connection", frame.username);
					ClientSender.getInstance().send(requestBodyDto);				
					} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	private ClientGUI() {
		username = JOptionPane.showInputDialog(mainCardPanel, "아이디를 입력하세요");

		if (Objects.isNull(username)) {	// x 버튼 눌렀을 때
			System.exit(0);
		}
		if (username.isBlank()) {	// 아이디를 입력하지 않고 확인 눌렀을 때
			JOptionPane.showMessageDialog(mainCardPanel, "아이디를 입력하세요.", "접속 실패", JOptionPane.ERROR_MESSAGE);
			username = JOptionPane.showInputDialog(mainCardPanel, "아이디를 입력하세요.");
		}
		
		try {
			// 소켓 연결
			socket = new Socket("127.0.0.1", 8000);
		} catch(ConnectException e) {
			JOptionPane.showMessageDialog(mainCardPanel, "서버와의 연결에 실패했습니다.", "접속 실패", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		setTitle("Talk & Talk");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 370);

		// << mainCard >>
		mainCardLayout = new CardLayout();
		mainCardPanel = new JPanel();
		mainCardPanel.setLayout(mainCardLayout);
		setContentPane(mainCardPanel);

		// << chattingRoomList >>
		chattingRoomListPanel = new JPanel();
		chattingRoomListPanel.setBackground(new Color(227, 243, 253));
		chattingRoomListPanel.setLayout(null);
		mainCardPanel.add(chattingRoomListPanel, "chattingRoomListPanel");

		JLabel titleLabel = new JLabel("Talk & Talk");
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
		titleLabel.setBounds(12, 12, 122, 27);
		chattingRoomListPanel.add(titleLabel);

		userNameLabel = new JLabel(username + "님 환영합니다!");
		userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		userNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameLabel.setBounds(146, 12, 211, 27);
		chattingRoomListPanel.add(userNameLabel);

		JButton createRoomButton = new JButton("방 생성");
		createRoomButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		createRoomButton.setBounds(369, 13, 105, 27);
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = JOptionPane.showInputDialog(chattingRoomListPanel,"방제목을 입력하세요.");
				if(Objects.isNull(roomName)) {	//취소 버튼을 눌렀을 때
					return;
				}
				if(roomName.isBlank()) {		//방제목을 입력하지 않았을 때
					JOptionPane.showMessageDialog(chattingRoomListPanel, "방제목을 입력하세요.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
					return;
				}
				for(int i = 0; i < roomListModel.size(); i++) {
					if(roomListModel.get(i).equals(roomName)) {
						JOptionPane.showMessageDialog(chattingRoomListPanel, "이미 존재하는 방제목입니다.", "방만들기 실패", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel");	//패널 전환
				
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
			}
		});
		chattingRoomListPanel.add(createRoomButton);
		
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(12, 49, 462, 274);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) { //더블 클릭
					String roomName = roomListModel.get(roomList.getSelectedIndex());
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");	//패널 전환
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);

		// << chattingRoom >>
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBackground(new Color(253, 252, 244));
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
		chattingRoomPanel.setLayout(null);

		roomNameLabel = new JLabel();
		roomNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		roomNameLabel.setBounds(12, 10, 220, 22);
		chattingRoomPanel.add(roomNameLabel);

		JLabel userNameListLabel = new JLabel("접속 유저");
		userNameListLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameListLabel.setBounds(365, 9, 109, 24);
		chattingRoomPanel.add(userNameListLabel);

		JButton roomQuitButton = new JButton("나가기");
		roomQuitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("branch test");
			}
		});
		roomQuitButton.setBounds(264, 9, 89, 24);
		chattingRoomPanel.add(roomQuitButton);

		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(12, 42, 341, 237);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);

		chattingTextArea = new JTextArea();
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);

		JLabel toUserNameLabel = new JLabel("전체");
		toUserNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		toUserNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		toUserNameLabel.setBounds(12, 289, 55, 34);
		chattingRoomPanel.add(toUserNameLabel);

		messageTextField = new JTextField();
		messageTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		messageTextField.setBounds(79, 289, 395, 34);
		messageTextField.setColumns(10);
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					SendMessage sendMessage = SendMessage.builder().fromUsername(username)
							.messageBody(messageTextField.getText()).build();
					RequestBodyDto<SendMessage> requestBodyDto = new RequestBodyDto<>("sendMessage", sendMessage);

					ClientSender.getInstance().send(requestBodyDto);

					messageTextField.setText(""); // 전송후 텍스트필드 비우기
				}
			}
		});
		chattingRoomPanel.add(messageTextField);

		userListScrollPanel = new JScrollPane();
		userListScrollPanel.setBounds(365, 42, 109, 237);
		chattingRoomPanel.add(userListScrollPanel);

		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userListScrollPanel.setViewportView(userList);

	}
}
