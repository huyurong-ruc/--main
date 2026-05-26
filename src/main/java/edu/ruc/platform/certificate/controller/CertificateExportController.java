package edu.ruc.platform.certificate.controller;

import edu.ruc.platform.certificate.dto.CertificatePreviewResponse;
import edu.ruc.platform.certificate.service.CertificateApplicationService;
import edu.ruc.platform.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequiredArgsConstructor
public class CertificateExportController {

    private final CertificateApplicationService certificateService;

    @GetMapping(value = "/exports/certificates/{id}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> export(@PathVariable Long id) {
        CertificatePreviewResponse preview = certificateService.preview(id);
        byte[] bytes = buildPdf(preview);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("certificate-" + preview.requestId() + ".pdf", StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(bytes));
    }

    private byte[] buildPdf(CertificatePreviewResponse preview) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDFont font = loadCjkFontOrFallback(doc);
            PDFont mono = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 52;
            float y = page.getMediaBox().getHeight() - margin;
            float x = margin;
            float leading = 18;
            float maxWidth = page.getMediaBox().getWidth() - margin * 2;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                y = writeWrapped(cs, font, 18, x, y, maxWidth, 22, "学院学生综合服务平台");
                y -= 4;
                y = writeWrapped(cs, font, 16, x, y, maxWidth, 20, "证明预览（示例导出）");
                y -= 10;

                y = writeKeyValue(cs, font, mono, 12, x, y, maxWidth, leading, "申请ID", String.valueOf(preview.requestId()));
                y = writeKeyValue(cs, font, mono, 12, x, y, maxWidth, leading, "学生", safe(preview.studentName()) + "（" + preview.studentId() + "）");
                y = writeKeyValue(cs, font, mono, 12, x, y, maxWidth, leading, "证明类型", safe(preview.certificateType()));
                y = writeKeyValue(cs, font, mono, 12, x, y, maxWidth, leading, "状态", safe(preview.status()));
                y = writeKeyValue(cs, font, mono, 12, x, y, maxWidth, leading, "用途/原因", safe(preview.reason()));
                y -= 6;

                y = writeWrapped(cs, font, 13, x, y, maxWidth, leading, "生成内容：");
                y = writeWrapped(cs, font, 12, x, y, maxWidth, leading, safe(preview.generatedContent()));
                y -= 6;

                Map<String, String> fields = preview.templateFields();
                if (fields != null && !fields.isEmpty()) {
                    y = writeWrapped(cs, font, 13, x, y, maxWidth, leading, "模板字段：");
                    for (Map.Entry<String, String> entry : fields.entrySet()) {
                        String line = entry.getKey() + " = " + safe(entry.getValue());
                        y = writeWrapped(cs, mono, 11, x, y, maxWidth, 14, line);
                        if (y < margin + 80) {
                            cs.endText();
                            y = margin;
                            break;
                        }
                    }
                }
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("导出PDF失败");
        }
    }

    private float writeKeyValue(PDPageContentStream cs, PDFont font, PDFont mono, float fontSize, float x, float y, float maxWidth, float leading,
                                String key, String value) throws IOException {
        float keyWidth = measure(mono, fontSize, key + "：");
        cs.beginText();
        cs.setFont(mono, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(key + "：");
        cs.endText();
        return writeWrapped(cs, font, fontSize, x + keyWidth + 6, y, maxWidth - keyWidth - 6, leading, value);
    }

    private float writeWrapped(PDPageContentStream cs, PDFont font, float fontSize, float x, float y, float maxWidth, float leading, String text) throws IOException {
        List<String> lines = wrap(font, fontSize, maxWidth, text == null ? "" : text);
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(x, y);
            cs.showText(line);
            cs.endText();
            y -= leading;
        }
        return y;
    }

    private List<String> wrap(PDFont font, float fontSize, float maxWidth, String text) throws IOException {
        String normalized = text.replace("\r", "");
        String[] paragraphs = normalized.split("\n");
        List<String> lines = new ArrayList<>();
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < paragraph.length(); i++) {
                char ch = paragraph.charAt(i);
                String next = current.toString() + ch;
                if (measure(font, fontSize, next) > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current.setLength(0);
                }
                current.append(ch);
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private float measure(PDFont font, float fontSize, String text) throws IOException {
        return font.getStringWidth(text) / 1000f * fontSize;
    }

    private PDFont loadCjkFontOrFallback(PDDocument doc) {
        List<Path> candidates = List.of(
                Path.of("/System/Library/Fonts/PingFang.ttc"),
                Path.of("/System/Library/Fonts/STHeiti Medium.ttc"),
                Path.of("/Library/Fonts/Arial Unicode.ttf"),
                Path.of("/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"),
                Path.of("/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc"),
                Path.of("/usr/share/fonts/opentype/noto/NotoSansCJKsc-Regular.otf"),
                Path.of("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"),
                Path.of("/usr/share/fonts/truetype/arphic/uming.ttc")
        );
        for (Path path : candidates) {
            if (!Files.exists(path)) {
                continue;
            }
            try {
                if (path.toString().toLowerCase().endsWith(".ttc")) {
                    try (InputStream in = Files.newInputStream(path); TrueTypeCollection ttc = new TrueTypeCollection(in)) {
                        AtomicReference<TrueTypeFont> first = new AtomicReference<>();
                        ttc.processAllFonts(item -> {
                            if (first.get() == null) {
                                first.set(item);
                            }
                        });
                        if (first.get() != null) {
                            return PDType0Font.load(doc, first.get(), true);
                        }
                    }
                }
                try (InputStream in = Files.newInputStream(path)) {
                    return PDType0Font.load(doc, in, true);
                }
            } catch (Exception ignored) {
            }
        }
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }
}
