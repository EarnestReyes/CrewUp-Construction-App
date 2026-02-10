package workers.works.invoice;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Modern Invoice Generator - Creates professional HTML invoices
 * Custom modern design with professional styling
 */
public class InvoiceGenerator {

    private static final String TAG = "InvoiceGenerator";
    private Context context;
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

    public InvoiceGenerator(Context context) {
        this.context = context;
    }

    public File generate(Invoice invoice) {
        try {
            // Validate and generate invoice number if needed
            String invoiceNum ="0";
            if (invoiceNum == null || invoiceNum.trim().isEmpty()) {
                invoiceNum = generateInvoiceNumber();

            }

            // Create output directory
            File outputDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Create filename
            String sanitizedNum = invoiceNum.replaceAll("[^a-zA-Z0-9]", "_");
            String fileName = "Invoice_" + sanitizedNum + ".html";
            File outputFile = new File(outputDir, fileName);

            // Generate HTML content
            String htmlContent = generateHTMLInvoice(invoice);

            // Write to file
            FileOutputStream fos = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            writer.write(htmlContent);
            writer.close();
            fos.close();

            Log.d(TAG, "Invoice generated successfully: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (Exception e) {
            Log.e(TAG, "Error generating invoice", e);
            e.printStackTrace();
            return null;
        }
    }

    private String generateInvoiceNumber() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        return "INV-" + sdf.format(new Date());
    }

    private String generateHTMLInvoice(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='en'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Invoice ").append("</title>\n");
        html.append("<style>\n");
        html.append(getModernCSS());
        html.append("</style>\n");
        html.append("</head>\n<body>\n");

        // Print Button
        html.append("<div class='no-print'>\n");
        html.append("<button class='print-btn' onclick='window.print()'>🖨️ Print Invoice</button>\n");
        html.append("</div>\n");

        // Main Container
        html.append("<div class='invoice-container'>\n");

        // Header with Company and Invoice Info
        html.append(generateModernHeader(invoice));

        // Client Information Bar
        html.append(generateClientBar(invoice));

        // Work Description (if exists)
        if (invoice.getWorkDescription() != null && !invoice.getWorkDescription().trim().isEmpty()) {
            html.append(generateWorkSection(invoice));
        }

        // Line Items Section
        html.append("<div class='items-section'>\n");

        // Materials
        if (invoice.getMaterials() != null && !invoice.getMaterials().isEmpty()) {
            html.append(generateMaterialsSection(invoice));
        }

        // Labor
        if (invoice.getLabor() != null && !invoice.getLabor().isEmpty()) {
            html.append(generateLaborSection(invoice));
        }

        // Miscellaneous
        if (invoice.getMiscellaneous() != null && !invoice.getMiscellaneous().isEmpty()) {
            html.append(generateMiscSection(invoice));
        }

        html.append("</div>\n");

        // Totals Summary
        html.append(generateTotalsSection(invoice));



        // Footer
        html.append(generateModernFooter());

        html.append("</div>\n");
        html.append("</body>\n</html>");

        return html.toString();
    }

