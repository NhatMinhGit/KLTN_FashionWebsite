// document.addEventListener("DOMContentLoaded", () => {
//     document.querySelector("#search-form").onsubmit = function (event) {
//         event.preventDefault();
//         searchProducts(event);
//     };
// });


// function searchProducts(event) {
//     event.preventDefault();
//
//     let keyword = document.getElementById("search-input").value.trim();
//     if (!keyword) {
//         alert("Vui lòng nhập từ khóa tìm kiếm.");
//         return;
//     }
//
//     fetch(`/user/shop/search?keyword=${encodeURIComponent(keyword)}`)  // FIX URL
//         .then(response => {
//             if (!response.ok) throw new Error("Lỗi kết nối server");
//             return response.json();
//         })
//         .then(products => {
//             if (products.length === 0) {  // <-- Đã sửa
//                 throw new Error("Không tìm thấy sản phẩm nào cho từ khóa: " + keyword);
//             }
//
//             let container = document.getElementById("productCardContainer");
//             container.innerHTML = "";
//
//             products.forEach(product => {
//                 const productCard = `
//         <div class="col">
//             <div class="product-card">
//                 <div class="product-images" onmouseover="startSlideshow(this)" onmouseout="stopSlideshow(this)">
//                     <a href="/user/product-detail/${product.id}">
//                         <img src="${(product.imageUrls && product.imageUrls.length > 0)
//                     ? product.imageUrls[0]
//                     : 'https://res.cloudinary.com/dgtfqxgvx/image/upload/v1744854407/default.png'}"
//                              alt="${product.name}" class="product-image"/>
//                     </a>
//
//                 </div>
//                 <h5>${product.name}</h5>
//                 <p class="product-price">${product.price}</p>
//                 <p>${product.description || ""}</p>
//             </div>
//         </div>`;
//                 container.innerHTML += productCard;
//             });
//         })
//         .catch(error => {
//             console.error("Lỗi khi tìm kiếm:", error);
//             let container = document.getElementById("productCardContainer");
//             container.innerHTML = `<p>${error.message}</p>`;
//         });
// }
// function searchProducts(event) {
//     event.preventDefault();
//
//     let keyword = document.getElementById("search-input").value.trim();
//     if (!keyword) {
//         alert("Vui lòng nhập từ khóa tìm kiếm.");
//         return;
//     }
//
//     fetch(`/user/shop/search?keyword=${encodeURIComponent(keyword)}`)  // FIX URL
//         .then(response => {
//             if (!response.ok) throw new Error("Lỗi kết nối server");
//             return response.json();
//         })
//         .then(products => {
//             if (products.length === 0) {  // <-- Đã sửa
//                 throw new Error("Không tìm thấy sản phẩm nào cho từ khóa: " + keyword);
//             }
//
//             let container = document.getElementById("productCardContainer");
//             container.innerHTML = "";
//
//             products.forEach(product => {
//                 const productCard = `
//         <div class="col">
//             <div class="product-card">
//                 <div class="product-images" onmouseover="startSlideshow(this)" onmouseout="stopSlideshow(this)">
//                     <a href="/user/product-detail/${product.id}">
//                         <div th:each="imageUrl, iterStat : ${product.imageUrls}">
//                             <!-- Hiển thị tối đa 3 ảnh -->
//                             <div th:if="${iterStat.index < 3}">
//                                 <img th:src="${imageUrl}"
//                                      alt="${product.name}" className="product-image"/>
//                             </div>
//                         </div>
//                     </a>
//
//                 </div>
//                 <h5>${product.name}</h5>
//                 <p class="product-price">${product.price}</p>
//                 <p>${product.description || ""}</p>
//             </div>
//         </div>`;
//                 container.innerHTML += productCard;
//             });
//         })
//         .catch(error => {
//             console.error("Lỗi khi tìm kiếm:", error);
//             let container = document.getElementById("productCardContainer");
//             container.innerHTML = `<p>${error.message}</p>`;
//         });
// }
