function uploadExcel() {
    const files = document.getElementById('excelFile').files;
    const allData = {}; // Tập hợp toàn bộ dữ liệu từ nhiều file

    let filesProcessed = 0;

    Array.from(files).forEach(file => {
        const reader = new FileReader();

        reader.onload = function (e) {
            const data = new Uint8Array(e.target.result);
            const workbook = XLSX.read(data, { type: 'array' });

            workbook.SheetNames.forEach(sheetName => {
                const sheet = workbook.Sheets[sheetName];
                const json = XLSX.utils.sheet_to_json(sheet);
                console.log(`Sheet: ${sheetName}`, json);  // In dữ liệu để kiểm tra

                // Nếu sheet đã tồn tại, nối thêm dữ liệu
                if (allData[sheetName]) {
                    allData[sheetName] = allData[sheetName].concat(json);
                } else {
                    allData[sheetName] = json;
                }
            });

            filesProcessed++;

            // Khi đã xử lý hết tất cả các file
            if (filesProcessed === files.length) {
                // Gửi dữ liệu lên backend
                fetch('/admin/datatree/import', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(allData)
                })
                    .then(res => res.text())
                    .then(msg => alert("Kết quả: " + msg));
            }
        };

        reader.readAsArrayBuffer(file);
    });
}