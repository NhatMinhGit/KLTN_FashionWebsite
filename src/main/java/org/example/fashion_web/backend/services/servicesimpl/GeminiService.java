package org.example.fashion_web.backend.services.servicesimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fashion_web.backend.configurations.GeminiConfig;
import org.example.fashion_web.backend.dto.ProductRevenueDto;
import org.example.fashion_web.backend.models.*;
import org.example.fashion_web.backend.repositories.*;
import org.example.fashion_web.backend.services.*;
import org.example.fashion_web.backend.utils.CurrencyFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private ProductService productService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserChatbotRepository userChatbotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatbotRepository chatbotRepository;

    @Autowired
    private ChatbotMessageRepository chatbotMessageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SizeService sizeService;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private DiscountService discountService;

    private final WebClient webClient = WebClient.builder().build();


    public String chatWithAI(String message,boolean isAdmin) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").trim();

        // Truy vấn sản phẩm liên quan đến câu hỏi
        List<Product> relatedProducts = productRepository.findByNameContaining(normalizedMessage);
        if (relatedProducts.isEmpty()) {
            return "{\"error\": \"Không tìm thấy sản phẩm nào liên quan.\"}";
        }
        
        String productInfo;
        if (isAdmin) {
            // Dành cho admin
            Map<Long, Map<Long, List<String>>> productVariantImages = prepareProductVariantImages(relatedProducts);
            productInfo = generateProductInfoForStaff(relatedProducts, productVariantImages);
        } else {
            // Dành cho user
            Map<Long, Map<Long, Map<Long, List<String>>>> productVariantImagesWithSize = prepareProductVariantImagesWithSize(relatedProducts);
            productInfo = generateProductInfo(relatedProducts, productVariantImagesWithSize);
        }

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

            // Kiểm tra phản hồi từ Gemini API
            JSONObject jsonResponse = new JSONObject(response);
            String aiResponse;
            if (jsonResponse.has("candidates")) {
                aiResponse = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
                aiResponse = aiResponse
                        .replaceAll("\\*\\s\\*\\*(.*?)\\*\\*", "<br><strong>$1</strong><br>")
                        .replaceAll("\\*\\s", "<br>- ");
            } else {
                aiResponse = "API không trả về phản hồi hợp lệ!";
            }

            // Gộp aiResponse và productInfo thành JSON trả về
            Map<String, String> result = new HashMap<>();
            result.put("aiResponse", aiResponse);

            if (!relatedProducts.isEmpty() && productInfo != null && !productInfo.isBlank()) {
                result.put("productInfo", productInfo);
            }

            return new ObjectMapper().writeValueAsString(result);

        } catch (WebClientResponseException e) {
            return "{\"error\": \"Lỗi từ Gemini API: " + e.getResponseBodyAsString() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Lỗi hệ thống: " + e.getMessage() + "\"}";
        }
    }

    private String generateProductInfoForStaff(List<Product> relatedProducts, Map<Long, Map<Long, List<String>>> productVariantImages) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<div style='display:flex; flex-wrap:wrap; gap:10px;'>");

        relatedProducts.forEach(p -> {
            Long productId = p.getId();
            Long variantId = p.getVariants().get(0).getId();
            List<String> imageUrls = productVariantImages.getOrDefault(productId, new HashMap<>())
                    .getOrDefault(variantId, Collections.emptyList());

            String productImage = (imageUrls.isEmpty())
                    ? "https://via.placeholder.com/80?text=No+Image"
                    : imageUrls.get(0);

            String priceHtml = CurrencyFormatter.formatVND(p.getPrice());
            String productOnClick = "onclick='window.location.href=\"/user/product-detail/" + productId + "\"'";

            // Thêm CSS animation inline
//            String cardStyle = "border:1px solid #ccc; border-radius:8px; padding:10px; width:150px; cursor:pointer; "
//                    + "transition: transform 0.3s, box-shadow 0.3s; "
//                    + "display:inline-block;";

            String cardStyle = "border:1px solid #ccc; border-radius:8px; padding:10px; width:100%; cursor:pointer; "
                    + "transition: transform 0.3s, box-shadow 0.3s; "
                    + "flex: 0 0 calc(25% - 10px); box-sizing: border-box;";
            String cardHoverStyle = "this.style.transform='scale(1.05)'; this.style.boxShadow='0 4px 12px rgba(0,0,0,0.2)';";
            String cardUnhoverStyle = "this.style.transform='scale(1)'; this.style.boxShadow='none';";


            htmlBuilder.append("<div style='").append(cardStyle).append("' ")
                    .append(productOnClick)
                    .append(" onmouseover=\"").append(cardHoverStyle).append("\" ")
                    .append(" onmouseout=\"").append(cardUnhoverStyle).append("\">")
                    .append("<div style='text-align:center; margin-bottom:8px;'>")
                    .append("<img src='").append(productImage).append("' style='border-radius:8px;' alt='").append(p.getName()).append("' width='80' height='80'>")
                    .append("</div><div style='text-align:center;'>")
                    .append("<h6 style='color:#6c757d; margin:0; font-size:12px;'>").append(p.getCategory().getName()).append("</h6>")
                    .append("<h6 style='margin:0; font-size:14px;'>").append(p.getName()).append("</h6>")
                    .append("<p style='font-weight:bold; margin:0; font-size:14px;'>").append(priceHtml).append("</p>")
                    .append("</div></div>");
        });

        htmlBuilder.append("</div>");
        return htmlBuilder.toString();

    }
