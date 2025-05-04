function calculateAndDisplaySize() {
    const weight = parseFloat(document.getElementById('weight').value);
    const height = parseFloat(document.getElementById('height').value);
    const gender = document.getElementById('gender').value;
    const age = parseInt(document.getElementById('age').value);
    const bodyType = document.querySelector('input[name="bodyType"]:checked').value;
    const fitPreference = document.querySelector('input[name="fitPreference"]:checked').value;
    const resultDiv = document.getElementById('sizeRecommendation');
    let recommendedSize = "Không xác định được"; // Default value

    // Basic size logic (needs refinement and brand-specific data)
    if (!isNaN(weight) && !isNaN(height)) {
        let bmi = weight / ((height / 100) * (height / 100));

        if (gender === 'Nam') {
            if (bmi < 18.5) {
                recommendedSize = 'S';
            } else if (bmi < 25) {
                recommendedSize = 'M';
            } else if (bmi < 30) {
                recommendedSize = 'L';
            } else {
                recommendedSize = 'XL';
            }

            if (age > 40) {
                if (recommendedSize === 'S') recommendedSize = 'M';
                else if (recommendedSize === 'M') recommendedSize = 'L';
                else if (recommendedSize === 'L') recommendedSize = 'XL';
                else if (recommendedSize === 'XL') recommendedSize = 'XXL';
            }

            if (bodyType === 'Thon gon') {
                if (recommendedSize === 'L') recommendedSize = 'M';
                else if (recommendedSize === 'XL') recommendedSize = 'L';
                else if (recommendedSize === 'XXL') recommendedSize = 'XL';
            } else if (bodyType === 'Đầy đặn') {
                if (recommendedSize === 'S') recommendedSize = 'M';
                else if (recommendedSize === 'M') recommendedSize = 'L';
                else if (recommendedSize === 'L') recommendedSize = 'XL';
                else if (recommendedSize === 'XL') recommendedSize = 'XXL';
            }

            if (fitPreference === 'Mặc ôm') {
                if (recommendedSize === 'M') recommendedSize = 'S';
                else if (recommendedSize === 'L') recommendedSize = 'M';
                else if (recommendedSize === 'XL') recommendedSize = 'L';
                else if (recommendedSize === 'XXL') recommendedSize = 'XL';
            } else if (fitPreference === 'Mặc rộng') {
                if (recommendedSize === 'S') recommendedSize = 'M';
                else if (recommendedSize === 'M') recommendedSize = 'L';
                else if (recommendedSize === 'L') recommendedSize = 'XL';
                else if (recommendedSize === 'XL') recommendedSize = 'XXL';
            }
        } else if (gender === 'Nữ') {
            // Add similar logic for females, potentially with different BMI ranges and considerations
            if (bmi < 18.5) {
                recommendedSize = 'XS';
            } else if (bmi < 24) {
                recommendedSize = 'S';
            } else if (bmi < 29) {
                recommendedSize = 'M';
            } else {
                recommendedSize = 'L';
            }

            // ... add adjustments for age, body type, and fit preference for females
        }

        resultDiv.textContent = 'Size phù hợp với bạn: ' + recommendedSize;
    } else {
        resultDiv.textContent = 'Vui lòng nhập chiều cao và cân nặng.';
    }
}