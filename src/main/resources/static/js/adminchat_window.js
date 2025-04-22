let stompClient;
let currentSubscription;
let sender;

const messagesDiv = document.getElementById('messages');

// Kết nối WebSocket
const connect = () => {
    sender = document.getElementById("sender").value;
    if (!sender) {
        console.error("Người gửi chưa được xác định.");
        return;
    }

    const socket = new SockJS(`/ws?user=${sender}`);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log('Connected to WebSocket as:', sender);
        subscribeToUser(sender);
    }, (error) => {
        console.error('Connection failed:', error);
    });
};

// Đăng ký nhận tin nhắn đến
const subscribeToUser = (username) => {
    if (!username) return;

    if (currentSubscription) {
        currentSubscription.unsubscribe();
    }

    currentSubscription = stompClient.subscribe(`/user/${username}/queue/messages`, (message) => {
        const msgObj = JSON.parse(message.body);

        // Chỉ hiển thị tin nhắn từ admin
        if (msgObj.sender === "admin") {
            showMessage(msgObj.content, 'left', msgObj.sender);
        }
    });
};


// Cập nhật người nhận khi chọn từ dropdown
const updateRecipient = () => {
    const selectedUser = document.getElementById("recipientSelect").value;
    document.getElementById("recipient").value = selectedUser;
    console.log("Người nhận được chọn:", selectedUser);  // Debug log để kiểm tra giá trị người nhận
};

// Tải danh sách người dùng và đưa vào dropdown
const loadUsers = () => {
    fetch("/api/users")
        .then(response => {
            if (!response.ok) throw new Error("Lỗi khi lấy danh sách người dùng");
            return response.json();
        })
        .then(users => {
            console.log("Users fetched:", users);  // Log dữ liệu người dùng
            const select = document.getElementById("recipientSelect");
            select.innerHTML = '<option disabled selected>-- Chọn người dùng --</option>';
            users.forEach(user => {
                console.log("User:", user);  // Log từng user
                const option = document.createElement("option");
                option.value = user.email; // Kiểm tra nếu user có thuộc tính username
                option.textContent = user.email;
                select.appendChild(option);
            });
        })
        .catch(error => {
            console.error("Lỗi khi tải danh sách người dùng:", error);
        });
};
function showMessage(content, side, senderName = '') {
    const messagesContainer = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', side);

    const now = new Date();
    const timeString = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');

    let displaySender = '';
    if (senderName) {
        displaySender = `<div class="message-sender">${senderName}</div>`;
    }

    messageDiv.innerHTML = `
        ${displaySender}
        <div class="message-content">
            <div class="message-text"><p>${content}</p></div>
            <div class="message-time">${timeString}</div>
        </div>
    `;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}
const sendMessage = () => {
    const messageInput = document.getElementById("messageInput");
    const recipient = document.getElementById("recipient").value;  // Đây sẽ là email của người nhận
    const message = messageInput.value.trim();

    if (!message || !recipient) {
        console.log("Không có tin nhắn hoặc người nhận không hợp lệ.");
        return;
    }

    const messageObj = {
        sender: sender,  // sender là email
        recipient: recipient,  // recipient là email
        content: message
    };

    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat", {}, JSON.stringify(messageObj));
        showMessage(messageObj.content, 'right', "Bạn"); // hiện phía phải
    } else {
        console.log("WebSocket chưa kết nối.");
    }

    // Hiển thị tin nhắn đã gửi trên giao diện người dùng
    const chatMessages = getOrCreateChatWindow(recipient); // Tạo hoặc lấy khung chat của recipient
    const sentMsg = document.createElement('div');
    sentMsg.innerHTML = `<strong>${messageObj.sender}:</strong> ${messageObj.content}`;
    chatMessages.appendChild(sentMsg);

    // Xóa ô nhập sau khi gửi
    messageInput.value = '';
};
const getOrCreateChatWindow = (userEmail) => {
    const chatWindowsContainer = document.getElementById("chatWindows");
    let chatWindow = document.getElementById(`chat-${userEmail.replace(/[@.]/g, "_")}`);

    if (!chatWindow) {
        // Tạo cửa sổ chat mới
        chatWindow = document.createElement("div");
        chatWindow.id = `chat-${userEmail.replace(/[@.]/g, "_")}`;
        chatWindow.className = "chat-window";
        chatWindow.style.border = "1px solid #ccc";
        chatWindow.style.padding = "10px";
        chatWindow.style.width = "250px";
        chatWindow.style.height = "300px";
        chatWindow.style.overflowY = "auto";
        chatWindow.innerHTML = `<h4>${userEmail}</h4><div class="chat-messages"></div>`;
        chatWindowsContainer.appendChild(chatWindow);
    }

    return chatWindow.querySelector(".chat-messages");
};
function toggleChat() {
    const chatBody = document.getElementById("chatBody");
    chatBody.style.display = chatBody.style.display === "none" ? "block" : "none";
}
// Khởi tạo khi load trang
const initializeChat = () => {
    loadUsers();
    connect();

    // Gắn sự kiện click cho nút "Gửi"
    const sendButton = document.getElementById('sendButton');
    sendButton.addEventListener('click', sendMessage);

    // Gắn sự kiện keypress cho ô nhập tin nhắn (gửi khi nhấn Enter)
    const messageInput = document.getElementById('messageInput');
    messageInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendMessage();
        }
    });
};

window.onload = initializeChat;