    private String getModernCSS() {
        return
                "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                        "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; color: #2d3748; }\n" +
                        ".invoice-container { max-width: 900px; margin: 0 auto; background: white; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); overflow: hidden; }\n" +
                        ".print-btn { position: fixed; top: 20px; right: 20px; background: #E6A423; color: #000; border: none; padding: 12px 24px; border-radius: 50px; font-weight: bold; cursor: pointer; box-shadow: 0 4px 15px rgba(230,164,35,0.4); transition: all 0.3s; font-size: 14px; z-index: 1000; }\n" +
                        ".print-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(230,164,35,0.6); }\n" +
                        ".header-section { background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); padding: 40px; color: white; position: relative; overflow: hidden; }\n" +
                        ".header-section::before { content: ''; position: absolute; top: -50%; right: -10%; width: 400px; height: 400px; background: rgba(230,164,35,0.1); border-radius: 50%; }\n" +
                        ".header-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; position: relative; z-index: 1; }\n" +
                        ".company-section h1 { font-size: 32px; margin-bottom: 10px; color: #E6A423; text-transform: uppercase; letter-spacing: 2px; }\n" +
                        ".company-section .company-name { font-size: 20px; font-weight: 600; margin-bottom: 15px; }\n" +
                        ".company-section .info-line { margin: 8px 0; opacity: 0.9; font-size: 14px; }\n" +
                        ".invoice-section { text-align: right; }\n" +
                        ".invoice-number { font-size: 28px; font-weight: bold; color: #E6A423; margin-bottom: 20px; }\n" +
                        ".invoice-meta { background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; backdrop-filter: blur(10px); }\n" +
                        ".meta-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(255,255,255,0.1); }\n" +
                        ".meta-row:last-child { border-bottom: none; }\n" +
                        ".meta-label { opacity: 0.8; font-size: 13px; }\n" +
                        ".meta-value { font-weight: 600; }\n" +
                        ".client-bar { background: #f7fafc; padding: 30px 40px; border-left: 5px solid #E6A423; }\n" +
                        ".client-badge { display: inline-block; background: #E6A423; color: #000; padding: 6px 16px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                        ".client-name { font-size: 24px; font-weight: bold; color: #1a202c; margin-bottom: 10px; }\n" +
                        ".client-details { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; margin-top: 15px; }\n" +
                        ".client-detail { display: flex; align-items: center; gap: 8px; color: #4a5568; font-size: 14px; }\n" +
                        ".client-detail::before { content: '●'; color: #E6A423; font-size: 8px; }\n" +
                        ".details-table { padding: 30px 40px; background: #fafafa; }\n" +
                        ".details-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }\n" +
                        ".detail-card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border-top: 3px solid #E6A423; }\n" +
                        ".detail-label { font-size: 12px; color: #718096; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; font-weight: 600; }\n" +
                        ".detail-value { font-size: 18px; font-weight: bold; color: #2d3748; }\n" +
                        ".work-section { padding: 30px 40px; background: white; border-left: 5px solid #4299e1; margin: 20px 0; }\n" +
                        ".work-title { font-size: 16px; font-weight: bold; color: #2d3748; margin-bottom: 15px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                        ".work-description { color: #4a5568; line-height: 1.8; }\n" +
                        ".items-section { padding: 40px; }\n" +
                        ".item-category { margin-bottom: 40px; }\n" +
                        ".category-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; padding-bottom: 10px; border-bottom: 2px solid #E6A423; }\n" +
                        ".category-icon { width: 40px; height: 40px; background: #E6A423; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; }\n" +
                        ".category-title { font-size: 20px; font-weight: bold; color: #1a202c; text-transform: uppercase; letter-spacing: 1px; }\n" +
                        ".items-table { width: 100%; border-collapse: separate; border-spacing: 0; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }\n" +
                        ".items-table thead { background: linear-gradient(135deg, #2d3748 0%, #1a202c 100%); color: white; }\n" +
                        ".items-table th { padding: 16px; text-align: left; font-weight: 600; font-size: 13px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                        ".items-table td { padding: 16px; border-bottom: 1px solid #e2e8f0; }\n" +
                        ".items-table tbody tr:hover { background: #f7fafc; }\n" +
                        ".items-table tbody tr:last-child td { border-bottom: none; }\n" +
                        ".total-row { background: #E6A423 !important; font-weight: bold; }\n" +
                        ".total-row td { color: #000 !important; font-size: 15px; padding: 18px 16px !important; }\n" +
                        ".text-right { text-align: right; }\n" +
                        ".text-center { text-align: center; }\n" +
                        ".totals-section { background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); padding: 40px; color: white; }\n" +
                        ".totals-grid { max-width: 500px; margin-left: auto; }\n" +
                        ".total-row-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.1); font-size: 15px; }\n" +
                        ".total-label { opacity: 0.9; }\n" +
                        ".total-value { font-weight: 600; color: #E6A423; }\n" +
                        ".grand-total-row { border-top: 3px solid #E6A423 !important; border-bottom: none !important; padding-top: 20px !important; margin-top: 10px; font-size: 24px !important; }\n" +
                        ".grand-total-row .total-label { font-weight: bold; color: white; }\n" +
                        ".grand-total-row .total-value { font-size: 28px; color: #E6A423; }\n" +
                        ".payment-section { padding: 40px; background: #f7fafc; }\n" +
                        ".payment-header { font-size: 20px; font-weight: bold; color: #1a202c; margin-bottom: 25px; text-transform: uppercase; letter-spacing: 1px; }\n" +
                        ".payment-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; }\n" +
                        ".payment-card { background: white; padding: 20px; border-radius: 12px; border-left: 4px solid #E6A423; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }\n" +
                        ".payment-label { font-size: 12px; color: #718096; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 8px; font-weight: 600; }\n" +
                        ".payment-value { font-size: 16px; font-weight: 600; color: #2d3748; }\n" +
                        ".footer-section { background: #1a202c; padding: 30px 40px; color: rgba(255,255,255,0.7); text-align: center; }\n" +
                        ".footer-text { font-size: 13px; line-height: 1.8; margin: 10px 0; }\n" +
                        ".footer-brand { margin-top: 20px; padding-top: 20px; border-top: 1px solid rgba(255,255,255,0.1); }\n" +
                        ".footer-brand strong { color: #E6A423; }\n" +
                        "@media print { body { background: white; padding: 0; } .invoice-container { box-shadow: none; border-radius: 0; } .print-btn, .no-print { display: none !important; } }\n" +
                        "@media (max-width: 768px) { .header-grid, .details-grid { grid-template-columns: 1fr; } .invoice-section { text-align: left; margin-top: 20px; } }\n";
    }

    private String generateModernHeader(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='header-section'>\n");
        html.append("<div class='header-grid'>\n");

        // Company Section
        html.append("<div class='company-section'>\n");
        html.append("<h1>INVOICE</h1>\n");
        html.append("<div class='company-name'>").append(escapeHtml(invoice.getCompanyName())).append("</div>\n");
        if (invoice.getCompanyAddress() != null) {
            html.append("<div class='info-line'>📍 ").append(escapeHtml(invoice.getCompanyAddress())).append("</div>\n");
        }
        if (invoice.getCompanyPhone() != null) {
            html.append("<div class='info-line'>📞 ").append(escapeHtml(invoice.getCompanyPhone())).append("</div>\n");
        }
        if (invoice.getCompanyEmail() != null) {
            html.append("<div class='info-line'>📧 ").append(escapeHtml(invoice.getCompanyEmail())).append("</div>\n");
        }
        html.append("</div>\n");

        // Invoice Section
        html.append("<div class='invoice-section'>\n");
        html.append("<div class='invoice-meta'>\n");

        html.append("</div>\n");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private String generateClientBar(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='client-bar'>\n");
        html.append("<span class='client-badge'>Bill To</span>\n");
        html.append("<div class='client-name'>").append(escapeHtml(invoice.getClientName())).append("</div>\n");
        html.append("<div class='client-details'>\n");

        if (invoice.getClientAddress() != null) {
            html.append("<div class='client-detail'>").append(escapeHtml(invoice.getClientAddress())).append("</div>\n");
        }
        if (invoice.getClientPhone() != null) {
            html.append("<div class='client-detail'>").append(escapeHtml(invoice.getClientPhone())).append("</div>\n");
        }
        if (invoice.getClientEmail() != null) {
            html.append("<div class='client-detail'>").append(escapeHtml(invoice.getClientEmail())).append("</div>\n");
        }

        html.append("</div>\n");
        html.append("</div>\n");

        return html.toString();
    }



    private String generateWorkSection(Invoice invoice) {
        return "<div class='work-section'>\n" +
                "<div class='work-title'>🔨 Project Description</div>\n" +
                "<div class='work-description'>" + escapeHtml(invoice.getWorkDescription()) + "</div>\n" +
                "</div>\n";
    }

    private String generateMaterialsSection(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='item-category'>\n");
        html.append("<div class='category-header'>\n");
        html.append("<div class='category-icon'>🧱</div>\n");
        html.append("<div class='category-title'>Materials</div>\n");
        html.append("</div>\n");

        html.append("<table class='items-table'>\n");
        html.append("<thead><tr><th>Item</th><th class='text-center'>Quantity</th><th class='text-right'>Rate</th><th class='text-right'>Amount</th></tr></thead>\n");
        html.append("<tbody>\n");

        for (MaterialItem item : invoice.getMaterials()) {
            html.append("<tr>");
            html.append("<td><strong>").append(escapeHtml(item.getMaterialName())).append("</strong></td>");
            html.append("<td class='text-center'>").append(item.getQuantity()).append("</td>");
            html.append("<td class='text-right'>$").append(currencyFormat.format(item.getRate())).append("</td>");
            html.append("<td class='text-right'><strong>$").append(currencyFormat.format(item.getTotal())).append("</strong></td>");
            html.append("</tr>\n");
        }

        html.append("<tr class='total-row'>");
        html.append("<td colspan='3'>MATERIALS TOTAL</td>");
        html.append("<td class='text-right'>$").append(currencyFormat.format(invoice.getTotalMaterials())).append("</td>");
        html.append("</tr>\n");

        html.append("</tbody>\n</table>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private String generateLaborSection(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='item-category'>\n");
        html.append("<div class='category-header'>\n");
        html.append("<div class='category-icon'>👷</div>\n");
        html.append("<div class='category-title'>Labor</div>\n");
        html.append("</div>\n");

        html.append("<table class='items-table'>\n");
        html.append("<thead><tr><th>Service</th><th class='text-center'>Hours</th><th class='text-right'>Rate/Hr</th><th class='text-right'>Amount</th></tr></thead>\n");
        html.append("<tbody>\n");

        for (LaborItem item : invoice.getLabor()) {
            html.append("<tr>");
            html.append("<td><strong>").append(escapeHtml(item.getLaborType())).append("</strong></td>");
            html.append("<td class='text-center'>").append(item.getHours()).append(" hrs</td>");
            html.append("<td class='text-right'>$").append(currencyFormat.format(item.getRate())).append("</td>");
            html.append("<td class='text-right'><strong>$").append(currencyFormat.format(item.getAmount())).append("</strong></td>");
            html.append("</tr>\n");
        }

        html.append("<tr class='total-row'>");
        html.append("<td colspan='3'>LABOR TOTAL</td>");
        html.append("<td class='text-right'>$").append(currencyFormat.format(invoice.getTotalLabor())).append("</td>");
        html.append("</tr>\n");

        html.append("</tbody>\n</table>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private String generateMiscSection(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='item-category'>\n");
        html.append("<div class='category-header'>\n");
        html.append("<div class='category-icon'>📋</div>\n");
        html.append("<div class='category-title'>Additional Charges</div>\n");
        html.append("</div>\n");

        html.append("<table class='items-table'>\n");
        html.append("<thead><tr><th>Description</th><th class='text-right'>Amount</th></tr></thead>\n");
        html.append("<tbody>\n");

        for (MiscItem item : invoice.getMiscellaneous()) {
            html.append("<tr>");
            html.append("<td><strong>").append(escapeHtml(item.getDescription())).append("</strong></td>");
            html.append("<td class='text-right'><strong>$").append(currencyFormat.format(item.getAmount())).append("</strong></td>");
            html.append("</tr>\n");
        }

        html.append("<tr class='total-row'>");
        html.append("<td>ADDITIONAL CHARGES TOTAL</td>");
        html.append("<td class='text-right'>$").append(currencyFormat.format(invoice.getTotalMiscellaneous())).append("</td>");
        html.append("</tr>\n");

        html.append("</tbody>\n</table>\n");
        html.append("</div>\n");

        return html.toString();
    }

    private String generateTotalsSection(Invoice invoice) {
        StringBuilder html = new StringBuilder();

        html.append("<div class='totals-section'>\n");
        html.append("<div class='totals-grid'>\n");

        html.append("<div class='total-row-item'>");
        html.append("<span class='total-label'>Subtotal</span>");
        html.append("<span class='total-value'>$").append(currencyFormat.format(invoice.getSubtotal())).append("</span>");
        html.append("</div>\n");


        html.append("<div class='total-row-item grand-total-row'>");
        html.append("<span class='total-label'>TOTAL DUE</span>");
        html.append("<span class='total-value'>$").append(currencyFormat.format(invoice.getGrandTotal())).append("</span>");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("</div>\n");

        return html.toString();
    }



    private String generateModernFooter() {
        return "<div class='footer-section'>\n" +
                "<div class='footer-text'>Thank you for your business! This invoice confirms completion of all specified work to satisfaction.</div>\n" +
                "<div class='footer-text'>Payment is due according to the terms specified above. Please retain this invoice for your records.</div>\n" +
                "<div class='footer-brand'>Generated by <strong>CrewUp Construction Management</strong></div>\n" +
                "</div>\n";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}