document.addEventListener("DOMContentLoaded", () => {
    document.querySelector("#search-form").onsubmit = function (event) {
        event.preventDefault();
        searchProducts(event);
    };
});


function searchProducts(event) {
    event.preventDefault();  // Ngừng hành động mặc định (không reload trang)

    let keyword = document.getElementById("search-input").value.trim();
    if (!keyword) {
        alert("Vui lòng nhập từ khóa tìm kiếm.");
        return;
    }
    fetch(`/user/search?keyword=${encodeURIComponent(keyword)}`)
        .then(response => {
            if (!response.ok) throw new Error("Lỗi kết nối server");
            return response.json(); // hoặc dùng response.text().then(console.log)
        })
        .then(products => {
            let container = document.getElementById("productCardContainer");
            container.innerHTML = "";

            // Kiểm tra nếu có sản phẩm
            if (products && products.length > 0) {
                products.forEach(product => {
                    const productCard = `
                    <div class="col">
                        <div class="product-card">
                            <div class="product-images">
                                <a href="/product-detail/${product.id}">
                                    <img src="${(product.imageUrls && product.imageUrls.length > 0)
                                            ? product.imageUrls[0]
                                            : 'https://res.cloudinary.com/dgtfqxgvx/image/upload/v1744854407/default.png'}" 
                                         alt="${product.name}" class="product-image"/>
                                </a>
                            </div>
                            <h5>${product.name}</h5>
                            <p class="product-price">${product.price}</p>
                            <p>${product.description || ""}</p>
                        </div>
                    </div>`;
                    container.innerHTML += productCard;
                });

            } else {
                // Nếu không có sản phẩm, có thể hiển thị thông báo
                container.innerHTML = "<p>Không tìm thấy sản phẩm nào.</p>";
            }
        })
        .catch(error => {
            console.error("Lỗi khi tìm kiếm:", error);
            // Có thể hiển thị thông báo lỗi cho người dùng
        });



}
