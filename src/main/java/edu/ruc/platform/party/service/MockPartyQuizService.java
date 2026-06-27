package edu.ruc.platform.party.service;

import edu.ruc.platform.common.exception.BusinessException;
import edu.ruc.platform.party.dto.PartyQuestionBankCreateRequest;
import edu.ruc.platform.party.dto.PartyQuestionBankResponse;
import edu.ruc.platform.party.dto.PartyQuestionCreateRequest;
import edu.ruc.platform.party.dto.PartyQuestionResponse;
import edu.ruc.platform.party.dto.PartyQuizRecordResponse;
import edu.ruc.platform.party.dto.PartyQuizResultResponse;
import edu.ruc.platform.party.dto.PartyQuizSubmitRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Profile("mock")
public class MockPartyQuizService implements PartyQuizApplicationService {

    private final AtomicLong bankIdGenerator = new AtomicLong(1);
    private final AtomicLong questionIdGenerator = new AtomicLong(1);
    private final AtomicLong recordIdGenerator = new AtomicLong(1);

    private final Map<Long, PartyQuestionBankResponse> banksById = new ConcurrentHashMap<>();
    private final Map<Long, List<PartyQuestionResponse>> questionsByBankId = new ConcurrentHashMap<>();
    private final Map<Long, List<PartyQuizRecordResponse>> recordsByStudentId = new ConcurrentHashMap<>();

    @Override
    public List<PartyQuestionBankResponse> listQuestionBanks() {
        return banksById.values().stream()
                .sorted(Comparator.comparing(PartyQuestionBankResponse::id))
                .toList();
    }

    @Override
    public PartyQuestionBankResponse getQuestionBankById(Long id) {
        PartyQuestionBankResponse bank = banksById.get(id);
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        List<PartyQuestionResponse> questions = questionsByBankId.getOrDefault(id, List.of()).stream()
                .sorted(Comparator.comparingInt(PartyQuestionResponse::seqNo))
                .toList();
        return new PartyQuestionBankResponse(
                bank.id(),
                bank.bankCode(),
                bank.bankName(),
                bank.category(),
                questions.size(),
                bank.isActive(),
                bank.description(),
                questions
        );
    }

    @Override
    public PartyQuestionBankResponse createQuestionBank(PartyQuestionBankCreateRequest request) {
        Long id = bankIdGenerator.incrementAndGet();
        PartyQuestionBankResponse created = new PartyQuestionBankResponse(
                id,
                request.bankCode(),
                request.bankName(),
                request.category(),
                0,
                true,
                request.description(),
                List.of()
        );
        banksById.put(id, created);
        questionsByBankId.put(id, new ArrayList<>());
        return created;
    }

    @Override
    public PartyQuestionResponse addQuestion(Long bankId, PartyQuestionCreateRequest request) {
        if (!banksById.containsKey(bankId)) {
            throw new BusinessException("题库不存在");
        }
        Long id = questionIdGenerator.incrementAndGet();
        PartyQuestionResponse question = new PartyQuestionResponse(
                id,
                request.seqNo(),
                request.questionText(),
                request.options(),
                request.correctAnswer(),
                request.explanation(),
                request.score() != null ? request.score() : 1
        );
        questionsByBankId.computeIfAbsent(bankId, key -> new ArrayList<>()).add(question);
        PartyQuestionBankResponse bank = banksById.get(bankId);
        List<PartyQuestionResponse> questions = questionsByBankId.getOrDefault(bankId, List.of());
        banksById.put(bankId, new PartyQuestionBankResponse(
                bank.id(),
                bank.bankCode(),
                bank.bankName(),
                bank.category(),
                questions.size(),
                bank.isActive(),
                bank.description(),
                List.of()
        ));
        return question;
    }

    @Override
    public PartyQuizResultResponse submitQuiz(Long studentId, PartyQuizSubmitRequest request) {
        PartyQuestionBankResponse bank = banksById.get(request.bankId());
        if (bank == null) {
            throw new BusinessException("题库不存在");
        }
        List<PartyQuestionResponse> questions = questionsByBankId.getOrDefault(request.bankId(), List.of());
        Map<Long, String> answers = request.answers() == null ? Map.of() : request.answers();

        int totalQuestions = questions.size();
        int correctCount = 0;
        int score = 0;
        int totalScore = questions.stream().mapToInt(q -> q.score() == null ? 1 : q.score()).sum();
        List<PartyQuizResultResponse.PartyQuizAnswerDetail> details = new ArrayList<>();

        for (PartyQuestionResponse q : questions) {
            String yourAnswer = answers.get(q.id());
            boolean correct = yourAnswer != null && yourAnswer.equalsIgnoreCase(q.correctAnswer());
            int qScore = q.score() == null ? 1 : q.score();
            if (correct) {
                correctCount++;
                score += qScore;
            }
            details.add(new PartyQuizResultResponse.PartyQuizAnswerDetail(
                    q.id(),
                    q.questionText(),
                    yourAnswer,
                    q.correctAnswer(),
                    correct,
                    qScore,
                    q.explanation()
            ));
        }

        boolean passed = totalScore == 0 ? true : score * 1.0 / totalScore >= 0.6;
        Long recordId = recordIdGenerator.incrementAndGet();
        LocalDateTime completedAt = LocalDateTime.now();

        PartyQuizRecordResponse record = new PartyQuizRecordResponse(
                recordId,
                request.bankId(),
                bank.bankName(),
                totalQuestions,
                correctCount,
                score,
                totalScore,
                passed,
                completedAt
        );
        recordsByStudentId.computeIfAbsent(studentId, key -> new ArrayList<>()).add(record);

        return new PartyQuizResultResponse(
                recordId,
                studentId,
                request.bankId(),
                bank.bankName(),
                totalQuestions,
                correctCount,
                score,
                totalScore,
                passed,
                details,
                completedAt
        );
    }

    @Override
    public List<PartyQuizRecordResponse> listQuizRecords(Long studentId) {
        return recordsByStudentId.getOrDefault(studentId, List.of()).stream()
                .sorted((a, b) -> b.completedAt().compareTo(a.completedAt()))
                .toList();
    }
}
