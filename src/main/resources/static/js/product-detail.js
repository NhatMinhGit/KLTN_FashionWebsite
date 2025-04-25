function changeImage(imgElement) {
    document.getElementById('mainImage').src = imgElement.src;
    // Xóa class 'active' khỏi tất cả các ảnh nhỏ
    document.querySelectorAll('.thumbnail').forEach(thumb => thumb.classList.remove('active'));
    // Thêm class 'active' vào ảnh được chọn
    imgElement.classList.add('active');
}

// add-to-cart
function addToCart() {
    const productElement = document.getElementById("product-details");
    const priceElement = document.getElementById("price");
    const quantityElement = document.getElementById("quantity");
    const sizeSelect = document.getElementById("sizeSelect");
    const selectedSize = sizeSelect.value; // Hoặc .options[sizeSelect.selectedIndex].text nếu muốn cả tên + tồn kho

    if (!productElement || !priceElement || !quantityElement || !sizeSelect) {
        alert("Không tìm thấy phần tử nhập dữ liệu!");
        return;
    }

    const productId = productElement.getAttribute("data-id");
    const variantId = productElement.getAttribute("data-variant-id");

    let price = priceElement.textContent.trim(); // Lấy giá từ thẻ <span>
    let quantity = quantityElement.value.trim();

    // Chuyển đổi giá thành số và loại bỏ dấu phẩy (nếu có)
    price = parseFloat(price.replace(/,/g, ""));

    // Kiểm tra nếu price bị NaN hoặc giá trị không hợp lệ
    quantity = parseInt(quantity);

    if (!productId || isNaN(price) || price <= 0 || isNaN(quantity) || quantity <= 0) {
        alert("Vui lòng nhập giá và số lượng hợp lệ!");
        return;
    }

    const data = {
        productId: productId,
        variantId: variantId,
        price: price,
        quantity: quantity,
        size: selectedSize  // Thêm dòng này
    };

    console.log("Dữ liệu gửi đi:", data); // Kiểm tra giá trị trong console

    fetch("/user/cart/add-to-cart", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert(`Sản phẩm đã được thêm vào giỏ hàng!\nID: ${data.productId}\nGiá: ${data.price}\nSố lượng: ${data.quantity}`);
                window.location.reload(); // Tải lại trang sau khi thêm sản phẩm
            } else {
                alert("Có lỗi xảy ra: " + result.message);
            }
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Đã có lỗi trong quá trình xử lý. Vui lòng thử lại sau.");
        });
}



