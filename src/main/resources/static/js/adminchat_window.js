document.addEventListener('DOMContentLoaded', function() {
    const messagesContainer = document.getElementById('adminMessages');

    if (!window.isAdminWebSocketConnected) {
        window.isAdminWebSocketConnected = true;

        var socket = new SockJS('/ws/admin');
        var stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log('Admin connected: ' + frame);

            // Nhận tin nhắn từ user
            stompClient.subscribe('/topic/admin', function(response) {
                let receivedData = JSON.parse(response.body);
                console.log("Nhận tin nhắn từ User:", receivedData);

                if (receivedData.content && receivedData.sender) {
                    addMessage(receivedData.content, receivedData.sender);
                }
            });
        });

        function addMessage(text, sender) {
            let messageDiv = document.createElement("div");
            messageDiv.classList.add("message", sender === "admin" ? "admin" : "user");

            messageDiv.innerHTML = `
                <div class="message-content">
                    <p>${text}</p>
                    <span class="message-time">${new Date().toLocaleTimeString()}</span>
                </div>
            `;

            messagesContainer.appendChild(messageDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }
});
