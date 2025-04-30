let stompClient;
let sender;

const connect = () => {
    sender = document.getElementById("sender").value;
    if (!sender) {
        console.error("Người gửi chưa được xác định.");
        return;
    }

    const socket = new SockJS(`/ws?user=${sender}`);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("Connected as:", sender);
        subscribeToUser(sender);
    }, (error) => {
        console.error("Connection failed:", error);
    });
};

const subscribeToUser = (username) => {
    if (!username) return;

    stompClient.subscribe(`/user/${username}/queue/messages`, (message) => {
        const msgObj = JSON.parse(message.body);
        showMessage(msgObj.content, 'left', msgObj.sender);
    });
};

const sendMessage = () => {
    const messageInput = document.getElementById("messageInput");
    const recipient = document.getElementById("recipient").value; // "admin"
    const message = messageInput.value.trim();

    if (!message || !recipient) {
        console.log("Tin nhắn trống hoặc người nhận không hợp lệ.");
        return;
    }

    const messageObj = {
        sender: sender,
        recipient: recipient,
        content: message
    };

    if (stompClient && stompClient.connected) {
        stompClient.send("/app/chat", {}, JSON.stringify(messageObj));
        showMessage(message, 'right', sender);
    } else {
        console.log("WebSocket chưa kết nối.");
    }

    messageInput.value = '';
};
function toggleChat() {
    const chatBody = document.getElementById("chatBody");
    chatBody.style.display = chatBody.style.display === "none" ? "block" : "none";
}
function showMessage(content, side, senderName = '') {
    const messagesContainer = document.getElementById('messages');
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', side);

    const now = new Date();
    const timeString = now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');

    let displaySender = senderName ? `<div class="message-sender">${senderName}</div>` : '';

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

document.addEventListener("DOMContentLoaded", () => {
    connect();

    document.getElementById("sendButton").addEventListener("click", sendMessage);

    document.getElementById("messageInput").addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            sendMessage();
        }
    });
});
