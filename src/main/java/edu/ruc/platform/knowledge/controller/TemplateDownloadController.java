package edu.ruc.platform.knowledge.controller;

import edu.ruc.platform.common.exception.BusinessException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
public class TemplateDownloadController {

    @GetMapping({"/templates/{fileName}", "/api/v1/templates/{fileName}"})
    public ResponseEntity<ByteArrayResource> download(@PathVariable String fileName) {
        String normalized = StringUtils.getFilename(fileName);
        if (normalized == null || normalized.isBlank() || normalized.contains("..")) {
            throw new BusinessException("模板文件名非法");
        }
        normalized = normalized.trim();

        TemplatePayload payload = switch (normalized.toLowerCase(Locale.ROOT)) {
            case "study-certificate.docx" -> buildDocx("在读证明模板", "本模板用于开具在读证明。", defaultDocxBody());
            case "leave-request.docx" -> buildDocx("请假申请表", "本模板用于学生请假申请。", leaveDocxBody());
            case "activity-budget.xlsx" -> buildXlsxBudget();
            case "knowledge-import.xlsx" -> buildXlsxKnowledgeImport();
            case "party-materials.zip" -> buildZipPartyMaterials();
            default -> throw new BusinessException("模板不存在");
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(payload.mediaType());
        headers.setContentDisposition(ContentDisposition.attachment().filename(payload.fileName(), StandardCharsets.UTF_8).build());
        headers.setContentLength(payload.bytes().length);
        return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(payload.bytes()));
    }

    private TemplatePayload buildDocx(String title, String subtitle, String body) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph h = doc.createParagraph();
            h.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun hr = h.createRun();
            hr.setBold(true);
            hr.setFontSize(18);
            hr.setText(title);

            XWPFParagraph s = doc.createParagraph();
            s.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun sr = s.createRun();
            sr.setFontSize(11);
            sr.setText(subtitle);

            XWPFParagraph p = doc.createParagraph();
            p.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun pr = p.createRun();
            pr.setFontSize(12);
            pr.setText(body);

            doc.write(out);
            return new TemplatePayload(
                    title.replaceAll("\\s+", "") + ".docx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
                    out.toByteArray()
            );
        } catch (IOException e) {
            throw new BusinessException("生成模板失败");
        }
    }

    private TemplatePayload buildXlsxBudget() {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("预算表");

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            XSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"序号", "项目", "单价(元)", "数量", "小计(元)", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i == 1 ? 18 * 256 : 14 * 256);
            }

            for (int r = 1; r <= 8; r++) {
                Row row = sheet.createRow(r);
                for (int c = 0; c < headers.length; c++) {
                    var cell = row.createCell(c);
                    cell.setCellStyle(cellStyle);
                }
            }

            wb.write(out);
            return new TemplatePayload(
                    "活动预算表.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    out.toByteArray()
            );
        } catch (IOException e) {
            throw new BusinessException("生成模板失败");
        }
    }

    private TemplatePayload buildXlsxKnowledgeImport() {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("知识库导入");

            XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"标题", "分类", "标准答案", "官方链接", "发布状态(YES/NO)", "受众范围"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }

            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("奖助学金申请流程");
            sample.createCell(1).setCellValue("奖助学金");
            sample.createCell(2).setCellValue("请在学院官网-奖助学金栏目下载表格并按通知提交。");
            sample.createCell(3).setCellValue("https://example.edu/notice");
            sample.createCell(4).setCellValue("YES");
            sample.createCell(5).setCellValue("全体学生");

            wb.write(out);
            return new TemplatePayload(
                    "知识库导入模板.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    out.toByteArray()
            );
        } catch (IOException e) {
            throw new BusinessException("生成模板失败");
        }
    }

    private TemplatePayload buildZipPartyMaterials() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            zipEntry(zip, "入党/材料清单.txt", partyMaterialsReadme().getBytes(StandardCharsets.UTF_8));
            zipEntry(zip, "在读证明模板.docx", buildDocx("在读证明模板", "用于党团事务材料准备。", defaultDocxBody()).bytes());
            zipEntry(zip, "请假申请表.docx", buildDocx("请假申请表", "用于党团活动请假。", leaveDocxBody()).bytes());
            zip.close();
            return new TemplatePayload(
                    "党团事务材料模板.zip",
                    MediaType.parseMediaType("application/zip"),
                    out.toByteArray()
            );
        } catch (IOException e) {
            throw new BusinessException("生成模板失败");
        }
    }

    private void zipEntry(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }

    private String defaultDocxBody() {
        return """
                兹证明：________（姓名），学号________，现为我院________专业在读学生。

                证明用途：________
                开具日期：%s

                学院盖章：
                """.formatted(LocalDate.now());
    }

    private String leaveDocxBody() {
        return """
                学生姓名：________    学号：________    班级：________
                请假类型：事假/病假/其他（请圈选）
                请假时间：____年__月__日 至 ____年__月__日
                请假事由：____________________________________

                学生签名：________    联系方式：________
                班主任/辅导员意见：____________________________
                """;
    }

    private String partyMaterialsReadme() {
        return """
                党团事务常用材料（示例清单）
                1. 入党申请书（纸质/电子）
                2. 思想汇报（按阶段提交）
                3. 在读证明
                4. 活动与培训相关证明材料

                本压缩包内为示例模板，请按学院最新通知要求使用。
                """;
    }

    private record TemplatePayload(String fileName, MediaType mediaType, byte[] bytes) {
    }
}