private String generateProductInfo(List<Product> relatedProducts, Map<Long, Map<Long, Map<Long, List<String>>>> preparedImages) {
    StringBuilder htmlBuilder = new StringBuilder();
    htmlBuilder.append("<div class='row'>");
    relatedProducts.forEach(product -> {
        Long productId = product.getId();

        // Lấy variant đầu tiên nếu có
        ProductVariant variant = product.getVariants() != null && !product.getVariants().isEmpty() ? product.getVariants().get(0) : null;
        if (variant == null) return;

        Long variantId = variant.getId();

        // Lấy map size->images theo productId và variantId
        Map<Long, Map<Long, List<String>>> variantMap = preparedImages.getOrDefault(productId, Collections.emptyMap());
        Map<Long, List<String>> sizeMap = variantMap.getOrDefault(variantId, Collections.emptyMap());

        // Lấy sizeId mặc định (ví dụ size đầu tiên trong sizeMap)
        Long defaultSizeId = sizeMap.keySet().stream().findFirst().orElse(null);

        // Lấy list ảnh theo size mặc định
        List<String> imageUrls = defaultSizeId != null ? sizeMap.getOrDefault(defaultSizeId, Collections.emptyList()) : Collections.emptyList();

        String productImage = imageUrls.isEmpty()
                ? "https://via.placeholder.com/80?text=No+Image"
                : imageUrls.get(0);

        String priceHtml = CurrencyFormatter.formatVND(product.getPrice());

        // CSS animation inline
        String cardStyle = "border:1px solid #ccc; border-radius:8px; padding:3px; width:180px; cursor:pointer; "
                + "transition: transform 0.3s, box-shadow 0.3s; display:inline-block;";

        String cardHoverStyle = "this.style.transform='scale(1.05)'; this.style.boxShadow='0 4px 12px rgba(0,0,0,0.2)';";
        String cardUnhoverStyle = "this.style.transform='scale(1)'; this.style.boxShadow='none';";

        htmlBuilder
                .append("<div class='col-md-3 mb-4'>")
                .append("<div id='product-card-").append(productId).append("' class='product-card' style='").append(cardStyle).append("' ")
                .append(" data-product-id='").append(product.getId()).append("' data-variant-id='").append(variant.getId()).append("' ")
                .append(" onmouseover=\"").append(cardHoverStyle).append("\" ")
                .append(" onmouseout=\"").append(cardUnhoverStyle).append("\">")

                // Ảnh
                .append("<div style='text-align:center; margin-bottom:8px;'>")
                .append("<img id='product-img-").append(productId).append("' src='").append(productImage)
                .append("' style='border-radius:8px;' alt='").append(product.getName()).append("' width='80' height='80'>")
                .append("</div>")

                // Thông tin
                .append("<div style='text-align:center;'>")
                .append("<h6 id='category-").append(productId).append("' style='color:#6c757d; margin:0; font-size:12px;'>")
                .append(product.getCategory().getName()).append("</h6>")
//                .append("<h6 id='name-").append(productId).append("' style='margin:0; font-size:14px;'>").append(product.getName()).append("</h6>")
                .append("<h6 id='name-").append(productId)
                .append("' style='margin:0; font-size:13px; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;'>")
                .append(product.getName()).append("</h6>")

                .append("<p id='price-").append(productId).append("' class='price' style='font-weight:bold; margin:0; font-size:14px;'>")
                .append(priceHtml).append("</p>")


                // Màu
                .append("<div class='mb-1'>")
//                .append("<label class='form-label' style='font-size: 12px;'>Chọn màu:</label>")
                .append("<select name='color_").append(product.getId()).append("' class='form-select form-select-sm' style='font-size: 12px;'>")
                .append("<option value='' disabled selected>-- Chọn màu --</option>");
                Set<String> uniqueColors = new HashSet<>();
                for (ProductVariant variantC : product.getVariants()) {
                    uniqueColors.add(variantC.getColor()); // hoặc addAll nếu trả về List
                }
                for (String color : uniqueColors) {
                    htmlBuilder.append("<option value='").append(color).append("'>").append(color).append("</option>");
                }
                htmlBuilder.append("</select>")
                .append("</div>")

                        // Size
                        .append("<div class='mb-1'>")
//                        .append("<label class='form-label' style='font-size: 12px;'>Chọn size:</label>")
                        .append("<select name='size_").append(product.getId()).append("' class='form-select form-select-sm size-select' style='font-size: 12px;'>")
                        .append("<option value='' disabled selected>-- Chọn size --</option>")
                        .append("<option value='S'>S</option>")
                        .append("<option value='M'>M</option>")
                        .append("<option value='L'>L</option>")
                        .append("<option value='XL'>XL</option>")
                        .append("<option value='XXL'>XXL</option>")
                        .append("</select>")
                        .append("</div>")

                        .append("<div class='mb-1'>")
                        .append("<label class='form-label' style='font-size: 12px;'>Số lượng:</label>")
                        .append("<input type='number' name='quantity_").append(product.getId())
                        .append("' class='form-control form-control-sm quantity' min='1' value='1' style='font-size: 12px;' />")
                        .append("</div>")

                        // Thêm vào trước nút "Thêm vào giỏ"
                        .append("<input type='hidden' name='productId_").append(product.getId()).append("' value='").append(product.getId()).append("'/>")
                        .append("<input type='hidden' name='variantId_").append(product.getId()).append("' value='").append(variant.getId()).append("'/>")
                        .append("<input type='hidden' name='selectedColor_").append(product.getId()).append("' id='selectedColor_").append(product.getId()).append("'/>")
                        .append("<input type='hidden' name='selectedSize_").append(product.getId()).append("' id='selectedSize_").append(product.getId()).append("'/>")

                        // Nút giỏ hàng
                        .append("<button type='button' class='btn btn-primary btn-sm w-100' onclick='addToCart(this)'>")
                        .append("<i class='fas fa-cart-plus'></i> Thêm vào giỏ</button>")


                        .append("</div>") // card-body
                        .append("</div>") // card
                .append("</div>");


    });

    htmlBuilder.append("</div>");
    return htmlBuilder.toString();
}


    public String searchProducts(String message) {
        List<Product> products = productRepository.findByNameContaining(message);
        return products.isEmpty() ? "Không tìm thấy sản phẩm phù hợp." : products.stream().map(Product::getName).collect(Collectors.joining(", "));
    }

    public String checkStock(String message,boolean isAdmin) {
        // Chuẩn hóa câu hỏi để lấy đúng tên sản phẩm
        String normalizedMessage = message.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();

        // Kiểm tra sản phẩm trong database
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> {
                    String normalizedProductName = p.getName().toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
                    return normalizedMessage.contains(normalizedProductName) || normalizedProductName.contains(normalizedMessage);
                })
                .collect(Collectors.toList());

        // Nếu không tìm thấy sản phẩm
        if (relatedProducts.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            result.put("aiResponse", "Dạ, em không tìm thấy sản phẩm nào khớp với yêu cầu.");
            result.put("productInfo", "");
            try {
                return new ObjectMapper().writeValueAsString(result);
            } catch (JsonProcessingException e) {
                return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
            }
        }

        String productInfo;
        if (isAdmin) {
            // Dành cho admin
            Map<Long, Map<Long, List<String>>> productVariantImages = prepareProductVariantImages(relatedProducts);
            productInfo = generateProductInfoForStaff(relatedProducts, productVariantImages);
        } else {
            // Dành cho user
            Map<Long, Map<Long, Map<Long, List<String>>>> productVariantImagesWithSize = prepareProductVariantImagesWithSize(relatedProducts);
            productInfo = generateProductInfo(relatedProducts, productVariantImagesWithSize);
        }



        // Chuẩn bị JSON phản hồi
        Map<String, String> result = new HashMap<>();
        StringBuilder aiResponse = new StringBuilder("Dạ, em tìm thấy sản phẩm bên dưới ạ:<br>");

        for (Product product : relatedProducts) {
            List<ProductVariant> variants = productVariantService.findAllByProductId(product.getId());
            for (ProductVariant variant : variants) {
                aiResponse.append("- ").append(variant.getColor()).append(":<br>");
                List<Size> sizes = sizeService.findAllByProductVariantId(variant.getId());
                for (Size size : sizes) {
                    String sizeName = size.getSizeName() != null ? size.getSizeName() : "Không rõ";
                    int quantity = size.getStockQuantity() != null ? size.getStockQuantity() : 0;
                    aiResponse.append("   + Size: ").append(sizeName)
                            .append(" – Số lượng: ").append(quantity).append("<br>");
                }
            }
        }

        result.put("aiResponse", aiResponse.toString());

        result.put("productInfo", productInfo);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String checkPriceAndCategory(String message,Boolean isAdmin) {
        String lowerCaseMessage = message.toLowerCase();

// --- Bước 1: Tách giá ---
        Pattern pattern = Pattern.compile("(\\d+[\\.,]?\\d*)\\s*(nghìn|k|tr|triệu|tỷ)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(lowerCaseMessage);

        List<BigDecimal> priceList = new ArrayList<>();
        while (matcher.find()) {
            try {
                String numberPart = matcher.group(1).replace(",", "").replace(".", "");
                String unit = matcher.group(2);

                // Sử dụng BigDecimal.valueOf() thay vì parseLong
                BigDecimal value = new BigDecimal(numberPart);

                if (unit != null) {
                    switch (unit.toLowerCase()) {
                        case "k", "nghìn" -> value = value.multiply(BigDecimal.valueOf(1_000));
                        case "tr", "triệu" -> value = value.multiply(BigDecimal.valueOf(1_000_000));
                        case "tỷ" -> value = value.multiply(BigDecimal.valueOf(1_000_000_000));
                    }
                }

                priceList.add(value);
            } catch (NumberFormatException ignored) {
            }
        }


        // --- Bước 3: Kiểm tra sản phẩm trong database ---
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> {
                    String normalizedProductName = normalize(p.getName()).toLowerCase();
                    String normalizedMessage = normalize(message).toLowerCase();

                    // Kiểm tra xem tên sản phẩm có chứa bất kỳ từ khóa nào trong thông điệp
                    return Arrays.stream(normalizedMessage.split("\\s+"))
                            .anyMatch(normalizedProductName::contains); // Kiểm tra từng từ khóa trong thông điệp
                })
                .toList();


        // --- Bước 2: Xác định khoảng giá ---
        BigDecimal minPrice;
        BigDecimal maxPrice;

        // Kiểm tra giá nếu có 1 giá
        if (priceList.size() == 1) {
            BigDecimal basePrice = priceList.get(0);
            minPrice = basePrice.subtract(BigDecimal.valueOf(50_000));
            maxPrice = basePrice.add(BigDecimal.valueOf(50_000));
        }
        // Nếu có 2 giá, chọn min và max
        else if (priceList.size() >= 2) {
            BigDecimal price1 = priceList.get(0);
            BigDecimal price2 = priceList.get(1);

            // Dùng compareTo để xác định giá trị nhỏ nhất và lớn nhất
            minPrice = price1.compareTo(price2) < 0 ? price1 : price2;
            maxPrice = price1.compareTo(price2) > 0 ? price1 : price2;
        } else {
            maxPrice = BigDecimal.valueOf(Long.MAX_VALUE);
            minPrice = BigDecimal.ZERO;
        }

        // --- Bước 4: Lọc theo giá trong Java ---
        List<Product> filteredProducts = relatedProducts.stream()
                .filter(p -> {
                    BigDecimal price = p.getEffectivePrice() != null ? p.getEffectivePrice() : p.getPrice();
                    return price != null && price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0;
                })
                .collect(Collectors.toList());


        // --- Bước 5: Tạo JSON phản hồi ---
        String productInfo;
        if (isAdmin) {
            // Dành cho admin
            Map<Long, Map<Long, List<String>>> productVariantImages = prepareProductVariantImages(filteredProducts);
            productInfo = generateProductInfoForStaff(filteredProducts, productVariantImages);
        } else {
            // Dành cho user
            Map<Long, Map<Long, Map<Long, List<String>>>> productVariantImagesWithSize = prepareProductVariantImagesWithSize(filteredProducts);
            productInfo = generateProductInfo(filteredProducts, productVariantImagesWithSize);
        }

        // Chuẩn bị JSON phản hồi
        Map<String, String> result = new HashMap<>();
        StringBuilder aiResponse = new StringBuilder();
        // Kiểm tra nếu không có sản phẩm nào
        if (filteredProducts.isEmpty()) {
            aiResponse.append("Dạ, hiện tại không có sản phẩm nào phù hợp với yêu cầu của bạn.<br>");
        } else {
            aiResponse.append("Dạ, em tìm thấy sản phẩm bên dưới ạ:<br>");
        }

        result.put("aiResponse", aiResponse.toString());

        result.put("productInfo", productInfo);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý JSON: " + e.getMessage() + "\"}";
        }
    }

    public String normalize(String input) {
        if (input == null) {
            return "";  // Tránh lỗi nếu input là null
        }
        // Chuyển tất cả ký tự thành chữ thường và loại bỏ ký tự không phải chữ cái, chữ số và khoảng trắng
        return input.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
    }


    private String extractEntityFromMessage(String message, String entityName) {
        // Dùng regex giống controller để lấy entity từ message
        Pattern pattern;
        if ("month".equals(entityName)) {
            pattern = Pattern.compile("(tháng\\s*(\\d{1,2}))|(\\b(0?[1-9]|1[0-2])\\b)");
        } else if ("year".equals(entityName)) {
            pattern = Pattern.compile("năm\\s*(\\d{4})|(\\b20\\d{2}\\b)");
        } else {
            return null;
        }

        Matcher matcher = pattern.matcher(message.toLowerCase());
        if (matcher.find()) {
            return matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
        }

        return null;
    }

    public String checkMonthlyRevenue(String message) {
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
        BigDecimal revenue = orderRepository.getMonthlyRevenue(yearToCheck, monthToCheck);
        if (revenue == null) revenue = BigDecimal.ZERO;

        // Lấy số đơn hàng trong tháng từ DB
        int totalOrders = orderRepository.countOrdersThisMonth();

        // Định dạng doanh thu
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        String formattedRevenue = formatter.format(revenue);

        // Tạo phản hồi JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", "Dạ, doanh thu tháng " + monthToCheck + "/" + yearToCheck + " là " + formattedRevenue +
                " và số đơn hàng trong tháng là " + totalOrders + " đơn.");
        result.put("month", String.valueOf(monthToCheck));
        result.put("year", String.valueOf(yearToCheck));
        result.put("revenue", formattedRevenue);
        result.put("totalOrders", String.valueOf(totalOrders));

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý JSON: " + e.getMessage() + "\"}";
        }
    }


    public String checkYearlyRenvenue(String message) {
        // Chuẩn hóa câu hỏi
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        // Mặc định lấy năm hiện tại
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

        // Định dạng doanh thu
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        String formattedRevenue = formatter.format(data);

        // Tạo phản hồi JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", "Dạ, doanh thu tổng của cả năm " + yearToCheck + " là " + formattedRevenue);
        result.put("year", String.valueOf(yearToCheck));
        result.put("revenue", formattedRevenue);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý JSON: " + e.getMessage() + "\"}";
        }
    }

    public String checkTopProductsRevenueForUser(String message,Boolean isAdmin) {
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

        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        if (yearMatcher.find()) {
            yearToCheck = Integer.parseInt(yearMatcher.group(1));
        }

        LocalDate monthStart = LocalDate.of(yearToCheck, monthToCheck, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> data = orderItemRepository.findTopProductsByRevenue(monthStart, monthEnd);

        if (data == null || data.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            result.put("aiResponse", "Dạ, không có dữ liệu sản phẩm bán chạy trong tháng " + monthToCheck + "/" + yearToCheck + " ạ.");
            result.put("productInfo", "");
            try {
                return new ObjectMapper().writeValueAsString(result);
            } catch (JsonProcessingException e) {
                return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
            }
        }

        // Lấy danh sách ID từ DTO
        List<Long> productIds = data.stream()
                .map(ProductRevenueDto::getId)
                .collect(Collectors.toList());

        // Lấy sản phẩm từ DB
        List<Product> productsFromDb = productRepository.findAllById(productIds);

        // Map id → Product để dễ tra cứu
        Map<Long, Product> productMap = productsFromDb.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Chuẩn bị danh sách Product có đầy đủ dữ liệu (bắt theo thứ tự DTO)
        List<Product> relatedProducts = data.stream()
                .map(dto -> productMap.get(dto.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String productInfoHtml;
        if (isAdmin) {
            // Dành cho admin
            Map<Long, Map<Long, List<String>>> productVariantImages = prepareProductVariantImages(relatedProducts);
            productInfoHtml = generateProductInfoForStaff(relatedProducts, productVariantImages);
        } else {
            // Dành cho user
            Map<Long, Map<Long, Map<Long, List<String>>>> productVariantImagesWithSize = prepareProductVariantImagesWithSize(relatedProducts);
            productInfoHtml = generateProductInfo(relatedProducts, productVariantImagesWithSize);
        }

        // Tạo response AI text
        StringBuilder response = new StringBuilder("Danh sách top sản phẩm bán chạy tháng " + monthToCheck + "/" + yearToCheck + " là:<br>");
        for (int i = 0; i < data.size(); i++) {
            ProductRevenueDto dto = data.get(i);
            response.append((i + 1)).append(". ").append(dto.getName()).append(" - Doanh thu: ").append(dto.getSales()).append(" VNĐ<br>");
        }

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", response.toString());
        if (!relatedProducts.isEmpty() && productInfoHtml != null && !productInfoHtml.isBlank()) {
            result.put("productInfo", productInfoHtml);
        }

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String checkTopProductsRevenueForOptimalPlan(String message,boolean isAdmin) {
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

        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        if (yearMatcher.find()) {
            yearToCheck = Integer.parseInt(yearMatcher.group(1));
        }

        LocalDate monthStart = LocalDate.of(yearToCheck, monthToCheck, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<ProductRevenueDto> data = orderItemRepository.findTopProductsByRevenue(monthStart, monthEnd);

        if (data == null || data.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            result.put("productInfo", "");
            try {
                return new ObjectMapper().writeValueAsString(result);
            } catch (JsonProcessingException e) {
                return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
            }
        }

        // Lấy danh sách ID từ DTO
        List<Long> productIds = data.stream()
                .map(ProductRevenueDto::getId)
                .collect(Collectors.toList());

        // Lấy sản phẩm từ DB
        List<Product> productsFromDb = productRepository.findAllById(productIds);

        // Map id → Product để dễ tra cứu
        Map<Long, Product> productMap = productsFromDb.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // Chuẩn bị danh sách Product có đầy đủ dữ liệu (bắt theo thứ tự DTO)
        List<Product> relatedProducts = data.stream()
                .map(dto -> productMap.get(dto.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Chuẩn bị map productVariantImages
        String productInfoHtml;
        if (isAdmin) {
            // Dành cho admin
            Map<Long, Map<Long, List<String>>> productVariantImages = prepareProductVariantImages(relatedProducts);
            productInfoHtml = generateProductInfoForStaff(relatedProducts, productVariantImages);
        } else {
            // Dành cho user
            Map<Long, Map<Long, Map<Long, List<String>>>> productVariantImagesWithSize = prepareProductVariantImagesWithSize(relatedProducts);
            productInfoHtml = generateProductInfo(relatedProducts, productVariantImagesWithSize);
        }

        // Tạo response AI text

        Map<String, String> result = new HashMap<>();
        if (!relatedProducts.isEmpty() && !productInfoHtml.isBlank()) {
            result.put("productInfo", productInfoHtml);
        }

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    private Map<Long, Map<Long, List<String>>> prepareProductVariantImages(List<Product> products) {
        Map<Long, Map<Long, List<String>>> result = new HashMap<>();

        for (Product product : products) {
            Map<Long, List<String>> variantImagesMap = new HashMap<>();
            boolean productImageSet = false;

            if (product.getVariants() != null) {
                for (ProductVariant variant : product.getVariants()) {
                    List<String> imageUrls = new ArrayList<>();
                    List<Image> variantImages = imageService.findImagesByProductVariantId(variant.getId());

                    if (variantImages != null && !variantImages.isEmpty()) {
                        imageUrls.add(variantImages.get(0).getImageUri());  // Lấy hình đầu tiên
                        variantImagesMap.put(variant.getId(), imageUrls);

                        if (!productImageSet) {
                            // Đặt hình đại diện cho product là hình đầu tiên tìm được từ variant
                            result.put(product.getId(), Map.of(variant.getId(), imageUrls));
                            productImageSet = true;
                        }
                    }
                }
            }

            // Nếu không tìm thấy hình từ variant nào, vẫn thêm product vào map (với map rỗng)
            result.putIfAbsent(product.getId(), variantImagesMap);
        }

        return result;
    }
    private Map<Long, Map<Long, Map<Long, List<String>>>> prepareProductVariantImagesWithSize(List<Product> products) {
        Map<Long, Map<Long, Map<Long, List<String>>>> result = new HashMap<>();

        for (Product product : products) {
            Map<Long, Map<Long, List<String>>> variantMap = new HashMap<>();
            boolean productImageSet = false;

            if (product.getVariants() != null) {
                for (ProductVariant variant : product.getVariants()) {
                    Map<Long, List<String>> sizeMap = new HashMap<>();

                    // Giả sử ProductVariant có getSizes() trả về danh sách size
                    if (variant.getSizes() != null) {
                        for (Size size : variant.getSizes()) {
                            List<Image> sizeImages = imageService.findImagesByProductVariantId(variant.getId());
                            List<String> imageUrls = new ArrayList<>();

                            if (sizeImages != null && !sizeImages.isEmpty()) {
                                // Lấy tất cả hình ảnh size hoặc chỉ lấy 1 tùy bạn
                                for (Image img : sizeImages) {
                                    imageUrls.add(img.getImageUri());
                                }

                                sizeMap.put(size.getId(), imageUrls);

                                if (!productImageSet) {
                                    // Đặt hình đại diện cho product là hình đầu tiên của size đầu tiên variant đầu tiên
                                    result.put(product.getId(), Map.of(variant.getId(), Map.of(size.getId(), imageUrls)));
                                    productImageSet = true;
                                }
                            }
                        }
                    }

                    // Nếu không có size, vẫn thêm variant với map size rỗng
                    variantMap.put(variant.getId(), sizeMap);
                }
            }

            // Nếu không có variant, thêm product với map rỗng
            result.putIfAbsent(product.getId(), variantMap);
        }

        return result;
    }


    public void saveConversation(Chatbot chatbot, String senderType, String messageText, String messageType, String intent, String entities) {
        ChatbotMessage message = new ChatbotMessage();
        message.setChatbot(chatbot);
        message.setCreatedAt(LocalDateTime.now());
        message.setSenderType(senderType);
        message.setMessageText(messageText);
        message.setMessageType(messageType);
        message.setIntent(intent);
        message.setEntities(entities);
        chatbotMessageRepository.save(message);
    }


    public String getVietnamEventSuggestion(String message) {
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();

        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");
        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

        String suggestion = switch (monthToCheck) {
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
            default -> "Không có sự kiện nổi bật.";
        };

        Map<String, Object> response = new HashMap<>();
        response.put("month", monthToCheck);
        response.put("suggestion", suggestion);

        try {
            return new ObjectMapper().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi JSON: " + e.getMessage() + "\"}";
        }
    }

    public String getVietnamWeatherSuggestion(String message) {
        String normalizedMessage = message.replaceAll("[^a-zA-Z0-9À-ỹ ]", "").toLowerCase().trim();

        LocalDate now = LocalDate.now();
        int monthToCheck = now.getMonthValue();

        Pattern monthPattern = Pattern.compile("tháng\\s*(\\d{1,2})");
        Matcher monthMatcher = monthPattern.matcher(normalizedMessage);
        if (monthMatcher.find()) {
            monthToCheck = Integer.parseInt(monthMatcher.group(1));
        }

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
        } else {
            currentSeason = "Mùa Đông";
            currentSuggestions = List.of("Áo khoác dày", "Áo len", "Khăn quàng cổ", "Giày boot");
            nextSeason = "Mùa Xuân";
            nextSuggestions = List.of("Áo khoác nhẹ", "Váy dài tay", "Áo sơ mi", "Giày búp bê");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", monthToCheck);
        result.put("currentSeason", currentSeason);
        result.put("currentSuggestions", currentSuggestions);
        result.put("nextSeason", nextSeason);
        result.put("nextSuggestions", nextSuggestions);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý JSON: " + e.getMessage() + "\"}";
        }
    }


    public String loadServiceLog(Long id) {
        try {
            // Lấy danh sách Service Log
            List<UserChatbot> serviceLogs = userChatbotRepository.findAllByUserId(id);

            // Kiểm tra nếu serviceLogs là null hoặc rỗng
            if (serviceLogs == null || serviceLogs.isEmpty()) {
                return "Không có dữ liệu dịch vụ cho người dùng này.";
            }

            // Tạo chuỗi kết quả
            StringBuilder result = new StringBuilder();
            for (UserChatbot log : serviceLogs) {
                result.append("ID: ").append(log.getId()).append(", ")
                        .append("Timestamp: ").append(log.getLastInteractionAt()).append(", ")
                        .append("Interaction Log: ").append(log.getInteractionLog()).append("\n");
            }

            return result.toString();
        } catch (Exception e) {
            // Log lỗi ra log file (nên dùng logger thay vì e.printStackTrace trong thực tế)
            e.printStackTrace();
            return "Đã xảy ra lỗi khi tải dữ liệu dịch vụ: " + e.getMessage();
        }
    }


    public String faqShowForUser() {

        // Tạo phản hồi mặc định
        String faqContent = """
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
                    Bạn có thể liên hệ qua hotline <strong>0765 599 103</strong>, email <strong>support@mntfashion.vn</strong>, hoặc nhắn tin qua fanpage Facebook của chúng tôi.</p>
                    </div>
                """;

        // Trả về nội dung câu hỏi thường gặp dưới dạng JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", faqContent);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String refundPolicyForUser() {
        // Nội dung chính sách đổi trả
        String refundPolicyContent = """
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
        // Trả về nội dung câu hỏi thường gặp dưới dạng JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", refundPolicyContent);

        // Chuyển đổi map thành JSON và trả về
        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String faqShowForStaff() {
        String refundPolicyContent = """
                <div style="max-width: 700px; margin: auto;">
                <h2>FAQ DÀNH CHO NHÂN VIÊN</h2>
                    
                <p><strong>1. Chính sách đổi trả được áp dụng thế nào?</strong><br>
                Nhân viên cần kiểm tra điều kiện sản phẩm trước khi nhận đổi/trả: sản phẩm chưa qua sử dụng, còn tem, nhãn, bao bì và kèm hóa đơn mua hàng.</p>
                    
                <p><strong>2. Quy trình xử lý đơn hàng ra sao?</strong><br>
                Khi nhận được đơn, nhân viên cần xác nhận tồn kho, chuẩn bị hàng, đóng gói đúng tiêu chuẩn và chuyển cho bộ phận giao hàng trong vòng <strong>24 giờ</strong>.</p>
                    
                <p><strong>3. Khi khách hàng khiếu nại, tôi phải làm gì?</strong><br>
                Lắng nghe khách hàng, ghi nhận thông tin cụ thể và chuyển ngay đến bộ phận chăm sóc khách hàng hoặc quản lý để xử lý kịp thời.</p>
                    
                <p><strong>4. Ca làm việc và chấm công thế nào?</strong><br>
                Nhân viên cần có mặt trước ca làm <strong>15 phút</strong>, chấm công đúng giờ và báo cáo cho quản lý nếu có vấn đề phát sinh.</p>
                    
                <p><strong>5. Mục tiêu doanh số của cửa hàng là gì?</strong><br>
                Mục tiêu doanh số được cập nhật hàng tháng. Nhân viên có thể xem chi tiết tại bảng thông báo nội bộ hoặc hỏi quản lý ca.</p>
                    
                <p><strong>6. Tôi có thể liên hệ phòng nhân sự qua đâu?</strong><br>
                Vui lòng liên hệ hotline nội bộ <strong>0765 599 103</strong> hoặc email <strong>hr@mntfashion.vn</strong> để được hỗ trợ các vấn đề nhân sự.</p>
                </div>
                """;
        // Trả về nội dung câu hỏi thường gặp dưới dạng JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", refundPolicyContent);

        // Chuyển đổi map thành JSON và trả về
        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String refundPolicyForStaff() {
        String refundPolicyContent = """
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
        // Trả về nội dung câu hỏi thường gặp dưới dạng JSON
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", refundPolicyContent);

        // Chuyển đổi map thành JSON và trả về
        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String shippingIssueResponseFoUser() {
        String shippingIssueContent = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>THÔNG TIN VỀ TÌNH TRẠNG GIAO HÀNG</h2>
                    <p>Chúng tôi rất xin lỗi vì sự bất tiện này. Nếu bạn chưa nhận được đơn hàng, vui lòng thực hiện theo các bước sau:</p>

                    <p><strong>1. Kiểm tra mã đơn hàng:</strong><br>
                    – Vui lòng chuẩn bị mã đơn hàng để chúng tôi có thể hỗ trợ nhanh chóng.</p>

                    <p><strong>2. Liên hệ bộ phận chăm sóc khách hàng:</strong><br>
                    – Gọi <strong>Hotline: 0765 599 103</strong> hoặc nhắn tin qua <strong>Fanpage chính thức</strong>.<br>
                    – Cung cấp <strong>mã đơn hàng</strong> và mô tả vấn đề (ví dụ: chưa nhận được hàng, giao nhầm, v.v).</p>

                    <p><strong>3. Thời gian xử lý:</strong><br>
                    – Nhân viên hỗ trợ sẽ kiểm tra tình trạng vận chuyển và phản hồi trong vòng <strong>24–48h</strong>.<br>
                    – Nếu đơn hàng thất lạc, chúng tôi sẽ tiến hành gửi lại hoặc hoàn tiền theo chính sách.</p>

                    <p><strong>Lưu ý:</strong><br>
                    – Vui lòng kiểm tra kỹ thông tin người nhận và địa chỉ đã cung cấp khi đặt hàng.<br>
                    – Trong một số trường hợp giao hàng chậm do <strong>thời tiết</strong>, <strong>dịch bệnh</strong> hoặc <strong>bưu tá liên hệ không thành công</strong>.</p>
                </div>
                """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", shippingIssueContent);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String paymentMethodChangeInstructions() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>THAY ĐỔI PHƯƠNG THỨC THANH TOÁN</h2>
                    <p>Nếu bạn muốn thay đổi phương thức thanh toán cho đơn hàng đã đặt, vui lòng liên hệ trực tiếp với bộ phận chăm sóc khách hàng để được hỗ trợ:</p>

                    <p><strong>1. Qua hotline:</strong> <br>
                    Gọi <strong>0765 599 103</strong> và cung cấp mã đơn hàng cùng thông tin bạn muốn điều chỉnh.</p>

                    <p><strong>2. Qua Fanpage:</strong><br>
                    Nhắn tin trực tiếp đến <strong>Fanpage chính thức</strong> của cửa hàng và yêu cầu hỗ trợ đổi phương thức thanh toán.</p>

                    <p><strong>Lưu ý:</strong><br>
                    – Việc thay đổi có thể chỉ áp dụng nếu đơn hàng chưa được xử lý giao.<br>
                    – Một số phương thức thanh toán có thể không hỗ trợ thay đổi sau khi xác nhận.</p>
                </div>
                """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String paymentDeclinedReasonResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>LÝ DO THANH TOÁN BỊ TỪ CHỐI</h2>
                    <p>Việc thanh toán có thể bị từ chối vì một số lý do phổ biến sau:</p>
                    <ul>
                        <li>Thông tin thẻ hoặc ví điện tử không chính xác.</li>
                        <li>Số dư tài khoản không đủ tại thời điểm giao dịch.</li>
                        <li>Ngân hàng hoặc cổng thanh toán tạm thời gián đoạn dịch vụ.</li>
                        <li>Giao dịch bị nghi ngờ là bất thường và bị chặn bởi hệ thống phòng chống gian lận.</li>
                        <li>Thẻ thanh toán chưa được kích hoạt hoặc bị khóa.</li>
                    </ul>
                    <p><strong>Hướng dẫn xử lý:</strong></p>
                    <ul>
                        <li>Vui lòng kiểm tra lại thông tin thanh toán.</li>
                        <li>Thử sử dụng một phương thức thanh toán khác.</li>
                        <li>Liên hệ ngân hàng để kiểm tra trạng thái tài khoản hoặc thẻ.</li>
                        <li>Hoặc liên hệ CSKH của cửa hàng qua hotline <strong>0765 599 103</strong> để được hỗ trợ thêm.</li>
                    </ul>
                </div>
                """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String deliveryTimeResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>THỜI GIAN GIAO HÀNG</h2>
                    <p>Thời gian giao hàng dự kiến sẽ phụ thuộc vào địa chỉ nhận hàng và phương thức vận chuyển bạn chọn khi đặt hàng:</p>
                    <ul>
                        <li><strong>Nội thành:</strong> 1–2 ngày làm việc.</li>
                        <li><strong>Ngoại thành / Tỉnh thành khác:</strong> 3–5 ngày làm việc.</li>
                    </ul>
                    <p><strong>Lưu ý:</strong> Thời gian trên có thể thay đổi trong dịp cao điểm, khuyến mãi hoặc điều kiện thời tiết xấu.</p>
                    <p>Chúng tôi luôn nỗ lực để giao hàng đến bạn sớm nhất có thể!</p>
                </div>
                """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String orderTrackingResponse(String message, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByEmail(username);

        // Kiểm tra nếu người dùng là admin
        List<Order> orders;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            orders = orderRepository.findAllOrdersInCurrentMonth();
        } else {
            orders = orderRepository.findOrdersInCurrentMonthByUser(user.getId());
        }

        Map<Order.OrderStatusType, List<Order>> groupedOrders = orders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatusType.PENDING
                        || order.getStatus() == Order.OrderStatusType.SHIPPED
                        || order.getStatus() == Order.OrderStatusType.COMPLETED
                        || order.getStatus() == Order.OrderStatusType.CANCELLED)
                .collect(Collectors.groupingBy(Order::getStatus));

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("📦 Tình trạng đơn hàng trong tháng này:<br><br>");

        for (Order.OrderStatusType status : Arrays.asList(
                Order.OrderStatusType.PENDING,
                Order.OrderStatusType.SHIPPED,
                Order.OrderStatusType.COMPLETED,
                Order.OrderStatusType.CANCELLED)) {

            List<Order> ordersByStatus = groupedOrders.getOrDefault(status, Collections.emptyList());

            responseBuilder.append("🔹 ").append(statusToText(status))
                    .append(": ").append(ordersByStatus.size()).append(" đơn<br>");
        }

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", responseBuilder.toString());

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }


    private String statusToText(Order.OrderStatusType status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case SHIPPED -> "Đang giao hàng";
            case COMPLETED -> "Đã hoàn thành";
            case CANCELLED -> "Đã hủy";
            default -> "Khác";
        };
    }

    public String returnPolicyResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>CHÍNH SÁCH ĐỔI TRẢ</h2>
                    <p>Chính sách đổi trả của cửa hàng cho phép <strong>đổi trong vòng 7 ngày</strong> kể từ khi nhận hàng.</p>
                    <p>Vui lòng giữ nguyên bao bì và hóa đơn khi đổi sản phẩm.</p>
                    <h3>Thông tin liên hệ:</h3>
                    <p>📞 Số điện thoại: 0765 599 103</p>
                    <p>📧 Email: support@mntfashion.com</p>
                    <p>🌐 Website: <a href="https://mntfashion.store">mntfashion.store</a></p>
                </div>
                """;
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }


    public String productIssueResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>SẢN PHẨM BỊ LỖI / KHÔNG ĐÚNG MÔ TẢ</h2>
                    <p>Nếu sản phẩm bạn nhận được <strong>bị lỗi</strong> hoặc <strong>không đúng mô tả</strong>, bạn có thể <strong>liên hệ ngay với chúng tôi để được đổi/trả miễn phí</strong>.</p>
                    <p>Đội ngũ CSKH sẽ hỗ trợ bạn nhanh chóng.</p>
                    <h3>Thông tin liên hệ:</h3>
                    <p>📞 Số điện thoại: 0765 599 103</p>
                    <p>📧 Email: support@mntfashion.com</p>
                    <p>🌐 Website: <a href="https://mntfashion.store">mntfashion.store</a></p>
                </div>
                """;
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String refundTimeResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>THỜI GIAN HOÀN TIỀN</h2>
                    <p>Thời gian xử lý hoàn tiền thường mất từ <strong>3 đến 7 ngày làm việc</strong> kể từ khi yêu cầu được xác nhận.</p>
                    <p>Thời gian có thể thay đổi tùy thuộc vào ngân hàng hoặc phương thức thanh toán.</p>
                    <h3>Thông tin liên hệ:</h3>
                    <p>📞 Số điện thoại: 0765 599 103</p>
                    <p>📧 Email: support@mntfashion.com</p>
                    <p>🌐 Website: <a href="https://mntfashion.store">mntfashion.store</a></p>
                </div>
                """;
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String changeProductModelResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto;">
                    <h2>ĐỔI SANG MẪU KHÁC</h2>
                    <p>Bạn có thể đổi sang mẫu khác trong vòng <strong>7 ngày</strong> nếu sản phẩm chưa qua sử dụng và còn nguyên bao bì.</p>
                    <p>Vui lòng liên hệ CSKH để được hỗ trợ đổi mẫu nhanh chóng.</p>
                    <h3>Thông tin liên hệ:</h3>
                    <p>📞 Số điện thoại: 0765 599 103</p>
                    <p>📧 Email: support@mntfashion.com</p>
                    <p>🌐 Website: <a href="https://mntfashion.store">mntfashion.store</a></p>
                </div>
                """;
        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String technicalSupportForStaffResponse() {
        String content = """
                <div style="max-width: 700px; margin: auto; font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #2E86C1;">HỖ TRỢ KỸ THUẬT (DÀNH CHO NHÂN VIÊN)</h2>
                    <p>Nếu bạn gặp bất kỳ vấn đề nào liên quan đến hệ thống công nghệ thông tin, phần mềm nội bộ hoặc thiết bị tại nơi làm việc, vui lòng liên hệ bộ phận kỹ thuật để được hỗ trợ nhanh chóng và hiệu quả.</p>
                    
                    <h3>Quy trình hỗ trợ:</h3>
                    <ol>
                        <li>Gửi yêu cầu qua hệ thống Helpdesk hoặc gọi số nội bộ 103.</li>
                        <li>Phòng kỹ thuật tiếp nhận và phản hồi trong vòng <strong>30 phút</strong> đến <strong>1 giờ</strong> làm việc.</li>
                        <li>Kỹ thuật viên sẽ hỗ trợ trực tiếp hoặc lên lịch xử lý sự cố.</li>
                        <li>Nhân viên xác nhận sự cố đã được giải quyết.</li>
                    </ol>
                    
                    <h3>Thời gian làm việc của bộ phận kỹ thuật:</h3>
                    <p><strong>Thứ 2 - Thứ 6:</strong> 8:00 - 17:30</p>
                    <p><strong>Thứ 7, Chủ nhật & ngày lễ:</strong> Hỗ trợ khẩn cấp qua số nội bộ 103</p>
                    
                    <h3>Thông tin liên hệ nội bộ:</h3>
                    <p>📞 <strong>Số nội bộ:</strong> 103 (Phòng kỹ thuật)</p>
                    <p>📧 <strong>Email:</strong> it.support@mntfashion.com</p>
                    <p>🛠️ <strong>Hệ thống yêu cầu hỗ trợ:</strong> <a href="https://intranet.mntfashion.store/helpdesk" target="_blank" rel="noopener noreferrer">intranet.mntfashion.store/helpdesk</a></p>
                    
                    <h3>Lưu ý:</h3>
                    <ul>
                        <li>Vui lòng cung cấp mô tả chi tiết về sự cố khi gửi yêu cầu để bộ phận kỹ thuật có thể xử lý nhanh hơn.</li>
                        <li>Đảm bảo thiết bị và phần mềm được cập nhật bản mới nhất theo hướng dẫn của phòng IT.</li>
                        <li>Không tự ý can thiệp vào hệ thống hoặc cấu hình máy tính khi chưa được sự đồng ý của phòng kỹ thuật.</li>
                    </ul>
                </div>
                """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String technicalSupportForCustomerResponse() {
        String content = """
        <div style="max-width: 700px; margin: auto; font-family: Arial, sans-serif; line-height: 1.6;">
            <h2 style="color: #28a745;">HỖ TRỢ KỸ THUẬT (DÀNH CHO KHÁCH HÀNG)</h2>
            <p>Nếu bạn gặp sự cố hoặc cần trợ giúp liên quan đến sản phẩm, dịch vụ hoặc website của chúng tôi, đội ngũ kỹ thuật luôn sẵn sàng hỗ trợ bạn.</p>
            <p>Chúng tôi cam kết phản hồi và giải quyết các vấn đề nhanh nhất có thể để đảm bảo trải nghiệm mua sắm và sử dụng dịch vụ của bạn luôn thuận tiện và hài lòng.</p>
            
            <h3>Phương thức liên hệ:</h3>
            <ul>
                <li>Gọi Hotline Kỹ Thuật: <strong>0765 599 103</strong> (24/7 hỗ trợ)</li>
                <li>Gửi Email: <a href="mailto:techsupport@mntfashion.com">techsupport@mntfashion.com</a></li>
                <li>Chat trực tiếp với nhân viên hỗ trợ: <a href="https://mntfashion.store/chat" target="_blank" rel="noopener noreferrer">https://mntfashion.store/chat</a></li>
            </ul>
            
            <h3>Hướng dẫn khi liên hệ:</h3>
            <ul>
                <li>Vui lòng cung cấp mô tả chi tiết về vấn đề bạn gặp phải (ví dụ: lỗi, thông báo, bước thực hiện).</li>
                <liChuẩn bị các thông tin về đơn hàng hoặc tài khoản để quá trình xử lý được nhanh chóng.</li>
                <li>Đội ngũ kỹ thuật sẽ phản hồi trong vòng <strong>24 giờ</strong> làm việc.</li>
            </ul>
            
            <p>Chúng tôi rất mong được đồng hành và hỗ trợ bạn trong suốt quá trình sử dụng sản phẩm và dịch vụ của MNT Fashion.</p>
        </div>
        """;

        Map<String, String> result = new HashMap<>();
        result.put("aiResponse", content);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Lỗi xử lý dữ liệu JSON: " + e.getMessage() + "\"}";
        }
    }

    public String recommendProductBasedOnViewedResponse(@CookieValue(value = "viewedProducts", required = false) String viewedProductsCookie) {
        if (viewedProductsCookie != null) {
            System.out.println("Cookie viewedProducts: " + viewedProductsCookie);
            ObjectMapper mapper = new ObjectMapper();
            try {
                // Parse chuỗi JSON cookie thành List<String>
                List<String> viewedProductIdsStr = mapper.readValue(viewedProductsCookie, new TypeReference<>() {});

                // Chuyển sang List<Long>, bỏ qua các id không hợp lệ
                List<Long> viewedProductIds = viewedProductIdsStr.stream()
                        .map(idStr -> {
                            try {
                                return Long.parseLong(idStr);
                            } catch (NumberFormatException e) {
                                return null; // Bỏ qua nếu không parse được
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();

                if (!viewedProductIds.isEmpty()) {
                    // Lấy danh sách sản phẩm đã xem
                    List<Product> viewedProducts = productService.getProductsById(viewedProductIds);

                    // Lấy các danh mục không trùng lặp
                    Set<Category> viewedCategories = viewedProducts.stream()
                            .map(Product::getCategory)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    // Xác định khoảng thời gian 3 tháng gần đây
                    LocalDate startDate = LocalDate.now().minusMonths(3);
                    LocalDate endDate = LocalDate.now();

                    // Map chứa top sản phẩm theo từng danh mục
                    Map<Category, List<ProductRevenueDto>> topProductsByCategory = new HashMap<>();

                    for (Category category : viewedCategories) {
                        List<ProductRevenueDto> topProducts = orderItemRepository.findTopProductsByRevenueAndCategory(
                                startDate, endDate, category.getId());

                        // Chỉ lấy top 10 sản phẩm
                        List<ProductRevenueDto> top10 = topProducts.stream().limit(10).toList();

                        topProductsByCategory.put(category, top10);
                    }

                    // Gom tất cả top product DTO thành 1 list duy nhất để lấy product info
                    List<ProductRevenueDto> allTopProducts = topProductsByCategory.values()
                            .stream()
                            .flatMap(List::stream)
                            .distinct()
                            .toList();

                    // Lấy Product entities tương ứng từ allTopProducts
                    List<Long> topProductIds = allTopProducts.stream()
                            .map(ProductRevenueDto::getId)
                            .toList();

                    List<Product> relatedProducts = productService.getProductsById(topProductIds);

                    // Lấy ảnh cho từng variant của top products
//                    Map<Long, Map<Long, List<String>>> topProductVariantImages = new HashMap<>();
//
//                    for (Product product : relatedProducts) {
//                        List<ProductVariant> variants = productVariantService.findAllByProductId(product.getId());
//                        Map<Long, List<String>> variantImageMap = new HashMap<>();
//
//                        for (ProductVariant variant : variants) {
//                            List<Image> images = imageService.findImagesByProductVariantId(variant.getId());
//                            List<String> imageUrls = images.stream()
//                                    .map(Image::getImageUri)
//                                    .toList();
//                            variantImageMap.put(variant.getId(), imageUrls);
//                        }
//                        topProductVariantImages.put(product.getId(), variantImageMap);
//                    }

                    // Tạo phần text AI response
                    StringBuilder aiResponse = new StringBuilder("Danh sách các sản phẩm bạn có thể thích "
                            + LocalDate.now().getMonthValue() + "/" + LocalDate.now().getYear() + " là:<br>");
                    for (int i = 0; i < allTopProducts.size(); i++) {
                        ProductRevenueDto dto = allTopProducts.get(i);
                        aiResponse.append(i + 1).append(". ").append(dto.getName())
                                .append(" - Danh mục: ").append(dto.getCategory())
                                .append(" - Nhãn hàng: ").append(dto.getBrand()).append("<br>");
                    }
                    Map<Long, Map<Long, Map<Long, List<String>>>> topProductVariantImages = prepareProductVariantImagesWithSize(relatedProducts);

                    // Tạo phần HTML thông tin sản phẩm
                    String productInfoHtml = generateProductInfo(relatedProducts, topProductVariantImages);

                    // Đóng vào Map để serialize JSON
                    Map<String, String> result = new HashMap<>();
                    result.put("aiResponse", aiResponse.toString());
                    if (relatedProducts != null && !relatedProducts.isEmpty() && productInfoHtml != null && !productInfoHtml.isBlank()) {
                        result.put("productInfo", productInfoHtml);
                    }

                    // Trả về JSON string
                    return mapper.writeValueAsString(result);

                } else {
                    System.out.println("No valid viewed product IDs found.");
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Lỗi xử lý JSON cookie: " + e.getMessage(), e);
            }
        }

        // Nếu không có cookie hoặc lỗi, trả về chuỗi rỗng hoặc thông báo phù hợp
        return "{\"aiResponse\":\"Không có sản phẩm đã xem hoặc dữ liệu không hợp lệ.\"}";
    }

}