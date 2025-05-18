// function changeImage(imgElement) {
//     document.getElementById('mainImage').src = imgElement.src;
//     // Xóa class 'active' khỏi tất cả các ảnh nhỏ
//     document.querySelectorAll('.thumbnail').forEach(thumb => thumb.classList.remove('active'));
//     // Thêm class 'active' vào ảnh được chọn
//     imgElement.classList.add('active');
// }
// function changeImage(imgElement) {
//     const mainImage = document.getElementById('mainImage');
//
//     // Thêm lớp fade-out để làm mờ ảnh
//     mainImage.classList.add('fade-out');
//
//     // Sau 300ms, đổi src và xóa fade-out (hiện ảnh mới)
//     setTimeout(() => {
//         mainImage.src = imgElement.src;
//         mainImage.classList.remove('fade-out');
//     }, 300);
//
//     // Xử lý active thumbnail
//     document.querySelectorAll('.thumbnail').forEach(thumb => thumb.classList.remove('active'));
//     imgElement.classList.add('active');
// }
// function changeImage(thumbnail) {
//     mainImage.src = thumbnail.src;
// }
function changeImage(imgElement) {
    const mainImage = document.getElementById('mainImage');

    // Thêm hiệu ứng mờ
    mainImage.classList.add('fade-out');

    // Đợi hiệu ứng hoàn thành rồi thay đổi ảnh và xóa hiệu ứng
    setTimeout(() => {
        mainImage.src = imgElement.src;
        mainImage.classList.remove('fade-out');
    }, 300);

    // Xóa class 'active' khỏi tất cả các thumbnail
    document.querySelectorAll('.thumbnail').forEach(thumb => thumb.classList.remove('active'));

    // Thêm class 'active' vào thumbnail được click
    imgElement.classList.add('active');
}
document.addEventListener('DOMContentLoaded', () => {
    const selectedColorInput = document.getElementById('selectedColor');
    if (selectedColorInput && selectedColorInput.value) {
        const selectedColorImg = document.querySelector(`.color-thumbnail[data-color="${selectedColorInput.value}"]`);
        if (selectedColorImg) {
            selectColor(selectedColorImg);
        }
    }
});
// add-to-cart
function addToCart() {
    const productElement = document.getElementById("product-details");
    const priceElement = document.getElementById("price");
    const quantityElement = document.getElementById("quantity");
    const sizeSelect = document.getElementById("selectedSize");
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
let variantSizeQuantities = {};
let currentVariantId = null;

async function updateSizeOptions(selectedColor) {
    try {
        const response = await fetch(`/api/sizes/${productId}/${selectedColor}`);
        console.log("Fetch sizes with productId:", productId, "color:", selectedColor);

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const sizes = await response.json();
        if (!sizes.length) {
            console.warn("Không có size nào với màu này");
            return;
        }

        // Reset
        variantSizeQuantities = {};
        const sizeStockMap = {};

        sizes.forEach(size => {
            // Gộp số lượng tồn kho theo size để hiển thị dễ hơn
            sizeStockMap[size.sizeValue] = size.stockQuantity;

            // Lưu chi tiết theo variantId
            if (!variantSizeQuantities[size.variantId]) {
                variantSizeQuantities[size.variantId] = {};
            }
            variantSizeQuantities[size.variantId][size.sizeValue] = size.stockQuantity;
        });

        currentVariantId = sizes[0].variantId;

        // Cập nhật số lượng lên các nút
        const buttons = document.querySelectorAll('#sizeButtons button');
        buttons.forEach(button => {
            const size = button.dataset.size;
            const stock = sizeStockMap[size];
            console.log(`Button size=${size}, stock=${stock}`);  // thêm log
            if (stock != null) {
                button.innerText = `${size} (${stock > 0 ? `Còn ${stock}` : 'Hết hàng'})`;
                button.disabled = stock <= 0;
            } else {
                button.innerText = size + ' (Không có)';
                button.disabled = true;
            }
        });

    } catch (error) {
        console.error("Lỗi khi lấy size:", error);
    }
}


function selectSize(size) {
    document.getElementById('selectedSize').value = size;

    const buttons = document.querySelectorAll('.btn-group .btn');
    buttons.forEach(btn => btn.classList.remove('active'));
    const clickedButton = Array.from(buttons).find(btn => btn.textContent.trim() === size);
    if (clickedButton) clickedButton.classList.add('active');

    // Tìm lại variantId tương ứng
    const selectedVariant = Object.entries(variantSizeQuantities).find(([variantId, sizes]) => sizes[size]);
    if (selectedVariant) {
        currentVariantId = selectedVariant[0]; // Cập nhật lại variantId
        console.log("Variant ID cập nhật:", currentVariantId);

        // Cập nhật data-variant-id trong DOM để addToCart lấy đúng
        const productElement = document.getElementById("product-details");
        if (productElement) {
            productElement.setAttribute("data-variant-id", currentVariantId);
        }
    }

    getStockQuantity(currentVariantId, size).then(stockQuantity => {
        const stockMessageElement = document.getElementById('stockMessage');
        if (stockQuantity < 10) {
            stockMessageElement.style.display = 'block';
            stockMessageElement.textContent = `Chỉ còn ${stockQuantity} sản phẩm!`;
        } else {
            stockMessageElement.style.display = 'none';
        }
    });
}

function selectColor(element) {
    // Bỏ viền cũ
    document.querySelectorAll('.color-thumbnail').forEach(img => {
        img.classList.remove('border-primary');
        img.classList.add('border');
    });

    // Thêm viền cho ảnh được chọn
    element.classList.add('border-primary');
    element.classList.remove('border');

    const selectedColor = element.getAttribute('data-color');

    // Cập nhật input hidden
    document.getElementById('selectedColor').value = selectedColor;

    Promise.all([
        updateSizeOptions(selectedColor),
        updateProductImages(selectedColor)
    ]).then(() => {
        console.log("Đã cập nhật size và ảnh");
    });
}
const productId = document.getElementById("productId").value;
const sizeSelect = document.getElementById("sizeSelect");
const mainImage = document.getElementById("mainImage");
const thumbnailContainer = document.getElementById("thumbnailContainer");



async function updateProductImages(selectedColor) {
    try {
        const productId = document.getElementById('productId').value;
        const mainImage = document.getElementById('mainImage');
        const thumbnailContainer = document.querySelector('.thumbnail-carousel');

        const response = await fetch(`/api/images/${productId}/${selectedColor}`);
        const images = await response.json();

        if (!images || images.length === 0) {
            mainImage.src = "";
            mainImage.alt = "Không có ảnh cho màu này.";
            thumbnailContainer.innerHTML = "<p>Không có ảnh cho màu đã chọn.</p>";
            return;
        }

        mainImage.src = images[0];
        mainImage.alt = "Ảnh sản phẩm";

        if ($(thumbnailContainer).hasClass('slick-initialized')) {
            $(thumbnailContainer).slick('unslick');
        }

        thumbnailContainer.innerHTML = images.map(imageUri =>
            `<img src="${imageUri}" class="thumbnail" onclick="changeImage(this)" />`
        ).join("");

        $(thumbnailContainer).slick({
            centerMode: true,
            centerPadding: '40px',
            slidesToShow: 2,
            arrows: true,
            focusOnSelect: true
        });

        // Tự động highlight ảnh đầu tiên (slick sẽ làm tự nhiên, bạn không cần thêm class)
    } catch (error) {
        console.error("Lỗi khi lấy ảnh:", error);
        const mainImage = document.getElementById('mainImage');
        const thumbnailContainer = document.querySelector('.thumbnail-carousel');
        mainImage.src = "";
        mainImage.alt = "Lỗi khi tải ảnh";
        thumbnailContainer.innerHTML = "<p>Lỗi khi tải ảnh</p>";
    }
}

function changeImage(thumbnail) {
    const mainImage = document.getElementById('mainImage');
    mainImage.src = thumbnail.src;
}



// Assuming you have a function or way to fetch stock data for the selected size
async function getStockQuantity(variantId, size) {
    // Lấy số lượng tồn kho từ variantSizeQuantities cho variantId và size
    const sizeQuantities = variantSizeQuantities[variantId];
    if (!sizeQuantities) {
        console.error(`Không tìm thấy variant với ID ${variantId}`);
        return 0; // Nếu không tìm thấy variantId thì trả về 0
    }

    // Lấy số lượng tồn kho của size
    const stockQuantity = sizeQuantities[size] || 0; // Nếu không có size trong variant thì trả về 0
    return stockQuantity;
}



// async function updateSizeOptions(selectedColor) {
//     sizeSelect.innerHTML = '<option disabled selected>Đang tải...</option>';
//     try {
//         const response = await fetch(`/api/sizes/${productId}/${selectedColor}`);
//         if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
//
//         const sizes = await response.json();
//
//         if (sizes.length === 0) {
//             sizeSelect.innerHTML = '<option disabled selected>Không có size</option>';
//             return;
//         }
//
//         sizeSelect.innerHTML = sizes.map(size =>
//             `<option value="${size.sizeValue}">${size.sizeValue} (Stock: ${size.stockQuantity})</option>`
//         ).join("");
//     } catch (error) {
//         console.error("Lỗi khi lấy size:", error);
//         sizeSelect.innerHTML = '<option disabled selected>Lỗi khi tải size</option>';
//     }
// }
// function selectSize(size) {
//     // Update selected size input
//     document.getElementById('selectedSize').value = size;
//
//     // Update the active size button
//     const buttons = document.querySelectorAll('.btn-group .btn');
//     buttons.forEach(btn => btn.classList.remove('active'));
//     const clickedButton = Array.from(buttons).find(btn => btn.textContent.trim() === size);
//     clickedButton.classList.add('active');
//
//     // Fetch and display the stock quantity for the selected size
//     getStockQuantity(size).then(stockQuantity => {
//         const stockMessageElement = document.getElementById('stockMessage');
//
//         if (stockQuantity < 10) {
//             stockMessageElement.style.display = 'block';
//             stockMessageElement.textContent = `Chỉ còn ${stockQuantity} sản phẩm!`;
//         } else {
//             stockMessageElement.style.display = 'none'; // Hide the message if stock is 10 or more
//         }
//     });
// }

function changeQuantity(change) {
    const quantityInput = document.getElementById('quantity');
    let value = parseInt(quantityInput.value) + change;
    if (value < 1) value = 1;
    quantityInput.value = value;
}
document.addEventListener('DOMContentLoaded', function () {
    var sizeGuideButton = document.querySelector('[data-bs-toggle="modal"]');
    if (sizeGuideButton) {
        sizeGuideButton.addEventListener('click', function () {
            var modal = new bootstrap.Modal(document.getElementById('sizeGuideModal'));
            modal.show();
        });
    }
});


var myModal = new bootstrap.Modal(document.getElementById('sizeGuideModal'));
myModal.hide();

// Function to select body type when an image is clicked
function selectBodyType(bodyType) {
    const bodyTypeRadios = document.getElementsByName('bodyType');
    bodyTypeRadios.forEach(radio => {
        if (radio.value === bodyType) {
            radio.checked = true;
        }
    });
}

// Function to select fit preference when an image is clicked
function selectFitPreference(fitPreference) {
    const fitPreferenceRadios = document.getElementsByName('fitPreference');
    fitPreferenceRadios.forEach(radio => {
        if (radio.value === fitPreference) {
            radio.checked = true;
        }
    });
}

// Function to calculate and display size (you can define this function as needed)
function calculateAndDisplaySize() {
    const weight = document.getElementById('weight').value;
    const height = document.getElementById('height').value;
    const gender = document.getElementById('gender').value;
    const age = document.getElementById('age').value;
    const bodyType = document.querySelector('input[name="bodyType"]:checked').value;
    const fitPreference = document.querySelector('input[name="fitPreference"]:checked').value;

    // Perform size calculation logic here and display the result
    const sizeRecommendation = `Size Recommendation: Weight: ${weight}kg, Height: ${height}cm, Gender: ${gender}, Age: ${age}, Body Type: ${bodyType}, Fit Preference: ${fitPreference}`;

    document.getElementById('sizeRecommendation').innerText = sizeRecommendation;
}
$(document).ready(function(){
    $('.thumbnail-carousel').slick({
        centerMode: true,
        centerPadding: '40px',
        slidesToShow: 2,
        arrows: true,
        focusOnSelect: true
    });
});
document.addEventListener("DOMContentLoaded", function () {
    const productId = document.getElementById("productId").value;
    const cookieName = "viewedProducts";
    const maxAge = 60 * 60 * 24; // 1 ngày

    function getCookie(name) {
        const cookieArr = document.cookie.split(";");
        for (let cookie of cookieArr) {
            let [key, val] = cookie.trim().split("=");
            if (key === name) return decodeURIComponent(val);
        }
        return null;
    }

    function setCookie(name, value, maxAgeSeconds) {
        document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAgeSeconds}`;
    }

    let viewed = getCookie(cookieName);
    let viewedList = viewed ? JSON.parse(viewed) : [];

    if (!viewedList.includes(productId)) {
        viewedList.push(productId);
        setCookie(cookieName, JSON.stringify(viewedList), maxAge);
    }
});