document.addEventListener('DOMContentLoaded', function() {
    // DOM elements
    const sendButton = document.getElementById('send-button');
    const userInput = document.getElementById('user-input');
    const messagesContainer = document.getElementById('messages');
    const categoryItems = document.querySelectorAll('.category-item');
    const questionItems = document.querySelectorAll('.question-item');
    const quickOptionButtons = document.querySelectorAll('.quick-options .btn');

    // Add click event for send button
    sendButton.addEventListener('click', sendMessage);

    // Add keypress event for input field (Enter to send)
    userInput.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    // Add click events for category items
    categoryItems.forEach(item => {
        item.addEventListener('click', function() {
            const categoryText = this.querySelector('span').textContent.trim();
            sendCategoryQuery(categoryText);
        });
    });

    // Add click events for question items
    questionItems.forEach(item => {
        item.addEventListener('click', function() {
            const questionText = this.querySelector('.question-text').textContent.trim();
            sendCategoryQuery(questionText);
        });
    });

    // Add click events for quick option buttons
    quickOptionButtons.forEach(button => {
        button.addEventListener('click', function() {
            const optionText = this.textContent.trim();
            sendCategoryQuery(optionText);
        });
    });
    function updateTime() {
        const now = new Date();
        const hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes().toString().padStart(2, '0');
        const formattedTime = `${hours}:${minutes}`;

        document.getElementById('currentTime').textContent = formattedTime;
    }

    // Cập nhật mỗi giây
    setInterval(updateTime, 1000);
    updateTime(); // gọi 1 lần lúc đầu

    // Function to send messages
    function sendMessage() {
        const message = userInput.value.trim();
        if (!message) return;

        // Add user message to chat
        addMessage(message, 'user');
        userInput.value = '';

        // Send to API and get response
        sendToAPI(message);
    }

    // Function to handle category/question selection
    function sendCategoryQuery(query) {
        // Add user selection as a message
        addMessage(query, 'user');

        // Send to API and get response
        sendToAPI(query);
    }
    function sendToAPI(message) {
        // Show loading indicator
        const loadingId = showLoadingIndicator();

        fetch(`/admin/chat?message=${encodeURIComponent(message)}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        })

            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json(); // Chuyển dữ liệu trả về thành JSON
            })
            .then(data => {
                console.log(data); // Log the full response to check its structure

                setTimeout(() => {
                    removeLoadingIndicator(loadingId); // Xoá spinner

                    // Kiểm tra dữ liệu từ API trả về (ví dụ: aiResponse và productInfo)
                    if (data.aiResponse) {
                        addMessage(data.aiResponse, 'ai');  // Hiển thị phản hồi từ AI
                    } else {
                        console.log('No aiResponse in the data'); // Debugging message
                    }

                    if (data.productInfo) {
                        addMessage(data.productInfo, 'html'); // Hiển thị thông tin sản phẩm
                    } else {
                        console.log('No productInfo in the data'); // Debugging message
                    }

                    // Nếu có yêu cầu chọn size và số lượng, yêu cầu người dùng nhập thông tin
                    if (data.sizeAndQuantityRequired) {
                        askSizeAndQuantity(data.productId); // Gửi câu hỏi về size và số lượng
                    } else {
                        console.log('No sizeAndQuantityRequired in the data'); // Debugging message
                    }
                }, 400);
            })

            .catch(error => {
                console.error('Error:', error);
                addMessage("Lỗi kết nối API! Vui lòng thử lại.", 'ai');
            });
    }
    // Function to send message to API
    // function sendToAPI(message) {
    //     // Show loading indicator
    //     const loadingId = showLoadingIndicator();
    //
    //     fetch(`/admin/chat?message=${encodeURIComponent(message)}`, {
    //         method: 'GET',
    //         headers: { 'Content-Type': 'application/json' }
    //     })
    //
    //         .then(response => {
    //             if (!response.ok) {
    //                 throw new Error(`HTTP error! Status: ${response.status}`);
    //             }
    //             return response.text(); //Nếu backend trả về plain text
    //         })
    //         .then(data => {
    //             setTimeout(() => {
    //                 removeLoadingIndicator(loadingId); // Xoá spinner
    //                 addMessage(data, 'ai');            // Hiển thị phản hồi từ AI
    //             }, 400);
    //         })
    //         .catch(error => {
    //             console.error('Error:', error);
    //             removeLoadingIndicator(loadingId);
    //             addMessage("Lỗi kết nối API! Vui lòng thử lại.", 'ai');
    //         });
    //     //
    // }

    // Hàm này để hỏi người dùng về size, màu sắc và số lượng
    function askSizeAndQuantity(productId, variants) {
        // Lưu productId hiện tại để sau này sử dụng khi gửi dữ liệu
        currentProductId = productId;

        // Tạo câu hỏi về size, màu sắc và số lượng
        const sizeQuestion = `Vui lòng chọn size cho sản phẩm.`;
        const quantityQuestion = `Vui lòng nhập số lượng bạn muốn mua.`;
        const colorQuestion = `Vui lòng chọn màu sắc cho sản phẩm. Các lựa chọn có sẵn: ${variants.join(", ")}.`;

        // Hiển thị câu hỏi về size, màu sắc và số lượng
        addMessage(sizeQuestion, 'ai');
        addMessage(colorQuestion, 'ai');
        addMessage(quantityQuestion, 'ai');
    }
    // Function to add a message to the chat
    function addMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', sender);

        const currentTime = new Date();
        const hours = currentTime.getHours().toString().padStart(2, '0');
        const minutes = currentTime.getMinutes().toString().padStart(2, '0');
        const timeString = `${hours}:${minutes}`;

        let messageHTML = `<div class="message-content">`;

        if (sender === 'ai') {
            messageHTML += `
                <div class="message-text">
                    <div class="ai-avatar">
                        <i class="fa-solid fa-user-headset"></i>
                    </div>
                    <p>${text}</p>
                </div>`;
        } else {
            messageHTML += `<div class="message-text"><p>${text}</p></div>`;
        }

        messageHTML += `<div class="message-time">${timeString}</div></div>`;

        messageDiv.innerHTML = messageHTML;
        messagesContainer.appendChild(messageDiv);

        // Scroll to bottom of messages
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }


    // Function to show loading indicator
    function showLoadingIndicator() {
        const loadingId = 'loading-' + Date.now();
        const loadingDiv = document.createElement('div');
        loadingDiv.classList.add('message', 'ai');
        loadingDiv.id = loadingId;

        loadingDiv.innerHTML = `
            <div class="message-content">
                <div class="message-text">
                    <div class="ai-avatar">
                        <i class="fa-solid fa-user-headset"></i>
                    </div>
                    <p><i class="fa-solid fa-spinner fa-spin"></i> Mith is typing...</p>
                </div>
            </div>`;

        messagesContainer.appendChild(loadingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        return loadingId;
    }

    // Function to remove loading indicator
    function removeLoadingIndicator(loadingId) {
        const loadingElement = document.getElementById(loadingId);
        if (loadingElement) {
            loadingElement.remove();
        }
    }

    // Function to handle the "back to the latest" button
    document.querySelector('.back-to-latest .btn').addEventListener('click', function(e) {
        e.preventDefault();
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    });

    // Focus input field when page loads
    userInput.focus();
});
