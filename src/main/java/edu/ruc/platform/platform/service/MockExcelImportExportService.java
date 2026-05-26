package edu.ruc.platform.platform.service;

import edu.ruc.platform.platform.dto.BatchImportResultResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("mock")
public class MockExcelImportExportService implements ExcelImportExportService {

    @Override
    public BatchImportResultResponse importUsers(MultipartFile file) {
        return new BatchImportResultResponse(0, 0, 0, java.util.List.of());
    }

    @Override
    public byte[] exportUsers(String role, Boolean enabled) {
        return new byte[0];
    }

    @Override
    public BatchImportResultResponse importStudents(MultipartFile file) {
        return new BatchImportResultResponse(0, 0, 0, java.util.List.of());
    }

    @Override
    public byte[] exportStudents(String grade, String className, String status) {
        return new byte[0];
    }
}
