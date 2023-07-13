package socket_project_client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.CardLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ClientGUI extends JFrame {
	
	//변수
	private String username; // 채팅하는 사람 이름
	private Socket socket;
	
	//mainCard
	private CardLayout mainCardLayout;
	private JPanel mainCardPanel;
	
	//chattingRoomList
	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;
	
	//chattingRoom
	private JPanel chattingRoomPanel;
	private JTextField messageTextField;
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
					ClientGUI frame = new ClientGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientGUI() {
		username = JOptionPane.showInputDialog(chattingRoomPanel, "아이디를 입력하세요.");

		if (Objects.isNull(username)) {
			System.exit(0);
		}
		if (username.isBlank()) {
			System.exit(0);
		}
		
		try {
			// 소켓 연결
			socket = new Socket("127.0.0.1", 8000);
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
		
		JLabel userNameLabel = new JLabel("userName");
		userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		userNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameLabel.setBounds(146, 12, 211, 27);
		chattingRoomListPanel.add(userNameLabel);
		
		JButton createRoomButton = new JButton("방 생성");
		createRoomButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		createRoomButton.setBounds(369, 13, 105, 27);
		chattingRoomListPanel.add(createRoomButton);
		
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(12, 49, 462, 274);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomListScrollPanel.setViewportView(roomList);
		
		// << chattingRoom >>
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBackground(new Color(253, 252, 244));
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
		chattingRoomPanel.setLayout(null);
		
		JLabel roomNameLabel = new JLabel("roomName");
		roomNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		roomNameLabel.setBounds(12, 10, 220, 22);
		chattingRoomPanel.add(roomNameLabel);
		
		JLabel userNameListLabel = new JLabel("접속 유저");
		userNameListLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameListLabel.setBounds(365, 9, 109, 24);
		chattingRoomPanel.add(userNameListLabel);
		
		JButton roomQuitButton = new JButton("나가기");
		roomQuitButton.setBounds(284, 9, 69, 24);
		chattingRoomPanel.add(roomQuitButton);
		
		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(12, 42, 341, 237);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);
		
		JLabel toUserNameLabel = new JLabel("전체");
		toUserNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		toUserNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		toUserNameLabel.setBounds(12, 289, 55, 34);
		chattingRoomPanel.add(toUserNameLabel);
		
		messageTextField = new JTextField();
		messageTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		messageTextField.setBounds(79, 289, 395, 34);
		chattingRoomPanel.add(messageTextField);
		messageTextField.setColumns(10);
		
		userListScrollPanel = new JScrollPane();
		userListScrollPanel.setBounds(365, 42, 109, 237);
		chattingRoomPanel.add(userListScrollPanel);
		
		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userListScrollPanel.setViewportView(userList);
		
	}
}
