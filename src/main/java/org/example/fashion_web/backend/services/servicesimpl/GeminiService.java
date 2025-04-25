package org.example.fashion_web.backend.services.servicesimpl;

import org.example.fashion_web.backend.configurations.GeminiConfig;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiService {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserChatbotRepository userChatbotRepository;

    @Autowired
    private ChatbotRepository chatbotRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    private final WebClient webClient = WebClient.builder().build();


    public String chatWithAI(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").trim();

        // Truy vấn sản phẩm liên quan đến câu hỏi
        List<Product> relatedProducts = productRepository.findByNameContaining(normalizedMessage);


        String productInfo = relatedProducts.isEmpty()
                ? "Em chưa tìm thấy sản phẩm chính xác, nhưng anh/chị có thể tham khảo thêm các mẫu tương tự không ạ?"
                : relatedProducts.stream()
                .map(p -> "Tên: " + p.getName() + ", Giá: " + p.getPrice() + " VNĐ, Mô tả: " + p.getDescription())
                .collect(Collectors.joining("\n"));

        // Tạo câu hỏi gửi đến AI cùng với dữ liệu từ database
        String prompt = "Người dùng hỏi: '" + message + "'.\n\nDữ liệu từ hệ thống:\n" + productInfo + "\n\nHãy phản hồi thông minh dựa trên thông tin có sẵn.";

        String url = GEMINI_API_URL + "?key=" + geminiConfig.getApiKey();

        // Tạo JSON yêu cầu
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Phản hồi từ Gemini API (raw): " + response);
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("candidates")) {
                return jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
            } else {
                return "API không trả về phản hồi hợp lệ!";
            }

        } catch (WebClientResponseException e) {
            System.err.println("Lỗi từ Gemini API: " + e.getResponseBodyAsString());
            return "Lỗi từ Gemini API: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            System.err.println("Lỗi hệ thống: " + e.getMessage());
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
    public String getGeminiResponse(String message) {
        String response;
        if (message.toLowerCase().contains("sản phẩm")) {
            response = searchProducts(message);
        } else if (message.toLowerCase().contains("tồn kho")) {
            response = checkStock(message);
        } else {
            response = "Xin lỗi, tôi chưa hiểu câu hỏi. Bạn có thể hỏi về sản phẩm hoặc tồn kho.";
        }
        return response;
    }

    public String searchProducts(String message) {
        List<Product> products = productRepository.findByNameContaining(message);
        return products.isEmpty() ? "Không tìm thấy sản phẩm phù hợp." : products.stream().map(Product::getName).collect(Collectors.joining(", "));
    }

    public String checkStock(String message) {
        // Chuẩn hóa câu hỏi để lấy đúng tên sản phẩm
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").trim();

        // Kiểm tra sản phẩm trong database
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> normalizedMessage.toLowerCase().contains(p.getName().toLowerCase()))
                .collect(Collectors.toList());

        // Kiểm tra sản phẩm trong database
        Optional<Image> relatedProductImage = imageRepository.findAll()
                .stream()
                .filter(i -> normalizedMessage.toLowerCase().contains(i.getProductVariant().getProduct().getName().toLowerCase()))
                .findFirst();

        // Định dạng giá tiền
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");

        // Tạo nội dung phản hồi
        Product firstProduct = relatedProducts.get(0); // lấy sản phẩm đầu tiên
        String productName = firstProduct.getName();
        String productPrice = firstProduct.getPrice() != null ? new DecimalFormat("#,### VNĐ").format(firstProduct.getPrice()) : "Chưa có giá";
        String imageUrl = relatedProductImage.map(Image::getImageUri).orElse("Không có hình ảnh");

        // Tạo phản hồi trả về
        return "Dạ, em tìm thấy sản phẩm:<br>" +
                "<b>Tên:</b> " + productName + "<br>" +
                "<b>Giá:</b> " + productPrice + "<br>" +
                "<img src='" + imageUrl + "' width='200' />";
    }
    public String checkMonthlyRenvenue(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();
        int yearToCheck = now.getYear();

        // Regex tìm "tháng x" và "năm xxxx"
        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");
        Pattern yearPattern = Pattern.compile("năm\\s*(\\d{4})");

        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);
        Matcher yearMatcher = yearPattern.matcher(normalizedMessage);

        // Nếu người dùng nhập tháng cụ thể
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        // Nếu người dùng nhập năm cụ thể
        if (yearMatcher.find()) {
            yearToCheck = Integer.parseInt(yearMatcher.group(1));
        }

        // Lấy doanh thu từ DB
        BigDecimal data = orderRepository.getMonthlyRevenue(yearToCheck, monthToCheck);

        if (data == null) data = BigDecimal.ZERO;

        // Trả về kết quả
        return "Dạ, doanh thu tháng " + monthToCheck + "/" + yearToCheck + " là " + data + " VNĐ";
    }

    public String checkYearlyRenvenue(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int yearToCheck = now.getYear();

        // Regex tìm "năm xxxx"
        Pattern yearPattern = Pattern.compile("năm\\s*(\\d{4})");
        Matcher yearMatcher = yearPattern.matcher(normalizedMessage);


        // Nếu người dùng nhập năm cụ thể
        if (yearMatcher.find()) {
            yearToCheck = Integer.parseInt(yearMatcher.group(1));
        }

        // Lấy doanh thu từ DB
        BigDecimal data = orderRepository.getYearlyRevenue(yearToCheck);

        if (data == null) data = BigDecimal.ZERO;

        // Trả về kết quả
        return "Dạ, doanh thu tổng của cả năm " + yearToCheck + " là " + data + " VNĐ";
    }
    public String checkTopProductsRenvenue(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();
        int yearToCheck = now.getYear();

        // Regex tìm "tháng x" và "năm xxxx"
        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");
        Pattern yearPattern = Pattern.compile("năm\\s*(\\d{4})");

        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);
        Matcher yearMatcher = yearPattern.matcher(normalizedMessage);

        // Nếu người dùng nhập tháng cụ thể
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        // Nếu người dùng nhập năm cụ thể
        if (yearMatcher.find()) {
            yearToCheck = Integer.parseInt(yearMatcher.group(1));
        }

        // Tính khoảng thời gian đầu và cuối tháng
        LocalDate monthStart = LocalDate.of(yearToCheck, monthToCheck, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // Lấy danh sách top sản phẩm từ DB
        List<ProductRevenueDto> data = orderItemRepository.findTopProductsByRevenue(monthStart, monthEnd);

        if (data == null || data.isEmpty()) {
            return "Dạ, không có dữ liệu sản phẩm bán chạy trong tháng " + monthToCheck + "/" + yearToCheck + " ạ.";
        }

        // Tạo phản hồi
        StringBuilder response = new StringBuilder("Danh sách top sản phẩm bán chạy tháng " + monthToCheck + "/" + yearToCheck + " là:\n");
        for (int i = 0; i < data.size(); i++) {
            ProductRevenueDto dto = data.get(i);
            response.append((i + 1) + ". " + dto.getName() + " - Doanh thu: " + dto.getSales() + " VNĐ\n");
        }

        return response.toString();
    }
