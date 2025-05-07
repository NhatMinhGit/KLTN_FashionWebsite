function updateQuantity(cartItemId, change) {
    console.log("Updating quantity: ", cartItemId, change);
    fetch('/user/cart/update-quantity', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `id=${cartItemId}&change=${change}`
    })
        .then(response => {
            if (!response.ok) throw new Error('Update failed');
            return response.json(); // trả JSON thay vì text
        })
        .then(data => {
            console.log(data);
            const input = document.querySelector(`input[data-cart-id='${cartItemId}']`);
            if (input) {
                input.value = data.newQuantity;  // set giá trị mới trả về
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("Không thể cập nhật số lượng.");
        });
}