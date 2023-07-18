package socket_project_client;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import lombok.Getter;
import lombok.Setter;
import socket_project_client.dto.RequestBodyDto;
import socket_project_client.dto.SendMessage;

@Getter // 모든 멤버 변수들에 Getter가 생성
@Setter
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
	private String roomName;
	private Socket socket;
	private boolean isWhisper;
	private String toUsername;	//귓속말 채팅받는 사람
	private int index;

	// mainCard
	private CardLayout mainCardLayout;
	private JPanel mainCardPanel;

	// chattingRoomList
	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private JScrollPane connectedUserListScrollPanel;
	private JLabel userNameLabel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;
	private DefaultListModel<String> connectedUserListModel;
	private JList connectedUserList;
	private CustomCellRenderer connectedUserListcellRenderer;

	// chattingRoom
	private JPanel chattingRoomPanel;
	private JLabel roomNameLabel;
	private JTextField messageTextField;
	private JTextArea chattingTextArea;
	private JScrollPane userListScrollPanel;
	private DefaultListModel<String> userListModel;
	private JList userList;
	private JLabel connectedUserLabel;
	private CustomCellRenderer userListcellRenderer;
	private JRadioButton entireRadioButton;
	private JRadioButton whisperRadioButton;
	private JLabel toUserNameLabel;

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

	private ClientGUI() {
		isWhisper = false;
		while(true) {
			username = JOptionPane.showInputDialog(mainCardPanel, "아이디를 입력하세요");
			
			if (Objects.isNull(username)) {	// x 버튼 눌렀을 때
				System.exit(0);
			}
			if (!username.isBlank()) {	// 아이디를 입력하지 않고 확인 눌렀을 때
				break;
			}
			JOptionPane.showMessageDialog(mainCardPanel, "아이디를 입력하세요.", "접속 실패", JOptionPane.ERROR_MESSAGE);
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
		setBounds(100, 100, 500, 530);

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
		titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		titleLabel.setBounds(12, 12, 191, 27);
		chattingRoomListPanel.add(titleLabel);

		userNameLabel = new JLabel();
		
		userNameLabel.setHorizontalAlignment(SwingConstants.LEFT);
		userNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameLabel.setBounds(12, 47, 253, 27);
		chattingRoomListPanel.add(userNameLabel);

		JButton createRoomButton = new JButton("방 만들기");
		createRoomButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		createRoomButton.setBounds(271, 47, 86, 27);
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				roomName = JOptionPane.showInputDialog(chattingRoomListPanel,"방제목을 입력하세요.");
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
				roomNameLabel.setText("[" + roomName + "]");
				
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel");	//패널 전환
				
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				chattingTextArea.setText("");
				messageTextField.requestFocus();	//채팅방 들어갔을 때 메시지 창에 focus 가도록
			}
		});
		chattingRoomListPanel.add(createRoomButton);
		
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(12, 84, 345, 399);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		roomListModel = new DefaultListModel<String>();
		roomList = new JList(roomListModel);
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) { //더블 클릭
					roomName = roomListModel.get(roomList.getSelectedIndex());
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");	//패널 전환
					entireRadioButton.setSelected(true);
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
					roomNameLabel.setText("[" + roomName + "]");
					chattingTextArea.setText("");
					messageTextField.requestFocus();
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);

		connectedUserLabel = new JLabel("전체 접속자");
		connectedUserLabel.setHorizontalAlignment(SwingConstants.CENTER);
		connectedUserLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		connectedUserLabel.setBounds(369, 47, 106, 27);
		chattingRoomListPanel.add(connectedUserLabel);
		
		connectedUserListScrollPanel = new JScrollPane();
		connectedUserListScrollPanel.setBounds(369, 84, 105, 399);
		chattingRoomListPanel.add(connectedUserListScrollPanel);
		
		connectedUserListModel = new DefaultListModel<String>();
		connectedUserList = new JList(connectedUserListModel);
		connectedUserList.setLocation(369, 0);
		connectedUserListcellRenderer = new CustomCellRenderer();
		connectedUserListScrollPanel.setViewportView(connectedUserList);
		
		// << chattingRoom >>
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBackground(new Color(253, 252, 244));
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
		chattingRoomPanel.setLayout(null);

		roomNameLabel = new JLabel();
		roomNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		roomNameLabel.setBounds(12, 10, 244, 24);
		chattingRoomPanel.add(roomNameLabel);

		JLabel userNameListLabel = new JLabel("참여 인원");
		userNameListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userNameListLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		userNameListLabel.setBounds(365, 44, 109, 24);
		chattingRoomPanel.add(userNameListLabel);

		JButton roomQuitButton = new JButton("나가기");
		roomQuitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("quit", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				mainCardLayout.show(mainCardPanel, "chattingRoomListPanel");
			}
		});
		roomQuitButton.setBounds(362, 10, 109, 24);
		chattingRoomPanel.add(roomQuitButton);

		JScrollPane chattingTextAreaScrollPanel = new JScrollPane();
		chattingTextAreaScrollPanel.setBounds(12, 70, 341, 368);
		chattingRoomPanel.add(chattingTextAreaScrollPanel);

		chattingTextArea = new JTextArea();
		chattingTextAreaScrollPanel.setViewportView(chattingTextArea);

		toUserNameLabel = new JLabel("전체");
		toUserNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		toUserNameLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		toUserNameLabel.setBounds(15, 449, 55, 34);
		chattingRoomPanel.add(toUserNameLabel);

		messageTextField = new JTextField();
		messageTextField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		messageTextField.setBounds(79, 449, 395, 34);
		messageTextField.setColumns(10);
		messageTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					RequestBodyDto<SendMessage> requestBodyDto = null;
					SendMessage sendMessage;
					sendMessage = SendMessage.builder().fromUsername(username)
								.messageBody(messageTextField.getText()).build();
					
					if(isWhisper) {
						String toUsername = userListModel.get(userList.getSelectedIndex()).replace("(방장)", "");
						sendMessage = SendMessage.builder().fromUsername(username).toUsername(toUsername)
								.messageBody(messageTextField.getText()).build();
					}
					
					requestBodyDto = new RequestBodyDto<>("sendMessage", sendMessage);
					ClientSender.getInstance().send(requestBodyDto);
					messageTextField.setText(""); // 전송후 텍스트필드 비우기
				}
			}
		});
		chattingRoomPanel.add(messageTextField);

		userListScrollPanel = new JScrollPane();
		userListScrollPanel.setBounds(365, 70, 109, 368);
		chattingRoomPanel.add(userListScrollPanel);

		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userList.setLocation(363, 0);
		userListcellRenderer = new CustomCellRenderer();
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) { //더블 클릭
					index = userList.getSelectedIndex();
					toUsername = userListModel.get(index);
					toUserNameLabel.setText(toUsername);
					isWhisper = true;
					whisperRadioButton.setSelected(true);
				}
			}
		});
		userListScrollPanel.setViewportView(userList);
		
		//RadioButton
		entireRadioButton = new JRadioButton("전체");
		entireRadioButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		entireRadioButton.setBackground(new Color(253, 252, 244));
		entireRadioButton.setSelected(true);
		entireRadioButton.setBounds(275, 10, 79, 22);
		entireRadioButton.addItemListener(new MyItemListener());
		chattingRoomPanel.add(entireRadioButton);
		
		whisperRadioButton = new JRadioButton("귓속말");
		whisperRadioButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
		whisperRadioButton.setBackground(new Color(253, 252, 244));
		whisperRadioButton.setBounds(275, 39, 79, 22);
		whisperRadioButton.addItemListener(new MyItemListener());
		chattingRoomPanel.add(whisperRadioButton);
		
		ButtonGroup group = new ButtonGroup();
		group.add(entireRadioButton);
		group.add(whisperRadioButton);

		//windowClosing시 user 삭제
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(chattingRoomPanel.isShowing()) {
					RequestBodyDto<String> quitRoomRequestBodyDto = new RequestBodyDto<String>("quit", roomName);
					ClientSender.getInstance().send(quitRoomRequestBodyDto);					
				}
				
				RequestBodyDto<String> disconnectionRequestBodyDto = new RequestBodyDto<String>("disconnection", username);
				ClientSender.getInstance().send(disconnectionRequestBodyDto);
			}
		});
	}
	
	//RadioButton event
	class MyItemListener implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(entireRadioButton.isSelected()){
				isWhisper = false;
				userList.clearSelection();
				index = 0;
				ClientGUI.getInstance().getToUserNameLabel().setText("전체");
			}
			else if(whisperRadioButton.isSelected()){
				userList.setSelectedIndex(index);
				toUsername = userListModel.get(index);
				ClientGUI.getInstance().getToUserNameLabel().setText(toUsername);
				
				isWhisper = true;
			}
			messageTextField.requestFocus();
		}   
	}
	
	@Setter 
	class CustomCellRenderer extends DefaultListCellRenderer {
		    
		private int targetIndex;
	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
	                                                  boolean isSelected, boolean cellHasFocus) {
	        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	        
	        if (index == targetIndex) {
	            component.setForeground(new Color(29,132,255));
	        } else {
	            component.setForeground(Color.BLACK);
	        }
	        
	        return component;
	    }
	}

}