//    public String checkTopCategoriesRenvenue(String message) {
//        // Chuẩn hóa câu hỏi
//        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();
//        // Trả về kết quả
//        return "Dạ, doanh thu tổng của cả năm " + yearToCheck + " là " + data + " VNĐ";
//    }


        // Lưu đoạn hội thoại vào database
    public void saveConversation(Long userId,String interactionLog) {
        // Tìm user trong database
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        // Tìm chatbot trong database
        Chatbot chatbot = chatbotRepository.findByUserId(userId);
        UserChatbotId userChatbotId = new UserChatbotId(chatbot.getId(), user.getId());

        // Tạo mới một đối tượng UserChatbot
        UserChatbot userChatbot = new UserChatbot();
        userChatbot.setId(userChatbotId);
        userChatbot.setUser(user);
        userChatbot.setChatbot(chatbot); // Đảm bảo chatbot luôn có giá trị

        userChatbot.setInteractionLog(interactionLog);
        userChatbot.setLastInteractionAt(LocalDateTime.now()); // Lưu thời gian hội thoại

        // Lưu vào database
        userChatbotRepository.save(userChatbot);
        System.out.println("Đã lưu đoạn hội thoại vào database.");
    }

    public String getVietnamEventSuggestion(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();
        int yearToCheck = now.getYear();

        // Regex tìm "tháng x"
        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");

        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);

        // Nếu người dùng nhập tháng cụ thể
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        // Tính khoảng thời gian đầu và cuối tháng
        LocalDate monthStart = LocalDate.of(yearToCheck, monthToCheck, 1);

        return switch (monthToCheck) {
            case 1, 2 -> "Gần Tết Nguyên Đán, nên tập trung áo dài, quần áo mới, đồ đi chơi Tết.";
            case 3 -> "Tháng có 8/3 – nên đẩy mạnh váy, đầm, phụ kiện nữ.";
            case 4, 5 -> "Chuẩn bị lễ 30/4 - 1/5, có thể tập trung thời trang du lịch, áo thun, kính mát.";
            case 6 -> "Mùa tốt nghiệp, ngày của Cha – sơ mi, vest, quà tặng nam.";
            case 7 -> "Mùa sale giữa năm – đẩy mạnh hàng tồn, khuyến mãi.";
            case 8 -> "Cuối hè – chuẩn bị thời trang thu, đồng phục học sinh.";
            case 9 -> "Gần 2/9 – tập trung thời trang công sở, đồ đi chơi.";
            case 10 -> "Tháng có 20/10 – váy, đầm, túi xách là lựa chọn hàng đầu.";
            case 11 -> "Ngày Nhà giáo Việt Nam – đồ thanh lịch, áo dài, quà tặng.";
            case 12 -> "Chuẩn bị Noel – tập trung áo len, áo khoác, đồ đông.";
            default -> "";
        };
    }

    public String getVietnamWeatherSuggestion(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy tháng và năm hiện tại
        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();
        int yearToCheck = now.getYear();

        // Regex tìm "tháng x"
        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");
        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        // Xác định mùa hiện tại
        String currentSeason = "";
        List<String> currentSuggestions = new ArrayList<>();

        String nextSeason = "";
        List<String> nextSuggestions = new ArrayList<>();

        if (monthToCheck >= 2 && monthToCheck <= 4) {
            currentSeason = "Mùa Xuân";
            currentSuggestions = List.of("Áo khoác nhẹ", "Váy dài tay", "Áo sơ mi", "Giày búp bê");
            nextSeason = "Mùa Hè";
            nextSuggestions = List.of("Áo thun", "Váy ngắn", "Quần short", "Kính râm", "Nón rộng vành");
        } else if (monthToCheck >= 5 && monthToCheck <= 8) {
            currentSeason = "Mùa Hè";
            currentSuggestions = List.of("Áo thun", "Váy ngắn", "Quần short", "Kính râm", "Nón rộng vành");
            nextSeason = "Mùa Thu";
            nextSuggestions = List.of("Cardigan", "Áo sơ mi dài tay", "Váy nhẹ", "Khăn lụa");
        } else if (monthToCheck >= 9 && monthToCheck <= 11) {
            currentSeason = "Mùa Thu";
            currentSuggestions = List.of("Cardigan", "Áo sơ mi dài tay", "Váy nhẹ", "Khăn lụa");
            nextSeason = "Mùa Đông";
            nextSuggestions = List.of("Áo khoác dày", "Áo len", "Khăn quàng cổ", "Giày boot");
        } else { // tháng 12, 1
            currentSeason = "Mùa Đông";
            currentSuggestions = List.of("Áo khoác dày", "Áo len", "Khăn quàng cổ", "Giày boot");
            nextSeason = "Mùa Xuân";
            nextSuggestions = List.of("Áo khoác nhẹ", "Váy dài tay", "Áo sơ mi", "Giày búp bê");
        }

        // Gợi ý trả về
        return "Tháng " + monthToCheck + " là " + currentSeason + " – gợi ý: " +
                String.join(", ", currentSuggestions) + ".<br>" +
                "Bạn cũng có thể chuẩn bị sớm cho " + nextSeason + " – các mặt hàng như: " +
                String.join(", ", nextSuggestions) + ".";
    }


    public String checkPriceAndCategory(String message) {
        String lowerCaseMessage = message.toLowerCase();

        // --- Bước 1: Tách giá ---
        Pattern pattern = Pattern.compile("(\\d+[\\.,]?\\d*)\\s*(nghìn|k|tr|triệu)?");
        Matcher matcher = pattern.matcher(lowerCaseMessage);

        List<Long> priceList = new ArrayList<>();
        while (matcher.find()) {
            String numberPart = matcher.group(1).replace(",", "").replace(".", "");
            String unit = matcher.group(2);
            long value = Long.parseLong(numberPart);

            if (unit != null) {
                switch (unit) {
                    case "k":
                    case "nghìn":
                        value *= 1_000;
                        break;
                    case "tr":
                    case "triệu":
                        value *= 1_000_000;
                        break;
                }
            }

            priceList.add(value);
        }

        // --- Bước 2: Xác định khoảng giá ---
        long minPrice = 0;
        long maxPrice = Long.MAX_VALUE;

        if (priceList.size() == 1) {
            long basePrice = priceList.get(0);
            minPrice = Math.max(0, basePrice - 50_000);
            maxPrice = basePrice + 50_000;
        } else if (priceList.size() >= 2) {
            minPrice = Math.min(priceList.get(0), priceList.get(1));
            maxPrice = Math.max(priceList.get(0), priceList.get(1));
        }

        // --- Bước 3: Kiểm tra danh mục ---
        List<Category> categories = categoryRepository.getAllByParentCategoryNotNull();
        Optional<String> matchedCategory = categories.stream()
                .map(cat -> cat.getName().toLowerCase())
                .filter(lowerCaseMessage::contains)
                .findFirst();

        List<Product> products;

        if (matchedCategory.isPresent()) {
            // Nếu có danh mục -> lọc theo khoảng giá và danh mục
            products = productRepository.findByPriceBetweenAndCategoryName(minPrice, maxPrice, matchedCategory.get());
        } else {
            // Nếu không có danh mục -> chỉ lọc theo giá
            products = productRepository.findByPriceBetween(minPrice, maxPrice);
        }

        // --- Bước 4: Trả kết quả ---
        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào phù hợp với yêu cầu.";
        }

        StringBuilder result = new StringBuilder("<b>Các sản phẩm phù hợp:</b>");
        for (Product product : products) {
            result.append("<br> - ")
                    .append(product.getName())
                    .append(" (")
                    .append(product.getPrice())
                    .append(" VND)");
        }

        return result.toString().trim();
    }

    public String faqShow() {
        return """
        <div style="max-width: 700px; margin: auto;">
        <h2>CÂU HỎI THƯỜNG GẶP (FAQ)</h2>
        <p><strong>1. Tôi có thể đổi trả sản phẩm trong bao lâu?</strong><br>
        Bạn có thể đổi hoặc trả sản phẩm trong vòng <strong>7 ngày</strong> kể từ ngày nhận hàng, với điều kiện sản phẩm chưa qua sử dụng và còn nguyên tem, nhãn, bao bì.</p>

        <p><strong>2. Tôi có thể theo dõi đơn hàng của mình ở đâu?</strong><br>
        Bạn có thể theo dõi đơn hàng bằng cách đăng nhập vào tài khoản và truy cập mục <strong>“Đơn hàng của tôi”</strong>.</p>

        <p><strong>3. Phí vận chuyển được tính như thế nào?</strong><br>
        Phí vận chuyển sẽ được tính dựa trên vị trí giao hàng. <strong>Miễn phí vận chuyển</strong> cho đơn hàng từ <strong>500.000 VNĐ</strong> trở lên.</p>

        <p><strong>4. Tôi có thể thanh toán bằng hình thức nào?</strong><br>
        Chúng tôi hỗ trợ nhiều hình thức thanh toán như:<br>
        – Thanh toán khi nhận hàng (<strong>COD</strong>)<br>
        – Chuyển khoản ngân hàng<br>
        – Thanh toán trực tuyến qua <strong>VNPay</strong> hoặc <strong>Momo</strong></p>

        <p><strong>5. Làm sao để sử dụng mã giảm giá?</strong><br>
        Bạn có thể nhập <strong>mã giảm giá</strong> tại bước thanh toán trong ô “Mã khuyến mãi”. Mỗi mã sẽ có điều kiện và thời hạn sử dụng riêng.</p>

        <p><strong>6. Tôi có thể liên hệ bộ phận chăm sóc khách hàng bằng cách nào?</strong><br>
        Bạn có thể liên hệ qua hotline <strong>1900 1234</strong>, email <strong>support@thoitrangweb.vn</strong>, hoặc nhắn tin qua fanpage Facebook của chúng tôi.</p>
        </div>
    """;
    }
    public String refundPolicyForStaff() {
        return """
    <div style="max-width: 700px; margin: auto;">
        <h2>CHÍNH SÁCH ĐỔI TRẢ SẢN PHẨM</h2>
        <p><strong>1. Thời hạn đổi/trả:</strong><br>
        Khách hàng được đổi hoặc trả sản phẩm trong vòng <strong>7 ngày</strong> kể từ ngày nhận hàng. Thời gian tính theo ngày giao hàng thành công được cập nhật trên hệ thống.</p>

        <p><strong>2. Điều kiện đổi/trả:</strong><br>
        – Sản phẩm chưa qua sử dụng, còn nguyên tem, nhãn mác, bao bì gốc.<br>
        – Không có dấu hiệu bị giặt, bẩn, rách, hỏng do tác động bên ngoài.<br>
        – Có đầy đủ hóa đơn mua hàng hoặc mã đơn hàng.<br>
        – Sản phẩm nằm trong danh mục <strong>được áp dụng đổi/trả</strong>.</p>

        <p><strong>3. Trường hợp không áp dụng đổi/trả:</strong><br>
        – Sản phẩm trong chương trình giảm giá trên 50% (trừ khi có lỗi từ nhà sản xuất).<br>
        – Sản phẩm thuộc nhóm phụ kiện, đồ lót, tất vớ, đồ mặc trong.<br>
        – Sản phẩm bị hư hỏng do khách hàng bảo quản sai cách.</p>

        <p><strong>4. Quy trình đổi/trả:</strong><br>
        – Bước 1: Khách hàng liên hệ bộ phận CSKH qua hotline hoặc fanpage.<br>
        – Bước 2: Cung cấp mã đơn hàng, lý do đổi/trả và hình ảnh sản phẩm.<br>
        – Bước 3: Chờ xác nhận từ nhân viên và tiến hành gửi hàng về kho.<br>
        – Bước 4: Nhận sản phẩm mới hoặc hoàn tiền sau khi kiểm tra hàng.</p>

        <p><strong>5. Phí đổi/trả:</strong><br>
        – Đổi hàng do lỗi của nhà cung cấp: <strong>Miễn phí</strong>.<br>
        – Đổi hàng do nhu cầu cá nhân: Khách hàng chịu phí vận chuyển 2 chiều.</p>

        <p><strong>Lưu ý:</strong> Toàn bộ quy trình xử lý đổi/trả thường mất từ <strong>3–5 ngày làm việc</strong> kể từ khi chúng tôi nhận được sản phẩm gửi về.</p>
    </div>
    """;
    }





//    public String getConversationHistory(Long userId) {
//        List<UserChatbot> history = userChatbotRepository.findTop5ByUserIdOrderByLastInteractionAtDesc(userId);
//        return history.stream().map(UserChatbot::getInteractionLog).collect(Collectors.joining("\n"));
//    }

      // Gợi ý theo người dùng
//    public String analyzeUserData(Long userId) {
//        List<Product> purchasedProducts = orderRepository.findPurchasedProductsByUserId(userId);
//        String dataSummary = purchasedProducts.stream().map(Product::getName).collect(Collectors.joining(", "));
//
//        String prompt = "Người dùng đã mua: " + dataSummary + ". Hãy gợi ý sản phẩm phù hợp.";
//        return chatWithAI(prompt);
//    }



}
