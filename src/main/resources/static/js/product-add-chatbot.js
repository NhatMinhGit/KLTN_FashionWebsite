function addToCart(button) {
    const card = button.closest('.product-card');

    const productId = card.getAttribute("data-product-id");
    const variantId = card.getAttribute("data-variant-id");
    const priceElement = card.querySelector(".price");
    const quantityElement = card.querySelector(".quantity");
    const sizeSelect = card.querySelector(".size-select");

    if (!priceElement || !quantityElement || !sizeSelect) {
        alert("Không tìm thấy phần tử nhập dữ liệu trong card!");
        return;
    }

    let priceText = priceElement.textContent.trim().replace(/[^0-9.]/g, "");
    let price = parseFloat(priceText);
    let quantity = parseInt(quantityElement.value.trim());
    const selectedSize = sizeSelect.value;

    if (!productId || isNaN(price) || price <= 0 || isNaN(quantity) || quantity <= 0) {
        alert("Vui lòng nhập giá và số lượng hợp lệ!");
        return;
    }

    const data = {
        productId: productId,
        variantId: variantId,
        price: price,
        quantity: quantity,
        size: selectedSize
    };

    console.log("Dữ liệu gửi đi:", data);

    fetch("/user/cart/add-to-cart", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert(`Sản phẩm đã được thêm vào giỏ hàng!\nID: ${data.productId}\nGiá: ${data.price}\nSố lượng: ${data.quantity}`);
                window.location.reload();
            } else {
                alert("Có lỗi xảy ra: " + result.message);
            }
        })
        .catch(error => {
            console.error("Lỗi:", error);
            alert("Có lỗi trong quá trình xử lý.");
        });
}

function selectColor(element) {
    const card = element.closest('.product-card');

    card.querySelectorAll('.color-thumbnail').forEach(img => {
        img.classList.remove('border-primary');
        img.classList.add('border');
    });

    element.classList.add('border-primary');
    element.classList.remove('border');

    const selectedColor = element.getAttribute('data-color');
    card.querySelector('.selected-color').value = selectedColor;

    updateSizeOptions(card, selectedColor);
    updateProductImages(card, selectedColor);
}
async function updateSizeOptions(card, selectedColor) {
    const productId = card.getAttribute("data-product-id");
    const sizeSelect = card.querySelector('.size-select');

    try {
        const response = await fetch(`/api/sizes/${productId}/${selectedColor}`);
        const sizes = await response.json();

        if (sizes.length === 0) {
            sizeSelect.innerHTML = '<option disabled selected>Không có size</option>';
            return;
        }

        sizeSelect.innerHTML = sizes.map(size =>
            `<option value="${size.sizeValue}">${size.sizeValue} (Stock: ${size.stockQuantity})</option>`
        ).join("");
    } catch (error) {
        console.error("Lỗi khi lấy size:", error);
        sizeSelect.innerHTML = '<option disabled selected>Lỗi khi tải size</option>';
    }
}
