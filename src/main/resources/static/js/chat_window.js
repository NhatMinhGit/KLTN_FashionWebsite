document.addEventListener('DOMContentLoaded', function() {
    const sendButton = document.getElementById('sendButton');
    const messageInput = document.getElementById('messageInput');
    const messagesContainer = document.getElementById('messages');
    const adminMessagesContainer = document.getElementById('adminMessages');

    let stompClientUser, stompClientAdmin;
    let isUserConnected = false;
    let isAdminConnected = false;

    // Kết nối WebSocket người dùng
    function connectUserWebSocket() {
        if (!isUserConnected) {
            const socketUser = new SockJS('/ws/user');
            stompClientUser = Stomp.over(socketUser);
            stompClientUser.connect({}, function(frame) {
                console.log('User connected: ' + frame);
                isUserConnected = true;

                // Nhận tin nhắn từ admin
                stompClientUser.subscribe('/topic/messages', function(message) {
                    let receivedData = JSON.parse(message.body);
                    addMessage(receivedData.content, receivedData.sender, 'admin');
                });
            }, function(error) {
                console.error('Error in user connection: ', error);
                // Thử kết nối lại sau khi 5 giây nếu kết nối bị lỗi
                setTimeout(connectUserWebSocket, 5000);
            });
        }
    }


    // Kết nối WebSocket admin
    function connectAdminWebSocket() {
        if (!isAdminConnected) {
            const socketAdmin = new SockJS('/ws/admin');
            stompClientAdmin = Stomp.over(socketAdmin);
            stompClientAdmin.connect({}, function(frame) {
                console.log('Admin connected: ' + frame);
                isAdminConnected = true;

                // Nhận tin nhắn từ user
                stompClientAdmin.subscribe('/topic/admin', function(response) {
                    let receivedData = JSON.parse(response.body);
                    console.log("Received message from user:", receivedData);

                    if (receivedData.content && receivedData.sender) {
                        addMessage(receivedData.content, receivedData.sender, 'user');
                    }
                });
            }, function(error) {
                console.error('Error in admin connection: ', error);
            });
        }
    }

    // Kết nối cả user và admin
    connectUserWebSocket();
    connectAdminWebSocket();

    // Xử lý sự kiện gửi tin nhắn
    sendButton.addEventListener('click', function() {
        sendMessage();
    });

    messageInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    // Gửi tin nhắn
    function sendMessage() {
        const message = messageInput.value.trim();
        if (!message || !isUserConnected) return;

        // Gửi tin nhắn đến WebSocket
        var jsonMessage = JSON.stringify({ sender: "user", content: message });
        stompClientUser.send("/app/chat.sendMessageUser", {}, jsonMessage);

        addMessage(message, 'user', 'user');
        messageInput.value = '';
    }

    // Hàm thêm tin nhắn vào container
    function addMessage(text, sender, type) {
        let messageDiv = document.createElement("div");
        messageDiv.classList.add("message", type === "user" ? "user" : "admin");

        messageDiv.innerHTML = `
            <div class="message-content">
                <p>${text}</p>
                <span class="message-time">${new Date().toLocaleTimeString()}</span>
            </div>
        `;

        if (type === 'user') {
            messagesContainer.appendChild(messageDiv);
        } else {
            adminMessagesContainer.appendChild(messageDiv);
        }

        // Tự động cuộn xuống cuối tin nhắn
        messageDiv.scrollIntoView({ behavior: 'smooth' });
    }

    // Focus vào ô nhập tin nhắn khi trang tải xong
    messageInput.focus();
});
