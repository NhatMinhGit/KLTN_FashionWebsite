// Đảm bảo DOM đã tải xong trước khi thêm sự kiện
// Đảm bảo DOM đã tải xong trước khi thêm sự kiện
document.addEventListener("DOMContentLoaded", function() {
    // Lấy tất cả các phần tử có class 'product-item'
    const productItems = document.querySelectorAll('.product-item');

    // Kiểm tra có lấy được các phần tử hay không
    if (productItems.length === 0) {
        console.log("Không tìm thấy phần tử có class 'product-item'");
    }

    productItems.forEach(item => {
        item.addEventListener('click', function() {
            // Lấy dữ liệu từ các thuộc tính data-*
            const productId = this.dataset.id;
            const productName = this.dataset.name;
            const price = this.dataset.price;

            // Thêm thông báo vào console để kiểm tra
            console.log("Sản phẩm đã được chọn:");
            console.log("ID: " + productId);
            console.log("Tên sản phẩm: " + productName);
            console.log("Giá: " + price);

            // Gọi hàm addToCart khi click
            addToCart(productId, productName, price);
        });
    });
});
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


    // Function to send message to API
    function sendToAPI(message) {
        // Show loading indicator
        const loadingId = showLoadingIndicator();

        fetch(`/user/chat?message=${encodeURIComponent(message)}`, {
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
                setTimeout(() => {
                    removeLoadingIndicator(loadingId); // Xoá spinner

                    // Kiểm tra dữ liệu từ API trả về (ví dụ: aiResponse và productInfo)
                    if (data.aiResponse) {
                        addMessage(data.aiResponse, 'ai');  // Hiển thị phản hồi từ AI
                    }
                    if (data.productInfo) {
                        addMessage(data.productInfo, 'html'); // Hiển thị thông tin sản phẩm
                    }
                    // Nếu có yêu cầu chọn size và số lượng, yêu cầu người dùng nhập thông tin
                    if (data.sizeAndQuantityRequired) {
                        askSizeAndQuantity(data.productId); // Gửi câu hỏi về size và số lượng
                    }
                }, 400);
            })

            .catch(error => {
                console.error('Error:', error);
                addMessage("Lỗi kết nối API! Vui lòng thử lại.", 'ai');
            });
    }
    // Function to send message to API
    let currentProductId = null;  // Biến lưu trữ productId khi hỏi
    let currentProductVariant = null;  // Biến lưu trữ variant (màu sắc) khi hỏi

// Hàm này hiển thị thông tin sản phẩm và yêu cầu người dùng chọn size, số lượng, và màu sắc
    function addToCart(productId, productName, price) {
        // Hiển thị câu hỏi về size, số lượng và màu sắc
        const message = `Bạn đã chọn sản phẩm "${productName}" với giá ${price}. Vui lòng chọn size, màu sắc và số lượng.`;
        addMessage(message, 'user');

        // Gửi câu hỏi về size, màu sắc và số lượng
        sendToAPI(`Bạn đã chọn sản phẩm "${productName}" (ID: ${productId}, giá: ${price}). Vui lòng hỏi tôi về size, màu sắc và số lượng.`);
    }

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
    function showPresaleProducts() {
        const saleProducts = allProducts.filter(p => p.sale);
        const cheapest10 = saleProducts.sort((a, b) => a.price - b.price).slice(0, 10);

        const container = document.getElementById("presale-products");
        container.innerHTML = ""; // Clear previous content

        cheapest10.forEach(product => {
            const item = document.createElement("div");
            item.className = "col-md-3 mb-3";
            item.innerHTML = `
      <div class="card">
        <div class="card-body">
          <h5 class="card-title">${product.name}</h5>
          <p class="card-text text-danger">Giá: ${product.price.toLocaleString()}đ</p>
        </div>
      </div>
    `;
            container.appendChild(item);
        });
    }

    // Kích hoạt khi tab được nhấn
    document.getElementById("presale-tab").addEventListener("click", showPresaleProducts);

    // Function to handle the "back to the latest" button
    document.querySelector('.back-to-latest .btn').addEventListener('click', function(e) {
        e.preventDefault();
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    });
    // Focus input field when page loads
    userInput.focus();
});

