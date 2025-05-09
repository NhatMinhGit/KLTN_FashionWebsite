package org.example.fashion_web.frontend.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhtmlrenderer.pdf.ITextRenderer;

@RestController
public class InvoicePdfController {

    @GetMapping("/admin/invoice")
    public void generateInvoice(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=invoice.pdf");

            // Load HTML content
            String htmlContent = "<html><body><h1>Hóa đơn xuất kho</h1><p>Mã đơn hàng: 12345</p><p>Ngày xuất: " + new java.util.Date() + "</p></body></html>";

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);

            // Layout and write PDF to output stream
            renderer.layout();
            renderer.createPDF(response.getOutputStream());
            response.getOutputStream().flush();  // Ensure the stream is flushed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
